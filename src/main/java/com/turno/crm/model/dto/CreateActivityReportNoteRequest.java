package com.turno.crm.model.dto;

import jakarta.validation.constraints.NotBlank;

public class CreateActivityReportNoteRequest {

    @NotBlank
    private String content;

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
