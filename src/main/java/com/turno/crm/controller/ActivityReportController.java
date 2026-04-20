package com.turno.crm.controller;

import com.turno.crm.model.dto.*;
import com.turno.crm.security.CurrentUserProvider;
import com.turno.crm.service.ActivityReportNoteService;
import com.turno.crm.service.ActivityReportService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/deals/{dealId}/reports")
public class ActivityReportController {

    private final ActivityReportService activityReportService;
    private final ActivityReportNoteService noteService;
    private final CurrentUserProvider currentUserProvider;

    public ActivityReportController(ActivityReportService activityReportService,
                                     ActivityReportNoteService noteService,
                                     CurrentUserProvider currentUserProvider) {
        this.activityReportService = activityReportService;
        this.noteService = noteService;
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping
    public ResponseEntity<Page<ActivityReportResponse>> listReports(
            @PathVariable Long dealId,
            @RequestParam(defaultValue = "false") boolean includeVoided,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(activityReportService.listReports(dealId, includeVoided, pageable));
    }

    @GetMapping("/{reportId}")
    public ResponseEntity<ActivityReportResponse> getReport(
            @PathVariable Long dealId,
            @PathVariable Long reportId) {
        return ResponseEntity.ok(activityReportService.getReport(dealId, reportId));
    }

    @PostMapping
    public ResponseEntity<ActivityReportResponse> submitReport(
            @PathVariable Long dealId,
            @Valid @RequestBody CreateActivityReportRequest request) {
        ActivityReportResponse response = activityReportService.submitReport(
                dealId, request, currentUserProvider.getCurrentUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{reportId}/void")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ActivityReportResponse> voidReport(
            @PathVariable Long dealId,
            @PathVariable Long reportId,
            @Valid @RequestBody VoidReportRequest request) {
        return ResponseEntity.ok(activityReportService.voidReport(
                dealId, reportId, request.getReason(), currentUserProvider.getCurrentUserId()));
    }

    @PostMapping("/{reportId}/unvoid")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ActivityReportResponse> unvoidReport(
            @PathVariable Long dealId,
            @PathVariable Long reportId) {
        return ResponseEntity.ok(activityReportService.unvoidReport(
                dealId, reportId, currentUserProvider.getCurrentUserId()));
    }

    @PutMapping("/{reportId}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ActivityReportResponse> updateReport(
            @PathVariable Long dealId,
            @PathVariable Long reportId,
            @Valid @RequestBody UpdateActivityReportRequest request) {
        return ResponseEntity.ok(activityReportService.updateReport(
                dealId, reportId, request, currentUserProvider.getCurrentUserId()));
    }

    @GetMapping("/{reportId}/notes")
    public ResponseEntity<List<ActivityReportNoteResponse>> getNotes(
            @PathVariable Long dealId,
            @PathVariable Long reportId) {
        return ResponseEntity.ok(noteService.list(reportId));
    }

    @PostMapping("/{reportId}/notes")
    public ResponseEntity<ActivityReportNoteResponse> addNote(
            @PathVariable Long dealId,
            @PathVariable Long reportId,
            @Valid @RequestBody CreateActivityReportNoteRequest request) {
        ActivityReportNoteResponse response = noteService.create(
                reportId, request, currentUserProvider.getCurrentUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
