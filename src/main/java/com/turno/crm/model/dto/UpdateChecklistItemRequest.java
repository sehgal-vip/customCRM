package com.turno.crm.model.dto;

public class UpdateChecklistItemRequest {

    private String documentName;
    private String requirement;
    private String requiredByStage;
    private Boolean active;

    public String getDocumentName() { return documentName; }
    public void setDocumentName(String documentName) { this.documentName = documentName; }

    public String getRequirement() { return requirement; }
    public void setRequirement(String requirement) { this.requirement = requirement; }

    public String getRequiredByStage() { return requiredByStage; }
    public void setRequiredByStage(String requiredByStage) { this.requiredByStage = requiredByStage; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}
