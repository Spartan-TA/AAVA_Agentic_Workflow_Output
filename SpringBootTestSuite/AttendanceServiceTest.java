package com.example.warehouse.service;

import com.example.warehouse.entity.Attendance;
import com.example.warehouse.entity.Employee;
import com.example.warehouse.exception.AlreadyClockedInException;
import com.example.warehouse.exception.AttendanceNotFoundException;
import com.example.warehouse.repository.AttendanceRepository;
import com.example.warehouse.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class AttendanceServiceTest {

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private AttendanceService attendanceService;

    private Employee employee;
    private Attendance attendance;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        employee = new Employee();
        employee.setId(1L);
        employee.setBadgeId("BADGE123");
        attendance = new Attendance();
        attendance.setId(1L);
        attendance.setEmployee(employee);
        attendance.setClockIn(LocalDateTime.now().minusHours(2));
    }

    @Test
    void testClockIn_ValidEmployee_Success() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(attendanceRepository.findByEmployeeAndClockOutIsNull(employee)).thenReturn(Optional.empty());
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(attendance);

        Attendance result = attendanceService.clockIn(1L);

        assertThat(result).isNotNull();
        assertThat(result.getEmployee().getId()).isEqualTo(1L);
        verify(attendanceRepository).save(any(Attendance.class));
    }

    @Test
    void testClockIn_AlreadyClockedIn_ThrowsException() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(attendanceRepository.findByEmployeeAndClockOutIsNull(employee)).thenReturn(Optional.of(attendance));

        assertThatThrownBy(() -> attendanceService.clockIn(1L))
                .isInstanceOf(AlreadyClockedInException.class);
    }

    @Test
    void testClockOut_ValidClockIn_CalculatesHours() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(attendanceRepository.findByEmployeeAndClockOutIsNull(employee)).thenReturn(Optional.of(attendance));
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(attendance);

        Attendance result = attendanceService.clockOut(1L);

        assertThat(result.getClockOut()).isNotNull();
        assertThat(result.getHoursWorked()).isGreaterThan(0);
        verify(attendanceRepository).save(any(Attendance.class));
    }

    @Test
    void testClockOut_NoClockIn_ThrowsException() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(attendanceRepository.findByEmployeeAndClockOutIsNull(employee)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> attendanceService.clockOut(1L))
                .isInstanceOf(AttendanceNotFoundException.class);
    }

    @Test
    void testGetAttendanceByDateRange_ValidRange_ReturnsRecords() {
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now();
        Page<Attendance> page = new PageImpl<>(Collections.singletonList(attendance));
        when(attendanceRepository.findByClockInBetween(start, end, PageRequest.of(0, 10))).thenReturn(page);

        Page<Attendance> result = attendanceService.getAttendanceByDateRange(start, end, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
    }
}