package com.turno.crm.service;

import com.turno.crm.model.entity.AuditLog;
import com.turno.crm.model.entity.User;
import com.turno.crm.model.enums.AuditAction;
import com.turno.crm.model.enums.AuditEntityType;
import com.turno.crm.repository.AuditLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@Transactional
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void log(AuditEntityType entityType, Long entityId, AuditAction action,
                    Long actorId, Map<String, Object> details) {
        AuditLog entry = new AuditLog();
        entry.setEntityType(entityType);
        entry.setEntityId(entityId);
        entry.setAction(action);

        User actor = new User();
        actor.setId(actorId);
        entry.setActor(actor);

        entry.setDetails(details != null ? details : Map.of());
        auditLogRepository.save(entry);
    }

    @Transactional(readOnly = true)
    public Page<Map<String, Object>> getDealAuditTrail(Long dealId, Pageable pageable) {
        Page<AuditLog> logs = auditLogRepository.findByEntityTypeAndEntityIdOrderByCreatedAtDesc(
                AuditEntityType.DEAL, dealId, pageable);
        return logs.map(log -> {
            Map<String, Object> entry = new HashMap<>();
            entry.put("id", log.getId());
            entry.put("entityType", log.getEntityType().name());
            entry.put("entityId", log.getEntityId());
            entry.put("action", log.getAction().name());
            entry.put("actorName", log.getActor() != null ? log.getActor().getName() : "System");
            entry.put("actorId", log.getActor() != null ? log.getActor().getId() : null);
            entry.put("details", log.getDetails());
            entry.put("createdAt", log.getCreatedAt());
            return entry;
        });
    }
}
