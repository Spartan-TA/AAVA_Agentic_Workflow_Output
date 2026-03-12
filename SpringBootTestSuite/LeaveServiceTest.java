package com.wms.leave.service;

import com.wms.leave.domain.LeaveRequest;
import com.wms.leave.domain.LeaveType;
import com.wms.leave.domain.LeaveStatus;
import com.wms.leave.dto.LeaveRequestDto;
import com.wms.leave.repository.LeaveRepository;
import com.wms.employee.domain.Employee;
import com.wms.employee.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive JUnit test suite for LeaveService
 * Tests cover leave requests, approvals, balance management, and edge cases
 */
@DisplayName("Leave Service Tests")
public class LeaveServiceTest {

    @Mock
    private LeaveRepository leaveRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private LeaveServiceImpl leaveService;

    private Employee testEmployee;
    private Employee testApprover;
    private LeaveRequest testLeaveRequest;
    private LeaveRequestDto leaveRequestDto;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        // Setup test employee
        testEmployee = new Employee();
        testEmployee.setId(1L);
        testEmployee.setBadgeId("EMP001");
        testEmployee.setName("John Doe");

        // Setup test approver
        testApprover = new Employee();
        testApprover.setId(2L);
        testApprover.setBadgeId("EMP002");
        testApprover.setName("Jane Manager");

        // Setup test leave request
        testLeaveRequest = new LeaveRequest();
        testLeaveRequest.setId(1L);
        testLeaveRequest.setEmployee(testEmployee);
        testLeaveRequest.setType(LeaveType.PTO);
        testLeaveRequest.setStartDate(LocalDate.now().plusDays(7));
        testLeaveRequest.setEndDate(LocalDate.now().plusDays(10));
        testLeaveRequest.setStatus(LeaveStatus.REQUESTED);
        testLeaveRequest.setAccrualBalance(80.0);

