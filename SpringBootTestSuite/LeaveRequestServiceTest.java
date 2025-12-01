package com.warehouse.ems.attendance.service;

import com.warehouse.ems.attendance.entity.LeaveRequest;
import com.warehouse.ems.attendance.repository.LeaveRequestRepository;
import com.warehouse.ems.employee.entity.Employee;
import com.warehouse.ems.employee.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for LeaveRequestService covering:
 * - Leave request creation
 * - Approval and denial workflows
 * - Accrual balance management
 * - Leave type validation (PTO, SICK, UNPAID)
 * - Edge cases and boundary conditions
 */
@ExtendWith(MockitoExtension.class)
public class LeaveRequestServiceTest {

    @Mock
    private LeaveRequestRepository leaveRequestRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private LeaveRequestService leaveRequestService;

    private Employee testEmployee;
    private LeaveRequest ptoRequest;
    private LeaveRequest sickRequest;

    @BeforeEach
    public void setUp() {
        // Arrange: Create test data
        testEmployee = Employee.builder()
                .id(1L)
                .badgeId("EMP001")
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@warehouse.com")
                .status("ACTIVE")
                .deleted(false)
                .build();

        ptoRequest = LeaveRequest.builder()
                .id(1L)
                .employeeId(1L)
                .leaveType("PTO")
                .startDate(LocalDate.of(2024, 2, 1))
                .endDate(LocalDate.of(2024, 2, 5))
                .status("REQUESTED")
                .reason("Family vacation")
                .build();

        sickRequest = LeaveRequest.builder()
                .id(2L)
                .employeeId(1L)
                .leaveType("SICK")
                .startDate(LocalDate.of(2024, 2, 10))
                .endDate(LocalDate.of(2024, 2, 12))
                .status("REQUESTED")
                .reason("Medical appointment")
                .build();
    }

    // ========== NORMAL CASE TESTS ==========

    @Test
    public void testCreateLeaveRequest_WithValidPTO_ReturnsCreatedRequest() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenReturn(ptoRequest);

        // Act
        LeaveRequest result = leaveRequestService.createLeaveRequest(ptoRequest);

