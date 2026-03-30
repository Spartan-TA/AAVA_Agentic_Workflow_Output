package com.companyname.wems.attendance.service;

import com.companyname.wems.attendance.dto.ClockEventRequest;
import com.companyname.wems.attendance.dto.AttendanceEventResponse;
import com.companyname.wems.attendance.entity.AttendanceEvent;
import com.companyname.wems.attendance.repository.AttendanceEventRepository;
import com.companyname.wems.employee.entity.Employee;
import com.companyname.wems.employee.repository.EmployeeRepository;
import com.companyname.wems.exception.BusinessException;
import com.companyname.wems.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive JUnit test suite for AttendanceService
 * Tests cover clock-in/out operations, geofencing, shift association, and corrections
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Attendance Service Tests")
class AttendanceServiceTest {

    @Mock
    private AttendanceEventRepository attendanceEventRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private GeofenceService geofenceService;

    @InjectMocks
    private AttendanceService attendanceService;

    private Employee validEmployee;
    private ClockEventRequest validClockInRequest;
    private AttendanceEvent validClockInEvent;

    @BeforeEach
    void setUp() {
        validEmployee = Employee.builder()
                .id(1L)
                .name("John Doe")
                .badgeId("EMP12345")
                .role(Employee.Role.WORKER)
                .department("Shipping")
                .status(Employee.Status.ACTIVE)
                .build();

        validClockInRequest = ClockEventRequest.builder()
                .badgeId("EMP12345")
                .deviceId("DEVICE001")
                .latitude(37.7749)
                .longitude(-122.4194)
                .location("Warehouse A")
                .build();

        validClockInEvent = AttendanceEvent.builder()
                .id(1L)
                .employee(validEmployee)
                .eventType(AttendanceEvent.EventType.CLOCK_IN)
                .timestamp(LocalDateTime.now())
                .deviceId("DEVICE001")
                .ipAddress("192.168.1.100")
                .latitude(37.7749)
                .longitude(-122.4194)
                .location("Warehouse A")
                .status(AttendanceEvent.Status.NORMAL)
                .build();
    }

    // ========== CLOCK IN TESTS ==========

    @Test
    @DisplayName("Should clock in employee with valid input")
    void testClockIn_ValidInput_Success() {
        // Arrange
        when(employeeRepository.findByBadgeId("EMP12345")).thenReturn(Optional.of(validEmployee));
        when(geofenceService.isWithinAllowedArea(anyDouble(), anyDouble())).thenReturn(true);
        when(attendanceEventRepository.save(any(AttendanceEvent.class))).thenReturn(validClockInEvent);

        // Act
        AttendanceEventResponse response = attendanceService.clockIn(validClockInRequest, "192.168.1.100");

        // Assert
        assertNotNull(response);
        assertEquals(AttendanceEvent.EventType.CLOCK_IN, response.getEventType());
        assertEquals("EMP12345", response.getEmployeeBadgeId());
        verify(employeeRepository, times(1)).findByBadgeId("EMP12345");
        verify(attendanceEventRepository, times(1)).save(any(AttendanceEvent.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when employee not found")
    void testClockIn_EmployeeNotFound_ThrowsException() {
        // Arrange
        when(employeeRepository.findByBadgeId("INVALID")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            attendanceService.clockIn(validClockInRequest, "192.168.1.100");
        });
        verify(attendanceEventRepository, never()).save(any(AttendanceEvent.class));
    }

    @Test
    @DisplayName("Should throw BusinessException when employee is not active")
    void testClockIn_InactiveEmployee_ThrowsException() {
        // Arrange
        validEmployee.setStatus(Employee.Status.INACTIVE);
        when(employeeRepository.findByBadgeId("EMP12345")).thenReturn(Optional.of(validEmployee));

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            attendanceService.clockIn(validClockInRequest, "192.168.1.100");
        });
    }

