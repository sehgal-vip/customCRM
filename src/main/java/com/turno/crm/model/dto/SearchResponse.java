package com.turno.crm.model.dto;

import java.util.List;
import java.util.Map;

public class SearchResponse {

    private List<DealSearchResult> deals;
    private List<OperatorSearchResult> operators;
    private List<TaskSearchResult> tasks;
    private List<UserSearchResult> users;
    private List<ContactSearchResult> contacts;

    public SearchResponse() {}

    public SearchResponse(List<DealSearchResult> deals, List<OperatorSearchResult> operators,
                           List<TaskSearchResult> tasks, List<UserSearchResult> users,
                           List<ContactSearchResult> contacts) {
        this.deals = deals;
        this.operators = operators;
        this.tasks = tasks;
        this.users = users;
        this.contacts = contacts;
    }

    public List<DealSearchResult> getDeals() { return deals; }
    public void setDeals(List<DealSearchResult> deals) { this.deals = deals; }

    public List<OperatorSearchResult> getOperators() { return operators; }
    public void setOperators(List<OperatorSearchResult> operators) { this.operators = operators; }

    public List<TaskSearchResult> getTasks() { return tasks; }
    public void setTasks(List<TaskSearchResult> tasks) { this.tasks = tasks; }

    public List<UserSearchResult> getUsers() { return users; }
    public void setUsers(List<UserSearchResult> users) { this.users = users; }

    public List<ContactSearchResult> getContacts() { return contacts; }
    public void setContacts(List<ContactSearchResult> contacts) { this.contacts = contacts; }

    public static class DealSearchResult {
        private Long id;
        private String name;
        private String operatorName;
        private String currentStage;
        private String status;

        public DealSearchResult() {}
        public DealSearchResult(Long id, String name, String operatorName, String currentStage, String status) {
            this.id = id; this.name = name; this.operatorName = operatorName;
            this.currentStage = currentStage; this.status = status;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getOperatorName() { return operatorName; }
        public void setOperatorName(String operatorName) { this.operatorName = operatorName; }
        public String getCurrentStage() { return currentStage; }
        public void setCurrentStage(String currentStage) { this.currentStage = currentStage; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    public static class OperatorSearchResult {
        private Long id;
        private String companyName;
        private String phone;
        private String email;

        public OperatorSearchResult() {}
        public OperatorSearchResult(Long id, String companyName, String phone, String email) {
            this.id = id; this.companyName = companyName; this.phone = phone; this.email = email;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getCompanyName() { return companyName; }
        public void setCompanyName(String companyName) { this.companyName = companyName; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }

    public static class TaskSearchResult {
        private Long id;
        private String title;
        private String assignedToName;
        private String dealName;
        private String status;
        private String dueDate;

        public TaskSearchResult() {}
        public TaskSearchResult(Long id, String title, String assignedToName, String dealName, String status, String dueDate) {
            this.id = id; this.title = title; this.assignedToName = assignedToName;
            this.dealName = dealName; this.status = status; this.dueDate = dueDate;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getAssignedToName() { return assignedToName; }
        public void setAssignedToName(String assignedToName) { this.assignedToName = assignedToName; }
        public String getDealName() { return dealName; }
        public void setDealName(String dealName) { this.dealName = dealName; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getDueDate() { return dueDate; }
        public void setDueDate(String dueDate) { this.dueDate = dueDate; }
    }

    public static class UserSearchResult {
        private Long id;
        private String name;
        private String email;
        private String role;

        public UserSearchResult() {}
        public UserSearchResult(Long id, String name, String email, String role) {
            this.id = id; this.name = name; this.email = email; this.role = role;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
    }

    public static class ContactSearchResult {
        private Long id;
        private String name;
        private String role;
        private String mobile;
        private String email;
        private Long operatorId;
        private String operatorName;

        public ContactSearchResult() {}
        public ContactSearchResult(Long id, String name, String role, String mobile, String email, Long operatorId, String operatorName) {
            this.id = id; this.name = name; this.role = role; this.mobile = mobile;
            this.email = email; this.operatorId = operatorId; this.operatorName = operatorName;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        public String getMobile() { return mobile; }
        public void setMobile(String mobile) { this.mobile = mobile; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public Long getOperatorId() { return operatorId; }
        public void setOperatorId(Long operatorId) { this.operatorId = operatorId; }
        public String getOperatorName() { return operatorName; }
        public void setOperatorName(String operatorName) { this.operatorName = operatorName; }
    }
}
