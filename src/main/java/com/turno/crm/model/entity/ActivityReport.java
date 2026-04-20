package com.turno.crm.model.entity;

import com.turno.crm.model.enums.*;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "activity_reports")
public class ActivityReport extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deal_id", nullable = false)
    private Deal deal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agent_id", nullable = false)
    private User agent;

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "template_type", nullable = false, columnDefinition = "template_type")
    private TemplateType templateType;

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "activity_type", nullable = false, columnDefinition = "activity_type")
    private ActivityType activityType;

    @Column(name = "interaction_datetime", nullable = false)
    private OffsetDateTime interactionDatetime;

    @Column(name = "submission_datetime", nullable = false)
    private OffsetDateTime submissionDatetime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contact_id", nullable = false)
    private Contact contact;

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "contact_role", nullable = false, columnDefinition = "contact_role")
    private ContactRole contactRole;

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "duration", nullable = false, columnDefinition = "report_duration")
    private ReportDuration duration;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "phase_specific_data", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> phaseSpecificData = new HashMap<>();

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "buying_signals", columnDefinition = "text[]")
    private List<String> buyingSignals;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "objections", columnDefinition = "text[]")
    private List<String> objections;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "next_action", nullable = false, columnDefinition = "TEXT")
    private String nextAction;

    @Column(name = "next_action_eta")
    private LocalDate nextActionEta;

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "next_action_owner", nullable = false, columnDefinition = "next_action_owner")
    private NextActionOwner nextActionOwner;

    @Column(name = "voided", nullable = false)
    private Boolean voided = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voided_by")
    private User voidedBy;

    @Column(name = "voided_reason", columnDefinition = "TEXT")
    private String voidedReason;

    @Column(name = "voided_at")
    private OffsetDateTime voidedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unvoided_by")
    private User unvoidedBy;

    @Column(name = "unvoided_at")
    private OffsetDateTime unvoidedAt;

    @Column(name = "is_first_in_phase", nullable = false)
    private Boolean isFirstInPhase = false;

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "logged_at_stage", nullable = false, columnDefinition = "deal_stage")
    private DealStage loggedAtStage;

    @OneToMany(mappedBy = "activityReport", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Attachment> attachments = new ArrayList<>();

    @OneToMany(mappedBy = "activityReport", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ActivityReportNote> activityReportNotes = new ArrayList<>();

    public Deal getDeal() { return deal; }
    public void setDeal(Deal deal) { this.deal = deal; }

    public User getAgent() { return agent; }
    public void setAgent(User agent) { this.agent = agent; }

    public TemplateType getTemplateType() { return templateType; }
    public void setTemplateType(TemplateType templateType) { this.templateType = templateType; }

    public ActivityType getActivityType() { return activityType; }
    public void setActivityType(ActivityType activityType) { this.activityType = activityType; }

    public OffsetDateTime getInteractionDatetime() { return interactionDatetime; }
    public void setInteractionDatetime(OffsetDateTime interactionDatetime) { this.interactionDatetime = interactionDatetime; }

    public OffsetDateTime getSubmissionDatetime() { return submissionDatetime; }
    public void setSubmissionDatetime(OffsetDateTime submissionDatetime) { this.submissionDatetime = submissionDatetime; }

    public Contact getContact() { return contact; }
    public void setContact(Contact contact) { this.contact = contact; }

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

    public Boolean getVoided() { return voided; }
    public void setVoided(Boolean voided) { this.voided = voided; }

    public User getVoidedBy() { return voidedBy; }
    public void setVoidedBy(User voidedBy) { this.voidedBy = voidedBy; }

    public String getVoidedReason() { return voidedReason; }
    public void setVoidedReason(String voidedReason) { this.voidedReason = voidedReason; }

    public OffsetDateTime getVoidedAt() { return voidedAt; }
    public void setVoidedAt(OffsetDateTime voidedAt) { this.voidedAt = voidedAt; }

    public User getUnvoidedBy() { return unvoidedBy; }
    public void setUnvoidedBy(User unvoidedBy) { this.unvoidedBy = unvoidedBy; }

    public OffsetDateTime getUnvoidedAt() { return unvoidedAt; }
    public void setUnvoidedAt(OffsetDateTime unvoidedAt) { this.unvoidedAt = unvoidedAt; }

    public Boolean getIsFirstInPhase() { return isFirstInPhase; }
    public void setIsFirstInPhase(Boolean isFirstInPhase) { this.isFirstInPhase = isFirstInPhase; }

    public DealStage getLoggedAtStage() { return loggedAtStage; }
    public void setLoggedAtStage(DealStage loggedAtStage) { this.loggedAtStage = loggedAtStage; }

    public List<Attachment> getAttachments() { return attachments; }
    public void setAttachments(List<Attachment> attachments) { this.attachments = attachments; }

    public List<ActivityReportNote> getActivityReportNotes() { return activityReportNotes; }
    public void setActivityReportNotes(List<ActivityReportNote> activityReportNotes) { this.activityReportNotes = activityReportNotes; }
}
