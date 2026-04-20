package com.turno.crm.model.dto;

public class ObjectionCount {

    private String objection;
    private int count;

    public ObjectionCount() {}

    public ObjectionCount(String objection, int count) {
        this.objection = objection;
        this.count = count;
    }

    public String getObjection() { return objection; }
    public void setObjection(String objection) { this.objection = objection; }

    public int getCount() { return count; }
    public void setCount(int count) { this.count = count; }
}
