package com.turno.crm.model.dto;

import com.turno.crm.model.enums.TaskStatus;
import jakarta.validation.constraints.NotNull;

public class UpdateTaskStatusRequest {

    @NotNull
    private TaskStatus status;

    public TaskStatus getStatus() { return status; }
    public void setStatus(TaskStatus status) { this.status = status; }
}
