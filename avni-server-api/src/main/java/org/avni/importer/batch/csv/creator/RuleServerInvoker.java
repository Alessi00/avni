package org.avni.importer.batch.csv.creator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import org.avni.application.Form;
import org.avni.domain.Encounter;
import org.avni.domain.Individual;
import org.avni.domain.ProgramEncounter;
import org.avni.domain.ProgramEnrolment;
import org.avni.importer.batch.csv.contract.UploadRuleServerRequestContract;
import org.avni.importer.batch.csv.contract.UploadRuleServerResponseContract;
import org.avni.importer.batch.model.Row;
import org.avni.service.EntityApprovalStatusService;
import org.avni.service.ObservationService;
import org.avni.util.ObjectMapperSingleton;
import org.avni.web.RestClient;
import org.avni.web.request.rules.RulesContractWrapper.EncounterContractWrapper;
import org.avni.web.request.rules.RulesContractWrapper.IndividualContractWrapper;
import org.avni.web.request.rules.RulesContractWrapper.ProgramEncounterContractWrapper;
import org.avni.web.request.rules.RulesContractWrapper.ProgramEnrolmentContractWrapper;
import org.avni.web.request.rules.constructWrappers.ProgramEncounterConstructionService;
import org.avni.web.request.rules.constructWrappers.ProgramEnrolmentConstructionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RuleServerInvoker {

    private RestClient restClient;
    private ProgramEnrolmentConstructionService programEnrolmentConstructionService;
    private ObservationService observationService;
    private ProgramEncounterConstructionService programEncounterConstructionService;
    private EntityApprovalStatusService entityApprovalStatusService;

    @Autowired
    public RuleServerInvoker(RestClient restClient,
                             ProgramEnrolmentConstructionService programEnrolmentConstructionService,
                             ObservationService observationService,
                             ProgramEncounterConstructionService programEncounterConstructionService,
                             EntityApprovalStatusService entityApprovalStatusService) {
        this.restClient = restClient;
        this.programEnrolmentConstructionService = programEnrolmentConstructionService;
        this.observationService = observationService;
        this.programEncounterConstructionService = programEncounterConstructionService;
        this.entityApprovalStatusService = entityApprovalStatusService;
    }

    private UploadRuleServerResponseContract invokeRuleServer(Row row, Form form, Object entity, List<String> allErrorMsgs) throws Exception {
        ObjectMapper mapper = ObjectMapperSingleton.getObjectMapper();
        mapper.registerModule(new JodaModule());
        UploadRuleServerRequestContract contract = UploadRuleServerRequestContract.buildRuleServerContract(row, form, entity);
        String ruleResponse = restClient.post("/api/upload", contract);
        UploadRuleServerResponseContract uploadRuleServerResponseContract = mapper.readValue(ruleResponse, UploadRuleServerResponseContract.class);
        allErrorMsgs.addAll(uploadRuleServerResponseContract.getErrors());
        if (allErrorMsgs.size() > 0) {
            throw new Exception(String.join(", ", allErrorMsgs));
        }
        return uploadRuleServerResponseContract;
    }

    public UploadRuleServerResponseContract getRuleServerResult(Row row, Form form, Individual individual, List<String> allErrorMsgs) throws Exception {
        IndividualContractWrapper entity = programEnrolmentConstructionService.constructBasicSubject(individual);
        UploadRuleServerResponseContract ruleResponse = invokeRuleServer(row, form, entity, allErrorMsgs);
        return ruleResponse;
    }

    public UploadRuleServerResponseContract getRuleServerResult(Row row, Form form, ProgramEnrolment programEnrolment, List<String> allErrorMsgs) throws Exception {
        ProgramEnrolmentContractWrapper entity = programEnrolmentConstructionService.constructProgramEnrolmentContract(programEnrolment);
        UploadRuleServerResponseContract ruleResponse = invokeRuleServer(row, form, entity, allErrorMsgs);
        return ruleResponse;
    }

    public UploadRuleServerResponseContract getRuleServerResult(Row row, Form form, ProgramEncounter programEncounter, List<String> allErrorMsgs) throws Exception {
        ProgramEncounterContractWrapper entity = programEncounterConstructionService.constructProgramEncounterContractWrapper(programEncounter);
        entity.setProgramEnrolment(programEncounterConstructionService.constructEnrolments(programEncounter.getProgramEnrolment(), programEncounter.getUuid()));
        UploadRuleServerResponseContract ruleResponse = invokeRuleServer(row, form, entity, allErrorMsgs);
        return ruleResponse;
    }

    public UploadRuleServerResponseContract getRuleServerResult(Row row, Form form, Encounter encounter, List<String> allErrorMsgs) throws Exception {
        EncounterContractWrapper entity = EncounterContractWrapper.fromEncounter(encounter, observationService, entityApprovalStatusService);
        entity.setSubject(programEnrolmentConstructionService.getSubjectInfo(encounter.getIndividual()));
        UploadRuleServerResponseContract ruleResponse = invokeRuleServer(row, form, entity, allErrorMsgs);
        return ruleResponse;
    }
}
