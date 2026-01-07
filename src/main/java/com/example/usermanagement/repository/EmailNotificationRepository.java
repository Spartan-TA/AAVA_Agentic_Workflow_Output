package com.example.usermanagement.repository;

import com.example.usermanagement.entity.EmailNotification;
import com.example.usermanagement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for EmailNotification entity.
 * Provides CRUD operations and custom queries for email notifications.
 */
@Repository
public interface EmailNotificationRepository extends JpaRepository<EmailNotification, Long> {
    /**
     * Find all email notifications for a user, ordered by sent date descending.
     * @param user User entity
     * @return List of email notifications
     */
    List<EmailNotification> findByUserOrderBySentAtDesc(User user);
}
