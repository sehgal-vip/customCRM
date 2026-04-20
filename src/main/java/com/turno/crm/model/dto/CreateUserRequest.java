package com.turno.crm.model.dto;

import com.turno.crm.model.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.List;

public class CreateUserRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Must be a valid email")
    @Pattern(regexp = ".*@turno\\.com$", message = "Email must end with @turno.com")
    private String email;

    @NotBlank(message = "Name is required")
    private String name;

    @NotNull(message = "Role is required")
    private UserRole role;

    private List<Long> regionIds;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }

    public List<Long> getRegionIds() { return regionIds; }
    public void setRegionIds(List<Long> regionIds) { this.regionIds = regionIds; }
}
