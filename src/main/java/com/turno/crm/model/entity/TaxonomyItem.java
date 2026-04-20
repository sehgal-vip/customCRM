package com.turno.crm.model.entity;

import com.turno.crm.model.enums.TaxonomyType;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.OffsetDateTime;

@Entity
@Table(name = "taxonomy_items", uniqueConstraints = {
    @UniqueConstraint(name = "uq_taxonomy_type_value", columnNames = {"taxonomy_type", "value"})
})
public class TaxonomyItem extends BaseEntity {

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "taxonomy_type", nullable = false, columnDefinition = "taxonomy_type")
    private TaxonomyType taxonomyType;

    @Column(name = "value", nullable = false, length = 255)
    private String value;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    @Override
    protected void onCreate() {
        super.onCreate();
        this.updatedAt = OffsetDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }

    public TaxonomyType getTaxonomyType() { return taxonomyType; }
    public void setTaxonomyType(TaxonomyType taxonomyType) { this.taxonomyType = taxonomyType; }

    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}
