package com.turno.crm.model.dto;

import java.math.BigDecimal;

public class StageMetric {

    private String stage;
    private int dealCount;
    private BigDecimal totalValue;

    public StageMetric() {}

    public StageMetric(String stage, int dealCount, BigDecimal totalValue) {
        this.stage = stage;
        this.dealCount = dealCount;
        this.totalValue = totalValue;
    }

    public String getStage() { return stage; }
    public void setStage(String stage) { this.stage = stage; }

    public int getDealCount() { return dealCount; }
    public void setDealCount(int dealCount) { this.dealCount = dealCount; }

    public BigDecimal getTotalValue() { return totalValue; }
    public void setTotalValue(BigDecimal totalValue) { this.totalValue = totalValue; }
}
