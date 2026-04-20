package com.turno.crm.model.dto;

import com.turno.crm.model.enums.LeadSource;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class CreateDealRequest {

    @NotBlank(message = "Deal name is required")
    @Size(max = 100, message = "Deal name must not exceed 100 characters")
    private String name;

    @NotNull(message = "Operator ID is required")
    private Long operatorId;

    @NotNull(message = "Assigned agent ID is required")
    private Long assignedAgentId;

    private Integer fleetSize;
    private BigDecimal estimatedMonthlyValue;

    @NotNull(message = "Lead source is required")
    private LeadSource leadSource;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Long getOperatorId() { return operatorId; }
    public void setOperatorId(Long operatorId) { this.operatorId = operatorId; }

    public Long getAssignedAgentId() { return assignedAgentId; }
    public void setAssignedAgentId(Long assignedAgentId) { this.assignedAgentId = assignedAgentId; }

    public Integer getFleetSize() { return fleetSize; }
    public void setFleetSize(Integer fleetSize) { this.fleetSize = fleetSize; }

    public BigDecimal getEstimatedMonthlyValue() { return estimatedMonthlyValue; }
    public void setEstimatedMonthlyValue(BigDecimal estimatedMonthlyValue) { this.estimatedMonthlyValue = estimatedMonthlyValue; }

    public LeadSource getLeadSource() { return leadSource; }
    public void setLeadSource(LeadSource leadSource) { this.leadSource = leadSource; }
}
