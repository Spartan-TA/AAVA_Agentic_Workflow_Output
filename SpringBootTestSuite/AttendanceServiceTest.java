package com.example.warehouse.test;

import com.example.warehouse.attendance.Attendance;
import com.example.warehouse.attendance.AttendanceRepository;
import com.example.warehouse.attendance.AttendanceService;
import com.example.warehouse.attendance.AttendanceController;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.http.ResponseEntity;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AttendanceServiceTest {
    @Mock
    private AttendanceRepository attendanceRepository;
    @InjectMocks
    private AttendanceService attendanceService;
    private AttendanceController attendanceController;
    private Attendance testAttendance;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        attendanceController = new AttendanceController(attendanceService);
        testAttendance = new Attendance(1L, 1L, LocalDateTime.now(), null, "IN", "device1", "geo1");
    }

    @AfterEach
    void tearDown() {
        // Cleanup if needed
    }

    @Test
    void testClockIn_ValidInput_Success() {
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(testAttendance);
        Attendance created = attendanceService.clockIn(testAttendance);
        assertNotNull(created);
        assertEquals("IN", created.getType());
    }

    @Test
    void testClockOut_ValidInput_Success() {
        Attendance out = new Attendance(1L, 1L, LocalDateTime.now().minusHours(8), LocalDateTime.now(), "OUT", "device1", "geo1");
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(out);
        Attendance result = attendanceService.clockOut(out);
        assertNotNull(result);
        assertEquals("OUT", result.getType());
    }

    @Test
    void testClockIn_InvalidGeofence_ThrowsException() {
        Attendance invalid = new Attendance(1L, 1L, LocalDateTime.now(), null, "IN", "device1", "invalid");
        when(attendanceService.isWithinGeofence(anyString())).thenReturn(false);
        assertThrows(IllegalArgumentException.class, () -> attendanceService.clockIn(invalid));
    }

    @Test
    void testClockIn_NullInput_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> attendanceService.clockIn(null));
    }

    @Test
    void testGetAttendanceByEmployeeId_EmptyList() {
        when(attendanceRepository.findByEmployeeId(2L)).thenReturn(Collections.emptyList());
        List<Attendance> result = attendanceService.getAttendanceByEmployeeId(2L);
        assertTrue(result.isEmpty());
    }

    @Test
    void testController_ClockIn_Success() {
        when(attendanceService.clockIn(any(Attendance.class))).thenReturn(testAttendance);
        ResponseEntity<Attendance> response = attendanceController.clockIn(testAttendance);
        assertEquals(201, response.getStatusCodeValue());
        assertEquals("IN", response.getBody().getType());
    }

    @Test
    void testController_ClockOut_Success() {
        Attendance out = new Attendance(1L, 1L, LocalDateTime.now().minusHours(8), LocalDateTime.now(), "OUT", "device1", "geo1");
        when(attendanceService.clockOut(any(Attendance.class))).thenReturn(out);
        ResponseEntity<Attendance> response = attendanceController.clockOut(out);
        assertEquals(201, response.getStatusCodeValue());
        assertEquals("OUT", response.getBody().getType());
    }

    @Test
    void testController_ClockIn_InvalidGeofence() {
        when(attendanceService.clockIn(any(Attendance.class))).thenThrow(new IllegalArgumentException("Invalid geofence"));
        assertThrows(IllegalArgumentException.class, () -> attendanceController.clockIn(testAttendance));
    }
}
