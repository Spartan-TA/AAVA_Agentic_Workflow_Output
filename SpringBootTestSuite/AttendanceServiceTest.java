package com.warehouse.ems.domain.attendance;

import com.warehouse.ems.domain.employee.Employee;
import com.warehouse.ems.domain.employee.EmployeeRepository;
import com.warehouse.ems.domain.shift.Shift;
import com.warehouse.ems.exception.BusinessException;
import com.warehouse.ems.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@DisplayName("Attendance Service Test Suite")
public class AttendanceServiceTest {

    @Mock
    private AttendanceEventRepository attendanceEventRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private GeofenceService geofenceService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private AttendanceServiceImpl attendanceService;

    private Employee testEmployee;
    private AttendanceEvent testAttendanceEvent;
    private ClockInDto clockInDto;
    private ClockOutDto clockOutDto;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        testEmployee = new Employee();
        testEmployee.setId(1L);
        testEmployee.setBadgeId("EMP001");
        testEmployee.setName("John Doe");

        testAttendanceEvent = new AttendanceEvent();
        testAttendanceEvent.setId(1L);
        testAttendanceEvent.setEmployee(testEmployee);
        testAttendanceEvent.setClockIn(LocalDateTime.now());
        testAttendanceEvent.setStatus(AttendanceStatus.ACTIVE);

        clockInDto = new ClockInDto();
        clockInDto.setEmployeeId(1L);
        clockInDto.setClockInTime(LocalDateTime.now());
        clockInDto.setDeviceId("DEVICE001");
        clockInDto.setLocation("Warehouse A");

