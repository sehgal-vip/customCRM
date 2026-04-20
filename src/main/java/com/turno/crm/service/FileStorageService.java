package com.turno.crm.service;

import com.turno.crm.exception.BusinessRuleViolationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            ".pdf", ".jpg", ".jpeg", ".png", ".gif",
            ".doc", ".docx", ".xls", ".xlsx", ".csv", ".txt"
    );

    @Value("${turno.upload.base-path}")
    private String basePath;

    public String store(MultipartFile file, String subPath) {
        try {
            if (file.isEmpty()) {
                throw new BusinessRuleViolationException("Cannot store empty file");
            }

            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            if (!extension.isEmpty() && !ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
                throw new BusinessRuleViolationException("File type not allowed: " + extension);
            }

            String storedName = UUID.randomUUID() + extension;
            String relativeKey = subPath + "/" + storedName;

            Path targetDir = Paths.get(basePath).resolve(subPath);
            Files.createDirectories(targetDir);

            Path targetPath = targetDir.resolve(storedName);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            return relativeKey;
        } catch (IOException e) {
            throw new BusinessRuleViolationException("Failed to store file: " + e.getMessage());
        }
    }

    public Resource load(String fileKey) {
        try {
            Path filePath = Paths.get(basePath).resolve(fileKey).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            }
            throw new BusinessRuleViolationException("File not found: " + fileKey);
        } catch (MalformedURLException e) {
            throw new BusinessRuleViolationException("File not found: " + fileKey);
        }
    }
}
