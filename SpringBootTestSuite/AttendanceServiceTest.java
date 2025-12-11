package com.wms.employee.service;

import com.wms.employee.entity.Attendance;
import com.wms.employee.entity.Employee;
import com.wms.employee.exception.ResourceNotFoundException;
import com.wms.employee.repository.AttendanceRepository;
import com.wms.employee.repository.EmployeeRepository;
import com.wms.employee.request.AttendanceRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AttendanceServiceTest {

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private AttendanceService attendanceService;

    private Employee employee;
    private Attendance attendance;
    private AttendanceRequest attendanceRequest;

    @BeforeEach
    void setUp() {
        employee = new Employee(1L, "B123", "John Doe", "Worker", "Logistics", "A", null, "ACTIVE", false);
        attendance = new Attendance(1L, employee, LocalDateTime.now(), null, "Morning", "D1", "Main Gate", "PRESENT");
        attendanceRequest = new AttendanceRequest(1L, "D1", "Main Gate", "Morning");
    }

    @Test
    void testClockIn_ValidEmployeeId_Success() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(attendance);

        Attendance result = attendanceService.clockIn(attendanceRequest);
        assertNotNull(result);
        assertEquals("PRESENT", result.getStatus());
    }

    @Test
    void testClockIn_NonExistentEmployeeId_ThrowsResourceNotFoundException() {
        attendanceRequest.setEmployeeId(99L);
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> attendanceService.clockIn(attendanceRequest));
    }

    @Test
    void testClockIn_NullDeviceId_ThrowsValidationException() {
        attendanceRequest.setDeviceId(null);
        assertThrows(IllegalArgumentException.class, () -> attendanceService.clockIn(attendanceRequest));
    }

    @Test
    void testClockOut_ValidAttendanceId_Success() {
        when(attendanceRepository.findById(1L)).thenReturn(Optional.of(attendance));
        attendance.setClockOut(null);
        Attendance result = attendanceService.clockOut(1L);
        assertNotNull(result.getClockOut());
    }

    @Test
    void testClockOut_NonExistentAttendanceId_ThrowsResourceNotFoundException() {
        when(attendanceRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> attendanceService.clockOut(99L));
    }

    @Test
    void testClockOut_AlreadyClockedOut_ThrowsException() {
        attendance.setClockOut(LocalDateTime.now());
        when(attendanceRepository.findById(1L)).thenReturn(Optional.of(attendance));
        assertThrows(IllegalStateException.class, () -> attendanceService.clockOut(1L));
    }
}