package com.turno.crm.model.dto;

import java.math.BigDecimal;

public class DealAlertItem {

    private Long dealId;
    private String dealName;
    private String agentName;
    private String stage;
    private long daysInStage;
    private BigDecimal estimatedMonthlyValue;
    private String alertReason;

    public Long getDealId() { return dealId; }
    public void setDealId(Long dealId) { this.dealId = dealId; }

    public String getDealName() { return dealName; }
    public void setDealName(String dealName) { this.dealName = dealName; }

    public String getAgentName() { return agentName; }
    public void setAgentName(String agentName) { this.agentName = agentName; }

    public String getStage() { return stage; }
    public void setStage(String stage) { this.stage = stage; }

    public long getDaysInStage() { return daysInStage; }
    public void setDaysInStage(long daysInStage) { this.daysInStage = daysInStage; }

    public BigDecimal getEstimatedMonthlyValue() { return estimatedMonthlyValue; }
    public void setEstimatedMonthlyValue(BigDecimal estimatedMonthlyValue) { this.estimatedMonthlyValue = estimatedMonthlyValue; }

    public String getAlertReason() { return alertReason; }
    public void setAlertReason(String alertReason) { this.alertReason = alertReason; }
}
