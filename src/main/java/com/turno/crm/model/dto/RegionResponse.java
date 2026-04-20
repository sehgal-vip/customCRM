package com.turno.crm.model.dto;

public class RegionResponse {

    private Long id;
    private String name;
    private boolean active;

    public RegionResponse() {}

    public RegionResponse(Long id, String name) {
        this.id = id;
        this.name = name;
        this.active = true;
    }

    public RegionResponse(Long id, String name, boolean active) {
        this.id = id;
        this.name = name;
        this.active = active;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
