package com.wms.notification.dtos;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for Announcements.
 */
@Data
public class AnnouncementDto {
    private Long id;
    private String title;
    private String message;
    private LocalDateTime publishedAt;
    private String channel;
}
