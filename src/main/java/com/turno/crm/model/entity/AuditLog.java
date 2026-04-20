package com.turno.crm.model.entity;

import com.turno.crm.model.enums.AuditAction;
import com.turno.crm.model.enums.AuditEntityType;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "audit_log")
public class AuditLog extends BaseEntity {

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "entity_type", nullable = false, columnDefinition = "audit_entity_type")
    private AuditEntityType entityType;

    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "action", nullable = false, columnDefinition = "audit_action")
    private AuditAction action;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id", nullable = false)
    private User actor;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "details", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> details = new HashMap<>();

    public AuditEntityType getEntityType() { return entityType; }
    public void setEntityType(AuditEntityType entityType) { this.entityType = entityType; }

    public Long getEntityId() { return entityId; }
    public void setEntityId(Long entityId) { this.entityId = entityId; }

    public AuditAction getAction() { return action; }
    public void setAction(AuditAction action) { this.action = action; }

    public User getActor() { return actor; }
    public void setActor(User actor) { this.actor = actor; }

    public Map<String, Object> getDetails() { return details; }
    public void setDetails(Map<String, Object> details) { this.details = details; }
}
