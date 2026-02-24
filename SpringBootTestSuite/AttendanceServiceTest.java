package SpringBootTestSuite;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.*;
import org.mockito.*;

import java.time.LocalDateTime;
import java.util.*;

class AttendanceServiceTest {

    @Mock
    private AttendanceRepository attendanceRepository;

    @InjectMocks
    private AttendanceService attendanceService;

    private AttendanceEvent validClockIn;
    private AttendanceEvent validClockOut;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        validClockIn = new AttendanceEvent(1L, 1L, LocalDateTime.now(), "IN", "DEVICE001", "Warehouse A", "NORMAL");
        validClockOut = new AttendanceEvent(2L, 1L, LocalDateTime.now().plusHours(8), "OUT", "DEVICE001", "Warehouse A", "NORMAL");
    }

    @AfterEach
    void tearDown() {
        // Clean up resources if needed
    }

    @Test
    void testClockIn_ValidInput() {
        when(attendanceRepository.save(any(AttendanceEvent.class))).thenReturn(validClockIn);
        AttendanceEvent result = attendanceService.clockIn(1L, "DEVICE001", "Warehouse A");
        assertNotNull(result);
        assertEquals("IN", result.getType());
        assertEquals(1L, result.getEmployeeId());
    }

    @Test
    void testClockIn_NullEmployeeId() {
        assertThrows(IllegalArgumentException.class, () -> attendanceService.clockIn(null, "DEVICE001", "Warehouse A"));
    }

    @Test
    void testClockOut_ValidInput() {
        when(attendanceRepository.save(any(AttendanceEvent.class))).thenReturn(validClockOut);
        AttendanceEvent result = attendanceService.clockOut(1L, "DEVICE001", "Warehouse A");
        assertNotNull(result);
        assertEquals("OUT", result.getType());
        assertEquals(1L, result.getEmployeeId());
    }

    @Test
    void testClockOut_NullEmployeeId() {
        assertThrows(IllegalArgumentException.class, () -> attendanceService.clockOut(null, "DEVICE001", "Warehouse A"));
    }

    @Test
    void testCalculateHoursWorked_ValidShift() {
        when(attendanceRepository.findByEmployeeIdAndDateRange(anyLong(), any(), any()))
            .thenReturn(Arrays.asList(validClockIn, validClockOut));
        double hours = attendanceService.calculateHoursWorked(1L, LocalDateTime.now().minusDays(1), LocalDateTime.now());
        assertTrue(hours > 0);
    }

    @Test
    void testCalculateHoursWorked_NoEvents() {
        when(attendanceRepository.findByEmployeeIdAndDateRange(anyLong(), any(), any()))
            .thenReturn(Collections.emptyList());
        double hours = attendanceService.calculateHoursWorked(1L, LocalDateTime.now().minusDays(1), LocalDateTime.now());
        assertEquals(0.0, hours);
    }

    @Test
    void testRequestCorrection_ValidInput() {
        AttendanceCorrection correction = new AttendanceCorrection(1L, 1L, 1L, "PENDING", null, "Forgot to clock out");
        when(attendanceRepository.saveCorrection(any(AttendanceCorrection.class))).thenReturn(correction);
        AttendanceCorrection result = attendanceService.requestCorrection(1L, 1L, "Forgot to clock out");
        assertNotNull(result);
        assertEquals("PENDING", result.getStatus());
    }

    @Test
    void testRequestCorrection_NullReason() {
        assertThrows(IllegalArgumentException.class, () -> attendanceService.requestCorrection(1L, 1L, null));
    }

    @Test
    void testExportToCSV_ValidData() {
        when(attendanceRepository.findAll()).thenReturn(Arrays.asList(validClockIn, validClockOut));
        String csv = attendanceService.exportToCSV();
        assertNotNull(csv);
        assertTrue(csv.contains("IN"));
        assertTrue(csv.contains("OUT"));
    }

    @Test
    void testExportToCSV_EmptyData() {
        when(attendanceRepository.findAll()).thenReturn(Collections.emptyList());
        String csv = attendanceService.exportToCSV();
        assertNotNull(csv);
        assertTrue(csv.isEmpty() || csv.equals(""));
    }

    @Test
    void testClockIn_InvalidLocation() {
        assertThrows(IllegalArgumentException.class, () -> attendanceService.clockIn(1L, "DEVICE001", ""));
    }

    @Test
    void testClockIn_DuplicateEvent() {
        when(attendanceRepository.findLastEventByEmployeeId(1L)).thenReturn(Optional.of(validClockIn));
        assertThrows(IllegalStateException.class, () -> attendanceService.clockIn(1L, "DEVICE001", "Warehouse A"));
    }

    @Test
    void testClockOut_NoClockIn() {
        when(attendanceRepository.findLastEventByEmployeeId(1L)).thenReturn(Optional.empty());
        assertThrows(IllegalStateException.class, () -> attendanceService.clockOut(1L, "DEVICE001", "Warehouse A"));
    }
}