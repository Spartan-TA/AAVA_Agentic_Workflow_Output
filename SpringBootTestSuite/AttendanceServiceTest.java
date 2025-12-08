import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.*;

public class AttendanceServiceTest {
    @Mock
    private AttendanceRepository attendanceRepository;
    @Mock
    private EmployeeRepository employeeRepository;
    @InjectMocks
    private AttendanceService attendanceService;
    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    void testClockIn_ValidInput() {
        Employee employee = new Employee("John Doe", "B123", "WORKER", "Shipping", "A", new Date(), "ACTIVE");
        Attendance attendance = new Attendance(employee, new Date(), null, "device1", "geo1");
        when(employeeRepository.findByBadgeId("B123")).thenReturn(Optional.of(employee));
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(attendance);
        Attendance result = attendanceService.clockIn("B123", "device1", "geo1");
        assertNotNull(result.getClockInTime());
        assertNull(result.getClockOutTime());
    }

    @Test
    void testClockIn_NullBadgeId() {
        assertThrows(IllegalArgumentException.class, () -> attendanceService.clockIn(null, "device1", "geo1"));
    }

    @Test
    void testClockIn_InvalidBadgeId() {
        when(employeeRepository.findByBadgeId("BADGE999")).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> attendanceService.clockIn("BADGE999", "device1", "geo1"));
    }

    @Test
    void testClockOut_ValidInput() {
        Employee employee = new Employee("John Doe", "B123", "WORKER", "Shipping", "A", new Date(), "ACTIVE");
        Attendance attendance = new Attendance(employee, new Date(System.currentTimeMillis() - 3600000), null, "device1", "geo1");
        when(attendanceRepository.findOpenAttendance("B123")).thenReturn(Optional.of(attendance));
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(attendance);
        Attendance result = attendanceService.clockOut("B123", "device1", "geo1");
        assertNotNull(result.getClockOutTime());
        assertTrue(result.getClockOutTime().after(result.getClockInTime()));
    }

    @Test
    void testClockOut_MissedPunch() {
        when(attendanceRepository.findOpenAttendance("B124")).thenReturn(Optional.empty());
        assertThrows(MissedPunchException.class, () -> attendanceService.clockOut("B124", "device1", "geo1"));
    }

    @Test
    void testCalculateHoursWorked_ValidShift() {
        Date in = new Date(System.currentTimeMillis() - 7200000); // 2 hours ago
        Date out = new Date();
        Attendance attendance = new Attendance(null, in, out, "device1", "geo1");
        double hours = attendanceService.calculateHoursWorked(attendance);
        assertEquals(2.0, hours, 0.01);
    }

    @Test
    void testCalculateHoursWorked_NullTimes() {
        Attendance attendance = new Attendance(null, null, null, "device1", "geo1");
        assertThrows(IllegalArgumentException.class, () -> attendanceService.calculateHoursWorked(attendance));
    }

    @Test
    void testClockIn_GeofenceValidation() {
        Employee employee = new Employee("John Doe", "B123", "WORKER", "Shipping", "A", new Date(), "ACTIVE");
        when(employeeRepository.findByBadgeId("B123")).thenReturn(Optional.of(employee));
        when(attendanceService.isWithinGeofence("geo1")).thenReturn(false);
        assertThrows(GeofenceViolationException.class, () -> attendanceService.clockIn("B123", "device1", "geo1"));
    }

    @Test
    void testClockIn_MinMaxBoundaryValues() {
        Employee employee = new Employee("A", "B126", "WORKER", "Shipping", "A", new Date(), "ACTIVE");
        when(employeeRepository.findByBadgeId("B126")).thenReturn(Optional.of(employee));
        Attendance attendance = new Attendance(employee, new Date(), null, "device1", "geo1");
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(attendance);
        assertDoesNotThrow(() -> attendanceService.clockIn("B126", "device1", "geo1"));
    }

    @Test
    void testClockOut_InvalidFormat() {
        assertThrows(IllegalArgumentException.class, () -> attendanceService.clockOut("", "device1", "geo1"));
    }
}
