package com.turno.crm.model.dto;

import com.turno.crm.model.enums.DealStage;
import com.turno.crm.model.enums.DealStatus;
import com.turno.crm.model.enums.Stage5SubStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class DealListResponse {

    private Long id;
    private String name;
    private String operatorName;
    private String agentName;
    private Integer fleetSize;
    private BigDecimal estimatedMonthlyValue;
    private DealStage currentStage;
    private Stage5SubStatus subStatus;
    private DealStatus status;
    private long daysInStage;
    private OffsetDateTime nextActionEta;
    private String nextAction;
    private boolean pricingApproved;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getOperatorName() { return operatorName; }
    public void setOperatorName(String operatorName) { this.operatorName = operatorName; }

    public String getAgentName() { return agentName; }
    public void setAgentName(String agentName) { this.agentName = agentName; }

    public Integer getFleetSize() { return fleetSize; }
    public void setFleetSize(Integer fleetSize) { this.fleetSize = fleetSize; }

    public BigDecimal getEstimatedMonthlyValue() { return estimatedMonthlyValue; }
    public void setEstimatedMonthlyValue(BigDecimal estimatedMonthlyValue) { this.estimatedMonthlyValue = estimatedMonthlyValue; }

    public DealStage getCurrentStage() { return currentStage; }
    public void setCurrentStage(DealStage currentStage) { this.currentStage = currentStage; }

    public Stage5SubStatus getSubStatus() { return subStatus; }
    public void setSubStatus(Stage5SubStatus subStatus) { this.subStatus = subStatus; }

    public DealStatus getStatus() { return status; }
    public void setStatus(DealStatus status) { this.status = status; }

    public long getDaysInStage() { return daysInStage; }
    public void setDaysInStage(long daysInStage) { this.daysInStage = daysInStage; }

    public OffsetDateTime getNextActionEta() { return nextActionEta; }
    public void setNextActionEta(OffsetDateTime nextActionEta) { this.nextActionEta = nextActionEta; }

    public String getNextAction() { return nextAction; }
    public void setNextAction(String nextAction) { this.nextAction = nextAction; }

    public boolean isPricingApproved() { return pricingApproved; }
    public void setPricingApproved(boolean pricingApproved) { this.pricingApproved = pricingApproved; }
}
