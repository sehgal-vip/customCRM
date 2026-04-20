package com.turno.crm.service;

import com.turno.crm.exception.BusinessRuleViolationException;
import com.turno.crm.exception.ResourceNotFoundException;
import com.turno.crm.model.dto.DocumentChecklistResponse;
import com.turno.crm.model.dto.DocumentCompletionResponse;
import com.turno.crm.model.entity.DealDocument;
import com.turno.crm.model.enums.AuditAction;
import com.turno.crm.model.enums.AuditEntityType;
import com.turno.crm.model.enums.DocStatus;
import com.turno.crm.repository.DealDocumentRepository;
import com.turno.crm.repository.DealRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class DocumentChecklistService {

    private final DealDocumentRepository dealDocumentRepository;
    private final DealRepository dealRepository;
    private final FileStorageService fileStorageService;
    private final AuditService auditService;

    public DocumentChecklistService(DealDocumentRepository dealDocumentRepository,
                                     DealRepository dealRepository,
                                     FileStorageService fileStorageService,
                                     AuditService auditService) {
        this.dealDocumentRepository = dealDocumentRepository;
        this.dealRepository = dealRepository;
        this.fileStorageService = fileStorageService;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public DocumentCompletionResponse getDocuments(Long dealId) {
        validateDealExists(dealId);

        List<DealDocument> documents = dealDocumentRepository.findByDealId(dealId);
        List<DocumentChecklistResponse> items = documents.stream()
                .map(this::toResponse)
                .toList();

        long mandatoryTotal = dealDocumentRepository.countMandatoryTotal(dealId);
        long mandatoryComplete = dealDocumentRepository.countMandatoryComplete(dealId);
        double percentage = mandatoryTotal == 0 ? 100.0
                : Math.round((double) mandatoryComplete / mandatoryTotal * 10000.0) / 100.0;

        DocumentCompletionResponse resp = new DocumentCompletionResponse();
        resp.setMandatoryComplete((int) mandatoryComplete);
        resp.setMandatoryTotal((int) mandatoryTotal);
        resp.setPercentage(percentage);
        resp.setItems(items);
        return resp;
    }

    public DocumentChecklistResponse updateStatus(Long dealId, Long documentId, String status, Long actorId) {
        DealDocument doc = findDocument(dealId, documentId);

        DocStatus newStatus;
        try {
            newStatus = DocStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            throw new BusinessRuleViolationException("Invalid document status: " + status
                    + ". Valid values: NOT_STARTED, REQUESTED, RECEIVED, VERIFIED");
        }

        DocStatus oldStatus = doc.getStatus();
        doc.setStatus(newStatus);
        doc = dealDocumentRepository.save(doc);

        auditService.log(AuditEntityType.DEAL_DOCUMENT, documentId, AuditAction.UPDATE, actorId,
                Map.of("dealId", dealId, "fromStatus", oldStatus.name(), "toStatus", newStatus.name()));

        return toResponse(doc);
    }

    public DocumentChecklistResponse uploadFile(Long dealId, Long documentId, MultipartFile file, Long actorId) {
        DealDocument doc = findDocument(dealId, documentId);

        String fileKey = fileStorageService.store(file, "deals/" + dealId + "/documents");
        doc.setFileKey(fileKey);
        doc.setUploadedAt(OffsetDateTime.now());
        doc.setStatus(DocStatus.RECEIVED);
        doc = dealDocumentRepository.save(doc);

        auditService.log(AuditEntityType.DEAL_DOCUMENT, documentId, AuditAction.UPDATE, actorId,
                Map.of("dealId", dealId, "action", "fileUpload", "fileKey", fileKey));

        return toResponse(doc);
    }

    @Transactional(readOnly = true)
    public DocumentCompletionResponse getCompletionMetrics(Long dealId) {
        validateDealExists(dealId);

        long mandatoryTotal = dealDocumentRepository.countMandatoryTotal(dealId);
        long mandatoryComplete = dealDocumentRepository.countMandatoryComplete(dealId);
        double percentage = mandatoryTotal == 0 ? 100.0
                : Math.round((double) mandatoryComplete / mandatoryTotal * 10000.0) / 100.0;

        DocumentCompletionResponse resp = new DocumentCompletionResponse();
        resp.setMandatoryComplete((int) mandatoryComplete);
        resp.setMandatoryTotal((int) mandatoryTotal);
        resp.setPercentage(percentage);
        return resp;
    }

    private DealDocument findDocument(Long dealId, Long documentId) {
        DealDocument doc = dealDocumentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("DealDocument", documentId));
        if (!doc.getDeal().getId().equals(dealId)) {
            throw new ResourceNotFoundException("DealDocument", documentId);
        }
        return doc;
    }

    private void validateDealExists(Long dealId) {
        if (!dealRepository.existsById(dealId)) {
            throw new ResourceNotFoundException("Deal", dealId);
        }
    }

    private DocumentChecklistResponse toResponse(DealDocument doc) {
        DocumentChecklistResponse resp = new DocumentChecklistResponse();
        resp.setId(doc.getId());
        resp.setDocumentName(doc.getChecklistItem().getDocumentName());
        resp.setRequirement(doc.getChecklistItem().getRequirement().name());
        resp.setRequiredByStage(doc.getChecklistItem().getRequiredByStage().name());
        resp.setStatus(doc.getStatus().name());
        resp.setFileKey(doc.getFileKey());
        resp.setHasFile(doc.getFileKey() != null && !doc.getFileKey().isBlank());
        return resp;
    }
}
