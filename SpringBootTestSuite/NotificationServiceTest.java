package SpringBootTestSuite;

import org.junit.jupiter.api.*;
import org.mockito.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import java.time.*;

class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private UserPreferencesRepository userPreferencesRepository;
    @InjectMocks
    private NotificationService notificationService;

    private AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        mocks.close();
    }

    @Test
    void testSendNotification_Valid() {
        when(userPreferencesRepository.isOptedOut(1L)).thenReturn(false);
        Notification notification = new Notification(1L, "EMAIL", "Hello");
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);
        Notification result = notificationService.sendNotification(1L, "EMAIL", "Hello");
        assertNotNull(result);
        assertEquals("EMAIL", result.getChannel());
    }

    @Test
    void testSendNotification_InvalidChannel() {
        Exception ex = assertThrows(IllegalArgumentException.class, () ->
            notificationService.sendNotification(1L, "FAX", "Hello"));
        assertEquals("Invalid notification channel", ex.getMessage());
    }

    @Test
    void testSendNotification_NullContent() {
        Exception ex = assertThrows(IllegalArgumentException.class, () ->
            notificationService.sendNotification(1L, "EMAIL", null));
        assertEquals("Content cannot be null", ex.getMessage());
    }

    @Test
    void testSendNotification_UserOptedOut() {
        when(userPreferencesRepository.isOptedOut(1L)).thenReturn(true);
        Exception ex = assertThrows(IllegalStateException.class, () ->
            notificationService.sendNotification(1L, "EMAIL", "Hello"));
        assertEquals("User has opted out", ex.getMessage());
    }

    @Test
    void testCheckQuietHours_Active() {
        when(userPreferencesRepository.getQuietHours(1L)).thenReturn(new QuietHours(LocalTime.of(22,0), LocalTime.of(6,0)));
        boolean result = notificationService.checkQuietHours(1L, LocalTime.of(23,0));
        assertTrue(result);
    }

    @Test
    void testTrackDelivery_NonExistentNotification() {
        when(notificationRepository.findById(99L)).thenReturn(Optional.empty());
        Exception ex = assertThrows(NoSuchElementException.class, () ->
            notificationService.trackDelivery(99L));
        assertEquals("Notification not found", ex.getMessage());
    }
}