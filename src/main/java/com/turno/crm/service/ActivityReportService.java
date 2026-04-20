package com.turno.crm.service;

import com.turno.crm.exception.BusinessRuleViolationException;
import com.turno.crm.exception.ResourceNotFoundException;
import com.turno.crm.model.dto.*;
import com.turno.crm.model.entity.*;
import com.turno.crm.model.enums.*;
import com.turno.crm.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.*;

@Service
@Transactional
public class ActivityReportService {

    private final ActivityReportRepository activityReportRepository;
    private final DealRepository dealRepository;
    private final ContactRepository contactRepository;
    private final AttachmentRepository attachmentRepository;
    private final SystemReminderRepository systemReminderRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;
    private final NotificationService notificationService;
    private final TaskService taskService;

    // Template required fields map
    private static final Map<TemplateType, List<String>> TEMPLATE_REQUIRED_FIELDS = Map.ofEntries(
            Map.entry(TemplateType.T1, List.of("operatorType", "currentFleetSize", "currentVehicleType", "primaryUseCase", "interestLevel", "decisionMakerMet")),
            Map.entry(TemplateType.T2, List.of("interestLevelUpdate", "decisionMakerAccess")),
            Map.entry(TemplateType.T3, List.of("operatorType", "interestLevel", "followUpVisitNeeded")),
            Map.entry(TemplateType.T4, List.of("interestLevelUpdate", "decisionMakerIdentified")),
            Map.entry(TemplateType.T5, List.of("routesAssessed", "dailyKmPerVehicle", "depotChargingFeasibility", "peakFleetRequirement", "keyPainPoints")),
            Map.entry(TemplateType.T6, List.of("routeViabilityConfirmed", "negotiationStatus", "operatorDecisionTimeline")),
            Map.entry(TemplateType.T7, List.of("routeDataReceived", "keyRequirementsDiscussed", "siteVisitScheduled")),
            Map.entry(TemplateType.T8, List.of("operatorDecisionTimeline", "pricingApprovalStatus")),
            Map.entry(TemplateType.T9, List.of("commitmentStatus", "documentsCollected", "documentsPending", "preferredDeliveryTimeline", "vehicleSpecsConfirmed", "signingTimeline")),
            Map.entry(TemplateType.T10, List.of("documentsCollectedThisVisit", "documentsStillPending", "signingStatus", "deliveryTimelineUpdate")),
            Map.entry(TemplateType.T11, List.of("commitmentReconfirmed", "documentsPending", "deliveryCoordinationStarted")),
            Map.entry(TemplateType.T12, List.of("documentSubmissionStatus", "signingStatus"))
    );

    // Template display labels
    private static final Map<TemplateType, String> TEMPLATE_LABELS = Map.ofEntries(
            Map.entry(TemplateType.T1, "Early Phase - First Field Visit"),
            Map.entry(TemplateType.T2, "Early Phase - Follow-up Field Visit"),
            Map.entry(TemplateType.T3, "Early Phase - First Virtual"),
            Map.entry(TemplateType.T4, "Early Phase - Follow-up Virtual"),
            Map.entry(TemplateType.T5, "Commercial Phase - First Field Visit"),
            Map.entry(TemplateType.T6, "Commercial Phase - Follow-up Field Visit"),
            Map.entry(TemplateType.T7, "Commercial Phase - First Virtual"),
            Map.entry(TemplateType.T8, "Commercial Phase - Follow-up Virtual"),
            Map.entry(TemplateType.T9, "Closure Phase - First Field Visit"),
            Map.entry(TemplateType.T10, "Closure Phase - Follow-up Field Visit"),
            Map.entry(TemplateType.T11, "Closure Phase - First Virtual"),
            Map.entry(TemplateType.T12, "Closure Phase - Follow-up Virtual")
    );

    public ActivityReportService(ActivityReportRepository activityReportRepository,
                                  DealRepository dealRepository,
                                  ContactRepository contactRepository,
                                  AttachmentRepository attachmentRepository,
                                  SystemReminderRepository systemReminderRepository,
                                  UserRepository userRepository,
                                  AuditService auditService,
                                  NotificationService notificationService,
                                  TaskService taskService) {
        this.activityReportRepository = activityReportRepository;
        this.dealRepository = dealRepository;
        this.contactRepository = contactRepository;
        this.attachmentRepository = attachmentRepository;
        this.systemReminderRepository = systemReminderRepository;
        this.userRepository = userRepository;
        this.auditService = auditService;
        this.notificationService = notificationService;
        this.taskService = taskService;
    }

