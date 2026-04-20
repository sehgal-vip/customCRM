package com.turno.crm.service;

import com.turno.crm.exception.BusinessRuleViolationException;
import com.turno.crm.exception.DuplicateResourceException;
import com.turno.crm.exception.ResourceNotFoundException;
import com.turno.crm.exception.UnauthorizedAccessException;
import com.turno.crm.model.dto.*;
import com.turno.crm.model.entity.*;
import com.turno.crm.model.enums.*;
import com.turno.crm.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class DealService {

    private final DealRepository dealRepository;
    private final OperatorRepository operatorRepository;
    private final UserRepository userRepository;
    private final DocumentChecklistItemRepository checklistItemRepository;
    private final DealDocumentRepository dealDocumentRepository;
    private final StageTransitionRepository stageTransitionRepository;
    private final PricingSubmissionRepository pricingSubmissionRepository;
    private final ActivityReportRepository activityReportRepository;
    private final AuditService auditService;
    private final NotificationService notificationService;

    public DealService(DealRepository dealRepository, OperatorRepository operatorRepository,
                       UserRepository userRepository, DocumentChecklistItemRepository checklistItemRepository,
                       DealDocumentRepository dealDocumentRepository,
                       StageTransitionRepository stageTransitionRepository,
                       PricingSubmissionRepository pricingSubmissionRepository,
                       ActivityReportRepository activityReportRepository,
                       AuditService auditService,
                       NotificationService notificationService) {
        this.dealRepository = dealRepository;
        this.operatorRepository = operatorRepository;
        this.userRepository = userRepository;
        this.checklistItemRepository = checklistItemRepository;
        this.dealDocumentRepository = dealDocumentRepository;
        this.stageTransitionRepository = stageTransitionRepository;
        this.pricingSubmissionRepository = pricingSubmissionRepository;
        this.activityReportRepository = activityReportRepository;
        this.auditService = auditService;
        this.notificationService = notificationService;
    }

    @Transactional(readOnly = true)
    public Page<DealListResponse> list(DealStatus status, List<DealStatus> statuses, DealStage stage, List<Long> agentIds,
                                       List<Long> operatorIds, List<Long> regionIds,
                                       OffsetDateTime dateFrom, OffsetDateTime dateTo,
                                       Long userId, UserRole userRole, Pageable pageable) {
        Specification<Deal> spec = Specification.where(null);

        // Agent sees own deals only
        if (userRole == UserRole.AGENT) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("assignedAgent").get("id"), userId));
        }

        if (statuses != null && !statuses.isEmpty()) {
            spec = spec.and((root, query, cb) -> root.get("status").in(statuses));
        } else if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }
        if (stage != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("currentStage"), stage));
        }
        if (agentIds != null && !agentIds.isEmpty()) {
            spec = spec.and((root, query, cb) -> root.get("assignedAgent").get("id").in(agentIds));
        }
        if (operatorIds != null && !operatorIds.isEmpty()) {
            spec = spec.and((root, query, cb) -> root.get("operator").get("id").in(operatorIds));
        }
        if (regionIds != null && !regionIds.isEmpty()) {
            spec = spec.and((root, query, cb) -> root.get("operator").get("region").get("id").in(regionIds));
        }
        if (dateFrom != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("createdAt"), dateFrom));
        }
        if (dateTo != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("createdAt"), dateTo));
        }

        return dealRepository.findAll(spec, pageable).map(this::toListResponse);
    }

    @Transactional(readOnly = true)
    public DealResponse getById(Long id, Long userId, UserRole userRole) {
        Deal deal = dealRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Deal", id));

        // Agent can only see own deals
        if (userRole == UserRole.AGENT && !deal.getAssignedAgent().getId().equals(userId)) {
            throw new UnauthorizedAccessException("You do not have access to this deal");
        }

        return toResponse(deal);
    }

    public DealResponse create(CreateDealRequest request, Long actorId) {
        if (dealRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Deal with name '" + request.getName() + "' already exists");
        }

        Operator operator = operatorRepository.findById(request.getOperatorId())
                .orElseThrow(() -> new ResourceNotFoundException("Operator", request.getOperatorId()));

        User agent = userRepository.findById(request.getAssignedAgentId())
                .orElseThrow(() -> new ResourceNotFoundException("User", request.getAssignedAgentId()));

        Deal deal = new Deal();
        deal.setName(request.getName());
        deal.setOperator(operator);
        deal.setAssignedAgent(agent);
        deal.setFleetSize(request.getFleetSize());
        deal.setEstimatedMonthlyValue(request.getEstimatedMonthlyValue());
        deal.setLeadSource(request.getLeadSource());
        deal.setCurrentStage(DealStage.STAGE_1);
        deal.setStatus(DealStatus.ACTIVE);

        deal = dealRepository.save(deal);

        // Auto-create deal_documents for all active checklist items
        List<DocumentChecklistItem> activeItems = checklistItemRepository.findByActiveTrue();
        for (DocumentChecklistItem item : activeItems) {
            DealDocument doc = new DealDocument();
            doc.setDeal(deal);
            doc.setChecklistItem(item);
            doc.setStatus(DocStatus.NOT_STARTED);
            dealDocumentRepository.save(doc);
        }

        auditService.log(AuditEntityType.DEAL, deal.getId(), AuditAction.CREATE, actorId,
                Map.of("name", deal.getName(), "operatorId", operator.getId(), "agentId", agent.getId()));

        return toResponse(deal);
    }

    public DealResponse update(Long id, UpdateDealRequest request, Long actorId, UserRole userRole) {
        Deal deal = dealRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Deal", id));

        // Agent can only update own deals and limited fields
        if (userRole == UserRole.AGENT) {
            if (!deal.getAssignedAgent().getId().equals(actorId)) {
                throw new UnauthorizedAccessException("Agents can only update their own deals");
            }
            // Agent cannot change assignedAgent or operator
            if (request.getAssignedAgentId() != null || request.getOperatorId() != null) {
                throw new UnauthorizedAccessException("Agents cannot reassign deals or change operator");
            }
        }

        Map<String, Object> changes = new HashMap<>();

        if (request.getName() != null) {
            // Check uniqueness
            dealRepository.findByName(request.getName()).ifPresent(existing -> {
                if (!existing.getId().equals(id)) {
                    throw new DuplicateResourceException("Deal with name '" + request.getName() + "' already exists");
                }
            });
            changes.put("name", Map.of("from", deal.getName(), "to", request.getName()));
            deal.setName(request.getName());
        }

        if (request.getAssignedAgentId() != null) {
            User previousAgent = deal.getAssignedAgent();
            User newAgent = userRepository.findById(request.getAssignedAgentId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", request.getAssignedAgentId()));
            changes.put("assignedAgentId", Map.of("from", previousAgent.getId(), "to", newAgent.getId()));
            deal.setAssignedAgent(newAgent);

            // Fire reassignment notifications
            if (!previousAgent.getId().equals(newAgent.getId())) {
                User manager = userRepository.findById(actorId)
                        .orElseThrow(() -> new ResourceNotFoundException("User", actorId));
                notificationService.fireDealAssignedNewOwner(deal, newAgent, manager);
                notificationService.fireDealReassignedPreviousOwner(deal, previousAgent, newAgent, manager);
            }
        }

        if (request.getOperatorId() != null) {
            Operator newOperator = operatorRepository.findById(request.getOperatorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Operator", request.getOperatorId()));
            changes.put("operatorId", Map.of("from", deal.getOperator().getId(), "to", newOperator.getId()));
            deal.setOperator(newOperator);
        }

        if (request.getFleetSize() != null) {
            changes.put("fleetSize", Map.of("from", String.valueOf(deal.getFleetSize()), "to", request.getFleetSize()));
            deal.setFleetSize(request.getFleetSize());
        }

        if (request.getEstimatedMonthlyValue() != null) {
            // Check if pricing is approved - if so, estimatedMonthlyValue is read-only
            boolean pricingApproved = pricingSubmissionRepository.existsByDealIdAndStatus(id, PricingStatus.APPROVED);
            if (pricingApproved) {
                throw new BusinessRuleViolationException("Cannot modify estimated monthly value after pricing is approved");
            }
            changes.put("estimatedMonthlyValue", Map.of(
                    "from", String.valueOf(deal.getEstimatedMonthlyValue()),
                    "to", request.getEstimatedMonthlyValue()));
            deal.setEstimatedMonthlyValue(request.getEstimatedMonthlyValue());
        }

        deal = dealRepository.save(deal);

        auditService.log(AuditEntityType.DEAL, deal.getId(), AuditAction.UPDATE, actorId, changes);

        return toResponse(deal);
    }

    public DealResponse archive(Long id, ArchiveDealRequest request, Long actorId) {
        Deal deal = dealRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Deal", id));

        if (deal.getStatus() != DealStatus.ACTIVE) {
            throw new BusinessRuleViolationException("Only active deals can be archived");
        }

        deal.setStatus(DealStatus.ARCHIVED);
        deal.setArchivedReason(request.getReason());
        deal.setArchivedReasonText(request.getReasonText());
        // Preserve current stage and sub_status

        deal = dealRepository.save(deal);

        auditService.log(AuditEntityType.DEAL, deal.getId(), AuditAction.ARCHIVE, actorId,
                Map.of("reason", request.getReason(),
                       "reasonText", request.getReasonText() != null ? request.getReasonText() : "",
                       "stage", deal.getCurrentStage().name()));

        // Fire notification
        notificationService.fireDealArchived(deal, deal.getAssignedAgent(), request.getReason());

        return toResponse(deal);
    }

    public DealResponse reactivate(Long id, Long actorId) {
        Deal deal = dealRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Deal", id));

        if (deal.getStatus() != DealStatus.ARCHIVED) {
            throw new BusinessRuleViolationException("Only archived deals can be reactivated");
        }

        deal.setStatus(DealStatus.ACTIVE);
        deal.setArchivedReason(null);
        deal.setArchivedReasonText(null);

        deal = dealRepository.save(deal);

        auditService.log(AuditEntityType.DEAL, deal.getId(), AuditAction.REACTIVATE, actorId, Map.of());

        // Fire notification
        notificationService.fireDealReactivated(deal, deal.getAssignedAgent());

        return toResponse(deal);
    }

    public DealResponse reopen(Long id, Long actorId) {
        Deal deal = dealRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Deal", id));

        if (deal.getStatus() != DealStatus.COMPLETED) {
            throw new BusinessRuleViolationException("Only completed deals can be reopened");
        }

        deal.setStatus(DealStatus.ACTIVE);
        deal.setCurrentStage(DealStage.STAGE_8);
        deal.setReopened(true);

        deal = dealRepository.save(deal);

        auditService.log(AuditEntityType.DEAL, deal.getId(), AuditAction.REOPEN, actorId, Map.of());

        return toResponse(deal);
    }

    private DealResponse toResponse(Deal deal) {
        DealResponse resp = new DealResponse();
        resp.setId(deal.getId());
        resp.setName(deal.getName());
        resp.setOperator(new OperatorSummary(deal.getOperator().getId(), deal.getOperator().getCompanyName()));
        resp.setAssignedAgent(new UserSummary(deal.getAssignedAgent().getId(), deal.getAssignedAgent().getName()));
        resp.setFleetSize(deal.getFleetSize());
        resp.setEstimatedMonthlyValue(deal.getEstimatedMonthlyValue());
        resp.setLeadSource(deal.getLeadSource());
        resp.setCurrentStage(deal.getCurrentStage());
        resp.setSubStatus(deal.getSubStatus());
        resp.setStatus(deal.getStatus());
        resp.setArchivedReason(deal.getArchivedReason());
        resp.setArchivedReasonText(deal.getArchivedReasonText());
        resp.setReopened(deal.getReopened() != null && deal.getReopened());
        resp.setBackfilled(deal.getBackfilled() != null && deal.getBackfilled());
        resp.setCreatedAt(deal.getCreatedAt());
        resp.setUpdatedAt(deal.getUpdatedAt());
        resp.setPricingApproved(pricingSubmissionRepository.existsByDealIdAndStatus(deal.getId(), PricingStatus.APPROVED));

        // Calculate daysInStage
        resp.setDaysInStage(calculateDaysInStage(deal));

        // Calculate docCompletionPct
        resp.setDocCompletionPct(calculateDocCompletionPct(deal.getId()));

        // Set nextAction and nextActionEta from latest non-voided report
        Optional<ActivityReport> latestReport = activityReportRepository
            .findFirstByDealIdAndVoidedFalseOrderBySubmissionDatetimeDesc(deal.getId());
        if (latestReport.isPresent()) {
            ActivityReport r = latestReport.get();
            resp.setNextAction(r.getNextAction());
            resp.setNextActionEta(r.getNextActionEta() != null ? r.getNextActionEta().toString() : null);
        }

        return resp;
    }

    private DealListResponse toListResponse(Deal deal) {
        DealListResponse resp = new DealListResponse();
        resp.setId(deal.getId());
        resp.setName(deal.getName());
        resp.setOperatorName(deal.getOperator().getCompanyName());
        resp.setAgentName(deal.getAssignedAgent().getName());
        resp.setFleetSize(deal.getFleetSize());
        resp.setEstimatedMonthlyValue(deal.getEstimatedMonthlyValue());
        resp.setCurrentStage(deal.getCurrentStage());
        resp.setSubStatus(deal.getSubStatus());
        resp.setStatus(deal.getStatus());
        resp.setDaysInStage(calculateDaysInStage(deal));
        resp.setPricingApproved(pricingSubmissionRepository.existsByDealIdAndStatus(deal.getId(), PricingStatus.APPROVED));

        // Set nextAction and nextActionEta from latest non-voided report
        Optional<ActivityReport> latestReport = activityReportRepository
            .findFirstByDealIdAndVoidedFalseOrderBySubmissionDatetimeDesc(deal.getId());
        if (latestReport.isPresent()) {
            ActivityReport r = latestReport.get();
            resp.setNextAction(r.getNextAction());
            resp.setNextActionEta(r.getNextActionEta() != null ? r.getNextActionEta().atStartOfDay().atOffset(java.time.ZoneOffset.UTC) : null);
        }

        return resp;
    }

    private long calculateDaysInStage(Deal deal) {
        List<StageTransition> transitions = stageTransitionRepository.findByDealIdOrderByCreatedAtDesc(deal.getId());
        OffsetDateTime stageEntryDate;
        if (!transitions.isEmpty()) {
            stageEntryDate = transitions.get(0).getCreatedAt();
        } else {
            stageEntryDate = deal.getCreatedAt();
        }
        return ChronoUnit.DAYS.between(stageEntryDate, OffsetDateTime.now());
    }

    private double calculateDocCompletionPct(Long dealId) {
        long total = dealDocumentRepository.countMandatoryTotal(dealId);
        if (total == 0) return 100.0;
        long complete = dealDocumentRepository.countMandatoryComplete(dealId);
        return Math.round((double) complete / total * 10000.0) / 100.0;
    }
}
