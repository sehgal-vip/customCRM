package com.turno.crm.repository;

import com.turno.crm.model.entity.SystemReminder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SystemReminderRepository extends JpaRepository<SystemReminder, Long> {

    List<SystemReminder> findByDealIdAndDismissedFalse(Long dealId);
}
