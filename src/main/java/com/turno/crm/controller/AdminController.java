package com.turno.crm.controller;

import com.turno.crm.model.dto.*;
import com.turno.crm.model.enums.TaxonomyType;
import com.turno.crm.security.CurrentUserProvider;
import com.turno.crm.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('MANAGER')")
public class AdminController {

    private final AdminService adminService;
    private final CurrentUserProvider currentUserProvider;

    public AdminController(AdminService adminService, CurrentUserProvider currentUserProvider) {
        this.adminService = adminService;
        this.currentUserProvider = currentUserProvider;
    }

    // ─── Taxonomy ───────────────────────────────────────────────────────

    @GetMapping("/taxonomy/{type}")
    public ResponseEntity<List<TaxonomyItemResponse>> listTaxonomy(@PathVariable TaxonomyType type) {
        return ResponseEntity.ok(adminService.listTaxonomy(type));
    }

    @PostMapping("/taxonomy/{type}")
    public ResponseEntity<TaxonomyItemResponse> addTaxonomy(@PathVariable TaxonomyType type,
                                                             @Valid @RequestBody CreateTaxonomyRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminService.addTaxonomy(type, request));
    }

    @PutMapping("/taxonomy/{type}/{id}")
    public ResponseEntity<TaxonomyItemResponse> updateTaxonomy(@PathVariable TaxonomyType type,
                                                                @PathVariable Long id,
                                                                @Valid @RequestBody UpdateTaxonomyRequest request) {
        return ResponseEntity.ok(adminService.updateTaxonomy(type, id, request));
    }

    // ─── Stale Thresholds ───────────────────────────────────────────────

    @GetMapping("/stale-thresholds")
    public ResponseEntity<List<StaleThresholdResponse>> getStaleThresholds() {
        return ResponseEntity.ok(adminService.getStaleThresholds());
    }

    @PutMapping("/stale-thresholds")
    public ResponseEntity<List<StaleThresholdResponse>> updateStaleThresholds(
            @Valid @RequestBody UpdateStaleThresholdsRequest request) {
        return ResponseEntity.ok(adminService.updateStaleThresholds(request, currentUserProvider.getCurrentUserId()));
    }

    // ─── Exit Criteria ──────────────────────────────────────────────────

    @GetMapping("/exit-criteria")
    public ResponseEntity<List<ExitCriteriaResponse>> getExitCriteria() {
        return ResponseEntity.ok(adminService.getExitCriteria());
    }

    @PutMapping("/exit-criteria")
    public ResponseEntity<List<ExitCriteriaResponse>> updateExitCriteria(
            @Valid @RequestBody UpdateExitCriteriaRequest request) {
        return ResponseEntity.ok(adminService.updateExitCriteria(request, currentUserProvider.getCurrentUserId()));
    }

    // ─── Document Checklist ─────────────────────────────────────────────

    @GetMapping("/checklist")
    public ResponseEntity<List<ChecklistItemResponse>> listChecklist() {
        return ResponseEntity.ok(adminService.listChecklistItems());
    }

    @PostMapping("/checklist")
    public ResponseEntity<ChecklistItemResponse> addChecklistItem(
            @Valid @RequestBody CreateChecklistItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(adminService.addChecklistItem(request, currentUserProvider.getCurrentUserId()));
    }

    @PutMapping("/checklist/{id}")
    public ResponseEntity<ChecklistItemResponse> updateChecklistItem(@PathVariable Long id,
                                                                      @Valid @RequestBody UpdateChecklistItemRequest request) {
        return ResponseEntity.ok(adminService.updateChecklistItem(id, request, currentUserProvider.getCurrentUserId()));
    }

    // ─── Regions ────────────────────────────────────────────────────────

    @GetMapping("/regions")
    public ResponseEntity<List<RegionResponse>> listRegions() {
        return ResponseEntity.ok(adminService.listRegions());
    }

    @PostMapping("/regions")
    public ResponseEntity<RegionResponse> addRegion(@Valid @RequestBody CreateRegionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminService.addRegion(request));
    }

    @PutMapping("/regions/{id}")
    public ResponseEntity<RegionResponse> updateRegion(@PathVariable Long id,
                                                        @Valid @RequestBody UpdateRegionRequest request) {
        return ResponseEntity.ok(adminService.updateRegion(id, request));
    }

    // ─── Webhook API Keys ───────────────────────────────────────────────

    @GetMapping("/api-keys")
    public ResponseEntity<List<WebhookKeyResponse>> listWebhookKeys() {
        return ResponseEntity.ok(adminService.listKeys());
    }

    @PostMapping("/api-keys")
    public ResponseEntity<GenerateKeyResponse> generateWebhookKey(@Valid @RequestBody GenerateKeyRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(adminService.generateKey(request, currentUserProvider.getCurrentUserId()));
    }

    @PostMapping("/api-keys/{id}/revoke")
    public ResponseEntity<Void> revokeWebhookKey(@PathVariable Long id) {
        adminService.revokeKey(id, currentUserProvider.getCurrentUserId());
        return ResponseEntity.noContent().build();
    }

    // ─── Notification Preferences ───────────────────────────────────────

    @GetMapping("/notification-preferences")
    public ResponseEntity<List<NotificationPrefResponse>> getNotificationPreferences() {
        return ResponseEntity.ok(adminService.getPreferences());
    }

    @PutMapping("/notification-preferences")
    public ResponseEntity<List<NotificationPrefResponse>> updateNotificationPreferences(
            @Valid @RequestBody UpdateNotificationPrefsRequest request) {
        return ResponseEntity.ok(adminService.updatePreferences(request, currentUserProvider.getCurrentUserId()));
    }
}
