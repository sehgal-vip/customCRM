package com.turno.crm.model.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "activity_report_notes")
public class ActivityReportNote extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "activity_report_id", nullable = false)
    private ActivityReport activityReport;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    public ActivityReport getActivityReport() { return activityReport; }
    public void setActivityReport(ActivityReport activityReport) { this.activityReport = activityReport; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }
}
