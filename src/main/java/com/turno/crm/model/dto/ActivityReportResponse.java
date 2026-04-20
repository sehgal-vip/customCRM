package com.turno.crm.model.dto;

import com.turno.crm.model.enums.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

public class ActivityReportResponse {

    private Long id;
    private Long dealId;
    private UserSummary agent;
    private TemplateType templateType;
    private String templateLabel;
    private ActivityType activityType;
    private OffsetDateTime interactionDatetime;
    private OffsetDateTime submissionDatetime;
    private ContactSummary contact;
    private ContactRole contactRole;
    private ReportDuration duration;
    private Map<String, Object> phaseSpecificData;
    private List<String> buyingSignals;
    private List<String> objections;
    private String notes;
    private String nextAction;
    private LocalDate nextActionEta;
    private NextActionOwner nextActionOwner;
    private boolean voided;
    private UserSummary voidedBy;
    private String voidedReason;
    private OffsetDateTime voidedAt;
    private UserSummary unvoidedBy;
    private OffsetDateTime unvoidedAt;
    private boolean isFirstInPhase;
    private String loggedAtStage;
    private List<AttachmentResponse> attachments;
    private OffsetDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getDealId() { return dealId; }
    public void setDealId(Long dealId) { this.dealId = dealId; }

    public UserSummary getAgent() { return agent; }
    public void setAgent(UserSummary agent) { this.agent = agent; }

    public TemplateType getTemplateType() { return templateType; }
    public void setTemplateType(TemplateType templateType) { this.templateType = templateType; }

    public String getTemplateLabel() { return templateLabel; }
    public void setTemplateLabel(String templateLabel) { this.templateLabel = templateLabel; }

    public ActivityType getActivityType() { return activityType; }
    public void setActivityType(ActivityType activityType) { this.activityType = activityType; }

    public OffsetDateTime getInteractionDatetime() { return interactionDatetime; }
    public void setInteractionDatetime(OffsetDateTime interactionDatetime) { this.interactionDatetime = interactionDatetime; }

    public OffsetDateTime getSubmissionDatetime() { return submissionDatetime; }
    public void setSubmissionDatetime(OffsetDateTime submissionDatetime) { this.submissionDatetime = submissionDatetime; }

    public ContactSummary getContact() { return contact; }
    public void setContact(ContactSummary contact) { this.contact = contact; }

    public ContactRole getContactRole() { return contactRole; }
    public void setContactRole(ContactRole contactRole) { this.contactRole = contactRole; }

    public ReportDuration getDuration() { return duration; }
    public void setDuration(ReportDuration duration) { this.duration = duration; }

    public Map<String, Object> getPhaseSpecificData() { return phaseSpecificData; }
    public void setPhaseSpecificData(Map<String, Object> phaseSpecificData) { this.phaseSpecificData = phaseSpecificData; }

    public List<String> getBuyingSignals() { return buyingSignals; }
    public void setBuyingSignals(List<String> buyingSignals) { this.buyingSignals = buyingSignals; }

    public List<String> getObjections() { return objections; }
    public void setObjections(List<String> objections) { this.objections = objections; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getNextAction() { return nextAction; }
    public void setNextAction(String nextAction) { this.nextAction = nextAction; }

    public LocalDate getNextActionEta() { return nextActionEta; }
    public void setNextActionEta(LocalDate nextActionEta) { this.nextActionEta = nextActionEta; }

    public NextActionOwner getNextActionOwner() { return nextActionOwner; }
    public void setNextActionOwner(NextActionOwner nextActionOwner) { this.nextActionOwner = nextActionOwner; }

    public boolean isVoided() { return voided; }
    public void setVoided(boolean voided) { this.voided = voided; }

    public UserSummary getVoidedBy() { return voidedBy; }
    public void setVoidedBy(UserSummary voidedBy) { this.voidedBy = voidedBy; }

    public String getVoidedReason() { return voidedReason; }
    public void setVoidedReason(String voidedReason) { this.voidedReason = voidedReason; }

    public OffsetDateTime getVoidedAt() { return voidedAt; }
    public void setVoidedAt(OffsetDateTime voidedAt) { this.voidedAt = voidedAt; }

    public UserSummary getUnvoidedBy() { return unvoidedBy; }
    public void setUnvoidedBy(UserSummary unvoidedBy) { this.unvoidedBy = unvoidedBy; }

    public OffsetDateTime getUnvoidedAt() { return unvoidedAt; }
    public void setUnvoidedAt(OffsetDateTime unvoidedAt) { this.unvoidedAt = unvoidedAt; }

    public boolean isFirstInPhase() { return isFirstInPhase; }
    public void setFirstInPhase(boolean firstInPhase) { isFirstInPhase = firstInPhase; }

    public String getLoggedAtStage() { return loggedAtStage; }
    public void setLoggedAtStage(String loggedAtStage) { this.loggedAtStage = loggedAtStage; }

    public List<AttachmentResponse> getAttachments() { return attachments; }
    public void setAttachments(List<AttachmentResponse> attachments) { this.attachments = attachments; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
