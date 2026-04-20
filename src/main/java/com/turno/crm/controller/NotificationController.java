package com.turno.crm.controller;

import com.turno.crm.model.dto.NotificationResponse;
import com.turno.crm.security.CurrentUserProvider;
import com.turno.crm.service.NotificationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final CurrentUserProvider currentUserProvider;

    public NotificationController(NotificationService notificationService,
                                   CurrentUserProvider currentUserProvider) {
        this.notificationService = notificationService;
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping
    public ResponseEntity<Page<NotificationResponse>> getNotifications(
            @RequestParam(defaultValue = "false") boolean cleared,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<NotificationResponse> notifications = notificationService.getNotifications(
                currentUserProvider.getCurrentUserId(), cleared, pageable);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> getUnreadCount() {
        long count = notificationService.getUnreadCount(currentUserProvider.getCurrentUserId());
        return ResponseEntity.ok(Map.of("unreadCount", count));
    }

    @PostMapping("/{id}/clear")
    public ResponseEntity<Void> clearNotification(@PathVariable Long id) {
        notificationService.clearNotification(id, currentUserProvider.getCurrentUserId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/clear-all")
    public ResponseEntity<Void> clearAll() {
        notificationService.clearAll(currentUserProvider.getCurrentUserId());
        return ResponseEntity.noContent().build();
    }
}
