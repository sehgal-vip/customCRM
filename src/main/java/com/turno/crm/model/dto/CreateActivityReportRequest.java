package com.turno.crm.model.dto;

import com.turno.crm.model.enums.ActivityType;
import com.turno.crm.model.enums.ContactRole;
import com.turno.crm.model.enums.NextActionOwner;
import com.turno.crm.model.enums.ReportDuration;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

public class CreateActivityReportRequest {

    @NotNull(message = "Activity type is required")
    private ActivityType activityType;

    @NotNull(message = "Interaction datetime is required")
    private OffsetDateTime interactionDatetime;

    private Long contactId;

    private CreateContactRequest newContact;

    @NotNull(message = "Contact role is required")
    private ContactRole contactRole;

    @NotNull(message = "Duration is required")
    private ReportDuration duration;

    private Map<String, Object> phaseSpecificData;

    private List<String> buyingSignals;

    private List<String> objections;

    private String notes;

    @NotBlank(message = "Next action is required")
    private String nextAction;

    private LocalDate nextActionEta;

    @NotNull(message = "Next action owner is required")
    private NextActionOwner nextActionOwner;

    private List<AttachmentRequest> attachments;

    public ActivityType getActivityType() { return activityType; }
    public void setActivityType(ActivityType activityType) { this.activityType = activityType; }

    public OffsetDateTime getInteractionDatetime() { return interactionDatetime; }
    public void setInteractionDatetime(OffsetDateTime interactionDatetime) { this.interactionDatetime = interactionDatetime; }

    public Long getContactId() { return contactId; }
    public void setContactId(Long contactId) { this.contactId = contactId; }

    public CreateContactRequest getNewContact() { return newContact; }
    public void setNewContact(CreateContactRequest newContact) { this.newContact = newContact; }

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

    public List<AttachmentRequest> getAttachments() { return attachments; }
    public void setAttachments(List<AttachmentRequest> attachments) { this.attachments = attachments; }
}
