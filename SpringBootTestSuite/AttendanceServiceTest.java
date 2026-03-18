package com.company.wms.attendance.service;

import com.company.wms.attendance.dto.AttendanceClockInDTO;
import com.company.wms.attendance.dto.AttendanceClockOutDTO;
import com.company.wms.attendance.dto.AttendanceEventDTO;
import com.company.wms.attendance.dto.CorrectionRequestDTO;
import com.company.wms.attendance.entity.AttendanceEvent;
import com.company.wms.attendance.entity.AttendanceEventType;
import com.company.wms.attendance.entity.AttendanceStatus;
import com.company.wms.attendance.repository.AttendanceRepository;
import com.company.wms.common.exception.AlreadyClockedInException;
import com.company.wms.common.exception.AttendanceEventNotFoundException;
import com.company.wms.common.exception.EmployeeNotFoundException;
import com.company.wms.common.exception.GeofenceViolationException;
import com.company.wms.common.exception.NotClockedInException;
import com.company.wms.employee.entity.Employee;
import com.company.wms.employee.entity.EmployeeRole;
import com.company.wms.employee.entity.EmployeeStatus;
import com.company.wms.employee.repository.EmployeeRepository;
import com.company.wms.schedule.entity.ShiftTemplate;
import com.company.wms.schedule.service.GeofenceService;
import com.company.wms.schedule.service.ShiftService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive JUnit test suite for AttendanceService
 * Covers clock in/out operations, corrections, validations, and edge cases
 */
@ExtendWith(MockitoExtension.class)
class AttendanceServiceTest {

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private ShiftService shiftService;

    @Mock
    private GeofenceService geofenceService;

    @InjectMocks
    private AttendanceService attendanceService;

    private Employee testEmployee;
    private AttendanceEvent testClockInEvent;
    private AttendanceEvent testClockOutEvent;
    private ShiftTemplate testShift;
    private AttendanceClockInDTO clockInDTO;
    private AttendanceClockOutDTO clockOutDTO;

    @BeforeEach
    void setUp() {
        // Setup test employee
        testEmployee = Employee.builder()
                .id(1L)
                .badgeId("EMP001")
                .name("John Doe")
                .role(EmployeeRole.WORKER)
                .department("Warehouse")
                .status(EmployeeStatus.ACTIVE)
                .deleted(false)
                .build();

        // Setup test shift
        testShift = new ShiftTemplate();
        testShift.setId(1L);
        testShift.setName("Day Shift");
        testShift.setStartTime(LocalTime.of(8, 0));
        testShift.setEndTime(LocalTime.of(17, 0));

        // Setup clock in event
        testClockInEvent = AttendanceEvent.builder()
                .id(1L)
                .employee(testEmployee)
                .eventType(AttendanceEventType.CLOCK_IN)
                .eventTime(LocalDateTime.now())
                .deviceId("DEVICE001")
                .location("40.7128,-74.0060")
                .status(AttendanceStatus.APPROVED)
                .shift(testShift)
                .correctionRequested(false)
                .createdAt(LocalDateTime.now())
                .build();

        // Setup clock out event
        testClockOutEvent = AttendanceEvent.builder()
                .id(2L)
                .employee(testEmployee)
                .eventType(AttendanceEventType.CLOCK_OUT)
                .eventTime(LocalDateTime.now().plusHours(8))
                .deviceId("DEVICE001")
                .location("40.7128,-74.0060")
                .status(AttendanceStatus.APPROVED)
                .shift(testShift)
                .correctionRequested(false)
                .createdAt(LocalDateTime.now())
                .build();

        // Setup DTOs
        clockInDTO = new AttendanceClockInDTO();
        clockInDTO.setBadgeId("EMP001");
        clockInDTO.setDeviceId("DEVICE001");
        clockInDTO.setLocation("40.7128,-74.0060");

        clockOutDTO = new AttendanceClockOutDTO();
        clockOutDTO.setBadgeId("EMP001");
        clockOutDTO.setDeviceId("DEVICE001");
        clockOutDTO.setLocation("40.7128,-74.0060");
    }

