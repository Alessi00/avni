package org.openchs.web;


import org.openchs.domain.Organisation;
import org.openchs.domain.OrganisationConfig;
import org.openchs.framework.security.UserContextHolder;
import org.openchs.service.OrganisationConfigService;
import org.openchs.web.request.OrganisationConfigRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import javax.transaction.Transactional;


@RepositoryRestController
public class OrganisationConfigController implements RestControllerResourceProcessor<OrganisationConfig> {
    private final OrganisationConfigService organisationConfigService;

    @Autowired
    public OrganisationConfigController(OrganisationConfigService organisationConfigService) {
        this.organisationConfigService = organisationConfigService;
    }

    @RequestMapping(value = "/organisationConfig", method = RequestMethod.POST)
    @Transactional
    @PreAuthorize(value = "hasAnyAuthority('admin','organisation_admin')")
    public ResponseEntity save(@RequestBody OrganisationConfigRequest request) {
        Organisation organisation = UserContextHolder.getUserContext().getOrganisation();
        OrganisationConfig organisationConfig = organisationConfigService.saveOrganisationConfig(request, organisation);
        return new ResponseEntity<>(organisationConfig, HttpStatus.CREATED);
    }

    @RequestMapping(value = "/organisationConfig", method = RequestMethod.PUT)
    @Transactional
    @PreAuthorize(value = "hasAnyAuthority('admin','organisation_admin')")
    public ResponseEntity update(@RequestBody OrganisationConfigRequest request) {
        Organisation organisation = UserContextHolder.getUserContext().getOrganisation();
        OrganisationConfig organisationConfig = organisationConfigService.getOrganisationConfig(organisation);
        if (organisationConfig == null ) {
            return save(request);
        } else {
            try {
                organisationConfigService.updateOrganisationConfig(request, organisationConfig);
                return new ResponseEntity<>(organisationConfig, HttpStatus.OK);
            } catch(Exception e) {
                e.printStackTrace();
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        }
    }

}
