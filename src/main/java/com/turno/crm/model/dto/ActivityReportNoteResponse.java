package com.turno.crm.model.dto;

public class ActivityReportNoteResponse {

    private Long id;
    private Long activityReportId;
    private String content;
    private Long createdById;
    private String createdByName;
    private String createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getActivityReportId() { return activityReportId; }
    public void setActivityReportId(Long activityReportId) { this.activityReportId = activityReportId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Long getCreatedById() { return createdById; }
    public void setCreatedById(Long createdById) { this.createdById = createdById; }

    public String getCreatedByName() { return createdByName; }
    public void setCreatedByName(String createdByName) { this.createdByName = createdByName; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
