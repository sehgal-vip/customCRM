package com.turno.crm.repository;

import com.turno.crm.model.entity.Deal;
import com.turno.crm.model.enums.DealStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DealRepository extends JpaRepository<Deal, Long>, JpaSpecificationExecutor<Deal> {

    Optional<Deal> findByName(String name);

    boolean existsByName(String name);

    List<Deal> findByAssignedAgentIdAndStatus(Long agentId, DealStatus status);

    long countByAssignedAgentIdAndStatus(Long agentId, DealStatus status);

    Optional<Deal> findBySourceEventId(String sourceEventId);

    Page<Deal> findByStatus(DealStatus status, Pageable pageable);
}
