package com.turno.crm.model.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.util.List;

public class PricingSubmitRequest {

    @NotEmpty(message = "Services selected is required")
    private List<String> servicesSelected;

    @NotNull(message = "Monthly km commitment is required")
    @Positive(message = "Monthly km commitment must be positive")
    private BigDecimal monthlyKmCommitment;

    @NotNull(message = "Price per km is required")
    @Positive(message = "Price per km must be positive")
    private BigDecimal pricePerKm;

    public List<String> getServicesSelected() { return servicesSelected; }
    public void setServicesSelected(List<String> servicesSelected) { this.servicesSelected = servicesSelected; }

    public BigDecimal getMonthlyKmCommitment() { return monthlyKmCommitment; }
    public void setMonthlyKmCommitment(BigDecimal monthlyKmCommitment) { this.monthlyKmCommitment = monthlyKmCommitment; }

    public BigDecimal getPricePerKm() { return pricePerKm; }
    public void setPricePerKm(BigDecimal pricePerKm) { this.pricePerKm = pricePerKm; }

    @PositiveOrZero(message = "Token amount must be zero or positive")
    private BigDecimal tokenAmount;

    public BigDecimal getTokenAmount() { return tokenAmount; }
    public void setTokenAmount(BigDecimal tokenAmount) { this.tokenAmount = tokenAmount; }
}
