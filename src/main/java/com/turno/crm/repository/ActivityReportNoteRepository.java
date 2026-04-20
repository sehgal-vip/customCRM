package com.turno.crm.repository;

import com.turno.crm.model.entity.ActivityReportNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActivityReportNoteRepository extends JpaRepository<ActivityReportNote, Long> {

    List<ActivityReportNote> findByActivityReportIdOrderByCreatedAtAsc(Long activityReportId);
}
