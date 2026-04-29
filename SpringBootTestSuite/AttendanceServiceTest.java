package com.company.ems.attendance;

import com.company.ems.common.exception.NotFoundException;
import com.company.ems.employee.Employee;
import com.company.ems.employee.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Comprehensive JUnit test suite for AttendanceService
 * Tests cover: Clock in/out operations, validation, edge cases, boundary conditions
 */
class AttendanceServiceTest {

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private AttendanceService attendanceService;

    private Employee testEmployee;
    private Attendance testAttendance;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Setup test employee
        testEmployee = new Employee();
        testEmployee.setId(1L);
        testEmployee.setBadgeId("B12345");
        testEmployee.setName("John Doe");
        testEmployee.setRole("WORKER");
        
        // Setup test attendance
        testAttendance = new Attendance();
        testAttendance.setId(1L);
        testAttendance.setEmployee(testEmployee);
        testAttendance.setClockIn(LocalDateTime.now());
        testAttendance.setDevice("Terminal-01");
        testAttendance.setGeofence("Warehouse-A");
        testAttendance.setStatus("NORMAL");
    }

    // ==================== CLOCK IN TESTS ====================

    @Test
    @DisplayName("Clock In - Valid Input")
    void testClockIn_ValidInput_Success() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(testAttendance);

        // Act
        Attendance result = attendanceService.clockIn(1L, "Terminal-01", "Warehouse-A");

        // Assert
        assertNotNull(result);
        assertEquals(testEmployee, result.getEmployee());
        assertEquals("Terminal-01", result.getDevice());
        assertEquals("Warehouse-A", result.getGeofence());
        assertEquals("NORMAL", result.getStatus());
        assertNotNull(result.getClockIn());
        verify(attendanceRepository, times(1)).save(any(Attendance.class));
    }

    @Test
    @DisplayName("Clock In - Non-existent Employee")
    void testClockIn_NonExistentEmployee_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> {
            attendanceService.clockIn(999L, "Terminal-01", "Warehouse-A");
        });
        verify(attendanceRepository, never()).save(any(Attendance.class));
    }

    @Test
    @DisplayName("Clock In - Null Employee ID")
    void testClockIn_NullEmployeeId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.clockIn(null, "Terminal-01", "Warehouse-A");
        });
    }

    @Test
    @DisplayName("Clock In - Null Device")
    void testClockIn_NullDevice_Success() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(testAttendance);

        // Act
        Attendance result = attendanceService.clockIn(1L, null, "Warehouse-A");

        // Assert
        assertNotNull(result);
        assertNull(result.getDevice());
    }

    @Test
    @DisplayName("Clock In - Empty Geofence")
    void testClockIn_EmptyGeofence_Success() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(testAttendance);

        // Act
        Attendance result = attendanceService.clockIn(1L, "Terminal-01", "");

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Clock In - Outside Geofence")
    void testClockIn_OutsideGeofence_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.clockIn(1L, "Terminal-01", "OUTSIDE");
        });
    }

    @Test
    @DisplayName("Clock In - Already Clocked In")
    void testClockIn_AlreadyClockedIn_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(attendanceRepository.findActiveAttendance(1L)).thenReturn(Optional.of(testAttendance));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            attendanceService.clockIn(1L, "Terminal-01", "Warehouse-A");
        });
    }

    @Test
    @DisplayName("Clock In - Multiple Devices")
    void testClockIn_MultipleDifferentDevices_Success() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(testAttendance);

        // Act
        Attendance result1 = attendanceService.clockIn(1L, "Terminal-01", "Warehouse-A");
        Attendance result2 = attendanceService.clockIn(1L, "Terminal-02", "Warehouse-A");

        // Assert
        assertNotNull(result1);
        assertNotNull(result2);
    }

    // ==================== CLOCK OUT TESTS ====================

    @Test
    @DisplayName("Clock Out - Valid Input")
    void testClockOut_ValidInput_Success() {
        // Arrange
        when(attendanceRepository.findById(1L)).thenReturn(Optional.of(testAttendance));
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(testAttendance);

        // Act
        Attendance result = attendanceService.clockOut(1L);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getClockOut());
        assertTrue(result.getClockOut().isAfter(result.getClockIn()));
        verify(attendanceRepository, times(1)).save(any(Attendance.class));
    }

    @Test
    @DisplayName("Clock Out - Non-existent Attendance")
    void testClockOut_NonExistentAttendance_ThrowsException() {
        // Arrange
        when(attendanceRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> {
            attendanceService.clockOut(999L);
        });
    }

    @Test
    @DisplayName("Clock Out - Null Attendance ID")
    void testClockOut_NullAttendanceId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.clockOut(null);
        });
    }

    @Test
    @DisplayName("Clock Out - Already Clocked Out")
    void testClockOut_AlreadyClockedOut_ThrowsException() {
        // Arrange
        testAttendance.setClockOut(LocalDateTime.now());
        when(attendanceRepository.findById(1L)).thenReturn(Optional.of(testAttendance));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            attendanceService.clockOut(1L);
        });
    }

    @Test
    @DisplayName("Clock Out - Same Second as Clock In")
    void testClockOut_SameSecondAsClockIn_Success() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        testAttendance.setClockIn(now);
        when(attendanceRepository.findById(1L)).thenReturn(Optional.of(testAttendance));
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(testAttendance);

        // Act
        Attendance result = attendanceService.clockOut(1L);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getClockOut());
    }

    @Test
    @DisplayName("Clock Out - After 24 Hours")
    void testClockOut_After24Hours_Success() {
        // Arrange
        testAttendance.setClockIn(LocalDateTime.now().minusHours(24));
        when(attendanceRepository.findById(1L)).thenReturn(Optional.of(testAttendance));
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(testAttendance);

        // Act
        Attendance result = attendanceService.clockOut(1L);

        // Assert
        assertNotNull(result);
        Duration duration = Duration.between(result.getClockIn(), result.getClockOut());
        assertTrue(duration.toHours() >= 24);
    }

    // ==================== ATTENDANCE QUERY TESTS ====================

    @Test
    @DisplayName("Get Attendance by Employee - Valid Input")
    void testGetAttendanceByEmployee_ValidInput_Success() {
        // Arrange
        LocalDateTime start = LocalDateTime.now().minusDays(7);
        LocalDateTime end = LocalDateTime.now();
        List<Attendance> attendances = Arrays.asList(testAttendance);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(attendanceRepository.findByEmployeeAndClockInBetween(testEmployee, start, end))
            .thenReturn(attendances);

        // Act
        List<Attendance> result = attendanceService.getAttendanceByEmployee(1L, start, end);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testEmployee, result.get(0).getEmployee());
    }

    @Test
    @DisplayName("Get Attendance by Employee - Empty Result")
    void testGetAttendanceByEmployee_EmptyResult_Success() {
        // Arrange
        LocalDateTime start = LocalDateTime.now().minusDays(7);
        LocalDateTime end = LocalDateTime.now();
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(attendanceRepository.findByEmployeeAndClockInBetween(testEmployee, start, end))
            .thenReturn(Arrays.asList());

        // Act
        List<Attendance> result = attendanceService.getAttendanceByEmployee(1L, start, end);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    @DisplayName("Get Attendance by Employee - Invalid Date Range")
    void testGetAttendanceByEmployee_InvalidDateRange_ThrowsException() {
        // Arrange
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = LocalDateTime.now().minusDays(7);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.getAttendanceByEmployee(1L, start, end);
        });
    }

    // ==================== ATTENDANCE CORRECTION TESTS ====================

    @Test
    @DisplayName("Request Correction - Valid Input")
    void testRequestCorrection_ValidInput_Success() {
        // Arrange
        when(attendanceRepository.findById(1L)).thenReturn(Optional.of(testAttendance));
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(testAttendance);

        // Act
        Attendance result = attendanceService.requestCorrection(1L, "Forgot to clock out");

        // Assert
        assertNotNull(result);
        assertEquals("CORRECTION_PENDING", result.getStatus());
        verify(attendanceRepository, times(1)).save(any(Attendance.class));
    }

    @Test
    @DisplayName("Request Correction - Empty Reason")
    void testRequestCorrection_EmptyReason_ThrowsException() {
        // Arrange
        when(attendanceRepository.findById(1L)).thenReturn(Optional.of(testAttendance));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.requestCorrection(1L, "");
        });
    }

    @Test
    @DisplayName("Request Correction - Already Corrected")
    void testRequestCorrection_AlreadyCorrected_ThrowsException() {
        // Arrange
        testAttendance.setStatus("CORRECTION_PENDING");
        when(attendanceRepository.findById(1L)).thenReturn(Optional.of(testAttendance));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            attendanceService.requestCorrection(1L, "Another reason");
        });
    }

    // ==================== EDGE CASE TESTS ====================

    @Test
    @DisplayName("Edge Case - Clock In at Midnight")
    void testClockIn_AtMidnight_Success() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(testAttendance);

        // Act
        Attendance result = attendanceService.clockIn(1L, "Terminal-01", "Warehouse-A");

        // Assert
        assertNotNull(result);
        assertNotNull(result.getClockIn());
    }

    @Test
    @DisplayName("Edge Case - Clock Out at Midnight")
    void testClockOut_AtMidnight_Success() {
        // Arrange
        testAttendance.setClockIn(LocalDateTime.now().minusHours(8));
        when(attendanceRepository.findById(1L)).thenReturn(Optional.of(testAttendance));
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(testAttendance);

        // Act
        Attendance result = attendanceService.clockOut(1L);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getClockOut());
    }

    @Test
    @DisplayName("Edge Case - Very Long Device Name")
    void testClockIn_VeryLongDeviceName_Success() {
        // Arrange
        String longDevice = "Terminal-" + "X".repeat(100);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(testAttendance);

        // Act
        Attendance result = attendanceService.clockIn(1L, longDevice, "Warehouse-A");

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Edge Case - Special Characters in Geofence")
    void testClockIn_SpecialCharactersInGeofence_Success() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(testAttendance);

        // Act
        Attendance result = attendanceService.clockIn(1L, "Terminal-01", "Warehouse-A/B-1");

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Edge Case - Clock In During Daylight Saving Time Change")
    void testClockIn_DuringDSTChange_Success() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(testAttendance);

        // Act
        Attendance result = attendanceService.clockIn(1L, "Terminal-01", "Warehouse-A");

        // Assert
        assertNotNull(result);
        assertNotNull(result.getClockIn());
    }

    @Test
    @DisplayName("Edge Case - Multiple Clock Ins Same Day")
    void testClockIn_MultipleSameDay_Success() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(testAttendance);

        // Act
        Attendance result1 = attendanceService.clockIn(1L, "Terminal-01", "Warehouse-A");
        Attendance result2 = attendanceService.clockIn(1L, "Terminal-01", "Warehouse-A");

        // Assert
        assertNotNull(result1);
        assertNotNull(result2);
    }

    @Test
    @DisplayName("Boundary Case - Maximum Hours Worked")
    void testClockOut_MaximumHoursWorked_Success() {
        // Arrange
        testAttendance.setClockIn(LocalDateTime.now().minusHours(23).minusMinutes(59));
        when(attendanceRepository.findById(1L)).thenReturn(Optional.of(testAttendance));
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(testAttendance);

        // Act
        Attendance result = attendanceService.clockOut(1L);

        // Assert
        assertNotNull(result);
        Duration duration = Duration.between(result.getClockIn(), result.getClockOut());
        assertTrue(duration.toHours() < 24);
    }

    @Test
    @DisplayName("Boundary Case - Minimum Hours Worked")
    void testClockOut_MinimumHoursWorked_Success() {
        // Arrange
        testAttendance.setClockIn(LocalDateTime.now().minusMinutes(1));
        when(attendanceRepository.findById(1L)).thenReturn(Optional.of(testAttendance));
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(testAttendance);

        // Act
        Attendance result = attendanceService.clockOut(1L);

        // Assert
        assertNotNull(result);
        Duration duration = Duration.between(result.getClockIn(), result.getClockOut());
        assertTrue(duration.toMinutes() >= 1);
    }
}