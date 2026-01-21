package com.wms.attendance.service;

import com.wms.attendance.entity.AttendanceRecord;
import com.wms.attendance.repository.AttendanceRepository;
import com.wms.attendance.dto.ClockInRequest;
import com.wms.attendance.dto.ClockOutRequest;
import com.wms.attendance.dto.AttendanceDto;
import com.wms.attendance.dto.CorrectionRequest;
import com.wms.employee.entity.Employee;
import com.wms.employee.repository.EmployeeRepository;
import com.wms.scheduling.entity.ShiftAssignment;
import com.wms.scheduling.repository.ShiftAssignmentRepository;
import com.wms.exception.ResourceNotFoundException;
import com.wms.exception.BadRequestException;
import com.wms.exception.ConflictException;
import com.wms.util.GeofenceUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.Optional;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Comprehensive JUnit test suite for AttendanceService
 * Covers clock-in/out operations, geofence validation, corrections, and edge cases
 */
public class AttendanceServiceTest {

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private ShiftAssignmentRepository shiftAssignmentRepository;

    @Mock
    private GeofenceUtil geofenceUtil;

    @InjectMocks
    private AttendanceService attendanceService;

    private Employee testEmployee;
    private AttendanceRecord testAttendance;
    private ClockInRequest clockInRequest;
    private ClockOutRequest clockOutRequest;
    private ShiftAssignment testShift;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Setup test employee
        testEmployee = new Employee();
        testEmployee.setId(1L);
        testEmployee.setName("John Doe");
        testEmployee.setBadgeId("BADGE001");
        
        // Setup test shift
        testShift = new ShiftAssignment();
        testShift.setId(1L);
        testShift.setEmployeeId(1L);
        testShift.setStartTime(LocalDateTime.now().minusHours(1));
        testShift.setEndTime(LocalDateTime.now().plusHours(7));
        
        // Setup test attendance
        testAttendance = new AttendanceRecord();
        testAttendance.setId(1L);
        testAttendance.setEmployeeId(1L);
        testAttendance.setClockInTime(LocalDateTime.now());
        testAttendance.setDeviceId("DEVICE001");
        testAttendance.setLatitude(40.7128);
        testAttendance.setLongitude(-74.0060);
        testAttendance.setShiftId(1L);
        
        // Setup clock-in request
        clockInRequest = new ClockInRequest();
        clockInRequest.setEmployeeId(1L);
        clockInRequest.setDeviceId("DEVICE001");
        clockInRequest.setLatitude(40.7128);
        clockInRequest.setLongitude(-74.0060);
        clockInRequest.setTimestamp(LocalDateTime.now());
        
