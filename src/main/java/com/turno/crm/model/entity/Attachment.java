package com.turno.crm.model.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "attachments")
public class Attachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "activity_report_id", nullable = false)
    private ActivityReport activityReport;

    @Column(name = "file_name", nullable = false, length = 500)
    private String fileName;

    @Column(name = "file_key", nullable = false, length = 1000)
    private String fileKey;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "category_tag", nullable = false, length = 100)
    private String categoryTag;

    @Column(name = "uploaded_at", nullable = false)
    private OffsetDateTime uploadedAt;

    @PrePersist
    protected void onCreate() {
        if (this.uploadedAt == null) {
            this.uploadedAt = OffsetDateTime.now();
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public ActivityReport getActivityReport() { return activityReport; }
    public void setActivityReport(ActivityReport activityReport) { this.activityReport = activityReport; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getFileKey() { return fileKey; }
    public void setFileKey(String fileKey) { this.fileKey = fileKey; }

    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }

    public String getCategoryTag() { return categoryTag; }
    public void setCategoryTag(String categoryTag) { this.categoryTag = categoryTag; }

    public OffsetDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(OffsetDateTime uploadedAt) { this.uploadedAt = uploadedAt; }
}
