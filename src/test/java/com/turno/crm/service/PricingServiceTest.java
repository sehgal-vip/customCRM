package com.turno.crm.service;

import com.turno.crm.exception.BusinessRuleViolationException;
import com.turno.crm.exception.ResourceNotFoundException;
import com.turno.crm.model.dto.PricingApproveRequest;
import com.turno.crm.model.dto.PricingRejectRequest;
import com.turno.crm.model.dto.PricingSubmissionResponse;
import com.turno.crm.model.dto.PricingSubmitRequest;
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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PricingServiceTest {

    @Mock private PricingSubmissionRepository pricingSubmissionRepository;
    @Mock private DealRepository dealRepository;
    @Mock private UserRepository userRepository;
    @Mock private AuditService auditService;
    @Mock private NotificationService notificationService;

    @InjectMocks
    private PricingService service;

    private Deal deal;
    private User agent;
    private User manager;

    @BeforeEach
    void setUp() {
        deal = new Deal();
        deal.setId(1L);
        deal.setName("Test Deal");
        deal.setCurrentStage(DealStage.STAGE_4);
        deal.setSubStatus(Stage5SubStatus.PROPOSAL_SENT);
        deal.setStatus(DealStatus.ACTIVE);
        deal.setFleetSize(5);

        agent = new User();
        agent.setId(100L);
        agent.setName("Agent A");
        deal.setAssignedAgent(agent);

        manager = new User();
        manager.setId(200L);
        manager.setName("Manager M");
    }

    private PricingSubmitRequest createValidSubmitRequest() {
        PricingSubmitRequest req = new PricingSubmitRequest();
        req.setServicesSelected(List.of("BUS_LEASE", "MAINTENANCE"));
        req.setMonthlyKmCommitment(new BigDecimal("5000"));
        req.setPricePerKm(new BigDecimal("12.50"));
        return req;
    }

    private PricingSubmission createSubmission() {
        PricingSubmission s = new PricingSubmission();
        s.setId(1L);
        s.setDeal(deal);
        s.setSubmittedBy(agent);
        s.setServicesSelected(List.of("BUS_LEASE"));
        s.setMonthlyKmCommitment(new BigDecimal("5000"));
        s.setPricePerKm(new BigDecimal("12.50"));
        s.setStatus(PricingStatus.SUBMITTED);
        return s;
    }

    // === Submit tests ===

    @Test
    void submit_validRequest_succeeds() {
        when(dealRepository.findById(1L)).thenReturn(Optional.of(deal));
        when(pricingSubmissionRepository.findByDealIdOrderByCreatedAtDesc(1L)).thenReturn(new ArrayList<>());
        when(pricingSubmissionRepository.save(any())).thenAnswer(inv -> {
            PricingSubmission s = inv.getArgument(0);
            s.setId(1L);
            return s;
        });
        when(userRepository.findById(100L)).thenReturn(Optional.of(agent));

        PricingSubmissionResponse resp = service.submit(1L, createValidSubmitRequest(), 100L);

        assertNotNull(resp);
        assertEquals(PricingStatus.SUBMITTED, resp.getStatus());
    }

    @Test
    void submit_validatesStage4() {
        deal.setCurrentStage(DealStage.STAGE_2);
        when(dealRepository.findById(1L)).thenReturn(Optional.of(deal));

        assertThrows(BusinessRuleViolationException.class, () ->
                service.submit(1L, createValidSubmitRequest(), 100L));
    }

    @Test
    void submit_rejectsIfNotStage4() {
        deal.setCurrentStage(DealStage.STAGE_6);
        when(dealRepository.findById(1L)).thenReturn(Optional.of(deal));

        assertThrows(BusinessRuleViolationException.class, () ->
                service.submit(1L, createValidSubmitRequest(), 100L));
    }

    @Test
    void submit_validatesBusLeaseInServices() {
        when(dealRepository.findById(1L)).thenReturn(Optional.of(deal));

        PricingSubmitRequest req = new PricingSubmitRequest();
        req.setServicesSelected(List.of("MAINTENANCE"));
        req.setMonthlyKmCommitment(new BigDecimal("5000"));
        req.setPricePerKm(new BigDecimal("12.50"));

        assertThrows(BusinessRuleViolationException.class, () ->
                service.submit(1L, req, 100L));
    }

    @Test
    void submit_supersedesPreviousSubmitted() {
        PricingSubmission existing = createSubmission();
        existing.setStatus(PricingStatus.SUBMITTED);

        when(dealRepository.findById(1L)).thenReturn(Optional.of(deal));
        when(pricingSubmissionRepository.findByDealIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(existing));
        when(pricingSubmissionRepository.save(any())).thenAnswer(inv -> {
            PricingSubmission s = inv.getArgument(0);
            if (s.getId() == null) s.setId(2L);
            return s;
        });
        when(userRepository.findById(100L)).thenReturn(Optional.of(agent));

        service.submit(1L, createValidSubmitRequest(), 100L);

        assertEquals(PricingStatus.SUPERSEDED, existing.getStatus());
    }

    @Test
    void submit_setsSubStatusAwaitingApproval() {
        when(dealRepository.findById(1L)).thenReturn(Optional.of(deal));
        when(pricingSubmissionRepository.findByDealIdOrderByCreatedAtDesc(1L)).thenReturn(new ArrayList<>());
        when(pricingSubmissionRepository.save(any())).thenAnswer(inv -> {
            PricingSubmission s = inv.getArgument(0);
            s.setId(1L);
            return s;
        });
        when(userRepository.findById(100L)).thenReturn(Optional.of(agent));

        service.submit(1L, createValidSubmitRequest(), 100L);

        assertEquals(Stage5SubStatus.AWAITING_APPROVAL, deal.getSubStatus());
    }

    // === Approve tests ===

    @Test
    void approve_setsApprovedAndNegotiating() {
        PricingSubmission submission = createSubmission();
        when(pricingSubmissionRepository.findById(1L)).thenReturn(Optional.of(submission));
        when(pricingSubmissionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(dealRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.approve(1L, 1L, new PricingApproveRequest(), 200L);

        assertEquals(PricingStatus.APPROVED, submission.getStatus());
        assertEquals(Stage5SubStatus.NEGOTIATING, deal.getSubStatus());
    }

    @Test
    void approve_withManagerEdits_usesManagerValues() {
        PricingSubmission submission = createSubmission();
        when(pricingSubmissionRepository.findById(1L)).thenReturn(Optional.of(submission));
        when(pricingSubmissionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(dealRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        PricingApproveRequest req = new PricingApproveRequest();
        req.setServicesSelected(List.of("BUS_LEASE", "CHARGING"));
        req.setMonthlyKmCommitment(new BigDecimal("6000"));
        req.setPricePerKm(new BigDecimal("15.00"));

        service.approve(1L, 1L, req, 200L);

        assertEquals(List.of("BUS_LEASE", "CHARGING"), submission.getManagerServicesSelected());
        assertEquals(new BigDecimal("6000"), submission.getManagerMonthlyKm());
        assertEquals(new BigDecimal("15.00"), submission.getManagerPricePerKm());
    }

    @Test
    void approve_calculatesMonthlyValueCorrectly() {
        PricingSubmission submission = createSubmission();
        // pricePerKm=12.50, monthlyKm=5000, fleetSize=5
        // Expected: 12.50 * 5000 * 5 = 312500
        when(pricingSubmissionRepository.findById(1L)).thenReturn(Optional.of(submission));
        when(pricingSubmissionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(dealRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.approve(1L, 1L, new PricingApproveRequest(), 200L);

        assertEquals(new BigDecimal("312500.00"), deal.getEstimatedMonthlyValue());
    }

    @Test
    void approve_locksEstimatedMonthlyValue() {
        PricingSubmission submission = createSubmission();
        when(pricingSubmissionRepository.findById(1L)).thenReturn(Optional.of(submission));
        when(pricingSubmissionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(dealRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.approve(1L, 1L, new PricingApproveRequest(), 200L);

        assertNotNull(deal.getEstimatedMonthlyValue());
        verify(dealRepository).save(deal);
    }

    @Test
    void approve_onlySubmittedCanBeApproved() {
        PricingSubmission submission = createSubmission();
        submission.setStatus(PricingStatus.REJECTED);
        when(pricingSubmissionRepository.findById(1L)).thenReturn(Optional.of(submission));

        assertThrows(BusinessRuleViolationException.class, () ->
                service.approve(1L, 1L, new PricingApproveRequest(), 200L));
    }

    @Test
    void approve_approvedCannotBeReApproved() {
        PricingSubmission submission = createSubmission();
        submission.setStatus(PricingStatus.APPROVED);
        when(pricingSubmissionRepository.findById(1L)).thenReturn(Optional.of(submission));

        assertThrows(BusinessRuleViolationException.class, () ->
                service.approve(1L, 1L, new PricingApproveRequest(), 200L));
    }

    // === Reject tests ===

    @Test
    void reject_setsRejectedStatus() {
        PricingSubmission submission = createSubmission();
        when(pricingSubmissionRepository.findById(1L)).thenReturn(Optional.of(submission));
        when(pricingSubmissionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        PricingRejectRequest req = new PricingRejectRequest();
        req.setRejectionNote("Price too high");

        service.reject(1L, 1L, req, 200L);

        assertEquals(PricingStatus.REJECTED, submission.getStatus());
        assertEquals("Price too high", submission.getRejectionNote());
    }

    @Test
    void reject_onlySubmittedCanBeRejected() {
        PricingSubmission submission = createSubmission();
        submission.setStatus(PricingStatus.APPROVED);
        when(pricingSubmissionRepository.findById(1L)).thenReturn(Optional.of(submission));

        PricingRejectRequest req = new PricingRejectRequest();
        req.setRejectionNote("note");

        assertThrows(BusinessRuleViolationException.class, () ->
                service.reject(1L, 1L, req, 200L));
    }

    @Test
    void reject_wrongDealId_throws() {
        Deal otherDeal = new Deal();
        otherDeal.setId(999L);

        PricingSubmission submission = createSubmission();
        submission.setDeal(otherDeal);
        when(pricingSubmissionRepository.findById(1L)).thenReturn(Optional.of(submission));

        PricingRejectRequest req = new PricingRejectRequest();
        req.setRejectionNote("note");

        assertThrows(ResourceNotFoundException.class, () ->
                service.reject(1L, 1L, req, 200L));
    }

    // === History tests ===

    @Test
    void getHistory_returnsAllSubmissions() {
        PricingSubmission s1 = createSubmission();
        PricingSubmission s2 = createSubmission();
        s2.setId(2L);
        s2.setStatus(PricingStatus.SUPERSEDED);

        when(pricingSubmissionRepository.findByDealIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(s1, s2));

        List<PricingSubmissionResponse> history = service.getHistory(1L);

        assertEquals(2, history.size());
    }

    @Test
    void approve_withManagerPricePerKm_usesManagerValueForCalculation() {
        PricingSubmission submission = createSubmission();
        // Original: pricePerKm=12.50, monthlyKm=5000, fleetSize=5
        // Manager overrides pricePerKm to 10.00
        // Expected: 10.00 * 5000 * 5 = 250000
        when(pricingSubmissionRepository.findById(1L)).thenReturn(Optional.of(submission));
        when(pricingSubmissionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(dealRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        PricingApproveRequest req = new PricingApproveRequest();
        req.setPricePerKm(new BigDecimal("10.00"));

        service.approve(1L, 1L, req, 200L);

        assertEquals(new BigDecimal("250000.00"), deal.getEstimatedMonthlyValue());
    }

    @Test
    void submit_dealNotFound_throws() {
        when(dealRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                service.submit(999L, createValidSubmitRequest(), 100L));
    }

    @Test
    void submit_nullServicesSelected_throws() {
        when(dealRepository.findById(1L)).thenReturn(Optional.of(deal));

        PricingSubmitRequest req = new PricingSubmitRequest();
        req.setServicesSelected(null);
        req.setMonthlyKmCommitment(new BigDecimal("5000"));
        req.setPricePerKm(new BigDecimal("12.50"));

        assertThrows(BusinessRuleViolationException.class, () ->
                service.submit(1L, req, 100L));
    }
}
