package com.turno.crm.model.dto;

import jakarta.validation.constraints.NotNull;

public class UpdateDocumentStatusRequest {

    @NotNull(message = "Status is required")
    private String status;

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
