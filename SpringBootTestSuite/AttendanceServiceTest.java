package com.wms.ems.attendance;

import com.wms.ems.employee.Employee;
import com.wms.ems.employee.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Comprehensive JUnit test suite for AttendanceService.
 * Tests cover clock-in/out operations, hours calculation, geofence validation,
 * missed punch corrections, and edge cases.
 * 
 * @author Warehouse EMS Test Suite
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
public class AttendanceServiceTest {

    @Mock
    private AttendanceEventRepository attendanceEventRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private GeofenceService geofenceService;

    @InjectMocks
    private AttendanceService attendanceService;

    private Employee testEmployee;
    private AttendanceEvent clockInEvent;
    private AttendanceEvent clockOutEvent;
    private ClockEventDto clockInDto;
    private ClockEventDto clockOutDto;

    @BeforeEach
    public void setUp() {
        // Arrange: Create test employee
        testEmployee = new Employee();
        testEmployee.setId(1L);
        testEmployee.setBadgeId("EMP001");
        testEmployee.setName("John Doe");
        testEmployee.setStatus("ACTIVE");

        // Arrange: Create clock-in event
        clockInEvent = new AttendanceEvent();
        clockInEvent.setId(1L);
        clockInEvent.setEmployeeId(1L);
        clockInEvent.setTimestamp(LocalDateTime.now().minusHours(8));
        clockInEvent.setType("IN");
        clockInEvent.setDeviceId("DEVICE001");
        clockInEvent.setLocation("40.7128,-74.0060");
        clockInEvent.setShiftId(1L);

        // Arrange: Create clock-out event
        clockOutEvent = new AttendanceEvent();
        clockOutEvent.setId(2L);
        clockOutEvent.setEmployeeId(1L);
        clockOutEvent.setTimestamp(LocalDateTime.now());
        clockOutEvent.setType("OUT");
        clockOutEvent.setDeviceId("DEVICE001");
        clockOutEvent.setLocation("40.7128,-74.0060");
        clockOutEvent.setShiftId(1L);

        // Arrange: Create clock-in DTO
        clockInDto = new ClockEventDto();
        clockInDto.setEmployeeId(1L);
        clockInDto.setDeviceId("DEVICE001");
        clockInDto.setLocation("40.7128,-74.0060");
        clockInDto.setShiftId(1L);

        // Arrange: Create clock-out DTO
        clockOutDto = new ClockEventDto();
        clockOutDto.setEmployeeId(1L);
        clockOutDto.setDeviceId("DEVICE001");
        clockOutDto.setLocation("40.7128,-74.0060");
        clockOutDto.setShiftId(1L);
    }

    // ==================== CLOCK-IN TESTS ====================

    @Test
    public void testClockIn_ValidInput_Success() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(geofenceService.validateLocation(anyString())).thenReturn(true);
        when(attendanceEventRepository.findLastEventByEmployeeId(1L)).thenReturn(Optional.empty());
        when(attendanceEventRepository.save(any(AttendanceEvent.class))).thenReturn(clockInEvent);

        // Act
        AttendanceEventDto result = attendanceService.clockIn(clockInDto);

