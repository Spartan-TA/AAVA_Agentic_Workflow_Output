package com.wms.notification.dtos;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for Notification.
 */
@Data
public class NotificationDto {
    private Long id;
    private Long recipientId;
    private String channel;
    private String template;
    private String payload;
    private boolean delivered;
    private LocalDateTime deliveredAt;
}
