package com.warehouse.employee.service;

import com.warehouse.employee.domain.AttendanceEvent;
import com.warehouse.employee.domain.AttendanceType;
import com.warehouse.employee.domain.Employee;
import com.warehouse.employee.dto.AttendanceEventDTO;
import com.warehouse.employee.dto.ClockEventRequest;
import com.warehouse.employee.repository.AttendanceEventRepository;
import com.warehouse.employee.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Comprehensive JUnit test suite for AttendanceService
 * Tests cover clock-in/out operations, validation, geofence, and edge cases
 */
@DisplayName("Attendance Service Tests")
public class AttendanceServiceTest {

    @Mock
    private AttendanceEventRepository attendanceEventRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private AttendanceService attendanceService;

    private Employee testEmployee;
    private AttendanceEvent testClockInEvent;
    private AttendanceEvent testClockOutEvent;
    private ClockEventRequest testClockRequest;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Setup test employee
        testEmployee = new Employee();
        testEmployee.setId(1L);
        testEmployee.setBadgeId("EMP001");
        testEmployee.setName("John Doe");

        // Setup clock-in event
        testClockInEvent = new AttendanceEvent();
        testClockInEvent.setId(1L);
        testClockInEvent.setEmployee(testEmployee);
        testClockInEvent.setType(AttendanceType.CLOCK_IN);
        testClockInEvent.setTimestamp(LocalDateTime.now());
        testClockInEvent.setDeviceId("DEVICE001");
        testClockInEvent.setGeoLocation("40.7128,-74.0060");

        // Setup clock-out event
        testClockOutEvent = new AttendanceEvent();
        testClockOutEvent.setId(2L);
        testClockOutEvent.setEmployee(testEmployee);
        testClockOutEvent.setType(AttendanceType.CLOCK_OUT);
        testClockOutEvent.setTimestamp(LocalDateTime.now().plusHours(8));
        testClockOutEvent.setDeviceId("DEVICE001");
        testClockOutEvent.setGeoLocation("40.7128,-74.0060");

