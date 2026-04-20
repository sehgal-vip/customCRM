package com.turno.crm.model.dto;

import com.turno.crm.model.enums.TaskPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public class CreateTaskRequest {

    @NotBlank
    private String title;

    private String description;

    private TaskPriority priority;

    @NotNull
    private Long assignedToId;

    private Long dealId;

    @NotNull
    private LocalDate dueDate;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public TaskPriority getPriority() { return priority; }
    public void setPriority(TaskPriority priority) { this.priority = priority; }

    public Long getAssignedToId() { return assignedToId; }
    public void setAssignedToId(Long assignedToId) { this.assignedToId = assignedToId; }

    public Long getDealId() { return dealId; }
    public void setDealId(Long dealId) { this.dealId = dealId; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
}
