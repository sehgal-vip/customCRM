package com.turno.crm.model.dto;

import jakarta.validation.constraints.NotBlank;

public class ArchiveDealRequest {

    @NotBlank(message = "Archive reason is required")
    private String reason;

    private String reasonText;

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getReasonText() { return reasonText; }
    public void setReasonText(String reasonText) { this.reasonText = reasonText; }
}