        // Setup clock request
        testClockRequest = new ClockEventRequest();
        testClockRequest.setEmployeeId(1L);
        testClockRequest.setDeviceId("DEVICE001");
        testClockRequest.setGeoLocation("40.7128,-74.0060");
        testClockRequest.setTimestamp(LocalDateTime.now());
    }

    // ========== CLOCK-IN TESTS ==========

    @Test
    @DisplayName("Test clock-in with valid data - success")
    public void testClockIn_ValidData_Success() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(attendanceEventRepository.save(any(AttendanceEvent.class))).thenReturn(testClockInEvent);

        // Act
        AttendanceEventDTO result = attendanceService.clockIn(1L, testClockRequest);

        // Assert
        assertNotNull(result);
        assertEquals(AttendanceType.CLOCK_IN.toString(), result.getType());
        assertEquals("DEVICE001", result.getDeviceId());
        verify(attendanceEventRepository, times(1)).save(any(AttendanceEvent.class));
    }

    @Test
    @DisplayName("Test clock-in with null employee ID - throws exception")
    public void testClockIn_NullEmployeeId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.clockIn(null, testClockRequest);
        });
        verify(attendanceEventRepository, never()).save(any(AttendanceEvent.class));
    }

    @Test
    @DisplayName("Test clock-in with non-existent employee - throws exception")
    public void testClockIn_NonExistentEmployee_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.clockIn(999L, testClockRequest);
        });
    }

    @Test
    @DisplayName("Test clock-in with null request - throws exception")
    public void testClockIn_NullRequest_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.clockIn(1L, null);
        });
    }

    @Test
    @DisplayName("Test clock-in with null device ID - success with warning")
    public void testClockIn_NullDeviceId_Success() {
        // Arrange
        testClockRequest.setDeviceId(null);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(attendanceEventRepository.save(any(AttendanceEvent.class))).thenReturn(testClockInEvent);

        // Act
        AttendanceEventDTO result = attendanceService.clockIn(1L, testClockRequest);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Test clock-in with null geolocation - success with warning")
    public void testClockIn_NullGeoLocation_Success() {
        // Arrange
        testClockRequest.setGeoLocation(null);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(attendanceEventRepository.save(any(AttendanceEvent.class))).thenReturn(testClockInEvent);

        // Act
        AttendanceEventDTO result = attendanceService.clockIn(1L, testClockRequest);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Test clock-in with invalid geolocation format - throws exception")
    public void testClockIn_InvalidGeoLocation_ThrowsException() {
        // Arrange
        testClockRequest.setGeoLocation("invalid-format");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.clockIn(1L, testClockRequest);
        });
    }

    @Test
    @DisplayName("Test clock-in when already clocked in - throws exception")
    public void testClockIn_AlreadyClockedIn_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(attendanceEventRepository.findByEmployeeAndDate(any(), any()))
                .thenReturn(Arrays.asList(testClockInEvent));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            attendanceService.clockIn(1L, testClockRequest);
        });
    }

    @Test
    @DisplayName("Test clock-in with future timestamp - throws exception")
    public void testClockIn_FutureTimestamp_ThrowsException() {
        // Arrange
        testClockRequest.setTimestamp(LocalDateTime.now().plusDays(1));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.clockIn(1L, testClockRequest);
        });
    }

    @Test
    @DisplayName("Test clock-in with past timestamp within allowed window - success")
    public void testClockIn_PastTimestampAllowed_Success() {
        // Arrange
        testClockRequest.setTimestamp(LocalDateTime.now().minusHours(1));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(attendanceEventRepository.save(any(AttendanceEvent.class))).thenReturn(testClockInEvent);

        // Act
        AttendanceEventDTO result = attendanceService.clockIn(1L, testClockRequest);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Test clock-in with past timestamp beyond allowed window - throws exception")
    public void testClockIn_PastTimestampBeyondWindow_ThrowsException() {
        // Arrange
        testClockRequest.setTimestamp(LocalDateTime.now().minusDays(2));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.clockIn(1L, testClockRequest);
        });
    }

    // ========== CLOCK-OUT TESTS ==========

    @Test
    @DisplayName("Test clock-out with valid data - success")
    public void testClockOut_ValidData_Success() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(attendanceEventRepository.findByEmployeeAndDate(any(), any()))
                .thenReturn(Arrays.asList(testClockInEvent));
        when(attendanceEventRepository.save(any(AttendanceEvent.class))).thenReturn(testClockOutEvent);

        // Act
        AttendanceEventDTO result = attendanceService.clockOut(1L, testClockRequest);

        // Assert
        assertNotNull(result);
        assertEquals(AttendanceType.CLOCK_OUT.toString(), result.getType());
        verify(attendanceEventRepository, times(1)).save(any(AttendanceEvent.class));
    }

    @Test
    @DisplayName("Test clock-out without clock-in - throws exception")
    public void testClockOut_WithoutClockIn_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(attendanceEventRepository.findByEmployeeAndDate(any(), any()))
                .thenReturn(Arrays.asList());

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            attendanceService.clockOut(1L, testClockRequest);
        });
    }

    @Test
    @DisplayName("Test clock-out when already clocked out - throws exception")
    public void testClockOut_AlreadyClockedOut_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(attendanceEventRepository.findByEmployeeAndDate(any(), any()))
                .thenReturn(Arrays.asList(testClockInEvent, testClockOutEvent));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            attendanceService.clockOut(1L, testClockRequest);
        });
    }

    @Test
    @DisplayName("Test clock-out with null employee ID - throws exception")
    public void testClockOut_NullEmployeeId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.clockOut(null, testClockRequest);
        });
    }

    @Test
    @DisplayName("Test clock-out before clock-in time - throws exception")
    public void testClockOut_BeforeClockIn_ThrowsException() {
        // Arrange
        testClockRequest.setTimestamp(testClockInEvent.getTimestamp().minusHours(1));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(attendanceEventRepository.findByEmployeeAndDate(any(), any()))
                .thenReturn(Arrays.asList(testClockInEvent));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.clockOut(1L, testClockRequest);
        });
    }

    // ========== HOURS CALCULATION TESTS ==========

    @Test
    @DisplayName("Test calculate hours worked - normal shift")
    public void testCalculateHoursWorked_NormalShift_Success() {
        // Arrange
        LocalDateTime clockIn = LocalDateTime.of(2024, 1, 1, 9, 0);
        LocalDateTime clockOut = LocalDateTime.of(2024, 1, 1, 17, 0);

        // Act
        double hours = attendanceService.calculateHoursWorked(clockIn, clockOut);

        // Assert
        assertEquals(8.0, hours, 0.01);
    }

    @Test
    @DisplayName("Test calculate hours worked - overtime shift")
    public void testCalculateHoursWorked_OvertimeShift_Success() {
        // Arrange
        LocalDateTime clockIn = LocalDateTime.of(2024, 1, 1, 9, 0);
        LocalDateTime clockOut = LocalDateTime.of(2024, 1, 1, 21, 0);

        // Act
        double hours = attendanceService.calculateHoursWorked(clockIn, clockOut);

        // Assert
        assertEquals(12.0, hours, 0.01);
    }

    @Test
    @DisplayName("Test calculate hours worked - partial hour")
    public void testCalculateHoursWorked_PartialHour_Success() {
        // Arrange
        LocalDateTime clockIn = LocalDateTime.of(2024, 1, 1, 9, 0);
        LocalDateTime clockOut = LocalDateTime.of(2024, 1, 1, 9, 30);

        // Act
        double hours = attendanceService.calculateHoursWorked(clockIn, clockOut);

        // Assert
        assertEquals(0.5, hours, 0.01);
    }

    @Test
    @DisplayName("Test calculate hours worked - overnight shift")
    public void testCalculateHoursWorked_OvernightShift_Success() {
        // Arrange
        LocalDateTime clockIn = LocalDateTime.of(2024, 1, 1, 22, 0);
        LocalDateTime clockOut = LocalDateTime.of(2024, 1, 2, 6, 0);

        // Act
        double hours = attendanceService.calculateHoursWorked(clockIn, clockOut);

        // Assert
        assertEquals(8.0, hours, 0.01);
    }

    // ========== GEOFENCE VALIDATION TESTS ==========

    @Test
    @DisplayName("Test geofence validation - within boundary - success")
    public void testGeofenceValidation_WithinBoundary_Success() {
        // Arrange
        String warehouseLocation = "40.7128,-74.0060";
        String employeeLocation = "40.7130,-74.0062";

        // Act
        boolean isValid = attendanceService.validateGeofence(employeeLocation, warehouseLocation, 100);

        // Assert
        assertTrue(isValid);
    }

    @Test
    @DisplayName("Test geofence validation - outside boundary - fails")
    public void testGeofenceValidation_OutsideBoundary_Fails() {
        // Arrange
        String warehouseLocation = "40.7128,-74.0060";
        String employeeLocation = "41.0000,-75.0000";

        // Act
        boolean isValid = attendanceService.validateGeofence(employeeLocation, warehouseLocation, 100);

        // Assert
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Test geofence validation - null employee location - throws exception")
    public void testGeofenceValidation_NullEmployeeLocation_ThrowsException() {
        // Arrange
        String warehouseLocation = "40.7128,-74.0060";

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.validateGeofence(null, warehouseLocation, 100);
        });
    }

    // ========== MISSED PUNCH CORRECTION TESTS ==========

    @Test
    @DisplayName("Test request missed punch correction - success")
    public void testRequestMissedPunchCorrection_Success() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));

        // Act
        attendanceService.requestMissedPunchCorrection(1L, LocalDate.now(), "Forgot to clock in");

        // Assert
        verify(attendanceEventRepository, times(1)).save(any(AttendanceEvent.class));
    }

    @Test
    @DisplayName("Test request missed punch correction - null employee ID - throws exception")
    public void testRequestMissedPunchCorrection_NullEmployeeId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.requestMissedPunchCorrection(null, LocalDate.now(), "Reason");
        });
    }

    @Test
    @DisplayName("Test request missed punch correction - null date - throws exception")
    public void testRequestMissedPunchCorrection_NullDate_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.requestMissedPunchCorrection(1L, null, "Reason");
        });
    }

    @Test
    @DisplayName("Test request missed punch correction - empty reason - throws exception")
    public void testRequestMissedPunchCorrection_EmptyReason_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.requestMissedPunchCorrection(1L, LocalDate.now(), "");
        });
    }

    // ========== DAILY TOTALS TESTS ==========

    @Test
    @DisplayName("Test get daily totals - success")
    public void testGetDailyTotals_Success() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(attendanceEventRepository.findByEmployeeAndDate(any(), any()))
                .thenReturn(Arrays.asList(testClockInEvent, testClockOutEvent));

        // Act
        double totalHours = attendanceService.getDailyTotals(1L, LocalDate.now());

        // Assert
        assertTrue(totalHours >= 0);
    }

    @Test
    @DisplayName("Test get daily totals - no events - returns zero")
    public void testGetDailyTotals_NoEvents_ReturnsZero() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(attendanceEventRepository.findByEmployeeAndDate(any(), any()))
                .thenReturn(Arrays.asList());

        // Act
        double totalHours = attendanceService.getDailyTotals(1L, LocalDate.now());

        // Assert
        assertEquals(0.0, totalHours, 0.01);
    }
}