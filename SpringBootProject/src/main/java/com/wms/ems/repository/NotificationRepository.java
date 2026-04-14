package com.wms.ems.repository;

import com.wms.ems.entity.Notification;
import com.wms.ems.entity.User;
import com.wms.ems.enums.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

/**
 * Repository interface for Notification entity operations.
 * Provides CRUD and custom query methods for notification management.
 */
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    /**
     * Find notifications by user and status.
     * @param user the user
     * @param status the notification status
     * @return List of Notifications
     */
    List<Notification> findByUserAndStatus(User user, NotificationStatus status);

    /**
     * Find unread notifications for a user.
     * @param user the user
     * @return List of unread Notifications
     */
    @Query("SELECT n FROM Notification n WHERE n.user = :user AND n.status = 'UNREAD'")
    List<Notification> findUnreadByUser(@Param("user") User user);
}
