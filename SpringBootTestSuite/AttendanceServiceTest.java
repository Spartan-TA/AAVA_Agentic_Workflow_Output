package com.warehouse.ems.service;

import com.warehouse.ems.entity.Attendance;
import com.warehouse.ems.entity.Employee;
import com.warehouse.ems.exception.GeofenceViolationException;
import com.warehouse.ems.exception.ResourceNotFoundException;
import com.warehouse.ems.repository.AttendanceRepository;
import com.warehouse.ems.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
 * Comprehensive JUnit test suite for AttendanceService
 * Tests cover clock in/out operations, geofence validation, and hours calculation
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
    private final double WAREHOUSE_LATITUDE = 40.7128;
    private final double WAREHOUSE_LONGITUDE = -74.0060;
    private final double GEOFENCE_RADIUS_METERS = 100.0;

    @BeforeEach
    public void setUp() {
        testEmployee = new Employee();
        testEmployee.setId(1L);
        testEmployee.setBadgeId("EMP001");
        testEmployee.setFirstName("John");
        testEmployee.setLastName("Doe");

        testAttendance = new Attendance();
        testAttendance.setId(1L);
        testAttendance.setEmployee(testEmployee);
        testAttendance.setClockInTime(LocalDateTime.now().minusHours(8));
        testAttendance.setClockInLatitude(WAREHOUSE_LATITUDE);
        testAttendance.setClockInLongitude(WAREHOUSE_LONGITUDE);
        testAttendance.setDevice("Mobile App");
    }

    // ========== CLOCK IN TESTS ==========

    @Test
    public void testClockIn_ValidLocationAndEmployee_Success() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(testAttendance);

        // Act
        Attendance result = attendanceService.clockIn(1L, WAREHOUSE_LATITUDE, WAREHOUSE_LONGITUDE, "Mobile App");

        // Assert
        assertNotNull(result);
        assertNotNull(result.getClockInTime());
        assertEquals(WAREHOUSE_LATITUDE, result.getClockInLatitude());
        assertEquals(WAREHOUSE_LONGITUDE, result.getClockInLongitude());
        verify(attendanceRepository, times(1)).save(any(Attendance.class));
    }

    @Test
    public void testClockIn_OutsideGeofence_ThrowsException() {
        // Arrange - Coordinates far from warehouse
        double farLatitude = 41.0;
        double farLongitude = -75.0;
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));

        // Act & Assert
        assertThrows(GeofenceViolationException.class, () -> {
            attendanceService.clockIn(1L, farLatitude, farLongitude, "Mobile App");
        });
        verify(attendanceRepository, never()).save(any(Attendance.class));
    }

    @Test
    public void testClockIn_NonExistentEmployee_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            attendanceService.clockIn(999L, WAREHOUSE_LATITUDE, WAREHOUSE_LONGITUDE, "Mobile App");
        });
    }

    @Test
    public void testClockIn_NullEmployeeId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.clockIn(null, WAREHOUSE_LATITUDE, WAREHOUSE_LONGITUDE, "Mobile App");
        });
    }

    @Test
    public void testClockIn_InvalidLatitude_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.clockIn(1L, 91.0, WAREHOUSE_LONGITUDE, "Mobile App"); // Latitude > 90
        });
    }

    @Test
    public void testClockIn_InvalidLongitude_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.clockIn(1L, WAREHOUSE_LATITUDE, 181.0, "Mobile App"); // Longitude > 180
        });
    }

    @Test
    public void testClockIn_NullDevice_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.clockIn(1L, WAREHOUSE_LATITUDE, WAREHOUSE_LONGITUDE, null);
        });
    }

    @Test
    public void testClockIn_EmptyDevice_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.clockIn(1L, WAREHOUSE_LATITUDE, WAREHOUSE_LONGITUDE, "");
        });
    }

    @Test
    public void testClockIn_AlreadyClockedIn_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(attendanceRepository.findByEmployeeIdAndClockOutTimeIsNull(1L))
                .thenReturn(Optional.of(testAttendance));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            attendanceService.clockIn(1L, WAREHOUSE_LATITUDE, WAREHOUSE_LONGITUDE, "Mobile App");
        });
    }

    @Test
    public void testClockIn_EdgeOfGeofence_Success() {
        // Arrange - Coordinates at edge of geofence (within radius)
        double edgeLatitude = WAREHOUSE_LATITUDE + 0.0009; // ~100 meters
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(testAttendance);

        // Act
        Attendance result = attendanceService.clockIn(1L, edgeLatitude, WAREHOUSE_LONGITUDE, "Mobile App");

        // Assert
        assertNotNull(result);
        verify(attendanceRepository, times(1)).save(any(Attendance.class));
    }

    // ========== CLOCK OUT TESTS ==========

    @Test
    public void testClockOut_ValidLocationAndEmployee_Success() {
        // Arrange
        when(attendanceRepository.findByEmployeeIdAndClockOutTimeIsNull(1L))
                .thenReturn(Optional.of(testAttendance));
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(testAttendance);

        // Act
        Attendance result = attendanceService.clockOut(1L, WAREHOUSE_LATITUDE, WAREHOUSE_LONGITUDE);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getClockOutTime());
        assertEquals(WAREHOUSE_LATITUDE, result.getClockOutLatitude());
        assertEquals(WAREHOUSE_LONGITUDE, result.getClockOutLongitude());
        verify(attendanceRepository, times(1)).save(any(Attendance.class));
    }

    @Test
    public void testClockOut_OutsideGeofence_ThrowsException() {
        // Arrange
        double farLatitude = 41.0;
        double farLongitude = -75.0;
        when(attendanceRepository.findByEmployeeIdAndClockOutTimeIsNull(1L))
                .thenReturn(Optional.of(testAttendance));

        // Act & Assert
        assertThrows(GeofenceViolationException.class, () -> {
            attendanceService.clockOut(1L, farLatitude, farLongitude);
        });
    }

    @Test
    public void testClockOut_NotClockedIn_ThrowsException() {
        // Arrange
        when(attendanceRepository.findByEmployeeIdAndClockOutTimeIsNull(1L))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            attendanceService.clockOut(1L, WAREHOUSE_LATITUDE, WAREHOUSE_LONGITUDE);
        });
    }

    @Test
    public void testClockOut_NullEmployeeId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.clockOut(null, WAREHOUSE_LATITUDE, WAREHOUSE_LONGITUDE);
        });
    }

    @Test
    public void testClockOut_InvalidLatitude_ThrowsException() {
        // Arrange
        when(attendanceRepository.findByEmployeeIdAndClockOutTimeIsNull(1L))
                .thenReturn(Optional.of(testAttendance));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.clockOut(1L, -91.0, WAREHOUSE_LONGITUDE);
        });
    }

    @Test
    public void testClockOut_InvalidLongitude_ThrowsException() {
        // Arrange
        when(attendanceRepository.findByEmployeeIdAndClockOutTimeIsNull(1L))
                .thenReturn(Optional.of(testAttendance));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.clockOut(1L, WAREHOUSE_LATITUDE, -181.0);
        });
    }

    // ========== CALCULATE HOURS WORKED TESTS ==========

    @Test
    public void testCalculateHoursWorked_ValidAttendance_ReturnsCorrectHours() {
        // Arrange
        testAttendance.setClockInTime(LocalDateTime.now().minusHours(8));
        testAttendance.setClockOutTime(LocalDateTime.now());

        // Act
        double hours = attendanceService.calculateHoursWorked(testAttendance);

        // Assert
        assertEquals(8.0, hours, 0.1); // Allow small delta for time precision
    }

    @Test
    public void testCalculateHoursWorked_PartialHour_ReturnsDecimalHours() {
        // Arrange
        testAttendance.setClockInTime(LocalDateTime.now().minusMinutes(90));
        testAttendance.setClockOutTime(LocalDateTime.now());

        // Act
        double hours = attendanceService.calculateHoursWorked(testAttendance);

        // Assert
        assertEquals(1.5, hours, 0.1);
    }

    @Test
    public void testCalculateHoursWorked_NoClockOut_ThrowsException() {
        // Arrange
        testAttendance.setClockOutTime(null);

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            attendanceService.calculateHoursWorked(testAttendance);
        });
    }

    @Test
    public void testCalculateHoursWorked_NullAttendance_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.calculateHoursWorked(null);
        });
    }

    @Test
    public void testCalculateHoursWorked_ZeroHours_ReturnsZero() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        testAttendance.setClockInTime(now);
        testAttendance.setClockOutTime(now);

        // Act
        double hours = attendanceService.calculateHoursWorked(testAttendance);

        // Assert
        assertEquals(0.0, hours, 0.01);
    }

    @Test
    public void testCalculateHoursWorked_OvertimeHours_ReturnsCorrectHours() {
        // Arrange - 12 hour shift
        testAttendance.setClockInTime(LocalDateTime.now().minusHours(12));
        testAttendance.setClockOutTime(LocalDateTime.now());

        // Act
        double hours = attendanceService.calculateHoursWorked(testAttendance);

        // Assert
        assertEquals(12.0, hours, 0.1);
        assertTrue(hours > 8.0); // Overtime threshold
    }

    // ========== MISSED PUNCH CORRECTION TESTS ==========

    @Test
    public void testCorrectMissedPunch_ValidCorrection_Success() {
        // Arrange
        when(attendanceRepository.findById(1L)).thenReturn(Optional.of(testAttendance));
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(testAttendance);
        LocalDateTime correctedTime = LocalDateTime.now().minusHours(4);

        // Act
        Attendance result = attendanceService.correctMissedPunch(1L, correctedTime, "CLOCK_OUT");

        // Assert
        assertNotNull(result);
        assertEquals(correctedTime, result.getClockOutTime());
        verify(attendanceRepository, times(1)).save(any(Attendance.class));
    }

    @Test
    public void testCorrectMissedPunch_NonExistentAttendance_ThrowsException() {
        // Arrange
        when(attendanceRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            attendanceService.correctMissedPunch(999L, LocalDateTime.now(), "CLOCK_OUT");
        });
    }

    @Test
    public void testCorrectMissedPunch_NullAttendanceId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.correctMissedPunch(null, LocalDateTime.now(), "CLOCK_OUT");
        });
    }

    @Test
    public void testCorrectMissedPunch_NullCorrectedTime_ThrowsException() {
        // Arrange
        when(attendanceRepository.findById(1L)).thenReturn(Optional.of(testAttendance));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.correctMissedPunch(1L, null, "CLOCK_OUT");
        });
    }

    @Test
    public void testCorrectMissedPunch_InvalidPunchType_ThrowsException() {
        // Arrange
        when(attendanceRepository.findById(1L)).thenReturn(Optional.of(testAttendance));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.correctMissedPunch(1L, LocalDateTime.now(), "INVALID");
        });
    }

    @Test
    public void testCorrectMissedPunch_FutureTime_ThrowsException() {
        // Arrange
        when(attendanceRepository.findById(1L)).thenReturn(Optional.of(testAttendance));
        LocalDateTime futureTime = LocalDateTime.now().plusHours(1);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.correctMissedPunch(1L, futureTime, "CLOCK_OUT");
        });
    }

    // ========== GET ATTENDANCE RECORDS TESTS ==========

    @Test
    public void testGetAttendanceByEmployee_ValidEmployee_ReturnsRecords() {
        // Arrange
        List<Attendance> attendances = Arrays.asList(testAttendance);
        when(attendanceRepository.findByEmployeeId(1L)).thenReturn(attendances);

        // Act
        List<Attendance> result = attendanceService.getAttendanceByEmployee(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testEmployee.getId(), result.get(0).getEmployee().getId());
    }

    @Test
    public void testGetAttendanceByEmployee_NoRecords_ReturnsEmptyList() {
        // Arrange
        when(attendanceRepository.findByEmployeeId(1L)).thenReturn(Arrays.asList());

        // Act
        List<Attendance> result = attendanceService.getAttendanceByEmployee(1L);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetAttendanceByEmployee_NullEmployeeId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.getAttendanceByEmployee(null);
        });
    }

    // ========== GEOFENCE VALIDATION TESTS ==========

    @Test
    public void testValidateGeofence_WithinRadius_ReturnsTrue() {
        // Arrange
        double nearbyLatitude = WAREHOUSE_LATITUDE + 0.0005; // ~50 meters
        double nearbyLongitude = WAREHOUSE_LONGITUDE + 0.0005;

        // Act
        boolean isValid = attendanceService.validateGeofence(nearbyLatitude, nearbyLongitude);

        // Assert
        assertTrue(isValid);
    }

    @Test
    public void testValidateGeofence_OutsideRadius_ReturnsFalse() {
        // Arrange
        double farLatitude = WAREHOUSE_LATITUDE + 0.01; // ~1000 meters
        double farLongitude = WAREHOUSE_LONGITUDE + 0.01;

        // Act
        boolean isValid = attendanceService.validateGeofence(farLatitude, farLongitude);

        // Assert
        assertFalse(isValid);
    }

    @Test
    public void testValidateGeofence_ExactWarehouseLocation_ReturnsTrue() {
        // Act
        boolean isValid = attendanceService.validateGeofence(WAREHOUSE_LATITUDE, WAREHOUSE_LONGITUDE);

        // Assert
        assertTrue(isValid);
    }
}