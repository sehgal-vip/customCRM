package com.turno.crm.model.entity;

import com.turno.crm.model.enums.DealStage;
import com.turno.crm.model.enums.DocRequirement;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "document_checklist_items")
public class DocumentChecklistItem extends BaseEntity {

    @Column(name = "document_name", nullable = false, length = 255)
    private String documentName;

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "requirement", nullable = false, columnDefinition = "doc_requirement")
    private DocRequirement requirement;

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "required_by_stage", nullable = false, columnDefinition = "deal_stage")
    private DealStage requiredByStage;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    public String getDocumentName() { return documentName; }
    public void setDocumentName(String documentName) { this.documentName = documentName; }

    public DocRequirement getRequirement() { return requirement; }
    public void setRequirement(DocRequirement requirement) { this.requirement = requirement; }

    public DealStage getRequiredByStage() { return requiredByStage; }
    public void setRequiredByStage(DealStage requiredByStage) { this.requiredByStage = requiredByStage; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}
