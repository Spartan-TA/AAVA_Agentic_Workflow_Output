package SpringBootTestSuite;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AttendanceServiceTest {

    @Mock
    private AttendanceRepository attendanceRepository;
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private GeofenceService geofenceService;
    @Mock
    private ShiftService shiftService;

    @InjectMocks
    private AttendanceService attendanceService;

    private ClockInRequest clockInRequest;
    private Employee employee;
    private AttendanceEvent lastEvent;
    private AttendanceEvent clockInEvent;
    private AttendanceEvent clockOutEvent;
    private AttendanceResponse attendanceResponse;

    @BeforeEach
    void setUp() {
        clockInRequest = new ClockInRequest();
        clockInRequest.setEmployeeId(1L);
        clockInRequest.setLocation("loc1");
        clockInRequest.setDeviceId("dev1");

        employee = new Employee();
        employee.setId(1L);
        employee.setBadgeId("BADGE123");

        lastEvent = AttendanceEvent.builder()
                .employee(employee)
                .eventType(AttendanceEvent.AttendanceEventType.CLOCK_OUT)
                .timestamp(LocalDateTime.now().minusHours(1))
                .build();

        clockInEvent = AttendanceEvent.builder()
                .employee(employee)
                .eventType(AttendanceEvent.AttendanceEventType.CLOCK_IN)
                .timestamp(LocalDateTime.now().withHour(8).withMinute(0))
                .build();

        clockOutEvent = AttendanceEvent.builder()
                .employee(employee)
                .eventType(AttendanceEvent.AttendanceEventType.CLOCK_OUT)
                .timestamp(LocalDateTime.now().withHour(16).withMinute(0))
                .build();

        attendanceResponse = new AttendanceResponse();
        attendanceResponse.setEmployeeId(1L);
    }

    @Test
    void testClockIn_Normal_Success() {
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.of(employee));
        when(attendanceRepository.findTopByEmployeeOrderByTimestampDesc(any(Employee.class)))
                .thenReturn(Optional.of(lastEvent));
        when(shiftService.determineShiftForTimestamp(any(Employee.class), any(LocalDateTime.class))).thenReturn(10L);
        when(attendanceRepository.save(any(AttendanceEvent.class))).thenAnswer(invocation -> invocation.getArgument(0));
        Mockito.doReturn(true).when(geofenceService).validateLocation(anyString());

        AttendanceResponse response = attendanceService.clockIn(clockInRequest);
        assertNotNull(response);
        assertEquals(1L, response.getEmployeeId());
    }

    @Test
    void testClockIn_EmployeeNotFound_ThrowsException() {
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThrows(AttendanceException.class, () -> attendanceService.clockIn(clockInRequest));
    }

    @Test
    void testClockIn_AlreadyClockedIn_ThrowsException() {
        AttendanceEvent alreadyClockedIn = AttendanceEvent.builder()
                .employee(employee)
                .eventType(AttendanceEvent.AttendanceEventType.CLOCK_IN)
                .timestamp(LocalDateTime.now())
                .build();
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.of(employee));
        when(attendanceRepository.findTopByEmployeeOrderByTimestampDesc(any(Employee.class)))
                .thenReturn(Optional.of(alreadyClockedIn));
        assertThrows(AttendanceException.class, () -> attendanceService.clockIn(clockInRequest));
    }

    @Test
    void testClockIn_GeofenceEnabled_ValidLocation_Success() {
        attendanceService.geofenceEnabled = true;
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.of(employee));
        when(attendanceRepository.findTopByEmployeeOrderByTimestampDesc(any(Employee.class)))
                .thenReturn(Optional.of(lastEvent));
        when(geofenceService.validateLocation(anyString())).thenReturn(true);
        when(shiftService.determineShiftForTimestamp(any(Employee.class), any(LocalDateTime.class))).thenReturn(10L);
        when(attendanceRepository.save(any(AttendanceEvent.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AttendanceResponse response = attendanceService.clockIn(clockInRequest);
        assertNotNull(response);
    }

    @Test
    void testClockIn_GeofenceEnabled_InvalidLocation_Success() {
        attendanceService.geofenceEnabled = true;
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.of(employee));
        when(attendanceRepository.findTopByEmployeeOrderByTimestampDesc(any(Employee.class)))
                .thenReturn(Optional.of(lastEvent));
        when(geofenceService.validateLocation(anyString())).thenReturn(false);
        when(shiftService.determineShiftForTimestamp(any(Employee.class), any(LocalDateTime.class))).thenReturn(10L);
        when(attendanceRepository.save(any(AttendanceEvent.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AttendanceResponse response = attendanceService.clockIn(clockInRequest);
        assertNotNull(response);
    }

    @Test
    void testCalculateDailyHours_Normal_Success() {
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.of(employee));
        List<AttendanceEvent> events = Arrays.asList(
                AttendanceEvent.builder().eventType(AttendanceEvent.AttendanceEventType.CLOCK_IN).timestamp(LocalDateTime.of(2023,1,1,8,0)).build(),
                AttendanceEvent.builder().eventType(AttendanceEvent.AttendanceEventType.CLOCK_OUT).timestamp(LocalDateTime.of(2023,1,1,16,0)).build()
        );
        when(attendanceRepository.findByEmployeeAndTimestampBetweenOrderByTimestamp(any(Employee.class), any(), any())).thenReturn(events);
        double hours = attendanceService.calculateDailyHours(1L, LocalDate.of(2023,1,1));
        assertEquals(8.0, hours, 0.01);
    }

    @Test
    void testCalculateDailyHours_NoEvents_ReturnsZero() {
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.of(employee));
        when(attendanceRepository.findByEmployeeAndTimestampBetweenOrderByTimestamp(any(Employee.class), any(), any())).thenReturn(Collections.emptyList());
        double hours = attendanceService.calculateDailyHours(1L, LocalDate.of(2023,1,1));
        assertEquals(0.0, hours, 0.01);
    }

    @Test
    void testCalculateDailyHours_UnmatchedClockIn_ReturnsZero() {
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.of(employee));
        List<AttendanceEvent> events = Collections.singletonList(
                AttendanceEvent.builder().eventType(AttendanceEvent.AttendanceEventType.CLOCK_IN).timestamp(LocalDateTime.of(2023,1,1,8,0)).build()
        );
        when(attendanceRepository.findByEmployeeAndTimestampBetweenOrderByTimestamp(any(Employee.class), any(), any())).thenReturn(events);
        double hours = attendanceService.calculateDailyHours(1L, LocalDate.of(2023,1,1));
        assertEquals(0.0, hours, 0.01);
    }

    @Test
    void testCalculateDailyHours_EmployeeNotFound_ThrowsException() {
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThrows(AttendanceException.class, () -> attendanceService.calculateDailyHours(1L, LocalDate.now()));
    }

    @Test
    void testCalculateDailyHours_MultiplePairs_CorrectSum() {
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.of(employee));
        List<AttendanceEvent> events = Arrays.asList(
                AttendanceEvent.builder().eventType(AttendanceEvent.AttendanceEventType.CLOCK_IN).timestamp(LocalDateTime.of(2023,1,1,8,0)).build(),
                AttendanceEvent.builder().eventType(AttendanceEvent.AttendanceEventType.CLOCK_OUT).timestamp(LocalDateTime.of(2023,1,1,12,0)).build(),
                AttendanceEvent.builder().eventType(AttendanceEvent.AttendanceEventType.CLOCK_IN).timestamp(LocalDateTime.of(2023,1,1,13,0)).build(),
                AttendanceEvent.builder().eventType(AttendanceEvent.AttendanceEventType.CLOCK_OUT).timestamp(LocalDateTime.of(2023,1,1,17,0)).build()
        );
        when(attendanceRepository.findByEmployeeAndTimestampBetweenOrderByTimestamp(any(Employee.class), any(), any())).thenReturn(events);
        double hours = attendanceService.calculateDailyHours(1L, LocalDate.of(2023,1,1));
        assertEquals(8.0, hours, 0.01);
    }

    @Test
    void testClockIn_NullLocation_GeofenceDisabled_Success() {
        attendanceService.geofenceEnabled = false;
        clockInRequest.setLocation(null);
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.of(employee));
        when(attendanceRepository.findTopByEmployeeOrderByTimestampDesc(any(Employee.class)))
                .thenReturn(Optional.of(lastEvent));
        when(shiftService.determineShiftForTimestamp(any(Employee.class), any(LocalDateTime.class))).thenReturn(10L);
        when(attendanceRepository.save(any(AttendanceEvent.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AttendanceResponse response = attendanceService.clockIn(clockInRequest);
        assertNotNull(response);
    }
}
