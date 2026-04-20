package com.turno.crm.controller;

import com.turno.crm.model.dto.*;
import com.turno.crm.security.CurrentUserProvider;
import com.turno.crm.service.PricingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/deals/{dealId}/pricing")
public class PricingController {

    private final PricingService pricingService;
    private final CurrentUserProvider currentUserProvider;

    public PricingController(PricingService pricingService, CurrentUserProvider currentUserProvider) {
        this.pricingService = pricingService;
        this.currentUserProvider = currentUserProvider;
    }

    @PostMapping("/submit")
    public ResponseEntity<PricingSubmissionResponse> submit(
            @PathVariable Long dealId,
            @Valid @RequestBody PricingSubmitRequest request) {
        PricingSubmissionResponse response = pricingService.submit(
                dealId, request, currentUserProvider.getCurrentUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{submissionId}/approve")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<PricingSubmissionResponse> approve(
            @PathVariable Long dealId,
            @PathVariable Long submissionId,
            @RequestBody(required = false) PricingApproveRequest request) {
        PricingSubmissionResponse response = pricingService.approve(
                dealId, submissionId, request, currentUserProvider.getCurrentUserId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{submissionId}/reject")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<PricingSubmissionResponse> reject(
            @PathVariable Long dealId,
            @PathVariable Long submissionId,
            @Valid @RequestBody PricingRejectRequest request) {
        PricingSubmissionResponse response = pricingService.reject(
                dealId, submissionId, request, currentUserProvider.getCurrentUserId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/history")
    public ResponseEntity<List<PricingSubmissionResponse>> getHistory(@PathVariable Long dealId) {
        return ResponseEntity.ok(pricingService.getHistory(dealId));
    }
}
