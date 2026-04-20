package com.turno.crm.model.dto;

import java.time.OffsetDateTime;

public class WebhookKeyResponse {

    private Long id;
    private String keyPrefix;
    private String description;
    private boolean active;
    private OffsetDateTime createdAt;
    private OffsetDateTime revokedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getKeyPrefix() { return keyPrefix; }
    public void setKeyPrefix(String keyPrefix) { this.keyPrefix = keyPrefix; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public OffsetDateTime getRevokedAt() { return revokedAt; }
    public void setRevokedAt(OffsetDateTime revokedAt) { this.revokedAt = revokedAt; }
}
