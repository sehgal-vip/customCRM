package com.turno.crm.service;

import com.turno.crm.exception.ResourceNotFoundException;
import com.turno.crm.model.dto.ActivityReportNoteResponse;
import com.turno.crm.model.dto.CreateActivityReportNoteRequest;
import com.turno.crm.model.entity.ActivityReport;
import com.turno.crm.model.entity.ActivityReportNote;
import com.turno.crm.model.entity.User;
import com.turno.crm.model.enums.AuditAction;
import com.turno.crm.model.enums.AuditEntityType;
import com.turno.crm.repository.ActivityReportNoteRepository;
import com.turno.crm.repository.ActivityReportRepository;
import com.turno.crm.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional
public class ActivityReportNoteService {

    private final ActivityReportNoteRepository noteRepository;
    private final ActivityReportRepository reportRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    public ActivityReportNoteService(ActivityReportNoteRepository noteRepository,
                                      ActivityReportRepository reportRepository,
                                      UserRepository userRepository,
                                      AuditService auditService) {
        this.noteRepository = noteRepository;
        this.reportRepository = reportRepository;
        this.userRepository = userRepository;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public List<ActivityReportNoteResponse> list(Long reportId) {
        if (!reportRepository.existsById(reportId)) {
            throw new ResourceNotFoundException("ActivityReport", reportId);
        }
        return noteRepository.findByActivityReportIdOrderByCreatedAtAsc(reportId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public ActivityReportNoteResponse create(Long reportId, CreateActivityReportNoteRequest request, Long actorId) {
        ActivityReport report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("ActivityReport", reportId));

        User actor = userRepository.findById(actorId)
                .orElseThrow(() -> new ResourceNotFoundException("User", actorId));

        ActivityReportNote note = new ActivityReportNote();
        note.setActivityReport(report);
        note.setContent(request.getContent());
        note.setCreatedBy(actor);

        note = noteRepository.save(note);

        auditService.log(AuditEntityType.ACTIVITY_REPORT_NOTE, note.getId(), AuditAction.CREATE, actorId,
                Map.of("reportId", reportId));

        return toResponse(note);
    }

    private ActivityReportNoteResponse toResponse(ActivityReportNote note) {
        ActivityReportNoteResponse resp = new ActivityReportNoteResponse();
        resp.setId(note.getId());
        resp.setActivityReportId(note.getActivityReport().getId());
        resp.setContent(note.getContent());
        resp.setCreatedById(note.getCreatedBy().getId());
        resp.setCreatedByName(note.getCreatedBy().getName());
        resp.setCreatedAt(note.getCreatedAt() != null ? note.getCreatedAt().toString() : null);
        return resp;
    }
}
