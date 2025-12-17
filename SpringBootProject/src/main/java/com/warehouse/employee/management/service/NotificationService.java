package com.warehouse.employee.management.service;

import com.warehouse.employee.management.entity.Notification;
import com.warehouse.employee.management.repository.NotificationRepository;
import com.warehouse.employee.management.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/**
 * Service class for managing Notification entities.
 */
@Service
public class NotificationService {
    private final NotificationRepository notificationRepository;

    @Autowired
    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    /**
     * Get all notifications.
     * @return List of notifications
     */
    public List<Notification> getAllNotifications() {
        return notificationRepository.findAll();
    }

    /**
     * Get notification by ID.
     * @param id Notification ID
     * @return Notification entity
     */
    public Notification getNotificationById(Long id) {
        return notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + id));
    }

    /**
     * Create a new notification.
     * @param notification Notification entity
     * @return Created notification
     */
    @Transactional
    public Notification createNotification(Notification notification) {
        return notificationRepository.save(notification);
    }

    /**
     * Update an existing notification.
     * @param id Notification ID
     * @param updatedNotification Updated notification entity
     * @return Updated notification
     */
    @Transactional
    public Notification updateNotification(Long id, Notification updatedNotification) {
        Notification existingNotification = getNotificationById(id);
        existingNotification.setMessage(updatedNotification.getMessage());
        existingNotification.setRecipient(updatedNotification.getRecipient());
        existingNotification.setType(updatedNotification.getType());
        existingNotification.setStatus(updatedNotification.getStatus());
        // Add other fields as needed
        return notificationRepository.save(existingNotification);
    }

    /**
     * Delete a notification by ID.
     * @param id Notification ID
     */
    @Transactional
    public void deleteNotification(Long id) {
        Notification notification = getNotificationById(id);
        notificationRepository.delete(notification);
    }

    /**
     * Mark notification as read.
     * @param id Notification ID
     * @return Updated notification
     */
    @Transactional
    public Notification markAsRead(Long id) {
        Notification notification = getNotificationById(id);
        notification.setStatus("READ");
        return notificationRepository.save(notification);
    }
}
