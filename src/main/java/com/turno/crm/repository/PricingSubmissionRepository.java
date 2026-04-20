package com.turno.crm.repository;

import com.turno.crm.model.entity.PricingSubmission;
import com.turno.crm.model.enums.PricingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PricingSubmissionRepository extends JpaRepository<PricingSubmission, Long> {

    List<PricingSubmission> findByDealIdOrderByCreatedAtDesc(Long dealId);

    Optional<PricingSubmission> findFirstByDealIdAndStatusOrderByCreatedAtDesc(Long dealId, PricingStatus status);

    boolean existsByDealIdAndStatus(Long dealId, PricingStatus status);
}
