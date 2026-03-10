package com.warehouse.ems.service;

import com.warehouse.ems.domain.entity.AttendanceEvent;
import com.warehouse.ems.domain.entity.Employee;
import com.warehouse.ems.dto.ClockInDto;
import com.warehouse.ems.exception.EmployeeNotFoundException;
import com.warehouse.ems.repository.AttendanceEventRepository;
import com.warehouse.ems.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AttendanceServiceTest {

    @Mock
    private AttendanceEventRepository attendanceEventRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private AttendanceService attendanceService;

    private Employee validEmployee;
    private AttendanceEvent clockInEvent;
    private AttendanceEvent clockOutEvent;
    private ClockInDto validClockInDto;

    @BeforeEach
    void setUp() {
        validEmployee = new Employee();
        validEmployee.setId(1L);
        validEmployee.setBadgeId("BADGE123");
        validEmployee.setName("John Doe");
        validEmployee.setRole("Worker");
        validEmployee.setDepartment("Logistics");
        validEmployee.setStatus("ACTIVE");
        validEmployee.setHireDate(LocalDate.of(2022, 1, 1));

        validClockInDto = new ClockInDto();
        validClockInDto.setEmployeeId(1L);
        validClockInDto.setTimestamp(LocalDateTime.of(2023, 3, 10, 8, 0));
        validClockInDto.setDeviceInfo("DeviceA");

        clockInEvent = new AttendanceEvent();
        clockInEvent.setId(100L);
        clockInEvent.setEmployee(validEmployee);
        clockInEvent.setTimestamp(LocalDateTime.of(2023, 3, 10, 8, 0));
        clockInEvent.setEventType("CLOCK_IN");
        clockInEvent.setDeviceInfo("DeviceA");

        clockOutEvent = new AttendanceEvent();
        clockOutEvent.setId(101L);
        clockOutEvent.setEmployee(validEmployee);
        clockOutEvent.setTimestamp(LocalDateTime.of(2023, 3, 10, 17, 0));
        clockOutEvent.setEventType("CLOCK_OUT");
        clockOutEvent.setHoursWorked(9.0);
    }

    @Test
    void testClockIn_ValidInput_Success() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        when(attendanceEventRepository.findLatestByEmployeeAndEventType(validEmployee, "CLOCK_IN")).thenReturn(Optional.empty());
        when(attendanceEventRepository.save(any(AttendanceEvent.class))).thenReturn(clockInEvent);

        AttendanceEvent result = attendanceService.clockIn(validClockInDto);
        assertNotNull(result);
        assertEquals("CLOCK_IN", result.getEventType());
        assertEquals(validEmployee, result.getEmployee());
        verify(attendanceEventRepository, times(1)).save(any(AttendanceEvent.class));
    }

    @Test
    void testClockIn_NullDto_ThrowsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.clockIn(null);
        });
        assertEquals("Clock-in DTO cannot be null", ex.getMessage());
    }

    @Test
    void testClockIn_NullEmployeeId_ThrowsException() {
        ClockInDto dto = new ClockInDto();
        dto.setEmployeeId(null);
        dto.setTimestamp(LocalDateTime.now());
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.clockIn(dto);
        });
        assertEquals("Employee ID must be positive", ex.getMessage());
    }

    @Test
    void testClockIn_NegativeEmployeeId_ThrowsException() {
        ClockInDto dto = new ClockInDto();
        dto.setEmployeeId(-1L);
        dto.setTimestamp(LocalDateTime.now());
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.clockIn(dto);
        });
        assertEquals("Employee ID must be positive", ex.getMessage());
    }

    @Test
    void testClockIn_NullTimestamp_ThrowsException() {
        ClockInDto dto = new ClockInDto();
        dto.setEmployeeId(1L);
        dto.setTimestamp(null);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.clockIn(dto);
        });
        assertEquals("Timestamp cannot be null", ex.getMessage());
    }

    @Test
    void testClockIn_NonExistentEmployee_ThrowsNotFoundException() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.empty());
        EmployeeNotFoundException ex = assertThrows(EmployeeNotFoundException.class, () -> {
            attendanceService.clockIn(validClockInDto);
        });
        assertTrue(ex.getMessage().contains("Employee not found"));
    }

    @Test
    void testClockIn_AlreadyClockedIn_ThrowsException() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        when(attendanceEventRepository.findLatestByEmployeeAndEventType(validEmployee, "CLOCK_IN")).thenReturn(Optional.of(clockInEvent));
        when(attendanceEventRepository.findByEmployeeAndEventTypeAndTimestampAfter(validEmployee, "CLOCK_OUT", clockInEvent.getTimestamp())).thenReturn(Optional.empty());

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            attendanceService.clockIn(validClockInDto);
        });
        assertEquals("Employee is already clocked in", ex.getMessage());
    }

    @Test
    void testClockIn_ClockedInButClockedOut_Success() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        when(attendanceEventRepository.findLatestByEmployeeAndEventType(validEmployee, "CLOCK_IN")).thenReturn(Optional.of(clockInEvent));
        when(attendanceEventRepository.findByEmployeeAndEventTypeAndTimestampAfter(validEmployee, "CLOCK_OUT", clockInEvent.getTimestamp())).thenReturn(Optional.of(clockOutEvent));
        when(attendanceEventRepository.save(any(AttendanceEvent.class))).thenReturn(clockInEvent);

        AttendanceEvent result = attendanceService.clockIn(validClockInDto);
        assertNotNull(result);
        assertEquals("CLOCK_IN", result.getEventType());
    }

    @Test
    void testClockOut_ValidInput_CalculatesHours() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        when(attendanceEventRepository.findLatestByEmployeeAndEventType(validEmployee, "CLOCK_IN")).thenReturn(Optional.of(clockInEvent));
        when(attendanceEventRepository.findByEmployeeAndEventTypeAndTimestampAfter(validEmployee, "CLOCK_OUT", clockInEvent.getTimestamp())).thenReturn(Optional.empty());
        when(attendanceEventRepository.save(any(AttendanceEvent.class))).thenAnswer(invocation -> {
            AttendanceEvent event = invocation.getArgument(0);
            event.setId(102L);
            return event;
        });

        LocalDateTime clockOutTime = LocalDateTime.of(2023, 3, 10, 17, 0);
        AttendanceEvent result = attendanceService.clockOut(1L, clockOutTime);
        assertNotNull(result);
        assertEquals("CLOCK_OUT", result.getEventType());
        assertEquals(9.0, result.getHoursWorked(), 0.01);
    }

    @Test
    void testClockOut_NullEmployeeId_ThrowsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.clockOut(null, LocalDateTime.now());
        });
        assertEquals("Employee ID must be positive", ex.getMessage());
    }

    @Test
    void testClockOut_NegativeEmployeeId_ThrowsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.clockOut(-1L, LocalDateTime.now());
        });
        assertEquals("Employee ID must be positive", ex.getMessage());
    }

    @Test
    void testClockOut_NullTimestamp_ThrowsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.clockOut(1L, null);
        });
        assertEquals("Timestamp cannot be null", ex.getMessage());
    }

    @Test
    void testClockOut_NonExistentEmployee_ThrowsNotFoundException() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.empty());
        EmployeeNotFoundException ex = assertThrows(EmployeeNotFoundException.class, () -> {
            attendanceService.clockOut(1L, LocalDateTime.now());
        });
        assertTrue(ex.getMessage().contains("Employee not found"));
    }

    @Test
    void testClockOut_NotClockedIn_ThrowsException() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        when(attendanceEventRepository.findLatestByEmployeeAndEventType(validEmployee, "CLOCK_IN")).thenReturn(Optional.empty());

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            attendanceService.clockOut(1L, LocalDateTime.now());
        });
        assertEquals("No clock-in event found for employee", ex.getMessage());
    }

    @Test
    void testClockOut_AlreadyClockedOut_ThrowsException() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        when(attendanceEventRepository.findLatestByEmployeeAndEventType(validEmployee, "CLOCK_IN")).thenReturn(Optional.of(clockInEvent));
        when(attendanceEventRepository.findByEmployeeAndEventTypeAndTimestampAfter(validEmployee, "CLOCK_OUT", clockInEvent.getTimestamp())).thenReturn(Optional.of(clockOutEvent));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            attendanceService.clockOut(1L, LocalDateTime.now());
        });
        assertEquals("Employee is already clocked out", ex.getMessage());
    }

    @Test
    void testGetAttendanceHistory_ValidInput_ReturnsEvents() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        LocalDate startDate = LocalDate.of(2023, 3, 1);
        LocalDate endDate = LocalDate.of(2023, 3, 31);
        List<AttendanceEvent> events = Arrays.asList(clockInEvent, clockOutEvent);
        when(attendanceEventRepository.findByEmployeeAndTimestampBetween(eq(validEmployee), any(), any())).thenReturn(events);

        List<AttendanceEvent> result = attendanceService.getAttendanceHistory(1L, startDate, endDate);
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void testGetAttendanceHistory_NullEmployeeId_ThrowsException() {
        LocalDate startDate = LocalDate.of(2023, 3, 1);
        LocalDate endDate = LocalDate.of(2023, 3, 31);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.getAttendanceHistory(null, startDate, endDate);
        });
        assertEquals("Employee ID must be positive", ex.getMessage());
    }

    @Test
    void testGetAttendanceHistory_NegativeEmployeeId_ThrowsException() {
        LocalDate startDate = LocalDate.of(2023, 3, 1);
        LocalDate endDate = LocalDate.of(2023, 3, 31);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.getAttendanceHistory(-1L, startDate, endDate);
        });
        assertEquals("Employee ID must be positive", ex.getMessage());
    }

    @Test
    void testGetAttendanceHistory_NullStartDate_ThrowsException() {
        LocalDate endDate = LocalDate.of(2023, 3, 31);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.getAttendanceHistory(1L, null, endDate);
        });
        assertEquals("Start date and end date cannot be null", ex.getMessage());
    }

    @Test
    void testGetAttendanceHistory_NullEndDate_ThrowsException() {
        LocalDate startDate = LocalDate.of(2023, 3, 1);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.getAttendanceHistory(1L, startDate, null);
        });
        assertEquals("Start date and end date cannot be null", ex.getMessage());
    }

    @Test
    void testGetAttendanceHistory_StartDateAfterEndDate_ThrowsException() {
        LocalDate startDate = LocalDate.of(2023, 4, 1);
        LocalDate endDate = LocalDate.of(2023, 3, 31);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.getAttendanceHistory(1L, startDate, endDate);
        });
        assertEquals("Start date must be before or equal to end date", ex.getMessage());
    }

    @Test
    void testGetAttendanceHistory_NonExistentEmployee_ThrowsNotFoundException() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.empty());
        LocalDate startDate = LocalDate.of(2023, 3, 1);
        LocalDate endDate = LocalDate.of(2023, 3, 31);
        EmployeeNotFoundException ex = assertThrows(EmployeeNotFoundException.class, () -> {
            attendanceService.getAttendanceHistory(1L, startDate, endDate);
        });
        assertTrue(ex.getMessage().contains("Employee not found"));
    }

    @Test
    void testGetAttendanceHistory_NoEvents_ReturnsEmptyList() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        LocalDate startDate = LocalDate.of(2023, 3, 1);
        LocalDate endDate = LocalDate.of(2023, 3, 31);
        when(attendanceEventRepository.findByEmployeeAndTimestampBetween(eq(validEmployee), any(), any())).thenReturn(Collections.emptyList());

        List<AttendanceEvent> result = attendanceService.getAttendanceHistory(1L, startDate, endDate);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
