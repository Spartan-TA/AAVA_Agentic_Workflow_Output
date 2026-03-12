package com.wms.attendance.service;

import com.wms.attendance.domain.AttendanceEvent;
import com.wms.attendance.domain.AttendanceType;
import com.wms.attendance.domain.AttendanceStatus;
import com.wms.attendance.dto.AttendanceEventDto;
import com.wms.attendance.dto.ClockInDto;
import com.wms.attendance.dto.ClockOutDto;
import com.wms.attendance.dto.CorrectionDto;
import com.wms.attendance.repository.AttendanceRepository;
import com.wms.employee.domain.Employee;
import com.wms.employee.repository.EmployeeRepository;
import com.wms.scheduling.domain.Shift;
import com.wms.scheduling.repository.ShiftRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive JUnit test suite for AttendanceService
 * Tests cover clock-in/out operations, hours calculation, corrections, and edge cases
 */
@DisplayName("Attendance Service Tests")
public class AttendanceServiceTest {

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private ShiftRepository shiftRepository;

    @InjectMocks
    private AttendanceServiceImpl attendanceService;

    private Employee testEmployee;
    private Shift testShift;
    private AttendanceEvent clockInEvent;
    private AttendanceEvent clockOutEvent;
    private ClockInDto clockInDto;
    private ClockOutDto clockOutDto;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        // Setup test employee
        testEmployee = new Employee();
        testEmployee.setId(1L);
        testEmployee.setBadgeId("EMP001");
        testEmployee.setName("John Doe");

        // Setup test shift
        testShift = new Shift();
        testShift.setId(1L);
        testShift.setStartTime(ZonedDateTime.now().withHour(8).withMinute(0));
        testShift.setEndTime(ZonedDateTime.now().withHour(17).withMinute(0));

        // Setup clock-in event
        clockInEvent = new AttendanceEvent();
        clockInEvent.setId(1L);
        clockInEvent.setEmployee(testEmployee);
        clockInEvent.setType(AttendanceType.CLOCK_IN);
        clockInEvent.setTimestamp(ZonedDateTime.now());
        clockInEvent.setDeviceId("DEVICE001");
        clockInEvent.setShift(testShift);
        clockInEvent.setStatus(AttendanceStatus.NORMAL);

        // Setup clock-out event
        clockOutEvent = new AttendanceEvent();
        clockOutEvent.setId(2L);
        clockOutEvent.setEmployee(testEmployee);
        clockOutEvent.setType(AttendanceType.CLOCK_OUT);
        clockOutEvent.setTimestamp(ZonedDateTime.now().plusHours(8));
        clockOutEvent.setDeviceId("DEVICE001");
        clockOutEvent.setShift(testShift);
        clockOutEvent.setStatus(AttendanceStatus.NORMAL);

        // Setup DTOs
        clockInDto = new ClockInDto();
        clockInDto.setEmployeeId(1L);
        clockInDto.setDeviceId("DEVICE001");
        clockInDto.setLatitude(40.7128);
        clockInDto.setLongitude(-74.0060);

