package com.wms.notification.model;

import com.wms.employee.model.Employee;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Entity representing a notification sent to an employee.
 */
@Data
@Entity
@Table(name = "notifications")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Recipient of the notification (Employee) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    private Employee recipient;

    /** Notification channel (e.g., EMAIL, SMS, APP) */
    @Column(nullable = false)
    private String channel;

    /** Notification template name */
    @Column(nullable = false)
    private String template;

    /** Notification payload (JSON or text) */
    @Lob
    @Column(nullable = false)
    private String payload;

    /** Delivery status */
    @Column(nullable = false)
    private boolean delivered = false;

    /** Timestamp when delivered */
    private LocalDateTime deliveredAt;
}
