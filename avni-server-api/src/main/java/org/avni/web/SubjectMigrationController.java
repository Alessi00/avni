package org.avni.web;

import org.avni.dao.OperatingIndividualScopeAwareRepository;
import org.avni.dao.SubjectMigrationRepository;
import org.avni.dao.SubjectTypeRepository;
import org.avni.domain.SubjectMigration;
import org.avni.domain.SubjectType;
import org.avni.service.ScopeBasedSyncService;
import org.avni.service.UserService;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;

@RestController
public class SubjectMigrationController extends AbstractController<SubjectMigration> implements RestControllerResourceProcessor<SubjectMigration>{
    private SubjectMigrationRepository subjectMigrationRepository;
    private SubjectTypeRepository subjectTypeRepository;
    private UserService userService;
    private final Logger logger;
    private ScopeBasedSyncService<SubjectMigration> scopeBasedSyncService;

    @Autowired
    public SubjectMigrationController(SubjectMigrationRepository subjectMigrationRepository, SubjectTypeRepository subjectTypeRepository, UserService userService, ScopeBasedSyncService<SubjectMigration> scopeBasedSyncService) {
        this.scopeBasedSyncService = scopeBasedSyncService;
        logger = LoggerFactory.getLogger(this.getClass());
        this.subjectMigrationRepository = subjectMigrationRepository;
        this.subjectTypeRepository = subjectTypeRepository;
        this.userService = userService;
    }

    @RequestMapping(value = "/subjectMigrations", method = RequestMethod.GET)
    @PreAuthorize(value = "hasAnyAuthority('user')")
    public PagedResources<Resource<SubjectMigration>> getEncountersByCatchmentAndLastModified(
            @RequestParam("lastModifiedDateTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) DateTime lastModifiedDateTime,
            @RequestParam("now") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) DateTime now,
            @RequestParam(value = "subjectTypeUuid", required = false) String subjectTypeUuid,
            Pageable pageable) {
        if (subjectTypeUuid.isEmpty()) return wrap(new PageImpl<>(Collections.emptyList()));
        SubjectType subjectType = subjectTypeRepository.findByUuid(subjectTypeUuid);
        if (subjectType == null) return wrap(new PageImpl<>(Collections.emptyList()));

        return wrap(scopeBasedSyncService.getSyncResult(subjectMigrationRepository, userService.getCurrentUser(), lastModifiedDateTime, now, subjectType.getId(), pageable));
    }

    @Override
    public Resource<SubjectMigration> process(Resource<SubjectMigration> resource) {
        SubjectMigration content = resource.getContent();
        resource.removeLinks();
        resource.add(new Link(content.getIndividual().getUuid(), "individualUUID"));
        resource.add(new Link(content.getOldAddressLevel().getUuid(), "oldAddressLevelUUID"));
        resource.add(new Link(content.getNewAddressLevel().getUuid(), "newAddressLevelUUID"));
        return resource;
    }
}
