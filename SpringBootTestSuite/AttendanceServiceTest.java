package com.warehouse.service;

import com.warehouse.entity.AttendanceEvent;
import com.warehouse.repository.AttendanceEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Comprehensive JUnit test suite for AttendanceService.
 * Tests cover clock-in/out operations, geofence validation, and business logic.
 * 
 * @author Warehouse EMS Test Team
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AttendanceService Tests")
public class AttendanceServiceTest {

    @Mock
    private AttendanceEventRepository attendanceEventRepository;

    @InjectMocks
    private AttendanceService attendanceService;

    private AttendanceEvent clockInEvent;
    private AttendanceEvent clockOutEvent;
    private LocalDateTime testTimestamp;

    @BeforeEach
    public void setUp() {
        testTimestamp = LocalDateTime.now();

        clockInEvent = AttendanceEvent.builder()
                .id(1L)
                .employeeId(100L)
                .timestamp(testTimestamp)
                .type("CLOCK_IN")
                .deviceId("DEVICE001")
                .location("40.7128,-74.0060")
                .shiftId(50L)
                .status("NORMAL")
                .build();

        clockOutEvent = AttendanceEvent.builder()
                .id(2L)
                .employeeId(100L)
                .timestamp(testTimestamp.plusHours(8))
                .type("CLOCK_OUT")
                .deviceId("DEVICE001")
                .location("40.7128,-74.0060")
                .shiftId(50L)
                .status("NORMAL")
                .build();
    }

    // ========== CLOCK-IN OPERATION TESTS ==========

    @Test
    @DisplayName("Test clock-in with valid data")
    public void testClockInWithValidData() {
        // Arrange
        when(attendanceEventRepository.save(any(AttendanceEvent.class))).thenReturn(clockInEvent);

        // Act
        AttendanceEvent result = attendanceService.clockIn(100L, "DEVICE001", "40.7128,-74.0060", 50L);

        // Assert
        assertNotNull(result);
        assertEquals(100L, result.getEmployeeId());
        assertEquals("CLOCK_IN", result.getType());
        assertEquals("DEVICE001", result.getDeviceId());
        assertEquals("40.7128,-74.0060", result.getLocation());
        assertEquals(50L, result.getShiftId());
        verify(attendanceEventRepository, times(1)).save(any(AttendanceEvent.class));
    }

