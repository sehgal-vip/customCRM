package com.turno.crm.model.dto;

import java.util.List;

public class AgentPerformanceResponse {

    private List<AgentMetric> agents;

    public List<AgentMetric> getAgents() { return agents; }
    public void setAgents(List<AgentMetric> agents) { this.agents = agents; }
}
