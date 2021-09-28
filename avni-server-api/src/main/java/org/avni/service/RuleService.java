package org.avni.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import org.codehaus.jettison.json.JSONException;
import org.avni.application.Form;
import org.avni.application.RuleType;
import org.avni.dao.*;
import org.avni.dao.application.FormRepository;
import org.avni.dao.individualRelationship.RuleFailureLogRepository;
import org.avni.domain.*;
import org.avni.framework.security.AuthenticationFilter;
import org.avni.framework.security.UserContextHolder;
import org.avni.util.ObjectMapperSingleton;
import org.avni.web.RestClient;
import org.avni.web.request.RuleRequest;
import org.avni.web.request.rules.RulesContractWrapper.EncounterContractWrapper;
import org.avni.web.request.rules.RulesContractWrapper.IndividualContractWrapper;
import org.avni.web.request.rules.RulesContractWrapper.ProgramEncounterContractWrapper;
import org.avni.web.request.rules.RulesContractWrapper.ProgramEnrolmentContractWrapper;
import org.avni.web.request.rules.constant.WorkFlowTypeEnum;
import org.avni.web.request.rules.constructWrappers.IndividualConstructionService;
import org.avni.web.request.rules.constructWrappers.ProgramEncounterConstructionService;
import org.avni.web.request.rules.constructWrappers.ProgramEnrolmentConstructionService;
import org.avni.web.request.rules.request.*;
import org.avni.web.request.rules.response.DecisionResponseEntity;
import org.avni.web.request.rules.response.KeyValueResponse;
import org.avni.web.request.rules.response.RuleError;
import org.avni.web.request.rules.response.RuleResponseEntity;
import org.avni.web.request.rules.validateRules.RuleValidationService;
import org.avni.web.validation.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.transaction.Transactional;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class RuleService {
    private final Logger logger;
    private final RuleDependencyRepository ruleDependencyRepository;
    private final RuleRepository ruleRepository;
    private final Map<RuledEntityType, CHSRepository> ruledEntityRepositories;
    private final RestClient restClient;
    private final IndividualConstructionService individualConstructionService;
    private final RuleValidationService ruleValidationService;
    private final ProgramEncounterConstructionService programEncounterConstructionService;
    private final ProgramEnrolmentConstructionService programEnrolmentConstructionService;
    private final FormRepository formRepository;
    private final RuleFailureLogRepository ruleFailureLogRepository;
    private final ObservationService observationService;
    private final EntityApprovalStatusService entityApprovalStatusService;

    @Autowired
    public RuleService(RuleDependencyRepository ruleDependencyRepository,
                       RuleRepository ruleRepository,
                       FormRepository formRepository,
                       ProgramRepository programRepository,
                       EncounterTypeRepository encounterTypeRepository,
                       RestClient restClient,
                       IndividualConstructionService individualConstructionService,
                       RuleValidationService ruleValidationService,
                       ProgramEncounterConstructionService programEncounterConstructionService,
                       ProgramEnrolmentConstructionService programEnrolmentConstructionService,
                       RuleFailureLogRepository ruleFailureLogRepository,
                       ObservationService observationService,
                       EntityApprovalStatusService entityApprovalStatusService) {
        this.ruleFailureLogRepository = ruleFailureLogRepository;
        this.observationService = observationService;
        this.entityApprovalStatusService = entityApprovalStatusService;
        logger = LoggerFactory.getLogger(this.getClass());
        this.ruleDependencyRepository = ruleDependencyRepository;
        this.ruleRepository = ruleRepository;
        this.ruledEntityRepositories = new HashMap<RuledEntityType, CHSRepository>() {{
            put(RuledEntityType.Form, formRepository);
            put(RuledEntityType.Program, programRepository);
            put(RuledEntityType.EncounterType, encounterTypeRepository);
        }};
        this.restClient = restClient;
        this.individualConstructionService = individualConstructionService;
        this.ruleValidationService = ruleValidationService;
        this.programEncounterConstructionService = programEncounterConstructionService;
        this.programEnrolmentConstructionService = programEnrolmentConstructionService;
        this.formRepository = formRepository;
    }

    @Transactional
    public RuleDependency createDependency(String ruleCode, String ruleHash) {
        RuleDependency ruleDependency = ruleDependencyRepository
                .findByOrganisationId(UserContextHolder.getUserContext().getOrganisation().getId());
        if (ruleDependency == null) ruleDependency = new RuleDependency();
        if (ruleHash.equals(ruleDependency.getChecksum())) return ruleDependency;
        ruleDependency.setCode(ruleCode);
        ruleDependency.setChecksum(ruleHash);
        ruleDependency.assignUUIDIfRequired();
        logger.info(String.format("Rule dependency with UUID: %s", ruleDependency.getUuid()));
        return ruleDependencyRepository.save(ruleDependency);
    }

    private Rule _setCommonAttributes(Rule rule, RuleRequest ruleRequest) {
        rule.setUuid(ruleRequest.getUuid());
        rule.setData(new RuleData(ruleRequest.getData()));
        rule.setName(ruleRequest.getName());
        rule.setFnName(ruleRequest.getFnName());
        rule.setType(RuleType.valueOf(StringUtils.capitalize(ruleRequest.getType())));
        rule.setExecutionOrder(ruleRequest.getExecutionOrder());
        rule.setVoided(false);
        return rule;
    }

    @Transactional
    public Rule createOrUpdate(RuleRequest ruleRequest) {
        String ruleDependencyUUID = ruleRequest.getRuleDependencyUUID();
        RuleDependency ruleDependency = ruleDependencyRepository.findByUuid(ruleDependencyUUID);
        String ruleUUID = ruleRequest.getUuid();
        Rule rule = ruleRepository.findByUuid(ruleUUID);
        if (rule == null) rule = new Rule();
        rule.setRuleDependency(ruleDependency);
        rule = this._setCommonAttributes(rule, ruleRequest);
        String entityUUID = ruleRequest.getEntityUUID();

        checkEntityExists(ruleRequest);

        rule.setEntity(ruleRequest.getEntity());

        logger.info(String.format("Creating Rule with UUID '%s', Name '%s', Type '%s', Entity '%s'",
                rule.getUuid(), rule.getName(), rule.getType(), ruleRequest.getEntityType()));

        return ruleRepository.save(rule);
    }

    private void checkEntityExists(RuleRequest ruleRequest) {
        String entityUUID = ruleRequest.getEntityUUID();
        if (!RuledEntityType.isNone(ruleRequest.getEntityType())) {
            CHSRepository chsRepository = ruledEntityRepositories.get(ruleRequest.getEntityType());
            if (chsRepository.findByUuid(entityUUID) == null) {
                throw new ValidationException(String.format("%s with uuid: %s not found for rule with uuid: %s",
                        ruleRequest.getEntityType(), entityUUID, ruleRequest.getUuid()));
            }
        }
    }

    @Transactional
    public void createOrUpdate(List<RuleRequest> rules) {
        List<Rule> rulesFromDB = ruleRepository.findByOrganisationId(UserContextHolder.getUserContext().getOrganisation().getId());
        List<String> newRuleUUIDs = rules.stream()
                .map(this::createOrUpdate)
                .map(Rule::getUuid)
                .collect(Collectors.toList());

        Stream<Rule> deletedRules = rulesFromDB.stream().filter(r -> !newRuleUUIDs.contains(r.getUuid()));

        deletedRules.peek(vr -> vr.setVoided(true)).forEach(ruleRepository::save);
    }

    public RuleResponseEntity executeProgramSummaryRule(ProgramEnrolment programEnrolment) {
        RuleResponseEntity ruleResponseEntity = new RuleResponseEntity();
        if (programEnrolment == null) {
            ruleResponseEntity.setStatus(HttpStatus.NOT_FOUND.toString());
            return ruleResponseEntity;
        }
        RuleRequestEntity rule = new RuleRequestEntity();
        Program program = programEnrolment.getProgram();
        rule.setProgramSummaryCode(program.getEnrolmentSummaryRule());
        String workFlowType = WorkFlowTypeEnum.PROGRAM_SUMMARY.getWorkFlowTypeName();
        rule.setWorkFlowType(workFlowType);
        rule.setFormUuid(program.getUuid());
        rule.setRuleType("Program Summary");
        ProgramEnrolmentContractWrapper programEnrolmentContractWrapper = ProgramEnrolmentContractWrapper.fromEnrolment(programEnrolment, observationService, entityApprovalStatusService);
        programEnrolmentContractWrapper.setRule(rule);
        Set<ProgramEncounterContractWrapper> programEncountersContracts = programEnrolment.getProgramEncounters().stream().map(programEncounterConstructionService::constructProgramEncounterContractWrapper).collect(Collectors.toSet());
        programEnrolmentContractWrapper.setProgramEncounters(programEncountersContracts);
        programEnrolmentContractWrapper.setSubject(programEnrolmentConstructionService.getSubjectInfo(programEnrolment.getIndividual()));
        RuleFailureLog ruleFailureLog = ruleValidationService.generateRuleFailureLog(rule, "Web", "Rules : " + workFlowType, programEnrolment.getUuid());
        ruleResponseEntity = createHttpHeaderAndSendRequest("/api/summaryRule", programEnrolmentContractWrapper, ruleFailureLog);
        setObservationsOnResponse(workFlowType, ruleResponseEntity);
        return ruleResponseEntity;
    }

    public RuleResponseEntity executeSubjectSummaryRule(Individual individual) {
        RuleResponseEntity ruleResponseEntity = new RuleResponseEntity();
        if (individual == null) {
            ruleResponseEntity.setStatus(HttpStatus.NOT_FOUND.toString());
            return ruleResponseEntity;
        }
        RuleRequestEntity rule = new RuleRequestEntity();
        SubjectType subjectType = individual.getSubjectType();
        rule.setSubjectSummaryCode(subjectType.getSubjectSummaryRule());
        String workFlowType = WorkFlowTypeEnum.SUBJECT_SUMMARY.getWorkFlowTypeName();
        rule.setWorkFlowType(workFlowType);
        rule.setFormUuid(subjectType.getUuid());
        rule.setRuleType("Subject Summary");
        IndividualContractWrapper individualContractWrapper = programEnrolmentConstructionService.getSubjectInfo(individual);
        individualContractWrapper.setRule(rule);
        RuleFailureLog ruleFailureLog = ruleValidationService.generateRuleFailureLog(rule, "Web", "Rules : " + workFlowType, individual.getUuid());
        ruleResponseEntity = createHttpHeaderAndSendRequest("/api/summaryRule", individualContractWrapper, ruleFailureLog);
        setObservationsOnResponse(workFlowType, ruleResponseEntity);
        return ruleResponseEntity;
    }

    public RuleResponseEntity executeServerSideRules(RequestEntityWrapper requestEntityWrapper) throws IOException, JSONException {
        RuleRequestEntity rule = requestEntityWrapper.getRule();
        Form form = formRepository.findByUuid(rule.getFormUuid());
        rule.setDecisionCode(form.getDecisionRule());
        rule.setVisitScheduleCode(form.getVisitScheduleRule());
        rule.setChecklistCode(form.getChecklistsRule());

        Object entity = null;
        String entityUuid = null;
        String workFlowType = requestEntityWrapper.getRule().getWorkFlowType();
        switch (WorkFlowTypeEnum.findByValue(workFlowType.toLowerCase())) {
            case PROGRAM_ENROLMENT:
                ProgramEnrolmentRequestEntity programEnrolmentRequestEntity = requestEntityWrapper.getProgramEnrolmentRequestEntity();
                entityUuid = programEnrolmentRequestEntity.getUuid();
                ProgramEnrolmentContractWrapper programEnrolmentContractWrapper = programEnrolmentConstructionService.constructProgramEnrolmentContract(programEnrolmentRequestEntity);
                programEnrolmentContractWrapper.setRule(rule);
                programEnrolmentContractWrapper.setVisitSchedules(new ArrayList<>());
                programEnrolmentContractWrapper.setChecklistDetails(programEnrolmentConstructionService.constructChecklistDetailRequest());
                entity = programEnrolmentContractWrapper;
                break;
            case PROGRAM_ENCOUNTER:
                ProgramEncounterRequestEntity programEncounterRequestEntity = requestEntityWrapper.getProgramEncounterRequestEntity();
                entityUuid = programEncounterRequestEntity.getUuid();
                ProgramEncounterContractWrapper programEncounterContractWrapper = programEncounterConstructionService.constructProgramEncounterContract(programEncounterRequestEntity);
                programEncounterContractWrapper.setRule(rule);
                programEncounterContractWrapper.setVisitSchedules(programEncounterConstructionService.constructProgramEnrolmentVisitScheduleContract(programEncounterRequestEntity));
                entity = programEncounterContractWrapper;
                break;
            case ENCOUNTER:
                EncounterRequestEntity encounterRequestEntity = requestEntityWrapper.getEncounterRequestEntity();
                entityUuid = encounterRequestEntity.getUuid();
                EncounterContractWrapper encounterContractWrapper = programEncounterConstructionService.constructEncounterContract(encounterRequestEntity);
                encounterContractWrapper.setRule(rule);
                encounterContractWrapper.setVisitSchedules(programEncounterConstructionService.constructIndividualVisitScheduleContract(encounterRequestEntity));
                entity = encounterContractWrapper;
                break;
            case INDIVIDUAL:
                IndividualRequestEntity individualRequestEntity = requestEntityWrapper.getIndividualRequestEntity();
                entityUuid = individualRequestEntity.getUuid();
                IndividualContractWrapper individualContractWrapper = individualConstructionService.constructIndividualContract(individualRequestEntity);
                individualContractWrapper.setRule(rule);
                individualContractWrapper.setVisitSchedules(new ArrayList<>());
                entity = individualContractWrapper;
                break;
        }

        RuleFailureLog ruleFailureLog = ruleValidationService.generateRuleFailureLog(rule, "Web", "Rules : " + workFlowType, entityUuid);
        RuleResponseEntity ruleResponseEntity = createHttpHeaderAndSendRequest("/api/rules", entity, ruleFailureLog);
        setObservationsOnResponse(workFlowType, ruleResponseEntity);
        return ruleResponseEntity;
    }

    private void setObservationsOnResponse(String workFlowType, RuleResponseEntity ruleResponseEntity) {
        DecisionResponseEntity decisions = ruleResponseEntity.getDecisions();
        List<KeyValueResponse> summaries = ruleResponseEntity.getSummaries();
        WorkFlowTypeEnum workFlowTypeEnum = WorkFlowTypeEnum.findByValue(workFlowType.toLowerCase());

        switch (workFlowTypeEnum) {
            case PROGRAM_ENROLMENT:
                decisions.setEnrolmentObservations(observationService.createObservationContractsFromKeyValueResponse(decisions.getEnrolmentDecisions(), workFlowTypeEnum));
                decisions.setRegistrationObservations(observationService.createObservationContractsFromKeyValueResponse(decisions.getRegistrationDecisions(), workFlowTypeEnum));
                break;
            case PROGRAM_ENCOUNTER:
                decisions.setEncounterObservations(observationService.createObservationContractsFromKeyValueResponse(decisions.getEncounterDecisions(), workFlowTypeEnum));
                decisions.setEnrolmentObservations(observationService.createObservationContractsFromKeyValueResponse(decisions.getEnrolmentDecisions(), workFlowTypeEnum));
                decisions.setRegistrationObservations(observationService.createObservationContractsFromKeyValueResponse(decisions.getRegistrationDecisions(), workFlowTypeEnum));
                break;
            case ENCOUNTER:
                decisions.setEncounterObservations(observationService.createObservationContractsFromKeyValueResponse(decisions.getEncounterDecisions(), workFlowTypeEnum));
                decisions.setRegistrationObservations(observationService.createObservationContractsFromKeyValueResponse(decisions.getRegistrationDecisions(), workFlowTypeEnum));
                break;
            case INDIVIDUAL:
                decisions.setRegistrationObservations(observationService.createObservationContractsFromKeyValueResponse(decisions.getRegistrationDecisions(), workFlowTypeEnum));
                break;
            case PROGRAM_SUMMARY:
            case SUBJECT_SUMMARY:
                ruleResponseEntity.setSummaryObservations(observationService.createObservationContractsFromKeyValueResponse(summaries, workFlowTypeEnum));
                break;
        }
    }

    private RuleResponseEntity emptySuccessEntity() {
        RuleResponseEntity entity = new RuleResponseEntity();
        entity.setStatus("success");
        return entity;
    }

    private RuleResponseEntity createHttpHeaderAndSendRequest(String url, Object contractObject, RuleFailureLog ruleFailureLog) {
        try {
            HttpHeaders httpHeaders = constructHeaders();
            ObjectMapper mapper = ObjectMapperSingleton.getObjectMapper();
            mapper.registerModule(new JodaModule());
            String ruleResponse = restClient.post(url, contractObject, httpHeaders);
            RuleResponseEntity ruleResponseEntity = mapper.readValue(ruleResponse, RuleResponseEntity.class);
            if (ruleResponseEntity.getStatus().equals("failure")) {
                RuleError ruleError = ruleResponseEntity.getError();
                saveRuleError(ruleFailureLog, ruleError.getMessage(), ruleError.getStack());
            }
            return ruleResponseEntity;
        } catch (Exception e) {
            saveRuleError(ruleFailureLog, e.getMessage(), getStackTrace(e));
            return getFailureRuleResponseEntity(e);
        }
    }

    private HttpHeaders constructHeaders() {
        HttpHeaders httpHeaders = new HttpHeaders();
        UserContext userContext = UserContextHolder.getUserContext();
        String userName = userContext.getUserName();
        String organisationUUID = userContext.getOrganisation().getUuid();
        String authToken = userContext.getAuthToken();

        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        if(userName != null)
            httpHeaders.add(AuthenticationFilter.USER_NAME_HEADER, userName);
        if(organisationUUID != null)
            httpHeaders.add(AuthenticationFilter.ORGANISATION_UUID, organisationUUID);
        if(authToken != null)
            httpHeaders.add(AuthenticationFilter.AUTH_TOKEN_HEADER, authToken);
        return httpHeaders;
    }

    private RuleResponseEntity getFailureRuleResponseEntity(Exception e) {
        RuleResponseEntity ruleResponseEntity = new RuleResponseEntity();
        RuleError ruleError = new RuleError();
        ruleError.setMessage(e.getMessage());
        ruleError.setStack(getStackTrace(e));
        ruleResponseEntity.setStatus("failure");
        ruleResponseEntity.setError(ruleError);
        return ruleResponseEntity;
    }

    private String getStackTrace(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }

    private void saveRuleError(RuleFailureLog ruleFailureLog, String message, String stack) {
        ruleFailureLog.setErrorMessage(message);
        ruleFailureLog.setStacktrace(stack);
        ruleFailureLogRepository.save(ruleFailureLog);
    }


}
