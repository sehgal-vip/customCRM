package com.turno.crm.model.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "regions")
public class Region extends BaseEntity {

    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}
