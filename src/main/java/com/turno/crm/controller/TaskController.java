package com.turno.crm.controller;

import com.turno.crm.model.dto.*;
import com.turno.crm.model.enums.TaskPriority;
import com.turno.crm.model.enums.TaskStatus;
import com.turno.crm.security.CurrentUserProvider;
import com.turno.crm.service.TaskNoteService;
import com.turno.crm.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/tasks")
public class TaskController {

    private final TaskService taskService;
    private final TaskNoteService taskNoteService;
    private final CurrentUserProvider currentUserProvider;

    public TaskController(TaskService taskService, TaskNoteService taskNoteService,
                          CurrentUserProvider currentUserProvider) {
        this.taskService = taskService;
        this.taskNoteService = taskNoteService;
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping
    public ResponseEntity<List<TaskResponse>> list(
            @RequestParam(required = false) List<TaskStatus> statuses,
            @RequestParam(required = false) List<Long> agentIds,
            @RequestParam(required = false) List<TaskPriority> priorities,
            @RequestParam(required = false) Long dealId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueTo) {
        List<TaskResponse> tasks = taskService.list(
                currentUserProvider.getCurrentUserId(),
                currentUserProvider.getCurrentUserRole(),
                statuses, agentIds, priorities, dealId, dueFrom, dueTo);
        return ResponseEntity.ok(tasks);
    }

    @PostMapping
    public ResponseEntity<TaskResponse> create(@Valid @RequestBody CreateTaskRequest request) {
        TaskResponse response = taskService.create(request, currentUserProvider.getCurrentUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(taskService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskResponse> update(@PathVariable Long id,
                                                @Valid @RequestBody UpdateTaskRequest request) {
        TaskResponse response = taskService.update(id, request, currentUserProvider.getCurrentUserId());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<TaskResponse> updateStatus(@PathVariable Long id,
                                                      @Valid @RequestBody UpdateTaskStatusRequest request) {
        TaskResponse response = taskService.updateStatus(id, request.getStatus(),
                currentUserProvider.getCurrentUserId());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        taskService.delete(id, currentUserProvider.getCurrentUserId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/deal/{dealId}")
    public ResponseEntity<List<TaskResponse>> getByDealId(@PathVariable Long dealId) {
        return ResponseEntity.ok(taskService.getByDealId(dealId));
    }

    @GetMapping("/{id}/notes")
    public ResponseEntity<List<TaskNoteResponse>> getNotes(@PathVariable Long id) {
        return ResponseEntity.ok(taskNoteService.list(id));
    }

    @PostMapping("/{id}/notes")
    public ResponseEntity<TaskNoteResponse> addNote(@PathVariable Long id,
                                                     @Valid @RequestBody CreateTaskNoteRequest request) {
        TaskNoteResponse response = taskNoteService.create(id, request,
                currentUserProvider.getCurrentUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
