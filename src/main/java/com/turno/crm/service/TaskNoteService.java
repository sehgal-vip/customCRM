package com.turno.crm.service;

import com.turno.crm.exception.ResourceNotFoundException;
import com.turno.crm.exception.UnauthorizedAccessException;
import com.turno.crm.model.dto.CreateTaskNoteRequest;
import com.turno.crm.model.dto.TaskNoteResponse;
import com.turno.crm.model.entity.Task;
import com.turno.crm.model.entity.TaskNote;
import com.turno.crm.model.entity.User;
import com.turno.crm.model.enums.*;
import com.turno.crm.repository.TaskNoteRepository;
import com.turno.crm.repository.TaskRepository;
import com.turno.crm.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional
public class TaskNoteService {

    private final TaskNoteRepository taskNoteRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    public TaskNoteService(TaskNoteRepository taskNoteRepository,
                           TaskRepository taskRepository,
                           UserRepository userRepository,
                           AuditService auditService) {
        this.taskNoteRepository = taskNoteRepository;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public List<TaskNoteResponse> list(Long taskId) {
        if (!taskRepository.existsById(taskId)) {
            throw new ResourceNotFoundException("Task", taskId);
        }
        return taskNoteRepository.findByTaskIdOrderByCreatedAtAsc(taskId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public TaskNoteResponse create(Long taskId, CreateTaskNoteRequest request, Long actorId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", taskId));

        User actor = userRepository.findById(actorId)
                .orElseThrow(() -> new ResourceNotFoundException("User", actorId));

        // Enforce: agents can only post AGENT notes, managers can only post MANAGER notes
        NoteType expectedType = actor.getRole() == UserRole.MANAGER ? NoteType.MANAGER : NoteType.AGENT;
        if (request.getNoteType() != expectedType) {
            throw new UnauthorizedAccessException("You can only add " + expectedType.name() + " notes");
        }

        TaskNote note = new TaskNote();
        note.setTask(task);
        note.setContent(request.getContent());
        note.setNoteType(request.getNoteType());
        note.setCreatedBy(actor);

        note = taskNoteRepository.save(note);

        auditService.log(AuditEntityType.TASK_NOTE, note.getId(), AuditAction.CREATE, actorId,
                Map.of("taskId", taskId, "noteType", request.getNoteType().name()));

        return toResponse(note);
    }

    private TaskNoteResponse toResponse(TaskNote note) {
        TaskNoteResponse resp = new TaskNoteResponse();
        resp.setId(note.getId());
        resp.setTaskId(note.getTask().getId());
        resp.setContent(note.getContent());
        resp.setNoteType(note.getNoteType().name());
        resp.setCreatedById(note.getCreatedBy().getId());
        resp.setCreatedByName(note.getCreatedBy().getName());
        resp.setCreatedAt(note.getCreatedAt() != null ? note.getCreatedAt().toString() : null);
        return resp;
    }
}
