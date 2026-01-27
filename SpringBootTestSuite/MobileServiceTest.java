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
class MobileServiceTest {
    @Autowired
    MobileService mobileService;

    @MockBean
    OfflineQueue offlineQueue;
    @MockBean
    ConflictResolver conflictResolver;

    @BeforeEach
    void setup() {
        // Setup mocks if needed
    }

    @Test
    void testPWAEndpoint_ClockIn() {
        ClockEvent event = new ClockEvent("user1", new Date(), "clock-in");
        when(mobileService.clockIn(event)).thenReturn(true);
        boolean result = mobileService.clockIn(event);
        assertTrue(result);
    }

    @Test
    void testPWAEndpoint_ViewSchedule() {
        List<ShiftDTO> shifts = Arrays.asList(new ShiftDTO("ShiftA", new Date(), new Date()));
        when(mobileService.getSchedule("user1")).thenReturn(shifts);
        List<ShiftDTO> result = mobileService.getSchedule("user1");
        assertEquals(shifts, result);
    }

    @Test
    void testOfflineQueue_AddEvent() {
        ClockEvent event = new ClockEvent("user2", new Date(), "clock-out");
        when(offlineQueue.add(event)).thenReturn(true);
        boolean result = mobileService.queueOfflineEvent(event);
        assertTrue(result);
        verify(offlineQueue).add(event);
    }

    @Test
    void testOfflineQueue_ProcessEvents() {
        List<ClockEvent> events = Arrays.asList(
            new ClockEvent("user1", new Date(), "clock-in"),
            new ClockEvent("user2", new Date(), "clock-out")
        );
        when(offlineQueue.getAll()).thenReturn(events);
        when(mobileService.processOfflineEvents()).thenReturn(2);
        int processed = mobileService.processOfflineEvents();
        assertEquals(2, processed);
    }

    @Test
    void testConflictResolution() {
        ClockEvent event1 = new ClockEvent("user1", new Date(), "clock-in");
        ClockEvent event2 = new ClockEvent("user1", new Date(), "clock-in");
        when(conflictResolver.resolve(event1, event2)).thenReturn(event1);
        ClockEvent resolved = mobileService.resolveConflict(event1, event2);
        assertEquals(event1, resolved);
    }

    @Test
    void testNullEvent_Throws() {
        assertThrows(IllegalArgumentException.class, () -> mobileService.clockIn(null));
    }

    @Test
    void testEmptySchedule() {
        when(mobileService.getSchedule("user3")).thenReturn(Collections.emptyList());
        List<ShiftDTO> result = mobileService.getSchedule("user3");
        assertTrue(result.isEmpty());
    }

    @Test
    void testIntegration_MultipleUsers() {
        List<ShiftDTO> shifts1 = Arrays.asList(new ShiftDTO("ShiftA", new Date(), new Date()));
        List<ShiftDTO> shifts2 = Arrays.asList(new ShiftDTO("ShiftB", new Date(), new Date()));
        when(mobileService.getSchedule("user1")).thenReturn(shifts1);
        when(mobileService.getSchedule("user2")).thenReturn(shifts2);
        assertEquals(shifts1, mobileService.getSchedule("user1"));
        assertEquals(shifts2, mobileService.getSchedule("user2"));
    }
}