        // Setup clock-out request
        clockOutRequest = new ClockOutRequest();
        clockOutRequest.setEmployeeId(1L);
        clockOutRequest.setDeviceId("DEVICE001");
        clockOutRequest.setLatitude(40.7128);
        clockOutRequest.setLongitude(-74.0060);
        clockOutRequest.setTimestamp(LocalDateTime.now());
    }

    // ========== CLOCK-IN TESTS ==========

    @Test
    @DisplayName("Test clock-in with valid data and location")
    public void testClockIn_ValidDataAndLocation_Success() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(geofenceUtil.isWithinGeofence(anyDouble(), anyDouble())).thenReturn(true);
        when(shiftAssignmentRepository.findActiveShiftForEmployee(anyLong(), any(LocalDateTime.class)))
            .thenReturn(Optional.of(testShift));
        when(attendanceRepository.findActiveClockInForEmployee(anyLong())).thenReturn(Optional.empty());
        when(attendanceRepository.save(any(AttendanceRecord.class))).thenReturn(testAttendance);

        // Act
        AttendanceDto result = attendanceService.clockIn(clockInRequest);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getEmployeeId());
        assertNotNull(result.getClockInTime());
        verify(attendanceRepository, times(1)).save(any(AttendanceRecord.class));
    }

    @Test
    @DisplayName("Test clock-in outside geofence")
    public void testClockIn_OutsideGeofence_ThrowsBadRequestException() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(geofenceUtil.isWithinGeofence(anyDouble(), anyDouble())).thenReturn(false);

        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            attendanceService.clockIn(clockInRequest);
        });
        verify(attendanceRepository, never()).save(any(AttendanceRecord.class));
    }

    @Test
    @DisplayName("Test clock-in for non-existent employee")
    public void testClockIn_NonExistentEmployee_ThrowsResourceNotFoundException() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());
        clockInRequest.setEmployeeId(999L);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            attendanceService.clockIn(clockInRequest);
        });
    }

    @Test
    @DisplayName("Test clock-in when already clocked in")
    public void testClockIn_AlreadyClockedIn_ThrowsConflictException() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(geofenceUtil.isWithinGeofence(anyDouble(), anyDouble())).thenReturn(true);
        when(attendanceRepository.findActiveClockInForEmployee(1L)).thenReturn(Optional.of(testAttendance));

        // Act & Assert
        assertThrows(ConflictException.class, () -> {
            attendanceService.clockIn(clockInRequest);
        });
    }

    @Test
    @DisplayName("Test clock-in with null device ID")
    public void testClockIn_NullDeviceId_ThrowsBadRequestException() {
        // Arrange
        clockInRequest.setDeviceId(null);

        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            attendanceService.clockIn(clockInRequest);
        });
    }

    @Test
    @DisplayName("Test clock-in with invalid coordinates")
    public void testClockIn_InvalidCoordinates_ThrowsBadRequestException() {
        // Arrange
        clockInRequest.setLatitude(999.0);
        clockInRequest.setLongitude(999.0);

        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            attendanceService.clockIn(clockInRequest);
        });
    }

    @Test
    @DisplayName("Test clock-in with future timestamp")
    public void testClockIn_FutureTimestamp_ThrowsBadRequestException() {
        // Arrange
        clockInRequest.setTimestamp(LocalDateTime.now().plusHours(1));

        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            attendanceService.clockIn(clockInRequest);
        });
    }

    @Test
    @DisplayName("Test clock-in without scheduled shift")
    public void testClockIn_NoScheduledShift_Success() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(geofenceUtil.isWithinGeofence(anyDouble(), anyDouble())).thenReturn(true);
        when(shiftAssignmentRepository.findActiveShiftForEmployee(anyLong(), any(LocalDateTime.class)))
            .thenReturn(Optional.empty());
        when(attendanceRepository.findActiveClockInForEmployee(anyLong())).thenReturn(Optional.empty());
        when(attendanceRepository.save(any(AttendanceRecord.class))).thenReturn(testAttendance);

        // Act
        AttendanceDto result = attendanceService.clockIn(clockInRequest);

        // Assert
        assertNotNull(result);
        assertNull(result.getShiftId());
    }

    // ========== CLOCK-OUT TESTS ==========

    @Test
    @DisplayName("Test clock-out with valid data")
    public void testClockOut_ValidData_Success() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(attendanceRepository.findActiveClockInForEmployee(1L)).thenReturn(Optional.of(testAttendance));
        when(geofenceUtil.isWithinGeofence(anyDouble(), anyDouble())).thenReturn(true);
        when(attendanceRepository.save(any(AttendanceRecord.class))).thenReturn(testAttendance);

        // Act
        AttendanceDto result = attendanceService.clockOut(clockOutRequest);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getClockOutTime());
        assertNotNull(result.getHoursWorked());
        verify(attendanceRepository, times(1)).save(any(AttendanceRecord.class));
    }

    @Test
    @DisplayName("Test clock-out without clock-in")
    public void testClockOut_NoActiveClockIn_ThrowsConflictException() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(attendanceRepository.findActiveClockInForEmployee(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ConflictException.class, () -> {
            attendanceService.clockOut(clockOutRequest);
        });
    }

    @Test
    @DisplayName("Test clock-out outside geofence")
    public void testClockOut_OutsideGeofence_ThrowsBadRequestException() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(attendanceRepository.findActiveClockInForEmployee(1L)).thenReturn(Optional.of(testAttendance));
        when(geofenceUtil.isWithinGeofence(anyDouble(), anyDouble())).thenReturn(false);

        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            attendanceService.clockOut(clockOutRequest);
        });
    }

    @Test
    @DisplayName("Test clock-out before clock-in time")
    public void testClockOut_BeforeClockIn_ThrowsBadRequestException() {
        // Arrange
        testAttendance.setClockInTime(LocalDateTime.now());
        clockOutRequest.setTimestamp(LocalDateTime.now().minusHours(1));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(attendanceRepository.findActiveClockInForEmployee(1L)).thenReturn(Optional.of(testAttendance));
        when(geofenceUtil.isWithinGeofence(anyDouble(), anyDouble())).thenReturn(true);

        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            attendanceService.clockOut(clockOutRequest);
        });
    }

    @Test
    @DisplayName("Test clock-out with maximum shift duration")
    public void testClockOut_MaximumShiftDuration_Success() {
        // Arrange
        testAttendance.setClockInTime(LocalDateTime.now().minusHours(16));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(attendanceRepository.findActiveClockInForEmployee(1L)).thenReturn(Optional.of(testAttendance));
        when(geofenceUtil.isWithinGeofence(anyDouble(), anyDouble())).thenReturn(true);
        when(attendanceRepository.save(any(AttendanceRecord.class))).thenReturn(testAttendance);

        // Act
        AttendanceDto result = attendanceService.clockOut(clockOutRequest);

        // Assert
        assertNotNull(result);
        assertTrue(result.getHoursWorked() >= 16.0);
    }

    // ========== CORRECTION TESTS ==========

    @Test
    @DisplayName("Test submit missed punch correction")
    public void testSubmitCorrection_ValidRequest_Success() {
        // Arrange
        CorrectionRequest correctionRequest = new CorrectionRequest();
        correctionRequest.setEmployeeId(1L);
        correctionRequest.setDate(LocalDate.now());
        correctionRequest.setMissedClockIn(true);
        correctionRequest.setProposedTime(LocalDateTime.now().minusHours(8));
        correctionRequest.setReason("Forgot to clock in");
        
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(attendanceRepository.save(any(AttendanceRecord.class))).thenReturn(testAttendance);

        // Act
        AttendanceDto result = attendanceService.submitCorrection(correctionRequest);

        // Assert
        assertNotNull(result);
        assertEquals("PENDING_APPROVAL", result.getStatus());
        verify(attendanceRepository, times(1)).save(any(AttendanceRecord.class));
    }

    @Test
    @DisplayName("Test submit correction with null reason")
    public void testSubmitCorrection_NullReason_ThrowsBadRequestException() {
        // Arrange
        CorrectionRequest correctionRequest = new CorrectionRequest();
        correctionRequest.setEmployeeId(1L);
        correctionRequest.setDate(LocalDate.now());
        correctionRequest.setMissedClockIn(true);
        correctionRequest.setProposedTime(LocalDateTime.now());
        correctionRequest.setReason(null);

        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            attendanceService.submitCorrection(correctionRequest);
        });
    }

    @Test
    @DisplayName("Test approve correction")
    public void testApproveCorrection_ValidId_Success() {
        // Arrange
        testAttendance.setStatus("PENDING_APPROVAL");
        when(attendanceRepository.findById(1L)).thenReturn(Optional.of(testAttendance));
        when(attendanceRepository.save(any(AttendanceRecord.class))).thenReturn(testAttendance);

        // Act
        AttendanceDto result = attendanceService.approveCorrection(1L, "APPROVED");

        // Assert
        assertNotNull(result);
        assertEquals("APPROVED", result.getStatus());
    }

    @Test
    @DisplayName("Test deny correction")
    public void testDenyCorrection_ValidId_Success() {
        // Arrange
        testAttendance.setStatus("PENDING_APPROVAL");
        when(attendanceRepository.findById(1L)).thenReturn(Optional.of(testAttendance));
        when(attendanceRepository.save(any(AttendanceRecord.class))).thenReturn(testAttendance);

        // Act
        AttendanceDto result = attendanceService.approveCorrection(1L, "DENIED");

        // Assert
        assertNotNull(result);
        assertEquals("DENIED", result.getStatus());
    }

    // ========== QUERY TESTS ==========

    @Test
    @DisplayName("Test get attendance by employee and date range")
    public void testGetAttendanceByEmployeeAndDateRange_ValidData_Success() {
        // Arrange
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();
        List<AttendanceRecord> records = Arrays.asList(testAttendance);
        when(attendanceRepository.findByEmployeeIdAndDateRange(1L, startDate, endDate)).thenReturn(records);

        // Act
        List<AttendanceDto> result = attendanceService.getAttendanceByEmployeeAndDateRange(1L, startDate, endDate);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Test get attendance with invalid date range")
    public void testGetAttendanceByEmployeeAndDateRange_InvalidRange_ThrowsBadRequestException() {
        // Arrange
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().minusDays(7);

        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            attendanceService.getAttendanceByEmployeeAndDateRange(1L, startDate, endDate);
        });
    }

    @Test
    @DisplayName("Test calculate daily hours")
    public void testCalculateDailyHours_ValidData_Success() {
        // Arrange
        testAttendance.setClockInTime(LocalDateTime.now().minusHours(8));
        testAttendance.setClockOutTime(LocalDateTime.now());
        when(attendanceRepository.findByEmployeeIdAndDate(1L, LocalDate.now()))
            .thenReturn(Arrays.asList(testAttendance));

        // Act
        Double totalHours = attendanceService.calculateDailyHours(1L, LocalDate.now());

        // Assert
        assertNotNull(totalHours);
        assertTrue(totalHours >= 7.9 && totalHours <= 8.1);
    }

    // ========== EDGE CASE TESTS ==========

    @Test
    @DisplayName("Test clock-in at exact geofence boundary")
    public void testClockIn_AtGeofenceBoundary_Success() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(geofenceUtil.isWithinGeofence(anyDouble(), anyDouble())).thenReturn(true);
        when(shiftAssignmentRepository.findActiveShiftForEmployee(anyLong(), any(LocalDateTime.class)))
            .thenReturn(Optional.of(testShift));
        when(attendanceRepository.findActiveClockInForEmployee(anyLong())).thenReturn(Optional.empty());
        when(attendanceRepository.save(any(AttendanceRecord.class))).thenReturn(testAttendance);

        // Act
        AttendanceDto result = attendanceService.clockIn(clockInRequest);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Test clock-out after midnight")
    public void testClockOut_AfterMidnight_Success() {
        // Arrange
        testAttendance.setClockInTime(LocalDateTime.now().minusDays(1).withHour(23).withMinute(0));
        clockOutRequest.setTimestamp(LocalDateTime.now().withHour(1).withMinute(0));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(attendanceRepository.findActiveClockInForEmployee(1L)).thenReturn(Optional.of(testAttendance));
        when(geofenceUtil.isWithinGeofence(anyDouble(), anyDouble())).thenReturn(true);
        when(attendanceRepository.save(any(AttendanceRecord.class))).thenReturn(testAttendance);

        // Act
        AttendanceDto result = attendanceService.clockOut(clockOutRequest);

        // Assert
        assertNotNull(result);
        assertTrue(result.getHoursWorked() > 0);
    }

    @Test
    @DisplayName("Test multiple clock-in attempts in short time")
    public void testClockIn_MultipleAttemptsQuickly_ThrowsConflictException() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(geofenceUtil.isWithinGeofence(anyDouble(), anyDouble())).thenReturn(true);
        when(attendanceRepository.findActiveClockInForEmployee(1L))
            .thenReturn(Optional.empty())
            .thenReturn(Optional.of(testAttendance));
        when(shiftAssignmentRepository.findActiveShiftForEmployee(anyLong(), any(LocalDateTime.class)))
            .thenReturn(Optional.of(testShift));
        when(attendanceRepository.save(any(AttendanceRecord.class))).thenReturn(testAttendance);

        // Act
        AttendanceDto result1 = attendanceService.clockIn(clockInRequest);
        
        // Assert first attempt succeeds
        assertNotNull(result1);
        
        // Second attempt should fail
        assertThrows(ConflictException.class, () -> {
            attendanceService.clockIn(clockInRequest);
        });
    }

    @Test
    @DisplayName("Test export attendance to CSV")
    public void testExportAttendanceToCsv_ValidData_Success() {
        // Arrange
        LocalDate startDate = LocalDate.now().minusDays(30);
        LocalDate endDate = LocalDate.now();
        List<AttendanceRecord> records = Arrays.asList(testAttendance);
        when(attendanceRepository.findByDateRange(startDate, endDate)).thenReturn(records);

        // Act
        String csvContent = attendanceService.exportAttendanceToCsv(startDate, endDate);

        // Assert
        assertNotNull(csvContent);
        assertTrue(csvContent.contains("Employee ID"));
        assertTrue(csvContent.contains("Clock In"));
        assertTrue(csvContent.contains("Clock Out"));
    }
}