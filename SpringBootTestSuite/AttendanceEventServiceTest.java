package com.warehouse.attendance;

import com.warehouse.attendance.exception.*;
import com.warehouse.attendance.model.*;
import com.warehouse.attendance.repository.*;
import com.warehouse.attendance.service.*;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AttendanceEventService.
 */
@SpringBootTest
class AttendanceEventServiceTest {

    @Mock
    private AttendanceEventRepository attendanceEventRepository;
    @Mock
    private EmployeeRepository employeeRepository;
    @InjectMocks
    private AttendanceEventService attendanceEventService;

    private Employee employee;
    private AttendanceEvent clockInEvent;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        employee = new Employee(1L, "John Doe");
        clockInEvent = new AttendanceEvent(1L, employee, LocalDateTime.now(), null, "DEVICE1", "IN", true);
    }

    @AfterEach
    void tearDown() {
        // Cleanup if needed
    }

    @Test
    void clockIn_withValidEmployee_shouldSucceed() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(attendanceEventRepository.findActiveClockInByEmployeeId(1L)).thenReturn(Optional.empty());
        when(attendanceEventRepository.save(any())).thenReturn(clockInEvent);

        AttendanceEvent result = attendanceEventService.clockIn(1L, "DEVICE1", 40.7128, -74.0060);

        assertThat(result).isNotNull();
        assertThat(result.getEmployee()).isEqualTo(employee);
        assertThat(result.getType()).isEqualTo("IN");
        verify(attendanceEventRepository).save(any());
    }

    @Test
    void clockIn_whenAlreadyClockedIn_shouldThrowException() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(attendanceEventRepository.findActiveClockInByEmployeeId(1L)).thenReturn(Optional.of(clockInEvent));

        assertThatThrownBy(() -> attendanceEventService.clockIn(1L, "DEVICE1", 40.7128, -74.0060))
                .isInstanceOf(AlreadyClockedInException.class);
    }

    @Test
    void clockOut_withValidClockIn_shouldSucceed() {
        when(attendanceEventRepository.findActiveClockInByEmployeeId(1L)).thenReturn(Optional.of(clockInEvent));
        when(attendanceEventRepository.save(any())).thenReturn(clockInEvent);

        AttendanceEvent result = attendanceEventService.clockOut(1L, "DEVICE1", 40.7128, -74.0060);

        assertThat(result.getType()).isEqualTo("OUT");
        verify(attendanceEventRepository).save(any());
    }

    @Test
    void clockOut_withoutClockIn_shouldThrowException() {
        when(attendanceEventRepository.findActiveClockInByEmployeeId(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> attendanceEventService.clockOut(1L, "DEVICE1", 40.7128, -74.0060))
                .isInstanceOf(NoActiveClockInException.class);
    }

    @Test
    void calculateDailyTotals_shouldReturnCorrectTotals() {
        List<AttendanceEvent> events = Arrays.asList(
                new AttendanceEvent(1L, employee, LocalDateTime.now().minusHours(8), LocalDateTime.now().minusHours(4), "DEVICE1", "IN", true),
                new AttendanceEvent(2L, employee, LocalDateTime.now().minusHours(3), LocalDateTime.now(), "DEVICE1", "IN", true)
        );
        when(attendanceEventRepository.findByEmployeeIdAndDate(anyLong(), any())).thenReturn(events);

        double total = attendanceEventService.calculateDailyTotals(1L, LocalDateTime.now().toLocalDate());

        assertThat(total).isGreaterThan(0);
    }

    @Test
    void clockIn_withNullEmployeeId_shouldThrowException() {
        assertThatThrownBy(() -> attendanceEventService.clockIn(null, "DEVICE1", 40.7128, -74.0060))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void clockIn_withInvalidDeviceId_shouldThrowException() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        assertThatThrownBy(() -> attendanceEventService.clockIn(1L, "", 40.7128, -74.0060))
                .isInstanceOf(InvalidDeviceException.class);
    }

    @Test
    void clockIn_withGeofenceViolation_shouldThrowException() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(attendanceEventRepository.findActiveClockInByEmployeeId(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> attendanceEventService.clockIn(1L, "DEVICE1", 0.0, 0.0))
                .isInstanceOf(GeofenceViolationException.class);
    }

    @Test
    void missedPunchCorrectionWorkflow_shouldSucceed() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(attendanceEventRepository.save(any())).thenReturn(clockInEvent);

        AttendanceEvent correction = attendanceEventService.correctMissedPunch(1L, LocalDateTime.now().minusHours(2), LocalDateTime.now(), "DEVICE1");

        assertThat(correction).isNotNull();
        verify(attendanceEventRepository).save(any());
    }
}