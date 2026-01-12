package com.warehouse.attendance;

import com.warehouse.attendance.entity.AttendanceEvent;
import com.warehouse.attendance.repository.AttendanceRepository;
import com.warehouse.attendance.service.AttendanceService;
import com.warehouse.attendance.dto.AttendanceDTO;
import com.warehouse.attendance.exception.AttendanceException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive JUnit test suite for AttendanceService
 * Covers clock-in/out, hours calculation, corrections, and edge cases
 * 
 * @author Automation Test Engineer
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
class AttendanceServiceTest {

    @Mock
    private AttendanceRepository attendanceRepository;

    @InjectMocks
    private AttendanceService attendanceService;

    private AttendanceEvent attendanceEvent;
    private LocalDate testDate;
    private LocalTime clockInTime;
    private LocalTime clockOutTime;

    @BeforeEach
    void setUp() {
        testDate = LocalDate.of(2024, 1, 15);
        clockInTime = LocalTime.of(8, 0);
        clockOutTime = LocalTime.of(17, 0);

        attendanceEvent = new AttendanceEvent();
        attendanceEvent.setId(1L);
        attendanceEvent.setEmployeeId(1L);
        attendanceEvent.setDate(testDate);
        attendanceEvent.setClockInTime(clockInTime);
        attendanceEvent.setClockOutTime(clockOutTime);
        attendanceEvent.setShiftId(1L);
        attendanceEvent.setStatus("COMPLETED");
        attendanceEvent.setHoursWorked(9.0);
    }

    @AfterEach
    void tearDown() {
        attendanceEvent = null;
    }

    // ==================== CLOCK-IN TESTS ====================

