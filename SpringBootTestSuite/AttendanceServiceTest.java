package com.example.service;

import com.example.entity.Attendance;
import com.example.entity.Employee;
import com.example.repository.AttendanceRepository;
import com.example.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AttendanceServiceTest {

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private AttendanceService attendanceService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void clockIn_validEmployee_success() {
        Employee emp = new Employee();
        emp.setId(1L);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(emp));
        Attendance att = new Attendance();
        when(attendanceRepository.save(any())).thenReturn(att);

        Attendance result = attendanceService.clockIn(1L, LocalDateTime.now(), "device1", "geofence1");

        assertNotNull(result);
        verify(attendanceRepository).save(any());
    }

    @Test
    void clockIn_employeeNotFound_throwsException() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> attendanceService.clockIn(1L, LocalDateTime.now(), "device1", "geofence1"));
    }
}