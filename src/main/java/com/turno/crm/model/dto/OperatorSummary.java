package com.turno.crm.model.dto;

public class OperatorSummary {

    private Long id;
    private String companyName;

    public OperatorSummary() {}

    public OperatorSummary(Long id, String companyName) {
        this.id = id;
        this.companyName = companyName;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
}
