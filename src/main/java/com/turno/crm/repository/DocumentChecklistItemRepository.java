package com.turno.crm.repository;

import com.turno.crm.model.entity.DocumentChecklistItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentChecklistItemRepository extends JpaRepository<DocumentChecklistItem, Long> {

    List<DocumentChecklistItem> findByActiveTrue();
}
