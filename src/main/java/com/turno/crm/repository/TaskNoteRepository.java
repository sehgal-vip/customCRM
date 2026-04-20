package com.turno.crm.repository;

import com.turno.crm.model.entity.TaskNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskNoteRepository extends JpaRepository<TaskNote, Long> {

    List<TaskNote> findByTaskIdOrderByCreatedAtAsc(Long taskId);
}
