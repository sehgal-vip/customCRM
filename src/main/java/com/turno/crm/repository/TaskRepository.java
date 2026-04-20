package com.turno.crm.repository;

import com.turno.crm.model.entity.Task;
import com.turno.crm.model.enums.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {

    List<Task> findByAssignedToIdAndStatusInOrderByDueDateAsc(Long agentId, List<TaskStatus> statuses);

    List<Task> findByStatusInOrderByDueDateAsc(List<TaskStatus> statuses);

    List<Task> findByDealIdOrderByDueDateAsc(Long dealId);

    long countByAssignedToIdAndStatusAndDueDateBefore(Long agentId, TaskStatus status, LocalDate date);
}
