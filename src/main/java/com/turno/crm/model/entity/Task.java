package com.turno.crm.model.entity;

import com.turno.crm.model.enums.TaskPriority;
import com.turno.crm.model.enums.TaskStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tasks")
public class Task extends BaseEntity {

    @Column(name = "title", nullable = false, length = 500)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "status", nullable = false, columnDefinition = "task_status")
    private TaskStatus status = TaskStatus.OPEN;

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "priority", nullable = false, columnDefinition = "task_priority")
    private TaskPriority priority = TaskPriority.MEDIUM;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to", nullable = false)
    private User assignedTo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deal_id")
    private Deal deal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "activity_report_id")
    private ActivityReport activityReport;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "completed_at")
    private OffsetDateTime completedAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TaskNote> taskNotes = new ArrayList<>();

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

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public TaskStatus getStatus() { return status; }
    public void setStatus(TaskStatus status) { this.status = status; }

    public TaskPriority getPriority() { return priority; }
    public void setPriority(TaskPriority priority) { this.priority = priority; }

    public User getAssignedTo() { return assignedTo; }
    public void setAssignedTo(User assignedTo) { this.assignedTo = assignedTo; }

    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }

    public Deal getDeal() { return deal; }
    public void setDeal(Deal deal) { this.deal = deal; }

    public ActivityReport getActivityReport() { return activityReport; }
    public void setActivityReport(ActivityReport activityReport) { this.activityReport = activityReport; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public OffsetDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(OffsetDateTime completedAt) { this.completedAt = completedAt; }

    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }

    public List<TaskNote> getTaskNotes() { return taskNotes; }
    public void setTaskNotes(List<TaskNote> taskNotes) { this.taskNotes = taskNotes; }
}
