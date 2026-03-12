package SpringBootTestSuite;

import com.example.warehouse.attendance.AttendanceEvent;
import com.example.warehouse.attendance.AttendanceService;
import com.example.warehouse.attendance.AttendanceRepository;
import com.example.warehouse.exception.ResourceNotFoundException;
import com.example.warehouse.exception.ValidationException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
public class AttendanceServiceTest {
    @Mock
    private AttendanceRepository attendanceRepository;

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
    public void clockIn_ValidInput_ReturnsAttendanceEvent() {
        AttendanceEvent event = new AttendanceEvent();
        event.setEmployeeId(1L);
        event.setClockInTime(LocalDateTime.now());
        when(attendanceRepository.save(any())).thenReturn(event);
        AttendanceEvent result = attendanceService.clockIn(1L, LocalDateTime.now());
        assertNotNull(result);
        assertEquals(1L, result.getEmployeeId());
    }

    @Test
    public void clockIn_NullEmployeeId_ThrowsValidationException() {
        assertThrows(ValidationException.class, () -> attendanceService.clockIn(null, LocalDateTime.now()));
    }

    @Test
    public void clockOut_ValidInput_ReturnsAttendanceEvent() {
        AttendanceEvent event = new AttendanceEvent();
        event.setEmployeeId(1L);
        event.setClockOutTime(LocalDateTime.now());
        when(attendanceRepository.findByEmployeeIdAndClockOutTimeIsNull(1L)).thenReturn(Optional.of(event));
        when(attendanceRepository.save(any())).thenReturn(event);
        AttendanceEvent result = attendanceService.clockOut(1L, LocalDateTime.now());
        assertNotNull(result);
        assertEquals(1L, result.getEmployeeId());
        assertNotNull(result.getClockOutTime());
    }

    @Test
    public void clockOut_NoOpenClockIn_ThrowsResourceNotFoundException() {
        when(attendanceRepository.findByEmployeeIdAndClockOutTimeIsNull(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> attendanceService.clockOut(1L, LocalDateTime.now()));
    }

    @Test
    public void getAttendanceEventsByEmployeeId_ValidId_ReturnsList() {
        AttendanceEvent event = new AttendanceEvent();
        event.setEmployeeId(1L);
        when(attendanceRepository.findByEmployeeId(1L)).thenReturn(Collections.singletonList(event));
        List<AttendanceEvent> result = attendanceService.getAttendanceEventsByEmployeeId(1L);
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    public void getAttendanceEventsByEmployeeId_NoEvents_ReturnsEmptyList() {
        when(attendanceRepository.findByEmployeeId(1L)).thenReturn(Collections.emptyList());
        List<AttendanceEvent> result = attendanceService.getAttendanceEventsByEmployeeId(1L);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void clockIn_InvalidTime_ThrowsValidationException() {
        assertThrows(ValidationException.class, () -> attendanceService.clockIn(1L, null));
    }

    @Test
    public void clockOut_InvalidTime_ThrowsValidationException() {
        assertThrows(ValidationException.class, () -> attendanceService.clockOut(1L, null));
    }
}