        // Setup DTO
        leaveRequestDto = new LeaveRequestDto();
        leaveRequestDto.setEmployeeId(1L);
        leaveRequestDto.setType("PTO");
        leaveRequestDto.setStartDate(LocalDate.now().plusDays(7));
        leaveRequestDto.setEndDate(LocalDate.now().plusDays(10));
    }

    // ========== REQUEST LEAVE TESTS ==========

    @Test
    @DisplayName("Test request leave with valid data")
    public void testRequestLeave_ValidData_Success() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(leaveRepository.findOverlappingLeaves(anyLong(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Arrays.asList());
        when(leaveRepository.save(any(LeaveRequest.class))).thenReturn(testLeaveRequest);

        // Act
        LeaveRequestDto result = leaveService.requestLeave(leaveRequestDto);

        // Assert
        assertNotNull(result);
        assertEquals(LeaveStatus.REQUESTED.name(), result.getStatus());
        verify(leaveRepository, times(1)).save(any(LeaveRequest.class));
    }

    @Test
    @DisplayName("Test request leave with null employee ID throws exception")
    public void testRequestLeave_NullEmployeeId_ThrowsException() {
        // Arrange
        leaveRequestDto.setEmployeeId(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            leaveService.requestLeave(leaveRequestDto);
        });
    }

    @Test
    @DisplayName("Test request leave with non-existent employee throws exception")
    public void testRequestLeave_NonExistentEmployee_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            leaveService.requestLeave(leaveRequestDto);
        });
    }

    @Test
    @DisplayName("Test request leave with null start date throws exception")
    public void testRequestLeave_NullStartDate_ThrowsException() {
        // Arrange
        leaveRequestDto.setStartDate(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            leaveService.requestLeave(leaveRequestDto);
        });
    }

    @Test
    @DisplayName("Test request leave with null end date throws exception")
    public void testRequestLeave_NullEndDate_ThrowsException() {
        // Arrange
        leaveRequestDto.setEndDate(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            leaveService.requestLeave(leaveRequestDto);
        });
    }

    @Test
    @DisplayName("Test request leave with end date before start date throws exception")
    public void testRequestLeave_EndBeforeStart_ThrowsException() {
        // Arrange
        leaveRequestDto.setStartDate(LocalDate.now().plusDays(10));
        leaveRequestDto.setEndDate(LocalDate.now().plusDays(7));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            leaveService.requestLeave(leaveRequestDto);
        });
    }

    @Test
    @DisplayName("Test request leave with past start date throws exception")
    public void testRequestLeave_PastStartDate_ThrowsException() {
        // Arrange
        leaveRequestDto.setStartDate(LocalDate.now().minusDays(1));
        leaveRequestDto.setEndDate(LocalDate.now().plusDays(1));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            leaveService.requestLeave(leaveRequestDto);
        });
    }

    @Test
    @DisplayName("Test request leave with overlapping dates throws exception")
    public void testRequestLeave_OverlappingDates_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(leaveRepository.findOverlappingLeaves(anyLong(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Arrays.asList(testLeaveRequest));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            leaveService.requestLeave(leaveRequestDto);
        });
    }

    @Test
    @DisplayName("Test request leave with insufficient balance throws exception")
    public void testRequestLeave_InsufficientBalance_ThrowsException() {
        // Arrange
        testEmployee.setPtoBalance(0.0);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(leaveRepository.findOverlappingLeaves(anyLong(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Arrays.asList());

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            leaveService.requestLeave(leaveRequestDto);
        });
    }

    // ========== APPROVE LEAVE TESTS ==========

    @Test
    @DisplayName("Test approve leave with valid data")
    public void testApproveLeave_ValidData_Success() {
        // Arrange
        when(leaveRepository.findById(1L)).thenReturn(Optional.of(testLeaveRequest));
        when(employeeRepository.findById(2L)).thenReturn(Optional.of(testApprover));
        when(leaveRepository.save(any(LeaveRequest.class))).thenReturn(testLeaveRequest);

        // Act
        LeaveRequestDto result = leaveService.approveLeave(1L, 2L);

        // Assert
        assertNotNull(result);
        assertEquals(LeaveStatus.APPROVED.name(), result.getStatus());
        verify(leaveRepository, times(1)).save(any(LeaveRequest.class));
    }

    @Test
    @DisplayName("Test approve leave with null request ID throws exception")
    public void testApproveLeave_NullRequestId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            leaveService.approveLeave(null, 2L);
        });
    }

    @Test
    @DisplayName("Test approve leave with null approver ID throws exception")
    public void testApproveLeave_NullApproverId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            leaveService.approveLeave(1L, null);
        });
    }

    @Test
    @DisplayName("Test approve non-existent leave request throws exception")
    public void testApproveLeave_NonExistentRequest_ThrowsException() {
        // Arrange
        when(leaveRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            leaveService.approveLeave(999L, 2L);
        });
    }

    @Test
    @DisplayName("Test approve already approved leave throws exception")
    public void testApproveLeave_AlreadyApproved_ThrowsException() {
        // Arrange
        testLeaveRequest.setStatus(LeaveStatus.APPROVED);
        when(leaveRepository.findById(1L)).thenReturn(Optional.of(testLeaveRequest));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            leaveService.approveLeave(1L, 2L);
        });
    }

    @Test
    @DisplayName("Test approve denied leave throws exception")
    public void testApproveLeave_AlreadyDenied_ThrowsException() {
        // Arrange
        testLeaveRequest.setStatus(LeaveStatus.DENIED);
        when(leaveRepository.findById(1L)).thenReturn(Optional.of(testLeaveRequest));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            leaveService.approveLeave(1L, 2L);
        });
    }

    // ========== DENY LEAVE TESTS ==========

    @Test
    @DisplayName("Test deny leave with valid data")
    public void testDenyLeave_ValidData_Success() {
        // Arrange
        when(leaveRepository.findById(1L)).thenReturn(Optional.of(testLeaveRequest));
        when(employeeRepository.findById(2L)).thenReturn(Optional.of(testApprover));
        when(leaveRepository.save(any(LeaveRequest.class))).thenReturn(testLeaveRequest);

        // Act
        LeaveRequestDto result = leaveService.denyLeave(1L, 2L, "Insufficient coverage");

        // Assert
        assertNotNull(result);
        assertEquals(LeaveStatus.DENIED.name(), result.getStatus());
        verify(leaveRepository, times(1)).save(any(LeaveRequest.class));
    }

    @Test
    @DisplayName("Test deny leave with null reason throws exception")
    public void testDenyLeave_NullReason_ThrowsException() {
        // Arrange
        when(leaveRepository.findById(1L)).thenReturn(Optional.of(testLeaveRequest));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            leaveService.denyLeave(1L, 2L, null);
        });
    }

    @Test
    @DisplayName("Test deny leave with empty reason throws exception")
    public void testDenyLeave_EmptyReason_ThrowsException() {
        // Arrange
        when(leaveRepository.findById(1L)).thenReturn(Optional.of(testLeaveRequest));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            leaveService.denyLeave(1L, 2L, "");
        });
    }

    // ========== UPDATE BALANCE TESTS ==========

    @Test
    @DisplayName("Test update leave balance with valid data")
    public void testUpdateLeaveBalance_ValidData_Success() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        leaveService.updateLeaveBalance(1L, LeaveType.PTO, 80.0);

        // Assert
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    @DisplayName("Test update leave balance with null employee ID throws exception")
    public void testUpdateLeaveBalance_NullEmployeeId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            leaveService.updateLeaveBalance(null, LeaveType.PTO, 80.0);
        });
    }

    @Test
    @DisplayName("Test update leave balance with null leave type throws exception")
    public void testUpdateLeaveBalance_NullLeaveType_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            leaveService.updateLeaveBalance(1L, null, 80.0);
        });
    }

    @Test
    @DisplayName("Test update leave balance with negative balance throws exception")
    public void testUpdateLeaveBalance_NegativeBalance_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            leaveService.updateLeaveBalance(1L, LeaveType.PTO, -10.0);
        });
    }

    // ========== GET LEAVE REQUESTS TESTS ==========

    @Test
    @DisplayName("Test get leave requests for employee")
    public void testGetLeaveRequests_ValidEmployee_Success() {
        // Arrange
        when(leaveRepository.findByEmployeeId(1L)).thenReturn(Arrays.asList(testLeaveRequest));

        // Act
        List<LeaveRequestDto> result = leaveService.getLeaveRequestsForEmployee(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Test get leave requests with null employee ID throws exception")
    public void testGetLeaveRequests_NullEmployeeId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            leaveService.getLeaveRequestsForEmployee(null);
        });
    }

    @Test
    @DisplayName("Test get pending leave requests")
    public void testGetPendingLeaveRequests_Success() {
        // Arrange
        when(leaveRepository.findByStatus(LeaveStatus.REQUESTED))
                .thenReturn(Arrays.asList(testLeaveRequest));

        // Act
        List<LeaveRequestDto> result = leaveService.getPendingLeaveRequests();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    // ========== CANCEL LEAVE TESTS ==========

    @Test
    @DisplayName("Test cancel leave request with valid data")
    public void testCancelLeaveRequest_ValidData_Success() {
        // Arrange
        when(leaveRepository.findById(1L)).thenReturn(Optional.of(testLeaveRequest));
        when(leaveRepository.save(any(LeaveRequest.class))).thenReturn(testLeaveRequest);

        // Act
        LeaveRequestDto result = leaveService.cancelLeaveRequest(1L);

        // Assert
        assertNotNull(result);
        assertEquals(LeaveStatus.CANCELLED.name(), result.getStatus());
    }

    @Test
    @DisplayName("Test cancel leave request with null ID throws exception")
    public void testCancelLeaveRequest_NullId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            leaveService.cancelLeaveRequest(null);
        });
    }

    @Test
    @DisplayName("Test cancel already approved leave throws exception")
    public void testCancelLeaveRequest_AlreadyApproved_ThrowsException() {
        // Arrange
        testLeaveRequest.setStatus(LeaveStatus.APPROVED);
        testLeaveRequest.setStartDate(LocalDate.now().minusDays(1));
        when(leaveRepository.findById(1L)).thenReturn(Optional.of(testLeaveRequest));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            leaveService.cancelLeaveRequest(1L);
        });
    }

    // ========== BOUNDARY AND EDGE CASE TESTS ==========

    @Test
    @DisplayName("Test request leave for single day")
    public void testRequestLeave_SingleDay_Success() {
        // Arrange
        leaveRequestDto.setStartDate(LocalDate.now().plusDays(7));
        leaveRequestDto.setEndDate(LocalDate.now().plusDays(7));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(leaveRepository.findOverlappingLeaves(anyLong(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Arrays.asList());
        when(leaveRepository.save(any(LeaveRequest.class))).thenReturn(testLeaveRequest);

        // Act
        LeaveRequestDto result = leaveService.requestLeave(leaveRequestDto);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Test request leave for maximum duration")
    public void testRequestLeave_MaxDuration_Success() {
        // Arrange
        leaveRequestDto.setStartDate(LocalDate.now().plusDays(7));
        leaveRequestDto.setEndDate(LocalDate.now().plusDays(37));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(leaveRepository.findOverlappingLeaves(anyLong(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Arrays.asList());
        when(leaveRepository.save(any(LeaveRequest.class))).thenReturn(testLeaveRequest);

        // Act
        LeaveRequestDto result = leaveService.requestLeave(leaveRequestDto);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Test request all leave types")
    public void testRequestLeave_AllLeaveTypes_Success() {
        // Test each leave type
        String[] leaveTypes = {"PTO", "SICK", "UNPAID"};
        
        for (String type : leaveTypes) {
            // Arrange
            leaveRequestDto.setType(type);
            when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
            when(leaveRepository.findOverlappingLeaves(anyLong(), any(LocalDate.class), any(LocalDate.class)))
                    .thenReturn(Arrays.asList());
            when(leaveRepository.save(any(LeaveRequest.class))).thenReturn(testLeaveRequest);

            // Act
            LeaveRequestDto result = leaveService.requestLeave(leaveRequestDto);

            // Assert
            assertNotNull(result);
        }
    }

    @Test
    @DisplayName("Test update balance with zero balance")
    public void testUpdateLeaveBalance_ZeroBalance_Success() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        leaveService.updateLeaveBalance(1L, LeaveType.PTO, 0.0);

        // Assert
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    @DisplayName("Test update balance with maximum balance")
    public void testUpdateLeaveBalance_MaxBalance_Success() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        leaveService.updateLeaveBalance(1L, LeaveType.PTO, 999.99);

        // Assert
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    @DisplayName("Test get leave requests returns empty list when no requests")
    public void testGetLeaveRequests_NoRequests_ReturnsEmptyList() {
        // Arrange
        when(leaveRepository.findByEmployeeId(1L)).thenReturn(Arrays.asList());

        // Act
        List<LeaveRequestDto> result = leaveService.getLeaveRequestsForEmployee(1L);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.size());
    }
}