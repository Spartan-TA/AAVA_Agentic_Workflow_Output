package com.example.warehouseems;

import com.example.warehouseems.attendance.AttendanceService;
import com.example.warehouseems.attendance.Attendance;
import com.example.warehouseems.attendance.AttendanceRepository;
import com.example.warehouseems.employee.Employee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AttendanceServiceTest {
    @Mock
    private AttendanceRepository attendanceRepository;

    @InjectMocks
    private AttendanceService attendanceService;

    private Employee employee;
    private Attendance attendance;

    @BeforeEach
    void setUp() {
        employee = new Employee();
        employee.setId(1L);
        employee.setName("John Doe");
        attendance = new Attendance();
        attendance.setId(1L);
        attendance.setEmployee(employee);
        attendance.setDate(LocalDate.now());
        attendance.setClockIn(LocalDateTime.now().minusHours(8));
        attendance.setClockOut(LocalDateTime.now());
    }

    @Test
    void clockIn_validEmployee_successfulClockIn() {
        when(attendanceRepository.findByEmployeeAndDate(any(), any())).thenReturn(Optional.empty());
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(attendance);
        Attendance result = attendanceService.clockIn(employee, LocalDateTime.now(), "valid-geofence");
        assertNotNull(result);
        verify(attendanceRepository).save(any(Attendance.class));
    }

    @Test
    void clockIn_duplicateClockIn_throwsException() {
        when(attendanceRepository.findByEmployeeAndDate(any(), any())).thenReturn(Optional.of(attendance));
        assertThrows(IllegalStateException.class, () -> attendanceService.clockIn(employee, LocalDateTime.now(), "valid-geofence"));
    }

    @Test
    void clockIn_invalidGeofence_throwsException() {
        when(attendanceRepository.findByEmployeeAndDate(any(), any())).thenReturn(Optional.empty());
        assertThrows(SecurityException.class, () -> attendanceService.clockIn(employee, LocalDateTime.now(), "invalid-geofence"));
    }

    @Test
    void clockOut_validEmployee_successfulClockOut() {
        Attendance incomplete = new Attendance();
        incomplete.setEmployee(employee);
        incomplete.setDate(LocalDate.now());
        incomplete.setClockIn(LocalDateTime.now().minusHours(8));
        when(attendanceRepository.findByEmployeeAndDate(any(), any())).thenReturn(Optional.of(incomplete));
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(attendance);
        Attendance result = attendanceService.clockOut(employee, LocalDateTime.now(), "valid-geofence");
        assertNotNull(result);
        verify(attendanceRepository).save(any(Attendance.class));
    }

    @Test
    void clockOut_withoutClockIn_throwsException() {
        when(attendanceRepository.findByEmployeeAndDate(any(), any())).thenReturn(Optional.empty());
        assertThrows(IllegalStateException.class, () -> attendanceService.clockOut(employee, LocalDateTime.now(), "valid-geofence"));
    }

    @Test
    void calculateDailyHours_validAttendance_returnsHours() {
        double hours = attendanceService.calculateDailyHours(attendance);
        assertTrue(hours > 0);
    }

    @Test
    void calculateDailyHours_missingClockOut_returnsZero() {
        attendance.setClockOut(null);
        double hours = attendanceService.calculateDailyHours(attendance);
        assertEquals(0.0, hours);
    }

    @Test
    void detectMissedPunches_employeeWithMissedPunch_returnsList() {
        Attendance missed = new Attendance();
        missed.setEmployee(employee);
        missed.setDate(LocalDate.now());
        missed.setClockIn(LocalDateTime.now().minusHours(8));
        missed.setClockOut(null);
        List<Attendance> missedList = Arrays.asList(missed);
        when(attendanceRepository.findByEmployeeAndClockOutIsNull(any())).thenReturn(missedList);
        List<Attendance> result = attendanceService.detectMissedPunches(employee);
        assertEquals(1, result.size());
    }

    @Test
    void correctAttendance_validCorrection_successfulCorrection() {
        when(attendanceRepository.findById(1L)).thenReturn(Optional.of(attendance));
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(attendance);
        Attendance corrected = attendanceService.correctAttendance(1L, LocalDateTime.now().minusHours(7), LocalDateTime.now(), "admin");
        assertNotNull(corrected);
        verify(attendanceRepository).save(any(Attendance.class));
    }

    @Test
    void correctAttendance_invalidId_throwsException() {
        when(attendanceRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> attendanceService.correctAttendance(2L, LocalDateTime.now(), LocalDateTime.now(), "admin"));
    }

    @Test
    void correctAttendance_nullCorrection_throwsException() {
        when(attendanceRepository.findById(1L)).thenReturn(Optional.of(attendance));
        assertThrows(IllegalArgumentException.class, () -> attendanceService.correctAttendance(1L, null, null, "admin"));
    }
}
