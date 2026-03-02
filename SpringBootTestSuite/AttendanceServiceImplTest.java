import org.junit.jupiter.api.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AttendanceServiceImplTest {
    @Mock
    private AttendanceRepository attendanceRepository;
    @Mock
    private EmployeeRepository employeeRepository;
    @InjectMocks
    private AttendanceServiceImpl attendanceService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("testClockIn_ValidInput_ReturnsAttendance")
    void testClockIn_ValidInput_ReturnsAttendance() {
        Employee employee = new Employee("John", "Doe", "12345", "john.doe@email.com");
        Attendance attendance = new Attendance(employee, LocalDateTime.now(), null);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(attendance);
        Attendance result = attendanceService.clockIn(1L);
        assertEquals(attendance, result);
    }

    @Test
    @DisplayName("testClockIn_InvalidEmployeeId_ThrowsEmployeeNotFoundException")
    void testClockIn_InvalidEmployeeId_ThrowsEmployeeNotFoundException() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(EmployeeNotFoundException.class, () -> attendanceService.clockIn(99L));
    }

    @Test
    @DisplayName("testClockOut_ValidInput_ReturnsAttendance")
    void testClockOut_ValidInput_ReturnsAttendance() {
        Employee employee = new Employee("John", "Doe", "12345", "john.doe@email.com");
        Attendance attendance = new Attendance(employee, LocalDateTime.now().minusHours(8), LocalDateTime.now());
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(attendanceRepository.findOpenAttendanceByEmployeeId(1L)).thenReturn(Optional.of(attendance));
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(attendance);
        Attendance result = attendanceService.clockOut(1L);
        assertEquals(attendance, result);
    }

    @Test
    @DisplayName("testClockOut_NoOpenAttendance_ThrowsAttendanceNotFoundException")
    void testClockOut_NoOpenAttendance_ThrowsAttendanceNotFoundException() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(new Employee()));
        when(attendanceRepository.findOpenAttendanceByEmployeeId(1L)).thenReturn(Optional.empty());
        assertThrows(AttendanceNotFoundException.class, () -> attendanceService.clockOut(1L));
    }

    @Test
    @DisplayName("testGetAttendanceByEmployee_ValidId_ReturnsAttendanceList")
    void testGetAttendanceByEmployee_ValidId_ReturnsAttendanceList() {
        Employee employee = new Employee("John", "Doe", "12345", "john.doe@email.com");
        List<Attendance> attendances = Arrays.asList(new Attendance(employee, LocalDateTime.now(), LocalDateTime.now().plusHours(8)));
        when(attendanceRepository.findByEmployeeId(1L)).thenReturn(attendances);
        List<Attendance> result = attendanceService.getAttendanceByEmployee(1L);
        assertEquals(attendances, result);
    }

    @Test
    @DisplayName("testGetAttendanceByEmployee_InvalidId_ReturnsEmptyList")
    void testGetAttendanceByEmployee_InvalidId_ReturnsEmptyList() {
        when(attendanceRepository.findByEmployeeId(99L)).thenReturn(Collections.emptyList());
        List<Attendance> result = attendanceService.getAttendanceByEmployee(99L);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("testCorrectAttendance_ValidInput_UpdatesAttendance")
    void testCorrectAttendance_ValidInput_UpdatesAttendance() {
        Attendance attendance = new Attendance();
        when(attendanceRepository.findById(1L)).thenReturn(Optional.of(attendance));
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(attendance);
        Attendance corrected = attendanceService.correctAttendance(1L, LocalDateTime.now(), LocalDateTime.now().plusHours(8));
        assertEquals(attendance, corrected);
    }

    @Test
    @DisplayName("testCorrectAttendance_InvalidId_ThrowsAttendanceNotFoundException")
    void testCorrectAttendance_InvalidId_ThrowsAttendanceNotFoundException() {
        when(attendanceRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(AttendanceNotFoundException.class, () -> attendanceService.correctAttendance(99L, LocalDateTime.now(), LocalDateTime.now().plusHours(8)));
    }

    @Test
    @DisplayName("testCalculateHoursWorked_ValidInput_ReturnsHours")
    void testCalculateHoursWorked_ValidInput_ReturnsHours() {
        Attendance attendance = new Attendance();
        attendance.setClockIn(LocalDateTime.now().minusHours(8));
        attendance.setClockOut(LocalDateTime.now());
        when(attendanceRepository.findById(1L)).thenReturn(Optional.of(attendance));
        double hours = attendanceService.calculateHoursWorked(1L);
        assertEquals(8.0, hours);
    }

    @Test
    @DisplayName("testCalculateHoursWorked_InvalidId_ThrowsAttendanceNotFoundException")
    void testCalculateHoursWorked_InvalidId_ThrowsAttendanceNotFoundException() {
        when(attendanceRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(AttendanceNotFoundException.class, () -> attendanceService.calculateHoursWorked(99L));
    }

    @Test
    @DisplayName("testGenerateAttendanceReport_ValidInput_ReturnsReport")
    void testGenerateAttendanceReport_ValidInput_ReturnsReport() {
        List<Attendance> attendances = Arrays.asList(new Attendance());
        when(attendanceRepository.findAll()).thenReturn(attendances);
        AttendanceReport report = attendanceService.generateAttendanceReport();
        assertNotNull(report);
    }

    @Test
    @DisplayName("testClockIn_NullInput_ThrowsIllegalArgumentException")
    void testClockIn_NullInput_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> attendanceService.clockIn(null));
    }

    @Test
    @DisplayName("testClockOut_NullInput_ThrowsIllegalArgumentException")
    void testClockOut_NullInput_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> attendanceService.clockOut(null));
    }

    @AfterEach
    void tearDown() {
        // Cleanup if needed
    }
}
