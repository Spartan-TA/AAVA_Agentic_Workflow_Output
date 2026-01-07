package com.company.warehouse.core.service;

import com.company.warehouse.core.domain.Attendance;
import com.company.warehouse.core.domain.Attendance.Status;
import com.company.warehouse.core.domain.Employee;
import com.company.warehouse.core.repository.AttendanceRepository;
import com.company.warehouse.core.repository.EmployeeRepository;
import com.company.warehouse.api.dto.AttendanceDTO;
import com.company.warehouse.api.exception.ResourceNotFoundException;
import com.company.warehouse.api.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
 * Comprehensive unit tests for AttendanceService.
 * Tests cover clock-in/out, hours calculation, missed punches, and edge cases.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Attendance Service Tests")
class AttendanceServiceTest {

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private AttendanceServiceImpl attendanceService;

    private Employee testEmployee;
    private Attendance testAttendance;
    private AttendanceDTO testAttendanceDTO;

    @BeforeEach
    void setUp() {
        testEmployee = Employee.builder()
                .id(1L)
                .name("John Doe")
                .badgeId("EMP001")
                .build();

        testAttendance = Attendance.builder()
                .id(1L)
                .employee(testEmployee)
                .clockInTime(LocalDateTime.now().minusHours(8))
                .clockOutTime(LocalDateTime.now())
                .location("Warehouse A")
                .deviceInfo("Device123")
                .hoursWorked(8.0)
                .status(Status.CLOCKED_OUT)
                .build();

        testAttendanceDTO = AttendanceDTO.builder()
                .employeeId(1L)
                .location("Warehouse A")
                .deviceInfo("Device123")
                .build();
    }

    // ========== CLOCK-IN TESTS ==========

    @Test
    @DisplayName("Should clock in employee with valid data")
    void testClockIn_ValidData_Success() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(attendanceRepository.findByEmployeeAndClockOutTimeIsNull(testEmployee)).thenReturn(Optional.empty());
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(testAttendance);

        // Act
        AttendanceDTO result = attendanceService.clockIn(testAttendanceDTO);

