package com.turno.crm.model.dto;

import java.time.OffsetDateTime;

public class AttachmentResponse {

    private Long id;
    private String fileName;
    private String fileKey;
    private long fileSize;
    private String categoryTag;
    private OffsetDateTime uploadedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getFileKey() { return fileKey; }
    public void setFileKey(String fileKey) { this.fileKey = fileKey; }

    public long getFileSize() { return fileSize; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }

    public String getCategoryTag() { return categoryTag; }
    public void setCategoryTag(String categoryTag) { this.categoryTag = categoryTag; }

    public OffsetDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(OffsetDateTime uploadedAt) { this.uploadedAt = uploadedAt; }
}
