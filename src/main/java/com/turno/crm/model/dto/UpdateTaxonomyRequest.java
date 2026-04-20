package com.turno.crm.model.dto;

public class UpdateTaxonomyRequest {

    private String value;
    private Boolean active;

    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}
