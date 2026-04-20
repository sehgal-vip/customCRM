package com.turno.crm.model.dto;

import com.turno.crm.model.enums.ApprovalStatus;
import com.turno.crm.model.enums.DealStage;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public class BackfillRequestResponse {

    private Long id;
    private Long dealId;
    private String dealName;
    private Long requestedById;
    private String requestedByName;
    private DealStage targetStage;
    private String context;
    private LocalDate originalStartDate;
    private ApprovalStatus status;
    private Long reviewedById;
    private String reviewedByName;
    private OffsetDateTime reviewedAt;
    private OffsetDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getDealId() { return dealId; }
    public void setDealId(Long dealId) { this.dealId = dealId; }

    public String getDealName() { return dealName; }
    public void setDealName(String dealName) { this.dealName = dealName; }

    public Long getRequestedById() { return requestedById; }
    public void setRequestedById(Long requestedById) { this.requestedById = requestedById; }

    public String getRequestedByName() { return requestedByName; }
    public void setRequestedByName(String requestedByName) { this.requestedByName = requestedByName; }

    public DealStage getTargetStage() { return targetStage; }
    public void setTargetStage(DealStage targetStage) { this.targetStage = targetStage; }

    public String getContext() { return context; }
    public void setContext(String context) { this.context = context; }

    public LocalDate getOriginalStartDate() { return originalStartDate; }
    public void setOriginalStartDate(LocalDate originalStartDate) { this.originalStartDate = originalStartDate; }

    public ApprovalStatus getStatus() { return status; }
    public void setStatus(ApprovalStatus status) { this.status = status; }

    public Long getReviewedById() { return reviewedById; }
    public void setReviewedById(Long reviewedById) { this.reviewedById = reviewedById; }

    public String getReviewedByName() { return reviewedByName; }
    public void setReviewedByName(String reviewedByName) { this.reviewedByName = reviewedByName; }

    public OffsetDateTime getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(OffsetDateTime reviewedAt) { this.reviewedAt = reviewedAt; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
