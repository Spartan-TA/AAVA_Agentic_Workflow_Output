package com.wms.notification.controllers;

import com.wms.notification.dtos.NotificationDto;
import com.wms.notification.dtos.AnnouncementDto;
import com.wms.notification.model.Notification;
import com.wms.notification.services.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for notifications and announcements.
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;

    /**
     * Sends a notification to an employee.
     * @param dto NotificationDto
     * @return NotificationDto
     */
    @PostMapping
    public ResponseEntity<NotificationDto> sendNotification(@RequestBody NotificationDto dto) {
        // TODO: Map DTO to entity and handle recipient lookup
        Notification notification = new Notification();
        notification.setChannel(dto.getChannel());
        notification.setTemplate(dto.getTemplate());
        notification.setPayload(dto.getPayload());
        // Set recipient, delivered, deliveredAt as needed
        Notification saved = notificationService.sendNotification(notification);
        NotificationDto response = new NotificationDto();
        response.setId(saved.getId());
        response.setChannel(saved.getChannel());
        response.setTemplate(saved.getTemplate());
        response.setPayload(saved.getPayload());
        response.setDelivered(saved.isDelivered());
        response.setDeliveredAt(saved.getDeliveredAt());
        return ResponseEntity.ok(response);
    }

    /**
     * Returns a list of announcements.
     * @return List of AnnouncementDto
     */
    @GetMapping("/announcements")
    public ResponseEntity<List<AnnouncementDto>> getAnnouncements() {
        // TODO: Implement actual announcement retrieval
        return ResponseEntity.ok(List.of());
    }
}
