package com.turno.crm.service;

import com.turno.crm.exception.BusinessRuleViolationException;
import com.turno.crm.exception.ExitCriteriaNotMetException;
import com.turno.crm.exception.ExitCriteriaNotMetException.CriteriaResult;
import com.turno.crm.exception.ResourceNotFoundException;
import com.turno.crm.model.dto.DealResponse;
import com.turno.crm.model.dto.StageTransitionResponse;
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
class StageTransitionServiceTest {

    @Mock private DealRepository dealRepository;
    @Mock private StageTransitionRepository stageTransitionRepository;
    @Mock private RegressionRequestRepository regressionRequestRepository;
    @Mock private UserRepository userRepository;
    @Mock private ExitCriteriaValidator exitCriteriaValidator;
    @Mock private AuditService auditService;
    @Mock private DealService dealService;
    @Mock private NotificationService notificationService;

    @InjectMocks
    private StageTransitionService service;

    private Deal deal;
    private User agent;
    private DealResponse dealResponse;

    @BeforeEach
    void setUp() {
        deal = new Deal();
        deal.setId(1L);
        deal.setStatus(DealStatus.ACTIVE);
        deal.setCurrentStage(DealStage.STAGE_1);

        Operator operator = new Operator();
        operator.setId(1L);
        operator.setCompanyName("Test Op");
        deal.setOperator(operator);

        agent = new User();
        agent.setId(100L);
        agent.setName("Agent A");
        deal.setAssignedAgent(agent);

        dealResponse = new DealResponse();
        dealResponse.setId(1L);
    }

