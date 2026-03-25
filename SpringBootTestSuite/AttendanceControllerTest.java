package SpringBootTestSuite;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

// Assume these imports exist
import com.example.ems.controller.AttendanceController;
import com.example.ems.dto.AttendanceEventDTO;
import com.example.ems.exception.EntityNotFoundException;
import com.example.ems.service.AttendanceService;

@ExtendWith(MockitoExtension.class)
public class AttendanceControllerTest {
    @Mock
    private AttendanceService attendanceService;
    @InjectMocks
    private AttendanceController attendanceController;

    private AttendanceEventDTO clockInDto;
    private AttendanceEventDTO clockOutDto;

    @BeforeEach
    public void setUp() {
        clockInDto = new AttendanceEventDTO();
        clockInDto.setEmployeeId(1L);
        clockInDto.setEventType("CLOCK_IN");
        clockInDto.setTimestamp(LocalDateTime.now());
        clockInDto.setDeviceId("DEV123");
        clockInDto.setGeoLocation("12.34,56.78");

        clockOutDto = new AttendanceEventDTO();
        clockOutDto.setEmployeeId(1L);
        clockOutDto.setEventType("CLOCK_OUT");
        clockOutDto.setTimestamp(LocalDateTime.now());
        clockOutDto.setDeviceId("DEV123");
        clockOutDto.setGeoLocation("12.34,56.78");
    }

    @Test
    public void testClockIn_Valid_ReturnsCreated() {
        when(attendanceService.clockIn(any(AttendanceEventDTO.class))).thenReturn(clockInDto);
        ResponseEntity<AttendanceEventDTO> response = attendanceController.clockIn(clockInDto);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("CLOCK_IN", response.getBody().getEventType());
        verify(attendanceService).clockIn(any(AttendanceEventDTO.class));
    }

    @Test
    public void testClockIn_Invalid_ThrowsValidation() {
        AttendanceEventDTO invalidDto = new AttendanceEventDTO();
        when(attendanceService.clockIn(any(AttendanceEventDTO.class))).thenThrow(new IllegalArgumentException("Invalid input"));
        assertThrows(IllegalArgumentException.class, () -> attendanceController.clockIn(invalidDto));
    }

    @Test
    public void testClockOut_Valid_ReturnsCreated() {
        when(attendanceService.clockOut(any(AttendanceEventDTO.class))).thenReturn(clockOutDto);
        ResponseEntity<AttendanceEventDTO> response = attendanceController.clockOut(clockOutDto);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("CLOCK_OUT", response.getBody().getEventType());
        verify(attendanceService).clockOut(any(AttendanceEventDTO.class));
    }

    @Test
    public void testClockOut_Invalid_ThrowsValidation() {
        AttendanceEventDTO invalidDto = new AttendanceEventDTO();
        when(attendanceService.clockOut(any(AttendanceEventDTO.class))).thenThrow(new IllegalArgumentException("Invalid input"));
        assertThrows(IllegalArgumentException.class, () -> attendanceController.clockOut(invalidDto));
    }

    @Test
    public void testGetDailyEvents_Valid_ReturnsOk() {
        List<AttendanceEventDTO> events = Arrays.asList(clockInDto, clockOutDto);
        when(attendanceService.getDailyEvents(1L, LocalDate.now())).thenReturn(events);
        ResponseEntity<List<AttendanceEventDTO>> response = attendanceController.getDailyEvents(1L, LocalDate.now());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
        verify(attendanceService).getDailyEvents(1L, LocalDate.now());
    }

    @Test
    public void testGetDailyEvents_EmployeeNotFound_Throws404() {
        when(attendanceService.getDailyEvents(2L, LocalDate.now())).thenThrow(new EntityNotFoundException("Not found"));
        assertThrows(EntityNotFoundException.class, () -> attendanceController.getDailyEvents(2L, LocalDate.now()));
    }
}
