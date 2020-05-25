package org.openchs.dao;

import org.openchs.domain.Group;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RepositoryRestResource(collectionResourceRel = "groups", path = "groups")
@PreAuthorize("hasAnyAuthority('user','admin','organisation_admin')")
public interface GroupRepository extends ReferenceDataRepository<Group>, FindByLastModifiedDateTime<Group> {

    Group findByNameAndOrganisationId(String name, Long groupId);

    Group findByIdAndOrganisationId(Long groupId, Long organisationId);

    Long deleteAllByNameNot(String name);

    List<Group> findAllByName(String name);
}
