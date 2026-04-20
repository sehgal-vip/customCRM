package com.turno.crm.model.dto;

public class GenerateKeyResponse {

    private Long id;
    private String apiKey;
    private String description;

    public GenerateKeyResponse() {}

    public GenerateKeyResponse(Long id, String apiKey, String description) {
        this.id = id;
        this.apiKey = apiKey;
        this.description = description;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