    @Test
    @DisplayName("Test clockIn with valid input - should create clock-in event")
    void testClockIn_ValidInput_CreatesClockInEvent() {
        // Arrange
        when(attendanceRepository.findByEmployeeIdAndDate(1L, testDate))
            .thenReturn(Optional.empty());
        when(attendanceRepository.save(any(AttendanceEvent.class)))
            .thenReturn(attendanceEvent);

        // Act
        AttendanceDTO result = attendanceService.clockIn(1L, testDate, clockInTime);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getEmployeeId());
        assertEquals(clockInTime, result.getClockInTime());
        verify(attendanceRepository, times(1)).save(any(AttendanceEvent.class));
    }

    @Test
    @DisplayName("Test clockIn with duplicate entry - should throw AttendanceException")
    void testClockIn_DuplicateEntry_ThrowsException() {
        // Arrange
        when(attendanceRepository.findByEmployeeIdAndDate(1L, testDate))
            .thenReturn(Optional.of(attendanceEvent));

        // Act & Assert
        assertThrows(AttendanceException.class, () -> {
            attendanceService.clockIn(1L, testDate, clockInTime);
        });
        verify(attendanceRepository, never()).save(any(AttendanceEvent.class));
    }

    @Test
    @DisplayName("Test clockIn with null employeeId - should throw IllegalArgumentException")
    void testClockIn_NullEmployeeId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.clockIn(null, testDate, clockInTime);
        });
    }

    @Test
    @DisplayName("Test clockIn with null date - should throw IllegalArgumentException")
    void testClockIn_NullDate_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.clockIn(1L, null, clockInTime);
        });
    }

    @Test
    @DisplayName("Test clockIn with null time - should throw IllegalArgumentException")
    void testClockIn_NullTime_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.clockIn(1L, testDate, null);
        });
    }

    @Test
    @DisplayName("Test clockIn with future date - should throw IllegalArgumentException")
    void testClockIn_FutureDate_ThrowsException() {
        // Arrange
        LocalDate futureDate = LocalDate.now().plusDays(1);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.clockIn(1L, futureDate, clockInTime);
        });
    }

    @Test
    @DisplayName("Test clockIn with geofence data - should capture location")
    void testClockIn_WithGeofence_CapturesLocation() {
        // Arrange
        when(attendanceRepository.findByEmployeeIdAndDate(1L, testDate))
            .thenReturn(Optional.empty());
        when(attendanceRepository.save(any(AttendanceEvent.class)))
            .thenReturn(attendanceEvent);
        
        String latitude = "40.7128";
        String longitude = "-74.0060";

        // Act
        AttendanceDTO result = attendanceService.clockInWithGeofence(
            1L, testDate, clockInTime, latitude, longitude
        );

        // Assert
        assertNotNull(result);
        verify(attendanceRepository, times(1)).save(argThat(event ->
            event.getLatitude() != null && event.getLongitude() != null
        ));
    }

    // ==================== CLOCK-OUT TESTS ====================

    @Test
    @DisplayName("Test clockOut with valid input - should update clock-out time")
    void testClockOut_ValidInput_UpdatesClockOutTime() {
        // Arrange
        attendanceEvent.setClockOutTime(null);
        when(attendanceRepository.findByEmployeeIdAndDate(1L, testDate))
            .thenReturn(Optional.of(attendanceEvent));
        when(attendanceRepository.save(any(AttendanceEvent.class)))
            .thenReturn(attendanceEvent);

        // Act
        AttendanceDTO result = attendanceService.clockOut(1L, testDate, clockOutTime);

        // Assert
        assertNotNull(result);
        assertEquals(clockOutTime, result.getClockOutTime());
        verify(attendanceRepository, times(1)).save(any(AttendanceEvent.class));
    }

    @Test
    @DisplayName("Test clockOut without clock-in - should throw AttendanceException")
    void testClockOut_WithoutClockIn_ThrowsException() {
        // Arrange
        when(attendanceRepository.findByEmployeeIdAndDate(1L, testDate))
            .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(AttendanceException.class, () -> {
            attendanceService.clockOut(1L, testDate, clockOutTime);
        });
    }

    @Test
    @DisplayName("Test clockOut with duplicate clock-out - should throw AttendanceException")
    void testClockOut_DuplicateClockOut_ThrowsException() {
        // Arrange
        when(attendanceRepository.findByEmployeeIdAndDate(1L, testDate))
            .thenReturn(Optional.of(attendanceEvent));

        // Act & Assert
        assertThrows(AttendanceException.class, () -> {
            attendanceService.clockOut(1L, testDate, clockOutTime);
        });
    }

    @Test
    @DisplayName("Test clockOut before clock-in time - should throw IllegalArgumentException")
    void testClockOut_BeforeClockIn_ThrowsException() {
        // Arrange
        attendanceEvent.setClockOutTime(null);
        LocalTime earlyClockOut = LocalTime.of(7, 0);
        when(attendanceRepository.findByEmployeeIdAndDate(1L, testDate))
            .thenReturn(Optional.of(attendanceEvent));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.clockOut(1L, testDate, earlyClockOut);
        });
    }

    @Test
    @DisplayName("Test clockOut with null parameters - should throw IllegalArgumentException")
    void testClockOut_NullParameters_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.clockOut(null, testDate, clockOutTime);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.clockOut(1L, null, clockOutTime);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.clockOut(1L, testDate, null);
        });
    }

    // ==================== HOURS CALCULATION TESTS ====================

    @Test
    @DisplayName("Test calculateHours with normal shift - should return correct hours")
    void testCalculateHours_NormalShift_ReturnsCorrectHours() {
        // Arrange
        attendanceEvent.setClockInTime(LocalTime.of(8, 0));
        attendanceEvent.setClockOutTime(LocalTime.of(17, 0));

        // Act
        double hours = attendanceService.calculateHours(attendanceEvent);

        // Assert
        assertEquals(9.0, hours, 0.01);
    }

    @Test
    @DisplayName("Test calculateHours with partial hours - should return decimal hours")
    void testCalculateHours_PartialHours_ReturnsDecimalHours() {
        // Arrange
        attendanceEvent.setClockInTime(LocalTime.of(8, 0));
        attendanceEvent.setClockOutTime(LocalTime.of(12, 30));

        // Act
        double hours = attendanceService.calculateHours(attendanceEvent);

        // Assert
        assertEquals(4.5, hours, 0.01);
    }

    @Test
    @DisplayName("Test calculateHours with overnight shift - should handle correctly")
    void testCalculateHours_OvernightShift_HandlesCorrectly() {
        // Arrange
        attendanceEvent.setClockInTime(LocalTime.of(22, 0));
        attendanceEvent.setClockOutTime(LocalTime.of(6, 0));

        // Act
        double hours = attendanceService.calculateHours(attendanceEvent);

        // Assert
        assertEquals(8.0, hours, 0.01);
    }

    @Test
    @DisplayName("Test calculateHours with missing clock-out - should throw IllegalStateException")
    void testCalculateHours_MissingClockOut_ThrowsException() {
        // Arrange
        attendanceEvent.setClockOutTime(null);

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            attendanceService.calculateHours(attendanceEvent);
        });
    }

    @Test
    @DisplayName("Test calculateHours with missing clock-in - should throw IllegalStateException")
    void testCalculateHours_MissingClockIn_ThrowsException() {
        // Arrange
        attendanceEvent.setClockInTime(null);

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            attendanceService.calculateHours(attendanceEvent);
        });
    }

    @Test
    @DisplayName("Test calculateHours with break time - should deduct break")
    void testCalculateHours_WithBreak_DeductsBreak() {
        // Arrange
        attendanceEvent.setClockInTime(LocalTime.of(8, 0));
        attendanceEvent.setClockOutTime(LocalTime.of(17, 0));
        attendanceEvent.setBreakMinutes(30);

        // Act
        double hours = attendanceService.calculateHoursWithBreak(attendanceEvent);

        // Assert
        assertEquals(8.5, hours, 0.01);
    }

    // ==================== OVERTIME CALCULATION TESTS ====================

    @Test
    @DisplayName("Test calculateOvertime with hours over threshold - should return overtime hours")
    void testCalculateOvertime_OverThreshold_ReturnsOvertimeHours() {
        // Arrange
        attendanceEvent.setHoursWorked(10.0);
        double overtimeThreshold = 8.0;

        // Act
        double overtime = attendanceService.calculateOvertime(attendanceEvent, overtimeThreshold);

        // Assert
        assertEquals(2.0, overtime, 0.01);
    }

    @Test
    @DisplayName("Test calculateOvertime with hours under threshold - should return zero")
    void testCalculateOvertime_UnderThreshold_ReturnsZero() {
        // Arrange
        attendanceEvent.setHoursWorked(7.0);
        double overtimeThreshold = 8.0;

        // Act
        double overtime = attendanceService.calculateOvertime(attendanceEvent, overtimeThreshold);

        // Assert
        assertEquals(0.0, overtime, 0.01);
    }

    // ==================== CORRECTION WORKFLOW TESTS ====================

    @Test
    @DisplayName("Test requestCorrection with valid input - should create correction request")
    void testRequestCorrection_ValidInput_CreatesRequest() {
        // Arrange
        when(attendanceRepository.findById(1L))
            .thenReturn(Optional.of(attendanceEvent));
        when(attendanceRepository.save(any(AttendanceEvent.class)))
            .thenReturn(attendanceEvent);

        LocalTime newClockIn = LocalTime.of(9, 0);
        LocalTime newClockOut = LocalTime.of(18, 0);
        String reason = "Forgot to clock in on time";

        // Act
        AttendanceDTO result = attendanceService.requestCorrection(
            1L, newClockIn, newClockOut, reason
        );

        // Assert
        assertNotNull(result);
        assertEquals("PENDING_CORRECTION", result.getStatus());
        verify(attendanceRepository, times(1)).save(any(AttendanceEvent.class));
    }

    @Test
    @DisplayName("Test approveCorrection with valid request - should update attendance")
    void testApproveCorrection_ValidRequest_UpdatesAttendance() {
        // Arrange
        attendanceEvent.setStatus("PENDING_CORRECTION");
        attendanceEvent.setCorrectionClockIn(LocalTime.of(9, 0));
        attendanceEvent.setCorrectionClockOut(LocalTime.of(18, 0));
        
        when(attendanceRepository.findById(1L))
            .thenReturn(Optional.of(attendanceEvent));
        when(attendanceRepository.save(any(AttendanceEvent.class)))
            .thenReturn(attendanceEvent);

        // Act
        AttendanceDTO result = attendanceService.approveCorrection(1L, 2L); // supervisorId = 2L

        // Assert
        assertNotNull(result);
        assertEquals("COMPLETED", result.getStatus());
        assertEquals(LocalTime.of(9, 0), result.getClockInTime());
        verify(attendanceRepository, times(1)).save(any(AttendanceEvent.class));
    }

    @Test
    @DisplayName("Test denyCorrection with valid request - should reject correction")
    void testDenyCorrection_ValidRequest_RejectsCorrection() {
        // Arrange
        attendanceEvent.setStatus("PENDING_CORRECTION");
        when(attendanceRepository.findById(1L))
            .thenReturn(Optional.of(attendanceEvent));
        when(attendanceRepository.save(any(AttendanceEvent.class)))
            .thenReturn(attendanceEvent);

        String reason = "Insufficient evidence";

        // Act
        AttendanceDTO result = attendanceService.denyCorrection(1L, 2L, reason);

        // Assert
        assertNotNull(result);
        assertEquals("CORRECTION_DENIED", result.getStatus());
        verify(attendanceRepository, times(1)).save(any(AttendanceEvent.class));
    }

    // ==================== REPORT GENERATION TESTS ====================

    @Test
    @DisplayName("Test generateAttendanceReport for date range - should return report data")
    void testGenerateAttendanceReport_DateRange_ReturnsReportData() {
        // Arrange
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 31);
        List<AttendanceEvent> events = Arrays.asList(attendanceEvent);
        
        when(attendanceRepository.findByDateBetween(startDate, endDate))
            .thenReturn(events);

        // Act
        List<AttendanceDTO> report = attendanceService.generateAttendanceReport(
            startDate, endDate
        );

        // Assert
        assertNotNull(report);
        assertEquals(1, report.size());
    }

    @Test
    @DisplayName("Test exportAttendanceToCSV - should generate CSV content")
    void testExportAttendanceToCSV_ValidData_GeneratesCSV() {
        // Arrange
        List<AttendanceEvent> events = Arrays.asList(attendanceEvent);
        when(attendanceRepository.findAll()).thenReturn(events);

        // Act
        String csv = attendanceService.exportAttendanceToCSV();

        // Assert
        assertNotNull(csv);
        assertTrue(csv.contains("Employee ID"));
        assertTrue(csv.contains("Date"));
        assertTrue(csv.contains("Clock In"));
        assertTrue(csv.contains("Clock Out"));
    }

    // ==================== BOUNDARY CONDITION TESTS ====================

    @Test
    @DisplayName("Test clockIn at midnight - should handle correctly")
    void testClockIn_AtMidnight_HandlesCorrectly() {
        // Arrange
        LocalTime midnight = LocalTime.MIDNIGHT;
        when(attendanceRepository.findByEmployeeIdAndDate(1L, testDate))
            .thenReturn(Optional.empty());
        when(attendanceRepository.save(any(AttendanceEvent.class)))
            .thenReturn(attendanceEvent);

        // Act
        AttendanceDTO result = attendanceService.clockIn(1L, testDate, midnight);

        // Assert
        assertNotNull(result);
        assertEquals(midnight, result.getClockInTime());
    }

    @Test
    @DisplayName("Test calculateHours with maximum shift length - should handle correctly")
    void testCalculateHours_MaximumShift_HandlesCorrectly() {
        // Arrange
        attendanceEvent.setClockInTime(LocalTime.of(0, 0));
        attendanceEvent.setClockOutTime(LocalTime.of(23, 59));

        // Act
        double hours = attendanceService.calculateHours(attendanceEvent);

        // Assert
        assertTrue(hours > 23.0 && hours < 24.0);
    }

    @Test
    @DisplayName("Test getAttendanceByEmployee with no records - should return empty list")
    void testGetAttendanceByEmployee_NoRecords_ReturnsEmptyList() {
        // Arrange
        when(attendanceRepository.findByEmployeeId(999L))
            .thenReturn(Collections.emptyList());

        // Act
        List<AttendanceDTO> result = attendanceService.getAttendanceByEmployee(999L);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ==================== INTEGRATION TESTS ====================

    @Test
    @DisplayName("Test complete attendance flow - clock-in, clock-out, calculate hours")
    void testCompleteAttendanceFlow_Success() {
        // Arrange
        when(attendanceRepository.findByEmployeeIdAndDate(1L, testDate))
            .thenReturn(Optional.empty())
            .thenReturn(Optional.of(attendanceEvent));
        when(attendanceRepository.save(any(AttendanceEvent.class)))
            .thenReturn(attendanceEvent);

        // Act - Clock In
        AttendanceDTO clockedIn = attendanceService.clockIn(1L, testDate, clockInTime);
        assertNotNull(clockedIn);

        // Act - Clock Out
        attendanceEvent.setClockOutTime(null);
        AttendanceDTO clockedOut = attendanceService.clockOut(1L, testDate, clockOutTime);
        assertNotNull(clockedOut);

        // Act - Calculate Hours
        attendanceEvent.setClockOutTime(clockOutTime);
        double hours = attendanceService.calculateHours(attendanceEvent);
        assertEquals(9.0, hours, 0.01);

        // Assert
        verify(attendanceRepository, times(2)).save(any(AttendanceEvent.class));
    }
}