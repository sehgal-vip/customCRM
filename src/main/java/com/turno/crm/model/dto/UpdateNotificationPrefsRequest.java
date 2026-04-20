package com.turno.crm.model.dto;

import com.turno.crm.model.enums.NotificationEventType;
import com.turno.crm.model.enums.UserRole;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public class UpdateNotificationPrefsRequest {

    @NotEmpty(message = "Preferences list must not be empty")
    private List<PrefItem> preferences;

    public List<PrefItem> getPreferences() { return preferences; }
    public void setPreferences(List<PrefItem> preferences) { this.preferences = preferences; }

    public static class PrefItem {
        private UserRole role;
        private NotificationEventType eventType;
        private boolean enabled;

        public UserRole getRole() { return role; }
        public void setRole(UserRole role) { this.role = role; }

        public NotificationEventType getEventType() { return eventType; }
        public void setEventType(NotificationEventType eventType) { this.eventType = eventType; }

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
    }
}
