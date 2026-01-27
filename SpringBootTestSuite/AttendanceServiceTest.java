package SpringBootTestSuite;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.*;

public class AttendanceServiceTest {
    @Mock
    private AttendanceRepository attendanceRepository;
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private ShiftRepository shiftRepository;

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
    void testClockIn_Valid_Success() {
        Employee employee = new Employee("John Doe", "B123", "WORKER", "Shipping", "A", new Date(), "ACTIVE");
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        Attendance attendance = new Attendance(1L, 1L, new Date(), null, "CLOCKED_IN");
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(attendance);
        Attendance result = attendanceService.clockIn(1L, new Date(), "device1", "location1");
        assertNotNull(result);
        assertEquals("CLOCKED_IN", result.getStatus());
    }

    @Test
    void testClockOut_Valid_Success() {
        Attendance attendance = new Attendance(1L, 1L, new Date(System.currentTimeMillis()-3600000), null, "CLOCKED_IN");
        when(attendanceRepository.findOpenByEmployeeId(1L)).thenReturn(Optional.of(attendance));
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(attendance);
        Attendance result = attendanceService.clockOut(1L, new Date(), "device1", "location1");
        assertNotNull(result);
        assertEquals("CLOCKED_OUT", result.getStatus());
    }

    @Test
    void testClockIn_AlreadyClockedIn_ThrowsException() {
        Attendance attendance = new Attendance(1L, 1L, new Date(), null, "CLOCKED_IN");
        when(attendanceRepository.findOpenByEmployeeId(1L)).thenReturn(Optional.of(attendance));
        assertThrows(AlreadyClockedInException.class, () -> attendanceService.clockIn(1L, new Date(), "device1", "location1"));
    }

    @Test
    void testClockOut_NotClockedIn_ThrowsException() {
        when(attendanceRepository.findOpenByEmployeeId(1L)).thenReturn(Optional.empty());
        assertThrows(NotClockedInException.class, () -> attendanceService.clockOut(1L, new Date(), "device1", "location1"));
    }

    @Test
    void testCalculateHoursWorked_Valid_Success() {
        Date clockIn = new Date(System.currentTimeMillis()-7200000); // 2 hours ago
        Date clockOut = new Date();
        Attendance attendance = new Attendance(1L, 1L, clockIn, clockOut, "CLOCKED_OUT");
        when(attendanceRepository.findById(1L)).thenReturn(Optional.of(attendance));
        double hours = attendanceService.calculateHoursWorked(1L);
        assertTrue(hours >= 1.99 && hours <= 2.01);
    }

    @Test
    void testCalculateHoursWorked_MissingClockOut_ThrowsException() {
        Attendance attendance = new Attendance(1L, 1L, new Date(), null, "CLOCKED_IN");
        when(attendanceRepository.findById(1L)).thenReturn(Optional.of(attendance));
        assertThrows(MissingClockOutException.class, () -> attendanceService.calculateHoursWorked(1L));
    }

    @Test
    void testSubmitCorrection_Valid_Success() {
        Attendance attendance = new Attendance(1L, 1L, new Date(), new Date(), "CLOCKED_OUT");
        when(attendanceRepository.findById(1L)).thenReturn(Optional.of(attendance));
        Correction correction = new Correction(1L, 1L, "Missed punch", "PENDING");
        when(attendanceRepository.saveCorrection(any(Correction.class))).thenReturn(correction);
        Correction result = attendanceService.submitCorrection(1L, "Missed punch");
        assertNotNull(result);
        assertEquals("PENDING", result.getStatus());
    }

    @Test
    void testSubmitCorrection_InvalidAttendance_ThrowsException() {
        when(attendanceRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(AttendanceNotFoundException.class, () -> attendanceService.submitCorrection(99L, "Missed punch"));
    }

    @Test
    void testClockIn_NullDevice_ThrowsException() {
        assertThrows(InvalidDeviceException.class, () -> attendanceService.clockIn(1L, new Date(), null, "location1"));
    }

    @Test
    void testClockIn_EmptyLocation_Success() {
        Employee employee = new Employee("John Doe", "B123", "WORKER", "Shipping", "A", new Date(), "ACTIVE");
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        Attendance attendance = new Attendance(1L, 1L, new Date(), null, "CLOCKED_IN");
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(attendance);
        Attendance result = attendanceService.clockIn(1L, new Date(), "device1", "");
        assertNotNull(result);
    }

    // Integration scenario: Attendance triggers payroll export
    @Test
    void testAttendanceTriggersPayrollExport_Success() {
        Attendance attendance = new Attendance(1L, 1L, new Date(), new Date(), "CLOCKED_OUT");
        when(attendanceRepository.findById(1L)).thenReturn(Optional.of(attendance));
        PayrollExport export = new PayrollExport(1L, 1L, 8.0, "PENDING");
        when(payrollService.generateExportForAttendance(1L)).thenReturn(export);
        PayrollExport result = attendanceService.triggerPayrollExport(1L);
        assertNotNull(result);
        assertEquals("PENDING", result.getStatus());
    }
}
