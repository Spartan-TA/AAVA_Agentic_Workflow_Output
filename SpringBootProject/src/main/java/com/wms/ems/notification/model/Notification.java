package com.wms.ems.notification.model;

import com.wms.ems.common.BaseEntity;
import com.wms.ems.employee.model.Employee;
import com.wms.ems.common.NotificationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity representing a notification sent to an employee.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "notifications")
public class Notification extends BaseEntity {

    /**
     * The recipient of the notification.
     */
    @ManyToOne
    @JoinColumn(name = "recipient_id")
    private Employee recipient;

    /**
     * Channel through which the notification was sent.
     */
    private String channel;

    /**
     * Message content of the notification.
     */
    @Column(length = 1000)
    private String message;

    /**
     * Date and time the notification was sent.
     */
    private LocalDateTime sentAt;

    /**
     * Status of the notification.
     */
    @Enumerated(EnumType.STRING)
    private NotificationStatus status;
}