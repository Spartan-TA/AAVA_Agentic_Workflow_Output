package com.wms.ems.notification.service;

import com.wms.ems.notification.entity.Notification;
import com.wms.ems.notification.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/**
 * Service class for Notification management.
 * Handles multi-channel delivery.
 */
@Service
@Transactional
public class NotificationService {
    private final NotificationRepository notificationRepository;

    @Autowired
    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    /**
     * Send a notification (stub for multi-channel delivery).
     * @param notification the notification to send
     * @return the saved Notification
     */
    public Notification sendNotification(Notification notification) {
        // Implement multi-channel delivery logic here (email, SMS, push, etc.)
        notification.setStatus("SENT");
        return notificationRepository.save(notification);
    }

    /**
     * Get all notifications for a recipient.
     * @param recipientId the recipient's employee ID
     * @return List of Notification
     */
    public List<Notification> getNotificationsForRecipient(Long recipientId) {
        return notificationRepository.findByRecipientId(recipientId);
    }
}