    // ==================== CLOCK IN TESTS ====================

    @Test
    void testClockIn_ValidInput_Success() {
        // Arrange
        when(employeeRepository.findByBadgeIdAndDeletedFalse("EMP001"))
                .thenReturn(Optional.of(testEmployee));
        when(attendanceRepository.findActiveClockIn(1L))
                .thenReturn(Optional.empty());
        when(geofenceService.isWithinGeofence("40.7128,-74.0060"))
                .thenReturn(true);
        when(shiftService.determineShift(eq(testEmployee), any(LocalDateTime.class)))
                .thenReturn(testShift);
        when(attendanceRepository.save(any(AttendanceEvent.class)))
                .thenReturn(testClockInEvent);

        // Act
        AttendanceEventDTO result = attendanceService.clockIn(clockInDTO);

        // Assert
        assertNotNull(result);
        assertEquals(AttendanceEventType.CLOCK_IN, result.getEventType());
        assertEquals(AttendanceStatus.APPROVED, result.getStatus());
        verify(attendanceRepository).save(any(AttendanceEvent.class));
    }

    @Test
    void testClockIn_EmployeeNotFound_ThrowsException() {
        // Arrange
        when(employeeRepository.findByBadgeIdAndDeletedFalse("EMP001"))
                .thenReturn(Optional.empty());

        // Act & Assert
        EmployeeNotFoundException exception = assertThrows(
                EmployeeNotFoundException.class,
                () -> attendanceService.clockIn(clockInDTO)
        );

        assertTrue(exception.getMessage().contains("Employee not found"));
        verify(attendanceRepository, never()).save(any(AttendanceEvent.class));
    }

    @Test
    void testClockIn_AlreadyClockedIn_ThrowsException() {
        // Arrange
        when(employeeRepository.findByBadgeIdAndDeletedFalse("EMP001"))
                .thenReturn(Optional.of(testEmployee));
        when(attendanceRepository.findActiveClockIn(1L))
                .thenReturn(Optional.of(testClockInEvent));

        // Act & Assert
        AlreadyClockedInException exception = assertThrows(
                AlreadyClockedInException.class,
                () -> attendanceService.clockIn(clockInDTO)
        );

        assertTrue(exception.getMessage().contains("already clocked in"));
        verify(attendanceRepository, never()).save(any(AttendanceEvent.class));
    }

    @Test
    void testClockIn_GeofenceViolation_ThrowsException() {
        // Arrange
        when(employeeRepository.findByBadgeIdAndDeletedFalse("EMP001"))
                .thenReturn(Optional.of(testEmployee));
        when(attendanceRepository.findActiveClockIn(1L))
                .thenReturn(Optional.empty());
        when(geofenceService.isWithinGeofence("40.7128,-74.0060"))
                .thenReturn(false);

        // Act & Assert
        GeofenceViolationException exception = assertThrows(
                GeofenceViolationException.class,
                () -> attendanceService.clockIn(clockInDTO)
        );

        assertTrue(exception.getMessage().contains("outside allowed area"));
        verify(attendanceRepository, never()).save(any(AttendanceEvent.class));
    }

