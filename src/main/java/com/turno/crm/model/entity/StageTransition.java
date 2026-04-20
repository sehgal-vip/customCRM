package com.turno.crm.model.entity;

import com.turno.crm.model.enums.DealStage;
import com.turno.crm.model.enums.TransitionType;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "stage_transitions")
public class StageTransition extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deal_id", nullable = false)
    private Deal deal;

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "from_stage", columnDefinition = "deal_stage")
    private DealStage fromStage;

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "to_stage", columnDefinition = "deal_stage")
    private DealStage toStage;

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "transition_type", nullable = false, columnDefinition = "transition_type")
    private TransitionType transitionType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id", nullable = false)
    private User actor;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    public Deal getDeal() { return deal; }
    public void setDeal(Deal deal) { this.deal = deal; }

    public DealStage getFromStage() { return fromStage; }
    public void setFromStage(DealStage fromStage) { this.fromStage = fromStage; }

    public DealStage getToStage() { return toStage; }
    public void setToStage(DealStage toStage) { this.toStage = toStage; }

    public TransitionType getTransitionType() { return transitionType; }
    public void setTransitionType(TransitionType transitionType) { this.transitionType = transitionType; }

    public User getActor() { return actor; }
    public void setActor(User actor) { this.actor = actor; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public User getApprovedBy() { return approvedBy; }
    public void setApprovedBy(User approvedBy) { this.approvedBy = approvedBy; }
}