    @Test
    @DisplayName("Should throw BusinessException when location is outside geofence")
    void testClockIn_OutsideGeofence_ThrowsException() {
        // Arrange
        when(employeeRepository.findByBadgeId("EMP12345")).thenReturn(Optional.of(validEmployee));
        when(geofenceService.isWithinAllowedArea(anyDouble(), anyDouble())).thenReturn(false);

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            attendanceService.clockIn(validClockInRequest, "192.168.1.100");
        });
        verify(attendanceEventRepository, never()).save(any(AttendanceEvent.class));
    }

    @Test
    @DisplayName("Should allow clock in without geofence coordinates")
    void testClockIn_NoGeofenceCoordinates_Success() {
        // Arrange
        validClockInRequest.setLatitude(null);
        validClockInRequest.setLongitude(null);
        when(employeeRepository.findByBadgeId("EMP12345")).thenReturn(Optional.of(validEmployee));
        when(attendanceEventRepository.save(any(AttendanceEvent.class))).thenReturn(validClockInEvent);

        // Act
        AttendanceEventResponse response = attendanceService.clockIn(validClockInRequest, "192.168.1.100");

        // Assert
        assertNotNull(response);
        verify(geofenceService, never()).isWithinAllowedArea(anyDouble(), anyDouble());
    }

    @Test
    @DisplayName("Should throw BusinessException when badge ID is null")
    void testClockIn_NullBadgeId_ThrowsException() {
        // Arrange
        validClockInRequest.setBadgeId(null);

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            attendanceService.clockIn(validClockInRequest, "192.168.1.100");
        });
    }

    @Test
    @DisplayName("Should throw BusinessException when device ID is null")
    void testClockIn_NullDeviceId_ThrowsException() {
        // Arrange
        validClockInRequest.setDeviceId(null);

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            attendanceService.clockIn(validClockInRequest, "192.168.1.100");
        });
    }

    @Test
    @DisplayName("Should throw BusinessException when already clocked in")
    void testClockIn_AlreadyClockedIn_ThrowsException() {
        // Arrange
        when(employeeRepository.findByBadgeId("EMP12345")).thenReturn(Optional.of(validEmployee));
        when(attendanceEventRepository.findLatestEventByEmployee(validEmployee))
                .thenReturn(Optional.of(validClockInEvent));

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            attendanceService.clockIn(validClockInRequest, "192.168.1.100");
        });
    }

    @Test
    @DisplayName("Should capture IP address on clock in")
    void testClockIn_CapturesIpAddress_Success() {
        // Arrange
        when(employeeRepository.findByBadgeId("EMP12345")).thenReturn(Optional.of(validEmployee));
        when(geofenceService.isWithinAllowedArea(anyDouble(), anyDouble())).thenReturn(true);
        when(attendanceEventRepository.save(any(AttendanceEvent.class))).thenReturn(validClockInEvent);

        // Act
        AttendanceEventResponse response = attendanceService.clockIn(validClockInRequest, "192.168.1.100");

        // Assert
        assertNotNull(response);
        verify(attendanceEventRepository, times(1)).save(argThat(event -> 
            "192.168.1.100".equals(event.getIpAddress())
        ));
    }

    // ========== CLOCK OUT TESTS ==========

    @Test
    @DisplayName("Should clock out employee with valid input")
    void testClockOut_ValidInput_Success() {
        // Arrange
        ClockEventRequest clockOutRequest = ClockEventRequest.builder()
                .badgeId("EMP12345")
                .deviceId("DEVICE001")
                .build();
        AttendanceEvent clockOutEvent = AttendanceEvent.builder()
                .id(2L)
                .employee(validEmployee)
                .eventType(AttendanceEvent.EventType.CLOCK_OUT)
                .timestamp(LocalDateTime.now())
                .deviceId("DEVICE001")
                .status(AttendanceEvent.Status.NORMAL)
                .hoursWorked(8.0)
                .build();

        when(employeeRepository.findByBadgeId("EMP12345")).thenReturn(Optional.of(validEmployee));
        when(attendanceEventRepository.findLatestEventByEmployee(validEmployee))
                .thenReturn(Optional.of(validClockInEvent));
        when(attendanceEventRepository.save(any(AttendanceEvent.class))).thenReturn(clockOutEvent);

        // Act
        AttendanceEventResponse response = attendanceService.clockOut(clockOutRequest, "192.168.1.100");

        // Assert
        assertNotNull(response);
        assertEquals(AttendanceEvent.EventType.CLOCK_OUT, response.getEventType());
        assertNotNull(response.getHoursWorked());
        verify(attendanceEventRepository, times(1)).save(any(AttendanceEvent.class));
    }

    @Test
    @DisplayName("Should throw BusinessException when not clocked in")
    void testClockOut_NotClockedIn_ThrowsException() {
        // Arrange
        ClockEventRequest clockOutRequest = ClockEventRequest.builder()
                .badgeId("EMP12345")
                .deviceId("DEVICE001")
                .build();
        when(employeeRepository.findByBadgeId("EMP12345")).thenReturn(Optional.of(validEmployee));
        when(attendanceEventRepository.findLatestEventByEmployee(validEmployee))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            attendanceService.clockOut(clockOutRequest, "192.168.1.100");
        });
    }

    @Test
    @DisplayName("Should calculate hours worked correctly")
    void testClockOut_CalculatesHoursWorked_Success() {
        // Arrange
        ClockEventRequest clockOutRequest = ClockEventRequest.builder()
                .badgeId("EMP12345")
                .deviceId("DEVICE001")
                .build();
        validClockInEvent.setTimestamp(LocalDateTime.now().minusHours(8));
        AttendanceEvent clockOutEvent = AttendanceEvent.builder()
                .id(2L)
                .employee(validEmployee)
                .eventType(AttendanceEvent.EventType.CLOCK_OUT)
                .timestamp(LocalDateTime.now())
                .hoursWorked(8.0)
                .build();

        when(employeeRepository.findByBadgeId("EMP12345")).thenReturn(Optional.of(validEmployee));
        when(attendanceEventRepository.findLatestEventByEmployee(validEmployee))
                .thenReturn(Optional.of(validClockInEvent));
        when(attendanceEventRepository.save(any(AttendanceEvent.class))).thenReturn(clockOutEvent);

        // Act
        AttendanceEventResponse response = attendanceService.clockOut(clockOutRequest, "192.168.1.100");

        // Assert
        assertNotNull(response.getHoursWorked());
        assertTrue(response.getHoursWorked() > 0);
    }

    // ========== MISSED PUNCH CORRECTION TESTS ==========

    @Test
    @DisplayName("Should create missed punch correction request")
    void testRequestMissedPunchCorrection_ValidInput_Success() {
        // Arrange
        MissedPunchCorrectionRequest request = MissedPunchCorrectionRequest.builder()
                .employeeBadgeId("EMP12345")
                .eventType(AttendanceEvent.EventType.CLOCK_IN)
                .timestamp(LocalDateTime.now().minusHours(1))
                .reason("Forgot to clock in")
                .build();
        MissedPunchCorrection correction = MissedPunchCorrection.builder()
                .id(1L)
                .employee(validEmployee)
                .status(MissedPunchCorrection.Status.PENDING)
                .build();

        when(employeeRepository.findByBadgeId("EMP12345")).thenReturn(Optional.of(validEmployee));
        when(missedPunchCorrectionRepository.save(any(MissedPunchCorrection.class))).thenReturn(correction);

        // Act
        MissedPunchCorrectionResponse response = attendanceService.requestMissedPunchCorrection(request);

        // Assert
        assertNotNull(response);
        assertEquals(MissedPunchCorrection.Status.PENDING, response.getStatus());
        verify(missedPunchCorrectionRepository, times(1)).save(any(MissedPunchCorrection.class));
    }

    @Test
    @DisplayName("Should throw BusinessException when correction timestamp is in future")
    void testRequestMissedPunchCorrection_FutureTimestamp_ThrowsException() {
        // Arrange
        MissedPunchCorrectionRequest request = MissedPunchCorrectionRequest.builder()
                .employeeBadgeId("EMP12345")
                .eventType(AttendanceEvent.EventType.CLOCK_IN)
                .timestamp(LocalDateTime.now().plusHours(1))
                .reason("Invalid future time")
                .build();

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            attendanceService.requestMissedPunchCorrection(request);
        });
    }

    @Test
    @DisplayName("Should approve missed punch correction")
    void testApproveMissedPunchCorrection_ValidId_Success() {
        // Arrange
        MissedPunchCorrection correction = MissedPunchCorrection.builder()
                .id(1L)
                .employee(validEmployee)
                .status(MissedPunchCorrection.Status.PENDING)
                .eventType(AttendanceEvent.EventType.CLOCK_IN)
                .timestamp(LocalDateTime.now().minusHours(1))
                .build();

        when(missedPunchCorrectionRepository.findById(1L)).thenReturn(Optional.of(correction));
        when(missedPunchCorrectionRepository.save(any(MissedPunchCorrection.class))).thenReturn(correction);
        when(attendanceEventRepository.save(any(AttendanceEvent.class))).thenReturn(validClockInEvent);

        // Act
        MissedPunchCorrectionResponse response = attendanceService.approveMissedPunchCorrection(1L, "SUPERVISOR001");

        // Assert
        assertNotNull(response);
        assertEquals(MissedPunchCorrection.Status.APPROVED, response.getStatus());
        verify(attendanceEventRepository, times(1)).save(any(AttendanceEvent.class));
    }

    @Test
    @DisplayName("Should reject missed punch correction")
    void testRejectMissedPunchCorrection_ValidId_Success() {
        // Arrange
        MissedPunchCorrection correction = MissedPunchCorrection.builder()
                .id(1L)
                .employee(validEmployee)
                .status(MissedPunchCorrection.Status.PENDING)
                .build();

        when(missedPunchCorrectionRepository.findById(1L)).thenReturn(Optional.of(correction));
        when(missedPunchCorrectionRepository.save(any(MissedPunchCorrection.class))).thenReturn(correction);

        // Act
        MissedPunchCorrectionResponse response = attendanceService.rejectMissedPunchCorrection(1L, "SUPERVISOR001", "Invalid reason");

        // Assert
        assertNotNull(response);
        assertEquals(MissedPunchCorrection.Status.REJECTED, response.getStatus());
        verify(attendanceEventRepository, never()).save(any(AttendanceEvent.class));
    }

    // ========== SHIFT ASSOCIATION TESTS ==========

    @Test
    @DisplayName("Should automatically associate clock event with scheduled shift")
    void testClockIn_AutoAssociateShift_Success() {
        // Arrange
        ShiftAssignment shift = ShiftAssignment.builder()
                .id(1L)
                .employee(validEmployee)
                .shiftDate(LocalDateTime.now().toLocalDate())
                .build();

        when(employeeRepository.findByBadgeId("EMP12345")).thenReturn(Optional.of(validEmployee));
        when(geofenceService.isWithinAllowedArea(anyDouble(), anyDouble())).thenReturn(true);
        when(shiftAssignmentRepository.findByEmployeeAndDate(validEmployee, LocalDateTime.now().toLocalDate()))
                .thenReturn(Optional.of(shift));
        when(attendanceEventRepository.save(any(AttendanceEvent.class))).thenReturn(validClockInEvent);

        // Act
        AttendanceEventResponse response = attendanceService.clockIn(validClockInRequest, "192.168.1.100");

        // Assert
        assertNotNull(response);
        verify(attendanceEventRepository, times(1)).save(argThat(event -> 
            event.getShiftAssignment() != null
        ));
    }

    // ========== ATTENDANCE REPORT TESTS ==========

    @Test
    @DisplayName("Should generate daily attendance report")
    void testGenerateDailyReport_ValidDate_Success() {
        // Arrange
        LocalDate reportDate = LocalDate.now();
        List<AttendanceEvent> events = Arrays.asList(validClockInEvent);
        when(attendanceEventRepository.findByDateRange(any(), any())).thenReturn(events);

        // Act
        AttendanceReportResponse response = attendanceService.generateDailyReport(reportDate);

        // Assert
        assertNotNull(response);
        assertNotNull(response.getTotalHours());
        verify(attendanceEventRepository, times(1)).findByDateRange(any(), any());
    }

    @Test
    @DisplayName("Should export attendance report as CSV")
    void testExportAttendanceReport_ValidDateRange_Success() {
        // Arrange
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();
        List<AttendanceEvent> events = Arrays.asList(validClockInEvent);
        when(attendanceEventRepository.findByDateRange(any(), any())).thenReturn(events);

        // Act
        byte[] csvData = attendanceService.exportAttendanceReportCsv(startDate, endDate);

        // Assert
        assertNotNull(csvData);
        assertTrue(csvData.length > 0);
    }

    // ========== EDGE CASE TESTS ==========

    @Test
    @DisplayName("Should handle clock in at exactly midnight")
    void testClockIn_AtMidnight_Success() {
        // Arrange
        validClockInEvent.setTimestamp(LocalDateTime.now().withHour(0).withMinute(0).withSecond(0));
        when(employeeRepository.findByBadgeId("EMP12345")).thenReturn(Optional.of(validEmployee));
        when(geofenceService.isWithinAllowedArea(anyDouble(), anyDouble())).thenReturn(true);
        when(attendanceEventRepository.save(any(AttendanceEvent.class))).thenReturn(validClockInEvent);

        // Act
        AttendanceEventResponse response = attendanceService.clockIn(validClockInRequest, "192.168.1.100");

        // Assert
        assertNotNull(response);
    }

    @Test
    @DisplayName("Should handle very long shift (over 24 hours)")
    void testClockOut_VeryLongShift_Success() {
        // Arrange
        ClockEventRequest clockOutRequest = ClockEventRequest.builder()
                .badgeId("EMP12345")
                .deviceId("DEVICE001")
                .build();
        validClockInEvent.setTimestamp(LocalDateTime.now().minusHours(30));
        AttendanceEvent clockOutEvent = AttendanceEvent.builder()
                .id(2L)
                .employee(validEmployee)
                .eventType(AttendanceEvent.EventType.CLOCK_OUT)
                .timestamp(LocalDateTime.now())
                .hoursWorked(30.0)
                .build();

        when(employeeRepository.findByBadgeId("EMP12345")).thenReturn(Optional.of(validEmployee));
        when(attendanceEventRepository.findLatestEventByEmployee(validEmployee))
                .thenReturn(Optional.of(validClockInEvent));
        when(attendanceEventRepository.save(any(AttendanceEvent.class))).thenReturn(clockOutEvent);

        // Act
        AttendanceEventResponse response = attendanceService.clockOut(clockOutRequest, "192.168.1.100");

        // Assert
        assertNotNull(response);
        assertTrue(response.getHoursWorked() > 24);
    }

    @Test
    @DisplayName("Should handle null IP address")
    void testClockIn_NullIpAddress_Success() {
        // Arrange
        when(employeeRepository.findByBadgeId("EMP12345")).thenReturn(Optional.of(validEmployee));
        when(geofenceService.isWithinAllowedArea(anyDouble(), anyDouble())).thenReturn(true);
        when(attendanceEventRepository.save(any(AttendanceEvent.class))).thenReturn(validClockInEvent);

        // Act
        AttendanceEventResponse response = attendanceService.clockIn(validClockInRequest, null);

        // Assert
        assertNotNull(response);
    }

    @Test
    @DisplayName("Should handle geofence boundary coordinates")
    void testClockIn_BoundaryCoordinates_Success() {
        // Arrange
        validClockInRequest.setLatitude(90.0); // North pole
        validClockInRequest.setLongitude(180.0); // Date line
        when(employeeRepository.findByBadgeId("EMP12345")).thenReturn(Optional.of(validEmployee));
        when(geofenceService.isWithinAllowedArea(90.0, 180.0)).thenReturn(true);
        when(attendanceEventRepository.save(any(AttendanceEvent.class))).thenReturn(validClockInEvent);

        // Act
        AttendanceEventResponse response = attendanceService.clockIn(validClockInRequest, "192.168.1.100");

        // Assert
        assertNotNull(response);
    }
}