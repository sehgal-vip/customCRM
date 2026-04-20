package com.turno.crm.model.dto;

import com.turno.crm.model.enums.UserRole;
import com.turno.crm.model.enums.UserStatus;

import java.util.List;

public class UserResponse {

    private Long id;
    private String email;
    private String name;
    private UserRole role;
    private UserStatus status;
    private List<RegionResponse> regions;
    private long activeDeals;

    public UserResponse() {}

    public UserResponse(Long id, String email, String name, UserRole role, UserStatus status, List<RegionResponse> regions) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.role = role;
        this.status = status;
        this.regions = regions;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }

    public UserStatus getStatus() { return status; }
    public void setStatus(UserStatus status) { this.status = status; }

    public List<RegionResponse> getRegions() { return regions; }
    public void setRegions(List<RegionResponse> regions) { this.regions = regions; }

    public long getActiveDeals() { return activeDeals; }
    public void setActiveDeals(long activeDeals) { this.activeDeals = activeDeals; }
}
