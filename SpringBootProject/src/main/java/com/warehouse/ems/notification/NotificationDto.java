package com.warehouse.ems.notification;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * DTO for Notification with validation annotations.
 */
public class NotificationDto {
    private Long id;

    @NotNull(message = "Recipient ID is required.")
    private Long recipientId;

    @NotNull(message = "Channel is required.")
    private String channel;

    @NotNull(message = "Message is required.")
    @Size(min = 1, max = 512, message = "Message must be between 1 and 512 characters.")
    private String message;

    private LocalDateTime sentAt;
    private boolean delivered;
    private LocalDateTime readAt;

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getRecipientId() { return recipientId; }
    public void setRecipientId(Long recipientId) { this.recipientId = recipientId; }
    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }
    public boolean isDelivered() { return delivered; }
    public void setDelivered(boolean delivered) { this.delivered = delivered; }
    public LocalDateTime getReadAt() { return readAt; }
    public void setReadAt(LocalDateTime readAt) { this.readAt = readAt; }
}
