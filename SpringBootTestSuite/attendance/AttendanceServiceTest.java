package com.warehouse.ems.attendance;

import com.warehouse.ems.employee.Employee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.*;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive service tests for Attendance
 * Tests cover: clock-in/out, hours calculation, missed punches, validations
 */
class AttendanceServiceTest {

    @Mock
    private AttendanceRepository attendanceRepository;

    @InjectMocks
    private AttendanceService attendanceService;

    private Employee employee;
    private AttendanceEvent clockInEvent;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        employee = Employee.builder().id(1L).name("John Doe").badgeId("B123").build();
        clockInEvent = AttendanceEvent.builder()
                .id(1L)
                .employee(employee)
                .timestamp(LocalDateTime.now())
                .type(EventType.IN)
                .deviceId("DEVICE123")
                .location("Warehouse A")
                .build();
    }

    @Test
    @DisplayName("Should clock in successfully")
    void testClockIn() {
        // Arrange
        when(attendanceRepository.save(any(AttendanceEvent.class))).thenReturn(clockInEvent);
        when(attendanceRepository.findLatestByEmployee(employee.getId())).thenReturn(Optional.empty());

        // Act
        AttendanceEvent result = attendanceService.clockIn(employee, "DEVICE123", "Warehouse A");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getType()).isEqualTo(EventType.IN);
        assertThat(result.getEmployee()).isEqualTo(employee);
        verify(attendanceRepository, times(1)).save(any(AttendanceEvent.class));
    }

    @Test
    @DisplayName("Should throw exception when clocking in twice")
    void testDuplicateClockIn() {
        // Arrange
        when(attendanceRepository.findLatestByEmployee(employee.getId()))
                .thenReturn(Optional.of(clockInEvent));

        // Act & Assert
        assertThatThrownBy(() -> attendanceService.clockIn(employee, "DEVICE123", "Warehouse A"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Already clocked in");
    }

    @Test
    @DisplayName("Should clock out successfully")
    void testClockOut() {
        // Arrange
        AttendanceEvent clockOutEvent = AttendanceEvent.builder()
                .id(2L)
                .employee(employee)
                .timestamp(LocalDateTime.now().plusHours(8))
                .type(EventType.OUT)
                .build();
        when(attendanceRepository.findLatestByEmployee(employee.getId()))
                .thenReturn(Optional.of(clockInEvent));
        when(attendanceRepository.save(any(AttendanceEvent.class))).thenReturn(clockOutEvent);

        // Act
        AttendanceEvent result = attendanceService.clockOut(employee, "DEVICE123", "Warehouse A");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getType()).isEqualTo(EventType.OUT);
        verify(attendanceRepository, times(1)).save(any(AttendanceEvent.class));
    }

    @Test
    @DisplayName("Should throw exception when clocking out without clock in")
    void testClockOutWithoutClockIn() {
        // Arrange
        when(attendanceRepository.findLatestByEmployee(employee.getId()))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> attendanceService.clockOut(employee, "DEVICE123", "Warehouse A"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No clock-in found");
    }

    @Test
    @DisplayName("Should calculate hours worked correctly")
    void testCalculateHoursWorked() {
        // Arrange
        LocalDateTime clockIn = LocalDateTime.of(2024, 1, 15, 9, 0);
        LocalDateTime clockOut = LocalDateTime.of(2024, 1, 15, 17, 0);
        AttendanceEvent inEvent = AttendanceEvent.builder()
                .timestamp(clockIn)
                .type(EventType.IN)
                .build();
        AttendanceEvent outEvent = AttendanceEvent.builder()
                .timestamp(clockOut)
                .type(EventType.OUT)
                .build();

        // Act
        double hours = attendanceService.calculateHours(inEvent, outEvent);

        // Assert
        assertThat(hours).isEqualTo(8.0);
    }

    @Test
    @DisplayName("Should calculate hours for overnight shift")
    void testCalculateHoursOvernightShift() {
        // Arrange
        LocalDateTime clockIn = LocalDateTime.of(2024, 1, 15, 23, 0);
        LocalDateTime clockOut = LocalDateTime.of(2024, 1, 16, 7, 0);
        AttendanceEvent inEvent = AttendanceEvent.builder()
                .timestamp(clockIn)
                .type(EventType.IN)
                .build();
        AttendanceEvent outEvent = AttendanceEvent.builder()
                .timestamp(clockOut)
                .type(EventType.OUT)
                .build();

        // Act
        double hours = attendanceService.calculateHours(inEvent, outEvent);

        // Assert
        assertThat(hours).isEqualTo(8.0);
    }

    @Test
    @DisplayName("Should handle missed punch correction")
    void testMissedPunchCorrection() {
        // Arrange
        LocalDateTime correctionTime = LocalDateTime.now().minusHours(2);
        when(attendanceRepository.save(any(AttendanceEvent.class))).thenReturn(clockInEvent);

        // Act
        AttendanceEvent correction = attendanceService.createMissedPunchCorrection(
                employee, EventType.OUT, correctionTime, "Forgot to clock out");

        // Assert
        assertThat(correction).isNotNull();
        assertThat(correction.getType()).isEqualTo(EventType.OUT);
        verify(attendanceRepository, times(1)).save(any(AttendanceEvent.class));
    }

    @Test
    @DisplayName("Should validate geofence location")
    void testGeofenceValidation() {
        // Arrange
        String validLocation = "40.7128,-74.0060"; // NYC
        String warehouseLocation = "40.7128,-74.0060";

        // Act
        boolean isValid = attendanceService.validateGeofence(validLocation, warehouseLocation, 100.0);

        // Assert
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("Should reject clock-in outside geofence")
    void testGeofenceRejection() {
        // Arrange
        String employeeLocation = "34.0522,-118.2437"; // LA
        String warehouseLocation = "40.7128,-74.0060"; // NYC

        // Act
        boolean isValid = attendanceService.validateGeofence(employeeLocation, warehouseLocation, 100.0);

        // Assert
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should get daily attendance summary")
    void testGetDailyAttendanceSummary() {
        // Arrange
        LocalDateTime date = LocalDateTime.now();
        List<AttendanceEvent> events = Arrays.asList(clockInEvent);
        when(attendanceRepository.findByEmployeeAndDate(employee.getId(), date.toLocalDate()))
                .thenReturn(events);

        // Act
        List<AttendanceEvent> summary = attendanceService.getDailyAttendance(employee.getId(), date.toLocalDate());

        // Assert
        assertThat(summary).hasSize(1);
        assertThat(summary.get(0)).isEqualTo(clockInEvent);
    }

    @Test
    @DisplayName("Should export attendance to CSV")
    void testExportAttendanceToCSV() {
        // Arrange
        List<AttendanceEvent> events = Arrays.asList(clockInEvent);
        when(attendanceRepository.findByDateRange(any(), any())).thenReturn(events);

        // Act
        String csv = attendanceService.exportToCSV(LocalDateTime.now().minusDays(7), LocalDateTime.now());

        // Assert
        assertThat(csv).isNotNull();
        assertThat(csv).contains("Employee,Timestamp,Type,Device,Location");
        assertThat(csv).contains(employee.getName());
    }

    @Test
    @DisplayName("Should throw exception for null employee")
    void testClockInWithNullEmployee() {
        // Act & Assert
        assertThatThrownBy(() -> attendanceService.clockIn(null, "DEVICE123", "Warehouse A"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Employee cannot be null");
    }

    @Test
    @DisplayName("Should handle concurrent clock-in attempts")
    void testConcurrentClockIn() {
        // Arrange
        when(attendanceRepository.findLatestByEmployee(employee.getId()))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(clockInEvent));

        // Act
        AttendanceEvent first = attendanceService.clockIn(employee, "DEVICE123", "Warehouse A");

        // Assert
        assertThat(first).isNotNull();
        assertThatThrownBy(() -> attendanceService.clockIn(employee, "DEVICE123", "Warehouse A"))
                .isInstanceOf(IllegalStateException.class);
    }
}