package com.turno.crm.repository;

import com.turno.crm.model.entity.DealDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DealDocumentRepository extends JpaRepository<DealDocument, Long> {

    List<DealDocument> findByDealId(Long dealId);

    Optional<DealDocument> findByDealIdAndChecklistItemId(Long dealId, Long checklistItemId);

    @Query("SELECT COUNT(dd) FROM DealDocument dd JOIN dd.checklistItem ci WHERE dd.deal.id = :dealId AND ci.requirement = 'MANDATORY' AND dd.status IN ('RECEIVED', 'VERIFIED')")
    long countMandatoryComplete(@Param("dealId") Long dealId);

    @Query("SELECT COUNT(dd) FROM DealDocument dd JOIN dd.checklistItem ci WHERE dd.deal.id = :dealId AND ci.requirement = 'MANDATORY'")
    long countMandatoryTotal(@Param("dealId") Long dealId);
}
