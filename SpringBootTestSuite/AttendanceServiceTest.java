package com.company.wems.attendance.service;

import com.company.wems.attendance.dto.AttendanceEventDTO;
import com.company.wems.attendance.entity.AttendanceEvent;
import com.company.wems.attendance.repository.AttendanceEventRepository;
import com.company.wems.employee.entity.Employee;
import com.company.wems.employee.repository.EmployeeRepository;
import com.company.wems.common.exception.ResourceNotFoundException;
import com.company.wems.common.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for AttendanceService
 * Tests cover clock-in/out operations, validation, and edge cases
 */
@DisplayName("Attendance Service Tests")
public class AttendanceServiceTest {

    @Mock
    private AttendanceEventRepository attendanceEventRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private AttendanceService attendanceService;

    private Employee validEmployee;
    private AttendanceEvent validAttendanceEvent;
    private AttendanceEventDTO validAttendanceEventDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Setup valid employee
        validEmployee = new Employee();
        validEmployee.setId(1L);
        validEmployee.setBadgeId("EMP001");
        validEmployee.setFirstName("John");
        validEmployee.setLastName("Doe");
        validEmployee.setDeleted(false);
        
        // Setup valid attendance event
        validAttendanceEvent = new AttendanceEvent();
        validAttendanceEvent.setId(1L);
        validAttendanceEvent.setEmployee(validEmployee);
        validAttendanceEvent.setTimestamp(LocalDateTime.now());
        validAttendanceEvent.setType(AttendanceEvent.EventType.CLOCK_IN);
        validAttendanceEvent.setDevice("Terminal-01");
        validAttendanceEvent.setLocation("Warehouse-A");
        
