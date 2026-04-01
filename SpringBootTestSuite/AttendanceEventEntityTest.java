package com.warehouse.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Comprehensive JUnit test suite for AttendanceEvent entity.
 * Tests cover clock-in/out operations, geofence validation, and edge cases.
 * 
 * @author Warehouse EMS Test Team
 * @version 1.0.0
 */
@DisplayName("AttendanceEvent Entity Tests")
public class AttendanceEventEntityTest {

    private AttendanceEvent attendanceEvent;
    private LocalDateTime testTimestamp;

    @BeforeEach
    public void setUp() {
        testTimestamp = LocalDateTime.now();
        
        // Arrange: Create test attendance event
        attendanceEvent = AttendanceEvent.builder()
                .id(1L)
                .employeeId(100L)
                .timestamp(testTimestamp)
                .type("CLOCK_IN")
                .deviceId("DEVICE001")
                .location("40.7128,-74.0060")
                .shiftId(50L)
                .status("NORMAL")
                .build();
    }

    // ========== NORMAL CASE TESTS ==========

    @Test
    @DisplayName("Test clock-in event creation with valid data")
    public void testClockInEventCreation() {
        // Assert: Verify all fields are set correctly
        assertNotNull(attendanceEvent);
        assertEquals(1L, attendanceEvent.getId());
        assertEquals(100L, attendanceEvent.getEmployeeId());
        assertEquals(testTimestamp, attendanceEvent.getTimestamp());
        assertEquals("CLOCK_IN", attendanceEvent.getType());
        assertEquals("DEVICE001", attendanceEvent.getDeviceId());
        assertEquals("40.7128,-74.0060", attendanceEvent.getLocation());
        assertEquals(50L, attendanceEvent.getShiftId());
        assertEquals("NORMAL", attendanceEvent.getStatus());
    }

    @Test
    @DisplayName("Test clock-out event creation")
    public void testClockOutEventCreation() {
        // Arrange: Create clock-out event
        AttendanceEvent clockOut = AttendanceEvent.builder()
                .employeeId(100L)
                .timestamp(testTimestamp.plusHours(8))
                .type("CLOCK_OUT")
                .deviceId("DEVICE001")
                .location("40.7128,-74.0060")
                .shiftId(50L)
                .status("NORMAL")
                .build();

        // Assert: Verify clock-out event
        assertEquals("CLOCK_OUT", clockOut.getType());
        assertTrue(clockOut.getTimestamp().isAfter(testTimestamp));
    }

    @Test
    @DisplayName("Test attendance event with geofence location")
    public void testAttendanceEventWithGeofence() {
        // Arrange: Create event with GPS coordinates
        String gpsLocation = "40.7128,-74.0060";
        attendanceEvent.setLocation(gpsLocation);

        // Assert: Verify location is stored correctly
        assertEquals(gpsLocation, attendanceEvent.getLocation());
    }

    @Test
    @DisplayName("Test attendance event with device ID")
    public void testAttendanceEventWithDeviceId() {
        // Arrange: Set device ID
        attendanceEvent.setDeviceId("TABLET-WAREHOUSE-01");

        // Assert: Verify device ID is stored
        assertEquals("TABLET-WAREHOUSE-01", attendanceEvent.getDeviceId());
    }

    @Test
    @DisplayName("Test attendance event with shift association")
    public void testAttendanceEventWithShiftAssociation() {
        // Arrange: Associate with shift
        attendanceEvent.setShiftId(75L);

        // Assert: Verify shift association
        assertEquals(75L, attendanceEvent.getShiftId());
    }

    // ========== BOUNDARY CONDITION TESTS ==========

    @Test
    @DisplayName("Test attendance event with minimum required fields")
    public void testAttendanceEventWithMinimumFields() {
        // Arrange: Create event with only required fields
        AttendanceEvent minEvent = AttendanceEvent.builder()
                .employeeId(100L)
                .timestamp(testTimestamp)
                .type("CLOCK_IN")
                .build();

        // Assert: Verify required fields are set
        assertNotNull(minEvent);
        assertEquals(100L, minEvent.getEmployeeId());
        assertEquals(testTimestamp, minEvent.getTimestamp());
        assertEquals("CLOCK_IN", minEvent.getType());
        assertNull(minEvent.getDeviceId());
        assertNull(minEvent.getLocation());
        assertNull(minEvent.getShiftId());
    }

