package com.turno.crm.model.dto;

import jakarta.validation.constraints.NotBlank;

public class GenerateKeyRequest {

    @NotBlank(message = "Description is required")
    private String description;

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
