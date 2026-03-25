package SpringBootTestSuite;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

// Assume these imports exist
import com.example.ems.dto.AttendanceEventDTO;
import com.example.ems.entity.AttendanceEvent;
import com.example.ems.entity.Employee;
import com.example.ems.exception.EntityNotFoundException;
import com.example.ems.repository.AttendanceEventRepository;
import com.example.ems.repository.EmployeeRepository;
import com.example.ems.service.impl.AttendanceServiceImpl;

@ExtendWith(MockitoExtension.class)
public class AttendanceServiceTest {
    @Mock
    private AttendanceEventRepository attendanceEventRepository;
    @Mock
    private EmployeeRepository employeeRepository;
    @InjectMocks
    private AttendanceServiceImpl attendanceService;

    private AttendanceEventDTO clockInDto;
    private AttendanceEventDTO clockOutDto;
    private Employee employee;
    private AttendanceEvent event;

    @BeforeEach
    public void setUp() {
        employee = new Employee();
        employee.setId(1L);
        employee.setName("John Doe");
        employee.setBadgeId("EMP001");
        employee.setStatus("ACTIVE");

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

        event = new AttendanceEvent();
        event.setId(1L);
        event.setEmployee(employee);
        event.setEventType("CLOCK_IN");
        event.setTimestamp(clockInDto.getTimestamp());
        event.setDeviceId("DEV123");
        event.setGeoLocation("12.34,56.78");
    }

    @Test
    public void testClockIn_WithValidData_ReturnsEvent() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(attendanceEventRepository.save(any(AttendanceEvent.class))).thenReturn(event);
        AttendanceEventDTO result = attendanceService.clockIn(clockInDto);
        assertNotNull(result);
        assertEquals("CLOCK_IN", result.getEventType());
        verify(attendanceEventRepository).save(any(AttendanceEvent.class));
    }

    @Test
    public void testClockIn_WithInvalidEmployee_ThrowsEntityNotFoundException() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> attendanceService.clockIn(clockInDto));
    }

    @Test
    public void testClockOut_WithValidData_ReturnsEvent() {
        event.setEventType("CLOCK_OUT");
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(attendanceEventRepository.save(any(AttendanceEvent.class))).thenReturn(event);
        AttendanceEventDTO result = attendanceService.clockOut(clockOutDto);
        assertNotNull(result);
        assertEquals("CLOCK_OUT", result.getEventType());
        verify(attendanceEventRepository).save(any(AttendanceEvent.class));
    }

    @Test
    public void testClockOut_WithInvalidEmployee_ThrowsEntityNotFoundException() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> attendanceService.clockOut(clockOutDto));
    }

    @Test
    public void testGetDailyEvents_WithValidEmployeeAndDate_ReturnsEvents() {
        LocalDate date = LocalDate.now();
        List<AttendanceEvent> events = Arrays.asList(event);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(attendanceEventRepository.findByEmployeeAndTimestampBetween(eq(employee), any(), any())).thenReturn(events);
        List<AttendanceEventDTO> result = attendanceService.getDailyEvents(1L, date);
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    public void testGetDailyEvents_WithInvalidEmployee_ThrowsEntityNotFoundException() {
        LocalDate date = LocalDate.now();
        when(employeeRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> attendanceService.getDailyEvents(1L, date));
    }

    @Test
    public void testClockIn_WithNullDto_ThrowsException() {
        assertThrows(NullPointerException.class, () -> attendanceService.clockIn(null));
    }

    @Test
    public void testClockIn_WithNullEmployeeId_ThrowsException() {
        AttendanceEventDTO dto = new AttendanceEventDTO();
        dto.setEventType("CLOCK_IN");
        dto.setTimestamp(LocalDateTime.now());
        assertThrows(IllegalArgumentException.class, () -> attendanceService.clockIn(dto));
    }

    @Test
    public void testClockIn_WithNullTimestamp_ThrowsException() {
        AttendanceEventDTO dto = new AttendanceEventDTO();
        dto.setEmployeeId(1L);
        dto.setEventType("CLOCK_IN");
        assertThrows(IllegalArgumentException.class, () -> attendanceService.clockIn(dto));
    }

    @Test
    public void testClockIn_WithInvalidEventType_ThrowsException() {
        AttendanceEventDTO dto = new AttendanceEventDTO();
        dto.setEmployeeId(1L);
        dto.setEventType("INVALID");
        dto.setTimestamp(LocalDateTime.now());
        assertThrows(IllegalArgumentException.class, () -> attendanceService.clockIn(dto));
    }
}
