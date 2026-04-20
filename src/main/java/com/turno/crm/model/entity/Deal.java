package com.turno.crm.model.entity;

import com.turno.crm.model.enums.*;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "deals")
public class Deal extends BaseEntity {

    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operator_id", nullable = false)
    private Operator operator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_agent_id", nullable = false)
    private User assignedAgent;

    @Column(name = "fleet_size")
    private Integer fleetSize;

    @Column(name = "estimated_monthly_value", precision = 14, scale = 2)
    private BigDecimal estimatedMonthlyValue;

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "lead_source", nullable = false, columnDefinition = "lead_source")
    private LeadSource leadSource;

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "current_stage", nullable = false, columnDefinition = "deal_stage")
    private DealStage currentStage = DealStage.STAGE_1;

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "sub_status", columnDefinition = "stage5_sub_status")
    private Stage5SubStatus subStatus;

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "status", nullable = false, columnDefinition = "deal_status")
    private DealStatus status = DealStatus.ACTIVE;

    @Column(name = "archived_reason", length = 100)
    private String archivedReason;

    @Column(name = "archived_reason_text", columnDefinition = "TEXT")
    private String archivedReasonText;

    @Column(name = "reopened", nullable = false)
    private Boolean reopened = false;

    @Column(name = "backfilled", nullable = false)
    private Boolean backfilled = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "backfill_approved_by")
    private User backfillApprovedBy;

    @Column(name = "original_start_date")
    private LocalDate originalStartDate;

    @Column(name = "source_event_id", unique = true, length = 255)
    private String sourceEventId;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @OneToMany(mappedBy = "deal", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ActivityReport> activityReports = new ArrayList<>();

    @OneToMany(mappedBy = "deal", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Task> tasks = new ArrayList<>();

    @OneToMany(mappedBy = "deal", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DealDocument> dealDocuments = new ArrayList<>();

    @PrePersist
    @Override
    protected void onCreate() {
        super.onCreate();
        this.updatedAt = OffsetDateTime.now();
        validateSubStatus();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = OffsetDateTime.now();
        validateSubStatus();
    }

    private void validateSubStatus() {
        if (currentStage == DealStage.STAGE_4 && subStatus == null) {
            throw new IllegalStateException("sub_status is required when stage is STAGE_4");
        }
        if (currentStage != DealStage.STAGE_4 && subStatus != null) {
            throw new IllegalStateException("sub_status must be null when stage is not STAGE_4");
        }
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Operator getOperator() { return operator; }
    public void setOperator(Operator operator) { this.operator = operator; }

    public User getAssignedAgent() { return assignedAgent; }
    public void setAssignedAgent(User assignedAgent) { this.assignedAgent = assignedAgent; }

    public Integer getFleetSize() { return fleetSize; }
    public void setFleetSize(Integer fleetSize) { this.fleetSize = fleetSize; }

    public BigDecimal getEstimatedMonthlyValue() { return estimatedMonthlyValue; }
    public void setEstimatedMonthlyValue(BigDecimal estimatedMonthlyValue) { this.estimatedMonthlyValue = estimatedMonthlyValue; }

    public LeadSource getLeadSource() { return leadSource; }
    public void setLeadSource(LeadSource leadSource) { this.leadSource = leadSource; }

    public DealStage getCurrentStage() { return currentStage; }
    public void setCurrentStage(DealStage currentStage) { this.currentStage = currentStage; }

    public Stage5SubStatus getSubStatus() { return subStatus; }
    public void setSubStatus(Stage5SubStatus subStatus) { this.subStatus = subStatus; }

    public DealStatus getStatus() { return status; }
    public void setStatus(DealStatus status) { this.status = status; }

    public String getArchivedReason() { return archivedReason; }
    public void setArchivedReason(String archivedReason) { this.archivedReason = archivedReason; }

    public String getArchivedReasonText() { return archivedReasonText; }
    public void setArchivedReasonText(String archivedReasonText) { this.archivedReasonText = archivedReasonText; }

    public Boolean getReopened() { return reopened; }
    public void setReopened(Boolean reopened) { this.reopened = reopened; }

    public Boolean getBackfilled() { return backfilled; }
    public void setBackfilled(Boolean backfilled) { this.backfilled = backfilled; }

    public User getBackfillApprovedBy() { return backfillApprovedBy; }
    public void setBackfillApprovedBy(User backfillApprovedBy) { this.backfillApprovedBy = backfillApprovedBy; }

    public LocalDate getOriginalStartDate() { return originalStartDate; }
    public void setOriginalStartDate(LocalDate originalStartDate) { this.originalStartDate = originalStartDate; }

    public String getSourceEventId() { return sourceEventId; }
    public void setSourceEventId(String sourceEventId) { this.sourceEventId = sourceEventId; }

    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }

    public List<ActivityReport> getActivityReports() { return activityReports; }
    public void setActivityReports(List<ActivityReport> activityReports) { this.activityReports = activityReports; }

    public List<Task> getTasks() { return tasks; }
    public void setTasks(List<Task> tasks) { this.tasks = tasks; }

    public List<DealDocument> getDealDocuments() { return dealDocuments; }
    public void setDealDocuments(List<DealDocument> dealDocuments) { this.dealDocuments = dealDocuments; }
}
