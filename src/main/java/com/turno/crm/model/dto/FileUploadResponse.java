package com.turno.crm.model.dto;

public class FileUploadResponse {

    private String fileKey;
    private String fileName;
    private long fileSize;

    public FileUploadResponse() {}

    public FileUploadResponse(String fileKey, String fileName, long fileSize) {
        this.fileKey = fileKey;
        this.fileName = fileName;
        this.fileSize = fileSize;
    }

    public String getFileKey() { return fileKey; }
    public void setFileKey(String fileKey) { this.fileKey = fileKey; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public long getFileSize() { return fileSize; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }
}
