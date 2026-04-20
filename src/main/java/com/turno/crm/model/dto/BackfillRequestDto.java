package com.turno.crm.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class BackfillRequestDto {

    @NotNull(message = "Target stage is required")
    private String targetStage;

    @NotBlank(message = "Context is required")
    private String context;

    @NotNull(message = "Original start date is required")
    private LocalDate originalStartDate;

    public String getTargetStage() { return targetStage; }
    public void setTargetStage(String targetStage) { this.targetStage = targetStage; }

    public String getContext() { return context; }
    public void setContext(String context) { this.context = context; }

    public LocalDate getOriginalStartDate() { return originalStartDate; }
    public void setOriginalStartDate(LocalDate originalStartDate) { this.originalStartDate = originalStartDate; }
}
