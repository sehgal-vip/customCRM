package com.turno.crm.service;

import com.turno.crm.model.dto.*;
import com.turno.crm.model.entity.*;
import com.turno.crm.model.enums.*;
import com.turno.crm.repository.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    private final DealRepository dealRepository;
    private final UserRepository userRepository;
    private final ActivityReportRepository activityReportRepository;
    private final PricingSubmissionRepository pricingSubmissionRepository;
    private final RegressionRequestRepository regressionRequestRepository;
    private final StageTransitionRepository stageTransitionRepository;
    private final DealDocumentRepository dealDocumentRepository;
    private final AdminSettingRepository adminSettingRepository;

    private static final Map<DealStage, Integer> DEFAULT_STALE_THRESHOLDS = Map.of(
            DealStage.STAGE_1, 7,
            DealStage.STAGE_2, 14,
            DealStage.STAGE_3, 21,
            DealStage.STAGE_4, 30,
            DealStage.STAGE_5, 14,
            DealStage.STAGE_6, 21,
            DealStage.STAGE_7, 14,
            DealStage.STAGE_8, 30
    );

    public DashboardService(DealRepository dealRepository,
                             UserRepository userRepository,
                             ActivityReportRepository activityReportRepository,
                             PricingSubmissionRepository pricingSubmissionRepository,
                             RegressionRequestRepository regressionRequestRepository,
                             StageTransitionRepository stageTransitionRepository,
                             DealDocumentRepository dealDocumentRepository,
                             AdminSettingRepository adminSettingRepository) {
        this.dealRepository = dealRepository;
        this.userRepository = userRepository;
        this.activityReportRepository = activityReportRepository;
        this.pricingSubmissionRepository = pricingSubmissionRepository;
        this.regressionRequestRepository = regressionRequestRepository;
        this.stageTransitionRepository = stageTransitionRepository;
        this.dealDocumentRepository = dealDocumentRepository;
        this.adminSettingRepository = adminSettingRepository;
    }

    public PipelineOverviewResponse getPipelineOverview(OffsetDateTime dateFrom, OffsetDateTime dateTo,
                                                         List<Long> agentIds, List<Long> regionIds) {
        Specification<Deal> spec = (root, query, cb) -> cb.equal(root.get("status"), DealStatus.ACTIVE);

        if (dateFrom != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("createdAt"), dateFrom));
        }
        if (dateTo != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("createdAt"), dateTo));
        }
        if (agentIds != null && !agentIds.isEmpty()) {
            spec = spec.and((root, query, cb) -> root.get("assignedAgent").get("id").in(agentIds));
        }
        if (regionIds != null && !regionIds.isEmpty()) {
            spec = spec.and((root, query, cb) -> root.get("operator").get("region").get("id").in(regionIds));
        }

        List<Deal> deals = dealRepository.findAll(spec);

        // Group by stage
        Map<DealStage, List<Deal>> byStage = deals.stream()
                .collect(Collectors.groupingBy(Deal::getCurrentStage));

        List<StageMetric> stageMetrics = new ArrayList<>();
        for (DealStage stage : DealStage.values()) {
            List<Deal> stageDeals = byStage.getOrDefault(stage, List.of());
            BigDecimal totalValue = stageDeals.stream()
                    .map(d -> d.getEstimatedMonthlyValue() != null ? d.getEstimatedMonthlyValue() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            stageMetrics.add(new StageMetric(stage.name(), stageDeals.size(), totalValue));
        }

        // Pending pricing count
        long pendingPricing = deals.stream()
                .filter(d -> d.getCurrentStage() == DealStage.STAGE_4
                        && d.getSubStatus() == Stage5SubStatus.AWAITING_APPROVAL)
                .count();

        // Pending regression count
        int pendingRegression = regressionRequestRepository.findByStatus(ApprovalStatus.PENDING).size();

        PipelineOverviewResponse resp = new PipelineOverviewResponse();
        resp.setStageMetrics(stageMetrics);
        resp.setPendingPricing((int) pendingPricing);
        resp.setPendingRegression(pendingRegression);
        return resp;
    }

    public AgentPerformanceResponse getAgentPerformance(OffsetDateTime dateFrom, OffsetDateTime dateTo) {
        List<User> agents = userRepository.findByRoleAndStatus(UserRole.AGENT, UserStatus.ACTIVE);
        LocalDate today = LocalDate.now();

        List<AgentMetric> metrics = new ArrayList<>();
        for (User agent : agents) {
            List<Deal> agentDeals = dealRepository.findByAssignedAgentIdAndStatus(agent.getId(), DealStatus.ACTIVE);

            BigDecimal pipelineValue = agentDeals.stream()
                    .map(d -> d.getEstimatedMonthlyValue() != null ? d.getEstimatedMonthlyValue() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            int overdueFollowUps = 0;
            for (Deal deal : agentDeals) {
                Optional<ActivityReport> latestReport = activityReportRepository
                        .findFirstByDealIdAndVoidedFalseOrderBySubmissionDatetimeDesc(deal.getId());
                if (latestReport.isPresent()) {
                    LocalDate eta = latestReport.get().getNextActionEta();
                    if (eta != null && eta.isBefore(today)) {
                        overdueFollowUps++;
                    }
                }
            }

            AgentMetric metric = new AgentMetric();
            metric.setAgentId(agent.getId());
            metric.setAgentName(agent.getName());
            metric.setActiveDeals(agentDeals.size());
            metric.setPipelineValue(pipelineValue);
            metric.setOverdueFollowUps(overdueFollowUps);
            metrics.add(metric);
        }

        AgentPerformanceResponse resp = new AgentPerformanceResponse();
        resp.setAgents(metrics);
        return resp;
    }

    public WinLossResponse getWinLoss(OffsetDateTime dateFrom, OffsetDateTime dateTo) {
        // Completed deals = wins
        Specification<Deal> completedSpec = (root, query, cb) -> cb.equal(root.get("status"), DealStatus.COMPLETED);
        if (dateFrom != null) {
            completedSpec = completedSpec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("createdAt"), dateFrom));
        }
        if (dateTo != null) {
            completedSpec = completedSpec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("createdAt"), dateTo));
        }
        long wins = dealRepository.count(completedSpec);

        // Archived deals = losses
        Specification<Deal> archivedSpec = (root, query, cb) -> cb.equal(root.get("status"), DealStatus.ARCHIVED);
        if (dateFrom != null) {
            archivedSpec = archivedSpec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("createdAt"), dateFrom));
        }
        if (dateTo != null) {
            archivedSpec = archivedSpec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("createdAt"), dateTo));
        }
        List<Deal> archivedDeals = dealRepository.findAll(archivedSpec);
        long losses = archivedDeals.size();

        double winRate = (wins + losses) == 0 ? 0 : Math.round((double) wins / (wins + losses) * 10000.0) / 100.0;

        // Lost reason breakdown
        Map<String, Integer> reasonCounts = new HashMap<>();
        for (Deal deal : archivedDeals) {
            String reason = deal.getArchivedReason() != null ? deal.getArchivedReason() : "Unknown";
            reasonCounts.merge(reason, 1, Integer::sum);
        }
        List<LostReasonCount> lostReasons = reasonCounts.entrySet().stream()
                .map(e -> new LostReasonCount(e.getKey(), e.getValue()))
                .sorted((a, b) -> b.getCount() - a.getCount())
                .toList();

        WinLossResponse resp = new WinLossResponse();
        resp.setWinRate(winRate);
        resp.setLostReasons(lostReasons);
        return resp;
    }

    public ObjectionHeatmapResponse getObjectionHeatmap(OffsetDateTime dateFrom, OffsetDateTime dateTo, Long agentId) {
        List<Deal> activeDeals;
        if (agentId != null) {
            activeDeals = dealRepository.findByAssignedAgentIdAndStatus(agentId, DealStatus.ACTIVE);
        } else {
            activeDeals = dealRepository.findAll(
                    (root, query, cb) -> cb.equal(root.get("status"), DealStatus.ACTIVE));
        }

        Map<String, Integer> objectionCounts = new HashMap<>();
        for (Deal deal : activeDeals) {
            List<ActivityReport> reports = activityReportRepository.findNonVoidedByDealId(deal.getId());
            for (ActivityReport report : reports) {
                if (report.getObjections() != null) {
                    for (String objection : report.getObjections()) {
                        objectionCounts.merge(objection, 1, Integer::sum);
                    }
                }
            }
        }

        List<ObjectionCount> objections = objectionCounts.entrySet().stream()
                .map(e -> new ObjectionCount(e.getKey(), e.getValue()))
                .sorted((a, b) -> b.getCount() - a.getCount())
                .toList();

        ObjectionHeatmapResponse resp = new ObjectionHeatmapResponse();
        resp.setObjections(objections);
        return resp;
    }

    public DashboardAlertsResponse getAlerts() {
        List<Deal> activeDeals = dealRepository.findAll(
                (root, query, cb) -> cb.equal(root.get("status"), DealStatus.ACTIVE));

        List<DealAlertItem> staleDeals = new ArrayList<>();
        List<DealAlertItem> incompleteDocsApproachingLease = new ArrayList<>();
        List<DealAlertItem> highValueAtRisk = new ArrayList<>();

        for (Deal deal : activeDeals) {
            long daysInStage = calculateDaysInStage(deal);
            int threshold = DEFAULT_STALE_THRESHOLDS.getOrDefault(deal.getCurrentStage(), 21);

            // Stale deals
            if (daysInStage > threshold) {
                staleDeals.add(toAlertItem(deal, daysInStage,
                        "In " + deal.getCurrentStage().getDisplayName() + " for " + daysInStage + " days"));
            }

            // Incomplete docs approaching lease (Stage 6+)
            if (deal.getCurrentStage().getNumber() >= 6) {
                long mandatoryTotal = dealDocumentRepository.countMandatoryTotal(deal.getId());
                long mandatoryComplete = dealDocumentRepository.countMandatoryComplete(deal.getId());
                if (mandatoryTotal > 0 && mandatoryComplete < mandatoryTotal) {
                    incompleteDocsApproachingLease.add(toAlertItem(deal, daysInStage,
                            mandatoryComplete + "/" + mandatoryTotal + " mandatory documents complete"));
                }
            }

            // High value at risk: deals with value > 500000 and overdue follow-up
            if (deal.getEstimatedMonthlyValue() != null
                    && deal.getEstimatedMonthlyValue().compareTo(BigDecimal.valueOf(500000)) > 0) {
                activityReportRepository.findFirstByDealIdAndVoidedFalseOrderBySubmissionDatetimeDesc(deal.getId())
                        .ifPresent(report -> {
                            if (report.getNextActionEta() != null && report.getNextActionEta().isBefore(LocalDate.now())) {
                                highValueAtRisk.add(toAlertItem(deal, daysInStage,
                                        "High value deal with overdue follow-up"));
                            }
                        });
            }
        }

        DashboardAlertsResponse resp = new DashboardAlertsResponse();
        resp.setStaleDeals(staleDeals);
        resp.setIncompleteDocsApproachingLease(incompleteDocsApproachingLease);
        resp.setHighValueAtRisk(highValueAtRisk);
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

    private DealAlertItem toAlertItem(Deal deal, long daysInStage, String alertReason) {
        DealAlertItem item = new DealAlertItem();
        item.setDealId(deal.getId());
        item.setDealName(deal.getName());
        item.setAgentName(deal.getAssignedAgent().getName());
        item.setStage(deal.getCurrentStage().name());
        item.setDaysInStage(daysInStage);
        item.setEstimatedMonthlyValue(deal.getEstimatedMonthlyValue());
        item.setAlertReason(alertReason);
        return item;
    }
}
