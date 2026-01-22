package com.warehouse.ems.notification;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * REST controller for Notification endpoints.
 */
@RestController
@RequestMapping("/notifications")
@Validated
public class NotificationController {
    private final NotificationService notificationService;

    @Autowired
    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * Send a notification.
     */
    @PostMapping
    public ResponseEntity<Notification> sendNotification(@Valid @RequestBody Notification notification) {
        try {
            Notification sent = notificationService.sendNotification(notification);
            return new ResponseEntity<>(sent, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Get all notifications.
     */
    @GetMapping
    public ResponseEntity<List<Notification>> getAllNotifications() {
        return ResponseEntity.ok(notificationService.getAllNotifications());
    }

    /**
     * Mark notification as read.
     */
    @PutMapping("/{id}/read")
    public ResponseEntity<Notification> markAsRead(@PathVariable Long id) {
        try {
            // In production, fetch Notification by id
            Notification notification = new Notification();
            notification.setId(id);
            Notification read = notificationService.markAsRead(notification);
            return ResponseEntity.ok(read);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }
}