        clockOutDto = new ClockOutDto();
        clockOutDto.setEmployeeId(1L);
        clockOutDto.setDeviceId("DEVICE001");
    }

    // ========== CLOCK-IN TESTS ==========

    @Test
    @DisplayName("Test clock-in with valid data")
    public void testClockIn_ValidData_Success() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(shiftRepository.findCurrentShiftForEmployee(anyLong(), any(ZonedDateTime.class)))
                .thenReturn(Optional.of(testShift));
        when(attendanceRepository.findLastEventForEmployee(anyLong())).thenReturn(Optional.empty());
        when(attendanceRepository.save(any(AttendanceEvent.class))).thenReturn(clockInEvent);

        // Act
        AttendanceEventDto result = attendanceService.clockIn(clockInDto);

        // Assert
        assertNotNull(result);
        assertEquals(AttendanceType.CLOCK_IN.name(), result.getType());
        verify(attendanceRepository, times(1)).save(any(AttendanceEvent.class));
    }

    @Test
    @DisplayName("Test clock-in with null employee ID throws exception")
    public void testClockIn_NullEmployeeId_ThrowsException() {
        // Arrange
        clockInDto.setEmployeeId(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.clockIn(clockInDto);
        });
        verify(attendanceRepository, never()).save(any(AttendanceEvent.class));
    }

    @Test
    @DisplayName("Test clock-in with non-existent employee throws exception")
    public void testClockIn_NonExistentEmployee_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.clockIn(clockInDto);
        });
    }

    @Test
    @DisplayName("Test clock-in when already clocked in throws exception")
    public void testClockIn_AlreadyClockedIn_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(attendanceRepository.findLastEventForEmployee(1L)).thenReturn(Optional.of(clockInEvent));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            attendanceService.clockIn(clockInDto);
        });
    }

    @Test
    @DisplayName("Test clock-in with null device ID throws exception")
    public void testClockIn_NullDeviceId_ThrowsException() {
        // Arrange
        clockInDto.setDeviceId(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.clockIn(clockInDto);
        });
    }

    @Test
    @DisplayName("Test clock-in with invalid geofence throws exception")
    public void testClockIn_InvalidGeofence_ThrowsException() {
        // Arrange
        clockInDto.setLatitude(0.0);
        clockInDto.setLongitude(0.0);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(attendanceRepository.findLastEventForEmployee(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            attendanceService.clockIn(clockInDto);
        });
    }

    @Test
    @DisplayName("Test clock-in without assigned shift creates event")
    public void testClockIn_NoAssignedShift_CreatesEvent() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(shiftRepository.findCurrentShiftForEmployee(anyLong(), any(ZonedDateTime.class)))
                .thenReturn(Optional.empty());
        when(attendanceRepository.findLastEventForEmployee(1L)).thenReturn(Optional.empty());
        when(attendanceRepository.save(any(AttendanceEvent.class))).thenReturn(clockInEvent);

        // Act
        AttendanceEventDto result = attendanceService.clockIn(clockInDto);

        // Assert
        assertNotNull(result);
        verify(attendanceRepository, times(1)).save(any(AttendanceEvent.class));
    }

    // ========== CLOCK-OUT TESTS ==========

    @Test
    @DisplayName("Test clock-out with valid data")
    public void testClockOut_ValidData_Success() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(attendanceRepository.findLastEventForEmployee(1L)).thenReturn(Optional.of(clockInEvent));
        when(attendanceRepository.save(any(AttendanceEvent.class))).thenReturn(clockOutEvent);

        // Act
        AttendanceEventDto result = attendanceService.clockOut(clockOutDto);

        // Assert
        assertNotNull(result);
        assertEquals(AttendanceType.CLOCK_OUT.name(), result.getType());
        verify(attendanceRepository, times(1)).save(any(AttendanceEvent.class));
    }

    @Test
    @DisplayName("Test clock-out with null employee ID throws exception")
    public void testClockOut_NullEmployeeId_ThrowsException() {
        // Arrange
        clockOutDto.setEmployeeId(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.clockOut(clockOutDto);
        });
    }

    @Test
    @DisplayName("Test clock-out when not clocked in throws exception")
    public void testClockOut_NotClockedIn_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(attendanceRepository.findLastEventForEmployee(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            attendanceService.clockOut(clockOutDto);
        });
    }

    @Test
    @DisplayName("Test clock-out when last event is clock-out throws exception")
    public void testClockOut_LastEventIsClockOut_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(attendanceRepository.findLastEventForEmployee(1L)).thenReturn(Optional.of(clockOutEvent));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            attendanceService.clockOut(clockOutDto);
        });
    }

    @Test
    @DisplayName("Test clock-out with non-existent employee throws exception")
    public void testClockOut_NonExistentEmployee_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.clockOut(clockOutDto);
        });
    }

    // ========== HOURS CALCULATION TESTS ==========

    @Test
    @DisplayName("Test calculate hours worked for valid shift")
    public void testCalculateHoursWorked_ValidShift_Success() {
        // Arrange
        when(attendanceRepository.findEventsForEmployeeAndDate(anyLong(), any(LocalDate.class)))
                .thenReturn(Arrays.asList(clockInEvent, clockOutEvent));

        // Act
        Duration result = attendanceService.calculateHoursWorked(1L, LocalDate.now());

        // Assert
        assertNotNull(result);
        assertTrue(result.toHours() > 0);
    }

    @Test
    @DisplayName("Test calculate hours worked with null employee ID throws exception")
    public void testCalculateHoursWorked_NullEmployeeId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.calculateHoursWorked(null, LocalDate.now());
        });
    }

    @Test
    @DisplayName("Test calculate hours worked with null date throws exception")
    public void testCalculateHoursWorked_NullDate_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.calculateHoursWorked(1L, null);
        });
    }

    @Test
    @DisplayName("Test calculate hours worked with no events returns zero")
    public void testCalculateHoursWorked_NoEvents_ReturnsZero() {
        // Arrange
        when(attendanceRepository.findEventsForEmployeeAndDate(anyLong(), any(LocalDate.class)))
                .thenReturn(Arrays.asList());

        // Act
        Duration result = attendanceService.calculateHoursWorked(1L, LocalDate.now());

        // Assert
        assertNotNull(result);
        assertEquals(0, result.toHours());
    }

    @Test
    @DisplayName("Test calculate hours worked with only clock-in returns zero")
    public void testCalculateHoursWorked_OnlyClockIn_ReturnsZero() {
        // Arrange
        when(attendanceRepository.findEventsForEmployeeAndDate(anyLong(), any(LocalDate.class)))
                .thenReturn(Arrays.asList(clockInEvent));

        // Act
        Duration result = attendanceService.calculateHoursWorked(1L, LocalDate.now());

        // Assert
        assertNotNull(result);
        assertEquals(0, result.toHours());
    }

    @Test
    @DisplayName("Test calculate hours worked with multiple shifts")
    public void testCalculateHoursWorked_MultipleShifts_Success() {
        // Arrange
        AttendanceEvent secondClockIn = new AttendanceEvent();
        secondClockIn.setType(AttendanceType.CLOCK_IN);
        secondClockIn.setTimestamp(clockOutEvent.getTimestamp().plusHours(1));
        
        AttendanceEvent secondClockOut = new AttendanceEvent();
        secondClockOut.setType(AttendanceType.CLOCK_OUT);
        secondClockOut.setTimestamp(secondClockIn.getTimestamp().plusHours(4));
        
        when(attendanceRepository.findEventsForEmployeeAndDate(anyLong(), any(LocalDate.class)))
                .thenReturn(Arrays.asList(clockInEvent, clockOutEvent, secondClockIn, secondClockOut));

        // Act
        Duration result = attendanceService.calculateHoursWorked(1L, LocalDate.now());

        // Assert
        assertNotNull(result);
        assertTrue(result.toHours() >= 12);
    }

    @Test
    @DisplayName("Test calculate hours worked with future date returns zero")
    public void testCalculateHoursWorked_FutureDate_ReturnsZero() {
        // Arrange
        when(attendanceRepository.findEventsForEmployeeAndDate(anyLong(), any(LocalDate.class)))
                .thenReturn(Arrays.asList());

        // Act
        Duration result = attendanceService.calculateHoursWorked(1L, LocalDate.now().plusDays(1));

        // Assert
        assertNotNull(result);
        assertEquals(0, result.toHours());
    }

    // ========== MISSED PUNCH TESTS ==========

    @Test
    @DisplayName("Test detect missed punch for employee")
    public void testDetectMissedPunch_ValidEmployee_Success() {
        // Arrange
        when(attendanceRepository.findLastEventForEmployee(1L)).thenReturn(Optional.of(clockInEvent));

        // Act
        boolean result = attendanceService.hasMissedPunch(1L);

        // Assert
        assertTrue(result);
    }

    @Test
    @DisplayName("Test detect missed punch with null employee ID throws exception")
    public void testDetectMissedPunch_NullEmployeeId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.hasMissedPunch(null);
        });
    }

    @Test
    @DisplayName("Test detect missed punch with no events returns false")
    public void testDetectMissedPunch_NoEvents_ReturnsFalse() {
        // Arrange
        when(attendanceRepository.findLastEventForEmployee(1L)).thenReturn(Optional.empty());

        // Act
        boolean result = attendanceService.hasMissedPunch(1L);

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("Test detect missed punch with complete pair returns false")
    public void testDetectMissedPunch_CompletePair_ReturnsFalse() {
        // Arrange
        when(attendanceRepository.findLastEventForEmployee(1L)).thenReturn(Optional.of(clockOutEvent));

        // Act
        boolean result = attendanceService.hasMissedPunch(1L);

        // Assert
        assertFalse(result);
    }

    // ========== CORRECTION TESTS ==========

    @Test
    @DisplayName("Test create correction with valid data")
    public void testCreateCorrection_ValidData_Success() {
        // Arrange
        CorrectionDto correctionDto = new CorrectionDto();
        correctionDto.setEmployeeId(1L);
        correctionDto.setType(AttendanceType.CLOCK_OUT);
        correctionDto.setTimestamp(ZonedDateTime.now());
        correctionDto.setReason("Forgot to clock out");
        correctionDto.setApprovedBy(2L);

        Employee approver = new Employee();
        approver.setId(2L);

        AttendanceEvent correctionEvent = new AttendanceEvent();
        correctionEvent.setStatus(AttendanceStatus.CORRECTION);

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.findById(2L)).thenReturn(Optional.of(approver));
        when(attendanceRepository.save(any(AttendanceEvent.class))).thenReturn(correctionEvent);

        // Act
        AttendanceEventDto result = attendanceService.createCorrection(correctionDto);

        // Assert
        assertNotNull(result);
        assertEquals(AttendanceStatus.CORRECTION.name(), result.getStatus());
        verify(attendanceRepository, times(1)).save(any(AttendanceEvent.class));
    }

    @Test
    @DisplayName("Test create correction with null employee ID throws exception")
    public void testCreateCorrection_NullEmployeeId_ThrowsException() {
        // Arrange
        CorrectionDto correctionDto = new CorrectionDto();
        correctionDto.setEmployeeId(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.createCorrection(correctionDto);
        });
    }

    @Test
    @DisplayName("Test create correction with null approver throws exception")
    public void testCreateCorrection_NullApprover_ThrowsException() {
        // Arrange
        CorrectionDto correctionDto = new CorrectionDto();
        correctionDto.setEmployeeId(1L);
        correctionDto.setApprovedBy(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.createCorrection(correctionDto);
        });
    }

    @Test
    @DisplayName("Test create correction with null reason throws exception")
    public void testCreateCorrection_NullReason_ThrowsException() {
        // Arrange
        CorrectionDto correctionDto = new CorrectionDto();
        correctionDto.setEmployeeId(1L);
        correctionDto.setApprovedBy(2L);
        correctionDto.setReason(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.createCorrection(correctionDto);
        });
    }

    @Test
    @DisplayName("Test create correction with future timestamp throws exception")
    public void testCreateCorrection_FutureTimestamp_ThrowsException() {
        // Arrange
        CorrectionDto correctionDto = new CorrectionDto();
        correctionDto.setEmployeeId(1L);
        correctionDto.setTimestamp(ZonedDateTime.now().plusDays(1));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.createCorrection(correctionDto);
        });
    }

    // ========== EXPORT TESTS ==========

    @Test
    @DisplayName("Test export attendance report for date range")
    public void testExportAttendanceReport_ValidDateRange_Success() {
        // Arrange
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();
        
        when(attendanceRepository.findEventsForDateRange(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Arrays.asList(clockInEvent, clockOutEvent));

        // Act
        byte[] result = attendanceService.exportAttendanceReport(startDate, endDate);

        // Assert
        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    @Test
    @DisplayName("Test export attendance report with null start date throws exception")
    public void testExportAttendanceReport_NullStartDate_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.exportAttendanceReport(null, LocalDate.now());
        });
    }

    @Test
    @DisplayName("Test export attendance report with null end date throws exception")
    public void testExportAttendanceReport_NullEndDate_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.exportAttendanceReport(LocalDate.now(), null);
        });
    }

    @Test
    @DisplayName("Test export attendance report with end date before start date throws exception")
    public void testExportAttendanceReport_EndBeforeStart_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.exportAttendanceReport(LocalDate.now(), LocalDate.now().minusDays(1));
        });
    }

    // ========== BOUNDARY AND EDGE CASE TESTS ==========

    @Test
    @DisplayName("Test clock-in at exact shift start time")
    public void testClockIn_ExactShiftStartTime_Success() {
        // Arrange
        clockInDto.setTimestamp(testShift.getStartTime());
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(shiftRepository.findCurrentShiftForEmployee(anyLong(), any(ZonedDateTime.class)))
                .thenReturn(Optional.of(testShift));
        when(attendanceRepository.findLastEventForEmployee(1L)).thenReturn(Optional.empty());
        when(attendanceRepository.save(any(AttendanceEvent.class))).thenReturn(clockInEvent);

        // Act
        AttendanceEventDto result = attendanceService.clockIn(clockInDto);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Test clock-out at exact shift end time")
    public void testClockOut_ExactShiftEndTime_Success() {
        // Arrange
        clockOutDto.setTimestamp(testShift.getEndTime());
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(attendanceRepository.findLastEventForEmployee(1L)).thenReturn(Optional.of(clockInEvent));
        when(attendanceRepository.save(any(AttendanceEvent.class))).thenReturn(clockOutEvent);

        // Act
        AttendanceEventDto result = attendanceService.clockOut(clockOutDto);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Test calculate hours worked with very short shift")
    public void testCalculateHoursWorked_VeryShortShift_Success() {
        // Arrange
        clockOutEvent.setTimestamp(clockInEvent.getTimestamp().plusMinutes(15));
        when(attendanceRepository.findEventsForEmployeeAndDate(anyLong(), any(LocalDate.class)))
                .thenReturn(Arrays.asList(clockInEvent, clockOutEvent));

        // Act
        Duration result = attendanceService.calculateHoursWorked(1L, LocalDate.now());

        // Assert
        assertNotNull(result);
        assertEquals(15, result.toMinutes());
    }

    @Test
    @DisplayName("Test calculate hours worked with very long shift")
    public void testCalculateHoursWorked_VeryLongShift_Success() {
        // Arrange
        clockOutEvent.setTimestamp(clockInEvent.getTimestamp().plusHours(16));
        when(attendanceRepository.findEventsForEmployeeAndDate(anyLong(), any(LocalDate.class)))
                .thenReturn(Arrays.asList(clockInEvent, clockOutEvent));

        // Act
        Duration result = attendanceService.calculateHoursWorked(1L, LocalDate.now());

        // Assert
        assertNotNull(result);
        assertEquals(16, result.toHours());
    }

    @Test
    @DisplayName("Test clock-in with maximum latitude and longitude")
    public void testClockIn_MaxLatLong_Success() {
        // Arrange
        clockInDto.setLatitude(90.0);
        clockInDto.setLongitude(180.0);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(shiftRepository.findCurrentShiftForEmployee(anyLong(), any(ZonedDateTime.class)))
                .thenReturn(Optional.of(testShift));
        when(attendanceRepository.findLastEventForEmployee(1L)).thenReturn(Optional.empty());
        when(attendanceRepository.save(any(AttendanceEvent.class))).thenReturn(clockInEvent);

        // Act
        AttendanceEventDto result = attendanceService.clockIn(clockInDto);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Test clock-in with minimum latitude and longitude")
    public void testClockIn_MinLatLong_Success() {
        // Arrange
        clockInDto.setLatitude(-90.0);
        clockInDto.setLongitude(-180.0);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(shiftRepository.findCurrentShiftForEmployee(anyLong(), any(ZonedDateTime.class)))
                .thenReturn(Optional.of(testShift));
        when(attendanceRepository.findLastEventForEmployee(1L)).thenReturn(Optional.empty());
        when(attendanceRepository.save(any(AttendanceEvent.class))).thenReturn(clockInEvent);

        // Act
        AttendanceEventDto result = attendanceService.clockIn(clockInDto);

        // Assert
        assertNotNull(result);
    }
}