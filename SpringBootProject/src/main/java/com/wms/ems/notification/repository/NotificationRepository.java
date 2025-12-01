package com.wms.ems.notification.repository;

import com.wms.ems.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repository interface for Notification entity.
 * Provides CRUD operations and custom queries for notifications.
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    /**
     * Find all notifications for a specific employee.
     * @param recipientId the recipient's employee ID
     * @return List of Notification
     */
    List<Notification> findByRecipientId(Long recipientId);

    /**
     * Find all notifications by status.
     * @param status the status of the notification (e.g., SENT, READ)
     * @return List of Notification
     */
    List<Notification> findByStatus(String status);
}