        // Assert
        assertNotNull(result);
        assertEquals("Warehouse A", result.getLocation());
        verify(employeeRepository, times(1)).findById(1L);
        verify(attendanceRepository, times(1)).save(any(Attendance.class));
    }

    @Test
    @DisplayName("Should throw exception when employee not found for clock-in")
    void testClockIn_EmployeeNotFound_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());
        testAttendanceDTO.setEmployeeId(999L);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            attendanceService.clockIn(testAttendanceDTO);
        });
        verify(employeeRepository, times(1)).findById(999L);
        verify(attendanceRepository, never()).save(any(Attendance.class));
    }

    @Test
    @DisplayName("Should throw exception when employee already clocked in")
    void testClockIn_AlreadyClockedIn_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(attendanceRepository.findByEmployeeAndClockOutTimeIsNull(testEmployee))
                .thenReturn(Optional.of(testAttendance));

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            attendanceService.clockIn(testAttendanceDTO);
        });
        verify(attendanceRepository, never()).save(any(Attendance.class));
    }

    @Test
    @DisplayName("Should throw exception when clock-in with null location")
    void testClockIn_NullLocation_ThrowsException() {
        // Arrange
        testAttendanceDTO.setLocation(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.clockIn(testAttendanceDTO);
        });
    }

    @Test
    @DisplayName("Should throw exception when clock-in with empty device info")
    void testClockIn_EmptyDeviceInfo_ThrowsException() {
        // Arrange
        testAttendanceDTO.setDeviceInfo("");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.clockIn(testAttendanceDTO);
        });
    }

    @Test
    @DisplayName("Should validate geofence when enabled")
    void testClockIn_GeofenceValidation_Success() {
        // Arrange
        testAttendanceDTO.setLatitude(40.7128);
        testAttendanceDTO.setLongitude(-74.0060);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(attendanceRepository.findByEmployeeAndClockOutTimeIsNull(testEmployee)).thenReturn(Optional.empty());
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(testAttendance);

        // Act
        AttendanceDTO result = attendanceService.clockIn(testAttendanceDTO);

        // Assert
        assertNotNull(result);
        verify(attendanceRepository, times(1)).save(any(Attendance.class));
    }

    @Test
    @DisplayName("Should throw exception when geofence validation fails")
    void testClockIn_GeofenceValidationFails_ThrowsException() {
        // Arrange
        testAttendanceDTO.setLatitude(0.0);
        testAttendanceDTO.setLongitude(0.0);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            attendanceService.clockIn(testAttendanceDTO);
        });
    }

    // ========== CLOCK-OUT TESTS ==========

    @Test
    @DisplayName("Should clock out employee with valid data")
    void testClockOut_ValidData_Success() {
        // Arrange
        Attendance clockedInAttendance = Attendance.builder()
                .id(1L)
                .employee(testEmployee)
                .clockInTime(LocalDateTime.now().minusHours(8))
                .status(Status.CLOCKED_IN)
                .build();
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(attendanceRepository.findByEmployeeAndClockOutTimeIsNull(testEmployee))
                .thenReturn(Optional.of(clockedInAttendance));
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(testAttendance);

        // Act
        AttendanceDTO result = attendanceService.clockOut(testAttendanceDTO);

        // Assert
        assertNotNull(result);
        assertEquals(Status.CLOCKED_OUT.name(), result.getStatus());
        verify(attendanceRepository, times(1)).save(any(Attendance.class));
    }

    @Test
    @DisplayName("Should throw exception when employee not clocked in for clock-out")
    void testClockOut_NotClockedIn_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(attendanceRepository.findByEmployeeAndClockOutTimeIsNull(testEmployee))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            attendanceService.clockOut(testAttendanceDTO);
        });
        verify(attendanceRepository, never()).save(any(Attendance.class));
    }

    @Test
    @DisplayName("Should calculate hours worked correctly on clock-out")
    void testClockOut_CalculateHours_Success() {
        // Arrange
        LocalDateTime clockInTime = LocalDateTime.now().minusHours(8).minusMinutes(30);
        Attendance clockedInAttendance = Attendance.builder()
                .id(1L)
                .employee(testEmployee)
                .clockInTime(clockInTime)
                .status(Status.CLOCKED_IN)
                .build();
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(attendanceRepository.findByEmployeeAndClockOutTimeIsNull(testEmployee))
                .thenReturn(Optional.of(clockedInAttendance));
        when(attendanceRepository.save(any(Attendance.class))).thenAnswer(invocation -> {
            Attendance saved = invocation.getArgument(0);
            assertTrue(saved.getHoursWorked() >= 8.0 && saved.getHoursWorked() <= 9.0);
            return saved;
        });

        // Act
        AttendanceDTO result = attendanceService.clockOut(testAttendanceDTO);

        // Assert
        assertNotNull(result);
        verify(attendanceRepository, times(1)).save(any(Attendance.class));
    }

    // ========== HOURS CALCULATION TESTS ==========

    @Test
    @DisplayName("Should calculate hours for standard 8-hour shift")
    void testCalculateHours_StandardShift_Success() {
        // Arrange
        LocalDateTime clockIn = LocalDateTime.of(2024, 1, 1, 9, 0);
        LocalDateTime clockOut = LocalDateTime.of(2024, 1, 1, 17, 0);

        // Act
        double hours = attendanceService.calculateHours(clockIn, clockOut);

        // Assert
        assertEquals(8.0, hours, 0.01);
    }

    @Test
    @DisplayName("Should calculate hours for overnight shift")
    void testCalculateHours_OvernightShift_Success() {
        // Arrange
        LocalDateTime clockIn = LocalDateTime.of(2024, 1, 1, 22, 0);
        LocalDateTime clockOut = LocalDateTime.of(2024, 1, 2, 6, 0);

        // Act
        double hours = attendanceService.calculateHours(clockIn, clockOut);

        // Assert
        assertEquals(8.0, hours, 0.01);
    }

    @Test
    @DisplayName("Should calculate hours for partial shift")
    void testCalculateHours_PartialShift_Success() {
        // Arrange
        LocalDateTime clockIn = LocalDateTime.of(2024, 1, 1, 9, 0);
        LocalDateTime clockOut = LocalDateTime.of(2024, 1, 1, 13, 30);

        // Act
        double hours = attendanceService.calculateHours(clockIn, clockOut);

        // Assert
        assertEquals(4.5, hours, 0.01);
    }

    @Test
    @DisplayName("Should throw exception when clock-out before clock-in")
    void testCalculateHours_ClockOutBeforeClockIn_ThrowsException() {
        // Arrange
        LocalDateTime clockIn = LocalDateTime.of(2024, 1, 1, 17, 0);
        LocalDateTime clockOut = LocalDateTime.of(2024, 1, 1, 9, 0);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.calculateHours(clockIn, clockOut);
        });
    }

    @Test
    @DisplayName("Should handle very long shifts (over 24 hours)")
    void testCalculateHours_VeryLongShift_Success() {
        // Arrange
        LocalDateTime clockIn = LocalDateTime.of(2024, 1, 1, 9, 0);
        LocalDateTime clockOut = LocalDateTime.of(2024, 1, 2, 18, 0);

        // Act
        double hours = attendanceService.calculateHours(clockIn, clockOut);

        // Assert
        assertEquals(33.0, hours, 0.01);
    }

    // ========== MISSED PUNCH TESTS ==========

    @Test
    @DisplayName("Should detect missed punches")
    void testGetMissedPunches_DetectsMissedPunches_Success() {
        // Arrange
        Attendance missedPunch = Attendance.builder()
                .id(2L)
                .employee(testEmployee)
                .clockInTime(LocalDateTime.now().minusDays(1))
                .clockOutTime(null)
                .status(Status.MISSED_PUNCH)
                .build();
        when(attendanceRepository.findByStatusAndClockInTimeBefore(any(), any()))
                .thenReturn(Arrays.asList(missedPunch));

        // Act
        List<AttendanceDTO> result = attendanceService.getMissedPunches();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(attendanceRepository, times(1)).findByStatusAndClockInTimeBefore(any(), any());
    }

    @Test
    @DisplayName("Should correct missed punch with approval")
    void testCorrectPunch_ValidCorrection_Success() {
        // Arrange
        Attendance missedPunch = Attendance.builder()
                .id(1L)
                .employee(testEmployee)
                .clockInTime(LocalDateTime.now().minusDays(1))
                .status(Status.MISSED_PUNCH)
                .build();
        when(attendanceRepository.findById(1L)).thenReturn(Optional.of(missedPunch));
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(missedPunch);

        AttendanceDTO correctionDTO = AttendanceDTO.builder()
                .id(1L)
                .clockOutTime(LocalDateTime.now())
                .correctionReason("Forgot to clock out")
                .build();

        // Act
        AttendanceDTO result = attendanceService.correctPunch(correctionDTO);

        // Assert
        assertNotNull(result);
        verify(attendanceRepository, times(1)).save(any(Attendance.class));
    }

    @Test
    @DisplayName("Should throw exception when correcting non-existent punch")
    void testCorrectPunch_NotFound_ThrowsException() {
        // Arrange
        when(attendanceRepository.findById(999L)).thenReturn(Optional.empty());
        AttendanceDTO correctionDTO = AttendanceDTO.builder()
                .id(999L)
                .clockOutTime(LocalDateTime.now())
                .build();

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            attendanceService.correctPunch(correctionDTO);
        });
    }

    @Test
    @DisplayName("Should require correction reason for missed punch")
    void testCorrectPunch_MissingReason_ThrowsException() {
        // Arrange
        AttendanceDTO correctionDTO = AttendanceDTO.builder()
                .id(1L)
                .clockOutTime(LocalDateTime.now())
                .correctionReason(null)
                .build();

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.correctPunch(correctionDTO);
        });
    }

    // ========== DAILY REPORT TESTS ==========

    @Test
    @DisplayName("Should generate daily attendance report")
    void testGetDailyReport_ValidDate_Success() {
        // Arrange
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfDay = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);
        when(attendanceRepository.findByClockInTimeBetween(startOfDay, endOfDay))
                .thenReturn(Arrays.asList(testAttendance));

        // Act
        List<AttendanceDTO> result = attendanceService.getDailyReport(startOfDay.toLocalDate());

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(attendanceRepository, times(1)).findByClockInTimeBetween(any(), any());
    }

    @Test
    @DisplayName("Should return empty report for date with no attendance")
    void testGetDailyReport_NoAttendance_ReturnsEmptyList() {
        // Arrange
        when(attendanceRepository.findByClockInTimeBetween(any(), any()))
                .thenReturn(Arrays.asList());

        // Act
        List<AttendanceDTO> result = attendanceService.getDailyReport(LocalDateTime.now().toLocalDate());

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ========== EDGE CASE TESTS ==========

    @Test
    @DisplayName("Should handle clock-in at midnight")
    void testClockIn_AtMidnight_Success() {
        // Arrange
        testAttendanceDTO.setClockInTime(LocalDateTime.now().withHour(0).withMinute(0));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(attendanceRepository.findByEmployeeAndClockOutTimeIsNull(testEmployee)).thenReturn(Optional.empty());
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(testAttendance);

        // Act
        AttendanceDTO result = attendanceService.clockIn(testAttendanceDTO);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Should handle multiple clock-ins on same day after clock-out")
    void testClockIn_MultipleOnSameDay_Success() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(attendanceRepository.findByEmployeeAndClockOutTimeIsNull(testEmployee)).thenReturn(Optional.empty());
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(testAttendance);

        // Act
        AttendanceDTO result1 = attendanceService.clockIn(testAttendanceDTO);
        AttendanceDTO result2 = attendanceService.clockIn(testAttendanceDTO);

        // Assert
        assertNotNull(result1);
        assertNotNull(result2);
        verify(attendanceRepository, times(2)).save(any(Attendance.class));
    }

    @Test
    @DisplayName("Should handle concurrent clock-in attempts")
    void testClockIn_ConcurrentAttempts_HandlesGracefully() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(attendanceRepository.findByEmployeeAndClockOutTimeIsNull(testEmployee))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(testAttendance));
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(testAttendance);

        // Act
        AttendanceDTO result1 = attendanceService.clockIn(testAttendanceDTO);

        // Assert
        assertNotNull(result1);
        assertThrows(BusinessException.class, () -> {
            attendanceService.clockIn(testAttendanceDTO);
        });
    }
}