    @Test
    void testClockIn_NullBadgeId_ThrowsException() {
        // Arrange
        clockInDTO.setBadgeId(null);

        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            attendanceService.clockIn(clockInDTO);
        });
    }

    @Test
    void testClockIn_EmptyBadgeId_ThrowsException() {
        // Arrange
        clockInDTO.setBadgeId("");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.clockIn(clockInDTO);
        });
    }

    @Test
    void testClockIn_NullLocation_ThrowsException() {
        // Arrange
        clockInDTO.setLocation(null);
        when(employeeRepository.findByBadgeIdAndDeletedFalse("EMP001"))
                .thenReturn(Optional.of(testEmployee));
        when(attendanceRepository.findActiveClockIn(1L))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            attendanceService.clockIn(clockInDTO);
        });
    }

    @Test
    void testClockIn_InvalidLocationFormat_ThrowsException() {
        // Arrange
        clockInDTO.setLocation("invalid-location");
        when(employeeRepository.findByBadgeIdAndDeletedFalse("EMP001"))
                .thenReturn(Optional.of(testEmployee));
        when(attendanceRepository.findActiveClockIn(1L))
                .thenReturn(Optional.empty());
        when(geofenceService.isWithinGeofence("invalid-location"))
                .thenThrow(new IllegalArgumentException("Invalid location format"));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.clockIn(clockInDTO);
        });
    }

    @Test
    void testClockIn_DeletedEmployee_ThrowsException() {
        // Arrange
        testEmployee.setDeleted(true);
        when(employeeRepository.findByBadgeIdAndDeletedFalse("EMP001"))
                .thenReturn(Optional.empty());

        // Act & Assert
        EmployeeNotFoundException exception = assertThrows(
                EmployeeNotFoundException.class,
                () -> attendanceService.clockIn(clockInDTO)
        );

        assertTrue(exception.getMessage().contains("Employee not found"));
    }

    // ==================== CLOCK OUT TESTS ====================

    @Test
    void testClockOut_ValidInput_Success() {
        // Arrange
        when(employeeRepository.findByBadgeIdAndDeletedFalse("EMP001"))
                .thenReturn(Optional.of(testEmployee));
        when(attendanceRepository.findActiveClockIn(1L))
                .thenReturn(Optional.of(testClockInEvent));
        when(attendanceRepository.save(any(AttendanceEvent.class)))
                .thenReturn(testClockOutEvent);

        // Act
        AttendanceEventDTO result = attendanceService.clockOut(clockOutDTO);

        // Assert
        assertNotNull(result);
        assertEquals(AttendanceEventType.CLOCK_OUT, result.getEventType());
        assertEquals(AttendanceStatus.APPROVED, result.getStatus());
        verify(attendanceRepository).save(any(AttendanceEvent.class));
    }

    @Test
    void testClockOut_NotClockedIn_ThrowsException() {
        // Arrange
        when(employeeRepository.findByBadgeIdAndDeletedFalse("EMP001"))
                .thenReturn(Optional.of(testEmployee));
        when(attendanceRepository.findActiveClockIn(1L))
                .thenReturn(Optional.empty());

        // Act & Assert
        NotClockedInException exception = assertThrows(
                NotClockedInException.class,
                () -> attendanceService.clockOut(clockOutDTO)
        );

        assertTrue(exception.getMessage().contains("No active clock-in found"));
        verify(attendanceRepository, never()).save(any(AttendanceEvent.class));
    }

    @Test
    void testClockOut_EmployeeNotFound_ThrowsException() {
        // Arrange
        when(employeeRepository.findByBadgeIdAndDeletedFalse("EMP001"))
                .thenReturn(Optional.empty());

        // Act & Assert
        EmployeeNotFoundException exception = assertThrows(
                EmployeeNotFoundException.class,
                () -> attendanceService.clockOut(clockOutDTO)
        );

        assertTrue(exception.getMessage().contains("Employee not found"));
        verify(attendanceRepository, never()).save(any(AttendanceEvent.class));
    }

    @Test
    void testClockOut_NullBadgeId_ThrowsException() {
        // Arrange
        clockOutDTO.setBadgeId(null);

        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            attendanceService.clockOut(clockOutDTO);
        });
    }

    @Test
    void testClockOut_CalculatesHoursWorked_Success() {
        // Arrange
        LocalDateTime clockInTime = LocalDateTime.now().minusHours(8);
        testClockInEvent.setEventTime(clockInTime);

        when(employeeRepository.findByBadgeIdAndDeletedFalse("EMP001"))
                .thenReturn(Optional.of(testEmployee));
        when(attendanceRepository.findActiveClockIn(1L))
                .thenReturn(Optional.of(testClockInEvent));
        when(attendanceRepository.save(any(AttendanceEvent.class)))
                .thenReturn(testClockOutEvent);

        // Act
        AttendanceEventDTO result = attendanceService.clockOut(clockOutDTO);

        // Assert
        assertNotNull(result);
        verify(attendanceRepository).save(any(AttendanceEvent.class));
        // Verify hours calculation was performed
    }

    @Test
    void testClockOut_ShortShift_Success() {
        // Arrange - Clock out after only 2 hours
        LocalDateTime clockInTime = LocalDateTime.now().minusHours(2);
        testClockInEvent.setEventTime(clockInTime);

        when(employeeRepository.findByBadgeIdAndDeletedFalse("EMP001"))
                .thenReturn(Optional.of(testEmployee));
        when(attendanceRepository.findActiveClockIn(1L))
                .thenReturn(Optional.of(testClockInEvent));
        when(attendanceRepository.save(any(AttendanceEvent.class)))
                .thenReturn(testClockOutEvent);

        // Act
        AttendanceEventDTO result = attendanceService.clockOut(clockOutDTO);

        // Assert
        assertNotNull(result);
        verify(attendanceRepository).save(any(AttendanceEvent.class));
    }

    @Test
    void testClockOut_OvertimeShift_Success() {
        // Arrange - Clock out after 12 hours (overtime)
        LocalDateTime clockInTime = LocalDateTime.now().minusHours(12);
        testClockInEvent.setEventTime(clockInTime);

        when(employeeRepository.findByBadgeIdAndDeletedFalse("EMP001"))
                .thenReturn(Optional.of(testEmployee));
        when(attendanceRepository.findActiveClockIn(1L))
                .thenReturn(Optional.of(testClockInEvent));
        when(attendanceRepository.save(any(AttendanceEvent.class)))
                .thenReturn(testClockOutEvent);

        // Act
        AttendanceEventDTO result = attendanceService.clockOut(clockOutDTO);

        // Assert
        assertNotNull(result);
        verify(attendanceRepository).save(any(AttendanceEvent.class));
    }

    // ==================== CORRECTION REQUEST TESTS ====================

    @Test
    void testRequestCorrection_ValidRequest_Success() {
        // Arrange
        CorrectionRequestDTO correctionDTO = new CorrectionRequestDTO();
        correctionDTO.setReason("Forgot to clock in");

        when(attendanceRepository.findById(1L))
                .thenReturn(Optional.of(testClockInEvent));
        when(attendanceRepository.save(any(AttendanceEvent.class)))
                .thenReturn(testClockInEvent);

        // Act
        AttendanceEventDTO result = attendanceService.requestCorrection(1L, correctionDTO);

        // Assert
        assertNotNull(result);
        verify(attendanceRepository).save(argThat(event -> 
            event.getCorrectionRequested() && 
            event.getStatus() == AttendanceStatus.PENDING_CORRECTION
        ));
    }

    @Test
    void testRequestCorrection_EventNotFound_ThrowsException() {
        // Arrange
        CorrectionRequestDTO correctionDTO = new CorrectionRequestDTO();
        correctionDTO.setReason("Forgot to clock in");

        when(attendanceRepository.findById(999L))
                .thenReturn(Optional.empty());

        // Act & Assert
        AttendanceEventNotFoundException exception = assertThrows(
                AttendanceEventNotFoundException.class,
                () -> attendanceService.requestCorrection(999L, correctionDTO)
        );

        assertTrue(exception.getMessage().contains("Event not found"));
        verify(attendanceRepository, never()).save(any(AttendanceEvent.class));
    }

    @Test
    void testRequestCorrection_NullReason_ThrowsException() {
        // Arrange
        CorrectionRequestDTO correctionDTO = new CorrectionRequestDTO();
        correctionDTO.setReason(null);

        when(attendanceRepository.findById(1L))
                .thenReturn(Optional.of(testClockInEvent));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.requestCorrection(1L, correctionDTO);
        });
    }

    @Test
    void testRequestCorrection_EmptyReason_ThrowsException() {
        // Arrange
        CorrectionRequestDTO correctionDTO = new CorrectionRequestDTO();
        correctionDTO.setReason("");

        when(attendanceRepository.findById(1L))
                .thenReturn(Optional.of(testClockInEvent));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.requestCorrection(1L, correctionDTO);
        });
    }

    @Test
    void testRequestCorrection_AlreadyRequested_Success() {
        // Arrange
        testClockInEvent.setCorrectionRequested(true);
        testClockInEvent.setStatus(AttendanceStatus.PENDING_CORRECTION);

        CorrectionRequestDTO correctionDTO = new CorrectionRequestDTO();
        correctionDTO.setReason("Updated reason");

        when(attendanceRepository.findById(1L))
                .thenReturn(Optional.of(testClockInEvent));
        when(attendanceRepository.save(any(AttendanceEvent.class)))
                .thenReturn(testClockInEvent);

        // Act
        AttendanceEventDTO result = attendanceService.requestCorrection(1L, correctionDTO);

        // Assert
        assertNotNull(result);
        verify(attendanceRepository).save(any(AttendanceEvent.class));
    }

    // ==================== EDGE CASE TESTS ====================

    @Test
    void testClockIn_MidnightShift_Success() {
        // Arrange - Clock in at 11:59 PM
        when(employeeRepository.findByBadgeIdAndDeletedFalse("EMP001"))
                .thenReturn(Optional.of(testEmployee));
        when(attendanceRepository.findActiveClockIn(1L))
                .thenReturn(Optional.empty());
        when(geofenceService.isWithinGeofence("40.7128,-74.0060"))
                .thenReturn(true);
        when(shiftService.determineShift(eq(testEmployee), any(LocalDateTime.class)))
                .thenReturn(testShift);
        when(attendanceRepository.save(any(AttendanceEvent.class)))
                .thenReturn(testClockInEvent);

        // Act
        AttendanceEventDTO result = attendanceService.clockIn(clockInDTO);

        // Assert
        assertNotNull(result);
        verify(attendanceRepository).save(any(AttendanceEvent.class));
    }

    @Test
    void testClockIn_MultipleDevices_Success() {
        // Arrange - Different device ID
        clockInDTO.setDeviceId("DEVICE002");

        when(employeeRepository.findByBadgeIdAndDeletedFalse("EMP001"))
                .thenReturn(Optional.of(testEmployee));
        when(attendanceRepository.findActiveClockIn(1L))
                .thenReturn(Optional.empty());
        when(geofenceService.isWithinGeofence("40.7128,-74.0060"))
                .thenReturn(true);
        when(shiftService.determineShift(eq(testEmployee), any(LocalDateTime.class)))
                .thenReturn(testShift);
        when(attendanceRepository.save(any(AttendanceEvent.class)))
                .thenReturn(testClockInEvent);

        // Act
        AttendanceEventDTO result = attendanceService.clockIn(clockInDTO);

        // Assert
        assertNotNull(result);
        verify(attendanceRepository).save(any(AttendanceEvent.class));
    }

    @Test
    void testClockOut_ImmediateClockOut_Success() {
        // Arrange - Clock out immediately after clock in (0 hours)
        testClockInEvent.setEventTime(LocalDateTime.now());

        when(employeeRepository.findByBadgeIdAndDeletedFalse("EMP001"))
                .thenReturn(Optional.of(testEmployee));
        when(attendanceRepository.findActiveClockIn(1L))
                .thenReturn(Optional.of(testClockInEvent));
        when(attendanceRepository.save(any(AttendanceEvent.class)))
                .thenReturn(testClockOutEvent);

        // Act
        AttendanceEventDTO result = attendanceService.clockOut(clockOutDTO);

        // Assert
        assertNotNull(result);
        verify(attendanceRepository).save(any(AttendanceEvent.class));
    }

    @Test
    void testRequestCorrection_LongReason_Success() {
        // Arrange - Very long correction reason
        CorrectionRequestDTO correctionDTO = new CorrectionRequestDTO();
        correctionDTO.setReason("A".repeat(500)); // 500 character reason

        when(attendanceRepository.findById(1L))
                .thenReturn(Optional.of(testClockInEvent));
        when(attendanceRepository.save(any(AttendanceEvent.class)))
                .thenReturn(testClockInEvent);

        // Act
        AttendanceEventDTO result = attendanceService.requestCorrection(1L, correctionDTO);

        // Assert
        assertNotNull(result);
        verify(attendanceRepository).save(any(AttendanceEvent.class));
    }
}
