package com.turno.crm.repository;

import com.turno.crm.model.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByUserIdAndClearedOrderByCreatedAtDesc(Long userId, boolean cleared, Pageable pageable);

    long countByUserIdAndClearedFalse(Long userId);

    List<Notification> findByUserIdAndClearedFalse(Long userId);
}