    @Test
    @DisplayName("Test attendance event at midnight boundary")
    public void testAttendanceEventAtMidnight() {
        // Arrange: Create event at midnight
        LocalDateTime midnight = LocalDateTime.now().truncatedTo(ChronoUnit.DAYS);
        attendanceEvent.setTimestamp(midnight);

        // Assert: Verify midnight timestamp
        assertEquals(midnight, attendanceEvent.getTimestamp());
        assertEquals(0, attendanceEvent.getTimestamp().getHour());
        assertEquals(0, attendanceEvent.getTimestamp().getMinute());
    }

    @Test
    @DisplayName("Test attendance event at end of day boundary")
    public void testAttendanceEventAtEndOfDay() {
        // Arrange: Create event at 23:59:59
        LocalDateTime endOfDay = LocalDateTime.now()
                .truncatedTo(ChronoUnit.DAYS)
                .plusHours(23)
                .plusMinutes(59)
                .plusSeconds(59);
        attendanceEvent.setTimestamp(endOfDay);

        // Assert: Verify end of day timestamp
        assertEquals(endOfDay, attendanceEvent.getTimestamp());
        assertEquals(23, attendanceEvent.getTimestamp().getHour());
        assertEquals(59, attendanceEvent.getTimestamp().getMinute());
    }

    @Test
    @DisplayName("Test attendance event with maximum device ID length")
    public void testAttendanceEventWithMaxDeviceIdLength() {
        // Arrange: Create device ID at maximum length (100 chars)
        String maxDeviceId = "D".repeat(100);
        attendanceEvent.setDeviceId(maxDeviceId);

        // Assert: Verify max length device ID
        assertEquals(100, attendanceEvent.getDeviceId().length());
        assertEquals(maxDeviceId, attendanceEvent.getDeviceId());
    }

    @Test
    @DisplayName("Test attendance event with maximum location length")
    public void testAttendanceEventWithMaxLocationLength() {
        // Arrange: Create location at maximum length (255 chars)
        String maxLocation = "L".repeat(255);
        attendanceEvent.setLocation(maxLocation);

        // Assert: Verify max length location
        assertEquals(255, attendanceEvent.getLocation().length());
        assertEquals(maxLocation, attendanceEvent.getLocation());
    }

    // ========== EDGE CASE TESTS ==========

    @Test
    @DisplayName("Test attendance event with null employee ID throws exception")
    public void testAttendanceEventWithNullEmployeeId() {
        // Act & Assert: Expect validation to fail for null employee ID
        assertThrows(NullPointerException.class, () -> {
            AttendanceEvent.builder()
                    .employeeId(null)
                    .timestamp(testTimestamp)
                    .type("CLOCK_IN")
                    .build();
        });
    }

    @Test
    @DisplayName("Test attendance event with null timestamp throws exception")
    public void testAttendanceEventWithNullTimestamp() {
        // Act & Assert: Expect validation to fail for null timestamp
        assertThrows(NullPointerException.class, () -> {
            AttendanceEvent.builder()
                    .employeeId(100L)
                    .timestamp(null)
                    .type("CLOCK_IN")
                    .build();
        });
    }

    @Test
    @DisplayName("Test attendance event with null type throws exception")
    public void testAttendanceEventWithNullType() {
        // Act & Assert: Expect validation to fail for null type
        assertThrows(NullPointerException.class, () -> {
            AttendanceEvent.builder()
                    .employeeId(100L)
                    .timestamp(testTimestamp)
                    .type(null)
                    .build();
        });
    }

    @Test
    @DisplayName("Test attendance event with empty type")
    public void testAttendanceEventWithEmptyType() {
        // Arrange: Create event with empty type
        AttendanceEvent emptyTypeEvent = AttendanceEvent.builder()
                .employeeId(100L)
                .timestamp(testTimestamp)
                .type("")
                .build();

        // Assert: Verify empty type is set (validation should catch this)
        assertEquals("", emptyTypeEvent.getType());
    }

