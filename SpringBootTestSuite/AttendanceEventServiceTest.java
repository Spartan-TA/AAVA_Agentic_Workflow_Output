package com.company.warehousemgmt.service;

import com.company.warehousemgmt.domain.AttendanceEvent;
import com.company.warehousemgmt.domain.Employee;
import com.company.warehousemgmt.dto.AttendanceEventDTO;
import com.company.warehousemgmt.exception.NotFoundException;
import com.company.warehousemgmt.repository.AttendanceEventRepository;
import com.company.warehousemgmt.repository.EmployeeRepository;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Comprehensive JUnit test suite for AttendanceEventService
 * Tests cover clock-in/out operations, geofence validation, and hours calculation
 */
@ExtendWith(MockitoExtension.class)
class AttendanceEventServiceTest {

    @Mock
    private AttendanceEventRepository attendanceEventRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private AttendanceEventService attendanceEventService;

    private Employee testEmployee;
    private AttendanceEvent testClockInEvent;
    private AttendanceEvent testClockOutEvent;
    private AttendanceEventDTO testClockInDTO;

    @BeforeEach
    void setUp() {
        testEmployee = new Employee();
        testEmployee.setId(1L);
        testEmployee.setBadgeId("EMP001");
        testEmployee.setName("John Doe");

        testClockInEvent = new AttendanceEvent();
        testClockInEvent.setId(1L);
        testClockInEvent.setEmployee(testEmployee);
        testClockInEvent.setEventType("CLOCK_IN");
        testClockInEvent.setTimestamp(LocalDateTime.now());
        testClockInEvent.setLatitude(37.7749);
        testClockInEvent.setLongitude(-122.4194);
        testClockInEvent.setDeviceId("DEVICE001");

        testClockOutEvent = new AttendanceEvent();
        testClockOutEvent.setId(2L);
        testClockOutEvent.setEmployee(testEmployee);
        testClockOutEvent.setEventType("CLOCK_OUT");
        testClockOutEvent.setTimestamp(LocalDateTime.now().plusHours(8));
        testClockOutEvent.setLatitude(37.7749);
        testClockOutEvent.setLongitude(-122.4194);
        testClockOutEvent.setDeviceId("DEVICE001");

        testClockInDTO = new AttendanceEventDTO();
        testClockInDTO.setEmployeeId(1L);
        testClockInDTO.setEventType("CLOCK_IN");
        testClockInDTO.setTimestamp(LocalDateTime.now());
        testClockInDTO.setLatitude(37.7749);
        testClockInDTO.setLongitude(-122.4194);
        testClockInDTO.setDeviceId("DEVICE001");
    }

    // ========== Clock In Tests ==========

    @Test
    void testClockIn_WithValidData_CreatesClockInEvent() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(attendanceEventRepository.save(any(AttendanceEvent.class))).thenReturn(testClockInEvent);

        // Act
        AttendanceEventDTO result = attendanceEventService.clockIn(testClockInDTO);

