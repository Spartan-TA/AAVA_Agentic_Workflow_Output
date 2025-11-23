package com.warehouse.ems.attendance.service;

import com.warehouse.ems.attendance.dto.AttendanceDto;
import com.warehouse.ems.attendance.entity.Attendance;
import com.warehouse.ems.attendance.repository.AttendanceRepository;
import com.warehouse.ems.attendance.service.impl.AttendanceServiceImpl;
import com.warehouse.ems.employee.entity.Employee;
import com.warehouse.ems.employee.repository.EmployeeRepository;
import com.warehouse.ems.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.Optional;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for AttendanceService
 * Tests clock-in/out operations, shift associations, and edge cases
 */
@ExtendWith(MockitoExtension.class)
public class AttendanceServiceTest {

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private AttendanceServiceImpl attendanceService;

    private Employee testEmployee;
    private Attendance testAttendance;
    private AttendanceDto testAttendanceDto;

    @BeforeEach
    void setUp() {
        testEmployee = new Employee();
        testEmployee.setId(1L);
        testEmployee.setBadgeId("EMP001");
        testEmployee.setName("John Doe");

        testAttendance = new Attendance();
        testAttendance.setId(1L);
        testAttendance.setEmployee(testEmployee);
        testAttendance.setClockIn(LocalDateTime.now().minusHours(8));
        testAttendance.setClockOut(LocalDateTime.now());
        testAttendance.setHoursWorked(8.0);
        testAttendance.setShiftId(1L);
        testAttendance.setDeviceInfo("Mobile App");
        testAttendance.setGeofenceValidated(true);

        testAttendanceDto = new AttendanceDto();
        testAttendanceDto.setEmployeeId(1L);
        testAttendanceDto.setClockIn(LocalDateTime.now().minusHours(8));
        testAttendanceDto.setShiftId(1L);
        testAttendanceDto.setDeviceInfo("Mobile App");
    }

    // ========== CLOCK-IN TESTS ==========

    @Test
    void testClockIn_ValidEmployee_Success() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(attendanceRepository.findActiveClockInByEmployee(1L)).thenReturn(Optional.empty());
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(testAttendance);

