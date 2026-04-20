package com.turno.crm.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public class AttachmentRequest {

    @NotBlank(message = "File name is required")
    private String fileName;

    @NotBlank(message = "File key is required")
    private String fileKey;

    @Positive(message = "File size must be positive")
    private long fileSize;

    @NotBlank(message = "Category tag is required")
    private String categoryTag;

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getFileKey() { return fileKey; }
    public void setFileKey(String fileKey) { this.fileKey = fileKey; }

    public long getFileSize() { return fileSize; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }

    public String getCategoryTag() { return categoryTag; }
    public void setCategoryTag(String categoryTag) { this.categoryTag = categoryTag; }
}
