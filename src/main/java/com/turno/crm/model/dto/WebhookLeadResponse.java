package com.turno.crm.model.dto;

public class WebhookLeadResponse {

    private Long dealId;
    private Long operatorId;
    private boolean duplicateOperator;

    public WebhookLeadResponse() {}

    public WebhookLeadResponse(Long dealId, Long operatorId, boolean duplicateOperator) {
        this.dealId = dealId;
        this.operatorId = operatorId;
        this.duplicateOperator = duplicateOperator;
    }

    public Long getDealId() { return dealId; }
    public void setDealId(Long dealId) { this.dealId = dealId; }

    public Long getOperatorId() { return operatorId; }
    public void setOperatorId(Long operatorId) { this.operatorId = operatorId; }

    public boolean isDuplicateOperator() { return duplicateOperator; }
    public void setDuplicateOperator(boolean duplicateOperator) { this.duplicateOperator = duplicateOperator; }
}
