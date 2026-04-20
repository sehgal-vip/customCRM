package com.turno.crm.controller;

import com.turno.crm.model.dto.WebhookLeadRequest;
import com.turno.crm.model.dto.WebhookLeadResponse;
import com.turno.crm.service.WebhookService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/webhook")
public class WebhookController {

    private final WebhookService webhookService;

    public WebhookController(WebhookService webhookService) {
        this.webhookService = webhookService;
    }

    @PostMapping("/leads")
    public ResponseEntity<WebhookLeadResponse> processLead(@Valid @RequestBody WebhookLeadRequest request) {
        WebhookLeadResponse response = webhookService.processLead(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
