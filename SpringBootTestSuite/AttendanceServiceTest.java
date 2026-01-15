package com.company.wms.attendance.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;

@ExtendWith(MockitoExtension.class)
public class AttendanceServiceTest {

    @Mock
    private AttendanceRepository attendanceRepository;
    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private AttendanceService attendanceService;

    private ClockInRequest validClockInRequest;
    private ClockOutRequest validClockOutRequest;
    private MissedPunchRequest validMissedPunchRequest;
    private Attendance attendance;

    @BeforeEach
    public void setUp() {
        validClockInRequest = new ClockInRequest(LocalDate.now(), "device123", "geofenceA");
        validClockOutRequest = new ClockOutRequest(LocalDate.now(), "device123");
        validMissedPunchRequest = new MissedPunchRequest(LocalDate.now(), "Missed clock-in");
        attendance = new Attendance(1L, 1L, LocalDate.now(), "IN", "device123", "geofenceA", null, null);
    }

    @Test
    public void testClockIn_WithValidInput_Success() {
        when(employeeRepository.existsById(anyLong())).thenReturn(true);
        when(attendanceRepository.findActiveAttendance(anyLong(), any(LocalDate.class))).thenReturn(null);
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(attendance);
        Attendance result = attendanceService.clockIn(1L, validClockInRequest);
        assertNotNull(result);
        assertEquals("IN", result.getType());
    }

    @Test
    public void testClockIn_WithNullEmployeeId_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> attendanceService.clockIn(null, validClockInRequest));
    }

    @Test
    public void testClockIn_WithInvalidEmployeeId_ThrowsResourceNotFoundException() {
        when(employeeRepository.existsById(anyLong())).thenReturn(false);
        assertThrows(ResourceNotFoundException.class, () -> attendanceService.clockIn(99L, validClockInRequest));
    }

    @Test
    public void testClockIn_WhenAlreadyClockedIn_ThrowsBusinessException() {
        when(employeeRepository.existsById(anyLong())).thenReturn(true);
        when(attendanceRepository.findActiveAttendance(anyLong(), any(LocalDate.class))).thenReturn(attendance);
        assertThrows(BusinessException.class, () -> attendanceService.clockIn(1L, validClockInRequest));
    }

    @Test
    public void testClockIn_WithGeofenceData_StoresLocationInfo() {
        when(employeeRepository.existsById(anyLong())).thenReturn(true);
        when(attendanceRepository.findActiveAttendance(anyLong(), any(LocalDate.class))).thenReturn(null);
        when(attendanceRepository.save(any(Attendance.class))).thenAnswer(invocation -> {
            Attendance att = invocation.getArgument(0);
            assertEquals("geofenceA", att.getGeofence());
            return att;
        });
        attendanceService.clockIn(1L, validClockInRequest);
    }

    @Test
    public void testClockOut_WithValidInput_Success() {
        Attendance clockedIn = new Attendance(1L, 1L, LocalDate.now(), "IN", "device123", "geofenceA", null, null);
        when(attendanceRepository.findActiveAttendance(anyLong(), any(LocalDate.class))).thenReturn(clockedIn);
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(clockedIn);
        Attendance result = attendanceService.clockOut(1L, validClockOutRequest);
        assertNotNull(result);
        assertEquals("OUT", result.getType());
    }

    @Test
    public void testClockOut_WithoutClockIn_ThrowsBusinessException() {
        when(attendanceRepository.findActiveAttendance(anyLong(), any(LocalDate.class))).thenReturn(null);
        assertThrows(BusinessException.class, () -> attendanceService.clockOut(1L, validClockOutRequest));
    }

    @Test
    public void testClockOut_WithNullEmployeeId_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> attendanceService.clockOut(null, validClockOutRequest));
    }

    @Test
    public void testCalculateHours_ForNormalShift_ReturnsCorrectHours() {
        Attendance in = new Attendance(1L, 1L, LocalDate.now(), "IN", "device123", "geofenceA", LocalDate.now().atTime(8,0), null);
        Attendance out = new Attendance(2L, 1L, LocalDate.now(), "OUT", "device123", "geofenceA", null, LocalDate.now().atTime(16,0));
        when(attendanceRepository.findByEmployeeIdAndDate(anyLong(), any(LocalDate.class))).thenReturn(Arrays.asList(in, out));
        double hours = attendanceService.calculateHours(1L, LocalDate.now());
        assertEquals(8.0, hours);
    }

    @Test
    public void testCalculateHours_ForOvernightShift_ReturnsCorrectHours() {
        Attendance in = new Attendance(1L, 1L, LocalDate.now(), "IN", "device123", "geofenceA", LocalDate.now().atTime(22,0), null);
        Attendance out = new Attendance(2L, 1L, LocalDate.now().plusDays(1), "OUT", "device123", "geofenceA", null, LocalDate.now().plusDays(1).atTime(6,0));
        when(attendanceRepository.findByEmployeeIdAndDate(anyLong(), any(LocalDate.class))).thenReturn(Arrays.asList(in, out));
        double hours = attendanceService.calculateHours(1L, LocalDate.now());
        assertEquals(8.0, hours);
    }

    @Test
    public void testCalculateHours_WithMissingClockOut_ThrowsBusinessException() {
        Attendance in = new Attendance(1L, 1L, LocalDate.now(), "IN", "device123", "geofenceA", LocalDate.now().atTime(8,0), null);
        when(attendanceRepository.findByEmployeeIdAndDate(anyLong(), any(LocalDate.class))).thenReturn(Collections.singletonList(in));
        assertThrows(BusinessException.class, () -> attendanceService.calculateHours(1L, LocalDate.now()));
    }

    @Test
    public void testHandleMissedPunch_CreatesApprovalWorkflow_Success() {
        when(employeeRepository.existsById(anyLong())).thenReturn(true);
        when(attendanceRepository.saveMissedPunch(any(MissedPunchRequest.class))).thenReturn(true);
        boolean result = attendanceService.handleMissedPunch(1L, validMissedPunchRequest);
        assertTrue(result);
    }

    @Test
    public void testExportAttendanceToCSV_WithValidDateRange_GeneratesCSV() {
        when(attendanceRepository.findByDateRange(any(LocalDate.class), any(LocalDate.class))).thenReturn(Arrays.asList(attendance));
        String csv = attendanceService.exportAttendanceToCSV(LocalDate.now().minusDays(1), LocalDate.now());
        assertTrue(csv.contains("BADGE123") || csv.length() > 0);
    }

    @Test
    public void testExportAttendanceToCSV_WithInvalidDateRange_ThrowsValidationException() {
        assertThrows(ValidationException.class, () -> attendanceService.exportAttendanceToCSV(LocalDate.now(), LocalDate.now().minusDays(1)));
    }

    @Test
    public void testGetAttendanceByEmployee_WithPagination_ReturnsPagedResults() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Attendance> page = new PageImpl<>(Arrays.asList(attendance));
        when(attendanceRepository.findByEmployeeId(eq(1L), eq(pageable))).thenReturn(page);
        Page<Attendance> result = attendanceService.getAttendanceByEmployee(1L, pageable);
        assertEquals(1, result.getTotalElements());
    }
}