        // Setup valid DTO
        validAttendanceEventDTO = new AttendanceEventDTO();
        validAttendanceEventDTO.setEmployeeId(1L);
        validAttendanceEventDTO.setType("CLOCK_IN");
        validAttendanceEventDTO.setDevice("Terminal-01");
        validAttendanceEventDTO.setLocation("Warehouse-A");
    }

    // ==================== CLOCK IN TESTS ====================

    @Test
    @DisplayName("Clock In - Valid Employee - Should Create Clock In Event")
    void testClockIn_WithValidEmployee_ShouldCreateEvent() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        when(attendanceEventRepository.findLatestEventByEmployee(1L)).thenReturn(Optional.empty());
        when(attendanceEventRepository.save(any(AttendanceEvent.class))).thenReturn(validAttendanceEvent);

        // Act
        AttendanceEventDTO result = attendanceService.clockIn(1L, "Terminal-01", "Warehouse-A");

        // Assert
        assertNotNull(result);
        verify(attendanceEventRepository, times(1)).save(any(AttendanceEvent.class));
    }

    @Test
    @DisplayName("Clock In - Non-Existent Employee - Should Throw ResourceNotFoundException")
    void testClockIn_WithNonExistentEmployee_ShouldThrowException() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            attendanceService.clockIn(999L, "Terminal-01", "Warehouse-A");
        });
        verify(attendanceEventRepository, never()).save(any(AttendanceEvent.class));
    }

    @Test
    @DisplayName("Clock In - Already Clocked In - Should Throw BusinessException")
    void testClockIn_WhenAlreadyClockedIn_ShouldThrowException() {
        // Arrange
        AttendanceEvent lastEvent = new AttendanceEvent();
        lastEvent.setType(AttendanceEvent.EventType.CLOCK_IN);
        lastEvent.setClockOutTime(null);
        
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        when(attendanceEventRepository.findLatestEventByEmployee(1L)).thenReturn(Optional.of(lastEvent));

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            attendanceService.clockIn(1L, "Terminal-01", "Warehouse-A");
        });
    }

    @Test
    @DisplayName("Clock In - Null Employee ID - Should Throw Exception")
    void testClockIn_WithNullEmployeeId_ShouldThrowException() {
        // Act & Assert
        assertThrows(Exception.class, () -> {
            attendanceService.clockIn(null, "Terminal-01", "Warehouse-A");
        });
    }

    @Test
    @DisplayName("Clock In - Null Device - Should Create Event with Null Device")
    void testClockIn_WithNullDevice_ShouldCreateEvent() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        when(attendanceEventRepository.findLatestEventByEmployee(1L)).thenReturn(Optional.empty());
        when(attendanceEventRepository.save(any(AttendanceEvent.class))).thenReturn(validAttendanceEvent);

        // Act
        AttendanceEventDTO result = attendanceService.clockIn(1L, null, "Warehouse-A");

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Clock In - Empty Location - Should Create Event")
    void testClockIn_WithEmptyLocation_ShouldCreateEvent() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        when(attendanceEventRepository.findLatestEventByEmployee(1L)).thenReturn(Optional.empty());
        when(attendanceEventRepository.save(any(AttendanceEvent.class))).thenReturn(validAttendanceEvent);

        // Act
        AttendanceEventDTO result = attendanceService.clockIn(1L, "Terminal-01", "");

        // Assert
        assertNotNull(result);
    }

    // ==================== CLOCK OUT TESTS ====================

    @Test
    @DisplayName("Clock Out - Valid Employee with Active Clock In - Should Create Clock Out Event")
    void testClockOut_WithValidEmployee_ShouldCreateEvent() {
        // Arrange
        AttendanceEvent clockInEvent = new AttendanceEvent();
        clockInEvent.setId(1L);
        clockInEvent.setEmployee(validEmployee);
        clockInEvent.setType(AttendanceEvent.EventType.CLOCK_IN);
        clockInEvent.setTimestamp(LocalDateTime.now().minusHours(8));
        clockInEvent.setClockOutTime(null);
        
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        when(attendanceEventRepository.findLatestEventByEmployee(1L)).thenReturn(Optional.of(clockInEvent));
        when(attendanceEventRepository.save(any(AttendanceEvent.class))).thenReturn(clockInEvent);

        // Act
        AttendanceEventDTO result = attendanceService.clockOut(1L, "Terminal-01", "Warehouse-A");

        // Assert
        assertNotNull(result);
        assertNotNull(clockInEvent.getClockOutTime());
        assertNotNull(clockInEvent.getTotalHours());
        verify(attendanceEventRepository, times(1)).save(clockInEvent);
    }

    @Test
    @DisplayName("Clock Out - No Active Clock In - Should Throw BusinessException")
    void testClockOut_WithoutActiveClock In_ShouldThrowException() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        when(attendanceEventRepository.findLatestEventByEmployee(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            attendanceService.clockOut(1L, "Terminal-01", "Warehouse-A");
        });
    }

    @Test
    @DisplayName("Clock Out - Already Clocked Out - Should Throw BusinessException")
    void testClockOut_WhenAlreadyClockedOut_ShouldThrowException() {
        // Arrange
        AttendanceEvent clockedOutEvent = new AttendanceEvent();
        clockedOutEvent.setType(AttendanceEvent.EventType.CLOCK_IN);
        clockedOutEvent.setTimestamp(LocalDateTime.now().minusHours(8));
        clockedOutEvent.setClockOutTime(LocalDateTime.now());
        
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        when(attendanceEventRepository.findLatestEventByEmployee(1L)).thenReturn(Optional.of(clockedOutEvent));

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            attendanceService.clockOut(1L, "Terminal-01", "Warehouse-A");
        });
    }

    @Test
    @DisplayName("Clock Out - Non-Existent Employee - Should Throw ResourceNotFoundException")
    void testClockOut_WithNonExistentEmployee_ShouldThrowException() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            attendanceService.clockOut(999L, "Terminal-01", "Warehouse-A");
        });
    }

    // ==================== CALCULATE HOURS TESTS ====================

    @Test
    @DisplayName("Calculate Hours - 8 Hour Shift - Should Calculate Correctly")
    void testCalculateHours_For8HourShift_ShouldCalculateCorrectly() {
        // Arrange
        AttendanceEvent event = new AttendanceEvent();
        event.setTimestamp(LocalDateTime.of(2024, 1, 1, 9, 0));
        event.setClockOutTime(LocalDateTime.of(2024, 1, 1, 17, 0));

        // Act
        event.calculateTotalHours();

        // Assert
        assertEquals(8.0, event.getTotalHours(), 0.01);
    }

    @Test
    @DisplayName("Calculate Hours - Partial Hour - Should Calculate Correctly")
    void testCalculateHours_ForPartialHour_ShouldCalculateCorrectly() {
        // Arrange
        AttendanceEvent event = new AttendanceEvent();
        event.setTimestamp(LocalDateTime.of(2024, 1, 1, 9, 0));
        event.setClockOutTime(LocalDateTime.of(2024, 1, 1, 9, 30));

        // Act
        event.calculateTotalHours();

        // Assert
        assertEquals(0.5, event.getTotalHours(), 0.01);
    }

    @Test
    @DisplayName("Calculate Hours - Overnight Shift - Should Calculate Correctly")
    void testCalculateHours_ForOvernightShift_ShouldCalculateCorrectly() {
        // Arrange
        AttendanceEvent event = new AttendanceEvent();
        event.setTimestamp(LocalDateTime.of(2024, 1, 1, 22, 0));
        event.setClockOutTime(LocalDateTime.of(2024, 1, 2, 6, 0));

        // Act
        event.calculateTotalHours();

        // Assert
        assertEquals(8.0, event.getTotalHours(), 0.01);
    }

    @Test
    @DisplayName("Calculate Hours - Null Clock Out Time - Should Not Calculate")
    void testCalculateHours_WithNullClockOutTime_ShouldNotCalculate() {
        // Arrange
        AttendanceEvent event = new AttendanceEvent();
        event.setTimestamp(LocalDateTime.now());
        event.setClockOutTime(null);

        // Act
        event.calculateTotalHours();

        // Assert
        assertNull(event.getTotalHours());
    }

    // ==================== GET ATTENDANCE HISTORY TESTS ====================

    @Test
    @DisplayName("Get Attendance History - Valid Employee - Should Return List")
    void testGetAttendanceHistory_WithValidEmployee_ShouldReturnList() {
        // Arrange
        List<AttendanceEvent> events = Arrays.asList(validAttendanceEvent);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        when(attendanceEventRepository.findByEmployeeIdOrderByTimestampDesc(1L)).thenReturn(events);

        // Act
        List<AttendanceEventDTO> result = attendanceService.getAttendanceHistory(1L);

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    @DisplayName("Get Attendance History - Non-Existent Employee - Should Throw Exception")
    void testGetAttendanceHistory_WithNonExistentEmployee_ShouldThrowException() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            attendanceService.getAttendanceHistory(999L);
        });
    }

    @Test
    @DisplayName("Get Attendance History - No Records - Should Return Empty List")
    void testGetAttendanceHistory_WithNoRecords_ShouldReturnEmptyList() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        when(attendanceEventRepository.findByEmployeeIdOrderByTimestampDesc(1L)).thenReturn(Arrays.asList());

        // Act
        List<AttendanceEventDTO> result = attendanceService.getAttendanceHistory(1L);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ==================== BOUNDARY AND EDGE CASE TESTS ====================

    @Test
    @DisplayName("Clock In - Maximum Device Name Length - Should Create Event")
    void testClockIn_WithMaxDeviceNameLength_ShouldCreateEvent() {
        // Arrange
        String maxLengthDevice = "D".repeat(50);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        when(attendanceEventRepository.findLatestEventByEmployee(1L)).thenReturn(Optional.empty());
        when(attendanceEventRepository.save(any(AttendanceEvent.class))).thenReturn(validAttendanceEvent);

        // Act
        AttendanceEventDTO result = attendanceService.clockIn(1L, maxLengthDevice, "Warehouse-A");

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Clock Out - Same Minute as Clock In - Should Calculate Zero Hours")
    void testClockOut_SameMinuteAsClockIn_ShouldCalculateZeroHours() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        AttendanceEvent clockInEvent = new AttendanceEvent();
        clockInEvent.setEmployee(validEmployee);
        clockInEvent.setType(AttendanceEvent.EventType.CLOCK_IN);
        clockInEvent.setTimestamp(now);
        clockInEvent.setClockOutTime(null);
        
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        when(attendanceEventRepository.findLatestEventByEmployee(1L)).thenReturn(Optional.of(clockInEvent));
        when(attendanceEventRepository.save(any(AttendanceEvent.class))).thenReturn(clockInEvent);

        // Act
        attendanceService.clockOut(1L, "Terminal-01", "Warehouse-A");

        // Assert
        assertTrue(clockInEvent.getTotalHours() >= 0);
    }

    @Test
    @DisplayName("Clock In - Deleted Employee - Should Throw Exception")
    void testClockIn_WithDeletedEmployee_ShouldThrowException() {
        // Arrange
        validEmployee.setDeleted(true);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            attendanceService.clockIn(1L, "Terminal-01", "Warehouse-A");
        });
    }
}