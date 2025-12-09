package com.warehouse.ems.attendance.service;

import com.warehouse.ems.attendance.entity.Attendance;
import com.warehouse.ems.attendance.repository.AttendanceRepository;
import com.warehouse.ems.employee.entity.Employee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for AttendanceService.
 * Tests cover clock in/out operations, validation, and edge cases.
 */
@ExtendWith(MockitoExtension.class)
public class AttendanceServiceTest {

    @Mock
    private AttendanceRepository attendanceRepository;

    @InjectMocks
    private AttendanceService attendanceService;

    private Attendance testAttendance;
    private Employee testEmployee;

    @BeforeEach
    public void setUp() {
        testEmployee = new Employee();
        testEmployee.setId(1L);
        testEmployee.setBadgeId("EMP001");
        testEmployee.setName("John Doe");

        testAttendance = new Attendance();
        testAttendance.setId(1L);
        testAttendance.setEmployee(testEmployee);
        testAttendance.setDeviceId("DEVICE001");
        testAttendance.setGeofence("WAREHOUSE_A");
        testAttendance.setCorrectionRequested(false);
    }

    // ========== CLOCK IN TESTS ==========

    @Test
    public void testClockIn_Success() {
        // Arrange
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(testAttendance);

        // Act
        Attendance result = attendanceService.clockIn(testAttendance);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getClockIn());
        assertEquals("CLOCKED_IN", result.getStatus());
        assertEquals("DEVICE001", result.getDeviceId());
        verify(attendanceRepository, times(1)).save(testAttendance);
    }

    @Test
    public void testClockIn_WithGeofence() {
        // Arrange
        testAttendance.setGeofence("WAREHOUSE_B");
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(testAttendance);

        // Act
        Attendance result = attendanceService.clockIn(testAttendance);

        // Assert
        assertNotNull(result);
        assertEquals("WAREHOUSE_B", result.getGeofence());
        assertEquals("CLOCKED_IN", result.getStatus());
        verify(attendanceRepository, times(1)).save(testAttendance);
    }

    @Test
    public void testClockIn_NullEmployee() {
        // Arrange
        testAttendance.setEmployee(null);
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(testAttendance);

        // Act
        Attendance result = attendanceService.clockIn(testAttendance);

        // Assert
        assertNotNull(result);
        assertNull(result.getEmployee());
        assertEquals("CLOCKED_IN", result.getStatus());
    }

    @Test
    public void testClockIn_NullDeviceId() {
        // Arrange
        testAttendance.setDeviceId(null);
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(testAttendance);

        // Act
        Attendance result = attendanceService.clockIn(testAttendance);

        // Assert
        assertNotNull(result);
        assertNull(result.getDeviceId());
        assertEquals("CLOCKED_IN", result.getStatus());
    }

    @Test
    public void testClockIn_EmptyGeofence() {
        // Arrange
        testAttendance.setGeofence("");
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(testAttendance);

        // Act
        Attendance result = attendanceService.clockIn(testAttendance);

        // Assert
        assertNotNull(result);
        assertEquals("", result.getGeofence());
        assertEquals("CLOCKED_IN", result.getStatus());
    }

    @Test
    public void testClockIn_MultipleTimesForSameEmployee() {
        // Arrange
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(testAttendance);

        // Act
        Attendance result1 = attendanceService.clockIn(testAttendance);
        Attendance result2 = attendanceService.clockIn(testAttendance);

        // Assert
        assertNotNull(result1);
        assertNotNull(result2);
        verify(attendanceRepository, times(2)).save(testAttendance);
    }

    // ========== CLOCK OUT TESTS ==========

    @Test
    public void testClockOut_Success() {
        // Arrange
        testAttendance.setClockIn(LocalDateTime.now().minusHours(8));
        testAttendance.setStatus("CLOCKED_IN");
        when(attendanceRepository.findById(1L)).thenReturn(Optional.of(testAttendance));
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(testAttendance);

        // Act
        Attendance result = attendanceService.clockOut(1L);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getClockOut());
        assertEquals("CLOCKED_OUT", result.getStatus());
        assertTrue(result.getClockOut().isAfter(result.getClockIn()));
        verify(attendanceRepository, times(1)).findById(1L);
        verify(attendanceRepository, times(1)).save(testAttendance);
    }

    @Test
    public void testClockOut_NotFound() {
        // Arrange
        when(attendanceRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            attendanceService.clockOut(999L);
        });
        assertTrue(exception.getMessage().contains("Attendance record not found"));
        verify(attendanceRepository, times(1)).findById(999L);
        verify(attendanceRepository, never()).save(any(Attendance.class));
    }

    @Test
    public void testClockOut_NullId() {
        // Arrange
        when(attendanceRepository.findById(null)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            attendanceService.clockOut(null);
        });
        assertTrue(exception.getMessage().contains("Attendance record not found"));
    }

    @Test
    public void testClockOut_AlreadyClockedOut() {
        // Arrange
        testAttendance.setClockIn(LocalDateTime.now().minusHours(8));
        testAttendance.setClockOut(LocalDateTime.now().minusHours(1));
        testAttendance.setStatus("CLOCKED_OUT");
        when(attendanceRepository.findById(1L)).thenReturn(Optional.of(testAttendance));
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(testAttendance);

        // Act
        Attendance result = attendanceService.clockOut(1L);

        // Assert
        assertNotNull(result);
        assertEquals("CLOCKED_OUT", result.getStatus());
        assertNotNull(result.getClockOut());
        verify(attendanceRepository, times(1)).save(testAttendance);
    }

    @Test
    public void testClockOut_SameDayClockOut() {
        // Arrange
        LocalDateTime clockInTime = LocalDateTime.now().minusHours(4);
        testAttendance.setClockIn(clockInTime);
        testAttendance.setStatus("CLOCKED_IN");
        when(attendanceRepository.findById(1L)).thenReturn(Optional.of(testAttendance));
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(testAttendance);

        // Act
        Attendance result = attendanceService.clockOut(1L);

        // Assert
        assertNotNull(result);
        assertEquals(clockInTime.toLocalDate(), result.getClockOut().toLocalDate());
        verify(attendanceRepository, times(1)).save(testAttendance);
    }

    @Test
    public void testClockOut_OvernightShift() {
        // Arrange
        LocalDateTime clockInTime = LocalDateTime.now().minusHours(12);
        testAttendance.setClockIn(clockInTime);
        testAttendance.setStatus("CLOCKED_IN");
        when(attendanceRepository.findById(1L)).thenReturn(Optional.of(testAttendance));
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(testAttendance);

        // Act
        Attendance result = attendanceService.clockOut(1L);

        // Assert
        assertNotNull(result);
        assertTrue(result.getClockOut().isAfter(result.getClockIn()));
        verify(attendanceRepository, times(1)).save(testAttendance);
    }

    // ========== GET ATTENDANCE BY EMPLOYEE TESTS ==========

    @Test
    public void testGetAttendanceByEmployee_Success() {
        // Arrange
        Attendance attendance2 = new Attendance();
        attendance2.setId(2L);
        attendance2.setEmployee(testEmployee);
        attendance2.setClockIn(LocalDateTime.now().minusDays(1));
        attendance2.setClockOut(LocalDateTime.now().minusDays(1).plusHours(8));

        List<Attendance> attendances = Arrays.asList(testAttendance, attendance2);
        when(attendanceRepository.findByEmployeeId(1L)).thenReturn(attendances);

        // Act
        List<Attendance> result = attendanceService.getAttendanceByEmployee(1L);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getEmployee().getId());
        assertEquals(1L, result.get(1).getEmployee().getId());
        verify(attendanceRepository, times(1)).findByEmployeeId(1L);
    }

    @Test
    public void testGetAttendanceByEmployee_EmptyList() {
        // Arrange
        when(attendanceRepository.findByEmployeeId(999L)).thenReturn(Arrays.asList());

        // Act
        List<Attendance> result = attendanceService.getAttendanceByEmployee(999L);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(attendanceRepository, times(1)).findByEmployeeId(999L);
    }

    @Test
    public void testGetAttendanceByEmployee_NullEmployeeId() {
        // Arrange
        when(attendanceRepository.findByEmployeeId(null)).thenReturn(Arrays.asList());

        // Act
        List<Attendance> result = attendanceService.getAttendanceByEmployee(null);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetAttendanceByEmployee_MultipleRecords() {
        // Arrange
        List<Attendance> attendances = Arrays.asList(
            createAttendance(1L, LocalDateTime.now().minusDays(5)),
            createAttendance(2L, LocalDateTime.now().minusDays(4)),
            createAttendance(3L, LocalDateTime.now().minusDays(3)),
            createAttendance(4L, LocalDateTime.now().minusDays(2)),
            createAttendance(5L, LocalDateTime.now().minusDays(1))
        );
        when(attendanceRepository.findByEmployeeId(1L)).thenReturn(attendances);

        // Act
        List<Attendance> result = attendanceService.getAttendanceByEmployee(1L);

        // Assert
        assertNotNull(result);
        assertEquals(5, result.size());
        verify(attendanceRepository, times(1)).findByEmployeeId(1L);
    }

    // ========== HELPER METHODS ==========

    private Attendance createAttendance(Long id, LocalDateTime clockIn) {
        Attendance attendance = new Attendance();
        attendance.setId(id);
        attendance.setEmployee(testEmployee);
        attendance.setClockIn(clockIn);
        attendance.setClockOut(clockIn.plusHours(8));
        attendance.setStatus("CLOCKED_OUT");
        return attendance;
    }

    // ========== EDGE CASE TESTS ==========

    @Test
    public void testClockIn_WithCorrectionRequested() {
        // Arrange
        testAttendance.setCorrectionRequested(true);
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(testAttendance);

        // Act
        Attendance result = attendanceService.clockIn(testAttendance);

        // Assert
        assertNotNull(result);
        assertTrue(result.getCorrectionRequested());
        assertEquals("CLOCKED_IN", result.getStatus());
    }

    @Test
    public void testClockOut_ImmediatelyAfterClockIn() {
        // Arrange
        testAttendance.setClockIn(LocalDateTime.now());
        testAttendance.setStatus("CLOCKED_IN");
        when(attendanceRepository.findById(1L)).thenReturn(Optional.of(testAttendance));
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(testAttendance);

        // Act
        Attendance result = attendanceService.clockOut(1L);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getClockOut());
        assertTrue(result.getClockOut().isAfter(result.getClockIn()) || 
                   result.getClockOut().isEqual(result.getClockIn()));
    }

    @Test
    public void testGetAttendanceByEmployee_WithMixedStatuses() {
        // Arrange
        Attendance clockedIn = new Attendance();
        clockedIn.setId(1L);
        clockedIn.setEmployee(testEmployee);
        clockedIn.setClockIn(LocalDateTime.now());
        clockedIn.setStatus("CLOCKED_IN");

        Attendance clockedOut = new Attendance();
        clockedOut.setId(2L);
        clockedOut.setEmployee(testEmployee);
        clockedOut.setClockIn(LocalDateTime.now().minusHours(8));
        clockedOut.setClockOut(LocalDateTime.now());
        clockedOut.setStatus("CLOCKED_OUT");

        when(attendanceRepository.findByEmployeeId(1L)).thenReturn(Arrays.asList(clockedIn, clockedOut));

        // Act
        List<Attendance> result = attendanceService.getAttendanceByEmployee(1L);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("CLOCKED_IN", result.get(0).getStatus());
        assertEquals("CLOCKED_OUT", result.get(1).getStatus());
    }
}