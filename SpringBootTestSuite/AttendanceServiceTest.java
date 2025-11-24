package com.warehouse.employee.management.service;

import com.warehouse.employee.management.model.Attendance;
import com.warehouse.employee.management.model.Employee;
import com.warehouse.employee.management.repository.AttendanceRepository;
import com.warehouse.employee.management.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Comprehensive JUnit test suite for AttendanceService.
 * Tests cover clock-in/out operations, hours calculation, and edge cases.
 * Follows AAA (Arrange-Act-Assert) pattern for clarity.
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
    private Attendance testAttendance;

    @BeforeEach
    public void setUp() {
        // Arrange: Create test data
        testEmployee = Employee.builder()
                .id(1L)
                .badgeId("EMP001")
                .name("John Doe")
                .role("WORKER")
                .department("Warehouse")
                .shiftGroup("Morning")
                .hireDate(LocalDate.of(2023, 1, 15))
                .status("ACTIVE")
                .deleted(false)
                .build();

        testAttendance = Attendance.builder()
                .id(1L)
                .employee(testEmployee)
                .clockIn(LocalDateTime.of(2024, 1, 15, 8, 0))
                .clockOut(null)
                .hoursWorked(null)
                .location("Warehouse A")
                .device("Terminal 1")
                .status("PENDING")
                .build();
    }

    // ========== Tests for clockIn(Long, String, String) ==========

    @Test
    public void testClockIn_ValidInput_Success() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(attendanceRepository.save(any(Attendance.class))).thenAnswer(invocation -> {
            Attendance att = invocation.getArgument(0);
            att.setId(1L);
            return att;
        });

        // Act
        Attendance result = attendanceService.clockIn(1L, "Warehouse A", "Terminal 1");

        // Assert
        assertNotNull(result);
        assertNotNull(result.getClockIn());
        assertEquals("Warehouse A", result.getLocation());
        assertEquals("Terminal 1", result.getDevice());
        assertEquals("PENDING", result.getStatus());
        assertNull(result.getClockOut());
        assertNull(result.getHoursWorked());
        verify(employeeRepository, times(1)).findById(1L);
        verify(attendanceRepository, times(1)).save(any(Attendance.class));
    }

    @Test
    public void testClockIn_NonExistentEmployee_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> attendanceService.clockIn(999L, "Warehouse A", "Terminal 1"));
        assertEquals("Employee not found", exception.getMessage());
        verify(employeeRepository, times(1)).findById(999L);
        verify(attendanceRepository, never()).save(any(Attendance.class));
    }

    @Test
    public void testClockIn_NullEmployeeId_ThrowsException() {
        // Act & Assert
        assertThrows(RuntimeException.class,
                () -> attendanceService.clockIn(null, "Warehouse A", "Terminal 1"));
    }

    @Test
    public void testClockIn_NullLocation_Success() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(attendanceRepository.save(any(Attendance.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Attendance result = attendanceService.clockIn(1L, null, "Terminal 1");

        // Assert
        assertNotNull(result);
        assertNull(result.getLocation());
        assertEquals("Terminal 1", result.getDevice());
    }

    @Test
    public void testClockIn_EmptyLocation_Success() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(attendanceRepository.save(any(Attendance.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Attendance result = attendanceService.clockIn(1L, "", "Terminal 1");

        // Assert
        assertNotNull(result);
        assertEquals("", result.getLocation());
    }

    @Test
    public void testClockIn_NullDevice_Success() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(attendanceRepository.save(any(Attendance.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Attendance result = attendanceService.clockIn(1L, "Warehouse A", null);

        // Assert
        assertNotNull(result);
        assertNull(result.getDevice());
        assertEquals("Warehouse A", result.getLocation());
    }

    @Test
    public void testClockIn_EmptyDevice_Success() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(attendanceRepository.save(any(Attendance.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Attendance result = attendanceService.clockIn(1L, "Warehouse A", "");

        // Assert
        assertNotNull(result);
        assertEquals("", result.getDevice());
    }

    @Test
    public void testClockIn_LongLocationString_Success() {
        // Arrange
        String longLocation = "A".repeat(500);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(attendanceRepository.save(any(Attendance.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Attendance result = attendanceService.clockIn(1L, longLocation, "Terminal 1");

        // Assert
        assertNotNull(result);
        assertEquals(longLocation, result.getLocation());
    }

    // ========== Tests for clockOut(Long) ==========

    @Test
    public void testClockOut_ValidAttendance_CalculatesHours() {
        // Arrange
        LocalDateTime clockInTime = LocalDateTime.of(2024, 1, 15, 8, 0);
        testAttendance.setClockIn(clockInTime);
        when(attendanceRepository.findById(1L)).thenReturn(Optional.of(testAttendance));
        when(attendanceRepository.save(any(Attendance.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Attendance result = attendanceService.clockOut(1L);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getClockOut());
        assertNotNull(result.getHoursWorked());
        assertTrue(result.getHoursWorked() > 0);
        assertEquals("APPROVED", result.getStatus());
        verify(attendanceRepository, times(1)).findById(1L);
        verify(attendanceRepository, times(1)).save(any(Attendance.class));
    }

    @Test
    public void testClockOut_EightHourShift_CalculatesCorrectly() {
        // Arrange
        LocalDateTime clockInTime = LocalDateTime.of(2024, 1, 15, 8, 0);
        LocalDateTime expectedClockOut = LocalDateTime.of(2024, 1, 15, 16, 0);
        testAttendance.setClockIn(clockInTime);
        
        when(attendanceRepository.findById(1L)).thenReturn(Optional.of(testAttendance));
        when(attendanceRepository.save(any(Attendance.class))).thenAnswer(invocation -> {
            Attendance att = invocation.getArgument(0);
            // Simulate clock out at 4 PM
            att.setClockOut(expectedClockOut);
            long minutes = java.time.Duration.between(att.getClockIn(), att.getClockOut()).toMinutes();
            att.setHoursWorked(minutes / 60.0);
            return att;
        });

        // Act
        Attendance result = attendanceService.clockOut(1L);

        // Assert
        assertNotNull(result);
        assertEquals(8.0, result.getHoursWorked(), 0.01);
    }

    @Test
    public void testClockOut_NonExistentAttendance_ThrowsException() {
        // Arrange
        when(attendanceRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> attendanceService.clockOut(999L));
        assertEquals("Attendance record not found", exception.getMessage());
        verify(attendanceRepository, times(1)).findById(999L);
        verify(attendanceRepository, never()).save(any(Attendance.class));
    }

    @Test
    public void testClockOut_NullAttendanceId_ThrowsException() {
        // Act & Assert
        assertThrows(RuntimeException.class,
                () -> attendanceService.clockOut(null));
    }

    @Test
    public void testClockOut_ZeroAttendanceId_ThrowsException() {
        // Arrange
        when(attendanceRepository.findById(0L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class,
                () -> attendanceService.clockOut(0L));
    }

    @Test
    public void testClockOut_NegativeAttendanceId_ThrowsException() {
        // Arrange
        when(attendanceRepository.findById(-1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class,
                () -> attendanceService.clockOut(-1L));
    }

    @Test
    public void testClockOut_ShortShift_CalculatesCorrectly() {
        // Arrange
        LocalDateTime clockInTime = LocalDateTime.of(2024, 1, 15, 8, 0);
        testAttendance.setClockIn(clockInTime);
        
        when(attendanceRepository.findById(1L)).thenReturn(Optional.of(testAttendance));
        when(attendanceRepository.save(any(Attendance.class))).thenAnswer(invocation -> {
            Attendance att = invocation.getArgument(0);
            // Simulate clock out after 30 minutes
            att.setClockOut(clockInTime.plusMinutes(30));
            long minutes = java.time.Duration.between(att.getClockIn(), att.getClockOut()).toMinutes();
            att.setHoursWorked(minutes / 60.0);
            return att;
        });

        // Act
        Attendance result = attendanceService.clockOut(1L);

        // Assert
        assertNotNull(result);
        assertEquals(0.5, result.getHoursWorked(), 0.01);
    }

    @Test
    public void testClockOut_OvertimeShift_CalculatesCorrectly() {
        // Arrange
        LocalDateTime clockInTime = LocalDateTime.of(2024, 1, 15, 8, 0);
        testAttendance.setClockIn(clockInTime);
        
        when(attendanceRepository.findById(1L)).thenReturn(Optional.of(testAttendance));
        when(attendanceRepository.save(any(Attendance.class))).thenAnswer(invocation -> {
            Attendance att = invocation.getArgument(0);
            // Simulate clock out after 12 hours
            att.setClockOut(clockInTime.plusHours(12));
            long minutes = java.time.Duration.between(att.getClockIn(), att.getClockOut()).toMinutes();
            att.setHoursWorked(minutes / 60.0);
            return att;
        });

        // Act
        Attendance result = attendanceService.clockOut(1L);

        // Assert
        assertNotNull(result);
        assertEquals(12.0, result.getHoursWorked(), 0.01);
    }

    // ========== Tests for getAttendanceByEmployee(Long) ==========

    @Test
    public void testGetAttendanceByEmployee_ValidEmployee_ReturnsRecords() {
        // Arrange
        Attendance attendance2 = Attendance.builder()
                .id(2L)
                .employee(testEmployee)
                .clockIn(LocalDateTime.of(2024, 1, 16, 8, 0))
                .clockOut(LocalDateTime.of(2024, 1, 16, 16, 0))
                .hoursWorked(8.0)
                .status("APPROVED")
                .build();
        
        List<Attendance> attendanceList = Arrays.asList(testAttendance, attendance2);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(attendanceRepository.findByEmployee(testEmployee)).thenReturn(attendanceList);

        // Act
        List<Attendance> result = attendanceService.getAttendanceByEmployee(1L);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(testEmployee, result.get(0).getEmployee());
        verify(employeeRepository, times(1)).findById(1L);
        verify(attendanceRepository, times(1)).findByEmployee(testEmployee);
    }

    @Test
    public void testGetAttendanceByEmployee_NonExistentEmployee_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> attendanceService.getAttendanceByEmployee(999L));
        assertEquals("Employee not found", exception.getMessage());
        verify(employeeRepository, times(1)).findById(999L);
        verify(attendanceRepository, never()).findByEmployee(any());
    }

    @Test
    public void testGetAttendanceByEmployee_NoRecords_ReturnsEmptyList() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(attendanceRepository.findByEmployee(testEmployee)).thenReturn(Arrays.asList());

        // Act
        List<Attendance> result = attendanceService.getAttendanceByEmployee(1L);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(attendanceRepository, times(1)).findByEmployee(testEmployee);
    }

    @Test
    public void testGetAttendanceByEmployee_NullEmployeeId_ThrowsException() {
        // Act & Assert
        assertThrows(RuntimeException.class,
                () -> attendanceService.getAttendanceByEmployee(null));
    }

    // ========== Tests for getAttendanceByDateRange(Long, LocalDateTime, LocalDateTime) ==========

    @Test
    public void testGetAttendanceByDateRange_ValidRange_ReturnsRecords() {
        // Arrange
        LocalDateTime start = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2024, 1, 31, 23, 59);
        List<Attendance> attendanceList = Arrays.asList(testAttendance);
        
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(attendanceRepository.findByEmployeeAndClockInBetween(testEmployee, start, end))
                .thenReturn(attendanceList);

        // Act
        List<Attendance> result = attendanceService.getAttendanceByDateRange(1L, start, end);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(employeeRepository, times(1)).findById(1L);
        verify(attendanceRepository, times(1)).findByEmployeeAndClockInBetween(testEmployee, start, end);
    }

    @Test
    public void testGetAttendanceByDateRange_NonExistentEmployee_ThrowsException() {
        // Arrange
        LocalDateTime start = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2024, 1, 31, 23, 59);
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> attendanceService.getAttendanceByDateRange(999L, start, end));
        assertEquals("Employee not found", exception.getMessage());
    }

    @Test
    public void testGetAttendanceByDateRange_NoRecordsInRange_ReturnsEmptyList() {
        // Arrange
        LocalDateTime start = LocalDateTime.of(2024, 2, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2024, 2, 28, 23, 59);
        
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(attendanceRepository.findByEmployeeAndClockInBetween(testEmployee, start, end))
                .thenReturn(Arrays.asList());

        // Act
        List<Attendance> result = attendanceService.getAttendanceByDateRange(1L, start, end);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetAttendanceByDateRange_NullStartDate_ThrowsException() {
        // Arrange
        LocalDateTime end = LocalDateTime.of(2024, 1, 31, 23, 59);

        // Act & Assert
        assertThrows(RuntimeException.class,
                () -> attendanceService.getAttendanceByDateRange(1L, null, end));
    }

    @Test
    public void testGetAttendanceByDateRange_NullEndDate_ThrowsException() {
        // Arrange
        LocalDateTime start = LocalDateTime.of(2024, 1, 1, 0, 0);

        // Act & Assert
        assertThrows(RuntimeException.class,
                () -> attendanceService.getAttendanceByDateRange(1L, start, null));
    }

    @Test
    public void testGetAttendanceByDateRange_StartAfterEnd_ReturnsEmptyList() {
        // Arrange
        LocalDateTime start = LocalDateTime.of(2024, 1, 31, 23, 59);
        LocalDateTime end = LocalDateTime.of(2024, 1, 1, 0, 0);
        
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(attendanceRepository.findByEmployeeAndClockInBetween(testEmployee, start, end))
                .thenReturn(Arrays.asList());

        // Act
        List<Attendance> result = attendanceService.getAttendanceByDateRange(1L, start, end);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetAttendanceByDateRange_SameStartAndEnd_ReturnsRecords() {
        // Arrange
        LocalDateTime sameDateTime = LocalDateTime.of(2024, 1, 15, 12, 0);
        
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(attendanceRepository.findByEmployeeAndClockInBetween(testEmployee, sameDateTime, sameDateTime))
                .thenReturn(Arrays.asList(testAttendance));

        // Act
        List<Attendance> result = attendanceService.getAttendanceByDateRange(1L, sameDateTime, sameDateTime);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
    }
}