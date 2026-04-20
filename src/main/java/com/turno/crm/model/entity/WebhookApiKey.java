package com.turno.crm.model.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "webhook_api_keys")
public class WebhookApiKey extends BaseEntity {

    @Column(name = "key_hash", nullable = false, unique = true, length = 128)
    private String keyHash;

    @Column(name = "description", length = 255)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Column(name = "revoked_at")
    private OffsetDateTime revokedAt;

    public String getKeyHash() { return keyHash; }
    public void setKeyHash(String keyHash) { this.keyHash = keyHash; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    public OffsetDateTime getRevokedAt() { return revokedAt; }
    public void setRevokedAt(OffsetDateTime revokedAt) { this.revokedAt = revokedAt; }
}
