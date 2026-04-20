package com.turno.crm.model.dto;

import com.turno.crm.model.enums.ContactRole;

public class ContactResponse {

    private Long id;
    private String name;
    private ContactRole role;
    private String mobile;
    private String email;
    private boolean incomplete;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public ContactRole getRole() { return role; }
    public void setRole(ContactRole role) { this.role = role; }

    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public boolean isIncomplete() { return incomplete; }
    public void setIncomplete(boolean incomplete) { this.incomplete = incomplete; }
}
