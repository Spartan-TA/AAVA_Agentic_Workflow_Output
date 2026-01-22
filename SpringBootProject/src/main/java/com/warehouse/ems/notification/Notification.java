package com.warehouse.ems.notification;

import com.warehouse.ems.employee.Employee;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * Entity representing a notification sent to an employee.
 */
@Entity
@Table(name = "notifications")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recipient_id", nullable = false)
    @NotNull(message = "Recipient is required.")
    private Employee recipient;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Channel is required.")
    @Column(nullable = false)
    private Channel channel;

    @NotNull(message = "Message is required.")
    @Size(min = 1, max = 512)
    @Column(nullable = false, length = 512)
    private String message;

    @NotNull(message = "SentAt is required.")
    @Column(nullable = false)
    private LocalDateTime sentAt;

    @Column(nullable = false)
    private boolean delivered;

    private LocalDateTime readAt;

    public enum Channel {
        IN_APP, EMAIL, SMS
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Employee getRecipient() { return recipient; }
    public void setRecipient(Employee recipient) { this.recipient = recipient; }
    public Channel getChannel() { return channel; }
    public void setChannel(Channel channel) { this.channel = channel; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }
    public boolean isDelivered() { return delivered; }
    public void setDelivered(boolean delivered) { this.delivered = delivered; }
    public LocalDateTime getReadAt() { return readAt; }
    public void setReadAt(LocalDateTime readAt) { this.readAt = readAt; }
}
