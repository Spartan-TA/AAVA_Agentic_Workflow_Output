package com.company.warehouse.attendance.service;

import com.company.warehouse.attendance.domain.*;
import com.company.warehouse.attendance.dto.*;
import com.company.warehouse.attendance.repository.AttendanceEventRepository;
import com.company.warehouse.employee.domain.*;
import com.company.warehouse.employee.repository.EmployeeRepository;
import com.company.warehouse.common.exception.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("Attendance Service Tests")
public class AttendanceServiceTest {
    @Mock private AttendanceEventRepository attendanceEventRepository;
    @Mock private EmployeeRepository employeeRepository;
    @Mock private GeofenceService geofenceService;
    @InjectMocks private AttendanceService attendanceService;
    private Employee testEmployee;
    private AttendanceEvent clockInEvent;
    private ClockInRequest clockInRequest;
    private ClockOutRequest clockOutRequest;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        testEmployee = new Employee();
        testEmployee.setId(1L);
        testEmployee.setBadgeId("EMP001");
        testEmployee.setStatus(Status.ACTIVE);
        clockInEvent = new AttendanceEvent();
        clockInEvent.setEventType(EventType.CLOCK_IN);
        clockInEvent.setTimestamp(LocalDateTime.now().minusHours(8));
        clockInRequest = new ClockInRequest();
        clockInRequest.setBadgeId("EMP001");
        clockOutRequest = new ClockOutRequest();
        clockOutRequest.setBadgeId("EMP001");
    }

    @Test
    @DisplayName("Test clockIn with valid badge ID")
    public void testClockIn_ValidBadgeId() {
        when(employeeRepository.findByBadgeId("EMP001")).thenReturn(Optional.of(testEmployee));
        when(attendanceEventRepository.findLastEventByEmployee(1L)).thenReturn(Optional.empty());
        when(attendanceEventRepository.save(any(AttendanceEvent.class))).thenReturn(clockInEvent);
        AttendanceEventDTO result = attendanceService.clockIn(clockInRequest);
        assertNotNull(result);
        verify(attendanceEventRepository, times(1)).save(any(AttendanceEvent.class));
    }

    @Test
    @DisplayName("Test clockIn with invalid badge ID")
    public void testClockIn_InvalidBadgeId() {
        when(employeeRepository.findByBadgeId("INVALID")).thenReturn(Optional.empty());
        clockInRequest.setBadgeId("INVALID");
        assertThrows(ResourceNotFoundException.class, () -> attendanceService.clockIn(clockInRequest));
    }

    @Test
    @DisplayName("Test clockIn when already clocked in")
    public void testClockIn_AlreadyClockedIn() {
        when(employeeRepository.findByBadgeId("EMP001")).thenReturn(Optional.of(testEmployee));
        when(attendanceEventRepository.findLastEventByEmployee(1L)).thenReturn(Optional.of(clockInEvent));
        assertThrows(BusinessException.class, () -> attendanceService.clockIn(clockInRequest));
    }

    @Test
    @DisplayName("Test clockOut with valid badge ID")
    public void testClockOut_ValidBadgeId() {
        when(employeeRepository.findByBadgeId("EMP001")).thenReturn(Optional.of(testEmployee));
        when(attendanceEventRepository.findLastEventByEmployee(1L)).thenReturn(Optional.of(clockInEvent));
        when(attendanceEventRepository.save(any(AttendanceEvent.class))).thenReturn(new AttendanceEvent());
        AttendanceEventDTO result = attendanceService.clockOut(clockOutRequest);
        assertNotNull(result);
    }

    @Test
    @DisplayName("Test clockOut without clock in")
    public void testClockOut_WithoutClockIn() {
        when(employeeRepository.findByBadgeId("EMP001")).thenReturn(Optional.of(testEmployee));
        when(attendanceEventRepository.findLastEventByEmployee(1L)).thenReturn(Optional.empty());
        assertThrows(BusinessException.class, () -> attendanceService.clockOut(clockOutRequest));
    }