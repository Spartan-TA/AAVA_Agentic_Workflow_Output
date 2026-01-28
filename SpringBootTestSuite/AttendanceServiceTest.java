package com.warehouse.management.attendance;

import com.warehouse.management.common.exceptions.BusinessException;
import com.warehouse.management.common.exceptions.ResourceNotFoundException;
import com.warehouse.management.employee.Employee;
import com.warehouse.management.employee.EmployeeRepository;
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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Comprehensive JUnit test suite for AttendanceService
 * Tests cover clock-in/out operations, geofence validation, hours calculation, and edge cases
 */
@ExtendWith(MockitoExtension.class)
class AttendanceServiceTest {

    @Mock
    private AttendanceEventRepository attendanceEventRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private GeofenceValidator geofenceValidator;

    @InjectMocks
    private AttendanceServiceImpl attendanceService;

    private Employee testEmployee;
    private AttendanceEvent testClockInEvent;
    private AttendanceEvent testClockOutEvent;
    private ClockInRequest clockInRequest;
    private ClockOutRequest clockOutRequest;
    private UUID employeeId;

    @BeforeEach
    void setUp() {
        employeeId = UUID.randomUUID();
        
        // Setup test employee
        testEmployee = new Employee();
        testEmployee.setId(employeeId);
        testEmployee.setBadgeId("EMP001");
        testEmployee.setFirstName("John");
        testEmployee.setLastName("Doe");
        
        // Setup clock-in event
        testClockInEvent = new AttendanceEvent();
        testClockInEvent.setId(UUID.randomUUID());
        testClockInEvent.setEmployee(testEmployee);
        testClockInEvent.setEventType(AttendanceEventType.CLOCK_IN);
        testClockInEvent.setTimestamp(LocalDateTime.now());
        testClockInEvent.setLatitude(40.7128);
        testClockInEvent.setLongitude(-74.0060);
        testClockInEvent.setDeviceId("DEVICE001");
        
        // Setup clock-out event
        testClockOutEvent = new AttendanceEvent();
        testClockOutEvent.setId(UUID.randomUUID());
        testClockOutEvent.setEmployee(testEmployee);
        testClockOutEvent.setEventType(AttendanceEventType.CLOCK_OUT);
        testClockOutEvent.setTimestamp(LocalDateTime.now().plusHours(8));
        testClockOutEvent.setLatitude(40.7128);
        testClockOutEvent.setLongitude(-74.0060);
        testClockOutEvent.setDeviceId("DEVICE001");
        
        // Setup clock-in request
        clockInRequest = new ClockInRequest();
        clockInRequest.setEmployeeId(employeeId);
        clockInRequest.setLatitude(40.7128);
        clockInRequest.setLongitude(-74.0060);
        clockInRequest.setDeviceId("DEVICE001");
        
        // Setup clock-out request
        clockOutRequest = new ClockOutRequest();
        clockOutRequest.setEmployeeId(employeeId);
        clockOutRequest.setLatitude(40.7128);
        clockOutRequest.setLongitude(-74.0060);
        clockOutRequest.setDeviceId("DEVICE001");
    }

    // ========== CLOCK-IN TESTS ==========

    @Test
    void testClockIn_ValidRequest_Success() {
        // Arrange
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(testEmployee));
        when(attendanceEventRepository.findLatestEventByEmployee(employeeId))
                .thenReturn(Optional.empty());
        when(geofenceValidator.isWithinGeofence(anyDouble(), anyDouble())).thenReturn(true);
        when(attendanceEventRepository.save(any(AttendanceEvent.class))).thenReturn(testClockInEvent);

        // Act
        AttendanceEventResponse result = attendanceService.clockIn(clockInRequest);

