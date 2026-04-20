package com.turno.crm.model.dto;

import java.math.BigDecimal;
import java.util.List;

public class PricingApproveRequest {

    private List<String> servicesSelected;
    private BigDecimal monthlyKmCommitment;
    private BigDecimal pricePerKm;

    public List<String> getServicesSelected() { return servicesSelected; }
    public void setServicesSelected(List<String> servicesSelected) { this.servicesSelected = servicesSelected; }

    public BigDecimal getMonthlyKmCommitment() { return monthlyKmCommitment; }
    public void setMonthlyKmCommitment(BigDecimal monthlyKmCommitment) { this.monthlyKmCommitment = monthlyKmCommitment; }

    public BigDecimal getPricePerKm() { return pricePerKm; }
    public void setPricePerKm(BigDecimal pricePerKm) { this.pricePerKm = pricePerKm; }

    private BigDecimal tokenAmount;

    public BigDecimal getTokenAmount() { return tokenAmount; }
    public void setTokenAmount(BigDecimal tokenAmount) { this.tokenAmount = tokenAmount; }
}
