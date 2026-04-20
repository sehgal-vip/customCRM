package com.turno.crm.model.dto;

public class StaleThresholdResponse {

    private String stage;
    private int thresholdDays;

    public StaleThresholdResponse() {}

    public StaleThresholdResponse(String stage, int thresholdDays) {
        this.stage = stage;
        this.thresholdDays = thresholdDays;
    }

    public String getStage() { return stage; }
    public void setStage(String stage) { this.stage = stage; }

    public int getThresholdDays() { return thresholdDays; }
    public void setThresholdDays(int thresholdDays) { this.thresholdDays = thresholdDays; }
}
