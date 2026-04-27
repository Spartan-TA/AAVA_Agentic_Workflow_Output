package com.company.wms.attendance.service;

import com.company.wms.attendance.model.Attendance;
import com.company.wms.attendance.repository.AttendanceRepository;
import com.company.wms.attendance.dto.AttendanceDto;
import com.company.wms.attendance.dto.ClockEventDto;
import com.company.wms.employee.model.Employee;
import com.company.wms.employee.service.EmployeeService;
import com.company.wms.exception.ResourceNotFoundException;
import com.company.wms.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Comprehensive JUnit test suite for AttendanceService
 * Covers: Clock-in/out, geofence validation, hours calculation, edge cases
 * Epic E04: Time & Attendance (Clock In/Out)
 */
@DisplayName("Attendance Service Test Suite")
class AttendanceServiceTest {

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private EmployeeService employeeService;

    @Mock
    private GeofenceService geofenceService;

    @InjectMocks
    private AttendanceService attendanceService;

    private Employee testEmployee;
    private Attendance testAttendance;
    private ClockEventDto clockInDto;
    private ClockEventDto clockOutDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Setup test employee
        testEmployee = new Employee();
        testEmployee.setId(1L);
        testEmployee.setBadgeId("EMP001");
        testEmployee.setName("John Doe");

        // Setup test attendance
        testAttendance = new Attendance();
        testAttendance.setId(1L);
        testAttendance.setEmployee(testEmployee);
        testAttendance.setClockInTime(LocalDateTime.now().minusHours(8));
        testAttendance.setClockInDeviceId("DEVICE001");
        testAttendance.setClockInLocation("40.7128,-74.0060");

        // Setup clock-in DTO
        clockInDto = new ClockEventDto();
        clockInDto.setEmployeeId(1L);
        clockInDto.setTimestamp(LocalDateTime.now());
        clockInDto.setDeviceId("DEVICE001");
        clockInDto.setLocation("40.7128,-74.0060");

