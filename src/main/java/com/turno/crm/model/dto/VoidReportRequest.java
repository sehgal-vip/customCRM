package com.turno.crm.model.dto;

import jakarta.validation.constraints.NotBlank;

public class VoidReportRequest {

    @NotBlank(message = "Reason is required")
    private String reason;

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
