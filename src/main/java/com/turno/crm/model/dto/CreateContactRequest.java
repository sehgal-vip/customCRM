package com.turno.crm.model.dto;

import com.turno.crm.model.enums.ContactRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CreateContactRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotNull(message = "Role is required")
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

    public boolean hasAtLeastOneContactMethod() {
        return (mobile != null && !mobile.isBlank()) || (email != null && !email.isBlank());
    }
}
