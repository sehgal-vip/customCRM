package com.turno.crm.model.dto;

public class StageTransitionResponse {

    private DealResponse deal;
    private String fromStage;
    private String toStage;
    private String transitionType;

    public StageTransitionResponse() {}

    public StageTransitionResponse(DealResponse deal, String fromStage, String toStage, String transitionType) {
        this.deal = deal;
        this.fromStage = fromStage;
        this.toStage = toStage;
        this.transitionType = transitionType;
    }

    public DealResponse getDeal() { return deal; }
    public void setDeal(DealResponse deal) { this.deal = deal; }

    public String getFromStage() { return fromStage; }
    public void setFromStage(String fromStage) { this.fromStage = fromStage; }

    public String getToStage() { return toStage; }
    public void setToStage(String toStage) { this.toStage = toStage; }

    public String getTransitionType() { return transitionType; }
    public void setTransitionType(String transitionType) { this.transitionType = transitionType; }
}
