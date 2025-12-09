package com.example.warehousemanagement.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity representing a Notification sent to a recipient.
 */
@Entity
@Table(name = "notifications")
public class Notification {
    public enum Channel {
        EMAIL, SMS, IN_APP
    }

    public enum Status {
        PENDING, SENT, FAILED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    private Employee recipient;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Channel channel;

    @Column(nullable = false)
    private String type;

    @Column(length = 2000, nullable = false)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Column(name = "sent_timestamp")
    private LocalDateTime sentTimestamp;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Employee getRecipient() { return recipient; }
    public void setRecipient(Employee recipient) { this.recipient = recipient; }

    public Channel getChannel() { return channel; }
    public void setChannel(Channel channel) { this.channel = channel; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public LocalDateTime getSentTimestamp() { return sentTimestamp; }
    public void setSentTimestamp(LocalDateTime sentTimestamp) { this.sentTimestamp = sentTimestamp; }
}
