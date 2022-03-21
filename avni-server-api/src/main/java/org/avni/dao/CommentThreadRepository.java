package org.avni.dao;

import java.util.Date;
import org.avni.domain.AddressLevel;
import org.avni.domain.CHSEntity;
import org.avni.domain.CommentThread;
import org.avni.domain.Individual;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RepositoryRestResource(collectionResourceRel = "commentThread", path = "commentThread", exported = false)
@PreAuthorize("hasAnyAuthority('user','admin')")
public interface CommentThreadRepository extends TransactionalDataRepository<CommentThread>, FindByLastModifiedDateTime<CommentThread>, OperatingIndividualScopeAwareRepository<CommentThread> {

    Page<CommentThread> findByComments_SubjectAddressLevelIdInAndComments_SubjectSubjectTypeIdAndLastModifiedDateTimeIsBetweenOrderByLastModifiedDateTimeAscIdAsc(
            List<Long> addressLevels,
            Long subjectTypeId,
            Date lastModifiedDateTime,
            Date now,
            Pageable pageable);

    boolean existsByComments_SubjectSubjectTypeIdAndLastModifiedDateTimeGreaterThanAndComments_SubjectAddressLevelIdIn(
            Long subjectTypeId,
            Date lastModifiedDateTime,
            List<Long> addressIds);


    @Override
    default Page<CommentThread> syncByCatchment(SyncParameters syncParameters) {
        return findByComments_SubjectAddressLevelIdInAndComments_SubjectSubjectTypeIdAndLastModifiedDateTimeIsBetweenOrderByLastModifiedDateTimeAscIdAsc(syncParameters.getAddressLevels(), syncParameters.getFilter(), CHSEntity.toDate(syncParameters.getLastModifiedDateTime()), CHSEntity.toDate(syncParameters.getNow()), syncParameters.getPageable());
    }

    @Override
    default boolean isEntityChangedForCatchment(List<Long> addressIds, Date lastModifiedDateTime, Long typeId){
        return existsByComments_SubjectSubjectTypeIdAndLastModifiedDateTimeGreaterThanAndComments_SubjectAddressLevelIdIn(typeId, lastModifiedDateTime, addressIds);
    }

    List<CommentThread> findDistinctByIsVoidedFalseAndCommentsIsVoidedFalseAndComments_SubjectOrderByOpenDateTimeDescIdDesc(Individual subject);
}
