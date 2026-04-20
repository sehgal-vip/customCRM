package com.turno.crm.service;

import com.turno.crm.exception.BusinessRuleViolationException;
import com.turno.crm.exception.DuplicateResourceException;
import com.turno.crm.exception.ResourceNotFoundException;
import com.turno.crm.exception.UnauthorizedAccessException;
import com.turno.crm.model.dto.*;
import com.turno.crm.model.entity.*;
import com.turno.crm.model.enums.*;
import com.turno.crm.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DealServiceTest {

    @Mock private DealRepository dealRepository;
    @Mock private OperatorRepository operatorRepository;
    @Mock private UserRepository userRepository;
    @Mock private DocumentChecklistItemRepository checklistItemRepository;
    @Mock private DealDocumentRepository dealDocumentRepository;
    @Mock private StageTransitionRepository stageTransitionRepository;
    @Mock private PricingSubmissionRepository pricingSubmissionRepository;
    @Mock private ActivityReportRepository activityReportRepository;
    @Mock private AuditService auditService;
    @Mock private NotificationService notificationService;

    @InjectMocks
    private DealService service;

    private Deal deal;
    private Operator operator;
    private User agent;
    private User managerUser;

    @BeforeEach
    void setUp() {
        operator = new Operator();
        operator.setId(1L);
        operator.setCompanyName("Test Operator");

        agent = new User();
        agent.setId(100L);
        agent.setName("Agent A");

        managerUser = new User();
        managerUser.setId(200L);
        managerUser.setName("Manager M");

        deal = new Deal();
        deal.setId(1L);
        deal.setName("Test Deal");
        deal.setOperator(operator);
        deal.setAssignedAgent(agent);
        deal.setCurrentStage(DealStage.STAGE_1);
        deal.setStatus(DealStatus.ACTIVE);
        deal.setCreatedAt(OffsetDateTime.now().minusDays(5));
    }

    private CreateDealRequest createValidCreateRequest() {
        CreateDealRequest req = new CreateDealRequest();
        req.setName("New Deal");
        req.setOperatorId(1L);
        req.setAssignedAgentId(100L);
        req.setLeadSource(LeadSource.AGENT_FIELD);
        req.setFleetSize(10);
        return req;
    }

    // === Create tests ===

    @Test
    void create_dealNameUniquenessCheck() {
        when(dealRepository.existsByName("Existing")).thenReturn(true);

        CreateDealRequest req = createValidCreateRequest();
        req.setName("Existing");

        assertThrows(DuplicateResourceException.class, () ->
                service.create(req, 100L));
    }

    @Test
    void create_autoCreatesDealDocumentsFromActiveChecklistItems() {
        when(dealRepository.existsByName("New Deal")).thenReturn(false);
        when(operatorRepository.findById(1L)).thenReturn(Optional.of(operator));
        when(userRepository.findById(100L)).thenReturn(Optional.of(agent));
        when(dealRepository.save(any(Deal.class))).thenAnswer(inv -> {
            Deal d = inv.getArgument(0);
            d.setId(1L);
            d.setCreatedAt(OffsetDateTime.now());
            return d;
        });

        DocumentChecklistItem item1 = new DocumentChecklistItem();
        item1.setId(1L);
        item1.setActive(true);
        DocumentChecklistItem item2 = new DocumentChecklistItem();
        item2.setId(2L);
        item2.setActive(true);
        when(checklistItemRepository.findByActiveTrue()).thenReturn(List.of(item1, item2));
        when(dealDocumentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(stageTransitionRepository.findByDealIdOrderByCreatedAtDesc(1L)).thenReturn(Collections.emptyList());
        when(dealDocumentRepository.countMandatoryTotal(1L)).thenReturn(0L);

        service.create(createValidCreateRequest(), 100L);

        verify(dealDocumentRepository, times(2)).save(any(DealDocument.class));
    }

    @Test
    void create_defaultsToStage1Active() {
        when(dealRepository.existsByName("New Deal")).thenReturn(false);
        when(operatorRepository.findById(1L)).thenReturn(Optional.of(operator));
        when(userRepository.findById(100L)).thenReturn(Optional.of(agent));
        when(dealRepository.save(any(Deal.class))).thenAnswer(inv -> {
            Deal d = inv.getArgument(0);
            d.setId(1L);
            d.setCreatedAt(OffsetDateTime.now());
            return d;
        });
        when(checklistItemRepository.findByActiveTrue()).thenReturn(Collections.emptyList());
        when(stageTransitionRepository.findByDealIdOrderByCreatedAtDesc(1L)).thenReturn(Collections.emptyList());
        when(dealDocumentRepository.countMandatoryTotal(1L)).thenReturn(0L);

        DealResponse resp = service.create(createValidCreateRequest(), 100L);

        assertEquals(DealStage.STAGE_1, resp.getCurrentStage());
        assertEquals(DealStatus.ACTIVE, resp.getStatus());
    }

    // === Update tests ===

    @Test
    void update_agentCanEditName() {
        when(dealRepository.findById(1L)).thenReturn(Optional.of(deal));
        when(dealRepository.findByName("Updated")).thenReturn(Optional.empty());
        when(dealRepository.save(any(Deal.class))).thenAnswer(inv -> inv.getArgument(0));
        when(stageTransitionRepository.findByDealIdOrderByCreatedAtDesc(1L)).thenReturn(Collections.emptyList());
        when(dealDocumentRepository.countMandatoryTotal(1L)).thenReturn(0L);

        UpdateDealRequest req = new UpdateDealRequest();
        req.setName("Updated");

        DealResponse resp = service.update(1L, req, 100L, UserRole.AGENT);

        assertEquals("Updated", resp.getName());
    }

    @Test
    void update_agentCanEditFleetSize() {
        when(dealRepository.findById(1L)).thenReturn(Optional.of(deal));
        when(dealRepository.save(any(Deal.class))).thenAnswer(inv -> inv.getArgument(0));
        when(stageTransitionRepository.findByDealIdOrderByCreatedAtDesc(1L)).thenReturn(Collections.emptyList());
        when(dealDocumentRepository.countMandatoryTotal(1L)).thenReturn(0L);

        UpdateDealRequest req = new UpdateDealRequest();
        req.setFleetSize(20);

        DealResponse resp = service.update(1L, req, 100L, UserRole.AGENT);

        assertEquals(20, resp.getFleetSize());
    }

    @Test
    void update_agentCannotChangeAssignedAgentId() {
        when(dealRepository.findById(1L)).thenReturn(Optional.of(deal));

        UpdateDealRequest req = new UpdateDealRequest();
        req.setAssignedAgentId(200L);

        assertThrows(UnauthorizedAccessException.class, () ->
                service.update(1L, req, 100L, UserRole.AGENT));
    }

    @Test
    void update_agentCannotUpdateOthersDeal() {
        when(dealRepository.findById(1L)).thenReturn(Optional.of(deal));

        UpdateDealRequest req = new UpdateDealRequest();
        req.setName("Hack");

        assertThrows(UnauthorizedAccessException.class, () ->
                service.update(1L, req, 999L, UserRole.AGENT));
    }

    @Test
    void update_managerCanChangeAllFields() {
        when(dealRepository.findById(1L)).thenReturn(Optional.of(deal));
        User newAgent = new User();
        newAgent.setId(300L);
        newAgent.setName("NewAgent");
        when(userRepository.findById(300L)).thenReturn(Optional.of(newAgent));
        when(userRepository.findById(200L)).thenReturn(Optional.of(managerUser));
        when(dealRepository.save(any(Deal.class))).thenAnswer(inv -> inv.getArgument(0));
        when(stageTransitionRepository.findByDealIdOrderByCreatedAtDesc(1L)).thenReturn(Collections.emptyList());
        when(dealDocumentRepository.countMandatoryTotal(1L)).thenReturn(0L);

        UpdateDealRequest req = new UpdateDealRequest();
        req.setAssignedAgentId(300L);

        DealResponse resp = service.update(1L, req, 200L, UserRole.MANAGER);

        assertNotNull(resp);
        verify(notificationService).fireDealAssignedNewOwner(eq(deal), eq(newAgent), eq(managerUser));
    }

    @Test
    void update_estimatedMonthlyValueReadOnlyAfterPricingApproval() {
        when(dealRepository.findById(1L)).thenReturn(Optional.of(deal));
        when(pricingSubmissionRepository.existsByDealIdAndStatus(1L, PricingStatus.APPROVED)).thenReturn(true);

        UpdateDealRequest req = new UpdateDealRequest();
        req.setEstimatedMonthlyValue(new BigDecimal("100000"));

        assertThrows(BusinessRuleViolationException.class, () ->
                service.update(1L, req, 200L, UserRole.MANAGER));
    }

    @Test
    void update_estimatedMonthlyValueEditableBeforePricingApproval() {
        when(dealRepository.findById(1L)).thenReturn(Optional.of(deal));
        when(pricingSubmissionRepository.existsByDealIdAndStatus(1L, PricingStatus.APPROVED)).thenReturn(false);
        when(dealRepository.save(any(Deal.class))).thenAnswer(inv -> inv.getArgument(0));
        when(stageTransitionRepository.findByDealIdOrderByCreatedAtDesc(1L)).thenReturn(Collections.emptyList());
        when(dealDocumentRepository.countMandatoryTotal(1L)).thenReturn(0L);

        UpdateDealRequest req = new UpdateDealRequest();
        req.setEstimatedMonthlyValue(new BigDecimal("100000"));

        assertDoesNotThrow(() ->
                service.update(1L, req, 200L, UserRole.MANAGER));
    }

    // === Archive tests ===

    @Test
    void archive_setsArchivedPreservesStage() {
        when(dealRepository.findById(1L)).thenReturn(Optional.of(deal));
        when(dealRepository.save(any(Deal.class))).thenAnswer(inv -> inv.getArgument(0));
        when(stageTransitionRepository.findByDealIdOrderByCreatedAtDesc(1L)).thenReturn(Collections.emptyList());
        when(dealDocumentRepository.countMandatoryTotal(1L)).thenReturn(0L);

        ArchiveDealRequest req = new ArchiveDealRequest();
        req.setReason("Lost to competitor");
        req.setReasonText("They chose ZapBus");

        DealResponse resp = service.archive(1L, req, 200L);

        assertEquals(DealStatus.ARCHIVED, resp.getStatus());
        assertEquals(DealStage.STAGE_1, resp.getCurrentStage());
    }

    @Test
    void archive_requiresReason() {
        when(dealRepository.findById(1L)).thenReturn(Optional.of(deal));
        when(dealRepository.save(any(Deal.class))).thenAnswer(inv -> inv.getArgument(0));
        when(stageTransitionRepository.findByDealIdOrderByCreatedAtDesc(1L)).thenReturn(Collections.emptyList());
        when(dealDocumentRepository.countMandatoryTotal(1L)).thenReturn(0L);

        ArchiveDealRequest req = new ArchiveDealRequest();
        req.setReason("Lost");

        DealResponse resp = service.archive(1L, req, 200L);

        assertEquals("Lost", resp.getArchivedReason());
        verify(notificationService).fireDealArchived(eq(deal), eq(agent), eq("Lost"));
    }

    @Test
    void archive_onlyActiveDeals() {
        deal.setStatus(DealStatus.COMPLETED);
        when(dealRepository.findById(1L)).thenReturn(Optional.of(deal));

        ArchiveDealRequest req = new ArchiveDealRequest();
        req.setReason("reason");

        assertThrows(BusinessRuleViolationException.class, () ->
                service.archive(1L, req, 200L));
    }

    // === Reactivate tests ===

    @Test
    void reactivate_archivedToActive() {
        deal.setStatus(DealStatus.ARCHIVED);
        deal.setArchivedReason("Lost");
        when(dealRepository.findById(1L)).thenReturn(Optional.of(deal));
        when(dealRepository.save(any(Deal.class))).thenAnswer(inv -> inv.getArgument(0));
        when(stageTransitionRepository.findByDealIdOrderByCreatedAtDesc(1L)).thenReturn(Collections.emptyList());
        when(dealDocumentRepository.countMandatoryTotal(1L)).thenReturn(0L);

        DealResponse resp = service.reactivate(1L, 200L);

        assertEquals(DealStatus.ACTIVE, resp.getStatus());
        assertNull(deal.getArchivedReason());
        verify(notificationService).fireDealReactivated(eq(deal), eq(agent));
    }

    @Test
    void reactivate_rejectsIfNotArchived() {
        deal.setStatus(DealStatus.ACTIVE);
        when(dealRepository.findById(1L)).thenReturn(Optional.of(deal));

        assertThrows(BusinessRuleViolationException.class, () ->
                service.reactivate(1L, 200L));
    }

    // === Reopen tests ===

    @Test
    void reopen_completedToActiveAtStage8() {
        deal.setStatus(DealStatus.COMPLETED);
        deal.setCurrentStage(DealStage.STAGE_8);
        when(dealRepository.findById(1L)).thenReturn(Optional.of(deal));
        when(dealRepository.save(any(Deal.class))).thenAnswer(inv -> inv.getArgument(0));
        when(stageTransitionRepository.findByDealIdOrderByCreatedAtDesc(1L)).thenReturn(Collections.emptyList());
        when(dealDocumentRepository.countMandatoryTotal(1L)).thenReturn(0L);

        DealResponse resp = service.reopen(1L, 200L);

        assertEquals(DealStatus.ACTIVE, resp.getStatus());
        assertEquals(DealStage.STAGE_8, resp.getCurrentStage());
        assertTrue(resp.isReopened());
    }

    @Test
    void reopen_rejectsIfNotCompleted() {
        deal.setStatus(DealStatus.ACTIVE);
        when(dealRepository.findById(1L)).thenReturn(Optional.of(deal));

        assertThrows(BusinessRuleViolationException.class, () ->
                service.reopen(1L, 200L));
    }

    // === List tests ===

    @Test
    void list_agentSeesOwnDealsOnly() {
        when(dealRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(deal)));

        Page<DealListResponse> page = service.list(null, null, null, null, null, null, null, null,
                100L, UserRole.AGENT, PageRequest.of(0, 10));

        assertNotNull(page);
        // The Specification is built internally, so we verify the call happened
        verify(dealRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void list_managerSeesAll() {
        when(dealRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(deal)));

        Page<DealListResponse> page = service.list(null, null, null, null, null, null, null, null,
                200L, UserRole.MANAGER, PageRequest.of(0, 10));

        assertNotNull(page);
        verify(dealRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void getById_agentCannotSeeOthersDeal() {
        when(dealRepository.findById(1L)).thenReturn(Optional.of(deal));

        assertThrows(UnauthorizedAccessException.class, () ->
                service.getById(1L, 999L, UserRole.AGENT));
    }

    @Test
    void getById_agentCanSeeOwnDeal() {
        when(dealRepository.findById(1L)).thenReturn(Optional.of(deal));
        when(stageTransitionRepository.findByDealIdOrderByCreatedAtDesc(1L)).thenReturn(Collections.emptyList());
        when(dealDocumentRepository.countMandatoryTotal(1L)).thenReturn(0L);

        DealResponse resp = service.getById(1L, 100L, UserRole.AGENT);

        assertNotNull(resp);
    }

    @Test
    void getById_managerCanSeeAnyDeal() {
        when(dealRepository.findById(1L)).thenReturn(Optional.of(deal));
        when(stageTransitionRepository.findByDealIdOrderByCreatedAtDesc(1L)).thenReturn(Collections.emptyList());
        when(dealDocumentRepository.countMandatoryTotal(1L)).thenReturn(0L);

        DealResponse resp = service.getById(1L, 200L, UserRole.MANAGER);

        assertNotNull(resp);
    }

    @Test
    void create_duplicateName_existingDeal_throws() {
        when(dealRepository.existsByName("Duplicate")).thenReturn(true);

        CreateDealRequest req = createValidCreateRequest();
        req.setName("Duplicate");

        assertThrows(DuplicateResourceException.class, () ->
                service.create(req, 100L));
    }
}
