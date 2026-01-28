package com.wms.ems.attendance;

import com.wms.ems.employee.Employee;
import com.wms.ems.enums.AttendanceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.time.LocalDateTime;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for AttendanceEvent entity
 * Tests cover:
 * - Normal clock-in/out scenarios
 * - Timestamp validation
 * - Device and location tracking
 * - Employee association
 * - Approval workflow
 * - Edge cases (null values, invalid data)
 */
@DisplayName("AttendanceEvent Entity Tests")
public class AttendanceEventEntityTest {

    private Validator validator;
    private AttendanceEvent attendanceEvent;
    private Employee testEmployee;

    @BeforeEach
    public void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        
        // Create test employee
        testEmployee = new Employee();
        testEmployee.setId(1L);
        testEmployee.setName("John Doe");
        testEmployee.setBadgeId("EMP001");
        
        // Initialize valid attendance event
        attendanceEvent = new AttendanceEvent();
        attendanceEvent.setEmployee(testEmployee);
        attendanceEvent.setType(AttendanceType.CLOCK_IN);
        attendanceEvent.setTimestamp(LocalDateTime.now());
        attendanceEvent.setDeviceId("DEVICE001");
        attendanceEvent.setLocation("Warehouse Entrance");
        attendanceEvent.setApproved(true);
    }

    // ========== NORMAL CASES ==========

    @Test
    @DisplayName("Should create attendance event with valid data")
    public void testCreateAttendanceEventWithValidData() {
        // Act
        Set<ConstraintViolation<AttendanceEvent>> violations = validator.validate(attendanceEvent);
        
        // Assert
        assertTrue(violations.isEmpty(), "Valid attendance event should have no validation errors");
        assertEquals(AttendanceType.CLOCK_IN, attendanceEvent.getType());
        assertEquals(testEmployee, attendanceEvent.getEmployee());
        assertTrue(attendanceEvent.isApproved());
    }

    @Test
    @DisplayName("Should create clock-in event")
    public void testCreateClockInEvent() {
        // Arrange
        attendanceEvent.setType(AttendanceType.CLOCK_IN);
        
        // Act
        Set<ConstraintViolation<AttendanceEvent>> violations = validator.validate(attendanceEvent);
        
        // Assert
        assertTrue(violations.isEmpty());
        assertEquals(AttendanceType.CLOCK_IN, attendanceEvent.getType());
    }

    @Test
    @DisplayName("Should create clock-out event")
    public void testCreateClockOutEvent() {
        // Arrange
        attendanceEvent.setType(AttendanceType.CLOCK_OUT);
        
        // Act
        Set<ConstraintViolation<AttendanceEvent>> violations = validator.validate(attendanceEvent);
        
        // Assert
        assertTrue(violations.isEmpty());
        assertEquals(AttendanceType.CLOCK_OUT, attendanceEvent.getType());
    }

    @Test
    @DisplayName("Should set and get all attendance event fields correctly")
    public void testAttendanceEventGettersAndSetters() {
        // Arrange
        Long id = 1L;
        LocalDateTime timestamp = LocalDateTime.of(2024, 1, 15, 8, 0);
        String deviceId = "DEVICE002";
        String location = "Warehouse Exit";
        boolean approved = false;
        
        // Act
        attendanceEvent.setId(id);
        attendanceEvent.setTimestamp(timestamp);
        attendanceEvent.setDeviceId(deviceId);
        attendanceEvent.setLocation(location);
        attendanceEvent.setApproved(approved);
        
        // Assert
        assertEquals(id, attendanceEvent.getId());
        assertEquals(timestamp, attendanceEvent.getTimestamp());
        assertEquals(deviceId, attendanceEvent.getDeviceId());
        assertEquals(location, attendanceEvent.getLocation());
        assertFalse(attendanceEvent.isApproved());
    }

    @Test
    @DisplayName("Should associate attendance event with employee")
    public void testEmployeeAssociation() {
        // Assert
        assertNotNull(attendanceEvent.getEmployee());
        assertEquals("John Doe", attendanceEvent.getEmployee().getName());
        assertEquals("EMP001", attendanceEvent.getEmployee().getBadgeId());
    }

    // ========== TIMESTAMP VALIDATION ==========

    @Test
    @DisplayName("Should accept current timestamp")
    public void testCurrentTimestamp() {
        // Arrange
        attendanceEvent.setTimestamp(LocalDateTime.now());
        
        // Act
        Set<ConstraintViolation<AttendanceEvent>> violations = validator.validate(attendanceEvent);
        
        // Assert
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("Should accept past timestamp")
    public void testPastTimestamp() {
        // Arrange
        attendanceEvent.setTimestamp(LocalDateTime.now().minusHours(2));
        
        // Act
        Set<ConstraintViolation<AttendanceEvent>> violations = validator.validate(attendanceEvent);
        
        // Assert
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("Should reject future timestamp")
    public void testFutureTimestamp() {
        // Arrange
        attendanceEvent.setTimestamp(LocalDateTime.now().plusHours(1));
        
        // Act
        Set<ConstraintViolation<AttendanceEvent>> violations = validator.validate(attendanceEvent);
        
        // Assert
        assertFalse(violations.isEmpty(), "Future timestamp should cause validation error");
    }

    @Test
    @DisplayName("Should handle timestamp at midnight")
    public void testMidnightTimestamp() {
        // Arrange
        attendanceEvent.setTimestamp(LocalDateTime.now().withHour(0).withMinute(0).withSecond(0));
        
        // Act
        Set<ConstraintViolation<AttendanceEvent>> violations = validator.validate(attendanceEvent);
        
        // Assert
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("Should handle timestamp at end of day")
    public void testEndOfDayTimestamp() {
        // Arrange
        attendanceEvent.setTimestamp(LocalDateTime.now().withHour(23).withMinute(59).withSecond(59));
        
        // Act
        Set<ConstraintViolation<AttendanceEvent>> violations = validator.validate(attendanceEvent);
        
        // Assert
        assertTrue(violations.isEmpty());
    }

    // ========== DEVICE AND LOCATION TRACKING ==========

    @Test
    @DisplayName("Should accept valid device ID")
    public void testValidDeviceId() {
        // Arrange
        attendanceEvent.setDeviceId("DEVICE123");
        
        // Act
        Set<ConstraintViolation<AttendanceEvent>> violations = validator.validate(attendanceEvent);
        
        // Assert
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("Should accept alphanumeric device ID")
    public void testAlphanumericDeviceId() {
        // Arrange
        attendanceEvent.setDeviceId("DEV-001-ABC");
        
        // Act
        Set<ConstraintViolation<AttendanceEvent>> violations = validator.validate(attendanceEvent);
        
        // Assert
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("Should accept valid location")
    public void testValidLocation() {
        // Arrange
        attendanceEvent.setLocation("Warehouse Section A");
        
        // Act
        Set<ConstraintViolation<AttendanceEvent>> violations = validator.validate(attendanceEvent);
        
        // Assert
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("Should accept location with special characters")
    public void testLocationWithSpecialCharacters() {
        // Arrange
        attendanceEvent.setLocation("Warehouse - Section A (Main)");
        
        // Act
        Set<ConstraintViolation<AttendanceEvent>> violations = validator.validate(attendanceEvent);
        
        // Assert
        assertTrue(violations.isEmpty());
    }

    // ========== APPROVAL WORKFLOW ==========

    @Test
    @DisplayName("Should handle approved status")
    public void testApprovedStatus() {
        // Arrange
        attendanceEvent.setApproved(true);
        
        // Assert
        assertTrue(attendanceEvent.isApproved());
    }

    @Test
    @DisplayName("Should handle unapproved status")
    public void testUnapprovedStatus() {
        // Arrange
        attendanceEvent.setApproved(false);
        
        // Assert
        assertFalse(attendanceEvent.isApproved());
    }

    @Test
    @DisplayName("Should default to unapproved")
    public void testDefaultApprovalStatus() {
        // Arrange
        AttendanceEvent newEvent = new AttendanceEvent();
        
        // Assert
        assertFalse(newEvent.isApproved(), "New attendance event should default to unapproved");
    }

    // ========== EDGE CASES - NULL VALUES ==========

    @Test
    @DisplayName("Should reject null employee")
    public void testNullEmployee() {
        // Arrange
        attendanceEvent.setEmployee(null);
        
        // Act
        Set<ConstraintViolation<AttendanceEvent>> violations = validator.validate(attendanceEvent);
        
        // Assert
        assertFalse(violations.isEmpty(), "Null employee should cause validation error");
    }

    @Test
    @DisplayName("Should reject null attendance type")
    public void testNullAttendanceType() {
        // Arrange
        attendanceEvent.setType(null);
        
        // Act
        Set<ConstraintViolation<AttendanceEvent>> violations = validator.validate(attendanceEvent);
        
        // Assert
        assertFalse(violations.isEmpty(), "Null attendance type should cause validation error");
    }

    @Test
    @DisplayName("Should reject null timestamp")
    public void testNullTimestamp() {
        // Arrange
        attendanceEvent.setTimestamp(null);
        
        // Act
        Set<ConstraintViolation<AttendanceEvent>> violations = validator.validate(attendanceEvent);
        
        // Assert
        assertFalse(violations.isEmpty(), "Null timestamp should cause validation error");
    }

    @Test
    @DisplayName("Should allow null device ID for manual entries")
    public void testNullDeviceId() {
        // Arrange
        attendanceEvent.setDeviceId(null);
        
        // Act
        Set<ConstraintViolation<AttendanceEvent>> violations = validator.validate(attendanceEvent);
        
        // Assert
        assertTrue(violations.isEmpty(), "Null device ID should be allowed for manual entries");
    }

    @Test
    @DisplayName("Should allow null location for manual entries")
    public void testNullLocation() {
        // Arrange
        attendanceEvent.setLocation(null);
        
        // Act
        Set<ConstraintViolation<AttendanceEvent>> violations = validator.validate(attendanceEvent);
        
        // Assert
        assertTrue(violations.isEmpty(), "Null location should be allowed for manual entries");
    }

    // ========== EDGE CASES - EMPTY STRINGS ==========

    @Test
    @DisplayName("Should reject empty device ID")
    public void testEmptyDeviceId() {
        // Arrange
        attendanceEvent.setDeviceId("");
        
        // Act
        Set<ConstraintViolation<AttendanceEvent>> violations = validator.validate(attendanceEvent);
        
        // Assert
        assertFalse(violations.isEmpty(), "Empty device ID should cause validation error");
    }

    @Test
    @DisplayName("Should reject empty location")
    public void testEmptyLocation() {
        // Arrange
        attendanceEvent.setLocation("");
        
        // Act
        Set<ConstraintViolation<AttendanceEvent>> violations = validator.validate(attendanceEvent);
        
        // Assert
        assertFalse(violations.isEmpty(), "Empty location should cause validation error");
    }

    @Test
    @DisplayName("Should reject whitespace-only device ID")
    public void testWhitespaceOnlyDeviceId() {
        // Arrange
        attendanceEvent.setDeviceId("   ");
        
        // Act
        Set<ConstraintViolation<AttendanceEvent>> violations = validator.validate(attendanceEvent);
        
        // Assert
        assertFalse(violations.isEmpty(), "Whitespace-only device ID should cause validation error");
    }

    // ========== BOUNDARY CONDITIONS ==========

    @Test
    @DisplayName("Should accept minimum valid device ID length")
    public void testMinimumDeviceIdLength() {
        // Arrange
        attendanceEvent.setDeviceId("D1");
        
        // Act
        Set<ConstraintViolation<AttendanceEvent>> violations = validator.validate(attendanceEvent);
        
        // Assert
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("Should accept maximum valid device ID length")
    public void testMaximumDeviceIdLength() {
        // Arrange
        String longDeviceId = "D".repeat(50);
        attendanceEvent.setDeviceId(longDeviceId);
        
        // Act
        Set<ConstraintViolation<AttendanceEvent>> violations = validator.validate(attendanceEvent);
        
        // Assert
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("Should accept minimum valid location length")
    public void testMinimumLocationLength() {
        // Arrange
        attendanceEvent.setLocation("A");
        
        // Act
        Set<ConstraintViolation<AttendanceEvent>> violations = validator.validate(attendanceEvent);
        
        // Assert
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("Should accept maximum valid location length")
    public void testMaximumLocationLength() {
        // Arrange
        String longLocation = "Location ".repeat(20);
        attendanceEvent.setLocation(longLocation);
        
        // Act
        Set<ConstraintViolation<AttendanceEvent>> violations = validator.validate(attendanceEvent);
        
        // Assert
        assertTrue(violations.isEmpty());
    }

    // ========== CLOCK-IN/OUT SEQUENCE TESTS ==========

    @Test
    @DisplayName("Should create valid clock-in followed by clock-out sequence")
    public void testClockInOutSequence() {
        // Arrange
        AttendanceEvent clockIn = new AttendanceEvent();
        clockIn.setEmployee(testEmployee);
        clockIn.setType(AttendanceType.CLOCK_IN);
        clockIn.setTimestamp(LocalDateTime.now().withHour(8).withMinute(0));
        clockIn.setDeviceId("DEVICE001");
        clockIn.setLocation("Warehouse Entrance");
        clockIn.setApproved(true);
        
        AttendanceEvent clockOut = new AttendanceEvent();
        clockOut.setEmployee(testEmployee);
        clockOut.setType(AttendanceType.CLOCK_OUT);
        clockOut.setTimestamp(LocalDateTime.now().withHour(17).withMinute(0));
        clockOut.setDeviceId("DEVICE001");
        clockOut.setLocation("Warehouse Exit");
        clockOut.setApproved(true);
        
        // Act
        Set<ConstraintViolation<AttendanceEvent>> clockInViolations = validator.validate(clockIn);
        Set<ConstraintViolation<AttendanceEvent>> clockOutViolations = validator.validate(clockOut);
        
        // Assert
        assertTrue(clockInViolations.isEmpty());
        assertTrue(clockOutViolations.isEmpty());
        assertTrue(clockOut.getTimestamp().isAfter(clockIn.getTimestamp()));
    }

    // ========== GEOFENCE VALIDATION ==========

    @Test
    @DisplayName("Should accept GPS coordinates in location")
    public void testGPSCoordinatesInLocation() {
        // Arrange
        attendanceEvent.setLocation("40.7128,-74.0060");
        
        // Act
        Set<ConstraintViolation<AttendanceEvent>> violations = validator.validate(attendanceEvent);
        
        // Assert
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("Should accept location with building and floor info")
    public void testDetailedLocation() {
        // Arrange
        attendanceEvent.setLocation("Building A, Floor 2, Section 3");
        
        // Act
        Set<ConstraintViolation<AttendanceEvent>> violations = validator.validate(attendanceEvent);
        
        // Assert
        assertTrue(violations.isEmpty());
    }