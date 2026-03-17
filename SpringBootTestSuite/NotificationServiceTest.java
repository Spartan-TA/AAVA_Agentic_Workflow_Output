package com.warehouse.ems.service;

import com.warehouse.ems.dto.NotificationRequestDto;
import com.warehouse.ems.entity.Notification;
import com.warehouse.ems.exception.EntityNotFoundException;
import com.warehouse.ems.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for NotificationService.
 * Covers normal operation, null/invalid input, and exception scenarios.
 */
@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;
    @InjectMocks
    private NotificationService notificationService;

    private Notification notification;
    private NotificationRequestDto notificationRequestDto;

    @BeforeEach
    void setUp() {
        notification = new Notification();
        notification.setId(1L);
        notification.setTitle("Test");
        notification.setMessage("Test message");
        notification.setRecipient("user1");
        notification.setSentAt(LocalDateTime.now());
        notification.setStatus("SENT");

        notificationRequestDto = new NotificationRequestDto();
        notificationRequestDto.setTitle("Test");
        notificationRequestDto.setMessage("Test message");
        notificationRequestDto.setRecipient("user1");
    }

    /**
     * Test createNotification with valid input returns Notification.
     */
    @Test
    void testCreateNotification_ValidInput_ReturnsNotification() {
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);
        Notification result = notificationService.createNotification(notificationRequestDto);
        assertNotNull(result);
        assertEquals("Test", result.getTitle());
    }

    /**
     * Test createNotification with null DTO throws exception.
     */
    @Test
    void testCreateNotification_NullDto_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                notificationService.createNotification(null));
    }

    /**
     * Test getNotificationById with valid ID returns Notification.
     */
    @Test
    void testGetNotificationById_ValidId_ReturnsNotification() {
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));
        Notification result = notificationService.getNotificationById(1L);
        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    /**
     * Test getNotificationById with non-existent ID throws EntityNotFoundException.
     */
    @Test
    void testGetNotificationById_NonExistentId_ThrowsEntityNotFoundException() {
        when(notificationRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () ->
                notificationService.getNotificationById(99L));
    }

    /**
     * Test getAllNotifications returns list.
     */
    @Test
    void testGetAllNotifications_ReturnsList() {
        when(notificationRepository.findAll()).thenReturn(List.of(notification));
        List<Notification> result = notificationService.getAllNotifications();
        assertEquals(1, result.size());
    }

    /**
     * Test updateNotification with valid input returns Notification.
     */
    @Test
    void testUpdateNotification_ValidInput_ReturnsNotification() {
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);
        Notification result = notificationService.updateNotification(1L, notificationRequestDto);
        assertNotNull(result);
    }
}
