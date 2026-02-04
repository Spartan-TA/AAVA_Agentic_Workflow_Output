package com.company.warehouse.attendance.service;

import com.company.warehouse.attendance.entity.AttendanceEvent;
import com.company.warehouse.attendance.entity.EventType;
import com.company.warehouse.attendance.repository.AttendanceEventRepository;
import com.company.warehouse.attendance.service.AttendanceService;
import com.company.warehouse.attendance.service.GeofenceService;
import com.company.warehouse.employee.entity.Employee;
import com.company.warehouse.employee.repository.EmployeeRepository;
import com.company.warehouse.attendance.dto.ClockInRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
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
class AttendanceServiceTest {
    @Mock
    private AttendanceEventRepository attendanceEventRepository;
    @Mock
    private GeofenceService geofenceService;
    @Mock
    private EmployeeRepository employeeRepository;
    @InjectMocks
    private AttendanceService attendanceService;

    private Employee employee;
    private ClockInRequest clockInRequest;

    @BeforeEach
    void setUp() {
        employee = new Employee();
        employee.setId(1L);
        employee.setName("John Doe");
        employee.setBadgeId("EMP001");

        clockInRequest = new ClockInRequest();
        clockInRequest.setEmployeeId(1L);
        clockInRequest.setDevice("Device1");
        clockInRequest.setLocation("Warehouse A");
    }

    @Test
    void clockIn_ValidLocation_ReturnsAttendanceEventWithValidStatus() {
        when(geofenceService.validateLocation(anyString())).thenReturn(true);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(attendanceEventRepository.save(any(AttendanceEvent.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AttendanceEvent result = attendanceService.clockIn(clockInRequest);
        assertNotNull(result);
        assertEquals("VALID", result.getStatus());
        assertTrue(result.getGeofenceValidated());
        assertEquals(EventType.CLOCK_IN, result.getType());
    }

    @Test
    void clockIn_InvalidLocation_ReturnsAttendanceEventWithPendingReviewStatus() {
        when(geofenceService.validateLocation(anyString())).thenReturn(false);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(attendanceEventRepository.save(any(AttendanceEvent.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AttendanceEvent result = attendanceService.clockIn(clockInRequest);
        assertNotNull(result);
        assertEquals("PENDING_REVIEW", result.getStatus());
        assertFalse(result.getGeofenceValidated());
    }

    @Test
    void clockIn_EmployeeNotFound_ThrowsException() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(Exception.class, () -> attendanceService.clockIn(clockInRequest));
    }

    @Test
    void calculateDailyHours_ValidEvents_ReturnsCorrectHours() {
        LocalDate date = LocalDate.now();
        AttendanceEvent clockIn = new AttendanceEvent();
        clockIn.setType(EventType.CLOCK_IN);
        clockIn.setTimestamp(LocalDateTime.of(date.getYear(), date.getMonth(), date.getDayOfMonth(), 8, 0));
        AttendanceEvent clockOut = new AttendanceEvent();
        clockOut.setType(EventType.CLOCK_OUT);
        clockOut.setTimestamp(LocalDateTime.of(date.getYear(), date.getMonth(), date.getDayOfMonth(), 16, 0));
        List<AttendanceEvent> events = Arrays.asList(clockIn, clockOut);
        when(attendanceEventRepository.findByEmployeeIdAndDate(1L, date)).thenReturn(events);

        Double hours = attendanceService.calculateDailyHours(1L, date);
        assertEquals(8.0, hours, 0.01);
    }

    @Test
    void calculateDailyHours_MultipleShifts_ReturnsSumOfHours() {
        LocalDate date = LocalDate.now();
        AttendanceEvent clockIn1 = new AttendanceEvent();
        clockIn1.setType(EventType.CLOCK_IN);
        clockIn1.setTimestamp(LocalDateTime.of(date.getYear(), date.getMonth(), date.getDayOfMonth(), 8, 0));
        AttendanceEvent clockOut1 = new AttendanceEvent();
        clockOut1.setType(EventType.CLOCK_OUT);
        clockOut1.setTimestamp(LocalDateTime.of(date.getYear(), date.getMonth(), date.getDayOfMonth(), 12, 0));
        AttendanceEvent clockIn2 = new AttendanceEvent();
        clockIn2.setType(EventType.CLOCK_IN);
        clockIn2.setTimestamp(LocalDateTime.of(date.getYear(), date.getMonth(), date.getDayOfMonth(), 13, 0));
        AttendanceEvent clockOut2 = new AttendanceEvent();
        clockOut2.setType(EventType.CLOCK_OUT);
        clockOut2.setTimestamp(LocalDateTime.of(date.getYear(), date.getMonth(), date.getDayOfMonth(), 17, 0));
        List<AttendanceEvent> events = Arrays.asList(clockIn1, clockOut1, clockIn2, clockOut2);
        when(attendanceEventRepository.findByEmployeeIdAndDate(1L, date)).thenReturn(events);

        Double hours = attendanceService.calculateDailyHours(1L, date);
        assertEquals(8.0, hours, 0.01);
    }

    @Test
    void calculateDailyHours_ClockInWithoutClockOut_ReturnsPartialHours() {
        LocalDate date = LocalDate.now();
        AttendanceEvent clockIn = new AttendanceEvent();
        clockIn.setType(EventType.CLOCK_IN);
        clockIn.setTimestamp(LocalDateTime.of(date.getYear(), date.getMonth(), date.getDayOfMonth(), 8, 0));
        List<AttendanceEvent> events = Collections.singletonList(clockIn);
        when(attendanceEventRepository.findByEmployeeIdAndDate(1L, date)).thenReturn(events);

        Double hours = attendanceService.calculateDailyHours(1L, date);
        assertEquals(0.0, hours, 0.01);
    }

    @Test
    void calculateDailyHours_NoEvents_ReturnsZero() {
        LocalDate date = LocalDate.now();
        when(attendanceEventRepository.findByEmployeeIdAndDate(1L, date)).thenReturn(Collections.emptyList());
        Double hours = attendanceService.calculateDailyHours(1L, date);
        assertEquals(0.0, hours, 0.01);
    }

    @Test
    void calculateDailyHours_NullEmployeeId_ThrowsException() {
        LocalDate date = LocalDate.now();
        assertThrows(Exception.class, () -> attendanceService.calculateDailyHours(null, date));
    }

    @Test
    void calculateDailyHours_NullDate_ThrowsException() {
        assertThrows(Exception.class, () -> attendanceService.calculateDailyHours(1L, null));
    }

    @Test
    void clockIn_NullRequest_ThrowsException() {
        assertThrows(NullPointerException.class, () -> attendanceService.clockIn(null));
    }
}
