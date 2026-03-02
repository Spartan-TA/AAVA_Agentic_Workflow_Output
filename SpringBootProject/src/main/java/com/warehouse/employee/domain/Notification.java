package com.warehouse.employee.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.time.LocalDateTime;

/**
 * Notification entity for notifications.
 */
@Entity
@Table(name = "notifications", indexes = {
        @Index(name = "idx_notification_recipient", columnList = "recipient_id"),
        @Index(name = "idx_notification_status", columnList = "status"),
        @Index(name = "idx_notification_channel", columnList = "channel")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The recipient of the notification.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    @JsonIgnore
    private Employee recipient;

    @NotBlank
    @Size(max = 100)
    private String notificationType;

    @NotNull
    @Enumerated(EnumType.STRING)
    private Channel channel;

    @NotBlank
    @Size(max = 150)
    private String subject;

    @NotBlank
    @Size(max = 2000)
    private String message;

    private LocalDateTime sentAt;

    private LocalDateTime readAt;

    @NotNull
    @Enumerated(EnumType.STRING)
    private Status status;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    /**
     * Notification channel.
     */
    public enum Channel {
        EMAIL, SMS, IN_APP
    }

    /**
     * Notification status.
     */
    public enum Status {
        PENDING, SENT, FAILED, READ
    }
}
