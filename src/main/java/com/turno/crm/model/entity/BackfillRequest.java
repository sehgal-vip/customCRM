package com.turno.crm.model.entity;

import com.turno.crm.model.enums.ApprovalStatus;
import com.turno.crm.model.enums.DealStage;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "backfill_requests")
public class BackfillRequest extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deal_id", nullable = false)
    private Deal deal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requested_by", nullable = false)
    private User requestedBy;

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "target_stage", nullable = false, columnDefinition = "deal_stage")
    private DealStage targetStage;

    @Column(name = "context", nullable = false, columnDefinition = "TEXT")
    private String context;

    @Column(name = "original_start_date", nullable = false)
    private LocalDate originalStartDate;

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "status", nullable = false, columnDefinition = "approval_status")
    private ApprovalStatus status = ApprovalStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private User reviewedBy;

    @Column(name = "reviewed_at")
    private OffsetDateTime reviewedAt;

    public Deal getDeal() { return deal; }
    public void setDeal(Deal deal) { this.deal = deal; }

    public User getRequestedBy() { return requestedBy; }
    public void setRequestedBy(User requestedBy) { this.requestedBy = requestedBy; }

    public DealStage getTargetStage() { return targetStage; }
    public void setTargetStage(DealStage targetStage) { this.targetStage = targetStage; }

    public String getContext() { return context; }
    public void setContext(String context) { this.context = context; }

    public LocalDate getOriginalStartDate() { return originalStartDate; }
    public void setOriginalStartDate(LocalDate originalStartDate) { this.originalStartDate = originalStartDate; }

    public ApprovalStatus getStatus() { return status; }
    public void setStatus(ApprovalStatus status) { this.status = status; }

    public User getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(User reviewedBy) { this.reviewedBy = reviewedBy; }

    public OffsetDateTime getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(OffsetDateTime reviewedAt) { this.reviewedAt = reviewedAt; }
}
