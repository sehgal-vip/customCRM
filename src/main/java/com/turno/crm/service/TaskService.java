package com.turno.crm.service;

import com.turno.crm.exception.ResourceNotFoundException;
import com.turno.crm.exception.UnauthorizedAccessException;
import com.turno.crm.model.dto.*;
import com.turno.crm.model.entity.*;
import com.turno.crm.model.enums.*;
import com.turno.crm.repository.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final DealRepository dealRepository;
    private final AuditService auditService;

    public TaskService(TaskRepository taskRepository,
                       UserRepository userRepository,
                       DealRepository dealRepository,
                       AuditService auditService) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.dealRepository = dealRepository;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> list(Long userId, UserRole role,
                                   List<TaskStatus> statuses,
                                   List<Long> agentIds,
                                   List<TaskPriority> priorities,
                                   Long dealId,
                                   LocalDate dueFrom,
                                   LocalDate dueTo) {
        Specification<Task> spec = Specification.where(null);

        // Agent always sees only own tasks
        if (role == UserRole.AGENT) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("assignedTo").get("id"), userId));
        } else if (agentIds != null && !agentIds.isEmpty()) {
            spec = spec.and((root, query, cb) -> root.get("assignedTo").get("id").in(agentIds));
        }

        if (statuses != null && !statuses.isEmpty()) {
            spec = spec.and((root, query, cb) -> root.get("status").in(statuses));
        }

        if (priorities != null && !priorities.isEmpty()) {
            spec = spec.and((root, query, cb) -> root.get("priority").in(priorities));
        }

        if (dealId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("deal").get("id"), dealId));
        }

        if (dueFrom != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("dueDate"), dueFrom));
        }

        if (dueTo != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("dueDate"), dueTo));
        }

        // Default sort by due date ascending
        List<Task> tasks = taskRepository.findAll(spec,
                org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.ASC, "dueDate"));

        return tasks.stream().map(this::toResponse).toList();
    }

    public TaskResponse create(CreateTaskRequest request, Long actorId) {
        User assignedTo = userRepository.findById(request.getAssignedToId())
                .orElseThrow(() -> new ResourceNotFoundException("User", request.getAssignedToId()));

        User createdBy = userRepository.findById(actorId)
                .orElseThrow(() -> new ResourceNotFoundException("User", actorId));

        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setPriority(request.getPriority() != null ? request.getPriority() : TaskPriority.MEDIUM);
        task.setAssignedTo(assignedTo);
        task.setCreatedBy(createdBy);
        task.setDueDate(request.getDueDate());
        task.setStatus(TaskStatus.OPEN);

        if (request.getDealId() != null) {
            Deal deal = dealRepository.findById(request.getDealId())
                    .orElseThrow(() -> new ResourceNotFoundException("Deal", request.getDealId()));
            task.setDeal(deal);
        }

        task = taskRepository.save(task);

        auditService.log(AuditEntityType.TASK, task.getId(), AuditAction.CREATE, actorId,
                Map.of("title", task.getTitle(), "assignedToId", assignedTo.getId()));

        return toResponse(task);
    }

    public Task autoCreateFromReport(ActivityReport report, Deal deal) {
        Task task = new Task();
        task.setTitle(report.getNextAction());
        task.setDueDate(report.getNextActionEta() != null ? report.getNextActionEta() : LocalDate.now());
        task.setAssignedTo(deal.getAssignedAgent());
        task.setCreatedBy(report.getAgent());
        task.setDeal(deal);
        task.setActivityReport(report);
        task.setStatus(TaskStatus.OPEN);
        task.setPriority(TaskPriority.MEDIUM);

        return taskRepository.save(task);
    }

    @Transactional(readOnly = true)
    public TaskResponse getById(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", taskId));
        return toResponse(task);
    }

    public TaskResponse updateStatus(Long taskId, TaskStatus newStatus, Long actorId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", taskId));

        TaskStatus oldStatus = task.getStatus();
        task.setStatus(newStatus);

        if (newStatus == TaskStatus.DONE) {
            task.setCompletedAt(OffsetDateTime.now());
        } else {
            task.setCompletedAt(null);
        }

        task = taskRepository.save(task);

        auditService.log(AuditEntityType.TASK, task.getId(), AuditAction.STATUS_CHANGE, actorId,
                Map.of("from", oldStatus.name(), "to", newStatus.name()));

        return toResponse(task);
    }

    public TaskResponse update(Long taskId, UpdateTaskRequest request, Long actorId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", taskId));

        Map<String, Object> changes = new HashMap<>();

        if (request.getTitle() != null) {
            changes.put("title", Map.of("from", task.getTitle(), "to", request.getTitle()));
            task.setTitle(request.getTitle());
        }

        if (request.getDescription() != null) {
            changes.put("description", "updated");
            task.setDescription(request.getDescription());
        }

        if (request.getPriority() != null) {
            changes.put("priority", Map.of("from", task.getPriority().name(), "to", request.getPriority().name()));
            task.setPriority(request.getPriority());
        }

        if (request.getAssignedToId() != null) {
            User newAssignee = userRepository.findById(request.getAssignedToId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", request.getAssignedToId()));
            changes.put("assignedToId", Map.of("from", task.getAssignedTo().getId(), "to", newAssignee.getId()));
            task.setAssignedTo(newAssignee);
        }

        if (request.getDealId() != null) {
            Deal deal = dealRepository.findById(request.getDealId())
                    .orElseThrow(() -> new ResourceNotFoundException("Deal", request.getDealId()));
            changes.put("dealId", request.getDealId());
            task.setDeal(deal);
        }

        if (request.getDueDate() != null) {
            changes.put("dueDate", Map.of("from", task.getDueDate().toString(), "to", request.getDueDate().toString()));
            task.setDueDate(request.getDueDate());
        }

        task = taskRepository.save(task);

        auditService.log(AuditEntityType.TASK, task.getId(), AuditAction.UPDATE, actorId, changes);

        return toResponse(task);
    }

    public void delete(Long taskId, Long actorId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", taskId));

        auditService.log(AuditEntityType.TASK, task.getId(), AuditAction.DELETE, actorId,
                Map.of("title", task.getTitle()));

        taskRepository.delete(task);
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getByDealId(Long dealId) {
        List<Task> tasks = taskRepository.findByDealIdOrderByDueDateAsc(dealId);
        return tasks.stream().map(this::toResponse).toList();
    }

    private TaskResponse toResponse(Task task) {
        TaskResponse resp = new TaskResponse();
        resp.setId(task.getId());
        resp.setTitle(task.getTitle());
        resp.setDescription(task.getDescription());
        resp.setStatus(task.getStatus().name());
        resp.setPriority(task.getPriority().name());
        resp.setAssignedToId(task.getAssignedTo().getId());
        resp.setAssignedToName(task.getAssignedTo().getName());
        resp.setCreatedById(task.getCreatedBy().getId());
        resp.setCreatedByName(task.getCreatedBy().getName());

        if (task.getDeal() != null) {
            resp.setDealId(task.getDeal().getId());
            resp.setDealName(task.getDeal().getName());
        }

        if (task.getActivityReport() != null) {
            resp.setActivityReportId(task.getActivityReport().getId());
        }

        resp.setDueDate(task.getDueDate().toString());
        resp.setCompletedAt(task.getCompletedAt() != null ? task.getCompletedAt().toString() : null);
        resp.setCreatedAt(task.getCreatedAt() != null ? task.getCreatedAt().toString() : null);
        resp.setOverdue(task.getStatus() != TaskStatus.DONE && task.getDueDate().isBefore(LocalDate.now()));

        return resp;
    }
}
