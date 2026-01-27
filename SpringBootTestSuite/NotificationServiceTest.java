package com.warehouse.ems.service;

import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
class NotificationServiceTest {
    @Autowired
    NotificationService notificationService;

    @MockBean
    NotificationChannel emailChannel;
    @MockBean
    NotificationChannel smsChannel;
    @MockBean
    NotificationChannel inAppChannel;

    @BeforeEach
    void setup() {
        // Assume NotificationService wires channels
        notificationService.setChannels(Arrays.asList(emailChannel, smsChannel, inAppChannel));
    }

    @Test
    void testSendNotification_AllChannels_Success() {
        Notification notif = new Notification("Shift changed", "Your shift is now 2pm", Arrays.asList("email", "sms", "inapp"));
        when(emailChannel.send(any())).thenReturn(true);
        when(smsChannel.send(any())).thenReturn(true);
        when(inAppChannel.send(any())).thenReturn(true);
        boolean result = notificationService.sendNotification(notif, "user1");
        assertTrue(result);
        verify(emailChannel).send(notif);
        verify(smsChannel).send(notif);
        verify(inAppChannel).send(notif);
    }

    @Test
    void testOptOut_Channel() {
        notificationService.optOut("user1", "sms");
        Notification notif = new Notification("Test", "Body", Arrays.asList("sms"));
        when(smsChannel.send(any())).thenReturn(true);
        boolean result = notificationService.sendNotification(notif, "user1");
        assertFalse(result); // Should not send due to opt-out
        verify(smsChannel, never()).send(notif);
    }

    @Test
    void testRateLimiting() {
        notificationService.setRateLimit("user1", 1); // 1 per minute
        Notification notif = new Notification("Test", "Body", Arrays.asList("email"));
        when(emailChannel.send(any())).thenReturn(true);
        assertTrue(notificationService.sendNotification(notif, "user1"));
        assertFalse(notificationService.sendNotification(notif, "user1")); // Exceeds rate
    }

    @Test
    void testQuietHours() {
        notificationService.setQuietHours("user1", 22, 6); // 10pm-6am
        Notification notif = new Notification("Night", "Body", Arrays.asList("email"));
        when(emailChannel.send(any())).thenReturn(true);
        // Simulate current time in quiet hours
        notificationService.setCurrentHour(23);
        assertFalse(notificationService.sendNotification(notif, "user1"));
        notificationService.setCurrentHour(7);
        assertTrue(notificationService.sendNotification(notif, "user1"));
    }

    @Test
    void testNullNotification_Throws() {
        assertThrows(IllegalArgumentException.class, () -> notificationService.sendNotification(null, "user1"));
    }

    @Test
    void testEmptyChannels() {
        Notification notif = new Notification("", "", Collections.emptyList());
        assertFalse(notificationService.sendNotification(notif, "user1"));
    }

    @Test
    void testInvalidChannel() {
        Notification notif = new Notification("Test", "Body", Arrays.asList("fax"));
        assertFalse(notificationService.sendNotification(notif, "user1"));
    }

    @Test
    void testIntegration_MultiUser() {
        Notification notif = new Notification("Announcement", "Body", Arrays.asList("email", "inapp"));
        when(emailChannel.send(any())).thenReturn(true);
        when(inAppChannel.send(any())).thenReturn(true);
        List<String> users = Arrays.asList("user1", "user2", "user3");
        Map<String, Boolean> results = notificationService.sendBulkNotification(notif, users);
        assertEquals(3, results.size());
        assertTrue(results.values().stream().allMatch(Boolean::booleanValue));
    }
}
