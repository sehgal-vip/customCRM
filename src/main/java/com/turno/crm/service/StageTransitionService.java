package com.turno.crm.service;

import com.turno.crm.exception.BusinessRuleViolationException;
import com.turno.crm.exception.ExitCriteriaNotMetException;
import com.turno.crm.exception.ExitCriteriaNotMetException.CriteriaResult;
import com.turno.crm.exception.ResourceNotFoundException;
import com.turno.crm.exception.UnauthorizedAccessException;
import com.turno.crm.model.dto.DealResponse;
import com.turno.crm.model.dto.StageTransitionResponse;
import com.turno.crm.model.entity.*;
import com.turno.crm.model.enums.*;
import com.turno.crm.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class StageTransitionService {

    private final DealRepository dealRepository;
    private final StageTransitionRepository stageTransitionRepository;
    private final RegressionRequestRepository regressionRequestRepository;
    private final UserRepository userRepository;
    private final ExitCriteriaValidator exitCriteriaValidator;
    private final AuditService auditService;
    private final DealService dealService;
    private final NotificationService notificationService;

    public StageTransitionService(DealRepository dealRepository,
                                   StageTransitionRepository stageTransitionRepository,
                                   RegressionRequestRepository regressionRequestRepository,
                                   UserRepository userRepository,
                                   ExitCriteriaValidator exitCriteriaValidator,
                                   AuditService auditService,
                                   DealService dealService,
                                   NotificationService notificationService) {
        this.dealRepository = dealRepository;
        this.stageTransitionRepository = stageTransitionRepository;
        this.regressionRequestRepository = regressionRequestRepository;
        this.userRepository = userRepository;
        this.exitCriteriaValidator = exitCriteriaValidator;
        this.auditService = auditService;
        this.dealService = dealService;
        this.notificationService = notificationService;
    }

    public StageTransitionResponse moveForward(Long dealId, String overrideReason, Long actorId) {
        Deal deal = dealRepository.findById(dealId)
                .orElseThrow(() -> new ResourceNotFoundException("Deal", dealId));

        if (deal.getStatus() != DealStatus.ACTIVE) {
            throw new BusinessRuleViolationException("Only active deals can be advanced");
        }

        DealStage currentStage = deal.getCurrentStage();
        DealStage nextStage = currentStage.next();

        if (nextStage == null && currentStage != DealStage.STAGE_8) {
            throw new BusinessRuleViolationException("Deal is already at the final stage");
        }

        // Validate exit criteria
        List<CriteriaResult> results = exitCriteriaValidator.validateForward(deal);

        List<CriteriaResult> hardFailures = results.stream()
                .filter(r -> !r.met() && !r.softBlock())
                .toList();
        List<CriteriaResult> softFailures = results.stream()
                .filter(r -> !r.met() && r.softBlock())
                .toList();

        if (!hardFailures.isEmpty()) {
            throw new ExitCriteriaNotMetException(results);
        }

        if (!softFailures.isEmpty() && (overrideReason == null || overrideReason.isBlank())) {
            throw new ExitCriteriaNotMetException(results);
        }

        // If soft failures exist but overrideReason provided, audit the override
        if (!softFailures.isEmpty()) {
            auditService.log(AuditEntityType.DEAL, dealId, AuditAction.STAGE_FORWARD, actorId,
                    Map.of("softOverride", true, "overrideReason", overrideReason,
                            "overriddenCriteria", softFailures.stream().map(CriteriaResult::rule).toList()));
        }

        DealStage fromStage = currentStage;

        // Handle Stage 4 exit: clear subStatus
        if (currentStage == DealStage.STAGE_4) {
            deal.setSubStatus(null);
        }

        // Handle STAGE_8 forward: complete the deal
        if (currentStage == DealStage.STAGE_8) {
            deal.setStatus(DealStatus.COMPLETED);
            deal = dealRepository.save(deal);

            StageTransition transition = createTransition(deal, fromStage, fromStage, TransitionType.COMPLETE, actorId);
            stageTransitionRepository.save(transition);

            auditService.log(AuditEntityType.DEAL, dealId, AuditAction.COMPLETE, actorId,
                    Map.of("fromStage", fromStage.name()));

            DealResponse dealResponse = dealService.getById(dealId, actorId, UserRole.MANAGER);
            return new StageTransitionResponse(dealResponse, fromStage.name(), "COMPLETED", "COMPLETE");
        }

        // Set next stage
        deal.setCurrentStage(nextStage);

        // Handle Stage 4 entry: set subStatus = PROPOSAL_SENT
        if (nextStage == DealStage.STAGE_4) {
            deal.setSubStatus(Stage5SubStatus.PROPOSAL_SENT);
        }

        deal = dealRepository.save(deal);

        StageTransition transition = createTransition(deal, fromStage, nextStage, TransitionType.FORWARD, actorId);
        stageTransitionRepository.save(transition);

        auditService.log(AuditEntityType.DEAL, dealId, AuditAction.STAGE_FORWARD, actorId,
                Map.of("fromStage", fromStage.name(), "toStage", nextStage.name()));

        DealResponse dealResponse = dealService.getById(dealId, actorId, UserRole.MANAGER);
        return new StageTransitionResponse(dealResponse, fromStage.name(), nextStage.name(), "FORWARD");
    }

    public StageTransitionResponse requestBackward(Long dealId, String reason, Long actorId, UserRole actorRole) {
        Deal deal = dealRepository.findById(dealId)
                .orElseThrow(() -> new ResourceNotFoundException("Deal", dealId));

        if (deal.getStatus() != DealStatus.ACTIVE) {
            throw new BusinessRuleViolationException("Only active deals can be regressed");
        }

        DealStage currentStage = deal.getCurrentStage();
        DealStage previousStage = currentStage.previous();

        if (previousStage == null) {
            throw new BusinessRuleViolationException("Deal is already at Stage 1 and cannot go backward");
        }

        if (actorRole == UserRole.MANAGER) {
            // Manager: move immediately (adjacent stage only)
            return executeBackward(deal, currentStage, previousStage, reason, actorId, null);
        } else {
            // Agent: create regression request, notify manager
            User requestedBy = new User();
            requestedBy.setId(actorId);

            RegressionRequest request = new RegressionRequest();
            request.setDeal(deal);
            request.setRequestedBy(requestedBy);
            request.setFromStage(currentStage);
            request.setToStage(previousStage);
            request.setReason(reason);
            request.setStatus(ApprovalStatus.PENDING);
            regressionRequestRepository.save(request);

            auditService.log(AuditEntityType.DEAL, dealId, AuditAction.REGRESSION_REQUEST, actorId,
                    Map.of("fromStage", currentStage.name(), "toStage", previousStage.name(), "reason", reason));

            // Fire regression requested notification
            User agent = userRepository.findById(actorId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", actorId));
            notificationService.fireRegressionRequested(deal, agent, currentStage, previousStage, reason);

            DealResponse dealResponse = dealService.getById(dealId, actorId, actorRole);
            return new StageTransitionResponse(dealResponse, currentStage.name(), previousStage.name(), "REGRESSION_REQUESTED");
        }
    }

    public StageTransitionResponse approveRegression(Long requestId, Long actorId) {
        RegressionRequest request = regressionRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("RegressionRequest", requestId));

        if (request.getStatus() != ApprovalStatus.PENDING) {
            throw new BusinessRuleViolationException("Regression request is not pending");
        }

        User reviewer = new User();
        reviewer.setId(actorId);
        request.setReviewedBy(reviewer);
        request.setReviewedAt(OffsetDateTime.now());
        request.setStatus(ApprovalStatus.APPROVED);
        regressionRequestRepository.save(request);

        Deal deal = request.getDeal();
        DealStage fromStage = request.getFromStage();
        DealStage toStage = request.getToStage();

        StageTransitionResponse response = executeBackward(deal, fromStage, toStage,
                request.getReason(), request.getRequestedBy().getId(), actorId);

        auditService.log(AuditEntityType.DEAL, deal.getId(), AuditAction.REGRESSION_APPROVE, actorId,
                Map.of("requestId", requestId, "fromStage", fromStage.name(), "toStage", toStage.name()));

        // Fire regression decision notification (approved)
        notificationService.fireRegressionDecision(deal, request.getRequestedBy(), true, fromStage, toStage);

        return response;
    }

    public void rejectRegression(Long requestId, Long actorId) {
        RegressionRequest request = regressionRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("RegressionRequest", requestId));

        if (request.getStatus() != ApprovalStatus.PENDING) {
            throw new BusinessRuleViolationException("Regression request is not pending");
        }

        User reviewer = new User();
        reviewer.setId(actorId);
        request.setReviewedBy(reviewer);
        request.setReviewedAt(OffsetDateTime.now());
        request.setStatus(ApprovalStatus.REJECTED);
        regressionRequestRepository.save(request);

        auditService.log(AuditEntityType.DEAL, request.getDeal().getId(), AuditAction.REGRESSION_REJECT, actorId,
                Map.of("requestId", requestId));

        // Fire regression decision notification (rejected)
        notificationService.fireRegressionDecision(request.getDeal(), request.getRequestedBy(), false,
                request.getFromStage(), request.getToStage());
    }

    private StageTransitionResponse executeBackward(Deal deal, DealStage fromStage, DealStage toStage,
                                                     String reason, Long actorId, Long approvedById) {
        // Handle Stage 4 exit: clear subStatus
        if (fromStage == DealStage.STAGE_4) {
            deal.setSubStatus(null);
        }

        deal.setCurrentStage(toStage);

        // Handle Stage 4 re-entry: reset subStatus = PROPOSAL_SENT
        if (toStage == DealStage.STAGE_4) {
            deal.setSubStatus(Stage5SubStatus.PROPOSAL_SENT);
        }

        deal = dealRepository.save(deal);

        StageTransition transition = createTransition(deal, fromStage, toStage, TransitionType.BACKWARD, actorId);
        transition.setReason(reason);
        if (approvedById != null) {
            User approvedBy = new User();
            approvedBy.setId(approvedById);
            transition.setApprovedBy(approvedBy);
        }
        stageTransitionRepository.save(transition);

        auditService.log(AuditEntityType.DEAL, deal.getId(), AuditAction.STAGE_BACKWARD, actorId,
                Map.of("fromStage", fromStage.name(), "toStage", toStage.name(), "reason", reason));

        DealResponse dealResponse = dealService.getById(deal.getId(), actorId, UserRole.MANAGER);
        return new StageTransitionResponse(dealResponse, fromStage.name(), toStage.name(), "BACKWARD");
    }

    private StageTransition createTransition(Deal deal, DealStage from, DealStage to,
                                              TransitionType type, Long actorId) {
        StageTransition transition = new StageTransition();
        transition.setDeal(deal);
        transition.setFromStage(from);
        transition.setToStage(to);
        transition.setTransitionType(type);

        User actor = new User();
        actor.setId(actorId);
        transition.setActor(actor);

        return transition;
    }
}
