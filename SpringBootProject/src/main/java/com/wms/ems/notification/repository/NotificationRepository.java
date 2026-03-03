package com.wms.ems.notification.repository;

import com.wms.ems.notification.entity.Notification;
import com.wms.ems.notification.entity.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for Notification entity operations.
 * Provides CRUD operations and custom queries for notification management.
 */
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * Finds notifications for a recipient with a specific status.
     * @param recipientId the recipient ID
     * @param status the notification status
     * @return a list of notifications
     */
    List<Notification> findByRecipientIdAndStatus(Long recipientId, NotificationStatus status);

    /**
     * Finds notifications sent between two timestamps.
     * @param start the start timestamp
     * @param end the end timestamp
     * @return a list of notifications
     */
    List<Notification> findBySentAtBetween(LocalDateTime start, LocalDateTime end);
}
