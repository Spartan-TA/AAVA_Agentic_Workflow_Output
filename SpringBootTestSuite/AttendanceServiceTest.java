package com.wms.ems.attendance.service;

import com.wms.ems.attendance.dto.AttendanceRequestDTO;
import com.wms.ems.attendance.dto.AttendanceResponseDTO;
import com.wms.ems.attendance.entity.AttendanceEvent;
import com.wms.ems.attendance.repository.AttendanceRepository;
import com.wms.ems.employee.entity.Employee;
import com.wms.ems.employee.repository.EmployeeRepository;
import com.wms.ems.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.Optional;
import java.util.List;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive JUnit test suite for AttendanceService
 * Covers: Clock-in/out, hours calculation, geofence validation, corrections
 * Epic: E04 - Time & Attendance (Clock In/Out)
 */
@ExtendWith(MockitoExtension.class)
public class AttendanceServiceTest {

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private AttendanceService attendanceService;

    private Employee testEmployee;
    private AttendanceEvent testAttendance;
    private AttendanceRequestDTO clockInRequest;

    @BeforeEach
    public void setUp() {
        testEmployee = new Employee();
        testEmployee.setId(1L);
        testEmployee.setBadgeId("EMP001");
        testEmployee.setName("John Doe");

        testAttendance = new AttendanceEvent();
        testAttendance.setId(1L);
        testAttendance.setEmployee(testEmployee);
        testAttendance.setClockInTime(LocalDateTime.now().minusHours(8));
        testAttendance.setDeviceId("DEVICE001");
        testAttendance.setLocation("Warehouse A");

        clockInRequest = new AttendanceRequestDTO();
        clockInRequest.setEmployeeId(1L);
        clockInRequest.setDeviceId("DEVICE001");
        clockInRequest.setLocation("Warehouse A");
        clockInRequest.setLatitude(37.7749);
        clockInRequest.setLongitude(-122.4194);
    }

    // ========== CLOCK-IN TESTS ==========

    @Test
    public void testClockIn_ValidRequest_CreatesAttendanceEvent() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(attendanceRepository.findActiveClockInByEmployeeId(1L)).thenReturn(Optional.empty());
        when(attendanceRepository.save(any(AttendanceEvent.class))).thenReturn(testAttendance);

