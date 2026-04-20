package com.turno.crm.model.dto;

import jakarta.validation.constraints.NotBlank;

public class CreateRegionRequest {

    @NotBlank(message = "Region name is required")
    private String name;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
