package com.warehouse.management.attendance;

import com.warehouse.management.attendance.AttendanceService;
import com.warehouse.management.attendance.AttendanceEvent;
import com.warehouse.management.employee.Employee;
import org.junit.jupiter.api.*;
import org.mockito.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AttendanceServiceTest {

    @Mock
    private AttendanceRepository attendanceRepository;

    @InjectMocks
    private AttendanceService attendanceService;

    private Employee employee;
    private AttendanceEvent clockInEvent;
    private AttendanceEvent clockOutEvent;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        employee = new Employee(1L, "John Doe", "BADGE123", "WORKER", "Logistics", "A", new Date(), "ACTIVE");
        clockInEvent = new AttendanceEvent(1L, employee, new Date(), "CLOCK_IN", "device123", "geo123");
        clockOutEvent = new AttendanceEvent(2L, employee, new Date(), "CLOCK_OUT", "device123", "geo123");
    }

    @Test
    void testClockIn_Valid() {
        when(attendanceRepository.save(any(AttendanceEvent.class))).thenReturn(clockInEvent);
        AttendanceEvent result = attendanceService.clockIn(employee, "device123", "geo123");
        assertNotNull(result);
        assertEquals("CLOCK_IN", result.getType());
    }

    @Test
    void testClockIn_NullEmployee() {
        assertThrows(IllegalArgumentException.class, () -> attendanceService.clockIn(null, "device123", "geo123"));
    }

    @Test
    void testClockOut_Valid() {
        when(attendanceRepository.save(any(AttendanceEvent.class))).thenReturn(clockOutEvent);
        AttendanceEvent result = attendanceService.clockOut(employee, "device123", "geo123");
        assertNotNull(result);
        assertEquals("CLOCK_OUT", result.getType());
    }

    @Test
    void testGeofenceValidation_Valid() {
        boolean result = attendanceService.validateGeofence("geo123", "geo123");
        assertTrue(result);
    }

    @Test
    void testGeofenceValidation_Invalid() {
        boolean result = attendanceService.validateGeofence("geo123", "geo456");
        assertFalse(result);
    }

    @Test
    void testHoursCalculation_Normal() {
        when(attendanceRepository.findByEmployeeAndDate(any(), any())).thenReturn(Arrays.asList(clockInEvent, clockOutEvent));
        double hours = attendanceService.calculateHours(employee, new Date());
        assertTrue(hours >= 0);
    }

    @Test
    void testHoursCalculation_MissedPunch() {
        when(attendanceRepository.findByEmployeeAndDate(any(), any())).thenReturn(Arrays.asList(clockInEvent));
        assertThrows(IllegalStateException.class, () -> attendanceService.calculateHours(employee, new Date()));
    }
}