        clockOutDto = new ClockOutDto();
        clockOutDto.setAttendanceEventId(1L);
        clockOutDto.setClockOutTime(LocalDateTime.now().plusHours(8));
    }

    @Test
    @DisplayName("Test clock-in with valid data")
    public void testClockInWithValidData() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(attendanceEventRepository.findActiveEventByEmployee(1L)).thenReturn(Optional.empty());
        when(attendanceEventRepository.save(any(AttendanceEvent.class))).thenReturn(testAttendanceEvent);

        // Act
        AttendanceEventDto result = attendanceService.clockIn(clockInDto);

        // Assert
        assertNotNull(result);
        verify(attendanceEventRepository, times(1)).save(any(AttendanceEvent.class));
        verify(eventPublisher, times(1)).publishEvent(any());
    }

    @Test
    @DisplayName("Test clock-in with non-existent employee")
    public void testClockInWithNonExistentEmployee() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());
        clockInDto.setEmployeeId(999L);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            attendanceService.clockIn(clockInDto);
        });
        verify(attendanceEventRepository, never()).save(any(AttendanceEvent.class));
    }

    @Test
    @DisplayName("Test clock-in with active event already exists")
    public void testClockInWithActiveEventExists() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(attendanceEventRepository.findActiveEventByEmployee(1L)).thenReturn(Optional.of(testAttendanceEvent));

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            attendanceService.clockIn(clockInDto);
        });
        assertTrue(exception.getMessage().contains("already has an active clock-in event"));
        verify(attendanceEventRepository, never()).save(any(AttendanceEvent.class));
    }

    @Test
    @DisplayName("Test clock-in with geofence validation - inside boundary")
    public void testClockInWithGeofenceInsideBoundary() {
        // Arrange
        clockInDto.setLatitude(40.7128);
        clockInDto.setLongitude(-74.0060);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(attendanceEventRepository.findActiveEventByEmployee(1L)).thenReturn(Optional.empty());
        when(geofenceService.isWithinWarehouseBoundary(40.7128, -74.0060)).thenReturn(true);
        when(attendanceEventRepository.save(any(AttendanceEvent.class))).thenReturn(testAttendanceEvent);

        // Act
        AttendanceEventDto result = attendanceService.clockIn(clockInDto);

        // Assert
        assertNotNull(result);
        verify(geofenceService, times(1)).isWithinWarehouseBoundary(40.7128, -74.0060);
    }

    @Test
    @DisplayName("Test clock-in with geofence validation - outside boundary")
    public void testClockInWithGeofenceOutsideBoundary() {
        // Arrange
        clockInDto.setLatitude(40.7128);
        clockInDto.setLongitude(-74.0060);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(attendanceEventRepository.findActiveEventByEmployee(1L)).thenReturn(Optional.empty());
        when(geofenceService.isWithinWarehouseBoundary(40.7128, -74.0060)).thenReturn(false);

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            attendanceService.clockIn(clockInDto);
        });
        assertTrue(exception.getMessage().contains("outside warehouse geofence"));
    }

    @Test
    @DisplayName("Test clock-in with null employee ID")
    public void testClockInWithNullEmployeeId() {
        // Arrange
        clockInDto.setEmployeeId(null);

        // Act & Assert
        assertThrows(Exception.class, () -> {
            attendanceService.clockIn(clockInDto);
        });
    }

    @Test
    @DisplayName("Test clock-in with null clock-in time")
    public void testClockInWithNullClockInTime() {
        // Arrange
        clockInDto.setClockInTime(null);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(attendanceEventRepository.findActiveEventByEmployee(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(Exception.class, () -> {
            attendanceService.clockIn(clockInDto);
        });
    }

    @Test
    @DisplayName("Test clock-in with future timestamp")
    public void testClockInWithFutureTimestamp() {
        // Arrange
        clockInDto.setClockInTime(LocalDateTime.now().plusDays(1));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(attendanceEventRepository.findActiveEventByEmployee(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            attendanceService.clockIn(clockInDto);
        });
    }

    @Test
    @DisplayName("Test clock-out with valid data")
    public void testClockOutWithValidData() {
        // Arrange
        when(attendanceEventRepository.findById(1L)).thenReturn(Optional.of(testAttendanceEvent));
        when(attendanceEventRepository.save(any(AttendanceEvent.class))).thenReturn(testAttendanceEvent);

        // Act
        AttendanceEventDto result = attendanceService.clockOut(clockOutDto);

        // Assert
        assertNotNull(result);
        verify(attendanceEventRepository, times(1)).save(any(AttendanceEvent.class));
    }

    @Test
    @DisplayName("Test clock-out with non-existent attendance event")
    public void testClockOutWithNonExistentEvent() {
        // Arrange
        when(attendanceEventRepository.findById(999L)).thenReturn(Optional.empty());
        clockOutDto.setAttendanceEventId(999L);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            attendanceService.clockOut(clockOutDto);
        });
    }

    @Test
    @DisplayName("Test clock-out calculates hours worked correctly")
    public void testClockOutCalculatesHoursWorked() {
        // Arrange
        LocalDateTime clockIn = LocalDateTime.of(2024, 1, 15, 8, 0);
        LocalDateTime clockOut = LocalDateTime.of(2024, 1, 15, 16, 30);
        testAttendanceEvent.setClockIn(clockIn);
        clockOutDto.setClockOutTime(clockOut);
        
        when(attendanceEventRepository.findById(1L)).thenReturn(Optional.of(testAttendanceEvent));
        when(attendanceEventRepository.save(any(AttendanceEvent.class))).thenAnswer(invocation -> {
            AttendanceEvent saved = invocation.getArgument(0);
            assertEquals(8.5, saved.getHoursWorked(), 0.01);
            return saved;
        });

        // Act
        AttendanceEventDto result = attendanceService.clockOut(clockOutDto);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Test clock-out with clock-out time before clock-in time")
    public void testClockOutWithInvalidTimeSequence() {
        // Arrange
        testAttendanceEvent.setClockIn(LocalDateTime.now());
        clockOutDto.setClockOutTime(LocalDateTime.now().minusHours(1));
        when(attendanceEventRepository.findById(1L)).thenReturn(Optional.of(testAttendanceEvent));

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            attendanceService.clockOut(clockOutDto);
        });
    }

    @Test
    @DisplayName("Test clock-out with null clock-out time")
    public void testClockOutWithNullClockOutTime() {
        // Arrange
        clockOutDto.setClockOutTime(null);
        when(attendanceEventRepository.findById(1L)).thenReturn(Optional.of(testAttendanceEvent));

        // Act & Assert
        assertThrows(Exception.class, () -> {
            attendanceService.clockOut(clockOutDto);
        });
    }

    @Test
    @DisplayName("Test clock-out with null attendance event ID")
    public void testClockOutWithNullEventId() {
        // Arrange
        clockOutDto.setAttendanceEventId(null);

        // Act & Assert
        assertThrows(Exception.class, () -> {
            attendanceService.clockOut(clockOutDto);
        });
    }

    @Test
    @DisplayName("Test clock-out with already completed event")
    public void testClockOutWithAlreadyCompletedEvent() {
        // Arrange
        testAttendanceEvent.setStatus(AttendanceStatus.COMPLETED);
        testAttendanceEvent.setClockOut(LocalDateTime.now());
        when(attendanceEventRepository.findById(1L)).thenReturn(Optional.of(testAttendanceEvent));

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            attendanceService.clockOut(clockOutDto);
        });
    }

    @Test
    @DisplayName("Test clock-in with empty device ID")
    public void testClockInWithEmptyDeviceId() {
        // Arrange
        clockInDto.setDeviceId("");
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(attendanceEventRepository.findActiveEventByEmployee(1L)).thenReturn(Optional.empty());
        when(attendanceEventRepository.save(any(AttendanceEvent.class))).thenReturn(testAttendanceEvent);

        // Act
        AttendanceEventDto result = attendanceService.clockIn(clockInDto);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Test clock-in with maximum length location")
    public void testClockInWithMaxLengthLocation() {
        // Arrange
        String maxLocation = "A".repeat(200);
        clockInDto.setLocation(maxLocation);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(attendanceEventRepository.findActiveEventByEmployee(1L)).thenReturn(Optional.empty());
        when(attendanceEventRepository.save(any(AttendanceEvent.class))).thenReturn(testAttendanceEvent);

        // Act
        AttendanceEventDto result = attendanceService.clockIn(clockInDto);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Test clock-out with overnight shift")
    public void testClockOutWithOvernightShift() {
        // Arrange
        LocalDateTime clockIn = LocalDateTime.of(2024, 1, 15, 22, 0);
        LocalDateTime clockOut = LocalDateTime.of(2024, 1, 16, 6, 0);
        testAttendanceEvent.setClockIn(clockIn);
        clockOutDto.setClockOutTime(clockOut);
        
        when(attendanceEventRepository.findById(1L)).thenReturn(Optional.of(testAttendanceEvent));
        when(attendanceEventRepository.save(any(AttendanceEvent.class))).thenAnswer(invocation -> {
            AttendanceEvent saved = invocation.getArgument(0);
            assertEquals(8.0, saved.getHoursWorked(), 0.01);
            return saved;
        });

        // Act
        AttendanceEventDto result = attendanceService.clockOut(clockOutDto);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Test clock-out with very short shift duration")
    public void testClockOutWithShortShiftDuration() {
        // Arrange
        LocalDateTime clockIn = LocalDateTime.now();
        LocalDateTime clockOut = clockIn.plusMinutes(15);
        testAttendanceEvent.setClockIn(clockIn);
        clockOutDto.setClockOutTime(clockOut);
        
        when(attendanceEventRepository.findById(1L)).thenReturn(Optional.of(testAttendanceEvent));
        when(attendanceEventRepository.save(any(AttendanceEvent.class))).thenReturn(testAttendanceEvent);

        // Act
        AttendanceEventDto result = attendanceService.clockOut(clockOutDto);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Test clock-out with very long shift duration")
    public void testClockOutWithLongShiftDuration() {
        // Arrange
        LocalDateTime clockIn = LocalDateTime.now().minusHours(16);
        LocalDateTime clockOut = LocalDateTime.now();
        testAttendanceEvent.setClockIn(clockIn);
        clockOutDto.setClockOutTime(clockOut);
        
        when(attendanceEventRepository.findById(1L)).thenReturn(Optional.of(testAttendanceEvent));
        when(attendanceEventRepository.save(any(AttendanceEvent.class))).thenReturn(testAttendanceEvent);

        // Act
        AttendanceEventDto result = attendanceService.clockOut(clockOutDto);

        // Assert
        assertNotNull(result);
    }
}