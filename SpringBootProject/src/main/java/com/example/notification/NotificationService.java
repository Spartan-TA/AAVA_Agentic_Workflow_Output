package com.example.notification;

import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class NotificationService {
    private final List<Notification> notifications = new ArrayList<>();

    public List<Notification> getAllNotifications() {
        return notifications;
    }

    public Notification sendNotification(Notification notification) {
        notification.setSentAt(java.time.LocalDateTime.now());
        notifications.add(notification);
        return notification;
    }

    public void markAsRead(Long id) {
        notifications.stream()
            .filter(n -> n.getId().equals(id))
            .findFirst()
            .ifPresent(n -> n.setRead(true));
    }
}