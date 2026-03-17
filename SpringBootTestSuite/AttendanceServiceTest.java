package com.warehouse.ems.service;

import com.warehouse.ems.dto.CorrectionRequestDto;
import com.warehouse.ems.entity.AttendanceEvent;
import com.warehouse.ems.entity.Employee;
import com.warehouse.ems.exception.EntityNotFoundException;
import com.warehouse.ems.repository.AttendanceEventRepository;
import com.warehouse.ems.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AttendanceService.
 * Covers normal operation, null/invalid input, business rules, and exception scenarios.
 */
@ExtendWith(MockitoExtension.class)
class AttendanceServiceTest {

    @Mock
    private AttendanceEventRepository attendanceEventRepository;
    @Mock
    private EmployeeRepository employeeRepository;
    @InjectMocks
    private AttendanceService attendanceService;

    private Employee employee;
    private AttendanceEvent attendanceEvent;

    @BeforeEach
    void setUp() {
        employee = new Employee();
        employee.setId(1L);
        employee.setBadgeId("BADGE123");
        employee.setStatus("ACTIVE");

        attendanceEvent = new AttendanceEvent();
        attendanceEvent.setId(1L);
        attendanceEvent.setEmployee(employee);
        attendanceEvent.setClockIn(LocalDateTime.now().minusHours(8));
        attendanceEvent.setClockOut(LocalDateTime.now());
        attendanceEvent.setHoursWorked(8.0);
        attendanceEvent.setDeviceId("DEV1");
        attendanceEvent.setLocation("Main Gate");
        attendanceEvent.setEventType("CLOCK_IN");
    }

    /**
     * Test clockIn with valid input returns AttendanceEvent.
     */
    @Test
    void testClockIn_ValidInput_ReturnsAttendanceEvent() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(attendanceEventRepository.save(any(AttendanceEvent.class))).thenReturn(attendanceEvent);
        AttendanceEvent result = attendanceService.clockIn(1L, "DEV1", "Main Gate");
        assertNotNull(result);
        assertEquals("CLOCK_IN", result.getEventType());
    }

    /**
     * Test clockIn with null employeeId throws exception.
     */
    @Test
    void testClockIn_NullEmployeeId_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                attendanceService.clockIn(null, "DEV1", "Main Gate"));
    }

    /**
     * Test clockIn with non-existent employee throws EntityNotFoundException.
     */
    @Test
    void testClockIn_NonExistentEmployee_ThrowsEntityNotFoundException() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () ->
                attendanceService.clockIn(99L, "DEV1", "Main Gate"));
    }

    /**
     * Test clockOut with valid input returns AttendanceEvent.
     */
    @Test
    void testClockOut_ValidInput_ReturnsAttendanceEvent() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(attendanceEventRepository.findTopByEmployeeIdOrderByClockInDesc(1L)).thenReturn(Optional.of(attendanceEvent));
        when(attendanceEventRepository.save(any(AttendanceEvent.class))).thenReturn(attendanceEvent);
        AttendanceEvent result = attendanceService.clockOut(1L);
        assertNotNull(result);
        assertEquals("CLOCK_OUT", result.getEventType());
    }

    /**
     * Test clockOut with null employeeId throws exception.
     */
    @Test
    void testClockOut_NullEmployeeId_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                attendanceService.clockOut(null));
    }

    /**
     * Test calculateHoursWorked with valid event returns hours.
     */
    @Test
    void testCalculateHoursWorked_ValidEvent_ReturnsHours() {
        when(attendanceEventRepository.findById(1L)).thenReturn(Optional.of(attendanceEvent));
        Double hours = attendanceService.calculateHoursWorked(1L);
        assertEquals(8.0, hours);
    }

    /**
     * Test calculateHoursWorked with non-existent event throws exception.
     */
    @Test
    void testCalculateHoursWorked_NonExistentEvent_ThrowsEntityNotFoundException() {
        when(attendanceEventRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () ->
                attendanceService.calculateHoursWorked(99L));
    }

    /**
     * Test getAttendanceByEmployee with valid input returns list.
     */
    @Test
    void testGetAttendanceByEmployee_ValidInput_ReturnsList() {
        when(attendanceEventRepository.findByEmployeeIdAndDate(eq(1L), any(LocalDate.class)))
                .thenReturn(List.of(attendanceEvent));
        List<AttendanceEvent> result = attendanceService.getAttendanceByEmployee(1L, LocalDate.now());
        assertEquals(1, result.size());
    }

    /**
     * Test createCorrection with valid input returns AttendanceEvent.
     */
    @Test
    void testCreateCorrection_ValidInput_ReturnsAttendanceEvent() {
        CorrectionRequestDto dto = new CorrectionRequestDto();
        dto.setClockIn(LocalDateTime.now().minusHours(7));
        dto.setClockOut(LocalDateTime.now());
        when(attendanceEventRepository.findById(1L)).thenReturn(Optional.of(attendanceEvent));
        when(attendanceEventRepository.save(any(AttendanceEvent.class))).thenReturn(attendanceEvent);
        AttendanceEvent result = attendanceService.createCorrection(1L, dto);
        assertNotNull(result);
        assertEquals("CORRECTION", result.getEventType());
    }

    /**
     * Test createCorrection with null DTO throws exception.
     */
    @Test
    void testCreateCorrection_NullDto_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                attendanceService.createCorrection(1L, null));
    }

    /**
     * Test getAttendanceByEmployee with null date throws exception.
     */
    @Test
    void testGetAttendanceByEmployee_NullDate_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                attendanceService.getAttendanceByEmployee(1L, null));
    }
}
