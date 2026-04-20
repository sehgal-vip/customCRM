package com.turno.crm.repository;

import com.turno.crm.model.entity.BackfillRequest;
import com.turno.crm.model.enums.ApprovalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BackfillRequestRepository extends JpaRepository<BackfillRequest, Long> {

    List<BackfillRequest> findByDealId(Long dealId);

    List<BackfillRequest> findByStatus(ApprovalStatus status);
}
