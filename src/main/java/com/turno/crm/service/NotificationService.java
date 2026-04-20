package com.turno.crm.service;

import com.turno.crm.exception.ResourceNotFoundException;
import com.turno.crm.model.dto.NotificationResponse;
import com.turno.crm.model.entity.*;
import com.turno.crm.model.enums.*;
import com.turno.crm.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationPreferenceRepository notificationPreferenceRepository;
    private final UserRepository userRepository;

    public NotificationService(NotificationRepository notificationRepository,
                                NotificationPreferenceRepository notificationPreferenceRepository,
                                UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.notificationPreferenceRepository = notificationPreferenceRepository;
        this.userRepository = userRepository;
    }

    // --- Event firing methods ---

    public void firePricingApprovalRequested(Deal deal, User agent, String pricingDetails) {
        List<User> managers = userRepository.findByRoleAndStatus(UserRole.MANAGER, UserStatus.ACTIVE);
        for (User manager : managers) {
            if (!isNotificationEnabled(UserRole.MANAGER, NotificationEventType.PRICING_APPROVAL_REQUESTED)) {
                continue;
            }
            createNotification(
                    manager,
                    NotificationEventType.PRICING_APPROVAL_REQUESTED,
                    NotificationPriority.HIGH,
                    "Pricing approval requested for " + deal.getName(),
                    "Agent " + agent.getName() + " has submitted pricing for deal " + deal.getName() + ". " + pricingDetails,
                    deal
            );
        }
    }

    public void firePricingDecision(Deal deal, User agent, boolean approved, String pricingDetails, String rejectionNote) {
        if (!isNotificationEnabled(UserRole.AGENT, NotificationEventType.PRICING_DECISION)) {
            return;
        }
        String status = approved ? "approved" : "rejected";
        String content = "Pricing for deal " + deal.getName() + " has been " + status + ". " + pricingDetails;
        if (!approved && rejectionNote != null) {
            content += " Reason: " + rejectionNote;
        }
        createNotification(
                agent,
                NotificationEventType.PRICING_DECISION,
                NotificationPriority.HIGH,
                "Pricing " + status + " for " + deal.getName(),
                content,
                deal
        );
    }

    public void fireFollowUpDueToday(Deal deal, User agent, String nextAction) {
        if (!isNotificationEnabled(UserRole.AGENT, NotificationEventType.FOLLOW_UP_DUE_TODAY)) {
            return;
        }
        createNotification(
                agent,
                NotificationEventType.FOLLOW_UP_DUE_TODAY,
                NotificationPriority.MEDIUM,
                "Follow-up due today for " + deal.getName(),
                "Next action: " + nextAction,
                deal
        );
    }

    public void fireFollowUpOverdue(Deal deal, User agent, String nextAction, long daysOverdue) {
        if (!isNotificationEnabled(UserRole.AGENT, NotificationEventType.FOLLOW_UP_OVERDUE)) {
            return;
        }
        createNotification(
                agent,
                NotificationEventType.FOLLOW_UP_OVERDUE,
                NotificationPriority.HIGH,
                "Follow-up overdue by " + daysOverdue + " days for " + deal.getName(),
                "Next action: " + nextAction + " (overdue by " + daysOverdue + " days)",
                deal
        );
    }

    public void fireRegressionRequested(Deal deal, User agent, DealStage fromStage, DealStage toStage, String reason) {
        List<User> managers = userRepository.findByRoleAndStatus(UserRole.MANAGER, UserStatus.ACTIVE);
        for (User manager : managers) {
            if (!isNotificationEnabled(UserRole.MANAGER, NotificationEventType.REGRESSION_REQUESTED)) {
                continue;
            }
            createNotification(
                    manager,
                    NotificationEventType.REGRESSION_REQUESTED,
                    NotificationPriority.HIGH,
                    "Regression requested for " + deal.getName(),
                    "Agent " + agent.getName() + " requests regression from " + fromStage.getDisplayName()
                            + " to " + toStage.getDisplayName() + ". Reason: " + reason,
                    deal
            );
        }
    }

    public void fireRegressionDecision(Deal deal, User agent, boolean approved, DealStage fromStage, DealStage toStage) {
        if (!isNotificationEnabled(UserRole.AGENT, NotificationEventType.REGRESSION_DECISION)) {
            return;
        }
        String status = approved ? "approved" : "rejected";
        createNotification(
                agent,
                NotificationEventType.REGRESSION_DECISION,
                approved ? NotificationPriority.MEDIUM : NotificationPriority.HIGH,
                "Regression " + status + " for " + deal.getName(),
                "Regression from " + fromStage.getDisplayName() + " to " + toStage.getDisplayName() + " has been " + status + ".",
                deal
        );
    }

    public void fireStaleDealAlert(Deal deal, User agent, long daysInStage, int threshold) {
        if (!isNotificationEnabled(UserRole.AGENT, NotificationEventType.STALE_DEAL_ALERT)) {
            return;
        }
        createNotification(
                agent,
                NotificationEventType.STALE_DEAL_ALERT,
                NotificationPriority.MEDIUM,
                "Stale deal alert: " + deal.getName(),
                "Deal has been in " + deal.getCurrentStage().getDisplayName() + " for " + daysInStage
                        + " days (threshold: " + threshold + " days).",
                deal
        );
    }

    public void fireNoNextActionSet(Deal deal, User agent) {
        if (!isNotificationEnabled(UserRole.AGENT, NotificationEventType.NO_NEXT_ACTION_SET)) {
            return;
        }
        createNotification(
                agent,
                NotificationEventType.NO_NEXT_ACTION_SET,
                NotificationPriority.MEDIUM,
                "No next action set for " + deal.getName(),
                "Deal " + deal.getName() + " has no active next action. Please log an activity report.",
                deal
        );
    }

    public void fireDealArchived(Deal deal, User agent, String reason) {
        if (!isNotificationEnabled(UserRole.AGENT, NotificationEventType.DEAL_ARCHIVED)) {
            return;
        }
        createNotification(
                agent,
                NotificationEventType.DEAL_ARCHIVED,
                NotificationPriority.MEDIUM,
                "Deal archived: " + deal.getName(),
                "Deal " + deal.getName() + " has been archived. Reason: " + reason,
                deal
        );
    }

    public void fireDealReactivated(Deal deal, User agent) {
        if (!isNotificationEnabled(UserRole.AGENT, NotificationEventType.DEAL_REACTIVATED)) {
            return;
        }
        createNotification(
                agent,
                NotificationEventType.DEAL_REACTIVATED,
                NotificationPriority.MEDIUM,
                "Deal reactivated: " + deal.getName(),
                "Deal " + deal.getName() + " has been reactivated and is now active again.",
                deal
        );
    }

    public void fireDealAssignedNewOwner(Deal deal, User newAgent, User manager) {
        if (!isNotificationEnabled(UserRole.AGENT, NotificationEventType.DEAL_ASSIGNED_NEW_OWNER)) {
            return;
        }
        createNotification(
                newAgent,
                NotificationEventType.DEAL_ASSIGNED_NEW_OWNER,
                NotificationPriority.HIGH,
                "New deal assigned: " + deal.getName(),
                "Manager " + manager.getName() + " has assigned deal " + deal.getName() + " to you.",
                deal
        );
    }

    public void fireDealReassignedPreviousOwner(Deal deal, User previousAgent, User newAgent, User manager) {
        if (!isNotificationEnabled(UserRole.AGENT, NotificationEventType.DEAL_REASSIGNED_PREVIOUS_OWNER)) {
            return;
        }
        createNotification(
                previousAgent,
                NotificationEventType.DEAL_REASSIGNED_PREVIOUS_OWNER,
                NotificationPriority.MEDIUM,
                "Deal reassigned: " + deal.getName(),
                "Manager " + manager.getName() + " has reassigned deal " + deal.getName()
                        + " from you to " + newAgent.getName() + ".",
                deal
        );
    }

    // --- List / clear notifications ---

    @Transactional(readOnly = true)
    public Page<NotificationResponse> getNotifications(Long userId, boolean cleared, Pageable pageable) {
        return notificationRepository.findByUserIdAndClearedOrderByCreatedAtDesc(userId, cleared, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndClearedFalse(userId);
    }

    public void clearNotification(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", notificationId));

        if (!notification.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Notification", notificationId);
        }

        notification.setCleared(true);
        notification.setClearedAt(OffsetDateTime.now());
        notificationRepository.save(notification);
    }

    public void clearAll(Long userId) {
        List<Notification> unread = notificationRepository.findByUserIdAndClearedFalse(userId);
        OffsetDateTime now = OffsetDateTime.now();
        for (Notification n : unread) {
            n.setCleared(true);
            n.setClearedAt(now);
        }
        if (!unread.isEmpty()) {
            notificationRepository.saveAll(unread);
        }
    }

    // --- Private helpers ---

    private boolean isNotificationEnabled(UserRole role, NotificationEventType eventType) {
        return notificationPreferenceRepository.findByRoleAndEventType(role, eventType)
                .map(NotificationPreference::getEnabled)
                .orElse(true); // default enabled
    }

    private void createNotification(User recipient, NotificationEventType eventType,
                                     NotificationPriority priority, String title, String content, Deal deal) {
        Notification notification = new Notification();
        notification.setUser(recipient);
        notification.setEventType(eventType);
        notification.setPriority(priority);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setDeal(deal);
        notificationRepository.save(notification);
    }

    private NotificationResponse toResponse(Notification notification) {
        NotificationResponse resp = new NotificationResponse();
        resp.setId(notification.getId());
        resp.setEventType(notification.getEventType());
        resp.setPriority(notification.getPriority());
        resp.setTitle(notification.getTitle());
        resp.setContent(notification.getContent());
        if (notification.getDeal() != null) {
            resp.setDealId(notification.getDeal().getId());
            resp.setDealName(notification.getDeal().getName());
        }
        resp.setCleared(notification.getCleared() != null && notification.getCleared());
        resp.setClearedAt(notification.getClearedAt());
        resp.setCreatedAt(notification.getCreatedAt());
        return resp;
    }
}
