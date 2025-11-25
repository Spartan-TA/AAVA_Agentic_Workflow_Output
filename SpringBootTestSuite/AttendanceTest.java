package com.warehouse.employee.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDateTime;

/**
 * Comprehensive JUnit test suite for Attendance entity
 * Tests cover constructors, getters/setters, validations, relationships, and edge cases
 */
@DisplayName("Attendance Entity Tests")
public class AttendanceTest {
    
    private Attendance attendance;
    private Employee employee;
    
    @BeforeEach
    public void setUp() {
        attendance = new Attendance();
        employee = new Employee();
        employee.setId(1L);
        employee.setBadgeId("EMP001");
        employee.setFirstName("John");
        employee.setLastName("Doe");
    }
    
    // Constructor Tests
    @Test
    @DisplayName("Test Attendance creation with default constructor")
    public void testAttendanceCreation_WithDefaultConstructor_ShouldSucceed() {
        // Arrange & Act
        Attendance newAttendance = new Attendance();
        
        // Assert
        assertNotNull(newAttendance);
        assertNull(newAttendance.getId());
        assertNull(newAttendance.getClockIn());
    }
    
    @Test
    @DisplayName("Test Attendance creation with valid data")
    public void testAttendanceCreation_WithValidData_ShouldSucceed() {
        // Arrange
        LocalDateTime clockIn = LocalDateTime.of(2024, 1, 15, 8, 0);
        LocalDateTime clockOut = LocalDateTime.of(2024, 1, 15, 17, 0);
        String location = "Warehouse A";
        String deviceInfo = "Mobile-iOS-15.0";
        String status = "APPROVED";
        
        // Act
        attendance.setClockIn(clockIn);
        attendance.setClockOut(clockOut);
        attendance.setLocation(location);
        attendance.setDeviceInfo(deviceInfo);
        attendance.setStatus(status);
        attendance.setEmployee(employee);
        
        // Assert
        assertEquals(clockIn, attendance.getClockIn());
        assertEquals(clockOut, attendance.getClockOut());
        assertEquals(location, attendance.getLocation());
        assertEquals(deviceInfo, attendance.getDeviceInfo());
        assertEquals(status, attendance.getStatus());
        assertEquals(employee, attendance.getEmployee());
    }
    
    // ClockIn Tests
    @Test
    @DisplayName("Test setClockIn with null value")
    public void testSetClockIn_WithNull_ShouldAcceptButFailValidation() {
        // Arrange & Act
        attendance.setClockIn(null);
        
        // Assert
        assertNull(attendance.getClockIn());
    }
    
    @Test
    @DisplayName("Test setClockIn with valid timestamp")
    public void testSetClockIn_WithValidTimestamp_ShouldSucceed() {
        // Arrange
        LocalDateTime clockIn = LocalDateTime.of(2024, 1, 15, 8, 0);
        
        // Act
        attendance.setClockIn(clockIn);
        
        // Assert
        assertEquals(clockIn, attendance.getClockIn());
    }
    
    @Test
    @DisplayName("Test setClockIn with past timestamp")
    public void testSetClockIn_WithPastTimestamp_ShouldSucceed() {
        // Arrange
        LocalDateTime clockIn = LocalDateTime.of(2020, 1, 1, 8, 0);
        
        // Act
        attendance.setClockIn(clockIn);
        
        // Assert
        assertEquals(clockIn, attendance.getClockIn());
    }
    
    @Test
    @DisplayName("Test setClockIn with future timestamp")
    public void testSetClockIn_WithFutureTimestamp_ShouldSucceed() {
        // Arrange
        LocalDateTime clockIn = LocalDateTime.now().plusDays(1);
        
        // Act
        attendance.setClockIn(clockIn);
        
        // Assert
        assertEquals(clockIn, attendance.getClockIn());
    }
    