        // Assert
        assertNotNull(result);
        assertEquals(AttendanceEventType.CLOCK_IN, result.getEventType());
        verify(employeeRepository, times(1)).findById(employeeId);
        verify(attendanceEventRepository, times(1)).save(any(AttendanceEvent.class));
    }

    @Test
    void testClockIn_EmployeeNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            attendanceService.clockIn(clockInRequest);
        });
        
        assertTrue(exception.getMessage().contains("Employee not found"));
        verify(attendanceEventRepository, never()).save(any(AttendanceEvent.class));
    }

    @Test
    void testClockIn_AlreadyClockedIn_ThrowsBusinessException() {
        // Arrange
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(testEmployee));
        when(attendanceEventRepository.findLatestEventByEmployee(employeeId))
                .thenReturn(Optional.of(testClockInEvent));

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            attendanceService.clockIn(clockInRequest);
        });
        
        assertTrue(exception.getMessage().contains("already clocked in"));
        verify(attendanceEventRepository, never()).save(any(AttendanceEvent.class));
    }

    @Test
    void testClockIn_OutsideGeofence_ThrowsBusinessException() {
        // Arrange
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(testEmployee));
        when(attendanceEventRepository.findLatestEventByEmployee(employeeId))
                .thenReturn(Optional.empty());
        when(geofenceValidator.isWithinGeofence(anyDouble(), anyDouble())).thenReturn(false);

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            attendanceService.clockIn(clockInRequest);
        });
        
        assertTrue(exception.getMessage().contains("outside geofence"));
        verify(attendanceEventRepository, never()).save(any(AttendanceEvent.class));
    }

    @Test
    void testClockIn_NullRequest_ThrowsException() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            attendanceService.clockIn(null);
        });
    }

    @Test
    void testClockIn_NullEmployeeId_ThrowsBusinessException() {
        // Arrange
        clockInRequest.setEmployeeId(null);

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            attendanceService.clockIn(clockInRequest);
        });
    }

    @Test
    void testClockIn_InvalidCoordinates_ThrowsBusinessException() {
        // Arrange
        clockInRequest.setLatitude(null);
        clockInRequest.setLongitude(null);

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            attendanceService.clockIn(clockInRequest);
        });
    }

    @Test
    void testClockIn_WithoutGeofence_Success() {
        // Arrange
        clockInRequest.setLatitude(null);
        clockInRequest.setLongitude(null);
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(testEmployee));
        when(attendanceEventRepository.findLatestEventByEmployee(employeeId))
                .thenReturn(Optional.empty());
        when(attendanceEventRepository.save(any(AttendanceEvent.class))).thenReturn(testClockInEvent);

        // Act
        AttendanceEventResponse result = attendanceService.clockIn(clockInRequest);

        // Assert
        assertNotNull(result);
        verify(geofenceValidator, never()).isWithinGeofence(anyDouble(), anyDouble());
    }

    // ========== CLOCK-OUT TESTS ==========

    @Test
    void testClockOut_ValidRequest_Success() {
        // Arrange
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(testEmployee));
        when(attendanceEventRepository.findLatestEventByEmployee(employeeId))
                .thenReturn(Optional.of(testClockInEvent));
        when(geofenceValidator.isWithinGeofence(anyDouble(), anyDouble())).thenReturn(true);
        when(attendanceEventRepository.save(any(AttendanceEvent.class))).thenReturn(testClockOutEvent);

        // Act
        AttendanceEventResponse result = attendanceService.clockOut(clockOutRequest);

        // Assert
        assertNotNull(result);
        assertEquals(AttendanceEventType.CLOCK_OUT, result.getEventType());
        verify(attendanceEventRepository, times(1)).save(any(AttendanceEvent.class));
    }

    @Test
    void testClockOut_NotClockedIn_ThrowsBusinessException() {
        // Arrange
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(testEmployee));
        when(attendanceEventRepository.findLatestEventByEmployee(employeeId))
                .thenReturn(Optional.empty());

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            attendanceService.clockOut(clockOutRequest);
        });
        
        assertTrue(exception.getMessage().contains("not clocked in"));
        verify(attendanceEventRepository, never()).save(any(AttendanceEvent.class));
    }

    @Test
    void testClockOut_AlreadyClockedOut_ThrowsBusinessException() {
        // Arrange
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(testEmployee));
        when(attendanceEventRepository.findLatestEventByEmployee(employeeId))
                .thenReturn(Optional.of(testClockOutEvent));

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            attendanceService.clockOut(clockOutRequest);
        });
        
        assertTrue(exception.getMessage().contains("already clocked out"));
        verify(attendanceEventRepository, never()).save(any(AttendanceEvent.class));
    }

    @Test
    void testClockOut_OutsideGeofence_ThrowsBusinessException() {
        // Arrange
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(testEmployee));
        when(attendanceEventRepository.findLatestEventByEmployee(employeeId))
                .thenReturn(Optional.of(testClockInEvent));
        when(geofenceValidator.isWithinGeofence(anyDouble(), anyDouble())).thenReturn(false);

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            attendanceService.clockOut(clockOutRequest);
        });
        
        assertTrue(exception.getMessage().contains("outside geofence"));
        verify(attendanceEventRepository, never()).save(any(AttendanceEvent.class));
    }

    // ========== HOURS CALCULATION TESTS ==========

    @Test
    void testCalculateHoursWorked_ValidShift_Success() {
        // Arrange
        LocalDateTime clockIn = LocalDateTime.of(2024, 1, 1, 9, 0);
        LocalDateTime clockOut = LocalDateTime.of(2024, 1, 1, 17, 0);
        
        testClockInEvent.setTimestamp(clockIn);
        testClockOutEvent.setTimestamp(clockOut);
        
        when(attendanceEventRepository.findByEmployeeAndDateRange(any(), any(), any()))
                .thenReturn(Arrays.asList(testClockInEvent, testClockOutEvent));

        // Act
        double hoursWorked = attendanceService.calculateHoursWorked(employeeId, clockIn.toLocalDate());

        // Assert
        assertEquals(8.0, hoursWorked, 0.01);
    }

    @Test
    void testCalculateHoursWorked_OvernightShift_Success() {
        // Arrange
        LocalDateTime clockIn = LocalDateTime.of(2024, 1, 1, 22, 0);
        LocalDateTime clockOut = LocalDateTime.of(2024, 1, 2, 6, 0);
        
        testClockInEvent.setTimestamp(clockIn);
        testClockOutEvent.setTimestamp(clockOut);
        
        when(attendanceEventRepository.findByEmployeeAndDateRange(any(), any(), any()))
                .thenReturn(Arrays.asList(testClockInEvent, testClockOutEvent));

        // Act
        double hoursWorked = attendanceService.calculateHoursWorked(employeeId, clockIn.toLocalDate());

        // Assert
        assertEquals(8.0, hoursWorked, 0.01);
    }

    @Test
    void testCalculateHoursWorked_PartialShift_Success() {
        // Arrange
        LocalDateTime clockIn = LocalDateTime.of(2024, 1, 1, 9, 0);
        LocalDateTime clockOut = LocalDateTime.of(2024, 1, 1, 13, 30);
        
        testClockInEvent.setTimestamp(clockIn);
        testClockOutEvent.setTimestamp(clockOut);
        
        when(attendanceEventRepository.findByEmployeeAndDateRange(any(), any(), any()))
                .thenReturn(Arrays.asList(testClockInEvent, testClockOutEvent));

        // Act
        double hoursWorked = attendanceService.calculateHoursWorked(employeeId, clockIn.toLocalDate());

        // Assert
        assertEquals(4.5, hoursWorked, 0.01);
    }

    @Test
    void testCalculateHoursWorked_NoEvents_ReturnsZero() {
        // Arrange
        when(attendanceEventRepository.findByEmployeeAndDateRange(any(), any(), any()))
                .thenReturn(Arrays.asList());

        // Act
        double hoursWorked = attendanceService.calculateHoursWorked(employeeId, LocalDateTime.now().toLocalDate());

        // Assert
        assertEquals(0.0, hoursWorked, 0.01);
    }

    @Test
    void testCalculateHoursWorked_MissingClockOut_ThrowsBusinessException() {
        // Arrange
        when(attendanceEventRepository.findByEmployeeAndDateRange(any(), any(), any()))
                .thenReturn(Arrays.asList(testClockInEvent));

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            attendanceService.calculateHoursWorked(employeeId, LocalDateTime.now().toLocalDate());
        });
    }

    // ========== MISSED PUNCH CORRECTION TESTS ==========

    @Test
    void testRequestMissedPunchCorrection_ValidRequest_Success() {
        // Arrange
        MissedPunchRequest request = new MissedPunchRequest();
        request.setEmployeeId(employeeId);
        request.setEventType(AttendanceEventType.CLOCK_IN);
        request.setTimestamp(LocalDateTime.now().minusHours(2));
        request.setReason("Forgot to clock in");
        
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(testEmployee));
        when(attendanceEventRepository.save(any(AttendanceEvent.class))).thenReturn(testClockInEvent);

        // Act
        MissedPunchResponse result = attendanceService.requestMissedPunchCorrection(request);

        // Assert
        assertNotNull(result);
        assertEquals("PENDING", result.getStatus());
        verify(attendanceEventRepository, times(1)).save(any(AttendanceEvent.class));
    }

    @Test
    void testRequestMissedPunchCorrection_FutureTimestamp_ThrowsBusinessException() {
        // Arrange
        MissedPunchRequest request = new MissedPunchRequest();
        request.setEmployeeId(employeeId);
        request.setEventType(AttendanceEventType.CLOCK_IN);
        request.setTimestamp(LocalDateTime.now().plusHours(1));
        request.setReason("Future punch");

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            attendanceService.requestMissedPunchCorrection(request);
        });
    }

    @Test
    void testRequestMissedPunchCorrection_EmptyReason_ThrowsBusinessException() {
        // Arrange
        MissedPunchRequest request = new MissedPunchRequest();
        request.setEmployeeId(employeeId);
        request.setEventType(AttendanceEventType.CLOCK_IN);
        request.setTimestamp(LocalDateTime.now().minusHours(2));
        request.setReason("");

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            attendanceService.requestMissedPunchCorrection(request);
        });
    }

    // ========== ATTENDANCE REPORT TESTS ==========

    @Test
    void testGetAttendanceReport_ValidDateRange_Success() {
        // Arrange
        LocalDateTime startDate = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(2024, 1, 31, 23, 59);
        
        when(attendanceEventRepository.findByEmployeeAndDateRange(employeeId, startDate, endDate))
                .thenReturn(Arrays.asList(testClockInEvent, testClockOutEvent));

        // Act
        List<AttendanceEventResponse> result = attendanceService.getAttendanceReport(employeeId, startDate, endDate);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(attendanceEventRepository, times(1)).findByEmployeeAndDateRange(employeeId, startDate, endDate);
    }

    @Test
    void testGetAttendanceReport_InvalidDateRange_ThrowsBusinessException() {
        // Arrange
        LocalDateTime startDate = LocalDateTime.of(2024, 1, 31, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(2024, 1, 1, 23, 59);

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            attendanceService.getAttendanceReport(employeeId, startDate, endDate);
        });
    }

    @Test
    void testGetAttendanceReport_EmptyResult_Success() {
        // Arrange
        LocalDateTime startDate = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(2024, 1, 31, 23, 59);
        
        when(attendanceEventRepository.findByEmployeeAndDateRange(employeeId, startDate, endDate))
                .thenReturn(Arrays.asList());

        // Act
        List<AttendanceEventResponse> result = attendanceService.getAttendanceReport(employeeId, startDate, endDate);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ========== EDGE CASE TESTS ==========

    @Test
    void testClockIn_MultipleDevices_Success() {
        // Arrange
        clockInRequest.setDeviceId("DEVICE002");
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(testEmployee));
        when(attendanceEventRepository.findLatestEventByEmployee(employeeId))
                .thenReturn(Optional.empty());
        when(geofenceValidator.isWithinGeofence(anyDouble(), anyDouble())).thenReturn(true);
        when(attendanceEventRepository.save(any(AttendanceEvent.class))).thenReturn(testClockInEvent);

        // Act
        AttendanceEventResponse result = attendanceService.clockIn(clockInRequest);

        // Assert
        assertNotNull(result);
        verify(attendanceEventRepository, times(1)).save(any(AttendanceEvent.class));
    }

    @Test
    void testCalculateHoursWorked_ExactlyMidnight_Success() {
        // Arrange
        LocalDateTime clockIn = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime clockOut = LocalDateTime.of(2024, 1, 1, 8, 0);
        
        testClockInEvent.setTimestamp(clockIn);
        testClockOutEvent.setTimestamp(clockOut);
        
        when(attendanceEventRepository.findByEmployeeAndDateRange(any(), any(), any()))
                .thenReturn(Arrays.asList(testClockInEvent, testClockOutEvent));

        // Act
        double hoursWorked = attendanceService.calculateHoursWorked(employeeId, clockIn.toLocalDate());

        // Assert
        assertEquals(8.0, hoursWorked, 0.01);
    }

    @Test
    void testClockIn_BoundaryCoordinates_Success() {
        // Arrange
        clockInRequest.setLatitude(90.0);  // North pole
        clockInRequest.setLongitude(180.0); // Date line
        
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(testEmployee));
        when(attendanceEventRepository.findLatestEventByEmployee(employeeId))
                .thenReturn(Optional.empty());
        when(geofenceValidator.isWithinGeofence(90.0, 180.0)).thenReturn(true);
        when(attendanceEventRepository.save(any(AttendanceEvent.class))).thenReturn(testClockInEvent);

        // Act
        AttendanceEventResponse result = attendanceService.clockIn(clockInRequest);

        // Assert
        assertNotNull(result);
        verify(geofenceValidator, times(1)).isWithinGeofence(90.0, 180.0);
    }
}