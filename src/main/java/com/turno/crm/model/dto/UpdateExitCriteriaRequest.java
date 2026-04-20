package com.turno.crm.model.dto;

import jakarta.validation.constraints.NotNull;
import java.util.Map;

public class UpdateExitCriteriaRequest {
    @NotNull
    private Map<String, Boolean> criteria; // stage name -> activityRequired

    public Map<String, Boolean> getCriteria() { return criteria; }
    public void setCriteria(Map<String, Boolean> criteria) { this.criteria = criteria; }
}
