package SpringBootTestSuite;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;

/**
 * NotificationServiceTest - Comprehensive unit tests for NotificationService covering sending, quiet hours, opt-in/out, boundaries, and edge cases.
 */
public class NotificationServiceTest {
    private NotificationService notificationService;

    @BeforeEach
    public void setUp() {
        notificationService = new NotificationService();
    }

    @Test
    public void testSendInAppNotification() {
        Notification notif = new Notification("Shift changed", "IN_APP");
        assertTrue(notificationService.sendInAppNotification(notif));
    }

    @Test
    public void testSendEmailNotification() {
        Notification notif = new Notification("Cert expiring", "EMAIL");
        assertTrue(notificationService.sendEmailNotification(notif, "user@example.com"));
    }

    @Test
    public void testSendSMSNotification() {
        Notification notif = new Notification("Leave approved", "SMS");
        assertTrue(notificationService.sendSMSNotification(notif, "+1234567890"));
    }

    @Test
    public void testCheckQuietHoursWithin() {
        assertTrue(notificationService.checkQuietHours("23:00"));
    }

    @Test
    public void testCheckQuietHoursOutside() {
        assertFalse(notificationService.checkQuietHours("09:00"));
    }

    @Test
    public void testOptInToChannel() {
        int empId = 100;
        assertTrue(notificationService.optInToChannel(empId, "EMAIL"));
    }

    @Test
    public void testOptOutOfChannel() {
        int empId = 101;
        assertTrue(notificationService.optOutOfChannel(empId, "SMS"));
    }

    @Test
    public void testGetNotificationPreferences() {
        int empId = 102;
        Map<String, Boolean> prefs = notificationService.getNotificationPreferences(empId);
        assertNotNull(prefs);
    }

    @Test
    public void testMarkNotificationAsRead() {
        int notifId = 1;
        assertTrue(notificationService.markNotificationAsRead(notifId));
    }

    @Test
    public void testGetUnreadNotifications() {
        int empId = 103;
        List<Notification> unread = notificationService.getUnreadNotifications(empId);
        assertNotNull(unread);
    }

    @Test
    public void testApplyRateLimits() {
        int empId = 104;
        assertTrue(notificationService.applyRateLimits(empId));
    }

    @ParameterizedTest
    @ValueSource(strings = {"22:00", "06:00"})
    public void testBoundaryQuietHours(String timeStr) {
        assertTrue(notificationService.checkQuietHours(timeStr));
    }

    @Test
    public void testInvalidEmail() {
        Notification notif = new Notification("Test", "EMAIL");
        assertFalse(notificationService.sendEmailNotification(notif, "invalid-email"));
    }

    @Test
    public void testInvalidPhone() {
        Notification notif = new Notification("Test", "SMS");
        assertFalse(notificationService.sendSMSNotification(notif, "12345"));
    }

    @Test
    public void testOptOutUsers() {
        int empId = 105;
        notificationService.optOutOfChannel(empId, "EMAIL");
        Notification notif = new Notification("Test", "EMAIL");
        assertFalse(notificationService.sendEmailNotification(notif, "user@example.com"));
    }
}
