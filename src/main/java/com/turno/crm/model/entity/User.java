package com.turno.crm.model.entity;

import com.turno.crm.model.enums.UserRole;
import com.turno.crm.model.enums.UserStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
public class User extends BaseEntity {

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "role", nullable = false, columnDefinition = "user_role")
    private UserRole role;

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "status", nullable = false, columnDefinition = "user_status")
    private UserStatus status = UserStatus.ACTIVE;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "user_regions",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "region_id")
    )
    private Set<Region> regions = new HashSet<>();

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

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }

    public UserStatus getStatus() { return status; }
    public void setStatus(UserStatus status) { this.status = status; }

    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Set<Region> getRegions() { return regions; }
    public void setRegions(Set<Region> regions) { this.regions = regions; }
}