        // Act
        AttendanceResponseDTO result = attendanceService.clockIn(clockInRequest);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getEmployeeId());
        assertNotNull(result.getClockInTime());
        assertNull(result.getClockOutTime());
        verify(attendanceRepository, times(1)).save(any(AttendanceEvent.class));
    }

    @Test
    public void testClockIn_NullRequest_ThrowsIllegalArgumentException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.clockIn(null);
        });
    }

    @Test
    public void testClockIn_InvalidEmployeeId_ThrowsResourceNotFoundException() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());
        clockInRequest.setEmployeeId(999L);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            attendanceService.clockIn(clockInRequest);
        });
    }

    @Test
    public void testClockIn_AlreadyClockedIn_ThrowsIllegalStateException() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(attendanceRepository.findActiveClockInByEmployeeId(1L))
                .thenReturn(Optional.of(testAttendance));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            attendanceService.clockIn(clockInRequest);
        });
        assertTrue(exception.getMessage().contains("already clocked in"));
    }

    @Test
    public void testClockIn_NullDeviceId_ThrowsIllegalArgumentException() {
        // Arrange
        clockInRequest.setDeviceId(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.clockIn(clockInRequest);
        });
    }

    @Test
    public void testClockIn_EmptyLocation_ThrowsIllegalArgumentException() {
        // Arrange
        clockInRequest.setLocation("");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.clockIn(clockInRequest);
        });
    }

    @Test
    public void testClockIn_WithGeofence_ValidLocation_Success() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(attendanceRepository.findActiveClockInByEmployeeId(1L)).thenReturn(Optional.empty());
        when(attendanceRepository.save(any(AttendanceEvent.class))).thenReturn(testAttendance);

        // Act
        AttendanceResponseDTO result = attendanceService.clockIn(clockInRequest);

        // Assert
        assertNotNull(result);
        verify(attendanceRepository, times(1)).save(any(AttendanceEvent.class));
    }

    @Test
    public void testClockIn_WithGeofence_InvalidLocation_ThrowsIllegalArgumentException() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        clockInRequest.setLatitude(0.0); // Outside geofence
        clockInRequest.setLongitude(0.0);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.clockIn(clockInRequest);
        });
    }

    // ========== CLOCK-OUT TESTS ==========

    @Test
    public void testClockOut_ValidRequest_UpdatesAttendanceEvent() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(attendanceRepository.findActiveClockInByEmployeeId(1L))
                .thenReturn(Optional.of(testAttendance));
        when(attendanceRepository.save(any(AttendanceEvent.class))).thenReturn(testAttendance);

        // Act
        AttendanceResponseDTO result = attendanceService.clockOut(clockInRequest);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getClockOutTime());
        assertNotNull(result.getTotalHours());
        verify(attendanceRepository, times(1)).save(any(AttendanceEvent.class));
    }

    @Test
    public void testClockOut_NotClockedIn_ThrowsIllegalStateException() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(attendanceRepository.findActiveClockInByEmployeeId(1L))
                .thenReturn(Optional.empty());

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            attendanceService.clockOut(clockInRequest);
        });
        assertTrue(exception.getMessage().contains("not clocked in"));
    }

    @Test
    public void testClockOut_NullRequest_ThrowsIllegalArgumentException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.clockOut(null);
        });
    }

    @Test
    public void testClockOut_InvalidEmployeeId_ThrowsResourceNotFoundException() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());
        clockInRequest.setEmployeeId(999L);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            attendanceService.clockOut(clockInRequest);
        });
    }

    // ========== HOURS CALCULATION TESTS ==========

    @Test
    public void testCalculateHours_StandardShift_ReturnsCorrectHours() {
        // Arrange
        LocalDateTime clockIn = LocalDateTime.of(2024, 1, 1, 8, 0);
        LocalDateTime clockOut = LocalDateTime.of(2024, 1, 1, 16, 0);
        testAttendance.setClockInTime(clockIn);
        testAttendance.setClockOutTime(clockOut);

        // Act
        double hours = attendanceService.calculateHours(testAttendance);

        // Assert
        assertEquals(8.0, hours, 0.01);
    }

    @Test
    public void testCalculateHours_OvernightShift_ReturnsCorrectHours() {
        // Arrange
        LocalDateTime clockIn = LocalDateTime.of(2024, 1, 1, 22, 0);
        LocalDateTime clockOut = LocalDateTime.of(2024, 1, 2, 6, 0);
        testAttendance.setClockInTime(clockIn);
        testAttendance.setClockOutTime(clockOut);

        // Act
        double hours = attendanceService.calculateHours(testAttendance);

        // Assert
        assertEquals(8.0, hours, 0.01);
    }

    @Test
    public void testCalculateHours_PartialHour_RoundsCorrectly() {
        // Arrange
        LocalDateTime clockIn = LocalDateTime.of(2024, 1, 1, 8, 0);
        LocalDateTime clockOut = LocalDateTime.of(2024, 1, 1, 12, 30);
        testAttendance.setClockInTime(clockIn);
        testAttendance.setClockOutTime(clockOut);

        // Act
        double hours = attendanceService.calculateHours(testAttendance);

        // Assert
        assertEquals(4.5, hours, 0.01);
    }

    @Test
    public void testCalculateHours_LessThanOneMinute_ReturnsZero() {
        // Arrange
        LocalDateTime clockIn = LocalDateTime.of(2024, 1, 1, 8, 0, 0);
        LocalDateTime clockOut = LocalDateTime.of(2024, 1, 1, 8, 0, 30);
        testAttendance.setClockInTime(clockIn);
        testAttendance.setClockOutTime(clockOut);

        // Act
        double hours = attendanceService.calculateHours(testAttendance);

        // Assert
        assertEquals(0.0, hours, 0.01);
    }

    @Test
    public void testCalculateHours_NullClockOut_ThrowsIllegalArgumentException() {
        // Arrange
        testAttendance.setClockOutTime(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.calculateHours(testAttendance);
        });
    }

    // ========== MISSED PUNCH CORRECTION TESTS ==========

    @Test
    public void testCreateCorrectionRequest_ValidRequest_CreatesCorrection() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(attendanceRepository.save(any(AttendanceEvent.class))).thenReturn(testAttendance);
        clockInRequest.setCorrectionRequested(true);
        clockInRequest.setCorrectionReason("Forgot to clock in");

        // Act
        AttendanceResponseDTO result = attendanceService.createCorrectionRequest(clockInRequest);

        // Assert
        assertNotNull(result);
        assertTrue(result.isCorrectionRequested());
        verify(attendanceRepository, times(1)).save(any(AttendanceEvent.class));
    }

    @Test
    public void testCreateCorrectionRequest_NullReason_ThrowsIllegalArgumentException() {
        // Arrange
        clockInRequest.setCorrectionRequested(true);
        clockInRequest.setCorrectionReason(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.createCorrectionRequest(clockInRequest);
        });
    }

    @Test
    public void testCreateCorrectionRequest_EmptyReason_ThrowsIllegalArgumentException() {
        // Arrange
        clockInRequest.setCorrectionRequested(true);
        clockInRequest.setCorrectionReason("");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.createCorrectionRequest(clockInRequest);
        });
    }

    // ========== GET ATTENDANCE HISTORY TESTS ==========

    @Test
    public void testGetAttendanceHistory_ValidEmployeeId_ReturnsHistory() {
        // Arrange
        List<AttendanceEvent> events = Arrays.asList(testAttendance);
        when(attendanceRepository.findByEmployeeIdOrderByClockInTimeDesc(1L))
                .thenReturn(events);

        // Act
        List<AttendanceResponseDTO> result = attendanceService.getAttendanceHistory(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    public void testGetAttendanceHistory_InvalidEmployeeId_ThrowsIllegalArgumentException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.getAttendanceHistory(null);
        });
    }

    @Test
    public void testGetAttendanceHistory_NegativeEmployeeId_ThrowsIllegalArgumentException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.getAttendanceHistory(-1L);
        });
    }

    // ========== EXPORT ATTENDANCE TESTS ==========

    @Test
    public void testExportAttendance_ValidDateRange_ReturnsCSV() {
        // Arrange
        LocalDateTime startDate = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(2024, 1, 31, 23, 59);
        List<AttendanceEvent> events = Arrays.asList(testAttendance);
        when(attendanceRepository.findByClockInTimeBetween(startDate, endDate))
                .thenReturn(events);

        // Act
        String csv = attendanceService.exportAttendanceToCSV(startDate, endDate);

        // Assert
        assertNotNull(csv);
        assertTrue(csv.contains("Employee ID"));
        assertTrue(csv.contains("Clock In"));
        assertTrue(csv.contains("Clock Out"));
    }

    @Test
    public void testExportAttendance_NullStartDate_ThrowsIllegalArgumentException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.exportAttendanceToCSV(null, LocalDateTime.now());
        });
    }

    @Test
    public void testExportAttendance_NullEndDate_ThrowsIllegalArgumentException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.exportAttendanceToCSV(LocalDateTime.now(), null);
        });
    }

    @Test
    public void testExportAttendance_EndDateBeforeStartDate_ThrowsIllegalArgumentException() {
        // Arrange
        LocalDateTime startDate = LocalDateTime.of(2024, 1, 31, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(2024, 1, 1, 0, 0);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.exportAttendanceToCSV(startDate, endDate);
        });
    }

    // ========== EDGE CASE TESTS ==========

    @Test
    public void testClockIn_SameSecond_AllowsMultipleEmployees() {
        // Arrange
        Employee employee2 = new Employee();
        employee2.setId(2L);
        employee2.setBadgeId("EMP002");

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.findById(2L)).thenReturn(Optional.of(employee2));
        when(attendanceRepository.findActiveClockInByEmployeeId(anyLong()))
                .thenReturn(Optional.empty());
        when(attendanceRepository.save(any(AttendanceEvent.class))).thenReturn(testAttendance);

        // Act
        AttendanceResponseDTO result1 = attendanceService.clockIn(clockInRequest);
        clockInRequest.setEmployeeId(2L);
        AttendanceResponseDTO result2 = attendanceService.clockIn(clockInRequest);

        // Assert
        assertNotNull(result1);
        assertNotNull(result2);
        verify(attendanceRepository, times(2)).save(any(AttendanceEvent.class));
    }

    @Test
    public void testCalculateHours_ExactlyOneHour_ReturnsOne() {
        // Arrange
        LocalDateTime clockIn = LocalDateTime.of(2024, 1, 1, 8, 0);
        LocalDateTime clockOut = LocalDateTime.of(2024, 1, 1, 9, 0);
        testAttendance.setClockInTime(clockIn);
        testAttendance.setClockOutTime(clockOut);

        // Act
        double hours = attendanceService.calculateHours(testAttendance);

        // Assert
        assertEquals(1.0, hours, 0.01);
    }

    @Test
    public void testCalculateHours_TwentyFourHourShift_ReturnsTwentyFour() {
        // Arrange
        LocalDateTime clockIn = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime clockOut = LocalDateTime.of(2024, 1, 2, 0, 0);
        testAttendance.setClockInTime(clockIn);
        testAttendance.setClockOutTime(clockOut);

        // Act
        double hours = attendanceService.calculateHours(testAttendance);

        // Assert
        assertEquals(24.0, hours, 0.01);
    }
}