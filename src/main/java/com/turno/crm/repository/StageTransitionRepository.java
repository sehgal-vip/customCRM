package com.turno.crm.repository;

import com.turno.crm.model.entity.StageTransition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StageTransitionRepository extends JpaRepository<StageTransition, Long> {

    List<StageTransition> findByDealIdOrderByCreatedAtDesc(Long dealId);
}
