package com.turno.crm.model.dto;

public class ChecklistItemResponse {

    private Long id;
    private String documentName;
    private String requirement;
    private String requiredByStage;
    private boolean active;

    public ChecklistItemResponse() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDocumentName() { return documentName; }
    public void setDocumentName(String documentName) { this.documentName = documentName; }

    public String getRequirement() { return requirement; }
    public void setRequirement(String requirement) { this.requirement = requirement; }

    public String getRequiredByStage() { return requiredByStage; }
    public void setRequiredByStage(String requiredByStage) { this.requiredByStage = requiredByStage; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
