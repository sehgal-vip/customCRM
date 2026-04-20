package com.turno.crm.service;

import com.turno.crm.exception.ExitCriteriaNotMetException.CriteriaResult;
import com.turno.crm.model.entity.Contact;
import com.turno.crm.model.entity.Deal;
import com.turno.crm.model.entity.Operator;
import com.turno.crm.model.enums.ActivityType;
import com.turno.crm.model.enums.DealStage;
import com.turno.crm.model.enums.LeadSource;
import com.turno.crm.model.enums.Stage5SubStatus;
import com.turno.crm.repository.ActivityReportRepository;
import com.turno.crm.repository.AdminSettingRepository;
import com.turno.crm.repository.DealDocumentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExitCriteriaValidatorTest {

    @Mock
    private ActivityReportRepository activityReportRepository;

    @Mock
    private DealDocumentRepository dealDocumentRepository;

    @Mock
    private AdminSettingRepository adminSettingRepository;

    @InjectMocks
    private ExitCriteriaValidator validator;

    // --- Helper methods ---

    private Deal createDealAtStage(DealStage stage) {
        Deal deal = new Deal();
        deal.setId(1L);
        deal.setCurrentStage(stage);
        return deal;
    }

    private Operator createOperatorWithContact(String companyName, String phone, String email) {
        Operator operator = new Operator();
        operator.setCompanyName(companyName);
        List<Contact> contacts = new ArrayList<>();
        Contact contact = new Contact();
        contact.setMobile(phone);
        contact.setEmail(email);
        contacts.add(contact);
        operator.setContacts(contacts);
        return operator;
    }

    // === Stage 1 Tests ===

    @Test
    void stage1_allCriteriaMet_returnsAllMet() {
        Deal deal = createDealAtStage(DealStage.STAGE_1);
        deal.setOperator(createOperatorWithContact("Turno Ltd", "+27123456789", "info@turno.co"));
        deal.setFleetSize(10);
        deal.setLeadSource(LeadSource.AGENT_FIELD);

        List<CriteriaResult> results = validator.validateForward(deal);

        assertTrue(results.stream().allMatch(CriteriaResult::met));
        assertEquals(4, results.size());
    }

    @Test
    void stage1_missingCompanyName_returnsNotMet() {
        Deal deal = createDealAtStage(DealStage.STAGE_1);
        Operator op = createOperatorWithContact(null, "+27123456789", null);
        op.setCompanyName(null);
        deal.setOperator(op);
        deal.setFleetSize(10);
        deal.setLeadSource(LeadSource.AGENT_FIELD);

        List<CriteriaResult> results = validator.validateForward(deal);

        assertFalse(results.get(0).met());
        assertEquals("operator_company_name", results.get(0).rule());
    }

    @Test
    void stage1_blankCompanyName_returnsNotMet() {
        Deal deal = createDealAtStage(DealStage.STAGE_1);
        Operator op = createOperatorWithContact("   ", "+27123456789", null);
        deal.setOperator(op);
        deal.setFleetSize(10);
        deal.setLeadSource(LeadSource.AGENT_FIELD);

        List<CriteriaResult> results = validator.validateForward(deal);

        assertFalse(results.get(0).met());
    }

    @Test
    void stage1_missingFleetSize_returnsNotMet() {
        Deal deal = createDealAtStage(DealStage.STAGE_1);
        deal.setOperator(createOperatorWithContact("Turno Ltd", "+27123456789", null));
        deal.setFleetSize(null);
        deal.setLeadSource(LeadSource.AGENT_FIELD);

        List<CriteriaResult> results = validator.validateForward(deal);

        CriteriaResult fleetResult = results.stream()
                .filter(r -> r.rule().equals("fleet_size_set"))
                .findFirst().orElseThrow();
        assertFalse(fleetResult.met());
    }

    @Test
    void stage1_zeroFleetSize_returnsNotMet() {
        Deal deal = createDealAtStage(DealStage.STAGE_1);
        deal.setOperator(createOperatorWithContact("Turno Ltd", "+27123456789", null));
        deal.setFleetSize(0);
        deal.setLeadSource(LeadSource.AGENT_FIELD);

        List<CriteriaResult> results = validator.validateForward(deal);

        CriteriaResult fleetResult = results.stream()
                .filter(r -> r.rule().equals("fleet_size_set"))
                .findFirst().orElseThrow();
        assertFalse(fleetResult.met());
    }

    @Test
    void stage1_missingLeadSource_returnsNotMet() {
        Deal deal = createDealAtStage(DealStage.STAGE_1);
        deal.setOperator(createOperatorWithContact("Turno Ltd", "+27123456789", null));
        deal.setFleetSize(10);
        deal.setLeadSource(null);

        List<CriteriaResult> results = validator.validateForward(deal);

        CriteriaResult leadResult = results.stream()
                .filter(r -> r.rule().equals("lead_source_set"))
                .findFirst().orElseThrow();
        assertFalse(leadResult.met());
    }

    @Test
    void stage1_contactWithOnlyEmail_passes() {
        Deal deal = createDealAtStage(DealStage.STAGE_1);
        deal.setOperator(createOperatorWithContact("Turno Ltd", null, "test@test.com"));
        deal.setFleetSize(10);
        deal.setLeadSource(LeadSource.INBOUND);

        List<CriteriaResult> results = validator.validateForward(deal);

        CriteriaResult contactResult = results.stream()
                .filter(r -> r.rule().equals("contact_with_phone_or_email"))
                .findFirst().orElseThrow();
        assertTrue(contactResult.met());
    }

    @Test
    void stage1_contactWithoutPhoneOrEmail_fails() {
        Deal deal = createDealAtStage(DealStage.STAGE_1);
        deal.setOperator(createOperatorWithContact("Turno Ltd", null, null));
        deal.setFleetSize(10);
        deal.setLeadSource(LeadSource.INBOUND);

        List<CriteriaResult> results = validator.validateForward(deal);

        CriteriaResult contactResult = results.stream()
                .filter(r -> r.rule().equals("contact_with_phone_or_email"))
                .findFirst().orElseThrow();
        assertFalse(contactResult.met());
    }

    @Test
    void stage1_nullOperator_contactFails() {
        Deal deal = createDealAtStage(DealStage.STAGE_1);
        deal.setOperator(null);
        deal.setFleetSize(10);
        deal.setLeadSource(LeadSource.INBOUND);

        List<CriteriaResult> results = validator.validateForward(deal);

        assertFalse(results.get(0).met()); // company name
        assertFalse(results.get(1).met()); // contact
    }

    // === Stage 2 Tests ===

    @Test
    void stage2_withNonVoidedReport_passes() {
        Deal deal = createDealAtStage(DealStage.STAGE_2);
        when(activityReportRepository.countByDealIdAndLoggedAtStageAndVoidedFalse(1L, DealStage.STAGE_2)).thenReturn(1L);

        List<CriteriaResult> results = validator.validateForward(deal);

        assertTrue(results.get(0).met());
    }

    @Test
    void stage2_noReports_fails() {
        Deal deal = createDealAtStage(DealStage.STAGE_2);
        when(activityReportRepository.countByDealIdAndLoggedAtStageAndVoidedFalse(1L, DealStage.STAGE_2)).thenReturn(0L);

        List<CriteriaResult> results = validator.validateForward(deal);

        assertFalse(results.get(0).met());
    }

    // === Stage 3 Tests (Qualified Lead — field visit criteria) ===

    @Test
    void stage3_requiresFieldVisit_virtualNotEnough() {
        Deal deal = createDealAtStage(DealStage.STAGE_3);
        when(activityReportRepository.countByDealIdAndLoggedAtStageAndActivityTypeAndVoidedFalse(1L, DealStage.STAGE_3, ActivityType.FIELD_VISIT)).thenReturn(0L);

        List<CriteriaResult> results = validator.validateForward(deal);

        assertFalse(results.get(0).met());
        assertEquals("min_one_field_visit_report", results.get(0).rule());
    }

    @Test
    void stage3_withFieldVisitReport_passes() {
        Deal deal = createDealAtStage(DealStage.STAGE_3);
        when(activityReportRepository.countByDealIdAndLoggedAtStageAndActivityTypeAndVoidedFalse(1L, DealStage.STAGE_3, ActivityType.FIELD_VISIT)).thenReturn(1L);

        List<CriteriaResult> results = validator.validateForward(deal);

        assertTrue(results.get(0).met());
    }

    // === Stage 4 Tests (Closure of Commercials — sub-status criteria) ===

    @Test
    void stage4_requiresNegotiatingSubStatus() {
        Deal deal = createDealAtStage(DealStage.STAGE_4);
        deal.setSubStatus(Stage5SubStatus.PROPOSAL_SENT);

        List<CriteriaResult> results = validator.validateForward(deal);

        assertFalse(results.get(0).met());
        assertEquals("sub_status_negotiating", results.get(0).rule());
    }

    @Test
    void stage4_negotiatingSubStatus_passes() {
        Deal deal = createDealAtStage(DealStage.STAGE_4);
        deal.setSubStatus(Stage5SubStatus.NEGOTIATING);

        List<CriteriaResult> results = validator.validateForward(deal);

        assertTrue(results.get(0).met());
    }

    @Test
    void stage4_awaitingApprovalSubStatus_fails() {
        Deal deal = createDealAtStage(DealStage.STAGE_4);
        deal.setSubStatus(Stage5SubStatus.AWAITING_APPROVAL);

        List<CriteriaResult> results = validator.validateForward(deal);

        assertFalse(results.get(0).met());
    }

    // === Stage 5 Tests (Token Received) ===

    @Test
    void stage5_withReport_passes() {
        Deal deal = createDealAtStage(DealStage.STAGE_5);
        when(activityReportRepository.countByDealIdAndLoggedAtStageAndVoidedFalse(1L, DealStage.STAGE_5)).thenReturn(1L);

        List<CriteriaResult> results = validator.validateForward(deal);

        assertTrue(results.get(0).met());
    }

    // === Stage 6 Tests (Documentation — soft block) ===

    @Test
    void stage6_allMandatoryDocsComplete_passes() {
        Deal deal = createDealAtStage(DealStage.STAGE_6);
        when(dealDocumentRepository.countMandatoryTotal(1L)).thenReturn(5L);
        when(dealDocumentRepository.countMandatoryComplete(1L)).thenReturn(5L);

        List<CriteriaResult> results = validator.validateForward(deal);

        assertTrue(results.get(0).met());
    }

    @Test
    void stage6_missingMandatoryDocs_isSoftBlock() {
        Deal deal = createDealAtStage(DealStage.STAGE_6);
        when(dealDocumentRepository.countMandatoryTotal(1L)).thenReturn(5L);
        when(dealDocumentRepository.countMandatoryComplete(1L)).thenReturn(3L);

        List<CriteriaResult> results = validator.validateForward(deal);

        assertFalse(results.get(0).met());
        assertTrue(results.get(0).softBlock(), "Stage 6 should be a soft block");
    }

    @Test
    void stage6_noMandatoryDocs_passes() {
        Deal deal = createDealAtStage(DealStage.STAGE_6);
        when(dealDocumentRepository.countMandatoryTotal(1L)).thenReturn(0L);
        when(dealDocumentRepository.countMandatoryComplete(1L)).thenReturn(0L);

        List<CriteriaResult> results = validator.validateForward(deal);

        assertTrue(results.get(0).met());
    }

    // === Stage 7 Tests (Lease Signing — hard block) ===

    @Test
    void stage7_allMandatoryDocsComplete_passes() {
        Deal deal = createDealAtStage(DealStage.STAGE_7);
        when(dealDocumentRepository.countMandatoryTotal(1L)).thenReturn(5L);
        when(dealDocumentRepository.countMandatoryComplete(1L)).thenReturn(5L);

        List<CriteriaResult> results = validator.validateForward(deal);

        assertTrue(results.get(0).met());
    }

    @Test
    void stage7_missingMandatoryDocs_isHardBlock() {
        Deal deal = createDealAtStage(DealStage.STAGE_7);
        when(dealDocumentRepository.countMandatoryTotal(1L)).thenReturn(5L);
        when(dealDocumentRepository.countMandatoryComplete(1L)).thenReturn(3L);

        List<CriteriaResult> results = validator.validateForward(deal);

        assertFalse(results.get(0).met());
        assertFalse(results.get(0).softBlock(), "Stage 7 should be a hard block (not soft)");
    }

    // === Stage 8 Tests (Vehicle Delivery) ===

    @Test
    void stage8_withReport_passes() {
        Deal deal = createDealAtStage(DealStage.STAGE_8);
        when(activityReportRepository.countByDealIdAndLoggedAtStageAndVoidedFalse(1L, DealStage.STAGE_8)).thenReturn(1L);

        List<CriteriaResult> results = validator.validateForward(deal);

        assertTrue(results.get(0).met());
    }

    @Test
    void stage8_noReport_fails() {
        Deal deal = createDealAtStage(DealStage.STAGE_8);
        when(activityReportRepository.countByDealIdAndLoggedAtStageAndVoidedFalse(1L, DealStage.STAGE_8)).thenReturn(0L);

        List<CriteriaResult> results = validator.validateForward(deal);

        assertFalse(results.get(0).met());
    }

    // === Voided reports tests ===

    @Test
    void voidedReports_dontCountTowardStage2Criteria() {
        Deal deal = createDealAtStage(DealStage.STAGE_2);
        // countByDealIdAndLoggedAtStageAndVoidedFalse returns 0 even though voided reports may exist
        when(activityReportRepository.countByDealIdAndLoggedAtStageAndVoidedFalse(1L, DealStage.STAGE_2)).thenReturn(0L);

        List<CriteriaResult> results = validator.validateForward(deal);

        assertFalse(results.get(0).met());
    }

    @Test
    void voidedFieldVisits_dontCountTowardStage3Criteria() {
        Deal deal = createDealAtStage(DealStage.STAGE_3);
        // Only non-voided field visits at STAGE_3 count
        when(activityReportRepository.countByDealIdAndLoggedAtStageAndActivityTypeAndVoidedFalse(1L, DealStage.STAGE_3, ActivityType.FIELD_VISIT)).thenReturn(0L);

        List<CriteriaResult> results = validator.validateForward(deal);

        assertFalse(results.get(0).met());
    }

    // === Stage-specific logging tests ===

    @Test
    void stage2_reportAtStage1_criteriaNotMet() {
        Deal deal = createDealAtStage(DealStage.STAGE_2);
        // Report exists but was logged at STAGE_1, not STAGE_2
        when(activityReportRepository.countByDealIdAndLoggedAtStageAndVoidedFalse(1L, DealStage.STAGE_2)).thenReturn(0L);

        List<CriteriaResult> results = validator.validateForward(deal);

        assertFalse(results.get(0).met());
    }

    @Test
    void stage2_reportAtStage2_criteriaMet() {
        Deal deal = createDealAtStage(DealStage.STAGE_2);
        // Report logged at STAGE_2
        when(activityReportRepository.countByDealIdAndLoggedAtStageAndVoidedFalse(1L, DealStage.STAGE_2)).thenReturn(1L);

        List<CriteriaResult> results = validator.validateForward(deal);

        assertTrue(results.get(0).met());
    }

    @Test
    void stage3_fieldVisitAtStage2_criteriaNotMet() {
        Deal deal = createDealAtStage(DealStage.STAGE_3);
        // Field visit exists but was logged at STAGE_2, not STAGE_3
        when(activityReportRepository.countByDealIdAndLoggedAtStageAndActivityTypeAndVoidedFalse(1L, DealStage.STAGE_3, ActivityType.FIELD_VISIT)).thenReturn(0L);

        List<CriteriaResult> results = validator.validateForward(deal);

        assertFalse(results.get(0).met());
    }

    @Test
    void stage3_fieldVisitAtStage3_criteriaMet() {
        Deal deal = createDealAtStage(DealStage.STAGE_3);
        // Field visit logged at STAGE_3
        when(activityReportRepository.countByDealIdAndLoggedAtStageAndActivityTypeAndVoidedFalse(1L, DealStage.STAGE_3, ActivityType.FIELD_VISIT)).thenReturn(1L);

        List<CriteriaResult> results = validator.validateForward(deal);

        assertTrue(results.get(0).met());
    }
}
