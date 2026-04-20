package com.turno.crm.model.dto;

import java.math.BigDecimal;

public class AgentMetric {

    private Long agentId;
    private String agentName;
    private int activeDeals;
    private BigDecimal pipelineValue;
    private int overdueFollowUps;

    public Long getAgentId() { return agentId; }
    public void setAgentId(Long agentId) { this.agentId = agentId; }

    public String getAgentName() { return agentName; }
    public void setAgentName(String agentName) { this.agentName = agentName; }

    public int getActiveDeals() { return activeDeals; }
    public void setActiveDeals(int activeDeals) { this.activeDeals = activeDeals; }

    public BigDecimal getPipelineValue() { return pipelineValue; }
    public void setPipelineValue(BigDecimal pipelineValue) { this.pipelineValue = pipelineValue; }

    public int getOverdueFollowUps() { return overdueFollowUps; }
    public void setOverdueFollowUps(int overdueFollowUps) { this.overdueFollowUps = overdueFollowUps; }
}
