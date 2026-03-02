package com.warehouse.employee.repository;

import com.warehouse.employee.domain.Employee;
import com.warehouse.employee.domain.Notification;
import com.warehouse.employee.domain.Notification.Status;
import com.warehouse.employee.domain.Notification.Channel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Repository interface for Notification entity.
 */
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    /**
     * Find notifications for a recipient by status.
     */
    List<Notification> findByRecipientAndStatus(Employee recipient, Status status);

    /**
     * Find unread notifications for a recipient.
     */
    @Query("SELECT n FROM Notification n WHERE n.recipient = :recipient AND n.status <> 'READ'")
    List<Notification> findUnreadByRecipient(@Param("recipient") Employee recipient);

    /**
     * Find notifications by channel.
     */
    List<Notification> findByChannel(Channel channel);
}
