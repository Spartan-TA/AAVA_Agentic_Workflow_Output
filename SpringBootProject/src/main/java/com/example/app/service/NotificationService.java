package com.example.app.service;

import com.example.app.dto.NotificationDto;
import java.util.List;

public interface NotificationService {
    List<NotificationDto> getUserNotifications(String username);
    void markAsRead(Long notificationId);
    void sendNotification(String username, String message);
}
