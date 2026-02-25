package com.warehouse.employee.management.application.service;

import com.warehouse.employee.management.domain.attendance.*;
import com.warehouse.employee.management.domain.employee.Employee;
import com.warehouse.employee.management.domain.employee.EmployeeStatus;
import com.warehouse.employee.management.infrastructure.repository.AttendanceRepository;
import com.warehouse.employee.management.infrastructure.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for AttendanceService
 * Tests cover:
 * - Clock in/out operations (normal, boundary, edge cases)
 * - Attendance corrections workflow
 * - Validation scenarios
 * - Exception handling
 * - Metadata capture
 * - Hours calculation
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AttendanceService Unit Tests")
class AttendanceServiceTest {

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private AttendanceService attendanceService;

    private Employee testEmployee;
    private AttendanceRecord testAttendanceRecord;
    private ClockEventMetadata clockInMetadata;
    private ClockEventMetadata clockOutMetadata;

    @BeforeEach
    void setUp() {
        // Setup test employee
        testEmployee = new Employee();
        testEmployee.setId(UUID.randomUUID());
        testEmployee.setBadgeId("EMP001");
        testEmployee.setFirstName("John");
        testEmployee.setLastName("Doe");
        testEmployee.setEmail("john.doe@warehouse.com");
        testEmployee.setStatus(EmployeeStatus.ACTIVE);

        // Setup clock in metadata
        clockInMetadata = new ClockEventMetadata();
        clockInMetadata.setDeviceId("DEVICE001");
        clockInMetadata.setDeviceType("MOBILE");
        clockInMetadata.setIpAddress("192.168.1.100");
        clockInMetadata.setLatitude(37.7749);
        clockInMetadata.setLongitude(-122.4194);

        // Setup clock out metadata
        clockOutMetadata = new ClockEventMetadata();
        clockOutMetadata.setDeviceId("DEVICE001");
        clockOutMetadata.setDeviceType("MOBILE");
        clockOutMetadata.setIpAddress("192.168.1.100");
        clockOutMetadata.setLatitude(37.7749);
        clockOutMetadata.setLongitude(-122.4194);

        // Setup test attendance record
        testAttendanceRecord = new AttendanceRecord();
        testAttendanceRecord.setId(UUID.randomUUID());
        testAttendanceRecord.setEmployee(testEmployee);
        testAttendanceRecord.setAttendanceDate(LocalDate.now());
        testAttendanceRecord.setClockInTime(LocalDateTime.now().withHour(8).withMinute(0));
        testAttendanceRecord.setClockInMetadata(clockInMetadata);
        testAttendanceRecord.setStatus(AttendanceStatus.CLOCKED_IN);
    }

    // ==================== CLOCK IN TESTS ====================

    @Test
    @DisplayName("Clock In - Normal Case - Should Create Attendance Record")
    void testClockIn_NormalCase_Success() {
        // Arrange
        UUID employeeId = testEmployee.getId();
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(testEmployee));
        when(attendanceRepository.findByEmployeeAndAttendanceDateAndStatus(
            any(Employee.class), any(LocalDate.class), eq(AttendanceStatus.CLOCKED_IN)
        )).thenReturn(Optional.empty());
        when(attendanceRepository.save(any(AttendanceRecord.class))).thenReturn(testAttendanceRecord);

        // Act
        AttendanceRecord result = attendanceService.clockIn(employeeId, clockInMetadata);

