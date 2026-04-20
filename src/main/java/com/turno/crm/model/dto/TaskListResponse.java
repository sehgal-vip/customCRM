package com.turno.crm.model.dto;

import java.util.List;

public class TaskListResponse {

    private List<TaskItem> overdue;
    private List<TaskItem> upcoming;
    private int overdueCount;
    private int upcomingCount;

    public TaskListResponse() {}

    public TaskListResponse(List<TaskItem> overdue, List<TaskItem> upcoming) {
        this.overdue = overdue;
        this.upcoming = upcoming;
        this.overdueCount = overdue.size();
        this.upcomingCount = upcoming.size();
    }

    public List<TaskItem> getOverdue() { return overdue; }
    public void setOverdue(List<TaskItem> overdue) { this.overdue = overdue; }

    public List<TaskItem> getUpcoming() { return upcoming; }
    public void setUpcoming(List<TaskItem> upcoming) { this.upcoming = upcoming; }

    public int getOverdueCount() { return overdueCount; }
    public void setOverdueCount(int overdueCount) { this.overdueCount = overdueCount; }

    public int getUpcomingCount() { return upcomingCount; }
    public void setUpcomingCount(int upcomingCount) { this.upcomingCount = upcomingCount; }
}
