package com.turno.crm.model.dto;

import com.turno.crm.model.enums.UserRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class UpdateUserRequest {

    @NotBlank
    private String name;
    @NotNull
    private UserRole role;
    private List<Long> regionIds;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }

    public List<Long> getRegionIds() { return regionIds; }
    public void setRegionIds(List<Long> regionIds) { this.regionIds = regionIds; }
}
