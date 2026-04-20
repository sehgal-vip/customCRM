package com.turno.crm.model.dto;

import com.turno.crm.model.enums.NotificationEventType;
import com.turno.crm.model.enums.NotificationPriority;

import java.time.OffsetDateTime;

public class NotificationResponse {

    private Long id;
    private NotificationEventType eventType;
    private NotificationPriority priority;
    private String title;
    private String content;
    private Long dealId;
    private String dealName;
    private boolean cleared;
    private OffsetDateTime clearedAt;
    private OffsetDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public NotificationEventType getEventType() { return eventType; }
    public void setEventType(NotificationEventType eventType) { this.eventType = eventType; }

    public NotificationPriority getPriority() { return priority; }
    public void setPriority(NotificationPriority priority) { this.priority = priority; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Long getDealId() { return dealId; }
    public void setDealId(Long dealId) { this.dealId = dealId; }

    public String getDealName() { return dealName; }
    public void setDealName(String dealName) { this.dealName = dealName; }

    public boolean isCleared() { return cleared; }
    public void setCleared(boolean cleared) { this.cleared = cleared; }

    public OffsetDateTime getClearedAt() { return clearedAt; }
    public void setClearedAt(OffsetDateTime clearedAt) { this.clearedAt = clearedAt; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
