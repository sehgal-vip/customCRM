package com.turno.crm.controller;

import com.turno.crm.model.dto.*;
import com.turno.crm.model.enums.DealStage;
import com.turno.crm.model.enums.DealStatus;
import com.turno.crm.security.CurrentUserProvider;
import com.turno.crm.service.AuditService;
import com.turno.crm.service.BackfillService;
import com.turno.crm.service.DealService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/deals")
public class DealController {

    private final DealService dealService;
    private final AuditService auditService;
    private final BackfillService backfillService;
    private final CurrentUserProvider currentUserProvider;

    public DealController(DealService dealService, AuditService auditService,
                          BackfillService backfillService,
                          CurrentUserProvider currentUserProvider) {
        this.dealService = dealService;
        this.auditService = auditService;
        this.backfillService = backfillService;
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping
    public ResponseEntity<Page<DealListResponse>> list(
            @RequestParam(required = false) DealStatus status,
            @RequestParam(required = false) List<DealStatus> statuses,
            @RequestParam(required = false) DealStage stage,
            @RequestParam(required = false) List<Long> agentIds,
            @RequestParam(required = false) List<Long> operatorIds,
            @RequestParam(required = false) List<Long> regionIds,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime dateTo,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<DealListResponse> deals = dealService.list(status, statuses, stage, agentIds, operatorIds, regionIds,
                dateFrom, dateTo, currentUserProvider.getCurrentUserId(),
                currentUserProvider.getCurrentUserRole(), pageable);
        return ResponseEntity.ok(deals);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DealResponse> getById(@PathVariable Long id) {
        DealResponse deal = dealService.getById(id, currentUserProvider.getCurrentUserId(),
                currentUserProvider.getCurrentUserRole());
        return ResponseEntity.ok(deal);
    }

    @PostMapping
    public ResponseEntity<DealResponse> create(@Valid @RequestBody CreateDealRequest request) {
        DealResponse response = dealService.create(request, currentUserProvider.getCurrentUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DealResponse> update(@PathVariable Long id,
                                               @Valid @RequestBody UpdateDealRequest request) {
        DealResponse response = dealService.update(id, request, currentUserProvider.getCurrentUserId(),
                currentUserProvider.getCurrentUserRole());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/archive")
    public ResponseEntity<DealResponse> archive(@PathVariable Long id,
                                                @Valid @RequestBody ArchiveDealRequest request) {
        return ResponseEntity.ok(dealService.archive(id, request, currentUserProvider.getCurrentUserId()));
    }

    @PostMapping("/{id}/reactivate")
    public ResponseEntity<DealResponse> reactivate(@PathVariable Long id) {
        return ResponseEntity.ok(dealService.reactivate(id, currentUserProvider.getCurrentUserId()));
    }

    @PostMapping("/{id}/reopen")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<DealResponse> reopen(@PathVariable Long id) {
        return ResponseEntity.ok(dealService.reopen(id, currentUserProvider.getCurrentUserId()));
    }

    @GetMapping("/{id}/audit")
    public ResponseEntity<Page<Map<String, Object>>> getAuditTrail(@PathVariable Long id,
                                                                    @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(auditService.getDealAuditTrail(id, pageable));
    }

    // ─── Backfill Endpoints ─────────────────────────────────────────────

    @PostMapping("/{id}/backfill")
    public ResponseEntity<BackfillRequestResponse> requestBackfill(@PathVariable Long id,
                                                                    @Valid @RequestBody BackfillRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(backfillService.requestBackfill(id, request, currentUserProvider.getCurrentUserId()));
    }

    @PostMapping("/backfill-requests/{requestId}/approve")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<BackfillRequestResponse> approveBackfill(@PathVariable Long requestId) {
        return ResponseEntity.ok(backfillService.approveBackfill(requestId, currentUserProvider.getCurrentUserId()));
    }

    @PostMapping("/backfill-requests/{requestId}/reject")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<BackfillRequestResponse> rejectBackfill(@PathVariable Long requestId) {
        return ResponseEntity.ok(backfillService.rejectBackfill(requestId, currentUserProvider.getCurrentUserId()));
    }
}
