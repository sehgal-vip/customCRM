package com.turno.crm.model.entity;

import com.turno.crm.model.enums.DocStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.OffsetDateTime;

@Entity
@Table(name = "deal_documents", uniqueConstraints = {
    @UniqueConstraint(name = "uq_deal_checklist_item", columnNames = {"deal_id", "checklist_item_id"})
})
public class DealDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deal_id", nullable = false)
    private Deal deal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "checklist_item_id", nullable = false)
    private DocumentChecklistItem checklistItem;

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "status", nullable = false, columnDefinition = "doc_status")
    private DocStatus status = DocStatus.NOT_STARTED;

    @Column(name = "file_key", length = 1000)
    private String fileKey;

    @Column(name = "uploaded_at")
    private OffsetDateTime uploadedAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.updatedAt = OffsetDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Deal getDeal() { return deal; }
    public void setDeal(Deal deal) { this.deal = deal; }

    public DocumentChecklistItem getChecklistItem() { return checklistItem; }
    public void setChecklistItem(DocumentChecklistItem checklistItem) { this.checklistItem = checklistItem; }

    public DocStatus getStatus() { return status; }
    public void setStatus(DocStatus status) { this.status = status; }

    public String getFileKey() { return fileKey; }
    public void setFileKey(String fileKey) { this.fileKey = fileKey; }

    public OffsetDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(OffsetDateTime uploadedAt) { this.uploadedAt = uploadedAt; }

    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}
