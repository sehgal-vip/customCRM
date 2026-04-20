package com.turno.crm.repository;

import com.turno.crm.model.entity.NotificationPreference;
import com.turno.crm.model.enums.NotificationEventType;
import com.turno.crm.model.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationPreferenceRepository extends JpaRepository<NotificationPreference, Long> {

    List<NotificationPreference> findByRole(UserRole role);

    Optional<NotificationPreference> findByRoleAndEventType(UserRole role, NotificationEventType eventType);
}
