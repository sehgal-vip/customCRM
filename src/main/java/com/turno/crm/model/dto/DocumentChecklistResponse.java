package com.turno.crm.model.dto;

public class DocumentChecklistResponse {

    private Long id;
    private String documentName;
    private String requirement;
    private String requiredByStage;
    private String status;
    private String fileKey;
    private boolean hasFile;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDocumentName() { return documentName; }
    public void setDocumentName(String documentName) { this.documentName = documentName; }

    public String getRequirement() { return requirement; }
    public void setRequirement(String requirement) { this.requirement = requirement; }

    public String getRequiredByStage() { return requiredByStage; }
    public void setRequiredByStage(String requiredByStage) { this.requiredByStage = requiredByStage; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getFileKey() { return fileKey; }
    public void setFileKey(String fileKey) { this.fileKey = fileKey; }

    public boolean isHasFile() { return hasFile; }
    public void setHasFile(boolean hasFile) { this.hasFile = hasFile; }
}