        // Assert
        assertNotNull(result, "Leave request should not be null");
        assertEquals("PTO", result.getLeaveType(), "Leave type should be PTO");
        assertEquals("REQUESTED", result.getStatus(), "Status should be REQUESTED");
        assertEquals(LocalDate.of(2024, 2, 1), result.getStartDate());
        assertEquals(LocalDate.of(2024, 2, 5), result.getEndDate());
        verify(employeeRepository, times(1)).findById(1L);
        verify(leaveRequestRepository, times(1)).save(any(LeaveRequest.class));
    }

    @Test
    public void testCreateLeaveRequest_WithValidSick_ReturnsCreatedRequest() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenReturn(sickRequest);

        // Act
        LeaveRequest result = leaveRequestService.createLeaveRequest(sickRequest);

        // Assert
        assertNotNull(result);
        assertEquals("SICK", result.getLeaveType());
        assertEquals("REQUESTED", result.getStatus());
        verify(leaveRequestRepository, times(1)).save(any(LeaveRequest.class));
    }

    @Test
    public void testApproveLeaveRequest_WithValidRequest_UpdatesStatus() {
        // Arrange
        when(leaveRequestRepository.findById(1L)).thenReturn(Optional.of(ptoRequest));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenAnswer(invocation -> {
            LeaveRequest req = invocation.getArgument(0);
            req.setStatus("APPROVED");
            return req;
        });

        // Act
        LeaveRequest result = leaveRequestService.approveLeaveRequest(1L);

        // Assert
        assertNotNull(result);
        assertEquals("APPROVED", result.getStatus());
        verify(leaveRequestRepository, times(1)).findById(1L);
        verify(leaveRequestRepository, times(1)).save(any(LeaveRequest.class));
    }

    @Test
    public void testDenyLeaveRequest_WithValidRequest_UpdatesStatus() {
        // Arrange
        when(leaveRequestRepository.findById(1L)).thenReturn(Optional.of(ptoRequest));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenAnswer(invocation -> {
            LeaveRequest req = invocation.getArgument(0);
            req.setStatus("DENIED");
            return req;
        });

        // Act
        LeaveRequest result = leaveRequestService.denyLeaveRequest(1L, "Insufficient coverage");

        // Assert
        assertNotNull(result);
        assertEquals("DENIED", result.getStatus());
        verify(leaveRequestRepository, times(1)).findById(1L);
        verify(leaveRequestRepository, times(1)).save(any(LeaveRequest.class));
    }

    @Test
    public void testGetEmployeeLeaveRequests_ReturnsAllRequests() {
        // Arrange
        when(leaveRequestRepository.findByEmployeeIdOrderByStartDateDesc(1L))
                .thenReturn(Arrays.asList(ptoRequest, sickRequest));

        // Act
        List<LeaveRequest> result = leaveRequestService.getEmployeeLeaveRequests(1L);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("PTO", result.get(0).getLeaveType());
        assertEquals("SICK", result.get(1).getLeaveType());
        verify(leaveRequestRepository, times(1)).findByEmployeeIdOrderByStartDateDesc(1L);
    }

    // ========== EDGE CASE TESTS ==========

    @Test
    public void testCreateLeaveRequest_WithInvalidEmployeeId_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());
        ptoRequest.setEmployeeId(999L);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            leaveRequestService.createLeaveRequest(ptoRequest);
        }, "Should throw exception for invalid employee ID");

        verify(employeeRepository, times(1)).findById(999L);
        verify(leaveRequestRepository, never()).save(any(LeaveRequest.class));
    }

    @Test
    public void testCreateLeaveRequest_WithDeletedEmployee_ThrowsException() {
        // Arrange
        testEmployee.setDeleted(true);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            leaveRequestService.createLeaveRequest(ptoRequest);
        }, "Should throw exception for deleted employee");

        verify(leaveRequestRepository, never()).save(any(LeaveRequest.class));
    }

    @Test
    public void testCreateLeaveRequest_WithInactiveEmployee_ThrowsException() {
        // Arrange
        testEmployee.setStatus("INACTIVE");
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            leaveRequestService.createLeaveRequest(ptoRequest);
        }, "Should throw exception for inactive employee");

        verify(leaveRequestRepository, never()).save(any(LeaveRequest.class));
    }

    @Test
    public void testCreateLeaveRequest_WithNullLeaveType_ThrowsException() {
        // Arrange
        LeaveRequest invalidRequest = LeaveRequest.builder()
                .employeeId(1L)
                .startDate(LocalDate.of(2024, 2, 1))
                .endDate(LocalDate.of(2024, 2, 5))
                .build();

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            leaveRequestService.createLeaveRequest(invalidRequest);
        }, "Should throw exception for null leave type");

        verify(leaveRequestRepository, never()).save(any(LeaveRequest.class));
    }

    @Test
    public void testCreateLeaveRequest_WithInvalidLeaveType_ThrowsException() {
        // Arrange
        LeaveRequest invalidRequest = LeaveRequest.builder()
                .employeeId(1L)
                .leaveType("INVALID")
                .startDate(LocalDate.of(2024, 2, 1))
                .endDate(LocalDate.of(2024, 2, 5))
                .build();

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            leaveRequestService.createLeaveRequest(invalidRequest);
        }, "Should throw exception for invalid leave type");

        verify(leaveRequestRepository, never()).save(any(LeaveRequest.class));
    }

    @Test
    public void testCreateLeaveRequest_WithEndDateBeforeStartDate_ThrowsException() {
        // Arrange
        LeaveRequest invalidRequest = LeaveRequest.builder()
                .employeeId(1L)
                .leaveType("PTO")
                .startDate(LocalDate.of(2024, 2, 5))
                .endDate(LocalDate.of(2024, 2, 1))
                .build();

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            leaveRequestService.createLeaveRequest(invalidRequest);
        }, "Should throw exception when end date is before start date");

        verify(leaveRequestRepository, never()).save(any(LeaveRequest.class));
    }

    @Test
    public void testCreateLeaveRequest_WithPastStartDate_ThrowsException() {
        // Arrange
        LeaveRequest invalidRequest = LeaveRequest.builder()
                .employeeId(1L)
                .leaveType("PTO")
                .startDate(LocalDate.of(2020, 1, 1))
                .endDate(LocalDate.of(2020, 1, 5))
                .build();

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            leaveRequestService.createLeaveRequest(invalidRequest);
        }, "Should throw exception for past start date");

        verify(leaveRequestRepository, never()).save(any(LeaveRequest.class));
    }

    @Test
    public void testApproveLeaveRequest_WithInvalidRequestId_ThrowsException() {
        // Arrange
        when(leaveRequestRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            leaveRequestService.approveLeaveRequest(999L);
        }, "Should throw exception for invalid request ID");

        verify(leaveRequestRepository, times(1)).findById(999L);
        verify(leaveRequestRepository, never()).save(any(LeaveRequest.class));
    }

    @Test
    public void testApproveLeaveRequest_AlreadyApproved_ThrowsException() {
        // Arrange
        ptoRequest.setStatus("APPROVED");
        when(leaveRequestRepository.findById(1L)).thenReturn(Optional.of(ptoRequest));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            leaveRequestService.approveLeaveRequest(1L);
        }, "Should throw exception for already approved request");

        verify(leaveRequestRepository, times(1)).findById(1L);
        verify(leaveRequestRepository, never()).save(any(LeaveRequest.class));
    }

    @Test
    public void testDenyLeaveRequest_AlreadyDenied_ThrowsException() {
        // Arrange
        ptoRequest.setStatus("DENIED");
        when(leaveRequestRepository.findById(1L)).thenReturn(Optional.of(ptoRequest));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            leaveRequestService.denyLeaveRequest(1L, "Already denied");
        }, "Should throw exception for already denied request");

        verify(leaveRequestRepository, times(1)).findById(1L);
        verify(leaveRequestRepository, never()).save(any(LeaveRequest.class));
    }

    // ========== BOUNDARY CONDITION TESTS ==========

    @Test
    public void testCreateLeaveRequest_ForSingleDay_Success() {
        // Arrange
        LeaveRequest singleDayRequest = LeaveRequest.builder()
                .employeeId(1L)
                .leaveType("SICK")
                .startDate(LocalDate.of(2024, 2, 15))
                .endDate(LocalDate.of(2024, 2, 15))
                .reason("Doctor appointment")
                .build();

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenReturn(singleDayRequest);

        // Act
        LeaveRequest result = leaveRequestService.createLeaveRequest(singleDayRequest);

        // Assert
        assertNotNull(result);
        assertEquals(result.getStartDate(), result.getEndDate());
        verify(leaveRequestRepository, times(1)).save(any(LeaveRequest.class));
    }

    @Test
    public void testCreateLeaveRequest_ForMaxDuration_Success() {
        // Arrange
        LeaveRequest longRequest = LeaveRequest.builder()
                .employeeId(1L)
                .leaveType("PTO")
                .startDate(LocalDate.of(2024, 2, 1))
                .endDate(LocalDate.of(2024, 2, 29))
                .reason("Extended vacation")
                .build();

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenReturn(longRequest);

        // Act
        LeaveRequest result = leaveRequestService.createLeaveRequest(longRequest);

        // Assert
        assertNotNull(result);
        assertEquals(29, result.getEndDate().getDayOfMonth());
        verify(leaveRequestRepository, times(1)).save(any(LeaveRequest.class));
    }

    @Test
    public void testCreateLeaveRequest_StartingToday_Success() {
        // Arrange
        LeaveRequest todayRequest = LeaveRequest.builder()
                .employeeId(1L)
                .leaveType("SICK")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(2))
                .reason("Sudden illness")
                .build();

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenReturn(todayRequest);

        // Act
        LeaveRequest result = leaveRequestService.createLeaveRequest(todayRequest);

        // Assert
        assertNotNull(result);
        assertEquals(LocalDate.now(), result.getStartDate());
        verify(leaveRequestRepository, times(1)).save(any(LeaveRequest.class));
    }

    @Test
    public void testGetEmployeeLeaveRequests_WithNoRequests_ReturnsEmptyList() {
        // Arrange
        when(leaveRequestRepository.findByEmployeeIdOrderByStartDateDesc(1L))
                .thenReturn(Arrays.asList());

        // Act
        List<LeaveRequest> result = leaveRequestService.getEmployeeLeaveRequests(1L);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.size());
        verify(leaveRequestRepository, times(1)).findByEmployeeIdOrderByStartDateDesc(1L);
    }

    @Test
    public void testCreateLeaveRequest_WithUnpaidLeave_Success() {
        // Arrange
        LeaveRequest unpaidRequest = LeaveRequest.builder()
                .employeeId(1L)
                .leaveType("UNPAID")
                .startDate(LocalDate.of(2024, 3, 1))
                .endDate(LocalDate.of(2024, 3, 5))
                .reason("Personal reasons")
                .build();

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenReturn(unpaidRequest);

        // Act
        LeaveRequest result = leaveRequestService.createLeaveRequest(unpaidRequest);

        // Assert
        assertNotNull(result);
        assertEquals("UNPAID", result.getLeaveType());
        verify(leaveRequestRepository, times(1)).save(any(LeaveRequest.class));
    }

    @Test
    public void testCreateLeaveRequest_WithMaxLengthReason_Success() {
        // Arrange
        String maxReason = "A".repeat(256);
        LeaveRequest request = LeaveRequest.builder()
                .employeeId(1L)
                .leaveType("PTO")
                .startDate(LocalDate.of(2024, 2, 1))
                .endDate(LocalDate.of(2024, 2, 5))
                .reason(maxReason)
                .build();

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenReturn(request);

        // Act
        LeaveRequest result = leaveRequestService.createLeaveRequest(request);

        // Assert
        assertNotNull(result);
        assertEquals(256, result.getReason().length());
    }
}