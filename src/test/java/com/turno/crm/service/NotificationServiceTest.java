package com.turno.crm.service;

import com.turno.crm.exception.ResourceNotFoundException;
import com.turno.crm.model.entity.*;
import com.turno.crm.model.enums.*;
import com.turno.crm.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock private NotificationRepository notificationRepository;
    @Mock private NotificationPreferenceRepository notificationPreferenceRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private NotificationService service;

    private Deal deal;
    private User agent;
    private User manager;

    @BeforeEach
    void setUp() {
        deal = new Deal();
        deal.setId(1L);
        deal.setName("Test Deal");

        agent = new User();
        agent.setId(100L);
        agent.setName("Agent A");
        deal.setAssignedAgent(agent);

        manager = new User();
        manager.setId(200L);
        manager.setName("Manager M");
    }

    private void enableNotification() {
        // Default behavior: no preference found => defaults to enabled
        lenient().when(notificationPreferenceRepository.findByRoleAndEventType(any(), any()))
                .thenReturn(Optional.empty());
    }

    private void disableNotification(UserRole role, NotificationEventType eventType) {
        NotificationPreference pref = new NotificationPreference();
        pref.setEnabled(false);
        when(notificationPreferenceRepository.findByRoleAndEventType(role, eventType))
                .thenReturn(Optional.of(pref));
    }

    // === Event firing tests ===

    @Test
    void firePricingApprovalRequested_notifiesManagers() {
        enableNotification();
        when(userRepository.findByRoleAndStatus(UserRole.MANAGER, UserStatus.ACTIVE))
                .thenReturn(List.of(manager));
        when(notificationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.firePricingApprovalRequested(deal, agent, "Price/km: 12.50");

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        assertEquals(NotificationEventType.PRICING_APPROVAL_REQUESTED, captor.getValue().getEventType());
        assertEquals(NotificationPriority.HIGH, captor.getValue().getPriority());
        assertEquals(manager, captor.getValue().getUser());
    }

    @Test
    void firePricingDecision_approved_notifiesAgent() {
        enableNotification();
        when(notificationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.firePricingDecision(deal, agent, true, "details", null);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        assertEquals(NotificationEventType.PRICING_DECISION, captor.getValue().getEventType());
        assertEquals(NotificationPriority.HIGH, captor.getValue().getPriority());
        assertTrue(captor.getValue().getTitle().contains("approved"));
    }

    @Test
    void firePricingDecision_rejected_includesRejectionNote() {
        enableNotification();
        when(notificationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.firePricingDecision(deal, agent, false, "details", "Price too high");

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        assertTrue(captor.getValue().getContent().contains("Price too high"));
    }

    @Test
    void fireFollowUpDueToday_notifiesAgent() {
        enableNotification();
        when(notificationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.fireFollowUpDueToday(deal, agent, "Call operator");

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        assertEquals(NotificationEventType.FOLLOW_UP_DUE_TODAY, captor.getValue().getEventType());
        assertEquals(NotificationPriority.MEDIUM, captor.getValue().getPriority());
    }

    @Test
    void fireFollowUpOverdue_notifiesAgent() {
        enableNotification();
        when(notificationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.fireFollowUpOverdue(deal, agent, "Call", 3);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        assertEquals(NotificationEventType.FOLLOW_UP_OVERDUE, captor.getValue().getEventType());
        assertEquals(NotificationPriority.HIGH, captor.getValue().getPriority());
    }

    @Test
    void fireRegressionRequested_notifiesManagers() {
        enableNotification();
        when(userRepository.findByRoleAndStatus(UserRole.MANAGER, UserStatus.ACTIVE))
                .thenReturn(List.of(manager));
        when(notificationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.fireRegressionRequested(deal, agent, DealStage.STAGE_3, DealStage.STAGE_2, "reason");

        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void fireRegressionDecision_approved_mediumPriority() {
        enableNotification();
        when(notificationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.fireRegressionDecision(deal, agent, true, DealStage.STAGE_3, DealStage.STAGE_2);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        assertEquals(NotificationPriority.MEDIUM, captor.getValue().getPriority());
    }

    @Test
    void fireRegressionDecision_rejected_highPriority() {
        enableNotification();
        when(notificationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.fireRegressionDecision(deal, agent, false, DealStage.STAGE_3, DealStage.STAGE_2);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        assertEquals(NotificationPriority.HIGH, captor.getValue().getPriority());
    }

    @Test
    void fireStaleDealAlert_notifiesAgent() {
        enableNotification();
        when(notificationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.fireStaleDealAlert(deal, agent, 20, 14);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        assertEquals(NotificationEventType.STALE_DEAL_ALERT, captor.getValue().getEventType());
        assertEquals(NotificationPriority.MEDIUM, captor.getValue().getPriority());
    }

    @Test
    void fireNoNextActionSet_notifiesAgent() {
        enableNotification();
        when(notificationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.fireNoNextActionSet(deal, agent);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        assertEquals(NotificationEventType.NO_NEXT_ACTION_SET, captor.getValue().getEventType());
    }

    @Test
    void fireDealArchived_notifiesAgent() {
        enableNotification();
        when(notificationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.fireDealArchived(deal, agent, "Lost to competitor");

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        assertEquals(NotificationEventType.DEAL_ARCHIVED, captor.getValue().getEventType());
    }

    @Test
    void fireDealReactivated_notifiesAgent() {
        enableNotification();
        when(notificationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.fireDealReactivated(deal, agent);

        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void fireDealAssignedNewOwner_notifiesNewAgent_highPriority() {
        enableNotification();
        when(notificationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        User newAgent = new User();
        newAgent.setId(300L);
        newAgent.setName("New Agent");

        service.fireDealAssignedNewOwner(deal, newAgent, manager);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        assertEquals(newAgent, captor.getValue().getUser());
        assertEquals(NotificationPriority.HIGH, captor.getValue().getPriority());
    }

    @Test
    void fireDealReassignedPreviousOwner_notifiesPreviousAgent_mediumPriority() {
        enableNotification();
        when(notificationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        User newAgent = new User();
        newAgent.setId(300L);
        newAgent.setName("New Agent");

        service.fireDealReassignedPreviousOwner(deal, agent, newAgent, manager);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        assertEquals(agent, captor.getValue().getUser());
        assertEquals(NotificationPriority.MEDIUM, captor.getValue().getPriority());
    }

    // === Preference disabled ===

    @Test
    void preferenceDisabled_noNotificationCreated() {
        disableNotification(UserRole.AGENT, NotificationEventType.FOLLOW_UP_DUE_TODAY);

        service.fireFollowUpDueToday(deal, agent, "Call");

        verify(notificationRepository, never()).save(any());
    }

    @Test
    void preferenceDisabledForPricingDecision_noNotification() {
        disableNotification(UserRole.AGENT, NotificationEventType.PRICING_DECISION);

        service.firePricingDecision(deal, agent, true, "details", null);

        verify(notificationRepository, never()).save(any());
    }

    // === Clear / getUnreadCount ===

    @Test
    void clearNotification_setsClearedTrue() {
        Notification notification = new Notification();
        notification.setId(1L);
        notification.setUser(agent);
        notification.setCleared(false);

        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));
        when(notificationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.clearNotification(1L, 100L);

        assertTrue(notification.getCleared());
        assertNotNull(notification.getClearedAt());
    }

    @Test
    void clearNotification_wrongUser_throws() {
        Notification notification = new Notification();
        notification.setId(1L);
        notification.setUser(manager); // belongs to manager, not agent

        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));

        assertThrows(ResourceNotFoundException.class, () ->
                service.clearNotification(1L, 100L));
    }

    @Test
    void clearAll_clearsAllUnread() {
        Notification n1 = new Notification();
        n1.setCleared(false);
        Notification n2 = new Notification();
        n2.setCleared(false);

        when(notificationRepository.findByUserIdAndClearedFalse(100L)).thenReturn(List.of(n1, n2));
        when(notificationRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        service.clearAll(100L);

        assertTrue(n1.getCleared());
        assertTrue(n2.getCleared());
    }

    @Test
    void getUnreadCount_returnsCount() {
        when(notificationRepository.countByUserIdAndClearedFalse(100L)).thenReturn(5L);

        long count = service.getUnreadCount(100L);

        assertEquals(5L, count);
    }
}
