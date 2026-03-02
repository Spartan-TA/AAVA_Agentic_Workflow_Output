package SpringBootTestSuite;

import org.junit.jupiter.api.*;
import org.mockito.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;

class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private UserPreferencesRepository userPreferencesRepository;
    @InjectMocks
    private NotificationServiceImpl notificationService;

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
    @DisplayName("sendNotification - valid - notification sent")
    void testSendNotification_Valid_NotificationSent() {
        Notification notif = new Notification(null, 1L, "Shift changed", "IN_APP");
        when(notificationRepository.save(any())).thenAnswer(i -> {
            Notification n = i.getArgument(0);
            n.setId(1L);
            return n;
        });
        Notification result = notificationService.sendNotification(notif);
        assertNotNull(result.getId());
        assertEquals("Shift changed", result.getMessage());
    }

    @Test
    @DisplayName("sendBulkNotification - valid - notifications sent")
    void testSendBulkNotification_Valid_NotificationsSent() {
        List<Notification> notifs = Arrays.asList(
            new Notification(null, 1L, "Shift changed", "IN_APP"),
            new Notification(null, 2L, "Shift changed", "IN_APP")
        );
        when(notificationRepository.saveAll(any())).thenReturn(notifs);
        List<Notification> result = notificationService.sendBulkNotification(notifs);
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("getUserPreferences - found - returns preferences")
    void testGetUserPreferences_Found_ReturnsPreferences() {
        UserPreferences prefs = new UserPreferences(1L, true, false);
        when(userPreferencesRepository.findByUserId(1L)).thenReturn(Optional.of(prefs));
        UserPreferences result = notificationService.getUserPreferences(1L);
        assertTrue(result.isInAppEnabled());
    }

    @Test
    @DisplayName("getUserPreferences - not found - returns default")
    void testGetUserPreferences_NotFound_ReturnsDefault() {
        when(userPreferencesRepository.findByUserId(2L)).thenReturn(Optional.empty());
        UserPreferences result = notificationService.getUserPreferences(2L);
        assertNotNull(result);
    }

    @Test
    @DisplayName("updatePreferences - valid - preferences updated")
    void testUpdatePreferences_Valid_PreferencesUpdated() {
        UserPreferences prefs = new UserPreferences(1L, true, false);
        when(userPreferencesRepository.save(any())).thenReturn(prefs);
        UserPreferences result = notificationService.updatePreferences(prefs);
        assertTrue(result.isInAppEnabled());
    }

    @Test
    @DisplayName("getNotificationHistory - returns list")
    void testGetNotificationHistory_ReturnsList() {
        List<Notification> notifs = Arrays.asList(new Notification(1L, 1L, "Shift changed", "IN_APP"));
        when(notificationRepository.findByUserId(1L)).thenReturn(notifs);
        List<Notification> result = notificationService.getNotificationHistory(1L);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("sendNotification - null message - throws exception")
    void testSendNotification_NullMessage_ThrowsException() {
        Notification notif = new Notification(null, 1L, null, "IN_APP");
        assertThrows(InvalidNotificationException.class, () -> notificationService.sendNotification(notif));
    }

    @Test
    @DisplayName("sendBulkNotification - empty list - returns empty list")
    void testSendBulkNotification_EmptyList_ReturnsEmptyList() {
        List<Notification> notifs = Collections.emptyList();
        List<Notification> result = notificationService.sendBulkNotification(notifs);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("getNotificationHistory - no notifications - returns empty list")
    void testGetNotificationHistory_NoNotifications_ReturnsEmptyList() {
        when(notificationRepository.findByUserId(2L)).thenReturn(Collections.emptyList());
        List<Notification> result = notificationService.getNotificationHistory(2L);
        assertTrue(result.isEmpty());
    }
}