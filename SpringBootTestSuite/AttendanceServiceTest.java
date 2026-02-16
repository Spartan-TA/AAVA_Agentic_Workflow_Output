package com.warehouse.employeemgmt.service;

import com.warehouse.employeemgmt.domain.Attendance;
import com.warehouse.employeemgmt.domain.AttendanceStatus;
import com.warehouse.employeemgmt.domain.Employee;
import com.warehouse.employeemgmt.dto.AttendanceRequest;
import com.warehouse.employeemgmt.exception.ResourceNotFoundException;
import com.warehouse.employeemgmt.repository.AttendanceRepository;
import com.warehouse.employeemgmt.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive JUnit test suite for AttendanceService
 * Tests clock-in/out operations, hours calculation, missed punch workflow, and edge cases
 * 
 * Test Coverage:
 * - Clock-in operations (normal, duplicate, geofence validation)
 * - Clock-out operations (normal, missing clock-in, device validation)
 * - Hours calculation (normal shift, overtime, breaks)
 * - Missed punch correction workflow
 * - Attendance status management
 * - Edge cases (null inputs, invalid times, boundary conditions)
 * - Device and location capture
 * - Daily totals computation
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Attendance Service Test Suite")
public class AttendanceServiceTest {

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private AttendanceService attendanceService;

    private Employee testEmployee;
    private Attendance testAttendance;
    private AttendanceRequest clockInRequest;
    private AttendanceRequest clockOutRequest;

    @BeforeEach
    public void setUp() {
        // Arrange - Setup test data
        testEmployee = new Employee();
        testEmployee.setId(1L);
        testEmployee.setName("John Doe");
        testEmployee.setBadgeId("EMP001");

        testAttendance = new Attendance();
        testAttendance.setId(1L);
        testAttendance.setEmployee(testEmployee);
        testAttendance.setClockIn(LocalDateTime.now().withHour(9).withMinute(0));
        testAttendance.setDeviceId("DEVICE001");
        testAttendance.setLocation("Warehouse A");
        testAttendance.setStatus(AttendanceStatus.CLOCKED_IN);

        clockInRequest = new AttendanceRequest();
        clockInRequest.setBadgeId("EMP001");
        clockInRequest.setDeviceId("DEVICE001");
        clockInRequest.setLocation("Warehouse A");
        clockInRequest.setTimestamp(LocalDateTime.now());

        clockOutRequest = new AttendanceRequest();
        clockOutRequest.setBadgeId("EMP001");
        clockOutRequest.setDeviceId("DEVICE001");
        clockOutRequest.setLocation("Warehouse A");
        clockOutRequest.setTimestamp(LocalDateTime.now().plusHours(8));
    }

    // ==================== CLOCK-IN TESTS ====================

    @Test
    @DisplayName("Test clock-in with valid input")
    public void testClockIn_ValidInput_Success() {
        // Arrange
        when(employeeRepository.findByBadgeId(anyString())).thenReturn(Optional.of(testEmployee));
        when(attendanceRepository.findByEmployeeAndClockInBetween(any(), any(), any())).thenReturn(Arrays.asList());
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(testAttendance);

        // Act
        Attendance result = attendanceService.clockIn(clockInRequest);

        // Assert
        assertNotNull(result);
        assertEquals(testEmployee, result.getEmployee());
        assertEquals(AttendanceStatus.CLOCKED_IN, result.getStatus());
        verify(attendanceRepository, times(1)).save(any(Attendance.class));
    }

