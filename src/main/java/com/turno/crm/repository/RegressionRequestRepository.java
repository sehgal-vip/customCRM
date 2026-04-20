package com.turno.crm.repository;

import com.turno.crm.model.entity.RegressionRequest;
import com.turno.crm.model.enums.ApprovalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RegressionRequestRepository extends JpaRepository<RegressionRequest, Long> {

    List<RegressionRequest> findByDealId(Long dealId);

    List<RegressionRequest> findByStatus(ApprovalStatus status);
}
