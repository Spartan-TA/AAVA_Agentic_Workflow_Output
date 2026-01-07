package com.example.usermanagement.repository;

import com.example.usermanagement.entity.Notification;
import com.example.usermanagement.entity.User;
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
     * Find all notifications for a user, ordered by creation date descending.
     * @param user User entity
     * @return List of notifications
     */
    List<Notification> findByUserOrderByCreatedAtDesc(User user);
}
