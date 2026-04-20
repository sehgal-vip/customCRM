package com.turno.crm.model.dto;

import java.time.LocalDate;

public class TaskItem {

    private Long dealId;
    private String dealName;
    private String operatorName;
    private String nextAction;
    private LocalDate nextActionEta;
    private String nextActionOwner;
    private String currentStage;
    private String agentName;
    private Long agentId;
    private boolean overdue;
    private String type;

    public TaskItem() {}

    public Long getDealId() { return dealId; }
    public void setDealId(Long dealId) { this.dealId = dealId; }

    public String getDealName() { return dealName; }
    public void setDealName(String dealName) { this.dealName = dealName; }

    public String getOperatorName() { return operatorName; }
    public void setOperatorName(String operatorName) { this.operatorName = operatorName; }

    public String getNextAction() { return nextAction; }
    public void setNextAction(String nextAction) { this.nextAction = nextAction; }

    public LocalDate getNextActionEta() { return nextActionEta; }
    public void setNextActionEta(LocalDate nextActionEta) { this.nextActionEta = nextActionEta; }

    public String getNextActionOwner() { return nextActionOwner; }
    public void setNextActionOwner(String nextActionOwner) { this.nextActionOwner = nextActionOwner; }

    public String getCurrentStage() { return currentStage; }
    public void setCurrentStage(String currentStage) { this.currentStage = currentStage; }

    public String getAgentName() { return agentName; }
    public void setAgentName(String agentName) { this.agentName = agentName; }

    public Long getAgentId() { return agentId; }
    public void setAgentId(Long agentId) { this.agentId = agentId; }

    public boolean isOverdue() { return overdue; }
    public void setOverdue(boolean overdue) { this.overdue = overdue; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}
