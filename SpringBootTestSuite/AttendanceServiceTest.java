package com.company.wem.attendance;

import com.company.wem.attendance.dto.AttendanceEventDTO;
import com.company.wem.attendance.entity.AttendanceEvent;
import com.company.wem.attendance.repository.AttendanceEventRepository;
import com.company.wem.attendance.service.AttendanceService;
import com.company.wem.employee.entity.Employee;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.dao.DataIntegrityViolationException;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AttendanceServiceTest {

    @Mock
    private AttendanceEventRepository attendanceEventRepository;

    @InjectMocks
    private AttendanceService attendanceService;

    private Employee employee;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        employee = new Employee(1L, "EMP001", "John Doe", "WORKER", "Warehouse", "A", java.time.LocalDate.now(), "ACTIVE");
    }

    @Test
    void testClockIn_ValidInput_Success() {
        AttendanceEventDTO dto = new AttendanceEventDTO(employee, LocalDateTime.now(), null, "DEV001", "37.7749,-122.4194");
        AttendanceEvent event = new AttendanceEvent(1L, employee, LocalDateTime.now(), null, "DEV001", "37.7749,-122.4194");
        when(attendanceEventRepository.save(any(AttendanceEvent.class))).thenReturn(event);
        AttendanceEvent result = attendanceService.clockIn(dto);
        assertNotNull(result);
        assertEquals("DEV001", result.getDeviceId());
    }

    @Test
    void testClockOut_ValidInput_Success() {
        AttendanceEventDTO dto = new AttendanceEventDTO(employee, LocalDateTime.now().minusHours(8), LocalDateTime.now(), "DEV001", "37.7749,-122.4194");
        AttendanceEvent event = new AttendanceEvent(2L, employee, LocalDateTime.now().minusHours(8), LocalDateTime.now(), "DEV001", "37.7749,-122.4194");
        when(attendanceEventRepository.save(any(AttendanceEvent.class))).thenReturn(event);
        AttendanceEvent result = attendanceService.clockOut(dto);
        assertNotNull(result);
        assertEquals(employee, result.getEmployee());
    }

    @Test
    void testClockIn_NullEmployee_ThrowsException() {
        AttendanceEventDTO dto = new AttendanceEventDTO(null, LocalDateTime.now(), null, "DEV001", "37.7749,-122.4194");
        assertThrows(IllegalArgumentException.class, () -> attendanceService.clockIn(dto));
    }

    @Test
    void testClockIn_InvalidGeoLocation_ThrowsException() {
        AttendanceEventDTO dto = new AttendanceEventDTO(employee, LocalDateTime.now(), null, "DEV001", "invalid_location");
        assertThrows(IllegalArgumentException.class, () -> attendanceService.clockIn(dto));
    }

    @Test
    void testClockOut_MissingClockIn_ThrowsException() {
        AttendanceEventDTO dto = new AttendanceEventDTO(employee, null, LocalDateTime.now(), "DEV001", "37.7749,-122.4194");
        assertThrows(IllegalArgumentException.class, () -> attendanceService.clockOut(dto));
    }

    @Test
    void testClockOut_ClockOutBeforeClockIn_ThrowsException() {
        AttendanceEventDTO dto = new AttendanceEventDTO(employee, LocalDateTime.now(), LocalDateTime.now().minusHours(1), "DEV001", "37.7749,-122.4194");
        assertThrows(IllegalArgumentException.class, () -> attendanceService.clockOut(dto));
    }

    @Test
    void testClockIn_DuplicateEvent_ThrowsException() {
        AttendanceEventDTO dto = new AttendanceEventDTO(employee, LocalDateTime.now(), null, "DEV001", "37.7749,-122.4194");
        when(attendanceEventRepository.findByEmployeeAndClockIn(any(), any())).thenReturn(Optional.of(new AttendanceEvent()));
        assertThrows(DataIntegrityViolationException.class, () -> attendanceService.clockIn(dto));
    }

    @Test
    void testMissedPunchCorrection_Valid_Success() {
        AttendanceEvent event = new AttendanceEvent(3L, employee, LocalDateTime.now().minusHours(8), null, "DEV001", "37.7749,-122.4194");
        when(attendanceEventRepository.findById(3L)).thenReturn(Optional.of(event));
        event.setClockOut(LocalDateTime.now());
        when(attendanceEventRepository.save(event)).thenReturn(event);
        AttendanceEvent result = attendanceService.correctMissedPunch(3L, LocalDateTime.now());
        assertNotNull(result.getClockOut());
    }

    @Test
    void testMissedPunchCorrection_NotFound_ThrowsException() {
        when(attendanceEventRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> attendanceService.correctMissedPunch(99L, LocalDateTime.now()));
    }

    @Test
    void testCalculateHoursWorked_Valid_Success() {
        AttendanceEvent event = new AttendanceEvent(4L, employee, LocalDateTime.now().minusHours(8), LocalDateTime.now(), "DEV001", "37.7749,-122.4194");
        double hours = attendanceService.calculateHoursWorked(event);
        assertTrue(hours >= 7.99 && hours <= 8.01);
    }

    @Test
    void testCalculateHoursWorked_MissingClockOut_ReturnsZero() {
        AttendanceEvent event = new AttendanceEvent(5L, employee, LocalDateTime.now().minusHours(8), null, "DEV001", "37.7749,-122.4194");
        double hours = attendanceService.calculateHoursWorked(event);
        assertEquals(0.0, hours);
    }

    @Test
    void testGeofenceValidation_OutsideGeofence_ThrowsException() {
        AttendanceEventDTO dto = new AttendanceEventDTO(employee, LocalDateTime.now(), null, "DEV001", "0.0000,0.0000");
        assertThrows(SecurityException.class, () -> attendanceService.clockIn(dto));
    }
}