        // Act
        AttendanceDto result = attendanceService.clockIn(testAttendanceDto);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getClockIn());
        assertNull(result.getClockOut());
        verify(attendanceRepository, times(1)).save(any(Attendance.class));
    }

    @Test
    void testClockIn_NonExistentEmployee_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());
        testAttendanceDto.setEmployeeId(999L);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> attendanceService.clockIn(testAttendanceDto));
        verify(attendanceRepository, never()).save(any(Attendance.class));
    }

    @Test
    void testClockIn_AlreadyClockedIn_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(attendanceRepository.findActiveClockInByEmployee(1L)).thenReturn(Optional.of(testAttendance));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> attendanceService.clockIn(testAttendanceDto));
        verify(attendanceRepository, never()).save(any(Attendance.class));
    }

    @Test
    void testClockIn_NullEmployeeId_ThrowsException() {
        // Arrange
        testAttendanceDto.setEmployeeId(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> attendanceService.clockIn(testAttendanceDto));
    }

    @Test
    void testClockIn_FutureClockInTime_ThrowsException() {
        // Arrange
        testAttendanceDto.setClockIn(LocalDateTime.now().plusHours(1));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> attendanceService.clockIn(testAttendanceDto));
    }

    @Test
    void testClockIn_WithoutGeofenceValidation_Success() {
        // Arrange
        testAttendanceDto.setGeofenceValidated(false);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(attendanceRepository.findActiveClockInByEmployee(1L)).thenReturn(Optional.empty());
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(testAttendance);

        // Act
        AttendanceDto result = attendanceService.clockIn(testAttendanceDto);

        // Assert
        assertNotNull(result);
        assertFalse(result.isGeofenceValidated());
    }

    @Test
    void testClockIn_WithShiftAssociation_Success() {
        // Arrange
        testAttendanceDto.setShiftId(5L);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(attendanceRepository.findActiveClockInByEmployee(1L)).thenReturn(Optional.empty());
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(testAttendance);

        // Act
        AttendanceDto result = attendanceService.clockIn(testAttendanceDto);

        // Assert
        assertNotNull(result);
        assertEquals(5L, result.getShiftId());
    }

    // ========== CLOCK-OUT TESTS ==========

    @Test
    void testClockOut_ValidClockIn_Success() {
        // Arrange
        testAttendance.setClockOut(null);
        when(attendanceRepository.findActiveClockInByEmployee(1L)).thenReturn(Optional.of(testAttendance));
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(testAttendance);

        // Act
        AttendanceDto result = attendanceService.clockOut(1L);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getClockOut());
        assertTrue(result.getHoursWorked() > 0);
        verify(attendanceRepository, times(1)).save(any(Attendance.class));
    }

    @Test
    void testClockOut_NoActiveClockIn_ThrowsException() {
        // Arrange
        when(attendanceRepository.findActiveClockInByEmployee(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> attendanceService.clockOut(1L));
        verify(attendanceRepository, never()).save(any(Attendance.class));
    }

    @Test
    void testClockOut_AlreadyClockedOut_ThrowsException() {
        // Arrange
        testAttendance.setClockOut(LocalDateTime.now());
        when(attendanceRepository.findActiveClockInByEmployee(1L)).thenReturn(Optional.of(testAttendance));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> attendanceService.clockOut(1L));
    }

    @Test
    void testClockOut_NullEmployeeId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> attendanceService.clockOut(null));
    }

    @Test
    void testClockOut_CalculatesHoursCorrectly() {
        // Arrange
        LocalDateTime clockIn = LocalDateTime.now().minusHours(8).minusMinutes(30);
        testAttendance.setClockIn(clockIn);
        testAttendance.setClockOut(null);
        when(attendanceRepository.findActiveClockInByEmployee(1L)).thenReturn(Optional.of(testAttendance));
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(testAttendance);

        // Act
        AttendanceDto result = attendanceService.clockOut(1L);

        // Assert
        assertNotNull(result);
        assertTrue(result.getHoursWorked() >= 8.0 && result.getHoursWorked() <= 9.0);
    }

    // ========== GET ATTENDANCE TESTS ==========

    @Test
    void testGetAttendanceById_ValidId_ReturnsAttendance() {
        // Arrange
        when(attendanceRepository.findById(1L)).thenReturn(Optional.of(testAttendance));

        // Act
        AttendanceDto result = attendanceService.getAttendanceById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(1L, result.getEmployeeId());
    }

    @Test
    void testGetAttendanceById_NonExistentId_ThrowsException() {
        // Arrange
        when(attendanceRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> attendanceService.getAttendanceById(999L));
    }

    @Test
    void testGetAttendanceById_NullId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> attendanceService.getAttendanceById(null));
    }

    @Test
    void testGetAttendanceByEmployee_ValidEmployee_ReturnsAttendanceList() {
        // Arrange
        List<Attendance> attendanceList = Arrays.asList(testAttendance);
        when(attendanceRepository.findByEmployeeId(1L)).thenReturn(attendanceList);

        // Act
        List<AttendanceDto> result = attendanceService.getAttendanceByEmployee(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getEmployeeId());
    }

    @Test
    void testGetAttendanceByEmployee_NoRecords_ReturnsEmptyList() {
        // Arrange
        when(attendanceRepository.findByEmployeeId(1L)).thenReturn(Arrays.asList());

        // Act
        List<AttendanceDto> result = attendanceService.getAttendanceByEmployee(1L);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ========== SHIFT ASSOCIATION TESTS ==========

    @Test
    void testAssociateShift_ValidAttendance_Success() {
        // Arrange
        when(attendanceRepository.findById(1L)).thenReturn(Optional.of(testAttendance));
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(testAttendance);

        // Act
        AttendanceDto result = attendanceService.associateShift(1L, 10L);

        // Assert
        assertNotNull(result);
        assertEquals(10L, result.getShiftId());
        verify(attendanceRepository, times(1)).save(any(Attendance.class));
    }

    @Test
    void testAssociateShift_NonExistentAttendance_ThrowsException() {
        // Arrange
        when(attendanceRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> attendanceService.associateShift(999L, 10L));
    }

    @Test
    void testAssociateShift_NullShiftId_ThrowsException() {
        // Arrange
        when(attendanceRepository.findById(1L)).thenReturn(Optional.of(testAttendance));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> attendanceService.associateShift(1L, null));
    }

    // ========== CORRECTION TESTS ==========

    @Test
    void testCorrectAttendance_ValidCorrection_Success() {
        // Arrange
        when(attendanceRepository.findById(1L)).thenReturn(Optional.of(testAttendance));
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(testAttendance);
        LocalDateTime newClockIn = LocalDateTime.now().minusHours(9);
        LocalDateTime newClockOut = LocalDateTime.now().minusHours(1);

        // Act
        AttendanceDto result = attendanceService.correctAttendance(1L, newClockIn, newClockOut);

        // Assert
        assertNotNull(result);
        verify(attendanceRepository, times(1)).save(any(Attendance.class));
    }

    @Test
    void testCorrectAttendance_ClockOutBeforeClockIn_ThrowsException() {
        // Arrange
        when(attendanceRepository.findById(1L)).thenReturn(Optional.of(testAttendance));
        LocalDateTime clockIn = LocalDateTime.now();
        LocalDateTime clockOut = LocalDateTime.now().minusHours(1);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, 
            () -> attendanceService.correctAttendance(1L, clockIn, clockOut));
    }

    @Test
    void testCorrectAttendance_ExcessiveHours_ThrowsException() {
        // Arrange
        when(attendanceRepository.findById(1L)).thenReturn(Optional.of(testAttendance));
        LocalDateTime clockIn = LocalDateTime.now().minusHours(25);
        LocalDateTime clockOut = LocalDateTime.now();

        // Act & Assert
        assertThrows(IllegalArgumentException.class, 
            () -> attendanceService.correctAttendance(1L, clockIn, clockOut));
    }

    // ========== BOUNDARY CONDITION TESTS ==========

    @Test
    void testClockIn_ExactlyAtMidnight_Success() {
        // Arrange
        LocalDateTime midnight = LocalDateTime.now().toLocalDate().atStartOfDay();
        testAttendanceDto.setClockIn(midnight);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(attendanceRepository.findActiveClockInByEmployee(1L)).thenReturn(Optional.empty());
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(testAttendance);

        // Act
        AttendanceDto result = attendanceService.clockIn(testAttendanceDto);

        // Assert
        assertNotNull(result);
    }

    @Test
    void testClockOut_MinimalWorkTime_Success() {
        // Arrange
        testAttendance.setClockIn(LocalDateTime.now().minusMinutes(1));
        testAttendance.setClockOut(null);
        when(attendanceRepository.findActiveClockInByEmployee(1L)).thenReturn(Optional.of(testAttendance));
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(testAttendance);

        // Act
        AttendanceDto result = attendanceService.clockOut(1L);

        // Assert
        assertNotNull(result);
        assertTrue(result.getHoursWorked() > 0);
    }

    @Test
    void testClockOut_MaximumAllowedHours_Success() {
        // Arrange
        testAttendance.setClockIn(LocalDateTime.now().minusHours(24));
        testAttendance.setClockOut(null);
        when(attendanceRepository.findActiveClockInByEmployee(1L)).thenReturn(Optional.of(testAttendance));
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(testAttendance);

        // Act
        AttendanceDto result = attendanceService.clockOut(1L);

        // Assert
        assertNotNull(result);
        assertTrue(result.getHoursWorked() <= 24.0);
    }

    @Test
    void testGetAttendanceByEmployee_MultipleRecords_ReturnsAll() {
        // Arrange
        Attendance attendance2 = new Attendance();
        attendance2.setId(2L);
        attendance2.setEmployee(testEmployee);
        List<Attendance> attendanceList = Arrays.asList(testAttendance, attendance2);
        when(attendanceRepository.findByEmployeeId(1L)).thenReturn(attendanceList);

        // Act
        List<AttendanceDto> result = attendanceService.getAttendanceByEmployee(1L);

        // Assert
        assertEquals(2, result.size());
    }

    @Test
    void testClockIn_WithNullDeviceInfo_Success() {
        // Arrange
        testAttendanceDto.setDeviceInfo(null);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(attendanceRepository.findActiveClockInByEmployee(1L)).thenReturn(Optional.empty());
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(testAttendance);

        // Act
        AttendanceDto result = attendanceService.clockIn(testAttendanceDto);

        // Assert
        assertNotNull(result);
    }

    @Test
    void testClockIn_WithEmptyDeviceInfo_Success() {
        // Arrange
        testAttendanceDto.setDeviceInfo("");
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(attendanceRepository.findActiveClockInByEmployee(1L)).thenReturn(Optional.empty());
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(testAttendance);

        // Act
        AttendanceDto result = attendanceService.clockIn(testAttendanceDto);

        // Assert
        assertNotNull(result);
    }
}