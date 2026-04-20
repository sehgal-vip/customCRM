package com.turno.crm.model.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.Map;

public class UpdateStaleThresholdsRequest {

    @NotEmpty(message = "Thresholds map must not be empty")
    private Map<String, Integer> thresholds;

    public Map<String, Integer> getThresholds() { return thresholds; }
    public void setThresholds(Map<String, Integer> thresholds) { this.thresholds = thresholds; }
}
