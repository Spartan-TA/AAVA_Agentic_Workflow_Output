package SpringBootTestSuite;

import com.example.demo.entity.Notification;
import com.example.demo.repository.NotificationRepository;
import com.example.demo.service.NotificationServiceImpl;
import com.example.demo.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class NotificationServiceImplTest {
    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private Notification notification;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        notification = new Notification();
        notification.setId(1L);
        notification.setUserId(1L);
        notification.setMessage("Test notification");
    }

    @Test
    void testGetNotificationById_HappyPath() {
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));
        Notification found = notificationService.getNotificationById(1L);
        assertEquals("Test notification", found.getMessage());
    }

    @Test
    void testGetNotificationById_NotFound() {
        when(notificationRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> notificationService.getNotificationById(2L));
    }

    @Test
    void testGetNotificationsForUser_HappyPath() {
        when(notificationRepository.findByUserId(1L)).thenReturn(Arrays.asList(notification));
        List<Notification> notifications = notificationService.getNotificationsForUser(1L);
        assertEquals(1, notifications.size());
    }

    @Test
    void testGetNotificationsForUser_EmptyList() {
        when(notificationRepository.findByUserId(2L)).thenReturn(Collections.emptyList());
        List<Notification> notifications = notificationService.getNotificationsForUser(2L);
        assertTrue(notifications.isEmpty());
    }

    @Test
    void testCreateNotification_HappyPath() {
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);
        Notification created = notificationService.createNotification(notification);
        assertEquals("Test notification", created.getMessage());
    }

    @Test
    void testCreateNotification_NullInput() {
        assertThrows(IllegalArgumentException.class, () -> notificationService.createNotification(null));
    }

    @Test
    void testDeleteNotification_HappyPath() {
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));
        doNothing().when(notificationRepository).delete(notification);
        assertDoesNotThrow(() -> notificationService.deleteNotification(1L));
    }

    @Test
    void testDeleteNotification_NotFound() {
        when(notificationRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> notificationService.deleteNotification(1L));
    }

    @Test
    void testMarkAsRead_HappyPath() {
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));
        notification.setRead(false);
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);
        Notification updated = notificationService.markAsRead(1L);
        assertTrue(updated.isRead());
    }

    @Test
    void testMarkAsRead_NotFound() {
        when(notificationRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> notificationService.markAsRead(1L));
    }
}