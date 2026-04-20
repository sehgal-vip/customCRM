package com.turno.crm.service;

import com.turno.crm.exception.BusinessRuleViolationException;
import com.turno.crm.model.dto.WebhookLeadRequest;
import com.turno.crm.model.dto.WebhookLeadResponse;
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

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebhookServiceTest {

    @Mock private DealRepository dealRepository;
    @Mock private OperatorRepository operatorRepository;
    @Mock private ContactRepository contactRepository;
    @Mock private UserRepository userRepository;
    @Mock private RegionRepository regionRepository;
    @Mock private DocumentChecklistItemRepository checklistItemRepository;
    @Mock private DealDocumentRepository dealDocumentRepository;
    @Mock private AuditService auditService;

    @InjectMocks
    private WebhookService service;

    private User manager;

    @BeforeEach
    void setUp() {
        manager = new User();
        manager.setId(1L);
        manager.setName("Manager M");
    }

    private WebhookLeadRequest createValidRequest() {
        WebhookLeadRequest req = new WebhookLeadRequest();
        req.setOperatorName("New Operator");
        req.setContactName("John Doe");
        req.setContactPhone("+27123456789");
        req.setContactEmail("john@test.com");
        req.setLeadSource("INBOUND");
        req.setFleetSize(10);
        return req;
    }

    private void setupManagerAndOperator() {
        when(userRepository.findByRoleAndStatus(UserRole.MANAGER, UserStatus.ACTIVE))
                .thenReturn(List.of(manager));
        when(operatorRepository.findByCompanyNameAndPhone(anyString(), anyString()))
                .thenReturn(Optional.empty());
        when(operatorRepository.save(any())).thenAnswer(inv -> {
            Operator op = inv.getArgument(0);
            op.setId(1L);
            return op;
        });
        when(contactRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(dealRepository.save(any())).thenAnswer(inv -> {
            Deal d = inv.getArgument(0);
            d.setId(1L);
            return d;
        });
        when(checklistItemRepository.findByActiveTrue()).thenReturn(Collections.emptyList());
    }

    // === Valid lead ===

    @Test
    void processLead_validLead_createsOperatorContactDeal() {
        setupManagerAndOperator();

        WebhookLeadResponse resp = service.processLead(createValidRequest());

        assertNotNull(resp.getDealId());
        assertNotNull(resp.getOperatorId());
        assertFalse(resp.isDuplicateOperator());
        verify(operatorRepository).save(any(Operator.class));
        verify(contactRepository).save(any(Contact.class));
        verify(dealRepository).save(any(Deal.class));
    }

    @Test
    void processLead_dealAssignedToManager() {
        setupManagerAndOperator();

        service.processLead(createValidRequest());

        ArgumentCaptor<Deal> captor = ArgumentCaptor.forClass(Deal.class);
        verify(dealRepository).save(captor.capture());
        assertEquals(manager, captor.getValue().getAssignedAgent());
    }

    @Test
    void processLead_dealDefaultsToStage1Active() {
        setupManagerAndOperator();

        service.processLead(createValidRequest());

        ArgumentCaptor<Deal> captor = ArgumentCaptor.forClass(Deal.class);
        verify(dealRepository).save(captor.capture());
        assertEquals(DealStage.STAGE_1, captor.getValue().getCurrentStage());
        assertEquals(DealStatus.ACTIVE, captor.getValue().getStatus());
    }

    // === Idempotency ===

    @Test
    void processLead_duplicateSourceEventId_returnsExistingDeal() {
        Deal existingDeal = new Deal();
        existingDeal.setId(42L);
        Operator existingOp = new Operator();
        existingOp.setId(5L);
        existingDeal.setOperator(existingOp);

        when(dealRepository.findBySourceEventId("evt_123")).thenReturn(Optional.of(existingDeal));

        WebhookLeadRequest req = createValidRequest();
        req.setSourceEventId("evt_123");

        WebhookLeadResponse resp = service.processLead(req);

        assertEquals(42L, resp.getDealId());
        assertEquals(5L, resp.getOperatorId());
        assertFalse(resp.isDuplicateOperator());
        verify(operatorRepository, never()).save(any());
        verify(dealRepository, never()).save(any());
    }

    // === Duplicate operator detection ===

    @Test
    void processLead_duplicateOperator_usesExisting() {
        when(userRepository.findByRoleAndStatus(UserRole.MANAGER, UserStatus.ACTIVE))
                .thenReturn(List.of(manager));

        Operator existingOp = new Operator();
        existingOp.setId(10L);
        existingOp.setCompanyName("Existing Op");
        when(operatorRepository.findByCompanyNameAndPhone("Existing Op", "+27111111111"))
                .thenReturn(Optional.of(existingOp));
        when(contactRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(dealRepository.save(any())).thenAnswer(inv -> {
            Deal d = inv.getArgument(0);
            d.setId(1L);
            return d;
        });
        when(checklistItemRepository.findByActiveTrue()).thenReturn(Collections.emptyList());

        WebhookLeadRequest req = new WebhookLeadRequest();
        req.setOperatorName("Existing Op");
        req.setContactName("Jane");
        req.setContactPhone("+27111111111");
        req.setLeadSource("INBOUND");

        WebhookLeadResponse resp = service.processLead(req);

        assertTrue(resp.isDuplicateOperator());
        assertEquals(10L, resp.getOperatorId());
        verify(operatorRepository, never()).save(any());
    }

    // === Missing required fields ===

    @Test
    void processLead_missingBothPhoneAndEmail_throws() {
        WebhookLeadRequest req = new WebhookLeadRequest();
        req.setOperatorName("Op");
        req.setContactName("John");
        req.setContactPhone(null);
        req.setContactEmail(null);
        req.setLeadSource("INBOUND");

        assertThrows(BusinessRuleViolationException.class, () ->
                service.processLead(req));
    }

    @Test
    void processLead_emptyPhoneAndEmail_throws() {
        WebhookLeadRequest req = new WebhookLeadRequest();
        req.setOperatorName("Op");
        req.setContactName("John");
        req.setContactPhone("");
        req.setContactEmail("");
        req.setLeadSource("INBOUND");

        assertThrows(BusinessRuleViolationException.class, () ->
                service.processLead(req));
    }

    @Test
    void processLead_contactPhoneOnly_succeeds() {
        when(userRepository.findByRoleAndStatus(UserRole.MANAGER, UserStatus.ACTIVE))
                .thenReturn(List.of(manager));
        when(operatorRepository.findByCompanyNameAndPhone(anyString(), anyString()))
                .thenReturn(Optional.empty());
        when(operatorRepository.save(any())).thenAnswer(inv -> {
            Operator op = inv.getArgument(0);
            op.setId(1L);
            return op;
        });
        when(contactRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(dealRepository.save(any())).thenAnswer(inv -> {
            Deal d = inv.getArgument(0);
            d.setId(1L);
            return d;
        });
        when(checklistItemRepository.findByActiveTrue()).thenReturn(Collections.emptyList());

        WebhookLeadRequest req = new WebhookLeadRequest();
        req.setOperatorName("Op");
        req.setContactName("John");
        req.setContactPhone("+27123456789");
        req.setContactEmail(null);
        req.setLeadSource("INBOUND");

        assertDoesNotThrow(() -> service.processLead(req));
    }

    @Test
    void processLead_contactEmailOnly_succeeds() {
        when(userRepository.findByRoleAndStatus(UserRole.MANAGER, UserStatus.ACTIVE))
                .thenReturn(List.of(manager));
        when(operatorRepository.findByCompanyNameAndPhone(anyString(), any()))
                .thenReturn(Optional.empty());
        when(operatorRepository.save(any())).thenAnswer(inv -> {
            Operator op = inv.getArgument(0);
            op.setId(1L);
            return op;
        });
        when(contactRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(dealRepository.save(any())).thenAnswer(inv -> {
            Deal d = inv.getArgument(0);
            d.setId(1L);
            return d;
        });
        when(checklistItemRepository.findByActiveTrue()).thenReturn(Collections.emptyList());

        WebhookLeadRequest req = new WebhookLeadRequest();
        req.setOperatorName("Op");
        req.setContactName("John");
        req.setContactPhone(null);
        req.setContactEmail("john@test.com");
        req.setLeadSource("INBOUND");

        assertDoesNotThrow(() -> service.processLead(req));
    }

    @Test
    void processLead_noActiveManagers_throws() {
        when(userRepository.findByRoleAndStatus(UserRole.MANAGER, UserStatus.ACTIVE))
                .thenReturn(Collections.emptyList());

        WebhookLeadRequest req = createValidRequest();

        assertThrows(BusinessRuleViolationException.class, () ->
                service.processLead(req));
    }

    @Test
    void processLead_autoCreatesDealDocuments() {
        setupManagerAndOperator();

        DocumentChecklistItem item = new DocumentChecklistItem();
        item.setId(1L);
        item.setActive(true);
        when(checklistItemRepository.findByActiveTrue()).thenReturn(List.of(item));
        when(dealDocumentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.processLead(createValidRequest());

        verify(dealDocumentRepository).save(any(DealDocument.class));
    }

    @Test
    void processLead_invalidLeadSource_defaultsToInbound() {
        setupManagerAndOperator();

        WebhookLeadRequest req = createValidRequest();
        req.setLeadSource("INVALID_SOURCE");

        service.processLead(req);

        ArgumentCaptor<Deal> captor = ArgumentCaptor.forClass(Deal.class);
        verify(dealRepository).save(captor.capture());
        assertEquals(LeadSource.INBOUND, captor.getValue().getLeadSource());
    }
}
