package com.turno.crm.model.dto;

import com.turno.crm.model.enums.TaxonomyType;

public class TaxonomyItemResponse {

    private Long id;
    private TaxonomyType taxonomyType;
    private String value;
    private boolean active;

    public TaxonomyItemResponse() {}

    public TaxonomyItemResponse(Long id, TaxonomyType taxonomyType, String value, boolean active) {
        this.id = id;
        this.taxonomyType = taxonomyType;
        this.value = value;
        this.active = active;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public TaxonomyType getTaxonomyType() { return taxonomyType; }
    public void setTaxonomyType(TaxonomyType taxonomyType) { this.taxonomyType = taxonomyType; }

    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
