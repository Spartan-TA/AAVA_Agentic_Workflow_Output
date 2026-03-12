package SpringBootTestSuite;

import com.example.warehouse.notification.Notification;
import com.example.warehouse.notification.Announcement;
import com.example.warehouse.notification.NotificationService;
import com.example.warehouse.notification.NotificationRepository;
import com.example.warehouse.exception.ResourceNotFoundException;
import com.example.warehouse.exception.ValidationException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
public class NotificationServiceTest {
    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    public void sendNotification_ValidInput_ReturnsNotification() {
        Notification notification = new Notification();
        notification.setRecipientId(1L);
        notification.setMessage("Shift changed");
        notification.setSentAt(LocalDateTime.now());
        when(notificationRepository.save(any())).thenReturn(notification);
        Notification result = notificationService.sendNotification(notification);
        assertNotNull(result);
        assertEquals("Shift changed", result.getMessage());
    }

    @Test
    public void sendNotification_NullInput_ThrowsValidationException() {
        assertThrows(ValidationException.class, () -> notificationService.sendNotification(null));
    }

    @Test
    public void getNotificationById_ValidId_ReturnsNotification() {
        Notification notification = new Notification();
        notification.setId(1L);
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));
        Notification result = notificationService.getNotificationById(1L);
        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    public void getNotificationById_InvalidId_ThrowsResourceNotFoundException() {
        when(notificationRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> notificationService.getNotificationById(99L));
    }

    @Test
    public void getAllNotifications_ReturnsList() {
        Notification notification = new Notification();
        notification.setId(1L);
        when(notificationRepository.findAll()).thenReturn(Collections.singletonList(notification));
        List<Notification> result = notificationService.getAllNotifications();
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    public void getAllNotifications_Empty_ReturnsEmptyList() {
        when(notificationRepository.findAll()).thenReturn(Collections.emptyList());
        List<Notification> result = notificationService.getAllNotifications();
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void sendAnnouncement_ValidInput_ReturnsAnnouncement() {
        Announcement announcement = new Announcement();
        announcement.setTitle("Warehouse Closed");
        announcement.setContent("Due to maintenance");
        when(notificationRepository.saveAnnouncement(any())).thenReturn(announcement);
        Announcement result = notificationService.sendAnnouncement(announcement);
        assertNotNull(result);
        assertEquals("Warehouse Closed", result.getTitle());
    }

    @Test
    public void sendAnnouncement_NullInput_ThrowsValidationException() {
        assertThrows(ValidationException.class, () -> notificationService.sendAnnouncement(null));
    }
}