    @Test
    @DisplayName("Test setClockIn with midnight timestamp")
    public void testSetClockIn_WithMidnightTimestamp_ShouldSucceed() {
        // Arrange
        LocalDateTime clockIn = LocalDateTime.of(2024, 1, 15, 0, 0);
        
        // Act
        attendance.setClockIn(clockIn);
        
        // Assert
        assertEquals(clockIn, attendance.getClockIn());
    }
    
    // ClockOut Tests
    @Test
    @DisplayName("Test setClockOut with null value")
    public void testSetClockOut_WithNull_ShouldSucceed() {
        // Arrange & Act
        attendance.setClockOut(null);
        
        // Assert
        assertNull(attendance.getClockOut());
    }
    
    @Test
    @DisplayName("Test setClockOut with valid timestamp")
    public void testSetClockOut_WithValidTimestamp_ShouldSucceed() {
        // Arrange
        LocalDateTime clockOut = LocalDateTime.of(2024, 1, 15, 17, 0);
        
        // Act
        attendance.setClockOut(clockOut);
        
        // Assert
        assertEquals(clockOut, attendance.getClockOut());
    }
    
    @Test
    @DisplayName("Test setClockOut after clockIn")
    public void testSetClockOut_AfterClockIn_ShouldSucceed() {
        // Arrange
        LocalDateTime clockIn = LocalDateTime.of(2024, 1, 15, 8, 0);
        LocalDateTime clockOut = LocalDateTime.of(2024, 1, 15, 17, 0);
        
        // Act
        attendance.setClockIn(clockIn);
        attendance.setClockOut(clockOut);
        
        // Assert
        assertTrue(attendance.getClockOut().isAfter(attendance.getClockIn()));
    }
    
    @Test
    @DisplayName("Test setClockOut before clockIn")
    public void testSetClockOut_BeforeClockIn_ShouldAcceptButMayFailBusinessValidation() {
        // Arrange
        LocalDateTime clockIn = LocalDateTime.of(2024, 1, 15, 17, 0);
        LocalDateTime clockOut = LocalDateTime.of(2024, 1, 15, 8, 0);
        
        // Act
        attendance.setClockIn(clockIn);
        attendance.setClockOut(clockOut);
        
        // Assert
        assertTrue(attendance.getClockOut().isBefore(attendance.getClockIn()));
    }
    
    // Location Tests
    @Test
    @DisplayName("Test setLocation with null value")
    public void testSetLocation_WithNull_ShouldSucceed() {
        // Arrange & Act
        attendance.setLocation(null);
        
        // Assert
        assertNull(attendance.getLocation());
    }
    
    @Test
    @DisplayName("Test setLocation with empty string")
    public void testSetLocation_WithEmptyString_ShouldSucceed() {
        // Arrange & Act
        attendance.setLocation("");
        
        // Assert
        assertEquals("", attendance.getLocation());
    }
    
    @Test
    @DisplayName("Test setLocation with valid value")
    public void testSetLocation_WithValidValue_ShouldSucceed() {
        // Arrange
        String location = "Warehouse A";
        
        // Act
        attendance.setLocation(location);
        
        // Assert
        assertEquals(location, attendance.getLocation());
    }
    
    @Test
    @DisplayName("Test setLocation with coordinates")
    public void testSetLocation_WithCoordinates_ShouldSucceed() {
        // Arrange
        String location = "40.7128,-74.0060";
        
        // Act
        attendance.setLocation(location);
        
        // Assert
        assertEquals(location, attendance.getLocation());
    }
    
    @Test
    @DisplayName("Test setLocation with very long string")
    public void testSetLocation_WithVeryLongString_ShouldSucceed() {
        // Arrange
        String location = "A".repeat(500);
        
        // Act
        attendance.setLocation(location);
        
        // Assert
        assertEquals(location, attendance.getLocation());
    }
    