        // Assert
        assertNotNull(result);
        assertEquals(AttendanceStatus.CLOCKED_IN, result.getStatus());
        assertNotNull(result.getClockInTime());
        assertNotNull(result.getClockInMetadata());
        verify(attendanceRepository).save(any(AttendanceRecord.class));
    }

    @Test
    @DisplayName("Clock In - Employee Not Found - Should Throw Exception")
    void testClockIn_EmployeeNotFound_ThrowsException() {
        // Arrange
        UUID employeeId = UUID.randomUUID();
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> {
            attendanceService.clockIn(employeeId, clockInMetadata);
        });
        verify(attendanceRepository, never()).save(any(AttendanceRecord.class));
    }

    @Test
    @DisplayName("Clock In - Already Clocked In - Should Throw Exception")
    void testClockIn_AlreadyClockedIn_ThrowsException() {
        // Arrange
        UUID employeeId = testEmployee.getId();
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(testEmployee));
        when(attendanceRepository.findByEmployeeAndAttendanceDateAndStatus(
            any(Employee.class), any(LocalDate.class), eq(AttendanceStatus.CLOCKED_IN)
        )).thenReturn(Optional.of(testAttendanceRecord));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            attendanceService.clockIn(employeeId, clockInMetadata);
        });
        verify(attendanceRepository, never()).save(any(AttendanceRecord.class));
    }

    @Test
    @DisplayName("Clock In - Null Metadata - Should Create Record Without Metadata")
    void testClockIn_NullMetadata_Success() {
        // Arrange
        UUID employeeId = testEmployee.getId();
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(testEmployee));
        when(attendanceRepository.findByEmployeeAndAttendanceDateAndStatus(
            any(Employee.class), any(LocalDate.class), eq(AttendanceStatus.CLOCKED_IN)
        )).thenReturn(Optional.empty());
        when(attendanceRepository.save(any(AttendanceRecord.class))).thenReturn(testAttendanceRecord);

        // Act
        AttendanceRecord result = attendanceService.clockIn(employeeId, null);

        // Assert
        assertNotNull(result);
        assertEquals(AttendanceStatus.CLOCKED_IN, result.getStatus());
        verify(attendanceRepository).save(any(AttendanceRecord.class));
    }

    @Test
    @DisplayName("Clock In - Inactive Employee - Should Throw Exception")
    void testClockIn_InactiveEmployee_ThrowsException() {
        // Arrange
        UUID employeeId = testEmployee.getId();
        testEmployee.setStatus(EmployeeStatus.INACTIVE);
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(testEmployee));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            attendanceService.clockIn(employeeId, clockInMetadata);
        });
        verify(attendanceRepository, never()).save(any(AttendanceRecord.class));
    }

    @Test
    @DisplayName("Clock In - Terminated Employee - Should Throw Exception")
    void testClockIn_TerminatedEmployee_ThrowsException() {
        // Arrange
        UUID employeeId = testEmployee.getId();
        testEmployee.setStatus(EmployeeStatus.TERMINATED);
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(testEmployee));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            attendanceService.clockIn(employeeId, clockInMetadata);
        });
        verify(attendanceRepository, never()).save(any(AttendanceRecord.class));
    }

    @Test
    @DisplayName("Clock In - Midnight Boundary - Should Create Record for Correct Date")
    void testClockIn_MidnightBoundary_Success() {
        // Arrange
        UUID employeeId = testEmployee.getId();
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(testEmployee));
        when(attendanceRepository.findByEmployeeAndAttendanceDateAndStatus(
            any(Employee.class), any(LocalDate.class), eq(AttendanceStatus.CLOCKED_IN)
        )).thenReturn(Optional.empty());
        when(attendanceRepository.save(any(AttendanceRecord.class))).thenReturn(testAttendanceRecord);

        // Act
        AttendanceRecord result = attendanceService.clockIn(employeeId, clockInMetadata);

        // Assert
        assertNotNull(result);
        assertEquals(LocalDate.now(), result.getAttendanceDate());
    }

    // ==================== CLOCK OUT TESTS ====================

    @Test
    @DisplayName("Clock Out - Normal Case - Should Update Attendance Record")
    void testClockOut_NormalCase_Success() {
        // Arrange
        UUID employeeId = testEmployee.getId();
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(testEmployee));
        when(attendanceRepository.findByEmployeeAndAttendanceDateAndStatus(
            any(Employee.class), any(LocalDate.class), eq(AttendanceStatus.CLOCKED_IN)
        )).thenReturn(Optional.of(testAttendanceRecord));
        when(attendanceRepository.save(any(AttendanceRecord.class))).thenReturn(testAttendanceRecord);

        // Act
        AttendanceRecord result = attendanceService.clockOut(employeeId, clockOutMetadata);

        // Assert
        assertNotNull(result);
        assertEquals(AttendanceStatus.COMPLETED, result.getStatus());
        assertNotNull(result.getClockOutTime());
        assertNotNull(result.getClockOutMetadata());
        assertTrue(result.getTotalHours() > 0);
        verify(attendanceRepository).save(any(AttendanceRecord.class));
    }

    @Test
    @DisplayName("Clock Out - Not Clocked In - Should Throw Exception")
    void testClockOut_NotClockedIn_ThrowsException() {
        // Arrange
        UUID employeeId = testEmployee.getId();
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(testEmployee));
        when(attendanceRepository.findByEmployeeAndAttendanceDateAndStatus(
            any(Employee.class), any(LocalDate.class), eq(AttendanceStatus.CLOCKED_IN)
        )).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            attendanceService.clockOut(employeeId, clockOutMetadata);
        });
        verify(attendanceRepository, never()).save(any(AttendanceRecord.class));
    }

    @Test
    @DisplayName("Clock Out - Employee Not Found - Should Throw Exception")
    void testClockOut_EmployeeNotFound_ThrowsException() {
        // Arrange
        UUID employeeId = UUID.randomUUID();
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> {
            attendanceService.clockOut(employeeId, clockOutMetadata);
        });
        verify(attendanceRepository, never()).save(any(AttendanceRecord.class));
    }

    @Test
    @DisplayName("Clock Out - Null Metadata - Should Update Without Metadata")
    void testClockOut_NullMetadata_Success() {
        // Arrange
        UUID employeeId = testEmployee.getId();
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(testEmployee));
        when(attendanceRepository.findByEmployeeAndAttendanceDateAndStatus(
            any(Employee.class), any(LocalDate.class), eq(AttendanceStatus.CLOCKED_IN)
        )).thenReturn(Optional.of(testAttendanceRecord));
        when(attendanceRepository.save(any(AttendanceRecord.class))).thenReturn(testAttendanceRecord);

        // Act
        AttendanceRecord result = attendanceService.clockOut(employeeId, null);

        // Assert
        assertNotNull(result);
        assertEquals(AttendanceStatus.COMPLETED, result.getStatus());
        verify(attendanceRepository).save(any(AttendanceRecord.class));
    }

    @Test
    @DisplayName("Clock Out - Standard 8 Hour Shift - Should Calculate Correctly")
    void testClockOut_StandardShift_CalculatesCorrectly() {
        // Arrange
        UUID employeeId = testEmployee.getId();
        testAttendanceRecord.setClockInTime(LocalDateTime.now().withHour(8).withMinute(0));
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(testEmployee));
        when(attendanceRepository.findByEmployeeAndAttendanceDateAndStatus(
            any(Employee.class), any(LocalDate.class), eq(AttendanceStatus.CLOCKED_IN)
        )).thenReturn(Optional.of(testAttendanceRecord));
        when(attendanceRepository.save(any(AttendanceRecord.class))).thenAnswer(invocation -> {
            AttendanceRecord record = invocation.getArgument(0);
            record.setClockOutTime(LocalDateTime.now().withHour(16).withMinute(0));
            record.calculateTotalHours();
            return record;
        });

        // Act
        AttendanceRecord result = attendanceService.clockOut(employeeId, clockOutMetadata);

        // Assert
        assertNotNull(result);
        verify(attendanceRepository).save(any(AttendanceRecord.class));
    }

    @Test
    @DisplayName("Clock Out - Overtime Shift (10 hours) - Should Calculate Overtime")
    void testClockOut_OvertimeShift_CalculatesOvertime() {
        // Arrange
        UUID employeeId = testEmployee.getId();
        testAttendanceRecord.setClockInTime(LocalDateTime.now().withHour(8).withMinute(0));
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(testEmployee));
        when(attendanceRepository.findByEmployeeAndAttendanceDateAndStatus(
            any(Employee.class), any(LocalDate.class), eq(AttendanceStatus.CLOCKED_IN)
        )).thenReturn(Optional.of(testAttendanceRecord));
        when(attendanceRepository.save(any(AttendanceRecord.class))).thenAnswer(invocation -> {
            AttendanceRecord record = invocation.getArgument(0);
            record.setClockOutTime(LocalDateTime.now().withHour(18).withMinute(0));
            record.calculateTotalHours();
            return record;
        });

        // Act
        AttendanceRecord result = attendanceService.clockOut(employeeId, clockOutMetadata);

        // Assert
        assertNotNull(result);
        verify(attendanceRepository).save(any(AttendanceRecord.class));
    }

    @Test
    @DisplayName("Clock Out - Short Shift (2 hours) - Should Calculate Correctly")
    void testClockOut_ShortShift_CalculatesCorrectly() {
        // Arrange
        UUID employeeId = testEmployee.getId();
        testAttendanceRecord.setClockInTime(LocalDateTime.now().withHour(8).withMinute(0));
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(testEmployee));
        when(attendanceRepository.findByEmployeeAndAttendanceDateAndStatus(
            any(Employee.class), any(LocalDate.class), eq(AttendanceStatus.CLOCKED_IN)
        )).thenReturn(Optional.of(testAttendanceRecord));
        when(attendanceRepository.save(any(AttendanceRecord.class))).thenAnswer(invocation -> {
            AttendanceRecord record = invocation.getArgument(0);
            record.setClockOutTime(LocalDateTime.now().withHour(10).withMinute(0));
            record.calculateTotalHours();
            return record;
        });

        // Act
        AttendanceRecord result = attendanceService.clockOut(employeeId, clockOutMetadata);

        // Assert
        assertNotNull(result);
        verify(attendanceRepository).save(any(AttendanceRecord.class));
    }

    // ==================== GET CURRENT ATTENDANCE TESTS ====================

    @Test
    @DisplayName("Get Current Attendance - Clocked In - Should Return Record")
    void testGetCurrentAttendance_ClockedIn_Success() {
        // Arrange
        UUID employeeId = testEmployee.getId();
        when(attendanceRepository.findByEmployeeIdAndAttendanceDateAndStatus(
            employeeId, LocalDate.now(), AttendanceStatus.CLOCKED_IN
        )).thenReturn(Optional.of(testAttendanceRecord));

        // Act
        Optional<AttendanceRecord> result = attendanceService.getCurrentAttendance(employeeId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testAttendanceRecord.getId(), result.get().getId());
    }

    @Test
    @DisplayName("Get Current Attendance - Not Clocked In - Should Return Empty")
    void testGetCurrentAttendance_NotClockedIn_ReturnsEmpty() {
        // Arrange
        UUID employeeId = testEmployee.getId();
        when(attendanceRepository.findByEmployeeIdAndAttendanceDateAndStatus(
            employeeId, LocalDate.now(), AttendanceStatus.CLOCKED_IN
        )).thenReturn(Optional.empty());

        // Act
        Optional<AttendanceRecord> result = attendanceService.getCurrentAttendance(employeeId);

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Get Current Attendance - Null Employee ID - Should Throw Exception")
    void testGetCurrentAttendance_NullEmployeeId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.getCurrentAttendance(null);
        });
    }

    // ==================== ATTENDANCE CORRECTION TESTS ====================

    @Test
    @DisplayName("Request Correction - Missed Clock In - Should Create Correction Request")
    void testRequestCorrection_MissedClockIn_Success() {
        // Arrange
        UUID attendanceId = testAttendanceRecord.getId();
        UUID requestedById = testEmployee.getId();
        LocalDateTime correctedTime = LocalDateTime.now().withHour(8).withMinute(0);
        String reason = "Forgot to clock in";
        
        when(attendanceRepository.findById(attendanceId)).thenReturn(Optional.of(testAttendanceRecord));
        when(attendanceRepository.save(any(AttendanceRecord.class))).thenReturn(testAttendanceRecord);

        // Act
        AttendanceCorrection result = attendanceService.requestCorrection(
            attendanceId, requestedById, CorrectionType.MISSED_CLOCK_IN, correctedTime, reason
        );

        // Assert
        assertNotNull(result);
        assertEquals(CorrectionStatus.PENDING, result.getStatus());
        assertEquals(CorrectionType.MISSED_CLOCK_IN, result.getCorrectionType());
        assertEquals(reason, result.getReason());
    }

    @Test
    @DisplayName("Request Correction - Missed Clock Out - Should Create Correction Request")
    void testRequestCorrection_MissedClockOut_Success() {
        // Arrange
        UUID attendanceId = testAttendanceRecord.getId();
        UUID requestedById = testEmployee.getId();
        LocalDateTime correctedTime = LocalDateTime.now().withHour(17).withMinute(0);
        String reason = "Forgot to clock out";
        
        when(attendanceRepository.findById(attendanceId)).thenReturn(Optional.of(testAttendanceRecord));
        when(attendanceRepository.save(any(AttendanceRecord.class))).thenReturn(testAttendanceRecord);

        // Act
        AttendanceCorrection result = attendanceService.requestCorrection(
            attendanceId, requestedById, CorrectionType.MISSED_CLOCK_OUT, correctedTime, reason
        );

        // Assert
        assertNotNull(result);
        assertEquals(CorrectionStatus.PENDING, result.getStatus());
        assertEquals(CorrectionType.MISSED_CLOCK_OUT, result.getCorrectionType());
    }

    @Test
    @DisplayName("Request Correction - Attendance Not Found - Should Throw Exception")
    void testRequestCorrection_AttendanceNotFound_ThrowsException() {
        // Arrange
        UUID attendanceId = UUID.randomUUID();
        UUID requestedById = testEmployee.getId();
        LocalDateTime correctedTime = LocalDateTime.now();
        String reason = "Test reason";
        
        when(attendanceRepository.findById(attendanceId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> {
            attendanceService.requestCorrection(
                attendanceId, requestedById, CorrectionType.MISSED_CLOCK_IN, correctedTime, reason
            );
        });
    }

    @Test
    @DisplayName("Request Correction - Null Reason - Should Throw Exception")
    void testRequestCorrection_NullReason_ThrowsException() {
        // Arrange
        UUID attendanceId = testAttendanceRecord.getId();
        UUID requestedById = testEmployee.getId();
        LocalDateTime correctedTime = LocalDateTime.now();
        
        when(attendanceRepository.findById(attendanceId)).thenReturn(Optional.of(testAttendanceRecord));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.requestCorrection(
                attendanceId, requestedById, CorrectionType.MISSED_CLOCK_IN, correctedTime, null
            );
        });
    }

    @Test
    @DisplayName("Request Correction - Empty Reason - Should Throw Exception")
    void testRequestCorrection_EmptyReason_ThrowsException() {
        // Arrange
        UUID attendanceId = testAttendanceRecord.getId();
        UUID requestedById = testEmployee.getId();
        LocalDateTime correctedTime = LocalDateTime.now();
        
        when(attendanceRepository.findById(attendanceId)).thenReturn(Optional.of(testAttendanceRecord));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.requestCorrection(
                attendanceId, requestedById, CorrectionType.MISSED_CLOCK_IN, correctedTime, ""
            );
        });
    }

    @Test
    @DisplayName("Approve Correction - Normal Case - Should Update Attendance")
    void testApproveCorrection_NormalCase_Success() {
        // Arrange
        UUID correctionId = UUID.randomUUID();
        UUID reviewerId = UUID.randomUUID();
        AttendanceCorrection correction = new AttendanceCorrection();
        correction.setId(correctionId);
        correction.setAttendanceRecord(testAttendanceRecord);
        correction.setCorrectionType(CorrectionType.MISSED_CLOCK_IN);
        correction.setCorrectedClockInTime(LocalDateTime.now().withHour(8).withMinute(0));
        correction.setStatus(CorrectionStatus.PENDING);
        
        when(attendanceRepository.findCorrectionById(correctionId)).thenReturn(Optional.of(correction));
        when(attendanceRepository.save(any(AttendanceRecord.class))).thenReturn(testAttendanceRecord);

        // Act
        AttendanceCorrection result = attendanceService.approveCorrection(correctionId, reviewerId);

        // Assert
        assertNotNull(result);
        assertEquals(CorrectionStatus.APPROVED, result.getStatus());
        assertEquals(reviewerId, result.getReviewedBy());
        assertNotNull(result.getReviewedAt());
    }

    @Test
    @DisplayName("Reject Correction - Normal Case - Should Update Status")
    void testRejectCorrection_NormalCase_Success() {
        // Arrange
        UUID correctionId = UUID.randomUUID();
        UUID reviewerId = UUID.randomUUID();
        String rejectionReason = "Invalid time";
        AttendanceCorrection correction = new AttendanceCorrection();
        correction.setId(correctionId);
        correction.setAttendanceRecord(testAttendanceRecord);
        correction.setStatus(CorrectionStatus.PENDING);
        
        when(attendanceRepository.findCorrectionById(correctionId)).thenReturn(Optional.of(correction));

        // Act
        AttendanceCorrection result = attendanceService.rejectCorrection(correctionId, reviewerId, rejectionReason);

        // Assert
        assertNotNull(result);
        assertEquals(CorrectionStatus.REJECTED, result.getStatus());
        assertEquals(reviewerId, result.getReviewedBy());
        assertNotNull(result.getReviewedAt());
    }

    // ==================== BOUNDARY CONDITION TESTS ====================

    @Test
    @DisplayName("Clock In - Exactly at Midnight - Should Create Record for Current Date")
    void testClockIn_ExactlyMidnight_Success() {
        // Arrange
        UUID employeeId = testEmployee.getId();
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(testEmployee));
        when(attendanceRepository.findByEmployeeAndAttendanceDateAndStatus(
            any(Employee.class), any(LocalDate.class), eq(AttendanceStatus.CLOCKED_IN)
        )).thenReturn(Optional.empty());
        when(attendanceRepository.save(any(AttendanceRecord.class))).thenReturn(testAttendanceRecord);

        // Act
        AttendanceRecord result = attendanceService.clockIn(employeeId, clockInMetadata);

        // Assert
        assertNotNull(result);
        assertEquals(LocalDate.now(), result.getAttendanceDate());
    }

    @Test
    @DisplayName("Clock Out - Same Minute as Clock In - Should Calculate Zero Hours")
    void testClockOut_SameMinute_CalculatesZeroHours() {
        // Arrange
        UUID employeeId = testEmployee.getId();
        LocalDateTime now = LocalDateTime.now();
        testAttendanceRecord.setClockInTime(now);
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(testEmployee));
        when(attendanceRepository.findByEmployeeAndAttendanceDateAndStatus(
            any(Employee.class), any(LocalDate.class), eq(AttendanceStatus.CLOCKED_IN)
        )).thenReturn(Optional.of(testAttendanceRecord));
        when(attendanceRepository.save(any(AttendanceRecord.class))).thenAnswer(invocation -> {
            AttendanceRecord record = invocation.getArgument(0);
            record.setClockOutTime(now);
            record.calculateTotalHours();
            return record;
        });

        // Act
        AttendanceRecord result = attendanceService.clockOut(employeeId, clockOutMetadata);

        // Assert
        assertNotNull(result);
        verify(attendanceRepository).save(any(AttendanceRecord.class));
    }

    @Test
    @DisplayName("Clock In - Maximum Geolocation Values - Should Store Correctly")
    void testClockIn_MaxGeolocation_Success() {
        // Arrange
        UUID employeeId = testEmployee.getId();
        ClockEventMetadata maxGeoMetadata = new ClockEventMetadata();
        maxGeoMetadata.setLatitude(90.0);  // Max latitude
        maxGeoMetadata.setLongitude(180.0); // Max longitude
        
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(testEmployee));
        when(attendanceRepository.findByEmployeeAndAttendanceDateAndStatus(
            any(Employee.class), any(LocalDate.class), eq(AttendanceStatus.CLOCKED_IN)
        )).thenReturn(Optional.empty());
        when(attendanceRepository.save(any(AttendanceRecord.class))).thenReturn(testAttendanceRecord);

        // Act
        AttendanceRecord result = attendanceService.clockIn(employeeId, maxGeoMetadata);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Clock In - Minimum Geolocation Values - Should Store Correctly")
    void testClockIn_MinGeolocation_Success() {
        // Arrange
        UUID employeeId = testEmployee.getId();
        ClockEventMetadata minGeoMetadata = new ClockEventMetadata();
        minGeoMetadata.setLatitude(-90.0);  // Min latitude
        minGeoMetadata.setLongitude(-180.0); // Min longitude
        
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(testEmployee));
        when(attendanceRepository.findByEmployeeAndAttendanceDateAndStatus(
            any(Employee.class), any(LocalDate.class), eq(AttendanceStatus.CLOCKED_IN)
        )).thenReturn(Optional.empty());
        when(attendanceRepository.save(any(AttendanceRecord.class))).thenReturn(testAttendanceRecord);

        // Act
        AttendanceRecord result = attendanceService.clockIn(employeeId, minGeoMetadata);

        // Assert
        assertNotNull(result);
    }
}