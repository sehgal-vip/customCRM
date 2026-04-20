package com.turno.crm.model.dto;

import java.util.List;

public class PipelineOverviewResponse {

    private List<StageMetric> stageMetrics;
    private int pendingPricing;
    private int pendingRegression;

    public List<StageMetric> getStageMetrics() { return stageMetrics; }
    public void setStageMetrics(List<StageMetric> stageMetrics) { this.stageMetrics = stageMetrics; }

    public int getPendingPricing() { return pendingPricing; }
    public void setPendingPricing(int pendingPricing) { this.pendingPricing = pendingPricing; }

    public int getPendingRegression() { return pendingRegression; }
    public void setPendingRegression(int pendingRegression) { this.pendingRegression = pendingRegression; }
}