        // Assert
        assertNotNull(result);
        assertEquals("CLOCK_IN", result.getEventType());
        assertEquals(1L, result.getEmployeeId());
        verify(attendanceEventRepository, times(1)).save(any(AttendanceEvent.class));
    }

    @Test
    void testClockIn_WithNonExistentEmployee_ThrowsNotFoundException() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());
        testClockInDTO.setEmployeeId(999L);

        // Act & Assert
        assertThrows(NotFoundException.class, () -> attendanceEventService.clockIn(testClockInDTO));
        verify(attendanceEventRepository, never()).save(any(AttendanceEvent.class));
    }

    @Test
    void testClockIn_WithNullEmployeeId_ThrowsIllegalArgumentException() {
        // Arrange
        testClockInDTO.setEmployeeId(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> attendanceEventService.clockIn(testClockInDTO));
        verify(attendanceEventRepository, never()).save(any(AttendanceEvent.class));
    }

    @Test
    void testClockIn_WithNullTimestamp_ThrowsIllegalArgumentException() {
        // Arrange
        testClockInDTO.setTimestamp(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> attendanceEventService.clockIn(testClockInDTO));
        verify(attendanceEventRepository, never()).save(any(AttendanceEvent.class));
    }

    @Test
    void testClockIn_WithFutureTimestamp_ThrowsIllegalArgumentException() {
        // Arrange
        testClockInDTO.setTimestamp(LocalDateTime.now().plusDays(1));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> attendanceEventService.clockIn(testClockInDTO));
        verify(attendanceEventRepository, never()).save(any(AttendanceEvent.class));
    }

    @Test
    void testClockIn_WithInvalidLatitude_ThrowsIllegalArgumentException() {
        // Arrange
        testClockInDTO.setLatitude(91.0); // Invalid latitude > 90

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> attendanceEventService.clockIn(testClockInDTO));
        verify(attendanceEventRepository, never()).save(any(AttendanceEvent.class));
    }

    @Test
    void testClockIn_WithInvalidLongitude_ThrowsIllegalArgumentException() {
        // Arrange
        testClockInDTO.setLongitude(181.0); // Invalid longitude > 180

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> attendanceEventService.clockIn(testClockInDTO));
        verify(attendanceEventRepository, never()).save(any(AttendanceEvent.class));
    }

    @Test
    void testClockIn_WithNullDeviceId_ThrowsIllegalArgumentException() {
        // Arrange
        testClockInDTO.setDeviceId(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> attendanceEventService.clockIn(testClockInDTO));
        verify(attendanceEventRepository, never()).save(any(AttendanceEvent.class));
    }

    @Test
    void testClockIn_WithEmptyDeviceId_ThrowsIllegalArgumentException() {
        // Arrange
        testClockInDTO.setDeviceId("");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> attendanceEventService.clockIn(testClockInDTO));
        verify(attendanceEventRepository, never()).save(any(AttendanceEvent.class));
    }

    @Test
    void testClockIn_WithAlreadyClockedIn_ThrowsIllegalArgumentException() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(attendanceEventRepository.findLatestEventByEmployee(1L)).thenReturn(Optional.of(testClockInEvent));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> attendanceEventService.clockIn(testClockInDTO));
        verify(attendanceEventRepository, never()).save(any(AttendanceEvent.class));
    }

    // ========== Clock Out Tests ==========

    @Test
    void testClockOut_WithValidData_CreatesClockOutEvent() {
        // Arrange
        AttendanceEventDTO clockOutDTO = new AttendanceEventDTO();
        clockOutDTO.setEmployeeId(1L);
        clockOutDTO.setEventType("CLOCK_OUT");
        clockOutDTO.setTimestamp(LocalDateTime.now());
        clockOutDTO.setLatitude(37.7749);
        clockOutDTO.setLongitude(-122.4194);
        clockOutDTO.setDeviceId("DEVICE001");

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(attendanceEventRepository.findLatestEventByEmployee(1L)).thenReturn(Optional.of(testClockInEvent));
        when(attendanceEventRepository.save(any(AttendanceEvent.class))).thenReturn(testClockOutEvent);

        // Act
        AttendanceEventDTO result = attendanceEventService.clockOut(clockOutDTO);

        // Assert
        assertNotNull(result);
        assertEquals("CLOCK_OUT", result.getEventType());
        verify(attendanceEventRepository, times(1)).save(any(AttendanceEvent.class));
    }

    @Test
    void testClockOut_WithoutClockIn_ThrowsIllegalArgumentException() {
        // Arrange
        AttendanceEventDTO clockOutDTO = new AttendanceEventDTO();
        clockOutDTO.setEmployeeId(1L);
        clockOutDTO.setEventType("CLOCK_OUT");
        clockOutDTO.setTimestamp(LocalDateTime.now());
        clockOutDTO.setLatitude(37.7749);
        clockOutDTO.setLongitude(-122.4194);
        clockOutDTO.setDeviceId("DEVICE001");

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(attendanceEventRepository.findLatestEventByEmployee(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> attendanceEventService.clockOut(clockOutDTO));
        verify(attendanceEventRepository, never()).save(any(AttendanceEvent.class));
    }

    @Test
    void testClockOut_WithAlreadyClockedOut_ThrowsIllegalArgumentException() {
        // Arrange
        AttendanceEventDTO clockOutDTO = new AttendanceEventDTO();
        clockOutDTO.setEmployeeId(1L);
        clockOutDTO.setEventType("CLOCK_OUT");
        clockOutDTO.setTimestamp(LocalDateTime.now());
        clockOutDTO.setLatitude(37.7749);
        clockOutDTO.setLongitude(-122.4194);
        clockOutDTO.setDeviceId("DEVICE001");

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(attendanceEventRepository.findLatestEventByEmployee(1L)).thenReturn(Optional.of(testClockOutEvent));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> attendanceEventService.clockOut(clockOutDTO));
        verify(attendanceEventRepository, never()).save(any(AttendanceEvent.class));
    }

    @Test
    void testClockOut_WithTimestampBeforeClockIn_ThrowsIllegalArgumentException() {
        // Arrange
        AttendanceEventDTO clockOutDTO = new AttendanceEventDTO();
        clockOutDTO.setEmployeeId(1L);
        clockOutDTO.setEventType("CLOCK_OUT");
        clockOutDTO.setTimestamp(testClockInEvent.getTimestamp().minusHours(1));
        clockOutDTO.setLatitude(37.7749);
        clockOutDTO.setLongitude(-122.4194);
        clockOutDTO.setDeviceId("DEVICE001");

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(attendanceEventRepository.findLatestEventByEmployee(1L)).thenReturn(Optional.of(testClockInEvent));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> attendanceEventService.clockOut(clockOutDTO));
        verify(attendanceEventRepository, never()).save(any(AttendanceEvent.class));
    }

    // ========== Get Events Tests ==========

    @Test
    void testGetEventsByEmployee_WithValidEmployeeId_ReturnsEvents() {
        // Arrange
        List<AttendanceEvent> events = Arrays.asList(testClockInEvent, testClockOutEvent);
        when(attendanceEventRepository.findByEmployeeId(1L)).thenReturn(events);

        // Act
        List<AttendanceEventDTO> result = attendanceEventService.getEventsByEmployee(1L);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(attendanceEventRepository, times(1)).findByEmployeeId(1L);
    }

    @Test
    void testGetEventsByEmployee_WithNonExistentEmployee_ReturnsEmptyList() {
        // Arrange
        when(attendanceEventRepository.findByEmployeeId(999L)).thenReturn(Arrays.asList());

        // Act
        List<AttendanceEventDTO> result = attendanceEventService.getEventsByEmployee(999L);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(attendanceEventRepository, times(1)).findByEmployeeId(999L);
    }

    @Test
    void testGetEventsByEmployee_WithNullEmployeeId_ThrowsIllegalArgumentException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> attendanceEventService.getEventsByEmployee(null));
        verify(attendanceEventRepository, never()).findByEmployeeId(anyLong());
    }

    // ========== Calculate Hours Tests ==========

    @Test
    void testCalculateHoursWorked_WithValidClockInOut_ReturnsCorrectHours() {
        // Arrange
        testClockOutEvent.setTimestamp(testClockInEvent.getTimestamp().plusHours(8));
        List<AttendanceEvent> events = Arrays.asList(testClockInEvent, testClockOutEvent);
        when(attendanceEventRepository.findByEmployeeIdAndDateRange(anyLong(), any(), any())).thenReturn(events);

        // Act
        double hours = attendanceEventService.calculateHoursWorked(1L, LocalDateTime.now().minusDays(1), LocalDateTime.now());

        // Assert
        assertEquals(8.0, hours, 0.01);
    }

    @Test
    void testCalculateHoursWorked_WithNoEvents_ReturnsZero() {
        // Arrange
        when(attendanceEventRepository.findByEmployeeIdAndDateRange(anyLong(), any(), any())).thenReturn(Arrays.asList());

        // Act
        double hours = attendanceEventService.calculateHoursWorked(1L, LocalDateTime.now().minusDays(1), LocalDateTime.now());

        // Assert
        assertEquals(0.0, hours, 0.01);
    }

    @Test
    void testCalculateHoursWorked_WithOnlyClockIn_ReturnsZero() {
        // Arrange
        List<AttendanceEvent> events = Arrays.asList(testClockInEvent);
        when(attendanceEventRepository.findByEmployeeIdAndDateRange(anyLong(), any(), any())).thenReturn(events);

        // Act
        double hours = attendanceEventService.calculateHoursWorked(1L, LocalDateTime.now().minusDays(1), LocalDateTime.now());

        // Assert
        assertEquals(0.0, hours, 0.01);
    }

    @Test
    void testCalculateHoursWorked_WithNullEmployeeId_ThrowsIllegalArgumentException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> 
            attendanceEventService.calculateHoursWorked(null, LocalDateTime.now().minusDays(1), LocalDateTime.now()));
    }

    @Test
    void testCalculateHoursWorked_WithNullStartDate_ThrowsIllegalArgumentException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> 
            attendanceEventService.calculateHoursWorked(1L, null, LocalDateTime.now()));
    }

    @Test
    void testCalculateHoursWorked_WithNullEndDate_ThrowsIllegalArgumentException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> 
            attendanceEventService.calculateHoursWorked(1L, LocalDateTime.now().minusDays(1), null));
    }

    @Test
    void testCalculateHoursWorked_WithEndDateBeforeStartDate_ThrowsIllegalArgumentException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> 
            attendanceEventService.calculateHoursWorked(1L, LocalDateTime.now(), LocalDateTime.now().minusDays(1)));
    }

    // ========== Geofence Validation Tests ==========

    @Test
    void testValidateGeofence_WithValidLocation_ReturnsTrue() {
        // Arrange
        double warehouseLat = 37.7749;
        double warehouseLon = -122.4194;
        double employeeLat = 37.7750;
        double employeeLon = -122.4195;
        double radiusMeters = 100.0;

        // Act
        boolean result = attendanceEventService.validateGeofence(employeeLat, employeeLon, warehouseLat, warehouseLon, radiusMeters);

        // Assert
        assertTrue(result);
    }

    @Test
    void testValidateGeofence_WithLocationOutsideRadius_ReturnsFalse() {
        // Arrange
        double warehouseLat = 37.7749;
        double warehouseLon = -122.4194;
        double employeeLat = 38.0;
        double employeeLon = -123.0;
        double radiusMeters = 100.0;

        // Act
        boolean result = attendanceEventService.validateGeofence(employeeLat, employeeLon, warehouseLat, warehouseLon, radiusMeters);

        // Assert
        assertFalse(result);
    }

    @Test
    void testValidateGeofence_WithExactLocation_ReturnsTrue() {
        // Arrange
        double warehouseLat = 37.7749;
        double warehouseLon = -122.4194;
        double radiusMeters = 100.0;

        // Act
        boolean result = attendanceEventService.validateGeofence(warehouseLat, warehouseLon, warehouseLat, warehouseLon, radiusMeters);

        // Assert
        assertTrue(result);
    }

    // ========== Edge Case Tests ==========

    @Test
    void testClockIn_WithBoundaryLatitude_CreatesEvent() {
        // Arrange
        testClockInDTO.setLatitude(90.0); // Maximum valid latitude
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(attendanceEventRepository.save(any(AttendanceEvent.class))).thenReturn(testClockInEvent);

        // Act
        AttendanceEventDTO result = attendanceEventService.clockIn(testClockInDTO);

        // Assert
        assertNotNull(result);
        verify(attendanceEventRepository, times(1)).save(any(AttendanceEvent.class));
    }

    @Test
    void testClockIn_WithBoundaryLongitude_CreatesEvent() {
        // Arrange
        testClockInDTO.setLongitude(180.0); // Maximum valid longitude
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(attendanceEventRepository.save(any(AttendanceEvent.class))).thenReturn(testClockInEvent);

        // Act
        AttendanceEventDTO result = attendanceEventService.clockIn(testClockInDTO);

        // Assert
        assertNotNull(result);
        verify(attendanceEventRepository, times(1)).save(any(AttendanceEvent.class));
    }

    @Test
    void testCalculateHoursWorked_WithOvernightShift_ReturnsCorrectHours() {
        // Arrange
        testClockInEvent.setTimestamp(LocalDateTime.now().withHour(22).withMinute(0));
        testClockOutEvent.setTimestamp(LocalDateTime.now().plusDays(1).withHour(6).withMinute(0));
        List<AttendanceEvent> events = Arrays.asList(testClockInEvent, testClockOutEvent);
        when(attendanceEventRepository.findByEmployeeIdAndDateRange(anyLong(), any(), any())).thenReturn(events);

        // Act
        double hours = attendanceEventService.calculateHoursWorked(1L, LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1));

        // Assert
        assertEquals(8.0, hours, 0.01);
    }

    @Test
    void testClockIn_WithVeryLongDeviceId_ThrowsIllegalArgumentException() {
        // Arrange
        testClockInDTO.setDeviceId("A".repeat(256));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> attendanceEventService.clockIn(testClockInDTO));
        verify(attendanceEventRepository, never()).save(any(AttendanceEvent.class));
    }
}