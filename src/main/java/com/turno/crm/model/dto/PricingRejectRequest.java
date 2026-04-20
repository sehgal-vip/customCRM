package com.turno.crm.model.dto;

import jakarta.validation.constraints.NotBlank;

public class PricingRejectRequest {

    @NotBlank(message = "Rejection note is required")
    private String rejectionNote;

    public String getRejectionNote() { return rejectionNote; }
    public void setRejectionNote(String rejectionNote) { this.rejectionNote = rejectionNote; }
}
