package com.turno.crm.model.dto;

import jakarta.validation.constraints.NotBlank;

public class CreateTaxonomyRequest {

    @NotBlank(message = "Value is required")
    private String value;

    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
}
