package com.company.warehouse.attendance;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for AttendanceService
 * Covers clock-in/out operations and hours calculation
 */
@DisplayName("Attendance Service Tests")
public class AttendanceServiceTest {

    @Mock
    private ClockEventRepository clockEventRepository;

    @InjectMocks
    private AttendanceService attendanceService;

    private Long employeeId;
    private Double latitude;
    private Double longitude;
    private String deviceId;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        employeeId = 1L;
        latitude = 40.7128;
        longitude = -74.0060;
        deviceId = "DEVICE123";
    }

    // ========== CLOCK-IN TESTS ==========

    @Test
    @DisplayName("Test clock-in with valid data")
    public void testClockInWithValidData() {
        // Arrange
        ClockEvent savedEvent = new ClockEvent();
        savedEvent.setId(1L);
        savedEvent.setEmployeeId(employeeId);
        savedEvent.setType(ClockEventType.CLOCK_IN);
        savedEvent.setLatitude(latitude);
        savedEvent.setLongitude(longitude);
        savedEvent.setDeviceId(deviceId);
        savedEvent.setTimestamp(LocalDateTime.now());

        when(clockEventRepository.save(any(ClockEvent.class))).thenReturn(savedEvent);

        // Act
        ClockEvent result = attendanceService.clockIn(employeeId, latitude, longitude, deviceId);

        // Assert
        assertNotNull(result);
        assertEquals(employeeId, result.getEmployeeId());
        assertEquals(ClockEventType.CLOCK_IN, result.getType());
        assertEquals(latitude, result.getLatitude());
        assertEquals(longitude, result.getLongitude());
        assertEquals(deviceId, result.getDeviceId());
        assertNotNull(result.getTimestamp());
        verify(clockEventRepository, times(1)).save(any(ClockEvent.class));
    }

    @Test
    @DisplayName("Test clock-in with null geolocation")
    public void testClockInWithNullGeolocation() {
        // Arrange
        ClockEvent savedEvent = new ClockEvent();
        savedEvent.setId(1L);
        savedEvent.setEmployeeId(employeeId);
        savedEvent.setType(ClockEventType.CLOCK_IN);
        savedEvent.setLatitude(null);
        savedEvent.setLongitude(null);
        savedEvent.setDeviceId(deviceId);

        when(clockEventRepository.save(any(ClockEvent.class))).thenReturn(savedEvent);

        // Act
        ClockEvent result = attendanceService.clockIn(employeeId, null, null, deviceId);

        // Assert
        assertNotNull(result);
        assertNull(result.getLatitude());
        assertNull(result.getLongitude());
        verify(clockEventRepository, times(1)).save(any(ClockEvent.class));
    }

    @Test
    @DisplayName("Test clock-in with null device ID")
    public void testClockInWithNullDeviceId() {
        // Arrange
        ClockEvent savedEvent = new ClockEvent();
        savedEvent.setId(1L);
        savedEvent.setEmployeeId(employeeId);
        savedEvent.setType(ClockEventType.CLOCK_IN);
        savedEvent.setDeviceId(null);

        when(clockEventRepository.save(any(ClockEvent.class))).thenReturn(savedEvent);

        // Act
        ClockEvent result = attendanceService.clockIn(employeeId, latitude, longitude, null);

        // Assert
        assertNotNull(result);
        assertNull(result.getDeviceId());
        verify(clockEventRepository, times(1)).save(any(ClockEvent.class));
    }

    @Test
    @DisplayName("Test clock-in with boundary latitude values")
    public void testClockInWithBoundaryLatitude() {
        // Arrange
        Double minLatitude = -90.0;
        Double maxLatitude = 90.0;
        ClockEvent savedEvent = new ClockEvent();
        savedEvent.setEmployeeId(employeeId);
        savedEvent.setType(ClockEventType.CLOCK_IN);

        when(clockEventRepository.save(any(ClockEvent.class))).thenReturn(savedEvent);

        // Act - Test minimum latitude
        ClockEvent result1 = attendanceService.clockIn(employeeId, minLatitude, longitude, deviceId);
        // Act - Test maximum latitude
        ClockEvent result2 = attendanceService.clockIn(employeeId, maxLatitude, longitude, deviceId);

        // Assert
        assertNotNull(result1);
        assertNotNull(result2);
        verify(clockEventRepository, times(2)).save(any(ClockEvent.class));
    }

    @Test
    @DisplayName("Test clock-in with boundary longitude values")
    public void testClockInWithBoundaryLongitude() {
        // Arrange
        Double minLongitude = -180.0;
        Double maxLongitude = 180.0;
        ClockEvent savedEvent = new ClockEvent();
        savedEvent.setEmployeeId(employeeId);
        savedEvent.setType(ClockEventType.CLOCK_IN);

        when(clockEventRepository.save(any(ClockEvent.class))).thenReturn(savedEvent);

        // Act
        ClockEvent result1 = attendanceService.clockIn(employeeId, latitude, minLongitude, deviceId);
        ClockEvent result2 = attendanceService.clockIn(employeeId, latitude, maxLongitude, deviceId);

        // Assert
        assertNotNull(result1);
        assertNotNull(result2);
        verify(clockEventRepository, times(2)).save(any(ClockEvent.class));
    }

    // ========== CLOCK-OUT TESTS ==========

    @Test
    @DisplayName("Test clock-out with valid data")
    public void testClockOutWithValidData() {
        // Arrange
        ClockEvent savedEvent = new ClockEvent();
        savedEvent.setId(2L);
        savedEvent.setEmployeeId(employeeId);
        savedEvent.setType(ClockEventType.CLOCK_OUT);
        savedEvent.setLatitude(latitude);
        savedEvent.setLongitude(longitude);
        savedEvent.setDeviceId(deviceId);
        savedEvent.setTimestamp(LocalDateTime.now());

        when(clockEventRepository.save(any(ClockEvent.class))).thenReturn(savedEvent);

        // Act
        ClockEvent result = attendanceService.clockOut(employeeId, latitude, longitude, deviceId);

        // Assert
        assertNotNull(result);
        assertEquals(employeeId, result.getEmployeeId());
        assertEquals(ClockEventType.CLOCK_OUT, result.getType());
        assertEquals(latitude, result.getLatitude());
        assertEquals(longitude, result.getLongitude());
        assertEquals(deviceId, result.getDeviceId());
        assertNotNull(result.getTimestamp());
        verify(clockEventRepository, times(1)).save(any(ClockEvent.class));
    }

    @Test
    @DisplayName("Test clock-out with null geolocation")
    public void testClockOutWithNullGeolocation() {
        // Arrange
        ClockEvent savedEvent = new ClockEvent();
        savedEvent.setEmployeeId(employeeId);
        savedEvent.setType(ClockEventType.CLOCK_OUT);

        when(clockEventRepository.save(any(ClockEvent.class))).thenReturn(savedEvent);

        // Act
        ClockEvent result = attendanceService.clockOut(employeeId, null, null, deviceId);

        // Assert
        assertNotNull(result);
        assertNull(result.getLatitude());
        assertNull(result.getLongitude());
        verify(clockEventRepository, times(1)).save(any(ClockEvent.class));
    }

    @Test
    @DisplayName("Test clock-out without prior clock-in")
    public void testClockOutWithoutClockIn() {
        // Arrange
        ClockEvent savedEvent = new ClockEvent();
        savedEvent.setEmployeeId(employeeId);
        savedEvent.setType(ClockEventType.CLOCK_OUT);

        when(clockEventRepository.save(any(ClockEvent.class))).thenReturn(savedEvent);

        // Act
        ClockEvent result = attendanceService.clockOut(employeeId, latitude, longitude, deviceId);

        // Assert
        assertNotNull(result);
        assertEquals(ClockEventType.CLOCK_OUT, result.getType());
        verify(clockEventRepository, times(1)).save(any(ClockEvent.class));
    }

    // ========== CALCULATE HOURS WORKED TESTS ==========

    @Test
    @DisplayName("Test calculate hours with complete clock-in/out pair")
    public void testCalculateHoursWithCompletePair() {
        // Arrange
        LocalDate date = LocalDate.of(2024, 1, 15);
        LocalDateTime clockInTime = date.atTime(9, 0);
        LocalDateTime clockOutTime = date.atTime(17, 0);

        ClockEvent clockIn = new ClockEvent();
        clockIn.setEmployeeId(employeeId);
        clockIn.setType(ClockEventType.CLOCK_IN);
        clockIn.setTimestamp(clockInTime);

        ClockEvent clockOut = new ClockEvent();
        clockOut.setEmployeeId(employeeId);
        clockOut.setType(ClockEventType.CLOCK_OUT);
        clockOut.setTimestamp(clockOutTime);

        when(clockEventRepository.findByEmployeeIdAndTimestampBetween(
            eq(employeeId), any(LocalDateTime.class), any(LocalDateTime.class)))
            .thenReturn(Arrays.asList(clockIn, clockOut));

        // Act
        Duration result = attendanceService.calculateHoursWorked(employeeId, date);

        // Assert
        assertNotNull(result);
        assertEquals(8, result.toHours());
        verify(clockEventRepository, times(1)).findByEmployeeIdAndTimestampBetween(
            eq(employeeId), any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("Test calculate hours with multiple clock-in/out pairs")
    public void testCalculateHoursWithMultiplePairs() {
        // Arrange
        LocalDate date = LocalDate.of(2024, 1, 15);
        
        ClockEvent clockIn1 = new ClockEvent();
        clockIn1.setType(ClockEventType.CLOCK_IN);
        clockIn1.setTimestamp(date.atTime(9, 0));

        ClockEvent clockOut1 = new ClockEvent();
        clockOut1.setType(ClockEventType.CLOCK_OUT);
        clockOut1.setTimestamp(date.atTime(12, 0));

        ClockEvent clockIn2 = new ClockEvent();
        clockIn2.setType(ClockEventType.CLOCK_IN);
        clockIn2.setTimestamp(date.atTime(13, 0));

        ClockEvent clockOut2 = new ClockEvent();
        clockOut2.setType(ClockEventType.CLOCK_OUT);
        clockOut2.setTimestamp(date.atTime(17, 0));

        when(clockEventRepository.findByEmployeeIdAndTimestampBetween(
            eq(employeeId), any(LocalDateTime.class), any(LocalDateTime.class)))
            .thenReturn(Arrays.asList(clockIn1, clockOut1, clockIn2, clockOut2));

        // Act
        Duration result = attendanceService.calculateHoursWorked(employeeId, date);

        // Assert
        assertNotNull(result);
        assertEquals(7, result.toHours()); // 3 hours + 4 hours
        verify(clockEventRepository, times(1)).findByEmployeeIdAndTimestampBetween(
            eq(employeeId), any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("Test calculate hours with no events")
    public void testCalculateHoursWithNoEvents() {
        // Arrange
        LocalDate date = LocalDate.of(2024, 1, 15);
        when(clockEventRepository.findByEmployeeIdAndTimestampBetween(
            eq(employeeId), any(LocalDateTime.class), any(LocalDateTime.class)))
            .thenReturn(Collections.emptyList());

        // Act
        Duration result = attendanceService.calculateHoursWorked(employeeId, date);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.toHours());
        verify(clockEventRepository, times(1)).findByEmployeeIdAndTimestampBetween(
            eq(employeeId), any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("Test calculate hours with only clock-in (no clock-out)")
    public void testCalculateHoursWithOnlyClockIn() {
        // Arrange
        LocalDate date = LocalDate.of(2024, 1, 15);
        ClockEvent clockIn = new ClockEvent();
        clockIn.setType(ClockEventType.CLOCK_IN);
        clockIn.setTimestamp(date.atTime(9, 0));

        when(clockEventRepository.findByEmployeeIdAndTimestampBetween(
            eq(employeeId), any(LocalDateTime.class), any(LocalDateTime.class)))
            .thenReturn(Arrays.asList(clockIn));

        // Act
        Duration result = attendanceService.calculateHoursWorked(employeeId, date);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.toHours()); // No complete pair
        verify(clockEventRepository, times(1)).findByEmployeeIdAndTimestampBetween(
            eq(employeeId), any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("Test calculate hours with only clock-out (no clock-in)")
    public void testCalculateHoursWithOnlyClockOut() {
        // Arrange
        LocalDate date = LocalDate.of(2024, 1, 15);
        ClockEvent clockOut = new ClockEvent();
        clockOut.setType(ClockEventType.CLOCK_OUT);
        clockOut.setTimestamp(date.atTime(17, 0));

        when(clockEventRepository.findByEmployeeIdAndTimestampBetween(
            eq(employeeId), any(LocalDateTime.class), any(LocalDateTime.class)))
            .thenReturn(Arrays.asList(clockOut));

        // Act
        Duration result = attendanceService.calculateHoursWorked(employeeId, date);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.toHours()); // No complete pair
        verify(clockEventRepository, times(1)).findByEmployeeIdAndTimestampBetween(
            eq(employeeId), any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("Test calculate hours with midnight crossing")
    public void testCalculateHoursWithMidnightCrossing() {
        // Arrange
        LocalDate date = LocalDate.of(2024, 1, 15);
        ClockEvent clockIn = new ClockEvent();
        clockIn.setType(ClockEventType.CLOCK_IN);
        clockIn.setTimestamp(date.atTime(23, 0));

        ClockEvent clockOut = new ClockEvent();
        clockOut.setType(ClockEventType.CLOCK_OUT);
        clockOut.setTimestamp(date.atTime(23, 59)); // Before midnight

        when(clockEventRepository.findByEmployeeIdAndTimestampBetween(
            eq(employeeId), any(LocalDateTime.class), any(LocalDateTime.class)))
            .thenReturn(Arrays.asList(clockIn, clockOut));

        // Act
        Duration result = attendanceService.calculateHoursWorked(employeeId, date);

        // Assert
        assertNotNull(result);
        assertTrue(result.toMinutes() < 60); // Less than 1 hour
        verify(clockEventRepository, times(1)).findByEmployeeIdAndTimestampBetween(
            eq(employeeId), any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("Test calculate hours with very short duration")
    public void testCalculateHoursWithShortDuration() {
        // Arrange
        LocalDate date = LocalDate.of(2024, 1, 15);
        ClockEvent clockIn = new ClockEvent();
        clockIn.setType(ClockEventType.CLOCK_IN);
        clockIn.setTimestamp(date.atTime(9, 0));

        ClockEvent clockOut = new ClockEvent();
        clockOut.setType(ClockEventType.CLOCK_OUT);
        clockOut.setTimestamp(date.atTime(9, 1)); // 1 minute

        when(clockEventRepository.findByEmployeeIdAndTimestampBetween(
            eq(employeeId), any(LocalDateTime.class), any(LocalDateTime.class)))
            .thenReturn(Arrays.asList(clockIn, clockOut));

        // Act
        Duration result = attendanceService.calculateHoursWorked(employeeId, date);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.toMinutes());
        verify(clockEventRepository, times(1)).findByEmployeeIdAndTimestampBetween(
            eq(employeeId), any(LocalDateTime.class), any(LocalDateTime.class));
    }

    // ========== GET EMPLOYEE CLOCK EVENTS TESTS ==========

    @Test
    @DisplayName("Test get employee clock events with valid employee ID")
    public void testGetEmployeeClockEventsWithValidId() {
        // Arrange
        ClockEvent event1 = new ClockEvent();
        event1.setEmployeeId(employeeId);
        event1.setType(ClockEventType.CLOCK_IN);

        ClockEvent event2 = new ClockEvent();
        event2.setEmployeeId(employeeId);
        event2.setType(ClockEventType.CLOCK_OUT);

        List<ClockEvent> events = Arrays.asList(event2, event1); // Descending order
        when(clockEventRepository.findByEmployeeIdOrderByTimestampDesc(employeeId))
            .thenReturn(events);

        // Act
        List<ClockEvent> result = attendanceService.getEmployeeClockEvents(employeeId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(ClockEventType.CLOCK_OUT, result.get(0).getType());
        assertEquals(ClockEventType.CLOCK_IN, result.get(1).getType());
        verify(clockEventRepository, times(1)).findByEmployeeIdOrderByTimestampDesc(employeeId);
    }

    @Test
    @DisplayName("Test get employee clock events with no events")
    public void testGetEmployeeClockEventsWithNoEvents() {
        // Arrange
        when(clockEventRepository.findByEmployeeIdOrderByTimestampDesc(employeeId))
            .thenReturn(Collections.emptyList());

        // Act
        List<ClockEvent> result = attendanceService.getEmployeeClockEvents(employeeId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(clockEventRepository, times(1)).findByEmployeeIdOrderByTimestampDesc(employeeId);
    }

    @Test
    @DisplayName("Test get employee clock events with null employee ID")
    public void testGetEmployeeClockEventsWithNullId() {
        // Arrange
        when(clockEventRepository.findByEmployeeIdOrderByTimestampDesc(null))
            .thenReturn(Collections.emptyList());

        // Act
        List<ClockEvent> result = attendanceService.getEmployeeClockEvents(null);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(clockEventRepository, times(1)).findByEmployeeIdOrderByTimestampDesc(null);
    }
}