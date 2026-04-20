package com.turno.crm.model.dto;

import com.turno.crm.model.enums.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class DealResponse {

    private Long id;
    private String name;
    private OperatorSummary operator;
    private UserSummary assignedAgent;
    private Integer fleetSize;
    private BigDecimal estimatedMonthlyValue;
    private LeadSource leadSource;
    private DealStage currentStage;
    private Stage5SubStatus subStatus;
    private DealStatus status;
    private String archivedReason;
    private String archivedReasonText;
    private boolean reopened;
    private boolean backfilled;
    private long daysInStage;
    private double docCompletionPct;
    private String nextAction;
    private String nextActionEta;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private boolean pricingApproved;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public OperatorSummary getOperator() { return operator; }
    public void setOperator(OperatorSummary operator) { this.operator = operator; }

    public UserSummary getAssignedAgent() { return assignedAgent; }
    public void setAssignedAgent(UserSummary assignedAgent) { this.assignedAgent = assignedAgent; }

    public Integer getFleetSize() { return fleetSize; }
    public void setFleetSize(Integer fleetSize) { this.fleetSize = fleetSize; }

    public BigDecimal getEstimatedMonthlyValue() { return estimatedMonthlyValue; }
    public void setEstimatedMonthlyValue(BigDecimal estimatedMonthlyValue) { this.estimatedMonthlyValue = estimatedMonthlyValue; }

    public LeadSource getLeadSource() { return leadSource; }
    public void setLeadSource(LeadSource leadSource) { this.leadSource = leadSource; }

    public DealStage getCurrentStage() { return currentStage; }
    public void setCurrentStage(DealStage currentStage) { this.currentStage = currentStage; }

    public Stage5SubStatus getSubStatus() { return subStatus; }
    public void setSubStatus(Stage5SubStatus subStatus) { this.subStatus = subStatus; }

    public DealStatus getStatus() { return status; }
    public void setStatus(DealStatus status) { this.status = status; }

    public String getArchivedReason() { return archivedReason; }
    public void setArchivedReason(String archivedReason) { this.archivedReason = archivedReason; }

    public String getArchivedReasonText() { return archivedReasonText; }
    public void setArchivedReasonText(String archivedReasonText) { this.archivedReasonText = archivedReasonText; }

    public boolean isReopened() { return reopened; }
    public void setReopened(boolean reopened) { this.reopened = reopened; }

    public boolean isBackfilled() { return backfilled; }
    public void setBackfilled(boolean backfilled) { this.backfilled = backfilled; }

    public long getDaysInStage() { return daysInStage; }
    public void setDaysInStage(long daysInStage) { this.daysInStage = daysInStage; }

    public double getDocCompletionPct() { return docCompletionPct; }
    public void setDocCompletionPct(double docCompletionPct) { this.docCompletionPct = docCompletionPct; }

    public String getNextAction() { return nextAction; }
    public void setNextAction(String nextAction) { this.nextAction = nextAction; }

    public String getNextActionEta() { return nextActionEta; }
    public void setNextActionEta(String nextActionEta) { this.nextActionEta = nextActionEta; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }

    public boolean isPricingApproved() { return pricingApproved; }
    public void setPricingApproved(boolean pricingApproved) { this.pricingApproved = pricingApproved; }
}
