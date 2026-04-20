package com.turno.crm.service;

import com.turno.crm.exception.BusinessRuleViolationException;
import com.turno.crm.exception.ResourceNotFoundException;
import com.turno.crm.model.dto.*;
import com.turno.crm.model.entity.*;
import com.turno.crm.model.enums.*;
import com.turno.crm.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class PricingService {

    private final PricingSubmissionRepository pricingSubmissionRepository;
    private final DealRepository dealRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;
    private final NotificationService notificationService;

    public PricingService(PricingSubmissionRepository pricingSubmissionRepository,
                          DealRepository dealRepository,
                          UserRepository userRepository,
                          AuditService auditService,
                          NotificationService notificationService) {
        this.pricingSubmissionRepository = pricingSubmissionRepository;
        this.dealRepository = dealRepository;
        this.userRepository = userRepository;
        this.auditService = auditService;
        this.notificationService = notificationService;
    }

    public PricingSubmissionResponse submit(Long dealId, PricingSubmitRequest request, Long actorId) {
        Deal deal = dealRepository.findById(dealId)
                .orElseThrow(() -> new ResourceNotFoundException("Deal", dealId));

        // Validate deal at STAGE_4
        if (deal.getCurrentStage() != DealStage.STAGE_4) {
            throw new BusinessRuleViolationException("Pricing can only be submitted for deals at Stage 4");
        }

        // Validate BUS_LEASE in servicesSelected
        if (request.getServicesSelected() == null || !request.getServicesSelected().contains("BUS_LEASE")) {
            throw new BusinessRuleViolationException("BUS_LEASE must be included in services selected");
        }

        // Mark existing SUBMITTED entries as SUPERSEDED
        List<PricingSubmission> existing = pricingSubmissionRepository.findByDealIdOrderByCreatedAtDesc(dealId);
        for (PricingSubmission ps : existing) {
            if (ps.getStatus() == PricingStatus.SUBMITTED) {
                ps.setStatus(PricingStatus.SUPERSEDED);
                pricingSubmissionRepository.save(ps);
            }
        }

        // Create new submission
        User submitter = new User();
        submitter.setId(actorId);

        PricingSubmission submission = new PricingSubmission();
        submission.setDeal(deal);
        submission.setSubmittedBy(submitter);
        submission.setServicesSelected(request.getServicesSelected());
        submission.setMonthlyKmCommitment(request.getMonthlyKmCommitment());
        submission.setPricePerKm(request.getPricePerKm());
        submission.setTokenAmount(request.getTokenAmount());
        submission.setStatus(PricingStatus.SUBMITTED);

        submission = pricingSubmissionRepository.save(submission);

        // Update deal subStatus
        deal.setSubStatus(Stage5SubStatus.AWAITING_APPROVAL);
        dealRepository.save(deal);

        // Fire notification to all managers
        User agent = userRepository.findById(actorId)
                .orElseThrow(() -> new ResourceNotFoundException("User", actorId));
        String pricingDetails = "Price/km: " + request.getPricePerKm()
                + ", Monthly km: " + request.getMonthlyKmCommitment();
        notificationService.firePricingApprovalRequested(deal, agent, pricingDetails);

        // Audit
        auditService.log(AuditEntityType.PRICING_SUBMISSION, submission.getId(), AuditAction.SUBMIT, actorId,
                Map.of("dealId", dealId, "pricePerKm", request.getPricePerKm().toString(),
                        "monthlyKm", request.getMonthlyKmCommitment().toString()));

        return toResponse(submission);
    }

    public PricingSubmissionResponse approve(Long dealId, Long submissionId, PricingApproveRequest request, Long actorId) {
        PricingSubmission submission = pricingSubmissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("PricingSubmission", submissionId));

        if (!submission.getDeal().getId().equals(dealId)) {
            throw new ResourceNotFoundException("PricingSubmission", submissionId);
        }

        if (submission.getStatus() != PricingStatus.SUBMITTED) {
            throw new BusinessRuleViolationException("Only submitted pricing can be approved");
        }

        // Store manager edits if provided
        if (request != null) {
            if (request.getServicesSelected() != null) {
                submission.setManagerServicesSelected(request.getServicesSelected());
            }
            if (request.getMonthlyKmCommitment() != null) {
                submission.setManagerMonthlyKm(request.getMonthlyKmCommitment());
            }
            if (request.getPricePerKm() != null) {
                submission.setManagerPricePerKm(request.getPricePerKm());
            }
            if (request.getTokenAmount() != null) {
                submission.setManagerTokenAmount(request.getTokenAmount());
            }
        }

        User reviewer = new User();
        reviewer.setId(actorId);
        submission.setStatus(PricingStatus.APPROVED);
        submission.setReviewedBy(reviewer);
        submission.setReviewedAt(OffsetDateTime.now());

        submission = pricingSubmissionRepository.save(submission);

        // Update deal
        Deal deal = submission.getDeal();
        deal.setSubStatus(Stage5SubStatus.NEGOTIATING);

        // Calculate final value
        BigDecimal finalPricePerKm = submission.getManagerPricePerKm() != null
                ? submission.getManagerPricePerKm() : submission.getPricePerKm();
        BigDecimal finalMonthlyKm = submission.getManagerMonthlyKm() != null
                ? submission.getManagerMonthlyKm() : submission.getMonthlyKmCommitment();
        int fleetSize = deal.getFleetSize() != null ? deal.getFleetSize() : 1;
        BigDecimal estimatedMonthlyValue = finalPricePerKm.multiply(finalMonthlyKm)
                .multiply(BigDecimal.valueOf(fleetSize));
        deal.setEstimatedMonthlyValue(estimatedMonthlyValue);

        dealRepository.save(deal);

        // Fire notification to agent
        User agent = deal.getAssignedAgent();
        String pricingDetails = "Final price/km: " + finalPricePerKm + ", Monthly km: " + finalMonthlyKm
                + ", Estimated monthly value: " + estimatedMonthlyValue;
        notificationService.firePricingDecision(deal, agent, true, pricingDetails, null);

        // Audit
        auditService.log(AuditEntityType.PRICING_SUBMISSION, submissionId, AuditAction.APPROVE, actorId,
                Map.of("dealId", dealId, "estimatedMonthlyValue", estimatedMonthlyValue.toString()));

        return toResponse(submission);
    }

    public PricingSubmissionResponse reject(Long dealId, Long submissionId, PricingRejectRequest request, Long actorId) {
        PricingSubmission submission = pricingSubmissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("PricingSubmission", submissionId));

        if (!submission.getDeal().getId().equals(dealId)) {
            throw new ResourceNotFoundException("PricingSubmission", submissionId);
        }

        if (submission.getStatus() != PricingStatus.SUBMITTED) {
            throw new BusinessRuleViolationException("Only submitted pricing can be rejected");
        }

        User reviewer = new User();
        reviewer.setId(actorId);
        submission.setStatus(PricingStatus.REJECTED);
        submission.setReviewedBy(reviewer);
        submission.setReviewedAt(OffsetDateTime.now());
        submission.setRejectionNote(request.getRejectionNote());

        submission = pricingSubmissionRepository.save(submission);

        // SubStatus stays AWAITING_APPROVAL

        // Fire notification to agent
        Deal deal = submission.getDeal();
        User agent = deal.getAssignedAgent();
        notificationService.firePricingDecision(deal, agent, false,
                "Price/km: " + submission.getPricePerKm() + ", Monthly km: " + submission.getMonthlyKmCommitment(),
                request.getRejectionNote());

        // Audit
        auditService.log(AuditEntityType.PRICING_SUBMISSION, submissionId, AuditAction.REJECT, actorId,
                Map.of("dealId", dealId, "rejectionNote", request.getRejectionNote()));

        return toResponse(submission);
    }

    @Transactional(readOnly = true)
    public List<PricingSubmissionResponse> getHistory(Long dealId) {
        return pricingSubmissionRepository.findByDealIdOrderByCreatedAtDesc(dealId).stream()
                .map(this::toResponse)
                .toList();
    }

    private PricingSubmissionResponse toResponse(PricingSubmission submission) {
        PricingSubmissionResponse resp = new PricingSubmissionResponse();
        resp.setId(submission.getId());
        resp.setDealId(submission.getDeal().getId());
        resp.setSubmittedBy(new UserSummary(submission.getSubmittedBy().getId(), submission.getSubmittedBy().getName()));
        resp.setServicesSelected(submission.getServicesSelected());
        resp.setMonthlyKmCommitment(submission.getMonthlyKmCommitment());
        resp.setPricePerKm(submission.getPricePerKm());
        resp.setMonthlyValuePerVehicle(submission.getMonthlyValuePerVehicle());
        resp.setManagerServicesSelected(submission.getManagerServicesSelected());
        resp.setManagerMonthlyKm(submission.getManagerMonthlyKm());
        resp.setManagerPricePerKm(submission.getManagerPricePerKm());
        resp.setTokenAmount(submission.getTokenAmount());
        resp.setManagerTokenAmount(submission.getManagerTokenAmount());
        resp.setStatus(submission.getStatus());
        if (submission.getReviewedBy() != null) {
            resp.setReviewedBy(new UserSummary(submission.getReviewedBy().getId(), submission.getReviewedBy().getName()));
        }
        resp.setReviewedAt(submission.getReviewedAt());
        resp.setRejectionNote(submission.getRejectionNote());
        resp.setCreatedAt(submission.getCreatedAt());
        return resp;
    }
}
