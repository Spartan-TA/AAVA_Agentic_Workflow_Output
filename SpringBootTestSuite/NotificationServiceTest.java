import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class NotificationServiceTest {
    private NotificationService service;

    @BeforeEach
    public void setUp() {
        service = new NotificationService();
    }

    @Test
    public void testSendNotification_Valid() {
        Notification notif = new Notification("emp1", "Your shift starts soon", "email");
        assertDoesNotThrow(() -> service.sendNotification(notif));
    }

    @Test
    public void testSendNotification_NullMessage() {
        Notification notif = new Notification("emp2", null, "sms");
        assertThrows(IllegalArgumentException.class, () -> service.sendNotification(notif));
    }

    @Test
    public void testSendNotification_InvalidChannel() {
        Notification notif = new Notification("emp3", "Test", "fax");
        assertThrows(InvalidChannelException.class, () -> service.sendNotification(notif));
    }

    @Test
    public void testBroadcastAnnouncement_Valid() {
        assertDoesNotThrow(() -> service.broadcastAnnouncement("System update tonight", new String[]{"emp1", "emp2"}));
    }

    @Test
    public void testBroadcastAnnouncement_EmptyList() {
        assertThrows(IllegalArgumentException.class, () -> service.broadcastAnnouncement("No recipients", new String[]{}));
    }

    @Test
    public void testSendEventNotification_Valid() {
        EventNotification eventNotif = new EventNotification("emp4", "Fire drill", "push");
        assertDoesNotThrow(() -> service.sendEventNotification(eventNotif));
    }

    @Test
    public void testSetPreferences_Valid() {
        assertTrue(service.setPreferences("emp5", "email", true));
    }

    @Test
    public void testSetPreferences_InvalidChannel() {
        assertThrows(InvalidChannelException.class, () -> service.setPreferences("emp6", "pager", true));
    }

    @Test
    public void testDeliverViaChannel_MultiChannel() {
        Notification notif = new Notification("emp7", "Multi-channel test", "email,sms");
        assertTrue(service.deliverViaChannel(notif));
    }

    @Test
    public void testDeliverViaChannel_NullNotification() {
        assertThrows(IllegalArgumentException.class, () -> service.deliverViaChannel(null));
    }
}