    @Test
    @DisplayName("Test attendance event with invalid type")
    public void testAttendanceEventWithInvalidType() {
        // Arrange: Create event with invalid type
        attendanceEvent.setType("INVALID_TYPE");

        // Assert: Verify invalid type is set (validation should catch this)
        assertEquals("INVALID_TYPE", attendanceEvent.getType());
    }

    @Test
    @DisplayName("Test attendance event with null device ID")
    public void testAttendanceEventWithNullDeviceId() {
        // Arrange: Set device ID to null
        attendanceEvent.setDeviceId(null);

        // Assert: Verify null device ID is allowed
        assertNull(attendanceEvent.getDeviceId());
    }

    @Test
    @DisplayName("Test attendance event with empty device ID")
    public void testAttendanceEventWithEmptyDeviceId() {
        // Arrange: Set device ID to empty string
        attendanceEvent.setDeviceId("");

        // Assert: Verify empty device ID is allowed
        assertEquals("", attendanceEvent.getDeviceId());
    }

    @Test
    @DisplayName("Test attendance event with null location")
    public void testAttendanceEventWithNullLocation() {
        // Arrange: Set location to null
        attendanceEvent.setLocation(null);

        // Assert: Verify null location is allowed
        assertNull(attendanceEvent.getLocation());
    }

    @Test
    @DisplayName("Test attendance event with empty location")
    public void testAttendanceEventWithEmptyLocation() {
        // Arrange: Set location to empty string
        attendanceEvent.setLocation("");

        // Assert: Verify empty location is allowed
        assertEquals("", attendanceEvent.getLocation());
    }

    @Test
    @DisplayName("Test attendance event with null shift ID")
    public void testAttendanceEventWithNullShiftId() {
        // Arrange: Set shift ID to null
        attendanceEvent.setShiftId(null);

        // Assert: Verify null shift ID is allowed
        assertNull(attendanceEvent.getShiftId());
    }

    @Test
    @DisplayName("Test attendance event with zero employee ID")
    public void testAttendanceEventWithZeroEmployeeId() {
        // Arrange: Set employee ID to zero
        attendanceEvent.setEmployeeId(0L);

        // Assert: Verify zero employee ID is set
        assertEquals(0L, attendanceEvent.getEmployeeId());
    }

    @Test
    @DisplayName("Test attendance event with negative employee ID")
    public void testAttendanceEventWithNegativeEmployeeId() {
        // Arrange: Set employee ID to negative value
        attendanceEvent.setEmployeeId(-1L);

        // Assert: Verify negative employee ID is set (validation should catch this)
        assertEquals(-1L, attendanceEvent.getEmployeeId());
    }

    @Test
    @DisplayName("Test attendance event with future timestamp")
    public void testAttendanceEventWithFutureTimestamp() {
        // Arrange: Set timestamp to future
        LocalDateTime futureTime = LocalDateTime.now().plusDays(1);
        attendanceEvent.setTimestamp(futureTime);

        // Assert: Verify future timestamp is set (validation should catch this)
        assertEquals(futureTime, attendanceEvent.getTimestamp());
        assertTrue(attendanceEvent.getTimestamp().isAfter(LocalDateTime.now()));
    }

    @Test
    @DisplayName("Test attendance event with past timestamp")
    public void testAttendanceEventWithPastTimestamp() {
        // Arrange: Set timestamp to past
        LocalDateTime pastTime = LocalDateTime.now().minusYears(1);
        attendanceEvent.setTimestamp(pastTime);

        // Assert: Verify past timestamp is set
        assertEquals(pastTime, attendanceEvent.getTimestamp());
        assertTrue(attendanceEvent.getTimestamp().isBefore(LocalDateTime.now()));
    }

    @Test
    @DisplayName("Test attendance event status transitions")
    public void testAttendanceEventStatusTransitions() {
        // Test NORMAL status
        attendanceEvent.setStatus("NORMAL");
        assertEquals("NORMAL", attendanceEvent.getStatus());

        // Test CORRECTION_PENDING status
        attendanceEvent.setStatus("CORRECTION_PENDING");
        assertEquals("CORRECTION_PENDING", attendanceEvent.getStatus());

        // Test CORRECTED status
        attendanceEvent.setStatus("CORRECTED");
        assertEquals("CORRECTED", attendanceEvent.getStatus());
    }

