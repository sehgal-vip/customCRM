package com.turno.crm.service;

import com.turno.crm.exception.BusinessRuleViolationException;
import com.turno.crm.exception.ResourceNotFoundException;
import com.turno.crm.model.dto.ActivityReportResponse;
import com.turno.crm.model.dto.CreateActivityReportRequest;
import com.turno.crm.model.dto.CreateContactRequest;
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

import java.time.OffsetDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ActivityReportServiceTest {

    @Mock private ActivityReportRepository activityReportRepository;
    @Mock private DealRepository dealRepository;
    @Mock private ContactRepository contactRepository;
    @Mock private AttachmentRepository attachmentRepository;
    @Mock private SystemReminderRepository systemReminderRepository;
    @Mock private UserRepository userRepository;
    @Mock private AuditService auditService;
    @Mock private NotificationService notificationService;
    @Mock private TaskService taskService;

    @InjectMocks
    private ActivityReportService service;

    private Deal deal;
    private Contact contact;
    private User agentUser;

    @BeforeEach
    void setUp() {
        deal = new Deal();
        deal.setId(1L);
        deal.setStatus(DealStatus.ACTIVE);
        deal.setCurrentStage(DealStage.STAGE_1);
        deal.setName("Test Deal");

        Operator operator = new Operator();
        operator.setId(1L);
        operator.setCompanyName("Test Op");
        deal.setOperator(operator);

        agentUser = new User();
        agentUser.setId(100L);
        agentUser.setName("Agent A");
        deal.setAssignedAgent(agentUser);

        contact = new Contact();
        contact.setId(10L);
        contact.setName("John");
        contact.setMobile("+27123456789");
        contact.setEmail("john@test.com");
        contact.setOperator(operator);
    }

    private CreateActivityReportRequest createValidRequest(ActivityType type) {
        CreateActivityReportRequest req = new CreateActivityReportRequest();
        req.setActivityType(type);
        req.setInteractionDatetime(OffsetDateTime.now());
        req.setContactId(10L);
        req.setContactRole(ContactRole.OWNER);
        req.setDuration(ReportDuration.values()[0]);
        req.setNextAction("Follow up");
        req.setNextActionOwner(NextActionOwner.values()[0]);
        return req;
    }

    private void setupForSubmit(DealStage stage, ActivityType type, Map<String, Object> phaseData) {
        deal.setCurrentStage(stage);
        if (stage == DealStage.STAGE_5) {
            deal.setSubStatus(Stage5SubStatus.PROPOSAL_SENT);
        }
        CreateActivityReportRequest req = createValidRequest(type);
        req.setPhaseSpecificData(phaseData);

        when(dealRepository.findById(1L)).thenReturn(Optional.of(deal));
        when(contactRepository.findById(10L)).thenReturn(Optional.of(contact));
        when(activityReportRepository.existsNonVoidedReportWithTemplateTypes(eq(1L), anyList())).thenReturn(false);
        when(activityReportRepository.save(any(ActivityReport.class))).thenAnswer(inv -> {
            ActivityReport r = inv.getArgument(0);
            r.setId(1L);
            return r;
        });
        when(systemReminderRepository.findByDealIdAndDismissedFalse(1L)).thenReturn(List.of());
    }

    // === Template selection: 12 combos ===

    @Test
    void determineTemplate_earlyPhase_firstFieldVisit_returnsT1() {
        assertEquals(TemplateType.T1, service.determineTemplate(DealStage.Phase.EARLY, ActivityType.FIELD_VISIT, true));
    }

    @Test
    void determineTemplate_earlyPhase_followUpFieldVisit_returnsT2() {
        assertEquals(TemplateType.T2, service.determineTemplate(DealStage.Phase.EARLY, ActivityType.FIELD_VISIT, false));
    }

    @Test
    void determineTemplate_earlyPhase_firstVirtual_returnsT3() {
        assertEquals(TemplateType.T3, service.determineTemplate(DealStage.Phase.EARLY, ActivityType.VIRTUAL, true));
    }

    @Test
    void determineTemplate_earlyPhase_followUpVirtual_returnsT4() {
        assertEquals(TemplateType.T4, service.determineTemplate(DealStage.Phase.EARLY, ActivityType.VIRTUAL, false));
    }

    @Test
    void determineTemplate_commercialPhase_firstFieldVisit_returnsT5() {
        assertEquals(TemplateType.T5, service.determineTemplate(DealStage.Phase.COMMERCIAL, ActivityType.FIELD_VISIT, true));
    }

    @Test
    void determineTemplate_commercialPhase_followUpFieldVisit_returnsT6() {
        assertEquals(TemplateType.T6, service.determineTemplate(DealStage.Phase.COMMERCIAL, ActivityType.FIELD_VISIT, false));
    }

    @Test
    void determineTemplate_commercialPhase_firstVirtual_returnsT7() {
        assertEquals(TemplateType.T7, service.determineTemplate(DealStage.Phase.COMMERCIAL, ActivityType.VIRTUAL, true));
    }

    @Test
    void determineTemplate_commercialPhase_followUpVirtual_returnsT8() {
        assertEquals(TemplateType.T8, service.determineTemplate(DealStage.Phase.COMMERCIAL, ActivityType.VIRTUAL, false));
    }

    @Test
    void determineTemplate_closurePhase_firstFieldVisit_returnsT9() {
        assertEquals(TemplateType.T9, service.determineTemplate(DealStage.Phase.CLOSURE, ActivityType.FIELD_VISIT, true));
    }

    @Test
    void determineTemplate_closurePhase_followUpFieldVisit_returnsT10() {
        assertEquals(TemplateType.T10, service.determineTemplate(DealStage.Phase.CLOSURE, ActivityType.FIELD_VISIT, false));
    }

    @Test
    void determineTemplate_closurePhase_firstVirtual_returnsT11() {
        assertEquals(TemplateType.T11, service.determineTemplate(DealStage.Phase.CLOSURE, ActivityType.VIRTUAL, true));
    }

    @Test
    void determineTemplate_closurePhase_followUpVirtual_returnsT12() {
        assertEquals(TemplateType.T12, service.determineTemplate(DealStage.Phase.CLOSURE, ActivityType.VIRTUAL, false));
    }

    // === isFirstInPhase ===

    @Test
    void submitReport_firstInPhase_setsIsFirstInPhaseTrue() {
        Map<String, Object> data = Map.of(
                "operatorType", "STV", "currentFleetSize", 10, "currentVehicleType", "Diesel",
                "primaryUseCase", "BRT", "interestLevel", "High", "decisionMakerMet", true);
        setupForSubmit(DealStage.STAGE_1, ActivityType.FIELD_VISIT, data);

        service.submitReport(1L, createValidRequestWithData(ActivityType.FIELD_VISIT, data), 100L);

        ArgumentCaptor<ActivityReport> captor = ArgumentCaptor.forClass(ActivityReport.class);
        verify(activityReportRepository).save(captor.capture());
        assertTrue(captor.getValue().getIsFirstInPhase());
    }

    @Test
    void submitReport_notFirstInPhase_setsIsFirstInPhaseFalse() {
        Map<String, Object> data = Map.of(
                "interestLevelUpdate", "Still high", "decisionMakerAccess", true);
        deal.setCurrentStage(DealStage.STAGE_2);

        when(dealRepository.findById(1L)).thenReturn(Optional.of(deal));
        when(contactRepository.findById(10L)).thenReturn(Optional.of(contact));
        when(activityReportRepository.existsNonVoidedReportWithTemplateTypes(eq(1L), anyList())).thenReturn(true);
        when(activityReportRepository.save(any(ActivityReport.class))).thenAnswer(inv -> {
            ActivityReport r = inv.getArgument(0);
            r.setId(2L);
            return r;
        });
        when(systemReminderRepository.findByDealIdAndDismissedFalse(1L)).thenReturn(List.of());

        service.submitReport(1L, createValidRequestWithData(ActivityType.FIELD_VISIT, data), 100L);

        ArgumentCaptor<ActivityReport> captor = ArgumentCaptor.forClass(ActivityReport.class);
        verify(activityReportRepository).save(captor.capture());
        assertFalse(captor.getValue().getIsFirstInPhase());
    }

    // === Contact validation ===

    @Test
    void submitReport_contactWithPhone_passes() {
        Map<String, Object> data = Map.of(
                "operatorType", "STV", "currentFleetSize", 10, "currentVehicleType", "Diesel",
                "primaryUseCase", "BRT", "interestLevel", "High", "decisionMakerMet", true);
        setupForSubmit(DealStage.STAGE_1, ActivityType.FIELD_VISIT, data);

        assertDoesNotThrow(() ->
                service.submitReport(1L, createValidRequestWithData(ActivityType.FIELD_VISIT, data), 100L));
    }

    @Test
    void submitReport_contactMissingBothPhoneAndEmail_throws() {
        Contact noContact = new Contact();
        noContact.setId(10L);
        noContact.setName("NoInfo");
        noContact.setMobile(null);
        noContact.setEmail(null);

        Map<String, Object> data = Map.of(
                "operatorType", "STV", "currentFleetSize", 10, "currentVehicleType", "Diesel",
                "primaryUseCase", "BRT", "interestLevel", "High", "decisionMakerMet", true);

        deal.setCurrentStage(DealStage.STAGE_1);
        when(dealRepository.findById(1L)).thenReturn(Optional.of(deal));
        when(contactRepository.findById(10L)).thenReturn(Optional.of(noContact));

        assertThrows(BusinessRuleViolationException.class, () ->
                service.submitReport(1L, createValidRequestWithData(ActivityType.FIELD_VISIT, data), 100L));
    }

    @Test
    void submitReport_newContactCreation_savesToOperator() {
        deal.setCurrentStage(DealStage.STAGE_1);
        when(dealRepository.findById(1L)).thenReturn(Optional.of(deal));
        when(activityReportRepository.existsNonVoidedReportWithTemplateTypes(eq(1L), anyList())).thenReturn(false);

        Contact newSavedContact = new Contact();
        newSavedContact.setId(20L);
        newSavedContact.setName("NewGuy");
        newSavedContact.setMobile("+27999999999");
        when(contactRepository.save(any(Contact.class))).thenReturn(newSavedContact);
        when(activityReportRepository.save(any(ActivityReport.class))).thenAnswer(inv -> {
            ActivityReport r = inv.getArgument(0);
            r.setId(3L);
            return r;
        });
        when(systemReminderRepository.findByDealIdAndDismissedFalse(1L)).thenReturn(List.of());

        CreateActivityReportRequest req = createValidRequest(ActivityType.FIELD_VISIT);
        req.setContactId(null);
        CreateContactRequest nc = new CreateContactRequest();
        nc.setName("NewGuy");
        nc.setRole(ContactRole.OWNER);
        nc.setMobile("+27999999999");
        req.setNewContact(nc);

        Map<String, Object> data = Map.of(
                "operatorType", "STV", "currentFleetSize", 10, "currentVehicleType", "Diesel",
                "primaryUseCase", "BRT", "interestLevel", "High", "decisionMakerMet", true);
        req.setPhaseSpecificData(data);

        service.submitReport(1L, req, 100L);

        verify(contactRepository).save(any(Contact.class));
    }

    @Test
    void submitReport_newContactMissingBothMethods_throws() {
        deal.setCurrentStage(DealStage.STAGE_1);
        when(dealRepository.findById(1L)).thenReturn(Optional.of(deal));

        CreateActivityReportRequest req = createValidRequest(ActivityType.FIELD_VISIT);
        req.setContactId(null);
        CreateContactRequest nc = new CreateContactRequest();
        nc.setName("NoInfo");
        nc.setRole(ContactRole.OWNER);
        // No mobile, no email
        req.setNewContact(nc);

        assertThrows(BusinessRuleViolationException.class, () ->
                service.submitReport(1L, req, 100L));
    }

    // === Phase-specific data validation ===

    @Test
    void submitReport_missingPhaseSpecificData_throws() {
        deal.setCurrentStage(DealStage.STAGE_1);
        when(dealRepository.findById(1L)).thenReturn(Optional.of(deal));
        when(contactRepository.findById(10L)).thenReturn(Optional.of(contact));
        when(activityReportRepository.existsNonVoidedReportWithTemplateTypes(eq(1L), anyList())).thenReturn(false);

        CreateActivityReportRequest req = createValidRequest(ActivityType.FIELD_VISIT);
        req.setPhaseSpecificData(null); // Missing!

        assertThrows(BusinessRuleViolationException.class, () ->
                service.submitReport(1L, req, 100L));
    }

    @Test
    void submitReport_missingRequiredField_throws() {
        deal.setCurrentStage(DealStage.STAGE_1);
        when(dealRepository.findById(1L)).thenReturn(Optional.of(deal));
        when(contactRepository.findById(10L)).thenReturn(Optional.of(contact));
        when(activityReportRepository.existsNonVoidedReportWithTemplateTypes(eq(1L), anyList())).thenReturn(false);

        // T1 requires operatorType, currentFleetSize, etc. - only provide partial
        Map<String, Object> data = Map.of("operatorType", "STV");
        CreateActivityReportRequest req = createValidRequest(ActivityType.FIELD_VISIT);
        req.setPhaseSpecificData(data);

        assertThrows(BusinessRuleViolationException.class, () ->
                service.submitReport(1L, req, 100L));
    }

    // === Deal must be active ===

    @Test
    void submitReport_dealNotActive_throws() {
        deal.setStatus(DealStatus.ARCHIVED);
        when(dealRepository.findById(1L)).thenReturn(Optional.of(deal));

        assertThrows(BusinessRuleViolationException.class, () ->
                service.submitReport(1L, createValidRequest(ActivityType.FIELD_VISIT), 100L));
    }

    // === Void / Unvoid ===

    @Test
    void voidReport_setsVoidedFlags() {
        ActivityReport report = new ActivityReport();
        report.setId(5L);
        report.setDeal(deal);
        report.setVoided(false);
        report.setAgent(agentUser);
        report.setContact(contact);
        report.setTemplateType(TemplateType.T1);

        when(activityReportRepository.findById(5L)).thenReturn(Optional.of(report));
        when(activityReportRepository.save(any(ActivityReport.class))).thenAnswer(inv -> inv.getArgument(0));
        when(activityReportRepository.findFirstByDealIdAndVoidedFalseOrderBySubmissionDatetimeDesc(1L))
                .thenReturn(Optional.empty());

        service.voidReport(1L, 5L, "Duplicate", 100L);

        assertTrue(report.getVoided());
        assertNotNull(report.getVoidedAt());
        assertEquals("Duplicate", report.getVoidedReason());
    }

    @Test
    void voidReport_alreadyVoided_throws() {
        ActivityReport report = new ActivityReport();
        report.setId(5L);
        report.setDeal(deal);
        report.setVoided(true);

        when(activityReportRepository.findById(5L)).thenReturn(Optional.of(report));

        assertThrows(BusinessRuleViolationException.class, () ->
                service.voidReport(1L, 5L, "reason", 100L));
    }

    @Test
    void voidReport_noRemainingActive_firesNoNextActionNotification() {
        ActivityReport report = new ActivityReport();
        report.setId(5L);
        report.setDeal(deal);
        report.setVoided(false);
        report.setAgent(agentUser);
        report.setContact(contact);
        report.setTemplateType(TemplateType.T1);

        when(activityReportRepository.findById(5L)).thenReturn(Optional.of(report));
        when(activityReportRepository.save(any(ActivityReport.class))).thenAnswer(inv -> inv.getArgument(0));
        when(activityReportRepository.findFirstByDealIdAndVoidedFalseOrderBySubmissionDatetimeDesc(1L))
                .thenReturn(Optional.empty());

        service.voidReport(1L, 5L, "reason", 100L);

        verify(notificationService).fireNoNextActionSet(deal, agentUser);
    }

    @Test
    void unvoidReport_clearsVoidedFlags() {
        ActivityReport report = new ActivityReport();
        report.setId(5L);
        report.setDeal(deal);
        report.setVoided(true);
        report.setVoidedAt(OffsetDateTime.now());
        report.setAgent(agentUser);
        report.setContact(contact);
        report.setTemplateType(TemplateType.T1);

        when(activityReportRepository.findById(5L)).thenReturn(Optional.of(report));
        when(activityReportRepository.save(any(ActivityReport.class))).thenAnswer(inv -> inv.getArgument(0));

        service.unvoidReport(1L, 5L, 100L);

        assertFalse(report.getVoided());
        assertNotNull(report.getUnvoidedAt());
    }

    @Test
    void unvoidReport_notVoided_throws() {
        ActivityReport report = new ActivityReport();
        report.setId(5L);
        report.setDeal(deal);
        report.setVoided(false);

        when(activityReportRepository.findById(5L)).thenReturn(Optional.of(report));

        assertThrows(BusinessRuleViolationException.class, () ->
                service.unvoidReport(1L, 5L, 100L));
    }

    @Test
    void submitReport_neitherContactIdNorNewContact_throws() {
        deal.setCurrentStage(DealStage.STAGE_1);
        when(dealRepository.findById(1L)).thenReturn(Optional.of(deal));

        CreateActivityReportRequest req = createValidRequest(ActivityType.FIELD_VISIT);
        req.setContactId(null);
        req.setNewContact(null);

        assertThrows(BusinessRuleViolationException.class, () ->
                service.submitReport(1L, req, 100L));
    }

    @Test
    void submitReport_contactIdNotFound_throws() {
        deal.setCurrentStage(DealStage.STAGE_1);
        when(dealRepository.findById(1L)).thenReturn(Optional.of(deal));
        when(contactRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                service.submitReport(1L, createValidRequest(ActivityType.FIELD_VISIT), 100L));
    }

    @Test
    void getReport_wrongDealId_throws() {
        Deal otherDeal = new Deal();
        otherDeal.setId(999L);

        ActivityReport report = new ActivityReport();
        report.setId(5L);
        report.setDeal(otherDeal);

        when(activityReportRepository.findById(5L)).thenReturn(Optional.of(report));

        assertThrows(ResourceNotFoundException.class, () ->
                service.getReport(1L, 5L));
    }

    // === Helper ===

    private CreateActivityReportRequest createValidRequestWithData(ActivityType type, Map<String, Object> data) {
        CreateActivityReportRequest req = createValidRequest(type);
        req.setPhaseSpecificData(data);
        return req;
    }
}