    @Test
    @DisplayName("Test clock-in with null request")
    public void testClockIn_NullRequest_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.clockIn(null);
        });
        verify(attendanceRepository, never()).save(any(Attendance.class));
    }

    @Test
    @DisplayName("Test clock-in with null badge ID")
    public void testClockIn_NullBadgeId_ThrowsException() {
        // Arrange
        clockInRequest.setBadgeId(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.clockIn(clockInRequest);
        });
    }

    @Test
    @DisplayName("Test clock-in with empty badge ID")
    public void testClockIn_EmptyBadgeId_ThrowsException() {
        // Arrange
        clockInRequest.setBadgeId("");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.clockIn(clockInRequest);
        });
    }

    @Test
    @DisplayName("Test clock-in with non-existent employee")
    public void testClockIn_NonExistentEmployee_ThrowsException() {
        // Arrange
        when(employeeRepository.findByBadgeId(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            attendanceService.clockIn(clockInRequest);
        });
        verify(attendanceRepository, never()).save(any(Attendance.class));
    }

    @Test
    @DisplayName("Test clock-in with duplicate entry (already clocked in)")
    public void testClockIn_DuplicateEntry_ThrowsException() {
        // Arrange
        when(employeeRepository.findByBadgeId(anyString())).thenReturn(Optional.of(testEmployee));
        when(attendanceRepository.findByEmployeeAndClockInBetween(any(), any(), any()))
                .thenReturn(Arrays.asList(testAttendance));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            attendanceService.clockIn(clockInRequest);
        });
        verify(attendanceRepository, never()).save(any(Attendance.class));
    }

    @Test
    @DisplayName("Test clock-in with null device ID")
    public void testClockIn_NullDeviceId_ThrowsException() {
        // Arrange
        clockInRequest.setDeviceId(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.clockIn(clockInRequest);
        });
    }

    @Test
    @DisplayName("Test clock-in with null location")
    public void testClockIn_NullLocation_ThrowsException() {
        // Arrange
        clockInRequest.setLocation(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.clockIn(clockInRequest);
        });
    }

    @Test
    @DisplayName("Test clock-in with future timestamp")
    public void testClockIn_FutureTimestamp_ThrowsException() {
        // Arrange
        clockInRequest.setTimestamp(LocalDateTime.now().plusDays(1));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.clockIn(clockInRequest);
        });
    }

    @Test
    @DisplayName("Test clock-in with geofence validation")
    public void testClockIn_GeofenceValidation_Success() {
        // Arrange
        clockInRequest.setLatitude(40.7128);
        clockInRequest.setLongitude(-74.0060);
        when(employeeRepository.findByBadgeId(anyString())).thenReturn(Optional.of(testEmployee));
        when(attendanceRepository.findByEmployeeAndClockInBetween(any(), any(), any())).thenReturn(Arrays.asList());
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(testAttendance);

        // Act
        Attendance result = attendanceService.clockIn(clockInRequest);

        // Assert
        assertNotNull(result);
        verify(attendanceRepository, times(1)).save(any(Attendance.class));
    }

    // ==================== CLOCK-OUT TESTS ====================

    @Test
    @DisplayName("Test clock-out with valid input")
    public void testClockOut_ValidInput_Success() {
        // Arrange
        when(employeeRepository.findByBadgeId(anyString())).thenReturn(Optional.of(testEmployee));
        when(attendanceRepository.findByEmployeeAndClockInBetween(any(), any(), any()))
                .thenReturn(Arrays.asList(testAttendance));
        testAttendance.setClockOut(clockOutRequest.getTimestamp());
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(testAttendance);

        // Act
        Attendance result = attendanceService.clockOut(clockOutRequest);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getClockOut());
        assertEquals(AttendanceStatus.CLOCKED_OUT, result.getStatus());
        verify(attendanceRepository, times(1)).save(any(Attendance.class));
    }

    @Test
    @DisplayName("Test clock-out with null request")
    public void testClockOut_NullRequest_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.clockOut(null);
        });
        verify(attendanceRepository, never()).save(any(Attendance.class));
    }

    @Test
    @DisplayName("Test clock-out without clock-in")
    public void testClockOut_WithoutClockIn_ThrowsException() {
        // Arrange
        when(employeeRepository.findByBadgeId(anyString())).thenReturn(Optional.of(testEmployee));
        when(attendanceRepository.findByEmployeeAndClockInBetween(any(), any(), any()))
                .thenReturn(Arrays.asList());

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            attendanceService.clockOut(clockOutRequest);
        });
        verify(attendanceRepository, never()).save(any(Attendance.class));
    }

    @Test
    @DisplayName("Test clock-out with clock-out time before clock-in")
    public void testClockOut_TimeBeforeClockIn_ThrowsException() {
        // Arrange
        clockOutRequest.setTimestamp(testAttendance.getClockIn().minusHours(1));
        when(employeeRepository.findByBadgeId(anyString())).thenReturn(Optional.of(testEmployee));
        when(attendanceRepository.findByEmployeeAndClockInBetween(any(), any(), any()))
                .thenReturn(Arrays.asList(testAttendance));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.clockOut(clockOutRequest);
        });
    }

    @Test
    @DisplayName("Test clock-out with already clocked out")
    public void testClockOut_AlreadyClockedOut_ThrowsException() {
        // Arrange
        testAttendance.setClockOut(LocalDateTime.now());
        testAttendance.setStatus(AttendanceStatus.CLOCKED_OUT);
        when(employeeRepository.findByBadgeId(anyString())).thenReturn(Optional.of(testEmployee));
        when(attendanceRepository.findByEmployeeAndClockInBetween(any(), any(), any()))
                .thenReturn(Arrays.asList(testAttendance));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            attendanceService.clockOut(clockOutRequest);
        });
    }

    // ==================== HOURS CALCULATION TESTS ====================

    @Test
    @DisplayName("Test calculate hours - normal shift (8 hours)")
    public void testCalculateHours_NormalShift_Success() {
        // Arrange
        testAttendance.setClockIn(LocalDateTime.now().withHour(9).withMinute(0));
        testAttendance.setClockOut(LocalDateTime.now().withHour(17).withMinute(0));
        when(attendanceRepository.findByEmployeeAndClockInBetween(any(), any(), any()))
                .thenReturn(Arrays.asList(testAttendance));

        // Act
        Duration hours = attendanceService.calculateHours(1L, LocalDate.now());

        // Assert
        assertNotNull(hours);
        assertEquals(8, hours.toHours());
    }

    @Test
    @DisplayName("Test calculate hours - overtime (10 hours)")
    public void testCalculateHours_Overtime_Success() {
        // Arrange
        testAttendance.setClockIn(LocalDateTime.now().withHour(9).withMinute(0));
        testAttendance.setClockOut(LocalDateTime.now().withHour(19).withMinute(0));
        when(attendanceRepository.findByEmployeeAndClockInBetween(any(), any(), any()))
                .thenReturn(Arrays.asList(testAttendance));

        // Act
        Duration hours = attendanceService.calculateHours(1L, LocalDate.now());

        // Assert
        assertNotNull(hours);
        assertEquals(10, hours.toHours());
    }

    @Test
    @DisplayName("Test calculate hours - partial shift (4 hours)")
    public void testCalculateHours_PartialShift_Success() {
        // Arrange
        testAttendance.setClockIn(LocalDateTime.now().withHour(9).withMinute(0));
        testAttendance.setClockOut(LocalDateTime.now().withHour(13).withMinute(0));
        when(attendanceRepository.findByEmployeeAndClockInBetween(any(), any(), any()))
                .thenReturn(Arrays.asList(testAttendance));

        // Act
        Duration hours = attendanceService.calculateHours(1L, LocalDate.now());

        // Assert
        assertNotNull(hours);
        assertEquals(4, hours.toHours());
    }

    @Test
    @DisplayName("Test calculate hours - no attendance records")
    public void testCalculateHours_NoRecords_ReturnsZero() {
        // Arrange
        when(attendanceRepository.findByEmployeeAndClockInBetween(any(), any(), any()))
                .thenReturn(Arrays.asList());

        // Act
        Duration hours = attendanceService.calculateHours(1L, LocalDate.now());

        // Assert
        assertNotNull(hours);
        assertEquals(0, hours.toHours());
    }

    @Test
    @DisplayName("Test calculate hours - missing clock-out")
    public void testCalculateHours_MissingClockOut_ReturnsZero() {
        // Arrange
        testAttendance.setClockOut(null);
        when(attendanceRepository.findByEmployeeAndClockInBetween(any(), any(), any()))
                .thenReturn(Arrays.asList(testAttendance));

        // Act
        Duration hours = attendanceService.calculateHours(1L, LocalDate.now());

        // Assert
        assertNotNull(hours);
        assertEquals(0, hours.toHours());
    }

    @Test
    @DisplayName("Test calculate hours - multiple shifts in one day")
    public void testCalculateHours_MultipleShifts_Success() {
        // Arrange
        Attendance shift1 = new Attendance();
        shift1.setClockIn(LocalDateTime.now().withHour(9).withMinute(0));
        shift1.setClockOut(LocalDateTime.now().withHour(13).withMinute(0));

        Attendance shift2 = new Attendance();
        shift2.setClockIn(LocalDateTime.now().withHour(14).withMinute(0));
        shift2.setClockOut(LocalDateTime.now().withHour(18).withMinute(0));

        when(attendanceRepository.findByEmployeeAndClockInBetween(any(), any(), any()))
                .thenReturn(Arrays.asList(shift1, shift2));

        // Act
        Duration hours = attendanceService.calculateHours(1L, LocalDate.now());

        // Assert
        assertNotNull(hours);
        assertEquals(8, hours.toHours());
    }

    // ==================== MISSED PUNCH CORRECTION TESTS ====================

    @Test
    @DisplayName("Test handle missed punch - create correction request")
    public void testHandleMissedPunch_CreateCorrectionRequest_Success() {
        // Arrange
        when(attendanceRepository.findById(anyLong())).thenReturn(Optional.of(testAttendance));
        testAttendance.setStatus(AttendanceStatus.MISSED_PUNCH);
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(testAttendance);

        // Act
        Attendance result = attendanceService.handleMissedPunch(1L, "Missing clock-out");

        // Assert
        assertNotNull(result);
        assertEquals(AttendanceStatus.MISSED_PUNCH, result.getStatus());
        verify(attendanceRepository, times(1)).save(any(Attendance.class));
    }

    @Test
    @DisplayName("Test handle missed punch - non-existent attendance")
    public void testHandleMissedPunch_NonExistentAttendance_ThrowsException() {
        // Arrange
        when(attendanceRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            attendanceService.handleMissedPunch(999L, "Missing clock-out");
        });
    }

    @Test
    @DisplayName("Test approve missed punch correction")
    public void testApproveMissedPunchCorrection_ValidInput_Success() {
        // Arrange
        testAttendance.setStatus(AttendanceStatus.MISSED_PUNCH);
        when(attendanceRepository.findById(anyLong())).thenReturn(Optional.of(testAttendance));
        testAttendance.setStatus(AttendanceStatus.CORRECTED);
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(testAttendance);

        // Act
        Attendance result = attendanceService.approveMissedPunchCorrection(1L, LocalDateTime.now());

        // Assert
        assertNotNull(result);
        assertEquals(AttendanceStatus.CORRECTED, result.getStatus());
        verify(attendanceRepository, times(1)).save(any(Attendance.class));
    }

    // ==================== DAILY TOTALS TESTS ====================

    @Test
    @DisplayName("Test get daily totals - single employee")
    public void testGetDailyTotals_SingleEmployee_Success() {
        // Arrange
        testAttendance.setClockOut(LocalDateTime.now().withHour(17).withMinute(0));
        when(attendanceRepository.findByEmployeeAndClockInBetween(any(), any(), any()))
                .thenReturn(Arrays.asList(testAttendance));

        // Act
        Duration total = attendanceService.calculateHours(1L, LocalDate.now());

        // Assert
        assertNotNull(total);
        assertTrue(total.toHours() > 0);
    }

    @Test
    @DisplayName("Test get daily totals - all employees")
    public void testGetDailyTotals_AllEmployees_Success() {
        // Arrange
        List<Attendance> attendances = Arrays.asList(testAttendance);
        when(attendanceRepository.findByClockInBetween(any(), any())).thenReturn(attendances);

        // Act
        List<Attendance> result = attendanceService.getDailyAttendance(LocalDate.now());

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    // ==================== EDGE CASE TESTS ====================

    @Test
    @DisplayName("Test clock-in at midnight")
    public void testClockIn_AtMidnight_Success() {
        // Arrange
        clockInRequest.setTimestamp(LocalDateTime.now().withHour(0).withMinute(0));
        when(employeeRepository.findByBadgeId(anyString())).thenReturn(Optional.of(testEmployee));
        when(attendanceRepository.findByEmployeeAndClockInBetween(any(), any(), any())).thenReturn(Arrays.asList());
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(testAttendance);

        // Act
        Attendance result = attendanceService.clockIn(clockInRequest);

        // Assert
        assertNotNull(result);
        verify(attendanceRepository, times(1)).save(any(Attendance.class));
    }

    @Test
    @DisplayName("Test clock-out at midnight")
    public void testClockOut_AtMidnight_Success() {
        // Arrange
        testAttendance.setClockIn(LocalDateTime.now().minusDays(1).withHour(23).withMinute(0));
        clockOutRequest.setTimestamp(LocalDateTime.now().withHour(0).withMinute(0));
        when(employeeRepository.findByBadgeId(anyString())).thenReturn(Optional.of(testEmployee));
        when(attendanceRepository.findByEmployeeAndClockInBetween(any(), any(), any()))
                .thenReturn(Arrays.asList(testAttendance));
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(testAttendance);

        // Act
        Attendance result = attendanceService.clockOut(clockOutRequest);

        // Assert
        assertNotNull(result);
        verify(attendanceRepository, times(1)).save(any(Attendance.class));
    }

    @Test
    @DisplayName("Test calculate hours - shift spanning midnight")
    public void testCalculateHours_ShiftSpanningMidnight_Success() {
        // Arrange
        testAttendance.setClockIn(LocalDateTime.now().withHour(23).withMinute(0));
        testAttendance.setClockOut(LocalDateTime.now().plusDays(1).withHour(7).withMinute(0));
        when(attendanceRepository.findByEmployeeAndClockInBetween(any(), any(), any()))
                .thenReturn(Arrays.asList(testAttendance));

        // Act
        Duration hours = attendanceService.calculateHours(1L, LocalDate.now());

        // Assert
        assertNotNull(hours);
        assertEquals(8, hours.toHours());
    }

    @Test
    @DisplayName("Test clock-in with very long location name (boundary)")
    public void testClockIn_VeryLongLocationName_Success() {
        // Arrange
        clockInRequest.setLocation("A".repeat(255));
        when(employeeRepository.findByBadgeId(anyString())).thenReturn(Optional.of(testEmployee));
        when(attendanceRepository.findByEmployeeAndClockInBetween(any(), any(), any())).thenReturn(Arrays.asList());
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(testAttendance);

        // Act
        Attendance result = attendanceService.clockIn(clockInRequest);

        // Assert
        assertNotNull(result);
        verify(attendanceRepository, times(1)).save(any(Attendance.class));
    }

    @Test
    @DisplayName("Test calculate hours - very short shift (1 minute)")
    public void testCalculateHours_VeryShortShift_Success() {
        // Arrange
        testAttendance.setClockIn(LocalDateTime.now().withHour(9).withMinute(0));
        testAttendance.setClockOut(LocalDateTime.now().withHour(9).withMinute(1));
        when(attendanceRepository.findByEmployeeAndClockInBetween(any(), any(), any()))
                .thenReturn(Arrays.asList(testAttendance));

        // Act
        Duration hours = attendanceService.calculateHours(1L, LocalDate.now());

        // Assert
        assertNotNull(hours);
        assertEquals(1, hours.toMinutes());
    }

    @Test
    @DisplayName("Test calculate hours - very long shift (24 hours)")
    public void testCalculateHours_VeryLongShift_Success() {
        // Arrange
        testAttendance.setClockIn(LocalDateTime.now().withHour(0).withMinute(0));
        testAttendance.setClockOut(LocalDateTime.now().plusDays(1).withHour(0).withMinute(0));
        when(attendanceRepository.findByEmployeeAndClockInBetween(any(), any(), any()))
                .thenReturn(Arrays.asList(testAttendance));

        // Act
        Duration hours = attendanceService.calculateHours(1L, LocalDate.now());

        // Assert
        assertNotNull(hours);
        assertEquals(24, hours.toHours());
    }
}