    public ActivityReportResponse submitReport(Long dealId, CreateActivityReportRequest request, Long actorId) {
        Deal deal = dealRepository.findById(dealId)
                .orElseThrow(() -> new ResourceNotFoundException("Deal", dealId));

        if (deal.getStatus() != DealStatus.ACTIVE) {
            throw new BusinessRuleViolationException("Reports can only be submitted for active deals");
        }

        // Resolve contact
        Contact contact = resolveContact(deal, request);

        // Validate contact has phone or email
        if ((contact.getMobile() == null || contact.getMobile().isBlank())
                && (contact.getEmail() == null || contact.getEmail().isBlank())) {
            throw new BusinessRuleViolationException("Contact must have at least one of mobile or email");
        }

        // Determine template type automatically
        DealStage.Phase phase = deal.getCurrentStage().getPhase();
        boolean isFirst = isFirstInPhase(dealId, phase);
        TemplateType templateType = determineTemplate(phase, request.getActivityType(), isFirst);

        // Validate phase-specific data
        validatePhaseSpecificData(templateType, request.getPhaseSpecificData());

        // Create report
        ActivityReport report = new ActivityReport();
        report.setDeal(deal);

        User agent = new User();
        agent.setId(actorId);
        report.setAgent(agent);

        report.setTemplateType(templateType);
        report.setActivityType(request.getActivityType());
        report.setInteractionDatetime(request.getInteractionDatetime());
        report.setSubmissionDatetime(OffsetDateTime.now());
        report.setContact(contact);
        report.setContactRole(request.getContactRole());
        report.setDuration(request.getDuration());
        report.setPhaseSpecificData(request.getPhaseSpecificData() != null ? request.getPhaseSpecificData() : new HashMap<>());
        report.setBuyingSignals(request.getBuyingSignals());
        report.setObjections(request.getObjections());
        report.setNotes(request.getNotes());
        report.setNextAction(request.getNextAction());
        report.setNextActionEta(request.getNextActionEta());
        report.setNextActionOwner(request.getNextActionOwner());
        report.setIsFirstInPhase(isFirst);
        report.setLoggedAtStage(deal.getCurrentStage());

        // Handle attachments
        if (request.getAttachments() != null && !request.getAttachments().isEmpty()) {
            for (AttachmentRequest ar : request.getAttachments()) {
                Attachment attachment = new Attachment();
                attachment.setActivityReport(report);
                attachment.setFileName(ar.getFileName());
                attachment.setFileKey(ar.getFileKey());
                attachment.setFileSize(ar.getFileSize());
                attachment.setCategoryTag(ar.getCategoryTag());
                report.getAttachments().add(attachment);
            }
        }

        report = activityReportRepository.save(report);

        // Handle T6/T8 report-to-substatus sync
        handleSubStatusSync(deal, templateType, request.getPhaseSpecificData());

        // Dismiss system reminders for this deal
        dismissSystemReminders(dealId);

        auditService.log(AuditEntityType.ACTIVITY_REPORT, report.getId(), AuditAction.SUBMIT, actorId,
                Map.of("dealId", dealId, "templateType", templateType.name()));

        // Auto-create task from next action
        if (report.getNextAction() != null && !report.getNextAction().isBlank()
                && !"No further action".equals(report.getNextAction())) {
            taskService.autoCreateFromReport(report, deal);
        }

        return toResponse(report);
    }

    public ActivityReportResponse voidReport(Long dealId, Long reportId, String reason, Long actorId) {
        ActivityReport report = findReportForDeal(dealId, reportId);

        if (report.getVoided()) {
            throw new BusinessRuleViolationException("Report is already voided");
        }

        User voidedBy = new User();
        voidedBy.setId(actorId);

        report.setVoided(true);
        report.setVoidedBy(voidedBy);
        report.setVoidedReason(reason);
        report.setVoidedAt(OffsetDateTime.now());
        report = activityReportRepository.save(report);

        auditService.log(AuditEntityType.ACTIVITY_REPORT, reportId, AuditAction.VOID, actorId,
                Map.of("dealId", dealId, "reason", reason));

        // Check if there are no remaining active (non-voided) reports with next action
        Deal deal = report.getDeal();
        boolean hasActiveNextAction = activityReportRepository
                .findFirstByDealIdAndVoidedFalseOrderBySubmissionDatetimeDesc(dealId)
                .isPresent();
        if (!hasActiveNextAction && deal.getStatus() == DealStatus.ACTIVE) {
            notificationService.fireNoNextActionSet(deal, deal.getAssignedAgent());
        }

        return toResponse(report);
    }

    public ActivityReportResponse unvoidReport(Long dealId, Long reportId, Long actorId) {
        ActivityReport report = findReportForDeal(dealId, reportId);

        if (!report.getVoided()) {
            throw new BusinessRuleViolationException("Report is not voided");
        }

        User unvoidedBy = new User();
        unvoidedBy.setId(actorId);

        report.setVoided(false);
        report.setUnvoidedBy(unvoidedBy);
        report.setUnvoidedAt(OffsetDateTime.now());
        report = activityReportRepository.save(report);

        auditService.log(AuditEntityType.ACTIVITY_REPORT, reportId, AuditAction.UNVOID, actorId,
                Map.of("dealId", dealId));

        return toResponse(report);
    }

