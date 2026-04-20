package com.turno.crm.model.entity;

import com.turno.crm.model.enums.ContactRole;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.OffsetDateTime;

@Entity
@Table(name = "contacts")
public class Contact extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operator_id", nullable = false)
    private Operator operator;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "role", nullable = false, columnDefinition = "contact_role")
    private ContactRole role;

    @Column(name = "mobile", length = 20)
    private String mobile;

    @Column(name = "email", length = 255)
    private String email;

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

    public Operator getOperator() { return operator; }
    public void setOperator(Operator operator) { this.operator = operator; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public ContactRole getRole() { return role; }
    public void setRole(ContactRole role) { this.role = role; }

    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}
