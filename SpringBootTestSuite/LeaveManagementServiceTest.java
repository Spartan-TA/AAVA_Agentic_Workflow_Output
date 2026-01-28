package com.warehouse.management.leave;

import com.warehouse.management.common.exceptions.BusinessException;
import com.warehouse.management.common.exceptions.ResourceNotFoundException;
import com.warehouse.management.employee.Employee;
import com.warehouse.management.employee.EmployeeRepository;
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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Comprehensive JUnit test suite for LeaveManagementService
 * Tests cover leave requests, approvals, accrual balances, and edge cases
 */
@ExtendWith(MockitoExtension.class)
class LeaveManagementServiceTest {

    @Mock
    private LeaveRequestRepository leaveRequestRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private LeaveBalanceRepository leaveBalanceRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private LeaveManagementServiceImpl leaveManagementService;

    private Employee testEmployee;
    private Employee testSupervisor;
    private LeaveRequest testLeaveRequest;
    private LeaveBalance testLeaveBalance;
    private LeaveRequestRequest requestRequest;
    private UUID employeeId;
    private UUID supervisorId;
    private UUID leaveRequestId;

    @BeforeEach
    void setUp() {
        employeeId = UUID.randomUUID();
        supervisorId = UUID.randomUUID();
        leaveRequestId = UUID.randomUUID();
        
        // Setup test employee
        testEmployee = new Employee();
        testEmployee.setId(employeeId);
        testEmployee.setBadgeId("EMP001");
        testEmployee.setFirstName("John");
        testEmployee.setLastName("Doe");
        testEmployee.setEmail("john.doe@warehouse.com");
        
        // Setup test supervisor
        testSupervisor = new Employee();
        testSupervisor.setId(supervisorId);
        testSupervisor.setBadgeId("SUP001");
        testSupervisor.setFirstName("Jane");
        testSupervisor.setLastName("Smith");
        
        // Setup leave balance
        testLeaveBalance = new LeaveBalance();
        testLeaveBalance.setId(UUID.randomUUID());
        testLeaveBalance.setEmployee(testEmployee);
        testLeaveBalance.setLeaveType(LeaveType.PTO);
        testLeaveBalance.setAvailableDays(15.0);
        testLeaveBalance.setAccruedDays(20.0);
        testLeaveBalance.setUsedDays(5.0);
        
        // Setup leave request
        testLeaveRequest = new LeaveRequest();
        testLeaveRequest.setId(leaveRequestId);
        testLeaveRequest.setEmployee(testEmployee);
        testLeaveRequest.setLeaveType(LeaveType.PTO);
        testLeaveRequest.setStartDate(LocalDate.now().plusDays(7));
        testLeaveRequest.setEndDate(LocalDate.now().plusDays(9));
        testLeaveRequest.setTotalDays(3.0);
        testLeaveRequest.setStatus(LeaveStatus.PENDING);
        testLeaveRequest.setReason("Family vacation");
        
        // Setup request DTO
        requestRequest = new LeaveRequestRequest();
        requestRequest.setEmployeeId(employeeId);
        requestRequest.setLeaveType(LeaveType.PTO);
        requestRequest.setStartDate(LocalDate.now().plusDays(7));
        requestRequest.setEndDate(LocalDate.now().plusDays(9));
        requestRequest.setReason("Family vacation");
    }

    // ========== CREATE LEAVE REQUEST TESTS ==========

    @Test
    void testCreateLeaveRequest_ValidInput_Success() {
        // Arrange
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(testEmployee));
        when(leaveBalanceRepository.findByEmployeeAndLeaveType(testEmployee, LeaveType.PTO))
                .thenReturn(Optional.of(testLeaveBalance));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenReturn(testLeaveRequest);
        doNothing().when(notificationService).sendLeaveRequestNotification(any());

        // Act
        LeaveRequestResponse result = leaveManagementService.createLeaveRequest(requestRequest);

