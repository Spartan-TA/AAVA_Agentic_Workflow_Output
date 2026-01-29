package com.warehouse.employee.management.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/notifications")
public class NotificationController {
    private final List<String> notifications = new ArrayList<>();
    private final List<String> announcements = new ArrayList<>();

    @PreAuthorize("hasAuthority('NOTIFICATION_SEND')")
    @PostMapping
    public String sendNotification(@RequestParam String to, @RequestParam String message) {
        String notif = "To: " + to + ", Message: " + message;
        notifications.add(notif);
        return notif;
    }

    @PreAuthorize("hasAuthority('ANNOUNCEMENT_SEND')")
    @PostMapping("/announcements")
    public String sendAnnouncement(@RequestParam String message) {
        announcements.add(message);
        return message;
    }

    @PreAuthorize("hasAuthority('NOTIFICATION_READ')")
    @GetMapping
    public List<String> getNotifications() {
        return Collections.unmodifiableList(notifications);
    }

    @PreAuthorize("hasAuthority('ANNOUNCEMENT_READ')")
    @GetMapping("/announcements")
    public List<String> getAnnouncements() {
        return Collections.unmodifiableList(announcements);
    }
}
