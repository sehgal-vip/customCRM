package com.turno.crm.model.entity;

import com.turno.crm.model.enums.OperatorType;
import com.turno.crm.model.enums.PrimaryUseCase;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "operators", uniqueConstraints = {
    @UniqueConstraint(name = "uq_operator_name_phone", columnNames = {"company_name", "phone"})
})
public class Operator extends BaseEntity {

    @Column(name = "company_name", nullable = false, length = 255)
    private String companyName;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "email", length = 255)
    private String email;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id")
    private Region region;

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "operator_type", columnDefinition = "operator_type")
    private OperatorType operatorType;

    @Column(name = "referral_source", length = 255)
    private String referralSource;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "referred_by_operator_id")
    private Operator referredByOperator;

    @Column(name = "fleet_size")
    private Integer fleetSize;

    @Column(name = "num_routes")
    private Integer numRoutes;

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "primary_use_case", columnDefinition = "primary_use_case")
    private PrimaryUseCase primaryUseCase;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @OneToMany(mappedBy = "operator", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Contact> contacts = new ArrayList<>();

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

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Region getRegion() { return region; }
    public void setRegion(Region region) { this.region = region; }

    public OperatorType getOperatorType() { return operatorType; }
    public void setOperatorType(OperatorType operatorType) { this.operatorType = operatorType; }

    public String getReferralSource() { return referralSource; }
    public void setReferralSource(String referralSource) { this.referralSource = referralSource; }

    public Operator getReferredByOperator() { return referredByOperator; }
    public void setReferredByOperator(Operator referredByOperator) { this.referredByOperator = referredByOperator; }

    public Integer getFleetSize() { return fleetSize; }
    public void setFleetSize(Integer fleetSize) { this.fleetSize = fleetSize; }

    public Integer getNumRoutes() { return numRoutes; }
    public void setNumRoutes(Integer numRoutes) { this.numRoutes = numRoutes; }

    public PrimaryUseCase getPrimaryUseCase() { return primaryUseCase; }
    public void setPrimaryUseCase(PrimaryUseCase primaryUseCase) { this.primaryUseCase = primaryUseCase; }

    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }

    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }

    public List<Contact> getContacts() { return contacts; }
    public void setContacts(List<Contact> contacts) { this.contacts = contacts; }
}
