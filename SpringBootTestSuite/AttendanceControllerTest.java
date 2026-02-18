package SpringBootTestSuite;

import com.example.controller.AttendanceController;
import com.example.dto.ClockEventDTO;
import com.example.entity.Attendance;
import com.example.service.AttendanceService;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AttendanceControllerTest {

    @Mock
    private AttendanceService attendanceService;

    @InjectMocks
    private AttendanceController attendanceController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void clockIn_ShouldReturnAttendance() {
        ClockEventDTO dto = new ClockEventDTO(1L, LocalDateTime.now());
        Attendance attendance = new Attendance(1L, 1L, LocalDateTime.now(), null);

        when(attendanceService.clockIn(dto)).thenReturn(attendance);

        ResponseEntity<Attendance> response = attendanceController.clockIn(dto);

        assertEquals(201, response.getStatusCodeValue());
        assertEquals(1L, response.getBody().getEmployeeId());
    }

    @Test
    void clockOut_ShouldReturnAttendance() {
        ClockEventDTO dto = new ClockEventDTO(1L, LocalDateTime.now());
        Attendance attendance = new Attendance(1L, 1L, LocalDateTime.now().minusHours(8), LocalDateTime.now());

        when(attendanceService.clockOut(dto)).thenReturn(attendance);

        ResponseEntity<Attendance> response = attendanceController.clockOut(dto);

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody().getClockOut());
    }
}