package com.turno.crm.service;

import com.turno.crm.exception.BusinessRuleViolationException;
import com.turno.crm.exception.ResourceNotFoundException;
import com.turno.crm.model.dto.BackfillRequestDto;
import com.turno.crm.model.dto.BackfillRequestResponse;
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

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BackfillServiceTest {

    @Mock private BackfillRequestRepository backfillRequestRepository;
    @Mock private DealRepository dealRepository;
    @Mock private UserRepository userRepository;
    @Mock private AuditService auditService;

    @InjectMocks
    private BackfillService service;

    private Deal deal;
    private User requester;
    private User reviewer;

    @BeforeEach
    void setUp() {
        deal = new Deal();
        deal.setId(1L);
        deal.setName("Test Deal");
        deal.setCurrentStage(DealStage.STAGE_1);
        deal.setStatus(DealStatus.ACTIVE);

        Operator operator = new Operator();
        operator.setId(1L);
        operator.setCompanyName("Test Op");
        deal.setOperator(operator);

        requester = new User();
        requester.setId(100L);
        requester.setName("Agent A");
        deal.setAssignedAgent(requester);

        reviewer = new User();
        reviewer.setId(200L);
        reviewer.setName("Manager M");
    }

    private BackfillRequestDto createValidRequest(String targetStage, LocalDate originalDate) {
        BackfillRequestDto dto = new BackfillRequestDto();
        dto.setTargetStage(targetStage);
        dto.setContext("Existing relationship");
        dto.setOriginalStartDate(originalDate);
        return dto;
    }

    // === Request tests ===

    @Test
    void requestBackfill_validWithin30DayWindow_succeeds() {
        when(dealRepository.findById(1L)).thenReturn(Optional.of(deal));
        when(userRepository.findById(100L)).thenReturn(Optional.of(requester));
        when(backfillRequestRepository.save(any())).thenAnswer(inv -> {
            BackfillRequest r = inv.getArgument(0);
            r.setId(1L);
            return r;
        });

        BackfillRequestDto dto = createValidRequest("STAGE_3", LocalDate.now().minusDays(10));

        BackfillRequestResponse resp = service.requestBackfill(1L, dto, 100L);

        assertNotNull(resp);
        assertEquals(ApprovalStatus.PENDING, resp.getStatus());
    }

    @Test
    void requestBackfill_within30DayWindow_onDay30_succeeds() {
        when(dealRepository.findById(1L)).thenReturn(Optional.of(deal));
        when(userRepository.findById(100L)).thenReturn(Optional.of(requester));
        when(backfillRequestRepository.save(any())).thenAnswer(inv -> {
            BackfillRequest r = inv.getArgument(0);
            r.setId(1L);
            return r;
        });

        BackfillRequestDto dto = createValidRequest("STAGE_3", LocalDate.now().minusDays(30));

        BackfillRequestResponse resp = service.requestBackfill(1L, dto, 100L);

        assertNotNull(resp);
    }

    @Test
    void requestBackfill_windowExpired_throws() {
        when(dealRepository.findById(1L)).thenReturn(Optional.of(deal));
        when(userRepository.findById(100L)).thenReturn(Optional.of(requester));

        BackfillRequestDto dto = createValidRequest("STAGE_3", LocalDate.now().minusDays(31));

        assertThrows(BusinessRuleViolationException.class, () ->
                service.requestBackfill(1L, dto, 100L));
    }

    @Test
    void requestBackfill_dealNotFound_throws() {
        when(dealRepository.findById(999L)).thenReturn(Optional.empty());

        BackfillRequestDto dto = createValidRequest("STAGE_3", LocalDate.now().minusDays(10));

        assertThrows(ResourceNotFoundException.class, () ->
                service.requestBackfill(999L, dto, 100L));
    }

    // === Approve tests ===

    @Test
    void approveBackfill_movesToTargetStage_setsBackfilledTrue() {
        BackfillRequest backfill = new BackfillRequest();
        backfill.setId(1L);
        backfill.setDeal(deal);
        backfill.setRequestedBy(requester);
        backfill.setTargetStage(DealStage.STAGE_3);
        backfill.setOriginalStartDate(LocalDate.now().minusDays(10));
        backfill.setStatus(ApprovalStatus.PENDING);

        when(backfillRequestRepository.findById(1L)).thenReturn(Optional.of(backfill));
        when(userRepository.findById(200L)).thenReturn(Optional.of(reviewer));
        when(backfillRequestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(dealRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.approveBackfill(1L, 200L);

        assertEquals(DealStage.STAGE_3, deal.getCurrentStage());
        assertTrue(deal.getBackfilled());
        assertEquals(ApprovalStatus.APPROVED, backfill.getStatus());
    }

    @Test
    void approveBackfill_stage4Target_setsSubStatusProposalSent() {
        BackfillRequest backfill = new BackfillRequest();
        backfill.setId(1L);
        backfill.setDeal(deal);
        backfill.setRequestedBy(requester);
        backfill.setTargetStage(DealStage.STAGE_4);
        backfill.setOriginalStartDate(LocalDate.now().minusDays(10));
        backfill.setStatus(ApprovalStatus.PENDING);

        when(backfillRequestRepository.findById(1L)).thenReturn(Optional.of(backfill));
        when(userRepository.findById(200L)).thenReturn(Optional.of(reviewer));
        when(backfillRequestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(dealRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.approveBackfill(1L, 200L);

        assertEquals(DealStage.STAGE_4, deal.getCurrentStage());
        assertEquals(Stage5SubStatus.PROPOSAL_SENT, deal.getSubStatus());
    }

    @Test
    void approveBackfill_notPending_throws() {
        BackfillRequest backfill = new BackfillRequest();
        backfill.setId(1L);
        backfill.setStatus(ApprovalStatus.APPROVED);

        when(backfillRequestRepository.findById(1L)).thenReturn(Optional.of(backfill));

        assertThrows(BusinessRuleViolationException.class, () ->
                service.approveBackfill(1L, 200L));
    }

    // === Reject tests ===

    @Test
    void rejectBackfill_setsRejected() {
        BackfillRequest backfill = new BackfillRequest();
        backfill.setId(1L);
        backfill.setDeal(deal);
        backfill.setRequestedBy(requester);
        backfill.setStatus(ApprovalStatus.PENDING);

        when(backfillRequestRepository.findById(1L)).thenReturn(Optional.of(backfill));
        when(userRepository.findById(200L)).thenReturn(Optional.of(reviewer));
        when(backfillRequestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.rejectBackfill(1L, 200L);

        assertEquals(ApprovalStatus.REJECTED, backfill.getStatus());
    }

    @Test
    void rejectBackfill_notPending_throws() {
        BackfillRequest backfill = new BackfillRequest();
        backfill.setId(1L);
        backfill.setStatus(ApprovalStatus.REJECTED);

        when(backfillRequestRepository.findById(1L)).thenReturn(Optional.of(backfill));

        assertThrows(BusinessRuleViolationException.class, () ->
                service.rejectBackfill(1L, 200L));
    }

    // === Backfilled flag ===

    @Test
    void approveBackfill_setsBackfillApprovedBy() {
        BackfillRequest backfill = new BackfillRequest();
        backfill.setId(1L);
        backfill.setDeal(deal);
        backfill.setRequestedBy(requester);
        backfill.setTargetStage(DealStage.STAGE_3);
        backfill.setOriginalStartDate(LocalDate.now().minusDays(10));
        backfill.setStatus(ApprovalStatus.PENDING);

        when(backfillRequestRepository.findById(1L)).thenReturn(Optional.of(backfill));
        when(userRepository.findById(200L)).thenReturn(Optional.of(reviewer));
        when(backfillRequestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(dealRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.approveBackfill(1L, 200L);

        assertEquals(reviewer, deal.getBackfillApprovedBy());
        assertNotNull(deal.getOriginalStartDate());
    }

    @Test
    void approveBackfill_movingFromStage4_clearsSubStatus() {
        deal.setCurrentStage(DealStage.STAGE_4);
        deal.setSubStatus(Stage5SubStatus.NEGOTIATING);

        BackfillRequest backfill = new BackfillRequest();
        backfill.setId(1L);
        backfill.setDeal(deal);
        backfill.setRequestedBy(requester);
        backfill.setTargetStage(DealStage.STAGE_3);
        backfill.setOriginalStartDate(LocalDate.now().minusDays(10));
        backfill.setStatus(ApprovalStatus.PENDING);

        when(backfillRequestRepository.findById(1L)).thenReturn(Optional.of(backfill));
        when(userRepository.findById(200L)).thenReturn(Optional.of(reviewer));
        when(backfillRequestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(dealRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.approveBackfill(1L, 200L);

        assertNull(deal.getSubStatus());
    }
}
