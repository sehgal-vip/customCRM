package com.turno.crm.model.entity;

import com.turno.crm.model.enums.ApprovalStatus;
import com.turno.crm.model.enums.DealStage;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.OffsetDateTime;

@Entity
@Table(name = "regression_requests")
public class RegressionRequest extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deal_id", nullable = false)
    private Deal deal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requested_by", nullable = false)
    private User requestedBy;

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "from_stage", nullable = false, columnDefinition = "deal_stage")
    private DealStage fromStage;

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "to_stage", nullable = false, columnDefinition = "deal_stage")
    private DealStage toStage;

    @Column(name = "reason", nullable = false, columnDefinition = "TEXT")
    private String reason;

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

    public DealStage getFromStage() { return fromStage; }
    public void setFromStage(DealStage fromStage) { this.fromStage = fromStage; }

    public DealStage getToStage() { return toStage; }
    public void setToStage(DealStage toStage) { this.toStage = toStage; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public ApprovalStatus getStatus() { return status; }
    public void setStatus(ApprovalStatus status) { this.status = status; }

    public User getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(User reviewedBy) { this.reviewedBy = reviewedBy; }

    public OffsetDateTime getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(OffsetDateTime reviewedAt) { this.reviewedAt = reviewedAt; }
}