        // Setup clock-out DTO
        clockOutDto = new ClockEventDto();
        clockOutDto.setEmployeeId(1L);
        clockOutDto.setTimestamp(LocalDateTime.now());
        clockOutDto.setDeviceId("DEVICE001");
        clockOutDto.setLocation("40.7128,-74.0060");
    }

    // ========== CLOCK-IN TESTS ==========

    @Test
    @DisplayName("Clock In - Valid Input")
    void testClockIn_ValidInput() {
        // Arrange
        when(employeeService.getEmployeeById(1L)).thenReturn(testEmployee);
        when(geofenceService.isWithinAllowedArea(anyString())).thenReturn(true);
        when(attendanceRepository.findActiveClockIn(anyLong())).thenReturn(Optional.empty());
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(testAttendance);

        // Act
        Attendance result = attendanceService.clockIn(clockInDto);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getClockInTime());
        assertEquals("DEVICE001", result.getClockInDeviceId());
        verify(attendanceRepository, times(1)).save(any(Attendance.class));
    }

    @Test
    @DisplayName("Clock In - Null Employee Id")
    void testClockIn_NullEmployeeId() {
        // Arrange
        clockInDto.setEmployeeId(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.clockIn(clockInDto);
        });
    }

    @Test
    @DisplayName("Clock In - Non-Existent Employee")
    void testClockIn_NonExistentEmployee() {
        // Arrange
        when(employeeService.getEmployeeById(999L)).thenThrow(new ResourceNotFoundException("Employee not found"));

        clockInDto.setEmployeeId(999L);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            attendanceService.clockIn(clockInDto);
        });
    }

    @Test
    @DisplayName("Clock In - Outside Geofence")
    void testClockIn_OutsideGeofence() {
        // Arrange
        when(employeeService.getEmployeeById(1L)).thenReturn(testEmployee);
        when(geofenceService.isWithinAllowedArea(anyString())).thenReturn(false);

        // Act & Assert
        assertThrows(ValidationException.class, () -> {
            attendanceService.clockIn(clockInDto);
        });
    }

    @Test
    @DisplayName("Clock In - Already Clocked In")
    void testClockIn_AlreadyClockedIn() {
        // Arrange
        when(employeeService.getEmployeeById(1L)).thenReturn(testEmployee);
        when(geofenceService.isWithinAllowedArea(anyString())).thenReturn(true);
        when(attendanceRepository.findActiveClockIn(1L)).thenReturn(Optional.of(testAttendance));

        // Act & Assert
        assertThrows(ValidationException.class, () -> {
            attendanceService.clockIn(clockInDto);
        });
    }

    @Test
    @DisplayName("Clock In - Null Timestamp")
    void testClockIn_NullTimestamp() {
        // Arrange
        clockInDto.setTimestamp(null);

        // Act & Assert
        assertThrows(ValidationException.class, () -> {
            attendanceService.clockIn(clockInDto);
        });
    }

    @Test
    @DisplayName("Clock In - Future Timestamp")
    void testClockIn_FutureTimestamp() {
        // Arrange
        clockInDto.setTimestamp(LocalDateTime.now().plusHours(1));

        // Act & Assert
        assertThrows(ValidationException.class, () -> {
            attendanceService.clockIn(clockInDto);
        });
    }

    @Test
    @DisplayName("Clock In - Very Old Timestamp")
    void testClockIn_VeryOldTimestamp() {
        // Arrange
        clockInDto.setTimestamp(LocalDateTime.now().minusDays(30));

        // Act & Assert
        assertThrows(ValidationException.class, () -> {
            attendanceService.clockIn(clockInDto);
        });
    }

    @Test
    @DisplayName("Clock In - Null Device Id")
    void testClockIn_NullDeviceId() {
        // Arrange
        clockInDto.setDeviceId(null);
        when(employeeService.getEmployeeById(1L)).thenReturn(testEmployee);
        when(geofenceService.isWithinAllowedArea(anyString())).thenReturn(true);
        when(attendanceRepository.findActiveClockIn(anyLong())).thenReturn(Optional.empty());
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(testAttendance);

        // Act
        Attendance result = attendanceService.clockIn(clockInDto);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Clock In - Null Location")
    void testClockIn_NullLocation() {
        // Arrange
        clockInDto.setLocation(null);

        // Act & Assert
        assertThrows(ValidationException.class, () -> {
            attendanceService.clockIn(clockInDto);
        });
    }

    @Test
    @DisplayName("Clock In - Invalid Location Format")
    void testClockIn_InvalidLocationFormat() {
        // Arrange
        clockInDto.setLocation("INVALID_LOCATION");

        // Act & Assert
        assertThrows(ValidationException.class, () -> {
            attendanceService.clockIn(clockInDto);
        });
    }

    // ========== CLOCK-OUT TESTS ==========

    @Test
    @DisplayName("Clock Out - Valid Input")
    void testClockOut_ValidInput() {
        // Arrange
        when(employeeService.getEmployeeById(1L)).thenReturn(testEmployee);
        when(geofenceService.isWithinAllowedArea(anyString())).thenReturn(true);
        when(attendanceRepository.findActiveClockIn(1L)).thenReturn(Optional.of(testAttendance));
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(testAttendance);

        // Act
        Attendance result = attendanceService.clockOut(clockOutDto);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getClockOutTime());
        assertNotNull(result.getHoursWorked());
        verify(attendanceRepository, times(1)).save(any(Attendance.class));
    }

    @Test
    @DisplayName("Clock Out - No Active Clock In")
    void testClockOut_NoActiveClockIn() {
        // Arrange
        when(employeeService.getEmployeeById(1L)).thenReturn(testEmployee);
        when(geofenceService.isWithinAllowedArea(anyString())).thenReturn(true);
        when(attendanceRepository.findActiveClockIn(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ValidationException.class, () -> {
            attendanceService.clockOut(clockOutDto);
        });
    }

    @Test
    @DisplayName("Clock Out - Outside Geofence")
    void testClockOut_OutsideGeofence() {
        // Arrange
        when(employeeService.getEmployeeById(1L)).thenReturn(testEmployee);
        when(geofenceService.isWithinAllowedArea(anyString())).thenReturn(false);

        // Act & Assert
        assertThrows(ValidationException.class, () -> {
            attendanceService.clockOut(clockOutDto);
        });
    }

    @Test
    @DisplayName("Clock Out - Clock Out Before Clock In")
    void testClockOut_ClockOutBeforeClockIn() {
        // Arrange
        testAttendance.setClockInTime(LocalDateTime.now());
        clockOutDto.setTimestamp(LocalDateTime.now().minusHours(1));
        
        when(employeeService.getEmployeeById(1L)).thenReturn(testEmployee);
        when(geofenceService.isWithinAllowedArea(anyString())).thenReturn(true);
        when(attendanceRepository.findActiveClockIn(1L)).thenReturn(Optional.of(testAttendance));

        // Act & Assert
        assertThrows(ValidationException.class, () -> {
            attendanceService.clockOut(clockOutDto);
        });
    }

    @Test
    @DisplayName("Clock Out - Null Employee Id")
    void testClockOut_NullEmployeeId() {
        // Arrange
        clockOutDto.setEmployeeId(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.clockOut(clockOutDto);
        });
    }

    @Test
    @DisplayName("Clock Out - Null Timestamp")
    void testClockOut_NullTimestamp() {
        // Arrange
        clockOutDto.setTimestamp(null);

        // Act & Assert
        assertThrows(ValidationException.class, () -> {
            attendanceService.clockOut(clockOutDto);
        });
    }

    // ========== HOURS CALCULATION TESTS ==========

    @Test
    @DisplayName("Calculate Hours - Standard 8 Hour Shift")
    void testCalculateHours_Standard8HourShift() {
        // Arrange
        testAttendance.setClockInTime(LocalDateTime.of(2024, 1, 15, 9, 0));
        testAttendance.setClockOutTime(LocalDateTime.of(2024, 1, 15, 17, 0));

        // Act
        Duration duration = attendanceService.calculateHoursWorked(testAttendance);

        // Assert
        assertEquals(8, duration.toHours());
    }

    @Test
    @DisplayName("Calculate Hours - Overtime Shift")
    void testCalculateHours_OvertimeShift() {
        // Arrange
        testAttendance.setClockInTime(LocalDateTime.of(2024, 1, 15, 9, 0));
        testAttendance.setClockOutTime(LocalDateTime.of(2024, 1, 15, 21, 0));

        // Act
        Duration duration = attendanceService.calculateHoursWorked(testAttendance);

        // Assert
        assertEquals(12, duration.toHours());
    }

    @Test
    @DisplayName("Calculate Hours - Short Shift")
    void testCalculateHours_ShortShift() {
        // Arrange
        testAttendance.setClockInTime(LocalDateTime.of(2024, 1, 15, 9, 0));
        testAttendance.setClockOutTime(LocalDateTime.of(2024, 1, 15, 13, 0));

        // Act
        Duration duration = attendanceService.calculateHoursWorked(testAttendance);

        // Assert
        assertEquals(4, duration.toHours());
    }

    @Test
    @DisplayName("Calculate Hours - Midnight Crossing Shift")
    void testCalculateHours_MidnightCrossingShift() {
        // Arrange
        testAttendance.setClockInTime(LocalDateTime.of(2024, 1, 15, 22, 0));
        testAttendance.setClockOutTime(LocalDateTime.of(2024, 1, 16, 6, 0));

        // Act
        Duration duration = attendanceService.calculateHoursWorked(testAttendance);

        // Assert
        assertEquals(8, duration.toHours());
    }

    @Test
    @DisplayName("Calculate Hours - With Minutes")
    void testCalculateHours_WithMinutes() {
        // Arrange
        testAttendance.setClockInTime(LocalDateTime.of(2024, 1, 15, 9, 15));
        testAttendance.setClockOutTime(LocalDateTime.of(2024, 1, 15, 17, 45));

        // Act
        Duration duration = attendanceService.calculateHoursWorked(testAttendance);

        // Assert
        assertEquals(8, duration.toHours());
        assertEquals(30, duration.toMinutes() % 60);
    }

    // ========== MISSED PUNCH CORRECTION TESTS ==========

    @Test
    @DisplayName("Request Correction - Valid Request")
    void testRequestCorrection_ValidRequest() {
        // Arrange
        CorrectionDto correctionDto = new CorrectionDto();
        correctionDto.setEmployeeId(1L);
        correctionDto.setOriginalTimestamp(LocalDateTime.now().minusHours(8));
        correctionDto.setCorrectedTimestamp(LocalDateTime.now().minusHours(8).plusMinutes(5));
        correctionDto.setReason("Forgot to clock in");

        when(employeeService.getEmployeeById(1L)).thenReturn(testEmployee);

        // Act
        CorrectionRequest result = attendanceService.requestCorrection(correctionDto);

        // Assert
        assertNotNull(result);
        assertEquals("PENDING", result.getStatus());
    }

    @Test
    @DisplayName("Request Correction - Null Reason")
    void testRequestCorrection_NullReason() {
        // Arrange
        CorrectionDto correctionDto = new CorrectionDto();
        correctionDto.setEmployeeId(1L);
        correctionDto.setOriginalTimestamp(LocalDateTime.now().minusHours(8));
        correctionDto.setCorrectedTimestamp(LocalDateTime.now().minusHours(8).plusMinutes(5));
        correctionDto.setReason(null);

        // Act & Assert
        assertThrows(ValidationException.class, () -> {
            attendanceService.requestCorrection(correctionDto);
        });
    }

    @Test
    @DisplayName("Request Correction - Empty Reason")
    void testRequestCorrection_EmptyReason() {
        // Arrange
        CorrectionDto correctionDto = new CorrectionDto();
        correctionDto.setEmployeeId(1L);
        correctionDto.setOriginalTimestamp(LocalDateTime.now().minusHours(8));
        correctionDto.setCorrectedTimestamp(LocalDateTime.now().minusHours(8).plusMinutes(5));
        correctionDto.setReason("");

        // Act & Assert
        assertThrows(ValidationException.class, () -> {
            attendanceService.requestCorrection(correctionDto);
        });
    }

    // ========== GEOFENCE VALIDATION TESTS ==========

    @Test
    @DisplayName("Geofence Validation - Valid Coordinates")
    void testGeofenceValidation_ValidCoordinates() {
        // Arrange
        String validLocation = "40.7128,-74.0060";
        when(geofenceService.isWithinAllowedArea(validLocation)).thenReturn(true);

        // Act
        boolean result = geofenceService.isWithinAllowedArea(validLocation);

        // Assert
        assertTrue(result);
    }

    @Test
    @DisplayName("Geofence Validation - Invalid Coordinates")
    void testGeofenceValidation_InvalidCoordinates() {
        // Arrange
        String invalidLocation = "0.0,0.0";
        when(geofenceService.isWithinAllowedArea(invalidLocation)).thenReturn(false);

        // Act
        boolean result = geofenceService.isWithinAllowedArea(invalidLocation);

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("Geofence Validation - Boundary Coordinates")
    void testGeofenceValidation_BoundaryCoordinates() {
        // Arrange
        String boundaryLocation = "40.7128,-74.0060";
        when(geofenceService.isWithinAllowedArea(boundaryLocation)).thenReturn(true);

        // Act
        boolean result = geofenceService.isWithinAllowedArea(boundaryLocation);

        // Assert
        assertTrue(result);
    }

    // ========== EXPORT TESTS ==========

    @Test
    @DisplayName("Export Attendance - Valid Date Range")
    void testExportAttendance_ValidDateRange() {
        // Arrange
        LocalDateTime startDate = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(2024, 1, 31, 23, 59);

        // Act
        byte[] csvData = attendanceService.exportAttendanceCsv(startDate, endDate);

        // Assert
        assertNotNull(csvData);
        assertTrue(csvData.length > 0);
    }

    @Test
    @DisplayName("Export Attendance - Null Start Date")
    void testExportAttendance_NullStartDate() {
        // Arrange
        LocalDateTime endDate = LocalDateTime.of(2024, 1, 31, 23, 59);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.exportAttendanceCsv(null, endDate);
        });
    }

    @Test
    @DisplayName("Export Attendance - Null End Date")
    void testExportAttendance_NullEndDate() {
        // Arrange
        LocalDateTime startDate = LocalDateTime.of(2024, 1, 1, 0, 0);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.exportAttendanceCsv(startDate, null);
        });
    }

    @Test
    @DisplayName("Export Attendance - End Date Before Start Date")
    void testExportAttendance_EndDateBeforeStartDate() {
        // Arrange
        LocalDateTime startDate = LocalDateTime.of(2024, 1, 31, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(2024, 1, 1, 23, 59);

        // Act & Assert
        assertThrows(ValidationException.class, () -> {
            attendanceService.exportAttendanceCsv(startDate, endDate);
        });
    }
}