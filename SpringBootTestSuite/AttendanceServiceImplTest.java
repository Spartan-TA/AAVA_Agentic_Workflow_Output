package com.warehouse.management.attendance.service;

import com.warehouse.management.attendance.entity.Attendance;
import com.warehouse.management.attendance.repository.AttendanceRepository;
import com.warehouse.management.employee.entity.Employee;
import com.warehouse.management.employee.repository.EmployeeRepository;
import com.warehouse.management.exception.BadRequestException;
import com.warehouse.management.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Comprehensive JUnit test suite for AttendanceServiceImpl
 * Tests cover clock-in/out operations, corrections, calculations, and edge cases
 */
@ExtendWith(MockitoExtension.class)
public class AttendanceServiceImplTest {

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private AttendanceServiceImpl attendanceService;

    private Employee validEmployee;
    private Attendance validAttendance;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();

        // Arrange: Create valid test employee
        validEmployee = new Employee();
        validEmployee.setId(1L);
        validEmployee.setBadgeId("EMP001");
        validEmployee.setFirstName("John");
        validEmployee.setLastName("Doe");
        validEmployee.setStatus("ACTIVE");
        validEmployee.setDeleted(false);

        // Arrange: Create valid attendance record
        validAttendance = new Attendance();
        validAttendance.setId(1L);
        validAttendance.setEmployeeId(1L);
        validAttendance.setClockIn(now.minusHours(8));
        validAttendance.setClockOut(now);
        validAttendance.setShiftId(1L);
        validAttendance.setHoursWorked(8.0);
        validAttendance.setStatus("COMPLETED");
    }

    // ========== CLOCK IN TESTS ==========

    @Test
    void testClockIn_ValidEmployee_Success() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(validAttendance);

        // Act
        Attendance result = attendanceService.clockIn(1L, 1L, now);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getEmployeeId());
        assertNotNull(result.getClockIn());
        verify(employeeRepository, times(1)).findById(1L);
        verify(attendanceRepository, times(1)).save(any(Attendance.class));
    }

    @Test
    void testClockIn_NonExistentEmployee_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
            () -> attendanceService.clockIn(999L, 1L, now));
        assertTrue(exception.getMessage().contains("Employee not found"));
        verify(employeeRepository, times(1)).findById(999L);
        verify(attendanceRepository, never()).save(any(Attendance.class));
    }

    @Test
    void testClockIn_NullEmployeeId_ThrowsException() {
        // Act & Assert
        assertThrows(BadRequestException.class, 
            () -> attendanceService.clockIn(null, 1L, now));
        verify(employeeRepository, never()).findById(any());
        verify(attendanceRepository, never()).save(any(Attendance.class));
    }

    @Test
    void testClockIn_NullShiftId_ThrowsException() {
        // Act & Assert
        assertThrows(BadRequestException.class, 
            () -> attendanceService.clockIn(1L, null, now));
        verify(employeeRepository, never()).findById(any());
        verify(attendanceRepository, never()).save(any(Attendance.class));
    }

    @Test
    void testClockIn_NullClockInTime_ThrowsException() {
        // Act & Assert
        assertThrows(BadRequestException.class, 
            () -> attendanceService.clockIn(1L, 1L, null));
        verify(employeeRepository, never()).findById(any());
        verify(attendanceRepository, never()).save(any(Attendance.class));
    }

    @Test
    void testClockIn_FutureClockInTime_ThrowsException() {
        // Arrange
        LocalDateTime futureTime = now.plusHours(1);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));

        // Act & Assert
        assertThrows(BadRequestException.class, 
            () -> attendanceService.clockIn(1L, 1L, futureTime));
        verify(employeeRepository, times(1)).findById(1L);
        verify(attendanceRepository, never()).save(any(Attendance.class));
    }

    @Test
    void testClockIn_InactiveEmployee_ThrowsException() {
        // Arrange
        validEmployee.setStatus("INACTIVE");
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));

        // Act & Assert
        assertThrows(BadRequestException.class, 
            () -> attendanceService.clockIn(1L, 1L, now));
        verify(employeeRepository, times(1)).findById(1L);
        verify(attendanceRepository, never()).save(any(Attendance.class));
    }

    @Test
    void testClockIn_DeletedEmployee_ThrowsException() {
        // Arrange
        validEmployee.setDeleted(true);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));

        // Act & Assert
        assertThrows(BadRequestException.class, 
            () -> attendanceService.clockIn(1L, 1L, now));
        verify(employeeRepository, times(1)).findById(1L);
        verify(attendanceRepository, never()).save(any(Attendance.class));
    }

    @Test
    void testClockIn_AlreadyClockedIn_ThrowsException() {
        // Arrange
        Attendance openAttendance = new Attendance();
        openAttendance.setEmployeeId(1L);
        openAttendance.setClockIn(now.minusHours(1));
        openAttendance.setStatus("IN_PROGRESS");

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        when(attendanceRepository.findByEmployeeIdAndStatus(1L, "IN_PROGRESS"))
            .thenReturn(Optional.of(openAttendance));

        // Act & Assert
        assertThrows(BadRequestException.class, 
            () -> attendanceService.clockIn(1L, 1L, now));
        verify(employeeRepository, times(1)).findById(1L);
        verify(attendanceRepository, never()).save(any(Attendance.class));
    }

    // ========== CLOCK OUT TESTS ==========

    @Test
    void testClockOut_ValidAttendance_Success() {
        // Arrange
        Attendance openAttendance = new Attendance();
        openAttendance.setId(1L);
        openAttendance.setEmployeeId(1L);
        openAttendance.setClockIn(now.minusHours(8));
        openAttendance.setStatus("IN_PROGRESS");

        when(attendanceRepository.findByEmployeeIdAndStatus(1L, "IN_PROGRESS"))
            .thenReturn(Optional.of(openAttendance));
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(openAttendance);

        // Act
        Attendance result = attendanceService.clockOut(1L, now);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getClockOut());
        assertEquals("COMPLETED", result.getStatus());
        assertTrue(result.getHoursWorked() > 0);
        verify(attendanceRepository, times(1)).findByEmployeeIdAndStatus(1L, "IN_PROGRESS");
        verify(attendanceRepository, times(1)).save(any(Attendance.class));
    }

    @Test
    void testClockOut_NoOpenAttendance_ThrowsException() {
        // Arrange
        when(attendanceRepository.findByEmployeeIdAndStatus(1L, "IN_PROGRESS"))
            .thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
            () -> attendanceService.clockOut(1L, now));
        assertTrue(exception.getMessage().contains("No open attendance record"));
        verify(attendanceRepository, times(1)).findByEmployeeIdAndStatus(1L, "IN_PROGRESS");
        verify(attendanceRepository, never()).save(any(Attendance.class));
    }

    @Test
    void testClockOut_NullEmployeeId_ThrowsException() {
        // Act & Assert
        assertThrows(BadRequestException.class, 
            () -> attendanceService.clockOut(null, now));
        verify(attendanceRepository, never()).findByEmployeeIdAndStatus(anyLong(), any());
    }

    @Test
    void testClockOut_NullClockOutTime_ThrowsException() {
        // Act & Assert
        assertThrows(BadRequestException.class, 
            () -> attendanceService.clockOut(1L, null));
        verify(attendanceRepository, never()).findByEmployeeIdAndStatus(anyLong(), any());
    }

    @Test
    void testClockOut_ClockOutBeforeClockIn_ThrowsException() {
        // Arrange
        Attendance openAttendance = new Attendance();
        openAttendance.setEmployeeId(1L);
        openAttendance.setClockIn(now);
        openAttendance.setStatus("IN_PROGRESS");

        LocalDateTime earlierTime = now.minusHours(1);
        when(attendanceRepository.findByEmployeeIdAndStatus(1L, "IN_PROGRESS"))
            .thenReturn(Optional.of(openAttendance));

        // Act & Assert
        assertThrows(BadRequestException.class, 
            () -> attendanceService.clockOut(1L, earlierTime));
        verify(attendanceRepository, times(1)).findByEmployeeIdAndStatus(1L, "IN_PROGRESS");
        verify(attendanceRepository, never()).save(any(Attendance.class));
    }

    @Test
    void testClockOut_FutureClockOutTime_ThrowsException() {
        // Arrange
        Attendance openAttendance = new Attendance();
        openAttendance.setEmployeeId(1L);
        openAttendance.setClockIn(now.minusHours(1));
        openAttendance.setStatus("IN_PROGRESS");

        LocalDateTime futureTime = now.plusHours(1);
        when(attendanceRepository.findByEmployeeIdAndStatus(1L, "IN_PROGRESS"))
            .thenReturn(Optional.of(openAttendance));

        // Act & Assert
        assertThrows(BadRequestException.class, 
            () -> attendanceService.clockOut(1L, futureTime));
        verify(attendanceRepository, times(1)).findByEmployeeIdAndStatus(1L, "IN_PROGRESS");
        verify(attendanceRepository, never()).save(any(Attendance.class));
    }

    // ========== HOURS CALCULATION TESTS ==========

    @Test
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
    void testCalculateHours_PartialHours_Success() {
        // Arrange
        LocalDateTime clockIn = LocalDateTime.of(2024, 1, 1, 9, 0);
        LocalDateTime clockOut = LocalDateTime.of(2024, 1, 1, 13, 30);

        // Act
        double hours = attendanceService.calculateHours(clockIn, clockOut);

        // Assert
        assertEquals(4.5, hours, 0.01);
    }

    @Test
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
    void testCalculateHours_VeryShortShift_Success() {
        // Arrange
        LocalDateTime clockIn = LocalDateTime.of(2024, 1, 1, 9, 0);
        LocalDateTime clockOut = LocalDateTime.of(2024, 1, 1, 9, 15);

        // Act
        double hours = attendanceService.calculateHours(clockIn, clockOut);

        // Assert
        assertEquals(0.25, hours, 0.01);
    }

    @Test
    void testCalculateHours_VeryLongShift_Success() {
        // Arrange
        LocalDateTime clockIn = LocalDateTime.of(2024, 1, 1, 6, 0);
        LocalDateTime clockOut = LocalDateTime.of(2024, 1, 1, 22, 0);

        // Act
        double hours = attendanceService.calculateHours(clockIn, clockOut);

        // Assert
        assertEquals(16.0, hours, 0.01);
    }

    @Test
    void testCalculateHours_NullClockIn_ThrowsException() {
        // Act & Assert
        assertThrows(BadRequestException.class, 
            () -> attendanceService.calculateHours(null, now));
    }

    @Test
    void testCalculateHours_NullClockOut_ThrowsException() {
        // Act & Assert
        assertThrows(BadRequestException.class, 
            () -> attendanceService.calculateHours(now, null));
    }

    // ========== GET ATTENDANCE TESTS ==========

    @Test
    void testGetAttendanceById_ExistingId_Success() {
        // Arrange
        when(attendanceRepository.findById(1L)).thenReturn(Optional.of(validAttendance));

        // Act
        Attendance result = attendanceService.getById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(1L, result.getEmployeeId());
        verify(attendanceRepository, times(1)).findById(1L);
    }

    @Test
    void testGetAttendanceById_NonExistingId_ThrowsException() {
        // Arrange
        when(attendanceRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
            () -> attendanceService.getById(999L));
        assertTrue(exception.getMessage().contains("Attendance not found"));
        verify(attendanceRepository, times(1)).findById(999L);
    }

    @Test
    void testGetAttendanceById_NullId_ThrowsException() {
        // Act & Assert
        assertThrows(BadRequestException.class, () -> attendanceService.getById(null));
        verify(attendanceRepository, never()).findById(any());
    }

    @Test
    void testGetAttendanceByEmployeeId_ValidEmployee_Success() {
        // Arrange
        List<Attendance> attendances = Arrays.asList(validAttendance);
        when(attendanceRepository.findByEmployeeId(1L)).thenReturn(attendances);

        // Act
        List<Attendance> result = attendanceService.getByEmployeeId(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getEmployeeId());
        verify(attendanceRepository, times(1)).findByEmployeeId(1L);
    }

    @Test
    void testGetAttendanceByEmployeeId_NoRecords_ReturnsEmptyList() {
        // Arrange
        when(attendanceRepository.findByEmployeeId(1L)).thenReturn(Arrays.asList());

        // Act
        List<Attendance> result = attendanceService.getByEmployeeId(1L);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.size());
        verify(attendanceRepository, times(1)).findByEmployeeId(1L);
    }

    @Test
    void testGetAttendanceByEmployeeId_NullEmployeeId_ThrowsException() {
        // Act & Assert
        assertThrows(BadRequestException.class, 
            () -> attendanceService.getByEmployeeId(null));
        verify(attendanceRepository, never()).findByEmployeeId(any());
    }

    // ========== CORRECTION WORKFLOW TESTS ==========

    @Test
    void testRequestCorrection_ValidRequest_Success() {
        // Arrange
        when(attendanceRepository.findById(1L)).thenReturn(Optional.of(validAttendance));
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(validAttendance);

        // Act
        Attendance result = attendanceService.requestCorrection(1L, "Forgot to clock out");

        // Assert
        assertNotNull(result);
        assertEquals("PENDING_CORRECTION", result.getStatus());
        verify(attendanceRepository, times(1)).findById(1L);
        verify(attendanceRepository, times(1)).save(any(Attendance.class));
    }

    @Test
    void testRequestCorrection_NonExistingAttendance_ThrowsException() {
        // Arrange
        when(attendanceRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
            () -> attendanceService.requestCorrection(999L, "Reason"));
        verify(attendanceRepository, times(1)).findById(999L);
        verify(attendanceRepository, never()).save(any(Attendance.class));
    }

    @Test
    void testRequestCorrection_NullReason_ThrowsException() {
        // Act & Assert
        assertThrows(BadRequestException.class, 
            () -> attendanceService.requestCorrection(1L, null));
        verify(attendanceRepository, never()).findById(any());
    }

    @Test
    void testRequestCorrection_EmptyReason_ThrowsException() {
        // Act & Assert
        assertThrows(BadRequestException.class, 
            () -> attendanceService.requestCorrection(1L, ""));
        verify(attendanceRepository, never()).findById(any());
    }

    // ========== DAILY SUMMARY TESTS ==========

    @Test
    void testGetDailySummary_ValidDate_Success() {
        // Arrange
        LocalDateTime startOfDay = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime endOfDay = LocalDateTime.of(2024, 1, 1, 23, 59, 59);
        List<Attendance> attendances = Arrays.asList(validAttendance);

        when(attendanceRepository.findByEmployeeIdAndClockInBetween(1L, startOfDay, endOfDay))
            .thenReturn(attendances);

        // Act
        List<Attendance> result = attendanceService.getDailySummary(1L, startOfDay.toLocalDate());

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(attendanceRepository, times(1))
            .findByEmployeeIdAndClockInBetween(anyLong(), any(), any());
    }

    @Test
    void testGetDailySummary_NullEmployeeId_ThrowsException() {
        // Act & Assert
        assertThrows(BadRequestException.class, 
            () -> attendanceService.getDailySummary(null, now.toLocalDate()));
        verify(attendanceRepository, never()).findByEmployeeIdAndClockInBetween(anyLong(), any(), any());
    }

    @Test
    void testGetDailySummary_NullDate_ThrowsException() {
        // Act & Assert
        assertThrows(BadRequestException.class, 
            () -> attendanceService.getDailySummary(1L, null));
        verify(attendanceRepository, never()).findByEmployeeIdAndClockInBetween(anyLong(), any(), any());
    }

    // ========== BOUNDARY CONDITION TESTS ==========

    @Test
    void testClockIn_ExactMidnight_Success() {
        // Arrange
        LocalDateTime midnight = LocalDateTime.of(2024, 1, 1, 0, 0, 0);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(validAttendance);

        // Act
        Attendance result = attendanceService.clockIn(1L, 1L, midnight);

        // Assert
        assertNotNull(result);
        verify(attendanceRepository, times(1)).save(any(Attendance.class));
    }

    @Test
    void testClockOut_ExactMidnight_Success() {
        // Arrange
        LocalDateTime midnight = LocalDateTime.of(2024, 1, 2, 0, 0, 0);
        Attendance openAttendance = new Attendance();
        openAttendance.setEmployeeId(1L);
        openAttendance.setClockIn(LocalDateTime.of(2024, 1, 1, 16, 0));
        openAttendance.setStatus("IN_PROGRESS");

        when(attendanceRepository.findByEmployeeIdAndStatus(1L, "IN_PROGRESS"))
            .thenReturn(Optional.of(openAttendance));
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(openAttendance);

        // Act
        Attendance result = attendanceService.clockOut(1L, midnight);

        // Assert
        assertNotNull(result);
        assertEquals(8.0, result.getHoursWorked(), 0.01);
    }

    @Test
    void testCalculateHours_ExactlyOneMinute_Success() {
        // Arrange
        LocalDateTime clockIn = LocalDateTime.of(2024, 1, 1, 9, 0);
        LocalDateTime clockOut = LocalDateTime.of(2024, 1, 1, 9, 1);

        // Act
        double hours = attendanceService.calculateHours(clockIn, clockOut);

        // Assert
        assertTrue(hours > 0);
        assertTrue(hours < 0.1);
    }

    @Test
    void testCalculateHours_ExactlyTwentyFourHours_Success() {
        // Arrange
        LocalDateTime clockIn = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime clockOut = LocalDateTime.of(2024, 1, 2, 0, 0);

        // Act
        double hours = attendanceService.calculateHours(clockIn, clockOut);

        // Assert
        assertEquals(24.0, hours, 0.01);
    }
}