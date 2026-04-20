package com.turno.crm.model.dto;

import java.util.List;

public class ObjectionHeatmapResponse {

    private List<ObjectionCount> objections;

    public List<ObjectionCount> getObjections() { return objections; }
    public void setObjections(List<ObjectionCount> objections) { this.objections = objections; }
}
