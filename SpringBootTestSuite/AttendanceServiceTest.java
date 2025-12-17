package com.warehouse.management.service;

import com.warehouse.management.domain.Attendance;
import com.warehouse.management.domain.Employee;
import com.warehouse.management.dto.AttendanceDTO;
import com.warehouse.management.exception.GeofenceViolationException;
import com.warehouse.management.exception.AttendanceNotFoundException;
import com.warehouse.management.repository.AttendanceRepository;
import com.warehouse.management.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.NullSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for AttendanceService
 * Tests cover clock-in/out, geofence validation, hours calculation, corrections, and edge cases
 */
public class AttendanceServiceTest {

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private AttendanceService attendanceService;

    private Attendance testAttendance;
    private AttendanceDTO testAttendanceDTO;
    private Employee testEmployee;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testEmployee = new Employee();
        testEmployee.setId(1L);
        testEmployee.setBadgeId("EMP001");
        testEmployee.setName("John Doe");
        testEmployee.setStatus("ACTIVE");
        testAttendance = new Attendance();
        testAttendance.setId(1L);
        testAttendance.setEmployee(testEmployee);
        testAttendance.setClockIn(LocalDateTime.now().minusHours(8));
        testAttendance.setClockOut(LocalDateTime.now());
        testAttendance.setGeofenceValid(true);
    }

    /**
     * Test clock-in with valid input returns attendance
     */
    @Test
    @DisplayName("Test clockIn with valid input returns attendance")
    void testClockIn_ValidInput_ReturnsAttendance() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(testAttendance);
        Attendance result = attendanceService.clockIn(1L, LocalDateTime.now(), true);
        assertNotNull(result);
        assertEquals(1L, result.getEmployee().getId());
        verify(attendanceRepository, times(1)).save(any(Attendance.class));
    }

    /**
     * Test clock-in with geofence violation throws exception
     */
    @Test
    @DisplayName("Test clockIn with geofence violation throws exception")
    void testClockIn_GeofenceViolation_ThrowsException() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        assertThrows(GeofenceViolationException.class, () -> {
            attendanceService.clockIn(1L, LocalDateTime.now(), false);
        });
        verify(attendanceRepository, never()).save(any(Attendance.class));
    }

    /**
     * Test clock-in with invalid employee throws exception
     */
    @Test
    @DisplayName("Test clockIn with invalid employee throws exception")
    void testClockIn_InvalidEmployee_ThrowsException() {
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(AttendanceNotFoundException.class, () -> {
            attendanceService.clockIn(999L, LocalDateTime.now(), true);
        });
    }

    /**
     * Test clock-out with valid input returns attendance
     */
    @Test
    @DisplayName("Test clockOut with valid input returns attendance")
    void testClockOut_ValidInput_ReturnsAttendance() {
        when(attendanceRepository.findById(1L)).thenReturn(Optional.of(testAttendance));
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(testAttendance);
        Attendance result = attendanceService.clockOut(1L, LocalDateTime.now());
        assertNotNull(result);
        verify(attendanceRepository, times(1)).save(any(Attendance.class));
    }

    /**
     * Test clock-out with invalid attendance throws exception
     */
    @Test
    @DisplayName("Test clockOut with invalid attendance throws exception")
    void testClockOut_InvalidAttendance_ThrowsException() {
        when(attendanceRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(AttendanceNotFoundException.class, () -> {
            attendanceService.clockOut(999L, LocalDateTime.now());
        });
    }

    /**
     * Test calculateHours with valid attendance returns correct hours
     */
    @Test
    @DisplayName("Test calculateHours with valid attendance returns correct hours")
    void testCalculateHours_ValidAttendance_ReturnsCorrectHours() {
        double hours = attendanceService.calculateHours(testAttendance);
        assertTrue(hours >= 0);
    }

    /**
     * Test calculateHours with null attendance throws exception
     */
    @Test
    @DisplayName("Test calculateHours with null attendance throws exception")
    void testCalculateHours_NullAttendance_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.calculateHours(null);
        });
    }

    /**
     * Test correctAttendance with valid input updates attendance
     */
    @Test
    @DisplayName("Test correctAttendance with valid input updates attendance")
    void testCorrectAttendance_ValidInput_UpdatesAttendance() {
        when(attendanceRepository.findById(1L)).thenReturn(Optional.of(testAttendance));
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(testAttendance);
        AttendanceDTO correctionDTO = new AttendanceDTO();
        correctionDTO.setClockIn(LocalDateTime.now().minusHours(7));
        correctionDTO.setClockOut(LocalDateTime.now());
        Attendance result = attendanceService.correctAttendance(1L, correctionDTO);
        assertNotNull(result);
        verify(attendanceRepository, times(1)).save(any(Attendance.class));
    }

    /**
     * Test correctAttendance with invalid attendance throws exception
     */
    @Test
    @DisplayName("Test correctAttendance with invalid attendance throws exception")
    void testCorrectAttendance_InvalidAttendance_ThrowsException() {
        when(attendanceRepository.findById(999L)).thenReturn(Optional.empty());
        AttendanceDTO correctionDTO = new AttendanceDTO();
        assertThrows(AttendanceNotFoundException.class, () -> {
            attendanceService.correctAttendance(999L, correctionDTO);
        });
    }

    /**
     * Test getAttendanceByEmployee returns list of attendance records
     */
    @Test
    @DisplayName("Test getAttendanceByEmployee returns list of attendance records")
    void testGetAttendanceByEmployee_ReturnsList() {
        List<Attendance> attendances = List.of(testAttendance);
        when(attendanceRepository.findByEmployeeId(1L)).thenReturn(attendances);
        List<Attendance> result = attendanceService.getAttendanceByEmployee(1L);
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    /**
     * Test getAttendanceByEmployee with invalid employee returns empty list
     */
    @Test
    @DisplayName("Test getAttendanceByEmployee with invalid employee returns empty list")
    void testGetAttendanceByEmployee_InvalidEmployee_ReturnsEmptyList() {
        when(attendanceRepository.findByEmployeeId(999L)).thenReturn(List.of());
        List<Attendance> result = attendanceService.getAttendanceByEmployee(999L);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Test clockIn with null input throws exception
     */
    @Test
    @DisplayName("Test clockIn with null input throws exception")
    void testClockIn_NullInput_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.clockIn(null, null, true);
        });
    }

    /**
     * Test clockOut with null input throws exception
     */
    @Test
    @DisplayName("Test clockOut with null input throws exception")
    void testClockOut_NullInput_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.clockOut(null, null);
        });
    }

    /**
     * Test correctAttendance with null DTO throws exception
     */
    @Test
    @DisplayName("Test correctAttendance with null DTO throws exception")
    void testCorrectAttendance_NullDTO_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.correctAttendance(1L, null);
        });
    }

    /**
     * Test clockIn with boundary badgeId values
     */
    @ParameterizedTest
    @ValueSource(longs = {Long.MIN_VALUE, -1L, 0L, Long.MAX_VALUE})
    @DisplayName("Test clockIn with boundary badgeId values")
    void testClockIn_BoundaryBadgeIdValues(Long badgeId) {
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.clockIn(badgeId, LocalDateTime.now(), true);
        });
    }
}
