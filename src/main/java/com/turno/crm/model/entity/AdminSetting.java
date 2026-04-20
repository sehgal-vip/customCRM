package com.turno.crm.model.entity;

import com.turno.crm.model.enums.AdminSettingType;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.Map;

@Entity
@Table(name = "admin_settings", uniqueConstraints = {
    @UniqueConstraint(name = "uq_setting_type_key", columnNames = {"setting_type", "setting_key"})
})
public class AdminSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "setting_type", nullable = false, columnDefinition = "admin_setting_type")
    private AdminSettingType settingType;

    @Column(name = "setting_key", nullable = false, length = 100)
    private String settingKey;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "setting_value", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> settingValue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private User updatedBy;

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

    public AdminSettingType getSettingType() { return settingType; }
    public void setSettingType(AdminSettingType settingType) { this.settingType = settingType; }

    public String getSettingKey() { return settingKey; }
    public void setSettingKey(String settingKey) { this.settingKey = settingKey; }

    public Map<String, Object> getSettingValue() { return settingValue; }
    public void setSettingValue(Map<String, Object> settingValue) { this.settingValue = settingValue; }

    public User getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(User updatedBy) { this.updatedBy = updatedBy; }

    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}