        // Assert
        assertNotNull(result);
        assertEquals("IN", result.getType());
        assertEquals(1L, result.getEmployeeId());
        verify(attendanceEventRepository, times(1)).save(any(AttendanceEvent.class));
    }

    @Test
    public void testClockIn_NullDto_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.clockIn(null);
        });
    }

    @Test
    public void testClockIn_NullEmployeeId_ThrowsException() {
        // Arrange
        clockInDto.setEmployeeId(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.clockIn(clockInDto);
        });
    }

    @Test
    public void testClockIn_InvalidEmployeeId_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());
        clockInDto.setEmployeeId(999L);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.clockIn(clockInDto);
        });
    }

    @Test
    public void testClockIn_InactiveEmployee_ThrowsException() {
        // Arrange
        testEmployee.setStatus("INACTIVE");
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            attendanceService.clockIn(clockInDto);
        });
    }

    @Test
    public void testClockIn_AlreadyClockedIn_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(attendanceEventRepository.findLastEventByEmployeeId(1L)).thenReturn(Optional.of(clockInEvent));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            attendanceService.clockIn(clockInDto);
        });
    }

    @Test
    public void testClockIn_InvalidGeofence_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(geofenceService.validateLocation(anyString())).thenReturn(false);
        when(attendanceEventRepository.findLastEventByEmployeeId(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            attendanceService.clockIn(clockInDto);
        });
    }

    @Test
    public void testClockIn_NullLocation_ThrowsException() {
        // Arrange
        clockInDto.setLocation(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.clockIn(clockInDto);
        });
    }

    @Test
    public void testClockIn_EmptyLocation_ThrowsException() {
        // Arrange
        clockInDto.setLocation("");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.clockIn(clockInDto);
        });
    }

    @Test
    public void testClockIn_InvalidLocationFormat_ThrowsException() {
        // Arrange
        clockInDto.setLocation("invalid-location");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.clockIn(clockInDto);
        });
    }

    // ==================== CLOCK-OUT TESTS ====================

    @Test
    public void testClockOut_ValidInput_Success() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(geofenceService.validateLocation(anyString())).thenReturn(true);
        when(attendanceEventRepository.findLastEventByEmployeeId(1L)).thenReturn(Optional.of(clockInEvent));
        when(attendanceEventRepository.save(any(AttendanceEvent.class))).thenReturn(clockOutEvent);

        // Act
        AttendanceEventDto result = attendanceService.clockOut(clockOutDto);

        // Assert
        assertNotNull(result);
        assertEquals("OUT", result.getType());
        assertEquals(1L, result.getEmployeeId());
        verify(attendanceEventRepository, times(1)).save(any(AttendanceEvent.class));
    }

    @Test
    public void testClockOut_NullDto_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.clockOut(null);
        });
    }

    @Test
    public void testClockOut_NotClockedIn_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(attendanceEventRepository.findLastEventByEmployeeId(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            attendanceService.clockOut(clockOutDto);
        });
    }

    @Test
    public void testClockOut_AlreadyClockedOut_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(attendanceEventRepository.findLastEventByEmployeeId(1L)).thenReturn(Optional.of(clockOutEvent));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            attendanceService.clockOut(clockOutDto);
        });
    }

    // ==================== HOURS CALCULATION TESTS ====================

    @Test
    public void testCalculateHoursWorked_ValidShift_Success() {
        // Arrange
        when(attendanceEventRepository.findEventsByEmployeeIdAndDate(anyLong(), any()))
            .thenReturn(Arrays.asList(clockInEvent, clockOutEvent));

        // Act
        double hours = attendanceService.calculateHoursWorked(1L, LocalDateTime.now().toLocalDate());

        // Assert
        assertTrue(hours > 0);
        assertTrue(hours <= 24);
    }

    @Test
    public void testCalculateHoursWorked_NoEvents_ReturnsZero() {
        // Arrange
        when(attendanceEventRepository.findEventsByEmployeeIdAndDate(anyLong(), any()))
            .thenReturn(Arrays.asList());

        // Act
        double hours = attendanceService.calculateHoursWorked(1L, LocalDateTime.now().toLocalDate());

        // Assert
        assertEquals(0.0, hours);
    }

    @Test
    public void testCalculateHoursWorked_OddNumberOfEvents_ThrowsException() {
        // Arrange
        when(attendanceEventRepository.findEventsByEmployeeIdAndDate(anyLong(), any()))
            .thenReturn(Arrays.asList(clockInEvent));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            attendanceService.calculateHoursWorked(1L, LocalDateTime.now().toLocalDate());
        });
    }

    @Test
    public void testCalculateHoursWorked_OvernightShift_Success() {
        // Arrange
        clockInEvent.setTimestamp(LocalDateTime.now().minusHours(10));
        clockOutEvent.setTimestamp(LocalDateTime.now().plusHours(2));
        when(attendanceEventRepository.findEventsByEmployeeIdAndDate(anyLong(), any()))
            .thenReturn(Arrays.asList(clockInEvent, clockOutEvent));

        // Act
        double hours = attendanceService.calculateHoursWorked(1L, LocalDateTime.now().toLocalDate());

        // Assert
        assertTrue(hours > 10);
    }

    // ==================== MISSED PUNCH CORRECTION TESTS ====================

    @Test
    public void testRequestCorrection_ValidInput_Success() {
        // Arrange
        CorrectionDto correctionDto = new CorrectionDto();
        correctionDto.setEmployeeId(1L);
        correctionDto.setOriginalTimestamp(LocalDateTime.now().minusHours(8));
        correctionDto.setCorrectedTimestamp(LocalDateTime.now().minusHours(7));
        correctionDto.setReason("Forgot to clock in");
        correctionDto.setType("IN");

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(attendanceEventRepository.save(any(AttendanceEvent.class))).thenReturn(clockInEvent);

        // Act
        CorrectionRequestDto result = attendanceService.requestCorrection(correctionDto);

        // Assert
        assertNotNull(result);
        assertEquals("PENDING", result.getStatus());
    }

    @Test
    public void testRequestCorrection_NullDto_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.requestCorrection(null);
        });
    }

    @Test
    public void testRequestCorrection_NullReason_ThrowsException() {
        // Arrange
        CorrectionDto correctionDto = new CorrectionDto();
        correctionDto.setEmployeeId(1L);
        correctionDto.setOriginalTimestamp(LocalDateTime.now().minusHours(8));
        correctionDto.setCorrectedTimestamp(LocalDateTime.now().minusHours(7));
        correctionDto.setReason(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.requestCorrection(correctionDto);
        });
    }

    @Test
    public void testRequestCorrection_EmptyReason_ThrowsException() {
        // Arrange
        CorrectionDto correctionDto = new CorrectionDto();
        correctionDto.setEmployeeId(1L);
        correctionDto.setOriginalTimestamp(LocalDateTime.now().minusHours(8));
        correctionDto.setCorrectedTimestamp(LocalDateTime.now().minusHours(7));
        correctionDto.setReason("");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.requestCorrection(correctionDto);
        });
    }

    @Test
    public void testRequestCorrection_FutureTimestamp_ThrowsException() {
        // Arrange
        CorrectionDto correctionDto = new CorrectionDto();
        correctionDto.setEmployeeId(1L);
        correctionDto.setOriginalTimestamp(LocalDateTime.now().plusHours(1));
        correctionDto.setCorrectedTimestamp(LocalDateTime.now().plusHours(2));
        correctionDto.setReason("Test");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.requestCorrection(correctionDto);
        });
    }

    @Test
    public void testApproveCorrection_ValidInput_Success() {
        // Arrange
        when(attendanceEventRepository.findById(1L)).thenReturn(Optional.of(clockInEvent));
        when(attendanceEventRepository.save(any(AttendanceEvent.class))).thenReturn(clockInEvent);

        // Act
        attendanceService.approveCorrection(1L, 2L);

        // Assert
        verify(attendanceEventRepository, times(1)).save(any(AttendanceEvent.class));
    }

    @Test
    public void testDenyCorrection_ValidInput_Success() {
        // Arrange
        when(attendanceEventRepository.findById(1L)).thenReturn(Optional.of(clockInEvent));

        // Act
        attendanceService.denyCorrection(1L, 2L, "Invalid reason");

        // Assert
        verify(attendanceEventRepository, times(1)).delete(any(AttendanceEvent.class));
    }

    // ==================== EXPORT TESTS ====================

    @Test
    public void testExportAttendance_ValidDateRange_Success() {
        // Arrange
        when(attendanceEventRepository.findEventsByDateRange(any(), any()))
            .thenReturn(Arrays.asList(clockInEvent, clockOutEvent));

        // Act
        byte[] csvData = attendanceService.exportAttendance(LocalDateTime.now().minusDays(7), LocalDateTime.now());

        // Assert
        assertNotNull(csvData);
        assertTrue(csvData.length > 0);
    }

    @Test
    public void testExportAttendance_InvalidDateRange_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.exportAttendance(LocalDateTime.now(), LocalDateTime.now().minusDays(7));
        });
    }
}