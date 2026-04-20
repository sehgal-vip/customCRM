package com.turno.crm.model.dto;

import com.turno.crm.model.enums.PricingStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public class PricingSubmissionResponse {

    private Long id;
    private Long dealId;
    private UserSummary submittedBy;
    private List<String> servicesSelected;
    private BigDecimal monthlyKmCommitment;
    private BigDecimal pricePerKm;
    private BigDecimal monthlyValuePerVehicle;
    private List<String> managerServicesSelected;
    private BigDecimal managerMonthlyKm;
    private BigDecimal managerPricePerKm;
    private BigDecimal tokenAmount;
    private BigDecimal managerTokenAmount;
    private PricingStatus status;
    private UserSummary reviewedBy;
    private OffsetDateTime reviewedAt;
    private String rejectionNote;
    private OffsetDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getDealId() { return dealId; }
    public void setDealId(Long dealId) { this.dealId = dealId; }

    public UserSummary getSubmittedBy() { return submittedBy; }
    public void setSubmittedBy(UserSummary submittedBy) { this.submittedBy = submittedBy; }

    public List<String> getServicesSelected() { return servicesSelected; }
    public void setServicesSelected(List<String> servicesSelected) { this.servicesSelected = servicesSelected; }

    public BigDecimal getMonthlyKmCommitment() { return monthlyKmCommitment; }
    public void setMonthlyKmCommitment(BigDecimal monthlyKmCommitment) { this.monthlyKmCommitment = monthlyKmCommitment; }

    public BigDecimal getPricePerKm() { return pricePerKm; }
    public void setPricePerKm(BigDecimal pricePerKm) { this.pricePerKm = pricePerKm; }

    public BigDecimal getMonthlyValuePerVehicle() { return monthlyValuePerVehicle; }
    public void setMonthlyValuePerVehicle(BigDecimal monthlyValuePerVehicle) { this.monthlyValuePerVehicle = monthlyValuePerVehicle; }

    public List<String> getManagerServicesSelected() { return managerServicesSelected; }
    public void setManagerServicesSelected(List<String> managerServicesSelected) { this.managerServicesSelected = managerServicesSelected; }

    public BigDecimal getManagerMonthlyKm() { return managerMonthlyKm; }
    public void setManagerMonthlyKm(BigDecimal managerMonthlyKm) { this.managerMonthlyKm = managerMonthlyKm; }

    public BigDecimal getManagerPricePerKm() { return managerPricePerKm; }
    public void setManagerPricePerKm(BigDecimal managerPricePerKm) { this.managerPricePerKm = managerPricePerKm; }

    public BigDecimal getTokenAmount() { return tokenAmount; }
    public void setTokenAmount(BigDecimal tokenAmount) { this.tokenAmount = tokenAmount; }

    public BigDecimal getManagerTokenAmount() { return managerTokenAmount; }
    public void setManagerTokenAmount(BigDecimal managerTokenAmount) { this.managerTokenAmount = managerTokenAmount; }

    public PricingStatus getStatus() { return status; }
    public void setStatus(PricingStatus status) { this.status = status; }

    public UserSummary getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(UserSummary reviewedBy) { this.reviewedBy = reviewedBy; }

    public OffsetDateTime getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(OffsetDateTime reviewedAt) { this.reviewedAt = reviewedAt; }

    public String getRejectionNote() { return rejectionNote; }
    public void setRejectionNote(String rejectionNote) { this.rejectionNote = rejectionNote; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
