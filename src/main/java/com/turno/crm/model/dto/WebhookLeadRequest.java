package com.turno.crm.model.dto;

import jakarta.validation.constraints.NotBlank;

public class WebhookLeadRequest {

    @NotBlank(message = "Operator name is required")
    private String operatorName;

    @NotBlank(message = "Contact name is required")
    private String contactName;

    private String contactPhone;
    private String contactEmail;

    @NotBlank(message = "Lead source is required")
    private String leadSource;

    private Integer fleetSize;
    private String operatorType;
    private String region;
    private String primaryUseCase;
    private String sourceEventId;

    public String getOperatorName() { return operatorName; }
    public void setOperatorName(String operatorName) { this.operatorName = operatorName; }

    public String getContactName() { return contactName; }
    public void setContactName(String contactName) { this.contactName = contactName; }

    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }

    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }

    public String getLeadSource() { return leadSource; }
    public void setLeadSource(String leadSource) { this.leadSource = leadSource; }

    public Integer getFleetSize() { return fleetSize; }
    public void setFleetSize(Integer fleetSize) { this.fleetSize = fleetSize; }

    public String getOperatorType() { return operatorType; }
    public void setOperatorType(String operatorType) { this.operatorType = operatorType; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    public String getPrimaryUseCase() { return primaryUseCase; }
    public void setPrimaryUseCase(String primaryUseCase) { this.primaryUseCase = primaryUseCase; }

    public String getSourceEventId() { return sourceEventId; }
    public void setSourceEventId(String sourceEventId) { this.sourceEventId = sourceEventId; }
}
