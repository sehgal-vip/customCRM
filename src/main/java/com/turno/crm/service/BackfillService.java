package com.turno.crm.service;

import com.turno.crm.exception.BusinessRuleViolationException;
import com.turno.crm.exception.ResourceNotFoundException;
import com.turno.crm.model.dto.BackfillRequestDto;
import com.turno.crm.model.dto.BackfillRequestResponse;
import com.turno.crm.model.entity.BackfillRequest;
import com.turno.crm.model.entity.Deal;
import com.turno.crm.model.entity.User;
import com.turno.crm.model.enums.*;
import com.turno.crm.repository.BackfillRequestRepository;
import com.turno.crm.repository.DealRepository;
import com.turno.crm.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;

@Service
@Transactional
public class BackfillService {

    private final BackfillRequestRepository backfillRequestRepository;
    private final DealRepository dealRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    public BackfillService(BackfillRequestRepository backfillRequestRepository,
                           DealRepository dealRepository,
                           UserRepository userRepository,
                           AuditService auditService) {
        this.backfillRequestRepository = backfillRequestRepository;
        this.dealRepository = dealRepository;
        this.userRepository = userRepository;
        this.auditService = auditService;
    }

    public BackfillRequestResponse requestBackfill(Long dealId, BackfillRequestDto request, Long actorId) {
        Deal deal = dealRepository.findById(dealId)
                .orElseThrow(() -> new ResourceNotFoundException("Deal", dealId));

        User requester = userRepository.findById(actorId)
                .orElseThrow(() -> new ResourceNotFoundException("User", actorId));

        DealStage targetStage = DealStage.valueOf(request.getTargetStage());

        // Validate within 30-day window
        long daysSinceOriginal = ChronoUnit.DAYS.between(request.getOriginalStartDate(), LocalDate.now());
        if (daysSinceOriginal > 30) {
            throw new BusinessRuleViolationException(
                    "Backfill requests must be within 30 days of the original start date. Days elapsed: " + daysSinceOriginal);
        }

        BackfillRequest backfill = new BackfillRequest();
        backfill.setDeal(deal);
        backfill.setRequestedBy(requester);
        backfill.setTargetStage(targetStage);
        backfill.setContext(request.getContext());
        backfill.setOriginalStartDate(request.getOriginalStartDate());
        backfill.setStatus(ApprovalStatus.PENDING);

        backfill = backfillRequestRepository.save(backfill);

        auditService.log(AuditEntityType.DEAL, dealId, AuditAction.BACKFILL_REQUEST, actorId,
                Map.of("targetStage", targetStage.name(), "originalStartDate", request.getOriginalStartDate().toString()));

        return toResponse(backfill);
    }

    public BackfillRequestResponse approveBackfill(Long requestId, Long actorId) {
        BackfillRequest backfill = backfillRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("BackfillRequest", requestId));

        if (backfill.getStatus() != ApprovalStatus.PENDING) {
            throw new BusinessRuleViolationException("Backfill request is not in PENDING status");
        }

        User reviewer = userRepository.findById(actorId)
                .orElseThrow(() -> new ResourceNotFoundException("User", actorId));

        backfill.setStatus(ApprovalStatus.APPROVED);
        backfill.setReviewedBy(reviewer);
        backfill.setReviewedAt(OffsetDateTime.now());
        backfill = backfillRequestRepository.save(backfill);

        // Move deal to target stage, set backfilled=true
        Deal deal = backfill.getDeal();
        DealStage targetStage = backfill.getTargetStage();

        // Clear sub_status if moving away from STAGE_4
        if (deal.getCurrentStage() == DealStage.STAGE_4 && targetStage != DealStage.STAGE_4) {
            deal.setSubStatus(null);
        }
        // If moving to STAGE_4, we need a sub_status — set default
        if (targetStage == DealStage.STAGE_4 && deal.getCurrentStage() != DealStage.STAGE_4) {
            deal.setSubStatus(Stage5SubStatus.values()[0]);
        }

        deal.setCurrentStage(targetStage);
        deal.setBackfilled(true);
        deal.setBackfillApprovedBy(reviewer);
        deal.setOriginalStartDate(backfill.getOriginalStartDate());
        dealRepository.save(deal);

        auditService.log(AuditEntityType.DEAL, deal.getId(), AuditAction.BACKFILL_APPROVE, actorId,
                Map.of("requestId", requestId, "targetStage", targetStage.name()));

        return toResponse(backfill);
    }

    public BackfillRequestResponse rejectBackfill(Long requestId, Long actorId) {
        BackfillRequest backfill = backfillRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("BackfillRequest", requestId));

        if (backfill.getStatus() != ApprovalStatus.PENDING) {
            throw new BusinessRuleViolationException("Backfill request is not in PENDING status");
        }

        User reviewer = userRepository.findById(actorId)
                .orElseThrow(() -> new ResourceNotFoundException("User", actorId));

        backfill.setStatus(ApprovalStatus.REJECTED);
        backfill.setReviewedBy(reviewer);
        backfill.setReviewedAt(OffsetDateTime.now());
        backfill = backfillRequestRepository.save(backfill);

        auditService.log(AuditEntityType.DEAL, backfill.getDeal().getId(), AuditAction.BACKFILL_REJECT, actorId,
                Map.of("requestId", requestId));

        return toResponse(backfill);
    }

    private BackfillRequestResponse toResponse(BackfillRequest backfill) {
        BackfillRequestResponse resp = new BackfillRequestResponse();
        resp.setId(backfill.getId());
        resp.setDealId(backfill.getDeal().getId());
        resp.setDealName(backfill.getDeal().getName());
        resp.setRequestedById(backfill.getRequestedBy().getId());
        resp.setRequestedByName(backfill.getRequestedBy().getName());
        resp.setTargetStage(backfill.getTargetStage());
        resp.setContext(backfill.getContext());
        resp.setOriginalStartDate(backfill.getOriginalStartDate());
        resp.setStatus(backfill.getStatus());
        if (backfill.getReviewedBy() != null) {
            resp.setReviewedById(backfill.getReviewedBy().getId());
            resp.setReviewedByName(backfill.getReviewedBy().getName());
        }
        resp.setReviewedAt(backfill.getReviewedAt());
        resp.setCreatedAt(backfill.getCreatedAt());
        return resp;
    }
}