    @Transactional(readOnly = true)
    public Page<ActivityReportResponse> listReports(Long dealId, boolean includeVoided, Pageable pageable) {
        if (includeVoided) {
            return activityReportRepository.findByDealIdOrderBySubmissionDatetimeDesc(dealId, pageable)
                    .map(this::toResponse);
        }
        // For non-voided, we use the full list and filter
        return activityReportRepository.findByDealIdOrderBySubmissionDatetimeDesc(dealId, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public ActivityReportResponse getReport(Long dealId, Long reportId) {
        ActivityReport report = findReportForDeal(dealId, reportId);
        return toResponse(report);
    }

    public ActivityReportResponse updateReport(Long dealId, Long reportId,
                                                UpdateActivityReportRequest request, Long actorId) {
        ActivityReport report = findReportForDeal(dealId, reportId);

        Map<String, Object> changes = new HashMap<>();

        if (request.getNextActionEta() != null) {
            String oldEta = report.getNextActionEta() != null ? report.getNextActionEta().toString() : "null";
            report.setNextActionEta(request.getNextActionEta());
            changes.put("nextActionEta", Map.of("from", oldEta, "to", request.getNextActionEta().toString()));
        }

        report = activityReportRepository.save(report);

        auditService.log(AuditEntityType.ACTIVITY_REPORT, report.getId(), AuditAction.UPDATE, actorId, changes);

        return toResponse(report);
    }

    // --- Template selection logic ---

    TemplateType determineTemplate(DealStage.Phase phase, ActivityType activityType, boolean isFirst) {
        boolean isField = activityType == ActivityType.FIELD_VISIT;
        return switch (phase) {
            case EARLY -> isFirst
                    ? (isField ? TemplateType.T1 : TemplateType.T3)
                    : (isField ? TemplateType.T2 : TemplateType.T4);
            case COMMERCIAL -> isFirst
                    ? (isField ? TemplateType.T5 : TemplateType.T7)
                    : (isField ? TemplateType.T6 : TemplateType.T8);
            case CLOSURE -> isFirst
                    ? (isField ? TemplateType.T9 : TemplateType.T11)
                    : (isField ? TemplateType.T10 : TemplateType.T12);
        };
    }

    private boolean isFirstInPhase(Long dealId, DealStage.Phase phase) {
        List<TemplateType> phaseTemplates = getTemplateTypesForPhase(phase);
        return !activityReportRepository.existsNonVoidedReportWithTemplateTypes(dealId, phaseTemplates);
    }

    private List<TemplateType> getTemplateTypesForPhase(DealStage.Phase phase) {
        return switch (phase) {
            case EARLY -> List.of(TemplateType.T1, TemplateType.T2, TemplateType.T3, TemplateType.T4);
            case COMMERCIAL -> List.of(TemplateType.T5, TemplateType.T6, TemplateType.T7, TemplateType.T8);
            case CLOSURE -> List.of(TemplateType.T9, TemplateType.T10, TemplateType.T11, TemplateType.T12);
        };
    }

    private void validatePhaseSpecificData(TemplateType templateType, Map<String, Object> data) {
        List<String> requiredFields = TEMPLATE_REQUIRED_FIELDS.get(templateType);
        if (requiredFields == null) return;

        if (data == null || data.isEmpty()) {
            throw new BusinessRuleViolationException(
                    "Phase-specific data is required for template " + templateType.name()
                            + ". Missing fields: " + String.join(", ", requiredFields));
        }

        List<String> missing = requiredFields.stream()
                .filter(f -> !data.containsKey(f) || data.get(f) == null)
                .toList();

        if (!missing.isEmpty()) {
            throw new BusinessRuleViolationException(
                    "Missing required fields for template " + templateType.name() + ": " + String.join(", ", missing));
        }
    }

    private Contact resolveContact(Deal deal, CreateActivityReportRequest request) {
        if (request.getContactId() != null) {
            return contactRepository.findById(request.getContactId())
                    .orElseThrow(() -> new ResourceNotFoundException("Contact", request.getContactId()));
        }

        if (request.getNewContact() != null) {
            CreateContactRequest nc = request.getNewContact();
            if (!nc.hasAtLeastOneContactMethod()) {
                throw new BusinessRuleViolationException("New contact must have at least one of mobile or email");
            }
            Contact contact = new Contact();
            contact.setOperator(deal.getOperator());
            contact.setName(nc.getName());
            contact.setRole(nc.getRole());
            contact.setMobile(nc.getMobile());
            contact.setEmail(nc.getEmail());
            return contactRepository.save(contact);
        }

        throw new BusinessRuleViolationException("Either contactId or newContact must be provided");
    }

    private void handleSubStatusSync(Deal deal, TemplateType templateType, Map<String, Object> data) {
        if (deal.getCurrentStage() != DealStage.STAGE_4 || data == null) return;

        // T6: negotiationStatus == "AWAITING_APPROVAL" syncs to sub-status
        if (templateType == TemplateType.T6) {
            Object negotiationStatus = data.get("negotiationStatus");
            if ("Awaiting approval".equals(negotiationStatus) || "AWAITING_APPROVAL".equals(negotiationStatus)) {
                deal.setSubStatus(Stage5SubStatus.AWAITING_APPROVAL);
                dealRepository.save(deal);
            }
        }

        // T8: pricingApprovalStatus == "Submitted" syncs to sub-status
        if (templateType == TemplateType.T8) {
            Object pricingStatus = data.get("pricingApprovalStatus");
            if ("Submitted".equals(pricingStatus) || "SUBMITTED".equals(pricingStatus)) {
                deal.setSubStatus(Stage5SubStatus.AWAITING_APPROVAL);
                dealRepository.save(deal);
            }
        }
    }

    private void dismissSystemReminders(Long dealId) {
        List<SystemReminder> reminders = systemReminderRepository.findByDealIdAndDismissedFalse(dealId);
        for (SystemReminder reminder : reminders) {
            reminder.setDismissed(true);
            reminder.setDismissedAt(OffsetDateTime.now());
        }
        if (!reminders.isEmpty()) {
            systemReminderRepository.saveAll(reminders);
        }
    }

    private ActivityReport findReportForDeal(Long dealId, Long reportId) {
        ActivityReport report = activityReportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("ActivityReport", reportId));
        if (!report.getDeal().getId().equals(dealId)) {
            throw new ResourceNotFoundException("ActivityReport", reportId);
        }
        return report;
    }

    ActivityReportResponse toResponse(ActivityReport report) {
        ActivityReportResponse resp = new ActivityReportResponse();
        resp.setId(report.getId());
        resp.setDealId(report.getDeal().getId());
        resp.setAgent(new UserSummary(report.getAgent().getId(), report.getAgent().getName()));
        resp.setTemplateType(report.getTemplateType());
        resp.setTemplateLabel(TEMPLATE_LABELS.getOrDefault(report.getTemplateType(), report.getTemplateType().name()));
        resp.setActivityType(report.getActivityType());
        resp.setInteractionDatetime(report.getInteractionDatetime());
        resp.setSubmissionDatetime(report.getSubmissionDatetime());
        resp.setContact(new ContactSummary(
                report.getContact().getId(),
                report.getContact().getName(),
                report.getContact().getMobile(),
                report.getContact().getEmail()));
        resp.setContactRole(report.getContactRole());
        resp.setDuration(report.getDuration());
        resp.setPhaseSpecificData(report.getPhaseSpecificData());
        resp.setBuyingSignals(report.getBuyingSignals());
        resp.setObjections(report.getObjections());
        resp.setNotes(report.getNotes());
        resp.setNextAction(report.getNextAction());
        resp.setNextActionEta(report.getNextActionEta());
        resp.setNextActionOwner(report.getNextActionOwner());
        resp.setVoided(report.getVoided() != null && report.getVoided());
        if (report.getVoidedBy() != null) {
            resp.setVoidedBy(new UserSummary(report.getVoidedBy().getId(), report.getVoidedBy().getName()));
        }
        resp.setVoidedReason(report.getVoidedReason());
        resp.setVoidedAt(report.getVoidedAt());
        if (report.getUnvoidedBy() != null) {
            resp.setUnvoidedBy(new UserSummary(report.getUnvoidedBy().getId(), report.getUnvoidedBy().getName()));
        }
        resp.setUnvoidedAt(report.getUnvoidedAt());
        resp.setFirstInPhase(report.getIsFirstInPhase() != null && report.getIsFirstInPhase());
        resp.setLoggedAtStage(report.getLoggedAtStage() != null ? report.getLoggedAtStage().name() : null);
        resp.setCreatedAt(report.getCreatedAt());

        // Map attachments
        if (report.getAttachments() != null) {
            resp.setAttachments(report.getAttachments().stream().map(a -> {
                AttachmentResponse ar = new AttachmentResponse();
                ar.setId(a.getId());
                ar.setFileName(a.getFileName());
                ar.setFileKey(a.getFileKey());
                ar.setFileSize(a.getFileSize());
                ar.setCategoryTag(a.getCategoryTag());
                ar.setUploadedAt(a.getUploadedAt());
                return ar;
            }).toList());
        }

        return resp;
    }
}