        // Assert
        assertNotNull(result);
        assertEquals(LeaveStatus.PENDING, result.getStatus());
        verify(leaveRequestRepository, times(1)).save(any(LeaveRequest.class));
        verify(notificationService, times(1)).sendLeaveRequestNotification(any());
    }

    @Test
    void testCreateLeaveRequest_EmployeeNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            leaveManagementService.createLeaveRequest(requestRequest);
        });
        
        assertTrue(exception.getMessage().contains("Employee not found"));
        verify(leaveRequestRepository, never()).save(any(LeaveRequest.class));
    }

    @Test
    void testCreateLeaveRequest_InsufficientBalance_ThrowsBusinessException() {
        // Arrange
        testLeaveBalance.setAvailableDays(2.0); // Less than requested 3 days
        
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(testEmployee));
        when(leaveBalanceRepository.findByEmployeeAndLeaveType(testEmployee, LeaveType.PTO))
                .thenReturn(Optional.of(testLeaveBalance));

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            leaveManagementService.createLeaveRequest(requestRequest);
        });
        
        assertTrue(exception.getMessage().contains("Insufficient leave balance"));
        verify(leaveRequestRepository, never()).save(any(LeaveRequest.class));
    }

    @Test
    void testCreateLeaveRequest_InvalidDateRange_ThrowsBusinessException() {
        // Arrange
        requestRequest.setStartDate(LocalDate.now().plusDays(9));
        requestRequest.setEndDate(LocalDate.now().plusDays(7));

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            leaveManagementService.createLeaveRequest(requestRequest);
        });
    }

    @Test
    void testCreateLeaveRequest_PastStartDate_ThrowsBusinessException() {
        // Arrange
        requestRequest.setStartDate(LocalDate.now().minusDays(1));

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            leaveManagementService.createLeaveRequest(requestRequest);
        });
    }

    @Test
    void testCreateLeaveRequest_EmptyReason_ThrowsBusinessException() {
        // Arrange
        requestRequest.setReason("");

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            leaveManagementService.createLeaveRequest(requestRequest);
        });
    }

    @Test
    void testCreateLeaveRequest_OverlappingLeave_ThrowsBusinessException() {
        // Arrange
        LeaveRequest existingLeave = new LeaveRequest();
        existingLeave.setStartDate(LocalDate.now().plusDays(8));
        existingLeave.setEndDate(LocalDate.now().plusDays(10));
        existingLeave.setStatus(LeaveStatus.APPROVED);
        
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(testEmployee));
        when(leaveBalanceRepository.findByEmployeeAndLeaveType(testEmployee, LeaveType.PTO))
                .thenReturn(Optional.of(testLeaveBalance));
        when(leaveRequestRepository.findOverlappingLeaves(any(), any(), any()))
                .thenReturn(Arrays.asList(existingLeave));

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            leaveManagementService.createLeaveRequest(requestRequest);
        });
        
        assertTrue(exception.getMessage().contains("overlapping"));
    }

    // ========== APPROVE LEAVE REQUEST TESTS ==========

    @Test
    void testApproveLeaveRequest_ValidRequest_Success() {
        // Arrange
        when(leaveRequestRepository.findById(leaveRequestId)).thenReturn(Optional.of(testLeaveRequest));
        when(leaveBalanceRepository.findByEmployeeAndLeaveType(testEmployee, LeaveType.PTO))
                .thenReturn(Optional.of(testLeaveBalance));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenReturn(testLeaveRequest);
        when(leaveBalanceRepository.save(any(LeaveBalance.class))).thenReturn(testLeaveBalance);
        doNothing().when(notificationService).sendLeaveApprovalNotification(any());

        // Act
        LeaveRequestResponse result = leaveManagementService.approveLeaveRequest(leaveRequestId, supervisorId);

        // Assert
        assertNotNull(result);
        assertEquals(LeaveStatus.APPROVED, testLeaveRequest.getStatus());
        verify(leaveRequestRepository, times(1)).save(testLeaveRequest);
        verify(leaveBalanceRepository, times(1)).save(testLeaveBalance);
        verify(notificationService, times(1)).sendLeaveApprovalNotification(any());
    }

    @Test
    void testApproveLeaveRequest_RequestNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(leaveRequestRepository.findById(leaveRequestId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            leaveManagementService.approveLeaveRequest(leaveRequestId, supervisorId);
        });
    }

    @Test
    void testApproveLeaveRequest_AlreadyApproved_ThrowsBusinessException() {
        // Arrange
        testLeaveRequest.setStatus(LeaveStatus.APPROVED);
        when(leaveRequestRepository.findById(leaveRequestId)).thenReturn(Optional.of(testLeaveRequest));

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            leaveManagementService.approveLeaveRequest(leaveRequestId, supervisorId);
        });
        
        assertTrue(exception.getMessage().contains("already"));
    }

    @Test
    void testApproveLeaveRequest_AlreadyDenied_ThrowsBusinessException() {
        // Arrange
        testLeaveRequest.setStatus(LeaveStatus.DENIED);
        when(leaveRequestRepository.findById(leaveRequestId)).thenReturn(Optional.of(testLeaveRequest));

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            leaveManagementService.approveLeaveRequest(leaveRequestId, supervisorId);
        });
    }

    // ========== DENY LEAVE REQUEST TESTS ==========

    @Test
    void testDenyLeaveRequest_ValidRequest_Success() {
        // Arrange
        String denialReason = "Insufficient staffing";
        when(leaveRequestRepository.findById(leaveRequestId)).thenReturn(Optional.of(testLeaveRequest));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenReturn(testLeaveRequest);
        doNothing().when(notificationService).sendLeaveDenialNotification(any());

        // Act
        LeaveRequestResponse result = leaveManagementService.denyLeaveRequest(leaveRequestId, supervisorId, denialReason);

        // Assert
        assertNotNull(result);
        assertEquals(LeaveStatus.DENIED, testLeaveRequest.getStatus());
        assertEquals(denialReason, testLeaveRequest.getDenialReason());
        verify(leaveRequestRepository, times(1)).save(testLeaveRequest);
        verify(notificationService, times(1)).sendLeaveDenialNotification(any());
    }

    @Test
    void testDenyLeaveRequest_EmptyReason_ThrowsBusinessException() {
        // Arrange
        when(leaveRequestRepository.findById(leaveRequestId)).thenReturn(Optional.of(testLeaveRequest));

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            leaveManagementService.denyLeaveRequest(leaveRequestId, supervisorId, "");
        });
    }

    @Test
    void testDenyLeaveRequest_NullReason_ThrowsBusinessException() {
        // Arrange
        when(leaveRequestRepository.findById(leaveRequestId)).thenReturn(Optional.of(testLeaveRequest));

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            leaveManagementService.denyLeaveRequest(leaveRequestId, supervisorId, null);
        });
    }

    // ========== CANCEL LEAVE REQUEST TESTS ==========

    @Test
    void testCancelLeaveRequest_ValidRequest_Success() {
        // Arrange
        testLeaveRequest.setStatus(LeaveStatus.APPROVED);
        when(leaveRequestRepository.findById(leaveRequestId)).thenReturn(Optional.of(testLeaveRequest));
        when(leaveBalanceRepository.findByEmployeeAndLeaveType(testEmployee, LeaveType.PTO))
                .thenReturn(Optional.of(testLeaveBalance));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenReturn(testLeaveRequest);
        when(leaveBalanceRepository.save(any(LeaveBalance.class))).thenReturn(testLeaveBalance);

        // Act
        LeaveRequestResponse result = leaveManagementService.cancelLeaveRequest(leaveRequestId, employeeId);

        // Assert
        assertNotNull(result);
        assertEquals(LeaveStatus.CANCELLED, testLeaveRequest.getStatus());
        verify(leaveBalanceRepository, times(1)).save(testLeaveBalance);
    }

    @Test
    void testCancelLeaveRequest_PastLeave_ThrowsBusinessException() {
        // Arrange
        testLeaveRequest.setStartDate(LocalDate.now().minusDays(5));
        testLeaveRequest.setStatus(LeaveStatus.APPROVED);
        when(leaveRequestRepository.findById(leaveRequestId)).thenReturn(Optional.of(testLeaveRequest));

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            leaveManagementService.cancelLeaveRequest(leaveRequestId, employeeId);
        });
        
        assertTrue(exception.getMessage().contains("past"));
    }

    // ========== GET LEAVE BALANCE TESTS ==========

    @Test
    void testGetLeaveBalance_ValidEmployee_Success() {
        // Arrange
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(testEmployee));
        when(leaveBalanceRepository.findByEmployee(testEmployee))
                .thenReturn(Arrays.asList(testLeaveBalance));

        // Act
        List<LeaveBalanceResponse> result = leaveManagementService.getLeaveBalance(employeeId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(15.0, result.get(0).getAvailableDays());
    }

    @Test
    void testGetLeaveBalance_EmployeeNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            leaveManagementService.getLeaveBalance(employeeId);
        });
    }

    @Test
    void testGetLeaveBalance_NoBalances_ReturnsEmptyList() {
        // Arrange
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(testEmployee));
        when(leaveBalanceRepository.findByEmployee(testEmployee)).thenReturn(Arrays.asList());

        // Act
        List<LeaveBalanceResponse> result = leaveManagementService.getLeaveBalance(employeeId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ========== GET LEAVE REQUESTS TESTS ==========

    @Test
    void testGetLeaveRequests_ValidEmployee_Success() {
        // Arrange
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(testEmployee));
        when(leaveRequestRepository.findByEmployee(testEmployee))
                .thenReturn(Arrays.asList(testLeaveRequest));

        // Act
        List<LeaveRequestResponse> result = leaveManagementService.getLeaveRequests(employeeId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(LeaveStatus.PENDING, result.get(0).getStatus());
    }

    @Test
    void testGetLeaveRequestsByStatus_ValidStatus_Success() {
        // Arrange
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(testEmployee));
        when(leaveRequestRepository.findByEmployeeAndStatus(testEmployee, LeaveStatus.PENDING))
                .thenReturn(Arrays.asList(testLeaveRequest));

        // Act
        List<LeaveRequestResponse> result = leaveManagementService.getLeaveRequestsByStatus(employeeId, LeaveStatus.PENDING);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    // ========== ACCRUAL CALCULATION TESTS ==========

    @Test
    void testCalculateAccrual_ValidEmployee_Success() {
        // Arrange
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(testEmployee));
        when(leaveBalanceRepository.findByEmployeeAndLeaveType(testEmployee, LeaveType.PTO))
                .thenReturn(Optional.of(testLeaveBalance));
        when(leaveBalanceRepository.save(any(LeaveBalance.class))).thenReturn(testLeaveBalance);

        // Act
        leaveManagementService.calculateAndUpdateAccrual(employeeId, LeaveType.PTO);

        // Assert
        verify(leaveBalanceRepository, times(1)).save(any(LeaveBalance.class));
    }

    // ========== EDGE CASE TESTS ==========

    @Test
    void testCreateLeaveRequest_SingleDayLeave_Success() {
        // Arrange
        requestRequest.setStartDate(LocalDate.now().plusDays(7));
        requestRequest.setEndDate(LocalDate.now().plusDays(7));
        
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(testEmployee));
        when(leaveBalanceRepository.findByEmployeeAndLeaveType(testEmployee, LeaveType.PTO))
                .thenReturn(Optional.of(testLeaveBalance));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenReturn(testLeaveRequest);

        // Act
        LeaveRequestResponse result = leaveManagementService.createLeaveRequest(requestRequest);

        // Assert
        assertNotNull(result);
        verify(leaveRequestRepository, times(1)).save(any(LeaveRequest.class));
    }

    @Test
    void testCreateLeaveRequest_SickLeave_Success() {
        // Arrange
        requestRequest.setLeaveType(LeaveType.SICK);
        testLeaveBalance.setLeaveType(LeaveType.SICK);
        
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(testEmployee));
        when(leaveBalanceRepository.findByEmployeeAndLeaveType(testEmployee, LeaveType.SICK))
                .thenReturn(Optional.of(testLeaveBalance));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenReturn(testLeaveRequest);

        // Act
        LeaveRequestResponse result = leaveManagementService.createLeaveRequest(requestRequest);

        // Assert
        assertNotNull(result);
        verify(leaveRequestRepository, times(1)).save(any(LeaveRequest.class));
    }

    @Test
    void testCreateLeaveRequest_UnpaidLeave_Success() {
        // Arrange
        requestRequest.setLeaveType(LeaveType.UNPAID);
        
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(testEmployee));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenReturn(testLeaveRequest);

        // Act
        LeaveRequestResponse result = leaveManagementService.createLeaveRequest(requestRequest);

        // Assert
        assertNotNull(result);
        verify(leaveBalanceRepository, never()).findByEmployeeAndLeaveType(any(), any());
    }

    @Test
    void testCreateLeaveRequest_LongTermLeave_Success() {
        // Arrange
        requestRequest.setStartDate(LocalDate.now().plusDays(30));
        requestRequest.setEndDate(LocalDate.now().plusDays(60));
        testLeaveBalance.setAvailableDays(50.0);
        
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(testEmployee));
        when(leaveBalanceRepository.findByEmployeeAndLeaveType(testEmployee, LeaveType.PTO))
                .thenReturn(Optional.of(testLeaveBalance));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenReturn(testLeaveRequest);

        // Act
        LeaveRequestResponse result = leaveManagementService.createLeaveRequest(requestRequest);

        // Assert
        assertNotNull(result);
        verify(leaveRequestRepository, times(1)).save(any(LeaveRequest.class));
    }

    @Test
    void testApproveLeaveRequest_ExactBalanceMatch_Success() {
        // Arrange
        testLeaveBalance.setAvailableDays(3.0);
        testLeaveRequest.setTotalDays(3.0);
        
        when(leaveRequestRepository.findById(leaveRequestId)).thenReturn(Optional.of(testLeaveRequest));
        when(leaveBalanceRepository.findByEmployeeAndLeaveType(testEmployee, LeaveType.PTO))
                .thenReturn(Optional.of(testLeaveBalance));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenReturn(testLeaveRequest);
        when(leaveBalanceRepository.save(any(LeaveBalance.class))).thenReturn(testLeaveBalance);

        // Act
        LeaveRequestResponse result = leaveManagementService.approveLeaveRequest(leaveRequestId, supervisorId);

        // Assert
        assertNotNull(result);
        assertEquals(0.0, testLeaveBalance.getAvailableDays());
    }
}