package com.warehouse.notifications.controller;

import com.warehouse.notifications.entity.Notification;
import com.warehouse.notifications.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    @Autowired
    private NotificationService notificationService;

    @GetMapping
    public ResponseEntity<List<Notification>> getAllNotifications() {
        return ResponseEntity.ok(notificationService.getAllNotifications());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Notification> getNotificationById(@PathVariable Long id) {
        return notificationService.getNotificationById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Notification> sendNotification(@RequestParam String channel,
                                                        @RequestParam String template,
                                                        @RequestParam String recipient,
                                                        @RequestParam String content) {
        Notification notification = notificationService.sendNotification(channel, template, recipient, content);
        return ResponseEntity.ok(notification);
    }
}
