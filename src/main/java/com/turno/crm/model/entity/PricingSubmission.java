package com.turno.crm.model.entity;

import com.turno.crm.model.enums.PricingStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Entity
@Table(name = "pricing_submissions")
public class PricingSubmission extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deal_id", nullable = false)
    private Deal deal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submitted_by", nullable = false)
    private User submittedBy;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "services_selected", nullable = false, columnDefinition = "text[]")
    private List<String> servicesSelected;

    @Column(name = "monthly_km_commitment", nullable = false, precision = 12, scale = 2)
    private BigDecimal monthlyKmCommitment;

    @Column(name = "price_per_km", nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePerKm;

    @Column(name = "monthly_value_per_vehicle", insertable = false, updatable = false, precision = 14, scale = 2)
    private BigDecimal monthlyValuePerVehicle;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "manager_services_selected", columnDefinition = "text[]")
    private List<String> managerServicesSelected;

    @Column(name = "manager_monthly_km", precision = 12, scale = 2)
    private BigDecimal managerMonthlyKm;

    @Column(name = "manager_price_per_km", precision = 10, scale = 2)
    private BigDecimal managerPricePerKm;

    @Column(name = "token_amount", precision = 14, scale = 2)
    private BigDecimal tokenAmount;

    @Column(name = "manager_token_amount", precision = 14, scale = 2)
    private BigDecimal managerTokenAmount;

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "status", nullable = false, columnDefinition = "pricing_status")
    private PricingStatus status = PricingStatus.SUBMITTED;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private User reviewedBy;

    @Column(name = "reviewed_at")
    private OffsetDateTime reviewedAt;

    @Column(name = "rejection_note", columnDefinition = "TEXT")
    private String rejectionNote;

    public Deal getDeal() { return deal; }
    public void setDeal(Deal deal) { this.deal = deal; }

    public User getSubmittedBy() { return submittedBy; }
    public void setSubmittedBy(User submittedBy) { this.submittedBy = submittedBy; }

    public List<String> getServicesSelected() { return servicesSelected; }
    public void setServicesSelected(List<String> servicesSelected) { this.servicesSelected = servicesSelected; }

    public BigDecimal getMonthlyKmCommitment() { return monthlyKmCommitment; }
    public void setMonthlyKmCommitment(BigDecimal monthlyKmCommitment) { this.monthlyKmCommitment = monthlyKmCommitment; }

    public BigDecimal getPricePerKm() { return pricePerKm; }
    public void setPricePerKm(BigDecimal pricePerKm) { this.pricePerKm = pricePerKm; }

    public BigDecimal getMonthlyValuePerVehicle() { return monthlyValuePerVehicle; }

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

    public User getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(User reviewedBy) { this.reviewedBy = reviewedBy; }

    public OffsetDateTime getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(OffsetDateTime reviewedAt) { this.reviewedAt = reviewedAt; }

    public String getRejectionNote() { return rejectionNote; }
    public void setRejectionNote(String rejectionNote) { this.rejectionNote = rejectionNote; }
}