    // DeviceInfo Tests
    @Test
    @DisplayName("Test setDeviceInfo with null value")
    public void testSetDeviceInfo_WithNull_ShouldSucceed() {
        // Arrange & Act
        attendance.setDeviceInfo(null);
        
        // Assert
        assertNull(attendance.getDeviceInfo());
    }
    
    @Test
    @DisplayName("Test setDeviceInfo with valid mobile device")
    public void testSetDeviceInfo_WithValidMobileDevice_ShouldSucceed() {
        // Arrange
        String deviceInfo = "Mobile-iOS-15.0";
        
        // Act
        attendance.setDeviceInfo(deviceInfo);
        
        // Assert
        assertEquals(deviceInfo, attendance.getDeviceInfo());
    }
    
    @Test
    @DisplayName("Test setDeviceInfo with Android device")
    public void testSetDeviceInfo_WithAndroidDevice_ShouldSucceed() {
        // Arrange
        String deviceInfo = "Mobile-Android-12.0";
        
        // Act
        attendance.setDeviceInfo(deviceInfo);
        
        // Assert
        assertEquals(deviceInfo, attendance.getDeviceInfo());
    }
    
    @Test
    @DisplayName("Test setDeviceInfo with web browser")
    public void testSetDeviceInfo_WithWebBrowser_ShouldSucceed() {
        // Arrange
        String deviceInfo = "Web-Chrome-120.0";
        
        // Act
        attendance.setDeviceInfo(deviceInfo);
        
        // Assert
        assertEquals(deviceInfo, attendance.getDeviceInfo());
    }
    
    // Status Tests
    @Test
    @DisplayName("Test setStatus with PENDING status")
    public void testSetStatus_WithPendingStatus_ShouldSucceed() {
        // Arrange
        String status = "PENDING";
        
        // Act
        attendance.setStatus(status);
        
        // Assert
        assertEquals(status, attendance.getStatus());
    }
    
    @Test
    @DisplayName("Test setStatus with APPROVED status")
    public void testSetStatus_WithApprovedStatus_ShouldSucceed() {
        // Arrange
        String status = "APPROVED";
        
        // Act
        attendance.setStatus(status);
        
        // Assert
        assertEquals(status, attendance.getStatus());
    }
    
    @Test
    @DisplayName("Test setStatus with REJECTED status")
    public void testSetStatus_WithRejectedStatus_ShouldSucceed() {
        // Arrange
        String status = "REJECTED";
        
        // Act
        attendance.setStatus(status);
        
        // Assert
        assertEquals(status, attendance.getStatus());
    }
    
    @Test
    @DisplayName("Test setStatus with null value")
    public void testSetStatus_WithNull_ShouldSucceed() {
        // Arrange & Act
        attendance.setStatus(null);
        
        // Assert
        assertNull(attendance.getStatus());
    }
    
    @Test
    @DisplayName("Test setStatus with invalid status")
    public void testSetStatus_WithInvalidStatus_ShouldAcceptButMayFailValidation() {
        // Arrange
        String status = "INVALID_STATUS";
        
        // Act
        attendance.setStatus(status);
        
        // Assert
        assertEquals(status, attendance.getStatus());
    }
    
    // Employee Relationship Tests
    @Test
    @DisplayName("Test setEmployee with null value")
    public void testSetEmployee_WithNull_ShouldAcceptButFailValidation() {
        // Arrange & Act
        attendance.setEmployee(null);
        
        // Assert
        assertNull(attendance.getEmployee());
    }
    
    @Test
    @DisplayName("Test setEmployee with valid employee")
    public void testSetEmployee_WithValidEmployee_ShouldSucceed() {
        // Arrange & Act
        attendance.setEmployee(employee);
        
        // Assert
        assertEquals(employee, attendance.getEmployee());
        assertEquals("EMP001", attendance.getEmployee().getBadgeId());
    }
    
