package com.turno.crm.model.dto;

import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class UpdateDealRequest {

    @Size(max = 100, message = "Deal name must not exceed 100 characters")
    private String name;

    private Long assignedAgentId;
    private Long operatorId;
    private Integer fleetSize;
    private BigDecimal estimatedMonthlyValue;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Long getAssignedAgentId() { return assignedAgentId; }
    public void setAssignedAgentId(Long assignedAgentId) { this.assignedAgentId = assignedAgentId; }

    public Long getOperatorId() { return operatorId; }
    public void setOperatorId(Long operatorId) { this.operatorId = operatorId; }

    public Integer getFleetSize() { return fleetSize; }
    public void setFleetSize(Integer fleetSize) { this.fleetSize = fleetSize; }

    public BigDecimal getEstimatedMonthlyValue() { return estimatedMonthlyValue; }
    public void setEstimatedMonthlyValue(BigDecimal estimatedMonthlyValue) { this.estimatedMonthlyValue = estimatedMonthlyValue; }
}
