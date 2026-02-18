package SpringBootTestSuite;

import com.example.dto.ClockEventDTO;
import com.example.entity.Attendance;
import com.example.repository.AttendanceRepository;
import com.example.service.AttendanceService;
import org.junit.jupiter.api.*;
import org.mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AttendanceServiceTest {

    @Mock
    private AttendanceRepository attendanceRepository;

    @InjectMocks
    private AttendanceService attendanceService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void clockIn_ShouldSaveAttendance() {
        ClockEventDTO dto = new ClockEventDTO(1L, LocalDateTime.now());
        Attendance attendance = new Attendance(1L, 1L, LocalDateTime.now(), null);

        when(attendanceRepository.save(any(Attendance.class))).thenReturn(attendance);

        Attendance result = attendanceService.clockIn(dto);

        assertNotNull(result);
        assertEquals(1L, result.getEmployeeId());
    }

    @Test
    void clockIn_ShouldThrowException_OnNullInput() {
        assertThrows(IllegalArgumentException.class, () -> attendanceService.clockIn(null));
    }

    @Test
    void clockOut_ShouldUpdateAttendance() {
        ClockEventDTO dto = new ClockEventDTO(1L, LocalDateTime.now());
        Attendance attendance = new Attendance(1L, 1L, LocalDateTime.now().minusHours(8), null);

        when(attendanceRepository.findByEmployeeIdAndClockOutIsNull(1L)).thenReturn(Optional.of(attendance));
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(attendance);

        Attendance result = attendanceService.clockOut(dto);

        assertNotNull(result.getClockOut());
    }

    @Test
    void clockOut_ShouldThrowException_WhenNoOpenAttendance() {
        ClockEventDTO dto = new ClockEventDTO(2L, LocalDateTime.now());
        when(attendanceRepository.findByEmployeeIdAndClockOutIsNull(2L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> attendanceService.clockOut(dto));
    }
}