package com.turno.crm.service;

import com.turno.crm.exception.ExitCriteriaNotMetException.CriteriaResult;
import com.turno.crm.model.entity.Contact;
import com.turno.crm.model.entity.Deal;
import com.turno.crm.model.enums.ActivityType;
import com.turno.crm.model.enums.DealStage;
import com.turno.crm.model.enums.DocStatus;
import com.turno.crm.model.enums.Stage5SubStatus;
import com.turno.crm.model.enums.TemplateType;
import com.turno.crm.model.enums.AdminSettingType;
import com.turno.crm.repository.ActivityReportRepository;
import com.turno.crm.repository.AdminSettingRepository;
import com.turno.crm.repository.DealDocumentRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ExitCriteriaValidator {

    private final ActivityReportRepository activityReportRepository;
    private final DealDocumentRepository dealDocumentRepository;
    private final AdminSettingRepository adminSettingRepository;

    public ExitCriteriaValidator(ActivityReportRepository activityReportRepository,
                                  DealDocumentRepository dealDocumentRepository,
                                  AdminSettingRepository adminSettingRepository) {
        this.activityReportRepository = activityReportRepository;
        this.dealDocumentRepository = dealDocumentRepository;
        this.adminSettingRepository = adminSettingRepository;
    }

    private boolean isActivityRequired(DealStage stage) {
        return adminSettingRepository
                .findBySettingTypeAndSettingKey(AdminSettingType.EXIT_CRITERIA, stage.name())
                .map(setting -> {
                    Object val = setting.getSettingValue().get("activityRequired");
                    return val instanceof Boolean ? (Boolean) val : true;
                })
                .orElse(true); // default: required
    }

    public List<CriteriaResult> validateForward(Deal deal) {
        return switch (deal.getCurrentStage()) {
            case STAGE_1 -> validateStage1(deal);
            case STAGE_2 -> validateStage2(deal);
            case STAGE_3 -> validateStage3(deal);
            case STAGE_4 -> validateStage4(deal);
            case STAGE_5 -> validateStage5(deal);
            case STAGE_6 -> validateStage6(deal);
            case STAGE_7 -> validateStage7(deal);
            case STAGE_8 -> validateStage8(deal);
        };
    }

    private List<CriteriaResult> validateStage1(Deal deal) {
        List<CriteriaResult> results = new ArrayList<>();

        // Operator companyName non-null
        boolean hasCompanyName = deal.getOperator() != null
                && deal.getOperator().getCompanyName() != null
                && !deal.getOperator().getCompanyName().isBlank();
        results.add(new CriteriaResult("operator_company_name", hasCompanyName, false,
                hasCompanyName ? "Operator company name is set" : "Operator company name is required"));

        // At least 1 contact with phone or email
        boolean hasContact = false;
        if (deal.getOperator() != null && deal.getOperator().getContacts() != null) {
            hasContact = deal.getOperator().getContacts().stream()
                    .anyMatch(c -> (c.getMobile() != null && !c.getMobile().isBlank())
                            || (c.getEmail() != null && !c.getEmail().isBlank()));
        }
        results.add(new CriteriaResult("contact_with_phone_or_email", hasContact, false,
                hasContact ? "At least one contact with phone/email exists" : "At least one contact with phone or email is required"));

        // Fleet size set
        boolean hasFleetSize = deal.getFleetSize() != null && deal.getFleetSize() > 0;
        results.add(new CriteriaResult("fleet_size_set", hasFleetSize, false,
                hasFleetSize ? "Fleet size is set" : "Fleet size is required"));

        // Lead source set
        boolean hasLeadSource = deal.getLeadSource() != null;
        results.add(new CriteriaResult("lead_source_set", hasLeadSource, false,
                hasLeadSource ? "Lead source is set" : "Lead source is required"));

        return results;
    }

    private List<CriteriaResult> validateStage2(Deal deal) {
        List<CriteriaResult> results = new ArrayList<>();
        if (isActivityRequired(DealStage.STAGE_2)) {
            long count = activityReportRepository.countByDealIdAndLoggedAtStageAndVoidedFalse(deal.getId(), DealStage.STAGE_2);
            boolean met = count >= 1;
            results.add(new CriteriaResult("min_one_report", met, false,
                    met ? "At least one non-voided report exists at this stage" : "At least one activity report at this stage is required"));
        }
        return results;
    }

    private List<CriteriaResult> validateStage3(Deal deal) {
        List<CriteriaResult> results = new ArrayList<>();
        if (isActivityRequired(DealStage.STAGE_3)) {
            long count = activityReportRepository.countByDealIdAndLoggedAtStageAndActivityTypeAndVoidedFalse(deal.getId(), DealStage.STAGE_3, ActivityType.FIELD_VISIT);
            boolean met = count >= 1;
            results.add(new CriteriaResult("min_one_field_visit_report", met, false,
                    met ? "At least one non-voided field visit report exists at this stage" : "At least one field visit report at this stage is required"));
        }
        return results;
    }

    private List<CriteriaResult> validateStage4(Deal deal) {
        List<CriteriaResult> results = new ArrayList<>();
        boolean met = deal.getSubStatus() == Stage5SubStatus.NEGOTIATING;
        results.add(new CriteriaResult("sub_status_negotiating", met, false,
                met ? "Sub-status is NEGOTIATING" : "Deal sub-status must be NEGOTIATING to advance past Stage 4"));
        return results;
    }

    private List<CriteriaResult> validateStage5(Deal deal) {
        List<CriteriaResult> results = new ArrayList<>();
        if (isActivityRequired(DealStage.STAGE_5)) {
            long count = activityReportRepository.countByDealIdAndLoggedAtStageAndVoidedFalse(deal.getId(), DealStage.STAGE_5);
            boolean met = count >= 1;
            results.add(new CriteriaResult("min_one_report", met, false,
                    met ? "At least one non-voided report exists at this stage" : "At least one activity report at this stage is required"));
        }
        return results;
    }

    private List<CriteriaResult> validateStage6(Deal deal) {
        List<CriteriaResult> results = new ArrayList<>();
        long total = dealDocumentRepository.countMandatoryTotal(deal.getId());
        long complete = dealDocumentRepository.countMandatoryComplete(deal.getId());
        boolean met = total == 0 || complete >= total;
        // Soft block: can be overridden by manager
        results.add(new CriteriaResult("all_mandatory_docs_received", met, true,
                met ? "All mandatory documents are received or verified"
                        : "Not all mandatory documents are received/verified (" + complete + "/" + total + ")"));
        return results;
    }

    private List<CriteriaResult> validateStage7(Deal deal) {
        List<CriteriaResult> results = new ArrayList<>();
        long total = dealDocumentRepository.countMandatoryTotal(deal.getId());
        long complete = dealDocumentRepository.countMandatoryComplete(deal.getId());
        boolean met = total == 0 || complete >= total;
        // Hard block: no override
        results.add(new CriteriaResult("all_mandatory_docs_received", met, false,
                met ? "All mandatory documents are received or verified"
                        : "All mandatory documents must be received/verified (" + complete + "/" + total + ")"));
        return results;
    }

    private List<CriteriaResult> validateStage8(Deal deal) {
        List<CriteriaResult> results = new ArrayList<>();
        if (isActivityRequired(DealStage.STAGE_8)) {
            long count = activityReportRepository.countByDealIdAndLoggedAtStageAndVoidedFalse(deal.getId(), DealStage.STAGE_8);
            boolean met = count >= 1;
            results.add(new CriteriaResult("min_one_report", met, false,
                    met ? "At least one non-voided report exists at this stage" : "At least one activity report at this stage is required"));
        }
        return results;
    }
}
