package com.turno.crm.model.entity;

import com.turno.crm.model.enums.NotificationEventType;
import com.turno.crm.model.enums.NotificationPriority;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.OffsetDateTime;

@Entity
@Table(name = "notifications")
public class Notification extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "event_type", nullable = false, columnDefinition = "notification_event_type")
    private NotificationEventType eventType;

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "priority", nullable = false, columnDefinition = "notification_priority")
    private NotificationPriority priority;

    @Column(name = "title", nullable = false, length = 500)
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deal_id")
    private Deal deal;

    @Column(name = "cleared", nullable = false)
    private Boolean cleared = false;

    @Column(name = "cleared_at")
    private OffsetDateTime clearedAt;

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public NotificationEventType getEventType() { return eventType; }
    public void setEventType(NotificationEventType eventType) { this.eventType = eventType; }

    public NotificationPriority getPriority() { return priority; }
    public void setPriority(NotificationPriority priority) { this.priority = priority; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Deal getDeal() { return deal; }
    public void setDeal(Deal deal) { this.deal = deal; }

    public Boolean getCleared() { return cleared; }
    public void setCleared(Boolean cleared) { this.cleared = cleared; }

    public OffsetDateTime getClearedAt() { return clearedAt; }
    public void setClearedAt(OffsetDateTime clearedAt) { this.clearedAt = clearedAt; }
}
