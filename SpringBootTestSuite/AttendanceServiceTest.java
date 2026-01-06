package com.warehouse.ems.attendance.service;

import com.warehouse.ems.attendance.entity.AttendanceEvent;
import com.warehouse.ems.attendance.repository.AttendanceEventRepository;
import com.warehouse.ems.entity.Employee;
import com.warehouse.ems.repository.EmployeeRepository;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AttendanceServiceTest {
    @Mock
    private AttendanceEventRepository attendanceEventRepository;
    @Mock
    private EmployeeRepository employeeRepository;
    @InjectMocks
    private AttendanceService attendanceService;
    private Employee employee;
    private AttendanceEvent clockInEvent;
    private AttendanceEvent clockOutEvent;

    @BeforeEach
    void setUp() {
        employee = new Employee();
        employee.setId(1L);
        employee.setBadgeId("B12345");
        employee.setName("John Doe");
        employee.setStatus("ACTIVE");
        
        clockInEvent = new AttendanceEvent();
        clockInEvent.setId(1L);
        clockInEvent.setEmployeeId(1L);
        clockInEvent.setEventType("CLOCK_IN");
        clockInEvent.setTimestamp(LocalDateTime.now());
        clockInEvent.setDeviceId("DEVICE001");
        clockInEvent.setGeofence("WAREHOUSE_A");
        
        clockOutEvent = new AttendanceEvent();
        clockOutEvent.setId(2L);
        clockOutEvent.setEmployeeId(1L);
        clockOutEvent.setEventType("CLOCK_OUT");
        clockOutEvent.setTimestamp(LocalDateTime.now().plusHours(8));
        clockOutEvent.setDeviceId("DEVICE001");
        clockOutEvent.setGeofence("WAREHOUSE_A");
    }

    @AfterEach
    void tearDown() {
        employee = null;
        clockInEvent = null;
        clockOutEvent = null;
    }

    @Test
    void testClockIn_ValidEmployeeId_CreatesEvent() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(attendanceEventRepository.save(any(AttendanceEvent.class))).thenReturn(clockInEvent);
        AttendanceEvent result = attendanceService.clockIn(1L, "DEVICE001", "WAREHOUSE_A");
        assertNotNull(result);
        assertEquals("CLOCK_IN", result.getEventType());
        verify(attendanceEventRepository, times(1)).save(any(AttendanceEvent.class));
    }

    @Test
    void testClockIn_NullEmployeeId_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> attendanceService.clockIn(null, "DEVICE001", "WAREHOUSE_A"));
    }

    @Test
    void testClockIn_NonExistentEmployee_ThrowsException() {
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> attendanceService.clockIn(999L, "DEVICE001", "WAREHOUSE_A"));
    }

    @Test
    void testClockOut_ValidData_CalculatesHours() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(attendanceEventRepository.findLastClockInByEmployeeId(1L)).thenReturn(Optional.of(clockInEvent));
        when(attendanceEventRepository.save(any(AttendanceEvent.class))).thenReturn(clockOutEvent);
        AttendanceEvent result = attendanceService.clockOut(1L, "DEVICE001", "WAREHOUSE_A");
        assertNotNull(result);
        assertEquals("CLOCK_OUT", result.getEventType());
    }

    @Test
    void testClockOut_WithoutClockIn_ThrowsException() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(attendanceEventRepository.findLastClockInByEmployeeId(1L)).thenReturn(Optional.empty());
        assertThrows(IllegalStateException.class, () -> attendanceService.clockOut(1L, "DEVICE001", "WAREHOUSE_A"));
    }

    @Test
    void testClockIn_TwiceWithoutClockOut_ThrowsException() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(attendanceEventRepository.findLastClockInByEmployeeId(1L)).thenReturn(Optional.of(clockInEvent));
        assertThrows(IllegalStateException.class, () -> attendanceService.clockIn(1L, "DEVICE001", "WAREHOUSE_A"));
    }

    @Test
    void testCalculateHours_NormalShift_ReturnsCorrectHours() {
        LocalDateTime start = LocalDateTime.of(2024, 1, 1, 8, 0);
        LocalDateTime end = LocalDateTime.of(2024, 1, 1, 16, 0);
        double hours = attendanceService.calculateHours(start, end);
        assertEquals(8.0, hours, 0.01);
    }

    @Test
    void testCalculateHours_OvernightShift_ReturnsCorrectHours() {
        LocalDateTime start = LocalDateTime.of(2024, 1, 1, 22, 0);
        LocalDateTime end = LocalDateTime.of(2024, 1, 2, 6, 0);
        double hours = attendanceService.calculateHours(start, end);
        assertEquals(8.0, hours, 0.01);
    }

    @Test
    void testCalculateHours_WithMissedPunch_ReturnsZero() {
        LocalDateTime start = LocalDateTime.of(2024, 1, 1, 8, 0);
        double hours = attendanceService.calculateHours(start, null);
        assertEquals(0.0, hours, 0.01);
    }

    @Test
    void testHandleMissedPunch_CreatesCorrectionRequest() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(attendanceEventRepository.save(any(AttendanceEvent.class))).thenReturn(clockInEvent);
        AttendanceEvent correction = attendanceService.handleMissedPunch(1L, LocalDateTime.now(), "CLOCK_IN", "Forgot to clock in");
        assertNotNull(correction);
        assertEquals("CORRECTION_PENDING", correction.getStatus());
    }

    @Test
    void testGeofenceValidation_ValidLocation_Passes() {
        boolean isValid = attendanceService.validateGeofence("WAREHOUSE_A", 40.7128, -74.0060);
        assertTrue(isValid);
    }

    @Test
    void testGeofenceValidation_InvalidLocation_Fails() {
        boolean isValid = attendanceService.validateGeofence("WAREHOUSE_A", 0.0, 0.0);
        assertFalse(isValid);
    }

    @Test
    void testDeviceCapture_RecordsDeviceId() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(attendanceEventRepository.save(any(AttendanceEvent.class))).thenReturn(clockInEvent);
        AttendanceEvent result = attendanceService.clockIn(1L, "DEVICE002", "WAREHOUSE_A");
        assertNotNull(result.getDeviceId());
    }

    @Test
    void testShiftAssociation_AutomaticallyLinksShift() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(attendanceEventRepository.save(any(AttendanceEvent.class))).thenReturn(clockInEvent);
        AttendanceEvent result = attendanceService.clockIn(1L, "DEVICE001", "WAREHOUSE_A");
        assertNotNull(result.getShiftId());
    }

    @Test
    void testDailyTotalsComputation_CalculatesCorrectly() {
        List<AttendanceEvent> events = Arrays.asList(clockInEvent, clockOutEvent);
        when(attendanceEventRepository.findByEmployeeIdAndDate(1L, LocalDate.now())).thenReturn(events);
        double totalHours = attendanceService.calculateDailyTotals(1L, LocalDate.now());
        assertEquals(8.0, totalHours, 0.01);
    }

    @Test
    void testCSVExportGeneration_GeneratesFile() {
        List<AttendanceEvent> events = Arrays.asList(clockInEvent, clockOutEvent);
        when(attendanceEventRepository.findByDateRange(any(LocalDate.class), any(LocalDate.class))).thenReturn(events);
        String csv = attendanceService.exportToCSV(LocalDate.now(), LocalDate.now());
        assertNotNull(csv);
        assertTrue(csv.contains("CLOCK_IN"));
    }
}