    private void setupForForward(DealStage stage) {
        deal.setCurrentStage(stage);
        if (stage == DealStage.STAGE_4) {
            deal.setSubStatus(Stage5SubStatus.NEGOTIATING);
        }
        when(dealRepository.findById(1L)).thenReturn(Optional.of(deal));
        when(exitCriteriaValidator.validateForward(deal))
                .thenReturn(List.of(new CriteriaResult("rule", true, false, "ok")));
        when(dealRepository.save(any(Deal.class))).thenAnswer(inv -> inv.getArgument(0));
        when(stageTransitionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(dealService.getById(eq(1L), anyLong(), any())).thenReturn(dealResponse);
    }

    // === moveForward tests ===

    @Test
    void moveForward_stage1To2_succeeds() {
        setupForForward(DealStage.STAGE_1);

        StageTransitionResponse resp = service.moveForward(1L, null, 100L);

        assertEquals("STAGE_1", resp.getFromStage());
        assertEquals("STAGE_2", resp.getToStage());
        assertEquals("FORWARD", resp.getTransitionType());
    }

    @Test
    void moveForward_stage2To3_succeeds() {
        setupForForward(DealStage.STAGE_2);

        StageTransitionResponse resp = service.moveForward(1L, null, 100L);

        assertEquals("STAGE_2", resp.getFromStage());
        assertEquals("STAGE_3", resp.getToStage());
    }

    @Test
    void moveForward_stage3To4_setsSubStatusProposalSent() {
        setupForForward(DealStage.STAGE_3);

        service.moveForward(1L, null, 100L);

        ArgumentCaptor<Deal> captor = ArgumentCaptor.forClass(Deal.class);
        verify(dealRepository).save(captor.capture());
        assertEquals(DealStage.STAGE_4, captor.getValue().getCurrentStage());
        assertEquals(Stage5SubStatus.PROPOSAL_SENT, captor.getValue().getSubStatus());
    }

    @Test
    void moveForward_stage4Exit_clearsSubStatus() {
        setupForForward(DealStage.STAGE_4);

        service.moveForward(1L, null, 100L);

        ArgumentCaptor<Deal> captor = ArgumentCaptor.forClass(Deal.class);
        verify(dealRepository).save(captor.capture());
        assertNull(captor.getValue().getSubStatus());
    }

    @Test
    void moveForward_stage8_completesTheDeal() {
        deal.setCurrentStage(DealStage.STAGE_8);
        when(dealRepository.findById(1L)).thenReturn(Optional.of(deal));
        when(exitCriteriaValidator.validateForward(deal))
                .thenReturn(List.of(new CriteriaResult("rule", true, false, "ok")));
        when(dealRepository.save(any(Deal.class))).thenAnswer(inv -> inv.getArgument(0));
        when(stageTransitionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(dealService.getById(eq(1L), anyLong(), any())).thenReturn(dealResponse);

        StageTransitionResponse resp = service.moveForward(1L, null, 100L);

        assertEquals("COMPLETED", resp.getToStage());
        assertEquals("COMPLETE", resp.getTransitionType());
        assertEquals(DealStatus.COMPLETED, deal.getStatus());
    }

    @Test
    void moveForward_softBlock_withOverrideReason_succeeds() {
        deal.setCurrentStage(DealStage.STAGE_6);
        when(dealRepository.findById(1L)).thenReturn(Optional.of(deal));
        when(exitCriteriaValidator.validateForward(deal))
                .thenReturn(List.of(new CriteriaResult("all_mandatory_docs_received", false, true, "Not all docs")));
        when(dealRepository.save(any(Deal.class))).thenAnswer(inv -> inv.getArgument(0));
        when(stageTransitionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(dealService.getById(eq(1L), anyLong(), any())).thenReturn(dealResponse);

        StageTransitionResponse resp = service.moveForward(1L, "Manager override", 100L);

        assertEquals("FORWARD", resp.getTransitionType());
    }

    @Test
    void moveForward_softBlock_withoutOverrideReason_throws() {
        deal.setCurrentStage(DealStage.STAGE_6);
        when(dealRepository.findById(1L)).thenReturn(Optional.of(deal));
        when(exitCriteriaValidator.validateForward(deal))
                .thenReturn(List.of(new CriteriaResult("all_mandatory_docs_received", false, true, "Not all docs")));

        assertThrows(ExitCriteriaNotMetException.class, () ->
                service.moveForward(1L, null, 100L));
    }

    @Test
    void moveForward_hardBlock_cannotBeOverridden() {
        deal.setCurrentStage(DealStage.STAGE_7);
        when(dealRepository.findById(1L)).thenReturn(Optional.of(deal));
        when(exitCriteriaValidator.validateForward(deal))
                .thenReturn(List.of(new CriteriaResult("all_mandatory_docs_received", false, false, "Hard fail")));

        assertThrows(ExitCriteriaNotMetException.class, () ->
                service.moveForward(1L, "Override attempt", 100L));
    }

    @Test
    void moveForward_dealNotActive_throws() {
        deal.setStatus(DealStatus.ARCHIVED);
        when(dealRepository.findById(1L)).thenReturn(Optional.of(deal));

        assertThrows(BusinessRuleViolationException.class, () ->
                service.moveForward(1L, null, 100L));
    }

    @Test
    void moveForward_dealNotFound_throws() {
        when(dealRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                service.moveForward(999L, null, 100L));
    }

    // === requestBackward tests ===

    @Test
    void requestBackward_agentCreatesRegressionRequest() {
        deal.setCurrentStage(DealStage.STAGE_3);
        when(dealRepository.findById(1L)).thenReturn(Optional.of(deal));
        when(regressionRequestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(userRepository.findById(100L)).thenReturn(Optional.of(agent));
        when(dealService.getById(eq(1L), anyLong(), any())).thenReturn(dealResponse);

        StageTransitionResponse resp = service.requestBackward(1L, "Need re-assessment", 100L, UserRole.AGENT);

        assertEquals("REGRESSION_REQUESTED", resp.getTransitionType());
        verify(regressionRequestRepository).save(any(RegressionRequest.class));
        verify(notificationService).fireRegressionRequested(eq(deal), eq(agent), eq(DealStage.STAGE_3), eq(DealStage.STAGE_2), anyString());
    }

    @Test
    void requestBackward_managerMovesImmediately() {
        deal.setCurrentStage(DealStage.STAGE_3);
        when(dealRepository.findById(1L)).thenReturn(Optional.of(deal));
        when(dealRepository.save(any(Deal.class))).thenAnswer(inv -> inv.getArgument(0));
        when(stageTransitionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(dealService.getById(eq(1L), anyLong(), any())).thenReturn(dealResponse);

        StageTransitionResponse resp = service.requestBackward(1L, "Manager regression", 100L, UserRole.MANAGER);

        assertEquals("BACKWARD", resp.getTransitionType());
        assertEquals("STAGE_3", resp.getFromStage());
        assertEquals("STAGE_2", resp.getToStage());
    }

    @Test
    void requestBackward_cannotGoBelowStage1() {
        deal.setCurrentStage(DealStage.STAGE_1);
        when(dealRepository.findById(1L)).thenReturn(Optional.of(deal));

        assertThrows(BusinessRuleViolationException.class, () ->
                service.requestBackward(1L, "reason", 100L, UserRole.MANAGER));
    }

    @Test
    void requestBackward_dealMustBeActive() {
        deal.setStatus(DealStatus.COMPLETED);
        deal.setCurrentStage(DealStage.STAGE_8);
        when(dealRepository.findById(1L)).thenReturn(Optional.of(deal));

        assertThrows(BusinessRuleViolationException.class, () ->
                service.requestBackward(1L, "reason", 100L, UserRole.MANAGER));
    }

    @Test
    void requestBackward_stage4Exit_clearsSubStatus() {
        deal.setCurrentStage(DealStage.STAGE_4);
        deal.setSubStatus(Stage5SubStatus.NEGOTIATING);
        when(dealRepository.findById(1L)).thenReturn(Optional.of(deal));
        when(dealRepository.save(any(Deal.class))).thenAnswer(inv -> inv.getArgument(0));
        when(stageTransitionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(dealService.getById(eq(1L), anyLong(), any())).thenReturn(dealResponse);

        service.requestBackward(1L, "reason", 100L, UserRole.MANAGER);

        ArgumentCaptor<Deal> captor = ArgumentCaptor.forClass(Deal.class);
        verify(dealRepository).save(captor.capture());
        assertNull(captor.getValue().getSubStatus());
    }

    @Test
    void requestBackward_stage4ReEntry_resetsSubStatus() {
        deal.setCurrentStage(DealStage.STAGE_5);
        when(dealRepository.findById(1L)).thenReturn(Optional.of(deal));
        when(dealRepository.save(any(Deal.class))).thenAnswer(inv -> inv.getArgument(0));
        when(stageTransitionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(dealService.getById(eq(1L), anyLong(), any())).thenReturn(dealResponse);

        service.requestBackward(1L, "reason", 100L, UserRole.MANAGER);

        ArgumentCaptor<Deal> captor = ArgumentCaptor.forClass(Deal.class);
        verify(dealRepository).save(captor.capture());
        assertEquals(Stage5SubStatus.PROPOSAL_SENT, captor.getValue().getSubStatus());
    }

    // === approveRegression / rejectRegression ===

    @Test
    void approveRegression_movesToTargetStage() {
        RegressionRequest request = new RegressionRequest();
        request.setId(1L);
        request.setDeal(deal);
        request.setFromStage(DealStage.STAGE_3);
        request.setToStage(DealStage.STAGE_2);
        request.setReason("reason");
        request.setStatus(ApprovalStatus.PENDING);
        request.setRequestedBy(agent);

        deal.setCurrentStage(DealStage.STAGE_3);
        when(regressionRequestRepository.findById(1L)).thenReturn(Optional.of(request));
        when(regressionRequestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(dealRepository.save(any(Deal.class))).thenAnswer(inv -> inv.getArgument(0));
        when(stageTransitionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(dealService.getById(eq(1L), anyLong(), any())).thenReturn(dealResponse);

        StageTransitionResponse resp = service.approveRegression(1L, 200L);

        assertEquals("BACKWARD", resp.getTransitionType());
        verify(notificationService).fireRegressionDecision(eq(deal), eq(agent), eq(true), any(), any());
    }

    @Test
    void approveRegression_notPending_throws() {
        RegressionRequest request = new RegressionRequest();
        request.setId(1L);
        request.setStatus(ApprovalStatus.APPROVED);
        when(regressionRequestRepository.findById(1L)).thenReturn(Optional.of(request));

        assertThrows(BusinessRuleViolationException.class, () ->
                service.approveRegression(1L, 200L));
    }

    @Test
    void rejectRegression_setsRejectedStatus() {
        RegressionRequest request = new RegressionRequest();
        request.setId(1L);
        request.setDeal(deal);
        request.setFromStage(DealStage.STAGE_3);
        request.setToStage(DealStage.STAGE_2);
        request.setStatus(ApprovalStatus.PENDING);
        request.setRequestedBy(agent);

        when(regressionRequestRepository.findById(1L)).thenReturn(Optional.of(request));
        when(regressionRequestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.rejectRegression(1L, 200L);

        assertEquals(ApprovalStatus.REJECTED, request.getStatus());
        verify(notificationService).fireRegressionDecision(eq(deal), eq(agent), eq(false), any(), any());
    }

    @Test
    void rejectRegression_notPending_throws() {
        RegressionRequest request = new RegressionRequest();
        request.setId(1L);
        request.setStatus(ApprovalStatus.REJECTED);
        when(regressionRequestRepository.findById(1L)).thenReturn(Optional.of(request));

        assertThrows(BusinessRuleViolationException.class, () ->
                service.rejectRegression(1L, 200L));
    }

    @Test
    void moveForward_stage3To4_succeeds() {
        setupForForward(DealStage.STAGE_3);

        StageTransitionResponse resp = service.moveForward(1L, null, 100L);

        assertEquals("STAGE_3", resp.getFromStage());
        assertEquals("STAGE_4", resp.getToStage());
    }

    @Test
    void moveForward_stage4To5_succeeds() {
        setupForForward(DealStage.STAGE_4);

        StageTransitionResponse resp = service.moveForward(1L, null, 100L);

        assertEquals("STAGE_4", resp.getFromStage());
        assertEquals("STAGE_5", resp.getToStage());
    }

    @Test
    void moveForward_stage5To6_succeeds() {
        setupForForward(DealStage.STAGE_5);

        StageTransitionResponse resp = service.moveForward(1L, null, 100L);

        assertEquals("STAGE_5", resp.getFromStage());
        assertEquals("STAGE_6", resp.getToStage());
    }

    @Test
    void moveForward_stage6To7_succeeds() {
        setupForForward(DealStage.STAGE_6);

        StageTransitionResponse resp = service.moveForward(1L, null, 100L);

        assertEquals("STAGE_6", resp.getFromStage());
        assertEquals("STAGE_7", resp.getToStage());
    }

    @Test
    void moveForward_stage7To8_succeeds() {
        setupForForward(DealStage.STAGE_7);

        StageTransitionResponse resp = service.moveForward(1L, null, 100L);

        assertEquals("STAGE_7", resp.getFromStage());
        assertEquals("STAGE_8", resp.getToStage());
    }
}
