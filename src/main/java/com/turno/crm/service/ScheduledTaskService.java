package com.turno.crm.service;

import com.turno.crm.model.entity.*;
import com.turno.crm.model.enums.*;
import com.turno.crm.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@Service
public class ScheduledTaskService {

    private static final Logger log = LoggerFactory.getLogger(ScheduledTaskService.class);

    // Default stale deal thresholds per stage (days)
    private static final Map<DealStage, Integer> STALE_THRESHOLDS = Map.of(
            DealStage.STAGE_1, 7,
            DealStage.STAGE_2, 14,
            DealStage.STAGE_3, 21,
            DealStage.STAGE_4, 30,
            DealStage.STAGE_5, 14,
            DealStage.STAGE_6, 21,
            DealStage.STAGE_7, 14,
            DealStage.STAGE_8, 30
    );

    private final DealRepository dealRepository;
    private final ActivityReportRepository activityReportRepository;
    private final StageTransitionRepository stageTransitionRepository;
    private final NotificationService notificationService;
    private final AdminSettingRepository adminSettingRepository;

    public ScheduledTaskService(DealRepository dealRepository,
                                 ActivityReportRepository activityReportRepository,
                                 StageTransitionRepository stageTransitionRepository,
                                 NotificationService notificationService,
                                 AdminSettingRepository adminSettingRepository) {
        this.dealRepository = dealRepository;
        this.activityReportRepository = activityReportRepository;
        this.stageTransitionRepository = stageTransitionRepository;
        this.notificationService = notificationService;
        this.adminSettingRepository = adminSettingRepository;
    }

    @Scheduled(cron = "0 0 8 * * *", zone = "Asia/Kolkata")
    @Transactional
    public void dailyNotificationScan() {
        log.info("Starting daily notification scan");

        List<Deal> activeDeals = dealRepository.findByAssignedAgentIdAndStatus(null, DealStatus.ACTIVE);
        // The above won't work with null agentId, so use specification
        List<Deal> allActiveDeals = dealRepository.findAll(
                (root, query, cb) -> cb.equal(root.get("status"), DealStatus.ACTIVE)
        );

        LocalDate today = LocalDate.now();

        for (Deal deal : allActiveDeals) {
            User agent = deal.getAssignedAgent();

            // Get latest non-voided report
            activityReportRepository.findFirstByDealIdAndVoidedFalseOrderBySubmissionDatetimeDesc(deal.getId())
                    .ifPresent(report -> {
                        LocalDate nextActionEta = report.getNextActionEta();
                        if (nextActionEta != null) {
                            // Follow-up due today
                            if (nextActionEta.equals(today)) {
                                notificationService.fireFollowUpDueToday(deal, agent, report.getNextAction());
                            }

                            // Follow-up overdue 3+ days
                            long daysOverdue = ChronoUnit.DAYS.between(nextActionEta, today);
                            if (daysOverdue >= 3) {
                                notificationService.fireFollowUpOverdue(deal, agent, report.getNextAction(), daysOverdue);
                            }
                        }
                    });

            // Stale deal alerts
            long daysInStage = calculateDaysInStage(deal);
            int threshold = getStaleThreshold(deal.getCurrentStage());
            if (daysInStage > threshold) {
                notificationService.fireStaleDealAlert(deal, agent, daysInStage, threshold);
            }
        }

        log.info("Daily notification scan completed for {} active deals", allActiveDeals.size());
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

    private int getStaleThreshold(DealStage stage) {
        // Try to load from admin settings first
        try {
            return adminSettingRepository
                    .findBySettingTypeAndSettingKey(AdminSettingType.STALE_THRESHOLD, stage.name())
                    .map(setting -> {
                        Object val = setting.getSettingValue().get("days");
                        return val != null ? Integer.parseInt(val.toString()) : STALE_THRESHOLDS.getOrDefault(stage, 21);
                    })
                    .orElse(STALE_THRESHOLDS.getOrDefault(stage, 21));
        } catch (Exception e) {
            return STALE_THRESHOLDS.getOrDefault(stage, 21);
        }
    }
}
