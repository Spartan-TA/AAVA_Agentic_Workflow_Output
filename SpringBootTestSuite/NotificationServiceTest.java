import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class NotificationServiceTest {
    @Mock
    private NotificationRepository notificationRepository;
    @InjectMocks
    private NotificationService notificationService;

    private Notification notification;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        notification = new Notification(1L, 1L, "You have a new like", false);
    }

    @Test
    public void testSendNotification_Success() {
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);
        Notification result = notificationService.sendNotification(notification);
        assertEquals(notification, result);
    }

    @Test
    public void testGetNotificationsForUser() {
        List<Notification> notifications = Arrays.asList(notification, new Notification(2L, 1L, "Another", false));
        when(notificationRepository.findByUserId(1L)).thenReturn(notifications);
        List<Notification> result = notificationService.getNotificationsForUser(1L);
        assertEquals(2, result.size());
    }

    @Test
    public void testMarkAsRead_Success() {
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);
        assertDoesNotThrow(() -> notificationService.markAsRead(1L));
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    public void testMarkAsRead_NotFound() {
        when(notificationRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> notificationService.markAsRead(1L));
    }

    @Test
    public void testSendNotification_Null() {
        assertThrows(IllegalArgumentException.class, () -> notificationService.sendNotification(null));
    }
}
