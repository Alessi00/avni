package org.avni.service;

import org.apache.commons.collections4.IterableUtils;
import org.avni.application.FormMapping;
import org.avni.dao.*;
import org.avni.dao.application.FormMappingRepository;
import org.avni.domain.*;
import org.avni.framework.security.UserContextHolder;
import org.avni.web.request.GroupPrivilegeContractWeb;
import org.joda.time.DateTime;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class GroupPrivilegeService implements NonScopeAwareService {
    private GroupRepository groupRepository;
    private PrivilegeRepository privilegeRepository;
    private SubjectTypeRepository subjectTypeRepository;
    private ProgramRepository programRepository;
    private EncounterTypeRepository encounterTypeRepository;
    private ChecklistDetailRepository checklistDetailRepository;
    private FormMappingRepository formMappingRepository;
    private GroupPrivilegeRepository groupPrivilegeRepository;
    private List<String> groupSubjectPrivileges = new ArrayList<String>() {{
        add("Add member");
        add("Edit member");
        add("Remove member");
    }};
    private UserGroupRepository userGroupRepository;

    public GroupPrivilegeService(GroupRepository groupRepository, PrivilegeRepository privilegeRepository, SubjectTypeRepository subjectTypeRepository, ProgramRepository programRepository, EncounterTypeRepository encounterTypeRepository, ChecklistDetailRepository checklistDetailRepository, FormMappingRepository formMappingRepository, GroupPrivilegeRepository groupPrivilegeRepository, UserGroupRepository userGroupRepository) {
        this.groupRepository = groupRepository;
        this.privilegeRepository = privilegeRepository;
        this.subjectTypeRepository = subjectTypeRepository;
        this.programRepository = programRepository;
        this.encounterTypeRepository = encounterTypeRepository;
        this.checklistDetailRepository = checklistDetailRepository;
        this.formMappingRepository = formMappingRepository;
        this.groupPrivilegeRepository = groupPrivilegeRepository;
        this.userGroupRepository = userGroupRepository;
    }

    private boolean isGroupSubjectTypePrivilege(SubjectType subjectType, String privilegeName) {
        if (!subjectType.isGroup()) {
            return !groupSubjectPrivileges.contains(privilegeName);
        }
        return true;
    }

    public List<GroupPrivilege> getAllGroupPrivileges(Long groupId) {

        List<FormMapping> formMappings = formMappingRepository.findAllByIsVoidedFalse();
        List<SubjectType.SubjectTypeProjection> subjectTypes = subjectTypeRepository.findAllOperational();

        List<Program.ProgramProjection> operationalPrograms = programRepository.findAllOperational();
        Set<Long> operationalProgramIds = operationalPrograms.stream().map(Program.ProgramProjection::getId).collect(Collectors.toSet());

        List<EncounterType.EncounterTypeProjection> operationalEncounterTypes = encounterTypeRepository.findAllOperational();
        Set<Long> operationalEncounterTypeIds = operationalEncounterTypes.stream().map(EncounterType.EncounterTypeProjection::getId).collect(Collectors.toSet());

        List<ChecklistDetail> checklistDetails = checklistDetailRepository.findAllByOrganisationId(UserContextHolder.getUserContext().getOrganisationId());

        Group currentGroup = groupRepository.findOne(groupId);
        List<Privilege> privilegeList = IterableUtils.toList(privilegeRepository.findAllByIsVoidedFalse());

        List<FormMapping> operationalFormMappings = formMappings.stream()
                .filter(formMapping -> (formMapping.getProgram() == null) || (formMapping.getProgram() != null && operationalProgramIds.contains(formMapping.getProgram().getId())))
                .collect(Collectors.toList());

        List<GroupPrivilege> allPrivileges = new ArrayList<>();

        subjectTypes.forEach(subjectTypeProjection -> {
            SubjectType subjectType = subjectTypeRepository.findByUuid(subjectTypeProjection.getUuid());
            privilegeList.stream()
                    .filter(privilege -> privilege.getEntityType() == EntityType.Subject && isGroupSubjectTypePrivilege(subjectType, privilege.getName()))
                    .forEach(subjectPrivilege -> {
                                GroupPrivilege groupPrivilege = new GroupPrivilege();
                                groupPrivilege.setGroup(currentGroup);
                                groupPrivilege.setPrivilege(subjectPrivilege);
                                groupPrivilege.setSubjectType(subjectType);
                                groupPrivilege.setAllow(false);
                                groupPrivilege.assignUUID();
                                allPrivileges.add(groupPrivilege);
                            }
                    );

            operationalFormMappings.forEach(operationalFormMapping -> {
                if (operationalFormMapping.getSubjectType() != subjectType) return;

                Program program = operationalFormMapping.getProgram();
                EncounterType encounterType = operationalFormMapping.getEncounterType();
                if (program != null) {
                    privilegeList.stream()
                            .filter(privilege -> privilege.getEntityType() == EntityType.Enrolment)
                            .forEach(enrolmentPrivilege -> {
                                GroupPrivilege groupPrivilege = new GroupPrivilege();
                                groupPrivilege.setGroup(currentGroup);
                                groupPrivilege.setPrivilege(enrolmentPrivilege);
                                groupPrivilege.setSubjectType(subjectType);
                                groupPrivilege.setProgram(program);
                                groupPrivilege.setAllow(false);
                                groupPrivilege.assignUUID();
                                allPrivileges.add(groupPrivilege);
                            });

                    if (encounterType != null && operationalEncounterTypeIds.contains(encounterType.getId())) {
                        privilegeList.stream()
                                .filter(privilege -> privilege.getEntityType() == EntityType.Encounter)
                                .forEach(encounterPrivilege -> {
                                    GroupPrivilege groupPrivilege = new GroupPrivilege();
                                    groupPrivilege.setGroup(currentGroup);
                                    groupPrivilege.setPrivilege(encounterPrivilege);
                                    groupPrivilege.setSubjectType(subjectType);
                                    groupPrivilege.setProgram(program);
                                    groupPrivilege.setProgramEncounterType(encounterType);
                                    groupPrivilege.setAllow(false);
                                    groupPrivilege.assignUUID();
                                    allPrivileges.add(groupPrivilege);
                                });
                    }

                    checklistDetails.forEach(checklistDetail ->
                            privilegeList.stream()
                                    .filter(privilege -> privilege.getEntityType() == EntityType.Checklist)
                                    .forEach(privilege -> {
                                        GroupPrivilege groupPrivilege = new GroupPrivilege();
                                        groupPrivilege.setGroup(currentGroup);
                                        groupPrivilege.setPrivilege(privilege);
                                        groupPrivilege.setSubjectType(subjectType);
                                        groupPrivilege.setChecklistDetail(checklistDetail);
                                        groupPrivilege.setAllow(false);
                                        groupPrivilege.assignUUID();
                                        allPrivileges.add(groupPrivilege);
                                    })
                    );
                } else {
                    if (encounterType != null && operationalEncounterTypeIds.contains(encounterType.getId())) {

                        privilegeList.stream()
                                .filter(privilege -> privilege.getEntityType() == EntityType.Encounter)
                                .forEach(encounterPrivilege -> {
                                    GroupPrivilege groupPrivilege = new GroupPrivilege();
                                    groupPrivilege.setGroup(currentGroup);
                                    groupPrivilege.setPrivilege(encounterPrivilege);
                                    groupPrivilege.setSubjectType(subjectType);
                                    groupPrivilege.setEncounterType(operationalFormMapping.getEncounterType());
                                    groupPrivilege.setAllow(false);
                                    groupPrivilege.assignUUID();
                                    allPrivileges.add(groupPrivilege);
                                });
                    }
                }
            });
        });

        return allPrivileges;
    }


    public void uploadPrivileges(GroupPrivilegeContractWeb request) {
        GroupPrivilege groupPrivilege = groupPrivilegeRepository.findByUuid(request.getUuid());
        if (groupPrivilege == null) {
            groupPrivilege = new GroupPrivilege();
        }
        groupPrivilege.setUuid(request.getUuid());
        groupPrivilege.setPrivilege(privilegeRepository.findByUuid(request.getPrivilegeUUID()));
        groupPrivilege.setGroup(groupRepository.findByUuid(request.getGroupUUID()));
        groupPrivilege.setSubjectType(subjectTypeRepository.findByUuid(request.getSubjectTypeUUID()));
        groupPrivilege.setProgram(programRepository.findByUuid(request.getProgramUUID()));
        groupPrivilege.setEncounterType(encounterTypeRepository.findByUuid(request.getEncounterTypeUUID()));
        groupPrivilege.setProgramEncounterType(encounterTypeRepository.findByUuid(request.getProgramEncounterTypeUUID()));
        groupPrivilege.setChecklistDetail(checklistDetailRepository.findByUuid(request.getChecklistDetailUUID()));
        groupPrivilege.setAllow(request.isAllow());
        groupPrivilegeRepository.save(groupPrivilege);
    }

    @Override
    public boolean isNonScopeEntityChanged(DateTime lastModifiedDateTime) {
        return groupPrivilegeRepository.existsByAuditLastModifiedDateTimeGreaterThan(lastModifiedDateTime);
    }

    public List<GroupPrivilege> getAllowedPrivilegesForUser() {
        User user = UserContextHolder.getUserContext().getUser();
        return groupPrivilegeRepository.getAllAllowPrivilegesForUser(user.getId());
    }

    public boolean hasPrivilege(String privilegeName, SubjectType subjectType, Program program, EncounterType encounterType, ChecklistDetail checklistDetail) {
        if (this.userHasAllPrivileges()) {
            return true;
        }
        User user = UserContextHolder.getUserContext().getUser();
        List<GroupPrivilege> privileges = groupPrivilegeRepository.getAllAllowPrivilegesForUser(user.getId());
        return privileges.stream().anyMatch(groupPrivilege -> groupPrivilege.matches(privilegeName, subjectType, program, encounterType, checklistDetail));
    }

    public boolean hasViewPrivilege(Individual individual) {
        return this.hasPrivilege("View subject", individual.getSubjectType(),
                null, null, null
        );
    }
    public boolean hasViewPrivilege(ProgramEnrolment programEnrolment) {
        return this.hasPrivilege("View enrolment details",
                programEnrolment.getIndividual().getSubjectType(),
                programEnrolment.getProgram(),
                null, null
        );
    }

    public boolean hasViewPrivilege(Checklist checklist) {
        return this.hasPrivilege("View checklist",
                checklist.getProgramEnrolment().getIndividual().getSubjectType(),
                null,
                null, checklist.getChecklistDetail()
        );
    }

    public boolean hasViewPrivilege(ChecklistItem checklistItem) {
        return this.hasViewPrivilege(checklistItem.getChecklist());
    }

    public boolean hasViewPrivilege(ProgramEncounter programEncounter) {
        return this.hasPrivilege("View visit",
                programEncounter.getProgramEnrolment().getIndividual().getSubjectType(),
                programEncounter.getProgramEnrolment().getProgram(),
                null, null
        );
    }

    public boolean hasViewPrivilege(Encounter encounter) {
        return this.hasPrivilege("View visit",
                encounter.getIndividual().getSubjectType(),
                null, null, null
        );
    }

    public List<GroupPrivilege> getRevokedPrivilegesForUser() {
        User user = UserContextHolder.getUserContext().getUser();
        List<String> allowedPrivilegeTypeUUIDs =  this.getAllowedPrivilegesForUser()
                .stream()
                .map(GroupPrivilege::getTypeUUID)
                .collect(Collectors.toList());
        return groupPrivilegeRepository.getAllRevokedPrivilegesForUser(user.getId())
                .stream()
                .filter(p -> !allowedPrivilegeTypeUUIDs.contains(p.getTypeUUID()))
                .collect(Collectors.toList());
    }


    public boolean userHasAllPrivileges() {
        User user = UserContextHolder.getUserContext().getUser();
        return userGroupRepository.findByUserAndGroupHasAllPrivilegesTrueAndIsVoidedFalse(user).size() > 0;
    }
}
