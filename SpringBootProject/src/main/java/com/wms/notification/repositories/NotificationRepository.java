package com.wms.notification.repositories;

import com.wms.notification.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for Notification entities.
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    // Additional query methods can be defined here if needed
}
