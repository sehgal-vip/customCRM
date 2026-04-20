package com.turno.crm.controller;

import com.turno.crm.exception.ResourceNotFoundException;
import com.turno.crm.exception.UnauthorizedAccessException;
import com.turno.crm.model.dto.DocumentChecklistResponse;
import com.turno.crm.model.dto.DocumentCompletionResponse;
import com.turno.crm.model.dto.UpdateDocumentStatusRequest;
import com.turno.crm.model.entity.Deal;
import com.turno.crm.model.enums.UserRole;
import com.turno.crm.repository.DealRepository;
import com.turno.crm.security.CurrentUserProvider;
import com.turno.crm.service.DocumentChecklistService;
import com.turno.crm.service.FileStorageService;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/deals/{dealId}/documents")
public class DocumentController {

    private final DocumentChecklistService documentChecklistService;
    private final FileStorageService fileStorageService;
    private final CurrentUserProvider currentUserProvider;
    private final DealRepository dealRepository;

    public DocumentController(DocumentChecklistService documentChecklistService,
                               FileStorageService fileStorageService,
                               CurrentUserProvider currentUserProvider,
                               DealRepository dealRepository) {
        this.documentChecklistService = documentChecklistService;
        this.fileStorageService = fileStorageService;
        this.currentUserProvider = currentUserProvider;
        this.dealRepository = dealRepository;
    }

    @GetMapping
    public ResponseEntity<DocumentCompletionResponse> getDocuments(@PathVariable Long dealId) {
        return ResponseEntity.ok(documentChecklistService.getDocuments(dealId));
    }

    @PutMapping("/{documentId}/status")
    public ResponseEntity<DocumentChecklistResponse> updateStatus(
            @PathVariable Long dealId,
            @PathVariable Long documentId,
            @Valid @RequestBody UpdateDocumentStatusRequest request) {
        DocumentChecklistResponse response = documentChecklistService.updateStatus(
                dealId, documentId, request.getStatus(), currentUserProvider.getCurrentUserId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{documentId}/upload")
    public ResponseEntity<DocumentChecklistResponse> uploadFile(
            @PathVariable Long dealId,
            @PathVariable Long documentId,
            @RequestParam("file") MultipartFile file) {
        DocumentChecklistResponse response = documentChecklistService.uploadFile(
                dealId, documentId, file, currentUserProvider.getCurrentUserId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{documentId}/download")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable Long dealId,
            @PathVariable Long documentId) {
        // Authorization check
        Long userId = currentUserProvider.getCurrentUserId();
        UserRole role = currentUserProvider.getCurrentUserRole();
        if (role != UserRole.MANAGER) {
            Deal deal = dealRepository.findById(dealId)
                    .orElseThrow(() -> new ResourceNotFoundException("Deal", dealId));
            if (!deal.getAssignedAgent().getId().equals(userId)) {
                throw new UnauthorizedAccessException("Not authorized to access this deal's files");
            }
        }

        // Get the document to find the file key
        DocumentCompletionResponse docs = documentChecklistService.getDocuments(dealId);
        String fileKey = docs.getItems().stream()
                .filter(item -> item.getId().equals(documentId))
                .findFirst()
                .map(item -> item.getFileKey())
                .orElseThrow(() -> new ResourceNotFoundException("DealDocument", documentId));

        if (fileKey == null || fileKey.isBlank()) {
            throw new com.turno.crm.exception.BusinessRuleViolationException("No file uploaded for this document");
        }

        Resource resource = fileStorageService.load(fileKey);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
}
