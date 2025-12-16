import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.time.LocalDateTime;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AttendanceServiceTest {
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
    void testClockIn_ValidEmployee() {
        Employee employee = new Employee();
        employee.setId(1L);
        Attendance attendance = new Attendance();
        attendance.setEmployee(employee);
        attendance.setClockInTime(LocalDateTime.now());
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(attendance);
        Attendance result = attendanceService.clockIn(1L, "DEVICE001");
        assertNotNull(result);
        assertNotNull(result.getClockInTime());
        verify(attendanceRepository, times(1)).save(any(Attendance.class));
    }

    @Test
    void testClockIn_EmployeeNotFound() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> attendanceService.clockIn(1L, "DEVICE001"));
    }

    @Test
    void testClockOut_ValidAttendance() {
        Attendance attendance = new Attendance();
        attendance.setId(1L);
        attendance.setClockInTime(LocalDateTime.now().minusHours(8));
        when(attendanceRepository.findById(1L)).thenReturn(Optional.of(attendance));
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(attendance);
        Attendance result = attendanceService.clockOut(1L);
        assertNotNull(result);
        assertNotNull(result.getClockOutTime());
        assertTrue(result.getHoursWorked() > 0);
    }

    @Test
    void testClockOut_AttendanceNotFound() {
        when(attendanceRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> attendanceService.clockOut(1L));
    }

    @Test
    void testClockOut_AlreadyClockedOut() {
        Attendance attendance = new Attendance();
        attendance.setId(1L);
        attendance.setClockInTime(LocalDateTime.now().minusHours(8));
        attendance.setClockOutTime(LocalDateTime.now());
        when(attendanceRepository.findById(1L)).thenReturn(Optional.of(attendance));
        assertThrows(ValidationException.class, () -> attendanceService.clockOut(1L));
    }

    @Test
    void testCalculateHours_Valid() {
        LocalDateTime clockIn = LocalDateTime.of(2024, 1, 1, 9, 0);
        LocalDateTime clockOut = LocalDateTime.of(2024, 1, 1, 17, 0);
        double hours = attendanceService.calculateHours(clockIn, clockOut);
        assertEquals(8.0, hours, 0.01);
    }

    @Test
    void testCalculateHours_NullClockIn() {
        assertThrows(ValidationException.class, () -> attendanceService.calculateHours(null, LocalDateTime.now()));
    }

    @Test
    void testCalculateHours_NullClockOut() {
        assertThrows(ValidationException.class, () -> attendanceService.calculateHours(LocalDateTime.now(), null));
    }
}