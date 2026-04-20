package com.turno.crm.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CreateChecklistItemRequest {

    @NotBlank(message = "Document name is required")
    private String documentName;

    @NotNull(message = "Requirement is required")
    private String requirement;

    @NotNull(message = "Required by stage is required")
    private String requiredByStage;

    public String getDocumentName() { return documentName; }
    public void setDocumentName(String documentName) { this.documentName = documentName; }

    public String getRequirement() { return requirement; }
    public void setRequirement(String requirement) { this.requirement = requirement; }

    public String getRequiredByStage() { return requiredByStage; }
    public void setRequiredByStage(String requiredByStage) { this.requiredByStage = requiredByStage; }
}
