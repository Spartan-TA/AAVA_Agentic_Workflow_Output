package com.warehouse.ems.attendance;

import com.warehouse.ems.audit.AuditLogService;
import com.warehouse.ems.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AttendanceServiceTest {
    @Mock
    private AttendanceEventRepository attendanceEventRepository;
    @Mock
    private AuditLogService auditLogService;
    @InjectMocks
    private AttendanceService attendanceService;

    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @Test
    void testClockIn_ValidData_ReturnsAttendanceEvent() {
        AttendanceEvent event = AttendanceEvent.builder()
            .employeeId(1L)
            .location("Warehouse A")
            .device("Device123")
            .clockIn(LocalDateTime.now())
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .deleted(false)
            .build();
        when(attendanceEventRepository.save(any(AttendanceEvent.class))).thenReturn(event);

        AttendanceEvent result = attendanceService.clockIn(1L, "Warehouse A", "Device123");

        assertThat(result.getEmployeeId()).isEqualTo(1L);
        assertThat(result.getLocation()).isEqualTo("Warehouse A");
        assertThat(result.getDevice()).isEqualTo("Device123");
        assertThat(result.getClockIn()).isNotNull();
        assertThat(result.getCreatedAt()).isNotNull();
        assertThat(result.getUpdatedAt()).isNotNull();
        verify(attendanceEventRepository).save(any(AttendanceEvent.class));
        verify(auditLogService).logCreate(eq("AttendanceEvent"), isNull(), any(AttendanceEvent.class));
    }

    @Test
    void testClockIn_NullEmployeeId_HandlesGracefully() {
        AttendanceEvent event = AttendanceEvent.builder()
            .employeeId(null)
            .location("Warehouse A")
            .device("Device123")
            .clockIn(LocalDateTime.now())
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .deleted(false)
            .build();
        when(attendanceEventRepository.save(any(AttendanceEvent.class))).thenReturn(event);

        AttendanceEvent result = attendanceService.clockIn(null, "Warehouse A", "Device123");

        assertThat(result.getEmployeeId()).isNull();
        verify(attendanceEventRepository).save(any(AttendanceEvent.class));
        verify(auditLogService).logCreate(eq("AttendanceEvent"), isNull(), any(AttendanceEvent.class));
    }

    @Test
    void testClockIn_NullLocation_HandlesGracefully() {
        AttendanceEvent event = AttendanceEvent.builder()
            .employeeId(1L)
            .location(null)
            .device("Device123")
            .clockIn(LocalDateTime.now())
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .deleted(false)
            .build();
        when(attendanceEventRepository.save(any(AttendanceEvent.class))).thenReturn(event);

        AttendanceEvent result = attendanceService.clockIn(1L, null, "Device123");

        assertThat(result.getLocation()).isNull();
        verify(attendanceEventRepository).save(any(AttendanceEvent.class));
        verify(auditLogService).logCreate(eq("AttendanceEvent"), isNull(), any(AttendanceEvent.class));
    }

    @Test
    void testClockOut_ValidEventId_UpdatesClockOut() {
        AttendanceEvent event = AttendanceEvent.builder()
            .id(1L)
            .employeeId(1L)
            .clockIn(LocalDateTime.now().minusHours(2))
            .clockOut(null)
            .createdAt(LocalDateTime.now().minusHours(2))
            .updatedAt(LocalDateTime.now().minusHours(2))
            .deleted(false)
            .build();
        AttendanceEvent saved = AttendanceEvent.builder()
            .id(1L)
            .employeeId(1L)
            .clockIn(event.getClockIn())
            .clockOut(LocalDateTime.now())
            .createdAt(event.getCreatedAt())
            .updatedAt(LocalDateTime.now())
            .deleted(false)
            .build();
        when(attendanceEventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(attendanceEventRepository.save(any(AttendanceEvent.class))).thenReturn(saved);

        AttendanceEvent result = attendanceService.clockOut(1L);

        assertThat(result.getClockOut()).isNotNull();
        assertThat(result.getUpdatedAt()).isNotNull();
        verify(attendanceEventRepository).findById(1L);
        verify(attendanceEventRepository).save(any(AttendanceEvent.class));
        verify(auditLogService).logUpdate(eq("AttendanceEvent"), eq(event), any(AttendanceEvent.class));
    }

    @Test
    void testClockOut_InvalidEventId_ThrowsResourceNotFoundException() {
        when(attendanceEventRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> attendanceService.clockOut(99L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Attendance event not found with id: 99");
        verify(attendanceEventRepository).findById(99L);
    }

    @Test
    void testGetAttendanceByEmployee_ValidEmployeeId_ReturnsEvents() {
        List<AttendanceEvent> events = Arrays.asList(
            AttendanceEvent.builder().employeeId(1L).deleted(false).build(),
            AttendanceEvent.builder().employeeId(1L).deleted(false).build()
        );
        when(attendanceEventRepository.findByEmployeeIdAndDeletedFalse(1L)).thenReturn(events);

        List<AttendanceEvent> result = attendanceService.getAttendanceByEmployee(1L);

        assertThat(result).hasSize(2);
        verify(attendanceEventRepository).findByEmployeeIdAndDeletedFalse(1L);
    }

    @Test
    void testGetAttendanceByEmployee_NoEvents_ReturnsEmptyList() {
        when(attendanceEventRepository.findByEmployeeIdAndDeletedFalse(2L)).thenReturn(Collections.emptyList());

        List<AttendanceEvent> result = attendanceService.getAttendanceByEmployee(2L);

        assertThat(result).isEmpty();
        verify(attendanceEventRepository).findByEmployeeIdAndDeletedFalse(2L);
    }

    @Test
    void testCalculateHoursWorked_ValidClockInOut_ReturnsCorrectHours() {
        LocalDateTime clockIn = LocalDateTime.now().minusHours(5).minusMinutes(30);
        LocalDateTime clockOut = LocalDateTime.now();
        AttendanceEvent event = AttendanceEvent.builder()
            .clockIn(clockIn)
            .clockOut(clockOut)
            .build();

        double hours = attendanceService.calculateHoursWorked(event);

        double expected = Duration.between(clockIn, clockOut).toMinutes() / 60.0;
        assertThat(hours).isCloseTo(expected, within(0.01));
    }

    @Test
    void testCalculateHoursWorked_NullClockIn_ReturnsZero() {
        AttendanceEvent event = AttendanceEvent.builder()
            .clockIn(null)
            .clockOut(LocalDateTime.now())
            .build();

        double hours = attendanceService.calculateHoursWorked(event);

        assertThat(hours).isEqualTo(0.0);
    }

    @Test
    void testCalculateHoursWorked_NullClockOut_ReturnsZero() {
        AttendanceEvent event = AttendanceEvent.builder()
            .clockIn(LocalDateTime.now())
            .clockOut(null)
            .build();

        double hours = attendanceService.calculateHoursWorked(event);

        assertThat(hours).isEqualTo(0.0);
    }

    @Test
    void testCalculateHoursWorked_ClockOutBeforeClockIn_HandlesCorrectly() {
        LocalDateTime clockIn = LocalDateTime.now();
        LocalDateTime clockOut = clockIn.minusHours(2);
        AttendanceEvent event = AttendanceEvent.builder()
            .clockIn(clockIn)
            .clockOut(clockOut)
            .build();

        double hours = attendanceService.calculateHoursWorked(event);

        // Duration will be negative, so hours will be negative
        assertThat(hours).isLessThan(0.0);
    }
}