    @Test
    @DisplayName("Test attendance event with special characters in location")
    public void testAttendanceEventWithSpecialCharactersInLocation() {
        // Arrange: Set location with special characters
        String specialLocation = "Warehouse #1, Building A-2 (Main)";
        attendanceEvent.setLocation(specialLocation);

        // Assert: Verify special characters are preserved
        assertEquals(specialLocation, attendanceEvent.getLocation());
    }

    @Test
    @DisplayName("Test attendance event with GPS coordinates format")
    public void testAttendanceEventWithGPSCoordinates() {
        // Arrange: Set various GPS coordinate formats
        String[] gpsFormats = {
            "40.7128,-74.0060",
            "40.7128, -74.0060",
            "40.7128Â°N, 74.0060Â°W",
            "N40.7128 W74.0060"
        };

        for (String gps : gpsFormats) {
            attendanceEvent.setLocation(gps);
            assertEquals(gps, attendanceEvent.getLocation());
        }
    }

    @Test
    @DisplayName("Test attendance event with multiple clock-ins same day")
    public void testMultipleClockInsSameDay() {
        // Arrange: Create multiple clock-in events for same employee
        AttendanceEvent clockIn1 = AttendanceEvent.builder()
                .employeeId(100L)
                .timestamp(testTimestamp.withHour(8))
                .type("CLOCK_IN")
                .build();

        AttendanceEvent clockIn2 = AttendanceEvent.builder()
                .employeeId(100L)
                .timestamp(testTimestamp.withHour(13))
                .type("CLOCK_IN")
                .build();

        // Assert: Verify both events are created
        assertNotNull(clockIn1);
        assertNotNull(clockIn2);
        assertEquals(clockIn1.getEmployeeId(), clockIn2.getEmployeeId());
        assertTrue(clockIn2.getTimestamp().isAfter(clockIn1.getTimestamp()));
    }

    @Test
    @DisplayName("Test attendance event with clock-out before clock-in")
    public void testClockOutBeforeClockIn() {
        // Arrange: Create clock-out before clock-in (invalid scenario)
        AttendanceEvent clockOut = AttendanceEvent.builder()
                .employeeId(100L)
                .timestamp(testTimestamp.withHour(8))
                .type("CLOCK_OUT")
                .build();

        AttendanceEvent clockIn = AttendanceEvent.builder()
                .employeeId(100L)
                .timestamp(testTimestamp.withHour(9))
                .type("CLOCK_IN")
                .build();

        // Assert: Verify both events are created (business logic should validate)
        assertNotNull(clockOut);
        assertNotNull(clockIn);
        assertTrue(clockIn.getTimestamp().isAfter(clockOut.getTimestamp()));
    }

    @Test
    @DisplayName("Test attendance event with very long device ID")
    public void testAttendanceEventWithVeryLongDeviceId() {
        // Arrange: Create device ID exceeding typical length
        String longDeviceId = "DEVICE-" + "X".repeat(200);
        attendanceEvent.setDeviceId(longDeviceId);

        // Assert: Verify long device ID is set
        assertEquals(longDeviceId, attendanceEvent.getDeviceId());
    }

    @Test
    @DisplayName("Test attendance event with correction pending status")
    public void testAttendanceEventWithCorrectionPending() {
        // Arrange: Set status to correction pending
        attendanceEvent.setStatus("CORRECTION_PENDING");

        // Assert: Verify correction pending status
        assertEquals("CORRECTION_PENDING", attendanceEvent.getStatus());
    }

    @Test
    @DisplayName("Test attendance event timestamp precision")
    public void testAttendanceEventTimestampPrecision() {
        // Arrange: Create timestamp with nanosecond precision
        LocalDateTime preciseTime = LocalDateTime.now();
        attendanceEvent.setTimestamp(preciseTime);

        // Assert: Verify timestamp precision is maintained
        assertEquals(preciseTime, attendanceEvent.getTimestamp());
        assertEquals(preciseTime.getNano(), attendanceEvent.getTimestamp().getNano());
    }
}
