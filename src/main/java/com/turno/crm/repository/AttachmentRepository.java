package com.turno.crm.repository;

import com.turno.crm.model.entity.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, Long> {

    List<Attachment> findByActivityReportId(Long reportId);
}
