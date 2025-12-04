package SpringBootTestSuite;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MockitoExtension.class)
public class AttendanceServiceTest {

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private AttendanceService attendanceService;

    private AttendanceDTO validClockInDTO;
    private AttendanceEvent validClockInEvent;

    @BeforeEach
    public void setUp() {
        validClockInDTO = new AttendanceDTO();
        validClockInDTO.setEmployeeId(1L);
        validClockInDTO.setDeviceId("DEV001");
        validClockInDTO.setLocation("Main Gate");

        validClockInEvent = new AttendanceEvent();
        validClockInEvent.setId(1L);
        validClockInEvent.setEmployeeId(1L);
        validClockInEvent.setTimestamp(LocalDateTime.now());
        validClockInEvent.setType(EventType.CLOCK_IN);
        validClockInEvent.setDeviceId("DEV001");
        validClockInEvent.setLocation("Main Gate");
        validClockInEvent.setApproved(true);
    }

    @Test
    public void testRecordClockIn_ValidInput_Success() {
        when(employeeRepository.existsById(1L)).thenReturn(true);
        when(attendanceRepository.findRecentByEmployeeId(1L)).thenReturn(Collections.emptyList());
        when(attendanceRepository.save(any(AttendanceEvent.class))).thenReturn(validClockInEvent);

        AttendanceDTO result = attendanceService.recordClockIn(validClockInDTO);

        assertNotNull(result);
        assertEquals(1L, result.getEmployeeId());
        assertEquals(EventType.CLOCK_IN, result.getType());
    }

    @Test
    public void testRecordClockIn_EmployeeNotFound_ThrowsException() {
        when(employeeRepository.existsById(1L)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> attendanceService.recordClockIn(validClockInDTO));
    }

    @Test
    public void testRecordClockIn_DuplicateClockIn_ThrowsException() {
        AttendanceEvent recentClockIn = new AttendanceEvent();
        recentClockIn.setType(EventType.CLOCK_IN);
        recentClockIn.setTimestamp(LocalDateTime.now());

        when(employeeRepository.existsById(1L)).thenReturn(true);
        when(attendanceRepository.findRecentByEmployeeId(1L)).thenReturn(Collections.singletonList(recentClockIn));

        assertThrows(IllegalStateException.class, () -> attendanceService.recordClockIn(validClockInDTO));
    }

    @Test
    public void testRecordClockOut_ValidInput_Success() {
        AttendanceDTO clockOutDTO = new AttendanceDTO();
        clockOutDTO.setEmployeeId(1L);

        AttendanceEvent clockOutEvent = new AttendanceEvent();
        clockOutEvent.setId(2L);
        clockOutEvent.setEmployeeId(1L);
        clockOutEvent.setTimestamp(LocalDateTime.now());
        clockOutEvent.setType(EventType.CLOCK_OUT);

        when(attendanceRepository.save(any(AttendanceEvent.class))).thenReturn(clockOutEvent);

        AttendanceDTO result = attendanceService.recordClockOut(clockOutDTO);

        assertNotNull(result);
        assertEquals(EventType.CLOCK_OUT, result.getType());
    }

    @Test
    public void testGetEmployeeAttendance_ValidDateRange_ReturnsEvents() {
        List<AttendanceEvent> events = Arrays.asList(validClockInEvent);
        when(attendanceRepository.findByEmployeeIdAndDateRange(eq(1L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(events);

        List<AttendanceDTO> result = attendanceService.getEmployeeAttendance(1L, LocalDate.now().minusDays(1), LocalDate.now());

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    public void testGetEmployeeAttendance_NoEvents_ReturnsEmptyList() {
        when(attendanceRepository.findByEmployeeIdAndDateRange(eq(1L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());

        List<AttendanceDTO> result = attendanceService.getEmployeeAttendance(1L, LocalDate.now().minusDays(1), LocalDate.now());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}