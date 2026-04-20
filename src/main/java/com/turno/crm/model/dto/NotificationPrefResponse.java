package com.turno.crm.model.dto;

import com.turno.crm.model.enums.NotificationEventType;
import com.turno.crm.model.enums.UserRole;

public class NotificationPrefResponse {

    private UserRole role;
    private NotificationEventType eventType;
    private boolean enabled;

    public NotificationPrefResponse() {}

    public NotificationPrefResponse(UserRole role, NotificationEventType eventType, boolean enabled) {
        this.role = role;
        this.eventType = eventType;
        this.enabled = enabled;
    }

    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }

    public NotificationEventType getEventType() { return eventType; }
    public void setEventType(NotificationEventType eventType) { this.eventType = eventType; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
}
