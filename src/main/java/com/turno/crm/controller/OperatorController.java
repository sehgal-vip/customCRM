package com.turno.crm.controller;

import com.turno.crm.model.dto.*;
import com.turno.crm.security.CurrentUserProvider;
import com.turno.crm.service.ContactService;
import com.turno.crm.service.OperatorService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/operators")
public class OperatorController {

    private final OperatorService operatorService;
    private final ContactService contactService;
    private final CurrentUserProvider currentUserProvider;

    public OperatorController(OperatorService operatorService, ContactService contactService,
                              CurrentUserProvider currentUserProvider) {
        this.operatorService = operatorService;
        this.contactService = contactService;
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping
    public ResponseEntity<Page<OperatorResponse>> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long regionId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(operatorService.list(search, regionId, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OperatorResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(operatorService.getById(id));
    }

    @PostMapping
    public ResponseEntity<OperatorResponse> create(@Valid @RequestBody CreateOperatorRequest request) {
        OperatorResponse response = operatorService.create(request, currentUserProvider.getCurrentUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<OperatorResponse> update(@PathVariable Long id,
                                                   @Valid @RequestBody UpdateOperatorRequest request) {
        return ResponseEntity.ok(operatorService.update(id, request, currentUserProvider.getCurrentUserId()));
    }

    @PostMapping("/{operatorId}/contacts")
    public ResponseEntity<ContactResponse> addContact(@PathVariable Long operatorId,
                                                      @Valid @RequestBody CreateContactRequest request) {
        ContactResponse response = contactService.addContact(operatorId, request,
                currentUserProvider.getCurrentUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{operatorId}/contacts/{contactId}")
    public ResponseEntity<ContactResponse> updateContact(@PathVariable Long operatorId,
                                                         @PathVariable Long contactId,
                                                         @Valid @RequestBody UpdateContactRequest request) {
        return ResponseEntity.ok(contactService.updateContact(operatorId, contactId, request,
                currentUserProvider.getCurrentUserId()));
    }

    @DeleteMapping("/{operatorId}/contacts/{contactId}")
    public ResponseEntity<Void> deleteContact(@PathVariable Long operatorId,
                                              @PathVariable Long contactId) {
        contactService.deleteContact(operatorId, contactId, currentUserProvider.getCurrentUserId());
        return ResponseEntity.noContent().build();
    }
}
