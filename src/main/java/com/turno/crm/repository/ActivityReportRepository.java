package com.turno.crm.repository;

import com.turno.crm.model.entity.ActivityReport;
import com.turno.crm.model.enums.ActivityType;
import com.turno.crm.model.enums.DealStage;
import com.turno.crm.model.enums.TemplateType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ActivityReportRepository extends JpaRepository<ActivityReport, Long> {

    List<ActivityReport> findByDealIdOrderBySubmissionDatetimeDesc(Long dealId);

    Page<ActivityReport> findByDealIdOrderBySubmissionDatetimeDesc(Long dealId, Pageable pageable);

    long countByDealIdAndVoidedFalse(Long dealId);

    long countByDealIdAndActivityTypeAndVoidedFalse(Long dealId, ActivityType activityType);

    long countByDealIdAndLoggedAtStageAndVoidedFalse(Long dealId, DealStage loggedAtStage);

    long countByDealIdAndLoggedAtStageAndActivityTypeAndVoidedFalse(Long dealId, DealStage loggedAtStage, ActivityType activityType);

    @Query("SELECT COUNT(ar) > 0 FROM ActivityReport ar WHERE ar.deal.id = :dealId AND ar.voided = false AND ar.templateType IN :types")
    boolean existsNonVoidedReportWithTemplateTypes(@Param("dealId") Long dealId, @Param("types") List<TemplateType> types);

    @Query("SELECT ar FROM ActivityReport ar WHERE ar.deal.id = :dealId AND ar.voided = false ORDER BY ar.submissionDatetime DESC")
    List<ActivityReport> findNonVoidedByDealId(@Param("dealId") Long dealId);

    Optional<ActivityReport> findFirstByDealIdAndVoidedFalseOrderBySubmissionDatetimeDesc(Long dealId);
}
