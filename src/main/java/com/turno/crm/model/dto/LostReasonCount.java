package com.turno.crm.model.dto;

public class LostReasonCount {

    private String reason;
    private int count;

    public LostReasonCount() {}

    public LostReasonCount(String reason, int count) {
        this.reason = reason;
        this.count = count;
    }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public int getCount() { return count; }
    public void setCount(int count) { this.count = count; }
}
