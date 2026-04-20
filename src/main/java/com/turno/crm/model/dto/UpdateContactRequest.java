package com.turno.crm.model.dto;

import com.turno.crm.model.enums.ContactRole;

public class UpdateContactRequest {

    private String name;
    private ContactRole role;
    private String mobile;
    private String email;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public ContactRole getRole() { return role; }
    public void setRole(ContactRole role) { this.role = role; }

    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
