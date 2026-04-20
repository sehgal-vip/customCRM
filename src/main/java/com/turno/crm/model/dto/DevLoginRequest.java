package com.turno.crm.model.dto;

import jakarta.validation.constraints.NotNull;

public class DevLoginRequest {

    @NotNull(message = "userId is required")
    private Long userId;

    public DevLoginRequest() {}

    public DevLoginRequest(Long userId) {
        this.userId = userId;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
}
