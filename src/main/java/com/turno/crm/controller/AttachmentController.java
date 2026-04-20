package com.turno.crm.controller;

import com.turno.crm.exception.ResourceNotFoundException;
import com.turno.crm.exception.UnauthorizedAccessException;
import com.turno.crm.model.dto.FileUploadResponse;
import com.turno.crm.model.entity.Attachment;
import com.turno.crm.model.entity.Deal;
import com.turno.crm.model.enums.UserRole;
import com.turno.crm.repository.AttachmentRepository;
import com.turno.crm.security.CurrentUserProvider;
import com.turno.crm.service.FileStorageService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/attachments")
public class AttachmentController {

    private final FileStorageService fileStorageService;
    private final AttachmentRepository attachmentRepository;
    private final CurrentUserProvider currentUserProvider;

    public AttachmentController(FileStorageService fileStorageService,
                                 AttachmentRepository attachmentRepository,
                                 CurrentUserProvider currentUserProvider) {
        this.fileStorageService = fileStorageService;
        this.attachmentRepository = attachmentRepository;
        this.currentUserProvider = currentUserProvider;
    }

    @PostMapping("/upload")
    public ResponseEntity<FileUploadResponse> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "subPath", defaultValue = "reports") String subPath) {
        String fileKey = fileStorageService.store(file, subPath);
        FileUploadResponse response = new FileUploadResponse(
                fileKey,
                file.getOriginalFilename(),
                file.getSize());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(@PathVariable Long id) {
        Attachment attachment = attachmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attachment", id));

        // Authorization check
        Long userId = currentUserProvider.getCurrentUserId();
        UserRole role = currentUserProvider.getCurrentUserRole();
        if (role != UserRole.MANAGER) {
            Deal deal = attachment.getActivityReport().getDeal();
            if (!deal.getAssignedAgent().getId().equals(userId)) {
                throw new UnauthorizedAccessException("Not authorized to access this deal's files");
            }
        }

        Resource resource = fileStorageService.load(attachment.getFileKey());

        // Use the sanitized UUID-based stored filename, not the user-supplied original
        String safeFilename = resource.getFilename() != null ? resource.getFilename() : attachment.getFileKey();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + safeFilename + "\"")
                .body(resource);
    }
}
