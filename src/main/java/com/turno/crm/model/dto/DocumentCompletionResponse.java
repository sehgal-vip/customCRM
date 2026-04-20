package com.turno.crm.model.dto;

import java.util.List;

public class DocumentCompletionResponse {

    private int mandatoryComplete;
    private int mandatoryTotal;
    private double percentage;
    private List<DocumentChecklistResponse> items;

    public int getMandatoryComplete() { return mandatoryComplete; }
    public void setMandatoryComplete(int mandatoryComplete) { this.mandatoryComplete = mandatoryComplete; }

    public int getMandatoryTotal() { return mandatoryTotal; }
    public void setMandatoryTotal(int mandatoryTotal) { this.mandatoryTotal = mandatoryTotal; }

    public double getPercentage() { return percentage; }
    public void setPercentage(double percentage) { this.percentage = percentage; }

    public List<DocumentChecklistResponse> getItems() { return items; }
    public void setItems(List<DocumentChecklistResponse> items) { this.items = items; }
}
