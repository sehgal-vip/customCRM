package com.turno.crm.controller;

import com.turno.crm.model.dto.CreateUserRequest;
import com.turno.crm.model.dto.UpdateUserRequest;
import com.turno.crm.model.dto.UserResponse;
import com.turno.crm.model.enums.UserRole;
import com.turno.crm.model.enums.UserStatus;
import com.turno.crm.security.CurrentUserProvider;
import com.turno.crm.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;
    private final CurrentUserProvider currentUserProvider;

    public UserController(UserService userService, CurrentUserProvider currentUserProvider) {
        this.userService = userService;
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<List<UserResponse>> list(
            @RequestParam(required = false) UserRole role,
            @RequestParam(required = false) UserStatus status) {
        return ResponseEntity.ok(userService.list(role, status));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getById(@PathVariable Long id) {
        // Manager or self
        if (!currentUserProvider.isManager() && !currentUserProvider.getCurrentUserId().equals(id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(userService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<UserResponse> create(@Valid @RequestBody CreateUserRequest request) {
        UserResponse response = userService.create(request, currentUserProvider.getCurrentUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<UserResponse> update(@PathVariable Long id,
                                               @Valid @RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(userService.update(id, request, currentUserProvider.getCurrentUserId()));
    }

    @PostMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<UserResponse> deactivate(@PathVariable Long id) {
        return ResponseEntity.ok(userService.deactivate(id, currentUserProvider.getCurrentUserId()));
    }

    @PostMapping("/{id}/reactivate")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<UserResponse> reactivate(@PathVariable Long id) {
        return ResponseEntity.ok(userService.reactivate(id, currentUserProvider.getCurrentUserId()));
    }
}
