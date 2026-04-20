package com.turno.crm.controller;

import com.turno.crm.model.dto.StageBackwardRequest;
import com.turno.crm.model.dto.StageForwardRequest;
import com.turno.crm.model.dto.StageTransitionResponse;
import com.turno.crm.security.CurrentUserProvider;
import com.turno.crm.service.StageTransitionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/deals")
public class StageController {

    private final StageTransitionService stageTransitionService;
    private final CurrentUserProvider currentUserProvider;

    public StageController(StageTransitionService stageTransitionService,
                           CurrentUserProvider currentUserProvider) {
        this.stageTransitionService = stageTransitionService;
        this.currentUserProvider = currentUserProvider;
    }

    @PostMapping("/{id}/stage/forward")
    public ResponseEntity<StageTransitionResponse> moveForward(
            @PathVariable Long id,
            @RequestBody(required = false) StageForwardRequest request) {
        String overrideReason = request != null ? request.getOverrideReason() : null;
        StageTransitionResponse response = stageTransitionService.moveForward(
                id, overrideReason, currentUserProvider.getCurrentUserId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/stage/backward")
    public ResponseEntity<StageTransitionResponse> requestBackward(
            @PathVariable Long id,
            @Valid @RequestBody StageBackwardRequest request) {
        StageTransitionResponse response = stageTransitionService.requestBackward(
                id, request.getReason(), currentUserProvider.getCurrentUserId(),
                currentUserProvider.getCurrentUserRole());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/regression-requests/{id}/approve")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<StageTransitionResponse> approveRegression(@PathVariable Long id) {
        StageTransitionResponse response = stageTransitionService.approveRegression(
                id, currentUserProvider.getCurrentUserId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/regression-requests/{id}/reject")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Void> rejectRegression(@PathVariable Long id) {
        stageTransitionService.rejectRegression(id, currentUserProvider.getCurrentUserId());
        return ResponseEntity.noContent().build();
    }
}
