package com.turno.crm.controller;

import com.turno.crm.model.dto.CsvImportRequest;
import com.turno.crm.model.dto.CsvImportResponse;
import com.turno.crm.model.dto.CsvPreviewResponse;
import com.turno.crm.security.CurrentUserProvider;
import com.turno.crm.service.CsvService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
public class CsvController {

    private final CsvService csvService;
    private final CurrentUserProvider currentUserProvider;

    // Store last preview per session in a simple thread-safe way
    // In production, use a cache or session store
    private final java.util.concurrent.ConcurrentHashMap<Long, CsvPreviewResponse> previewCache =
            new java.util.concurrent.ConcurrentHashMap<>();

    public CsvController(CsvService csvService, CurrentUserProvider currentUserProvider) {
        this.csvService = csvService;
        this.currentUserProvider = currentUserProvider;
    }

    @PostMapping("/api/v1/import/csv/upload")
    public ResponseEntity<CsvPreviewResponse> uploadCsv(@RequestParam("file") MultipartFile file) throws IOException {
        CsvPreviewResponse preview = csvService.parseAndValidate(file.getInputStream());
        // Evict cache if it grows too large to prevent unbounded memory use
        if (previewCache.size() > 50) {
            previewCache.clear();
        }
        // Cache preview for confirm step
        previewCache.put(currentUserProvider.getCurrentUserId(), preview);
        return ResponseEntity.ok(preview);
    }

    @PostMapping("/api/v1/import/csv/confirm")
    public ResponseEntity<CsvImportResponse> confirmImport(@Valid @RequestBody CsvImportRequest request) {
        Long userId = currentUserProvider.getCurrentUserId();
        CsvPreviewResponse preview = previewCache.get(userId);
        if (preview == null) {
            return ResponseEntity.badRequest().build();
        }

        CsvImportResponse result = csvService.executeImport(preview, request, userId);
        previewCache.remove(userId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/api/v1/export/deals")
    public ResponseEntity<byte[]> exportDeals(@RequestParam(defaultValue = "csv") String format) {
        byte[] data = csvService.exportDeals(format);

        String contentType;
        String filename;
        if ("xlsx".equalsIgnoreCase(format)) {
            contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            filename = "deals_export.xlsx";
        } else {
            contentType = "text/csv";
            filename = "deals_export.csv";
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType(contentType))
                .body(data);
    }
}