    @Test
    @DisplayName("Test clock-in with null employee ID throws exception")
    public void testClockInWithNullEmployeeId() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.clockIn(null, "DEVICE001", "40.7128,-74.0060", 50L);
        });
        verify(attendanceEventRepository, never()).save(any(AttendanceEvent.class));
    }

    @Test
    @DisplayName("Test clock-in with null device ID")
    public void testClockInWithNullDeviceId() {
        // Arrange
        when(attendanceEventRepository.save(any(AttendanceEvent.class))).thenReturn(clockInEvent);

        // Act
        AttendanceEvent result = attendanceService.clockIn(100L, null, "40.7128,-74.0060", 50L);

        // Assert
        assertNotNull(result);
        assertNull(result.getDeviceId());
        verify(attendanceEventRepository, times(1)).save(any(AttendanceEvent.class));
    }

    @Test
    @DisplayName("Test clock-in with null location")
    public void testClockInWithNullLocation() {
        // Arrange
        when(attendanceEventRepository.save(any(AttendanceEvent.class))).thenReturn(clockInEvent);

        // Act
        AttendanceEvent result = attendanceService.clockIn(100L, "DEVICE001", null, 50L);

        // Assert
        assertNotNull(result);
        assertNull(result.getLocation());
        verify(attendanceEventRepository, times(1)).save(any(AttendanceEvent.class));
    }

    @Test
    @DisplayName("Test clock-in with null shift ID")
    public void testClockInWithNullShiftId() {
        // Arrange
        when(attendanceEventRepository.save(any(AttendanceEvent.class))).thenReturn(clockInEvent);

        // Act
        AttendanceEvent result = attendanceService.clockIn(100L, "DEVICE001", "40.7128,-74.0060", null);

        // Assert
        assertNotNull(result);
        assertNull(result.getShiftId());
        verify(attendanceEventRepository, times(1)).save(any(AttendanceEvent.class));
    }

    @Test
    @DisplayName("Test clock-in with invalid geofence location throws exception")
    public void testClockInWithInvalidGeofence() {
        // Arrange
        String invalidLocation = "0.0,0.0"; // Outside geofence

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.clockIn(100L, "DEVICE001", invalidLocation, 50L);
        });
        verify(attendanceEventRepository, never()).save(any(AttendanceEvent.class));
    }

    @Test
    @DisplayName("Test clock-in when already clocked in throws exception")
    public void testClockInWhenAlreadyClockedIn() {
        // Arrange
        when(attendanceEventRepository.findLatestEventByEmployeeId(100L))
                .thenReturn(Optional.of(clockInEvent));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            attendanceService.clockIn(100L, "DEVICE001", "40.7128,-74.0060", 50L);
        });
        verify(attendanceEventRepository, never()).save(any(AttendanceEvent.class));
    }

    // ========== CLOCK-OUT OPERATION TESTS ==========

    @Test
    @DisplayName("Test clock-out with valid data")
    public void testClockOutWithValidData() {
        // Arrange
        when(attendanceEventRepository.findLatestEventByEmployeeId(100L))
                .thenReturn(Optional.of(clockInEvent));
        when(attendanceEventRepository.save(any(AttendanceEvent.class))).thenReturn(clockOutEvent);

        // Act
        AttendanceEvent result = attendanceService.clockOut(100L, "DEVICE001", "40.7128,-74.0060");

        // Assert
        assertNotNull(result);
        assertEquals(100L, result.getEmployeeId());
        assertEquals("CLOCK_OUT", result.getType());
        verify(attendanceEventRepository, times(1)).save(any(AttendanceEvent.class));
    }

    @Test
    @DisplayName("Test clock-out with null employee ID throws exception")
    public void testClockOutWithNullEmployeeId() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.clockOut(null, "DEVICE001", "40.7128,-74.0060");
        });
        verify(attendanceEventRepository, never()).save(any(AttendanceEvent.class));
    }

    @Test
    @DisplayName("Test clock-out without prior clock-in throws exception")
    public void testClockOutWithoutPriorClockIn() {
        // Arrange
        when(attendanceEventRepository.findLatestEventByEmployeeId(100L))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            attendanceService.clockOut(100L, "DEVICE001", "40.7128,-74.0060");
        });
        verify(attendanceEventRepository, never()).save(any(AttendanceEvent.class));
    }

    @Test
    @DisplayName("Test clock-out when already clocked out throws exception")
    public void testClockOutWhenAlreadyClockedOut() {
        // Arrange
        when(attendanceEventRepository.findLatestEventByEmployeeId(100L))
                .thenReturn(Optional.of(clockOutEvent));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            attendanceService.clockOut(100L, "DEVICE001", "40.7128,-74.0060");
        });
        verify(attendanceEventRepository, never()).save(any(AttendanceEvent.class));
    }

    // ========== HOURS CALCULATION TESTS ==========

    @Test
    @DisplayName("Test calculate hours worked for complete shift")
    public void testCalculateHoursWorkedForCompleteShift() {
        // Arrange
        List<AttendanceEvent> events = Arrays.asList(clockInEvent, clockOutEvent);
        when(attendanceEventRepository.findByEmployeeIdAndDateRange(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(events);

        // Act
        double hoursWorked = attendanceService.calculateHoursWorked(100L, LocalDate.now());

        // Assert
        assertEquals(8.0, hoursWorked, 0.01);
        verify(attendanceEventRepository, times(1)).findByEmployeeIdAndDateRange(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("Test calculate hours worked with no events returns zero")
    public void testCalculateHoursWorkedWithNoEvents() {
        // Arrange
        when(attendanceEventRepository.findByEmployeeIdAndDateRange(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Arrays.asList());

        // Act
        double hoursWorked = attendanceService.calculateHoursWorked(100L, LocalDate.now());

        // Assert
        assertEquals(0.0, hoursWorked, 0.01);
        verify(attendanceEventRepository, times(1)).findByEmployeeIdAndDateRange(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("Test calculate hours worked with only clock-in returns zero")
    public void testCalculateHoursWorkedWithOnlyClockIn() {
        // Arrange
        when(attendanceEventRepository.findByEmployeeIdAndDateRange(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Arrays.asList(clockInEvent));

        // Act
        double hoursWorked = attendanceService.calculateHoursWorked(100L, LocalDate.now());

        // Assert
        assertEquals(0.0, hoursWorked, 0.01);
        verify(attendanceEventRepository, times(1)).findByEmployeeIdAndDateRange(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("Test calculate hours worked with multiple shifts")
    public void testCalculateHoursWorkedWithMultipleShifts() {
        // Arrange
        AttendanceEvent clockIn2 = AttendanceEvent.builder()
                .employeeId(100L)
                .timestamp(testTimestamp.plusHours(10))
                .type("CLOCK_IN")
                .build();

        AttendanceEvent clockOut2 = AttendanceEvent.builder()
                .employeeId(100L)
                .timestamp(testTimestamp.plusHours(14))
                .type("CLOCK_OUT")
                .build();

        List<AttendanceEvent> events = Arrays.asList(clockInEvent, clockOutEvent, clockIn2, clockOut2);
        when(attendanceEventRepository.findByEmployeeIdAndDateRange(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(events);

        // Act
        double hoursWorked = attendanceService.calculateHoursWorked(100L, LocalDate.now());

        // Assert
        assertEquals(12.0, hoursWorked, 0.01); // 8 hours + 4 hours
        verify(attendanceEventRepository, times(1)).findByEmployeeIdAndDateRange(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class));
    }

    // ========== CORRECTION WORKFLOW TESTS ==========

    @Test
    @DisplayName("Test request correction for missed punch")
    public void testRequestCorrectionForMissedPunch() {
        // Arrange
        when(attendanceEventRepository.findById(1L)).thenReturn(Optional.of(clockInEvent));
        when(attendanceEventRepository.save(any(AttendanceEvent.class))).thenReturn(clockInEvent);

        // Act
        attendanceService.requestCorrection(1L, "Forgot to clock out");

        // Assert
        assertEquals("CORRECTION_PENDING", clockInEvent.getStatus());
        verify(attendanceEventRepository, times(1)).save(clockInEvent);
    }

    @Test
    @DisplayName("Test request correction with non-existent event throws exception")
    public void testRequestCorrectionWithNonExistentEvent() {
        // Arrange
        when(attendanceEventRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.requestCorrection(999L, "Reason");
        });
        verify(attendanceEventRepository, never()).save(any(AttendanceEvent.class));
    }

    @Test
    @DisplayName("Test approve correction")
    public void testApproveCorrection() {
        // Arrange
        clockInEvent.setStatus("CORRECTION_PENDING");
        when(attendanceEventRepository.findById(1L)).thenReturn(Optional.of(clockInEvent));
        when(attendanceEventRepository.save(any(AttendanceEvent.class))).thenReturn(clockInEvent);

        // Act
        attendanceService.approveCorrection(1L);

        // Assert
        assertEquals("CORRECTED", clockInEvent.getStatus());
        verify(attendanceEventRepository, times(1)).save(clockInEvent);
    }

    @Test
    @DisplayName("Test deny correction")
    public void testDenyCorrection() {
        // Arrange
        clockInEvent.setStatus("CORRECTION_PENDING");
        when(attendanceEventRepository.findById(1L)).thenReturn(Optional.of(clockInEvent));
        when(attendanceEventRepository.save(any(AttendanceEvent.class))).thenReturn(clockInEvent);

        // Act
        attendanceService.denyCorrection(1L);

        // Assert
        assertEquals("NORMAL", clockInEvent.getStatus());
        verify(attendanceEventRepository, times(1)).save(clockInEvent);
    }

    // ========== GEOFENCE VALIDATION TESTS ==========

    @Test
    @DisplayName("Test validate geofence with valid location")
    public void testValidateGeofenceWithValidLocation() {
        // Arrange
        String validLocation = "40.7128,-74.0060";

        // Act
        boolean isValid = attendanceService.validateGeofence(validLocation);

        // Assert
        assertTrue(isValid);
    }

    @Test
    @DisplayName("Test validate geofence with invalid location")
    public void testValidateGeofenceWithInvalidLocation() {
        // Arrange
        String invalidLocation = "0.0,0.0";

        // Act
        boolean isValid = attendanceService.validateGeofence(invalidLocation);

        // Assert
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Test validate geofence with null location")
    public void testValidateGeofenceWithNullLocation() {
        // Act
        boolean isValid = attendanceService.validateGeofence(null);

        // Assert
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Test validate geofence with empty location")
    public void testValidateGeofenceWithEmptyLocation() {
        // Act
        boolean isValid = attendanceService.validateGeofence("");

        // Assert
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Test validate geofence with malformed location")
    public void testValidateGeofenceWithMalformedLocation() {
        // Arrange
        String malformedLocation = "invalid-format";

        // Act
        boolean isValid = attendanceService.validateGeofence(malformedLocation);

        // Assert
        assertFalse(isValid);
    }

    // ========== REPORT GENERATION TESTS ==========

    @Test
    @DisplayName("Test generate daily attendance report")
    public void testGenerateDailyAttendanceReport() {
        // Arrange
        List<AttendanceEvent> events = Arrays.asList(clockInEvent, clockOutEvent);
        when(attendanceEventRepository.findByEmployeeIdAndDateRange(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(events);

        // Act
        List<AttendanceEvent> report = attendanceService.generateDailyReport(100L, LocalDate.now());

        // Assert
        assertNotNull(report);
        assertEquals(2, report.size());
        verify(attendanceEventRepository, times(1)).findByEmployeeIdAndDateRange(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("Test generate weekly attendance report")
    public void testGenerateWeeklyAttendanceReport() {
        // Arrange
        List<AttendanceEvent> events = Arrays.asList(clockInEvent, clockOutEvent);
        when(attendanceEventRepository.findByEmployeeIdAndDateRange(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(events);

        // Act
        List<AttendanceEvent> report = attendanceService.generateWeeklyReport(100L, LocalDate.now());

        // Assert
        assertNotNull(report);
        assertEquals(2, report.size());
        verify(attendanceEventRepository, times(1)).findByEmployeeIdAndDateRange(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("Test export attendance report to CSV")
    public void testExportAttendanceReportToCSV() {
        // Arrange
        List<AttendanceEvent> events = Arrays.asList(clockInEvent, clockOutEvent);
        when(attendanceEventRepository.findByEmployeeIdAndDateRange(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(events);

        // Act
        String csvContent = attendanceService.exportToCSV(100L, LocalDate.now());

        // Assert
        assertNotNull(csvContent);
        assertTrue(csvContent.contains("CLOCK_IN"));
        assertTrue(csvContent.contains("CLOCK_OUT"));
        verify(attendanceEventRepository, times(1)).findByEmployeeIdAndDateRange(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class));
    }

    // ========== EDGE CASE TESTS ==========

    @Test
    @DisplayName("Test clock-in at midnight boundary")
    public void testClockInAtMidnight() {
        // Arrange
        LocalDateTime midnight = LocalDateTime.now().with(LocalTime.MIDNIGHT);
        clockInEvent.setTimestamp(midnight);
        when(attendanceEventRepository.save(any(AttendanceEvent.class))).thenReturn(clockInEvent);

        // Act
        AttendanceEvent result = attendanceService.clockIn(100L, "DEVICE001", "40.7128,-74.0060", 50L);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTimestamp().getHour());
        verify(attendanceEventRepository, times(1)).save(any(AttendanceEvent.class));
    }

    @Test
    @DisplayName("Test clock-out spanning midnight")
    public void testClockOutSpanningMidnight() {
        // Arrange
        LocalDateTime lateNight = LocalDateTime.now().with(LocalTime.of(23, 30));
        LocalDateTime earlyMorning = lateNight.plusHours(2);
        
        clockInEvent.setTimestamp(lateNight);
        clockOutEvent.setTimestamp(earlyMorning);

        when(attendanceEventRepository.findLatestEventByEmployeeId(100L))
                .thenReturn(Optional.of(clockInEvent));
        when(attendanceEventRepository.save(any(AttendanceEvent.class))).thenReturn(clockOutEvent);

        // Act
        AttendanceEvent result = attendanceService.clockOut(100L, "DEVICE001", "40.7128,-74.0060");

        // Assert
        assertNotNull(result);
        assertTrue(result.getTimestamp().isAfter(clockInEvent.getTimestamp()));
        verify(attendanceEventRepository, times(1)).save(any(AttendanceEvent.class));
    }

    @Test
    @DisplayName("Test calculate overtime hours")
    public void testCalculateOvertimeHours() {
        // Arrange
        LocalDateTime clockIn = testTimestamp.with(LocalTime.of(8, 0));
        LocalDateTime clockOut = testTimestamp.with(LocalTime.of(20, 0)); // 12 hours

        clockInEvent.setTimestamp(clockIn);
        clockOutEvent.setTimestamp(clockOut);

        List<AttendanceEvent> events = Arrays.asList(clockInEvent, clockOutEvent);
        when(attendanceEventRepository.findByEmployeeIdAndDateRange(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(events);

        // Act
        double overtimeHours = attendanceService.calculateOvertimeHours(100L, LocalDate.now(), 8.0);

        // Assert
        assertEquals(4.0, overtimeHours, 0.01); // 12 - 8 = 4 hours overtime
        verify(attendanceEventRepository, times(1)).findByEmployeeIdAndDateRange(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class));
    }
}
