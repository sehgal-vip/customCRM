package com.turno.crm.repository;

import com.turno.crm.model.entity.AuditLog;
import com.turno.crm.model.enums.AuditEntityType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    Page<AuditLog> findByEntityTypeAndEntityIdOrderByCreatedAtDesc(AuditEntityType entityType, Long entityId, Pageable pageable);
}