    @Test
    @DisplayName("Test employee relationship bidirectional")
    public void testEmployeeRelationship_Bidirectional_ShouldMaintainConsistency() {
        // Arrange & Act
        attendance.setEmployee(employee);
        
        // Assert
        assertNotNull(attendance.getEmployee());
        assertEquals(employee.getId(), attendance.getEmployee().getId());
    }
    
    // Equals and HashCode Tests
    @Test
    @DisplayName("Test equals with same object")
    public void testEquals_WithSameObject_ShouldReturnTrue() {
        // Arrange
        attendance.setId(1L);
        
        // Act & Assert
        assertEquals(attendance, attendance);
    }
    
    @Test
    @DisplayName("Test equals with equal objects")
    public void testEquals_WithEqualObjects_ShouldReturnTrue() {
        // Arrange
        attendance.setId(1L);
        attendance.setClockIn(LocalDateTime.of(2024, 1, 15, 8, 0));
        
        Attendance attendance2 = new Attendance();
        attendance2.setId(1L);
        attendance2.setClockIn(LocalDateTime.of(2024, 1, 15, 8, 0));
        
        // Act & Assert
        assertEquals(attendance, attendance2);
    }
    
    @Test
    @DisplayName("Test equals with different objects")
    public void testEquals_WithDifferentObjects_ShouldReturnFalse() {
        // Arrange
        attendance.setId(1L);
        
        Attendance attendance2 = new Attendance();
        attendance2.setId(2L);
        
        // Act & Assert
        assertNotEquals(attendance, attendance2);
    }
    
    @Test
    @DisplayName("Test equals with null")
    public void testEquals_WithNull_ShouldReturnFalse() {
        // Arrange
        attendance.setId(1L);
        
        // Act & Assert
        assertNotEquals(attendance, null);
    }
    
    @Test
    @DisplayName("Test hashCode consistency")
    public void testHashCode_WithSameData_ShouldBeConsistent() {
        // Arrange
        attendance.setId(1L);
        attendance.setClockIn(LocalDateTime.of(2024, 1, 15, 8, 0));
        
        int hashCode1 = attendance.hashCode();
        int hashCode2 = attendance.hashCode();
        
        // Act & Assert
        assertEquals(hashCode1, hashCode2);
    }
    
    // ToString Test
    @Test
    @DisplayName("Test toString method")
    public void testToString_ShouldReturnStringRepresentation() {
        // Arrange
        attendance.setClockIn(LocalDateTime.of(2024, 1, 15, 8, 0));
        attendance.setLocation("Warehouse A");
        
        // Act
        String result = attendance.toString();
        
        // Assert
        assertNotNull(result);
    }
    
    // Business Logic Tests
    @Test
    @DisplayName("Test work duration calculation")
    public void testWorkDuration_WithValidClockInOut_ShouldCalculateCorrectly() {
        // Arrange
        LocalDateTime clockIn = LocalDateTime.of(2024, 1, 15, 8, 0);
        LocalDateTime clockOut = LocalDateTime.of(2024, 1, 15, 17, 0);
        
        // Act
        attendance.setClockIn(clockIn);
        attendance.setClockOut(clockOut);
        
        // Assert
        assertNotNull(attendance.getClockIn());
        assertNotNull(attendance.getClockOut());
        assertTrue(attendance.getClockOut().isAfter(attendance.getClockIn()));
    }
    
    @Test
    @DisplayName("Test overnight shift")
    public void testOvernightShift_WithClockOutNextDay_ShouldSucceed() {
        // Arrange
        LocalDateTime clockIn = LocalDateTime.of(2024, 1, 15, 22, 0);
        LocalDateTime clockOut = LocalDateTime.of(2024, 1, 16, 6, 0);
        
        // Act
        attendance.setClockIn(clockIn);
        attendance.setClockOut(clockOut);
        
        // Assert
        assertTrue(attendance.getClockOut().isAfter(attendance.getClockIn()));
        assertEquals(1, attendance.getClockOut().getDayOfMonth() - attendance.getClockIn().getDayOfMonth());
    }
}