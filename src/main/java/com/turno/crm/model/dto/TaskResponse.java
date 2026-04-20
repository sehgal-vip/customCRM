package com.turno.crm.model.dto;

public class TaskResponse {

    private Long id;
    private String title;
    private String description;
    private String status;
    private String priority;
    private Long assignedToId;
    private String assignedToName;
    private Long createdById;
    private String createdByName;
    private Long dealId;
    private String dealName;
    private Long activityReportId;
    private String dueDate;
    private String completedAt;
    private String createdAt;
    private boolean overdue;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public Long getAssignedToId() { return assignedToId; }
    public void setAssignedToId(Long assignedToId) { this.assignedToId = assignedToId; }

    public String getAssignedToName() { return assignedToName; }
    public void setAssignedToName(String assignedToName) { this.assignedToName = assignedToName; }

    public Long getCreatedById() { return createdById; }
    public void setCreatedById(Long createdById) { this.createdById = createdById; }

    public String getCreatedByName() { return createdByName; }
    public void setCreatedByName(String createdByName) { this.createdByName = createdByName; }

    public Long getDealId() { return dealId; }
    public void setDealId(Long dealId) { this.dealId = dealId; }

    public String getDealName() { return dealName; }
    public void setDealName(String dealName) { this.dealName = dealName; }

    public Long getActivityReportId() { return activityReportId; }
    public void setActivityReportId(Long activityReportId) { this.activityReportId = activityReportId; }

    public String getDueDate() { return dueDate; }
    public void setDueDate(String dueDate) { this.dueDate = dueDate; }

    public String getCompletedAt() { return completedAt; }
    public void setCompletedAt(String completedAt) { this.completedAt = completedAt; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public boolean isOverdue() { return overdue; }
    public void setOverdue(boolean overdue) { this.overdue = overdue; }
}
