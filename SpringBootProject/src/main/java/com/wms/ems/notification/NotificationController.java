package com.wms.ems.notification;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','HR','SUPERVISOR')")
    public ResponseEntity<Notification> sendNotification(@RequestParam Long userId, @RequestParam String channel, @RequestParam String message) {
        return ResponseEntity.ok(notificationService.sendNotification(userId, channel, message));
    }

    // Announcements endpoints can be added here
}
