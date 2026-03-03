package com.wms.ems.notification.service;

import com.wms.ems.notification.repository.NotificationRepository;
import com.wms.ems.employee.repository.EmployeeRepository;
import com.wms.ems.notification.entity.Notification;
import com.wms.ems.notification.enums.NotificationStatus;
import com.wms.ems.employee.entity.Employee;
import com.wms.ems.common.exception.ResourceNotFoundException;
import com.wms.ems.common.exception.ValidationException;
import com.wms.ems.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for managing notifications.
 */
@Service
@Transactional
@Slf4j
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    /**
     * Sends a notification to a recipient.
     * @param recipientId Employee ID
     * @param channel Notification channel
     * @param message Message content
     * @return Notification
     */
    public Notification sendNotification(Long recipientId, String channel, String message) {
        if (recipientId == null || channel == null || channel.isEmpty() || message == null || message.isEmpty()) {
            log.error("Validation failed: All fields are required");
            throw new ValidationException("All fields are required");
        }
        Employee recipient = employeeRepository.findById(recipientId)
                .orElseThrow(() -> new ResourceNotFoundException("Recipient not found"));
        Notification notification = new Notification();
        notification.setRecipient(recipient);
        notification.setChannel(channel);
        notification.setMessage(message);
        notification.setStatus(NotificationStatus.PENDING);
        notification.setCreatedAt(java.time.LocalDateTime.now());
        try {
            Notification saved = notificationRepository.save(notification);
            // Simulate sending (integration with actual channel omitted)
            log.info("Notification sent to employee {} via {}", recipientId, channel);
            return saved;
        } catch (Exception e) {
            log.error("Failed to send notification", e);
            throw new BusinessException("Failed to send notification");
        }
    }

    /**
     * Gets notifications for an employee by status.
     * @param employeeId Employee ID
     * @param status NotificationStatus
     * @return List of Notification
     */
    @Transactional(readOnly = true)
    public List<Notification> getEmployeeNotifications(Long employeeId, NotificationStatus status) {
        try {
            return notificationRepository.findByRecipientIdAndStatus(employeeId, status);
        } catch (Exception e) {
            log.error("Failed to fetch employee notifications", e);
            throw new BusinessException("Failed to fetch employee notifications");
        }
    }

    /**
     * Marks a notification as delivered.
     * @param notificationId Notification ID
     */
    public void markAsDelivered(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
        notification.setStatus(NotificationStatus.DELIVERED);
        try {
            notificationRepository.save(notification);
            log.info("Notification {} marked as delivered", notificationId);
        } catch (Exception e) {
            log.error("Failed to mark notification as delivered", e);
            throw new BusinessException("Failed to mark notification as delivered");
        }
    }

    /**
     * Marks a notification as failed with a reason.
     * @param notificationId Notification ID
     * @param reason Failure reason
     */
    public void markAsFailed(Long notificationId, String reason) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
        notification.setStatus(NotificationStatus.FAILED);
        notification.setFailureReason(reason);
        try {
            notificationRepository.save(notification);
            log.info("Notification {} marked as failed: {}", notificationId, reason);
        } catch (Exception e) {
            log.error("Failed to mark notification as failed", e);
            throw new BusinessException("Failed to mark notification as failed");
        }
    }

    /**
     * Gets all pending notifications.
     * @return List of Notification
     */
    @Transactional(readOnly = true)
    public List<Notification> getPendingNotifications() {
        try {
            return notificationRepository.findByStatus(NotificationStatus.PENDING);
        } catch (Exception e) {
            log.error("Failed to fetch pending notifications", e);
            throw new BusinessException("Failed to fetch pending notifications");
        }
    }
}
