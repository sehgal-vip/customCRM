package com.turno.crm.model.entity;

import com.turno.crm.model.enums.NotificationEventType;
import com.turno.crm.model.enums.UserRole;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.OffsetDateTime;

@Entity
@Table(name = "notification_preferences", uniqueConstraints = {
    @UniqueConstraint(name = "uq_role_event_type", columnNames = {"role", "event_type"})
})
public class NotificationPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "role", nullable = false, columnDefinition = "user_role")
    private UserRole role;

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "event_type", nullable = false, columnDefinition = "notification_event_type")
    private NotificationEventType eventType;

    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private User updatedBy;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.updatedAt = OffsetDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }

    public NotificationEventType getEventType() { return eventType; }
    public void setEventType(NotificationEventType eventType) { this.eventType = eventType; }

    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }

    public User getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(User updatedBy) { this.updatedBy = updatedBy; }

    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}
