package com.wms.leave.service;

import com.wms.leave.entity.LeaveRequest;
import com.wms.leave.entity.LeaveBalance;
import com.wms.leave.repository.LeaveRequestRepository;
import com.wms.leave.repository.LeaveBalanceRepository;
import com.wms.leave.dto.LeaveRequestDto;
import com.wms.leave.dto.LeaveApprovalDto;
import com.wms.leave.dto.LeaveBalanceDto;
import com.wms.employee.entity.Employee;
import com.wms.employee.repository.EmployeeRepository;
import com.wms.scheduling.service.SchedulingService;
import com.wms.exception.ResourceNotFoundException;
import com.wms.exception.BadRequestException;
import com.wms.exception.ConflictException;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Comprehensive JUnit test suite for LeaveService
 * Covers leave requests, approval workflow, balance management, and integration
 */
public class LeaveServiceTest {

    @Mock
    private LeaveRequestRepository leaveRequestRepository;

    @Mock
    private LeaveBalanceRepository leaveBalanceRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private SchedulingService schedulingService;

    @InjectMocks
    private LeaveService leaveService;

    private Employee testEmployee;
    private LeaveRequest testLeaveRequest;
    private LeaveBalance testLeaveBalance;
    private LeaveRequestDto leaveRequestDto;
    private LeaveApprovalDto approvalDto;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Setup test employee
        testEmployee = new Employee();
        testEmployee.setId(1L);
        testEmployee.setName("John Doe");
        testEmployee.setBadgeId("BADGE001");
        
        // Setup test leave balance
        testLeaveBalance = new LeaveBalance();
        testLeaveBalance.setId(1L);
        testLeaveBalance.setEmployeeId(1L);
        testLeaveBalance.setLeaveType("VACATION");
        testLeaveBalance.setTotalDays(20.0);
        testLeaveBalance.setUsedDays(5.0);
        testLeaveBalance.setRemainingDays(15.0);
        testLeaveBalance.setYear(LocalDate.now().getYear());
        
        // Setup test leave request
        testLeaveRequest = new LeaveRequest();
        testLeaveRequest.setId(1L);
        testLeaveRequest.setEmployeeId(1L);
        testLeaveRequest.setLeaveType("VACATION");
        testLeaveRequest.setStartDate(LocalDate.now().plusDays(7));
        testLeaveRequest.setEndDate(LocalDate.now().plusDays(9));
        testLeaveRequest.setDays(3.0);
        testLeaveRequest.setReason("Family vacation");
        testLeaveRequest.setStatus("PENDING");
        
        // Setup leave request DTO
        leaveRequestDto = new LeaveRequestDto();
        leaveRequestDto.setEmployeeId(1L);
        leaveRequestDto.setLeaveType("VACATION");
        leaveRequestDto.setStartDate(LocalDate.now().plusDays(7));
        leaveRequestDto.setEndDate(LocalDate.now().plusDays(9));
        leaveRequestDto.setReason("Family vacation");
        
        // Setup approval DTO
        approvalDto = new LeaveApprovalDto();
        approvalDto.setApproved(true);
        approvalDto.setApproverComments("Approved");
    }

    // ========== LEAVE REQUEST SUBMISSION TESTS ==========

    @Test
    @DisplayName("Test submit leave request with valid data")
    public void testSubmitLeaveRequest_ValidData_Success() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(leaveBalanceRepository.findByEmployeeIdAndLeaveTypeAndYear(1L, "VACATION", LocalDate.now().getYear()))
            .thenReturn(Optional.of(testLeaveBalance));
        when(leaveRequestRepository.findOverlappingRequests(anyLong(), any(LocalDate.class), any(LocalDate.class)))
            .thenReturn(Arrays.asList());
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenReturn(testLeaveRequest);

        // Act
        LeaveRequestDto result = leaveService.submitLeaveRequest(leaveRequestDto);

        // Assert
        assertNotNull(result);
        assertEquals("PENDING", result.getStatus());
        assertEquals(3.0, result.getDays());
        verify(leaveRequestRepository, times(1)).save(any(LeaveRequest.class));
    }

    @Test
    @DisplayName("Test submit leave request with insufficient balance")
    public void testSubmitLeaveRequest_InsufficientBalance_ThrowsBadRequestException() {
        // Arrange
        testLeaveBalance.setRemainingDays(2.0);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(leaveBalanceRepository.findByEmployeeIdAndLeaveTypeAndYear(1L, "VACATION", LocalDate.now().getYear()))
            .thenReturn(Optional.of(testLeaveBalance));

        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            leaveService.submitLeaveRequest(leaveRequestDto);
        });
    }

    @Test
    @DisplayName("Test submit leave request for non-existent employee")
    public void testSubmitLeaveRequest_NonExistentEmployee_ThrowsResourceNotFoundException() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());
        leaveRequestDto.setEmployeeId(999L);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            leaveService.submitLeaveRequest(leaveRequestDto);
        });
    }

    @Test
    @DisplayName("Test submit leave request with overlapping dates")
    public void testSubmitLeaveRequest_OverlappingDates_ThrowsConflictException() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(leaveBalanceRepository.findByEmployeeIdAndLeaveTypeAndYear(1L, "VACATION", LocalDate.now().getYear()))
            .thenReturn(Optional.of(testLeaveBalance));
        when(leaveRequestRepository.findOverlappingRequests(anyLong(), any(LocalDate.class), any(LocalDate.class)))
            .thenReturn(Arrays.asList(testLeaveRequest));

        // Act & Assert
        assertThrows(ConflictException.class, () -> {
            leaveService.submitLeaveRequest(leaveRequestDto);
        });
    }

    @Test
    @DisplayName("Test submit leave request with end date before start date")
    public void testSubmitLeaveRequest_InvalidDateRange_ThrowsBadRequestException() {
        // Arrange
        leaveRequestDto.setStartDate(LocalDate.now().plusDays(10));
        leaveRequestDto.setEndDate(LocalDate.now().plusDays(5));

        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            leaveService.submitLeaveRequest(leaveRequestDto);
        });
    }

    @Test
    @DisplayName("Test submit leave request with past dates")
    public void testSubmitLeaveRequest_PastDates_ThrowsBadRequestException() {
        // Arrange
        leaveRequestDto.setStartDate(LocalDate.now().minusDays(5));
        leaveRequestDto.setEndDate(LocalDate.now().minusDays(3));

        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            leaveService.submitLeaveRequest(leaveRequestDto);
        });
    }

    @Test
    @DisplayName("Test submit leave request with null reason")
    public void testSubmitLeaveRequest_NullReason_ThrowsBadRequestException() {
        // Arrange
        leaveRequestDto.setReason(null);

        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            leaveService.submitLeaveRequest(leaveRequestDto);
        });
    }

    @Test
    @DisplayName("Test submit sick leave without balance check")
    public void testSubmitLeaveRequest_SickLeave_NoBalanceCheck() {
        // Arrange
        leaveRequestDto.setLeaveType("SICK");
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(leaveRequestRepository.findOverlappingRequests(anyLong(), any(LocalDate.class), any(LocalDate.class)))
            .thenReturn(Arrays.asList());
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenReturn(testLeaveRequest);

        // Act
        LeaveRequestDto result = leaveService.submitLeaveRequest(leaveRequestDto);

        // Assert
        assertNotNull(result);
        verify(leaveBalanceRepository, never()).findByEmployeeIdAndLeaveTypeAndYear(anyLong(), anyString(), anyInt());
    }

    // ========== LEAVE APPROVAL TESTS ==========

    @Test
    @DisplayName("Test approve leave request")
    public void testApproveLeaveRequest_ValidRequest_Success() {
        // Arrange
        when(leaveRequestRepository.findById(1L)).thenReturn(Optional.of(testLeaveRequest));
        when(leaveBalanceRepository.findByEmployeeIdAndLeaveTypeAndYear(1L, "VACATION", LocalDate.now().getYear()))
            .thenReturn(Optional.of(testLeaveBalance));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenReturn(testLeaveRequest);
        when(leaveBalanceRepository.save(any(LeaveBalance.class))).thenReturn(testLeaveBalance);
        doNothing().when(schedulingService).flagShiftsForCoverage(anyLong(), any(LocalDate.class), any(LocalDate.class));

        // Act
        LeaveRequestDto result = leaveService.approveLeaveRequest(1L, approvalDto);

        // Assert
        assertNotNull(result);
        assertEquals("APPROVED", result.getStatus());
        verify(leaveBalanceRepository, times(1)).save(any(LeaveBalance.class));
        verify(schedulingService, times(1)).flagShiftsForCoverage(anyLong(), any(LocalDate.class), any(LocalDate.class));
    }

    @Test
    @DisplayName("Test deny leave request")
    public void testDenyLeaveRequest_ValidRequest_Success() {
        // Arrange
        approvalDto.setApproved(false);
        approvalDto.setApproverComments("Insufficient staffing");
        when(leaveRequestRepository.findById(1L)).thenReturn(Optional.of(testLeaveRequest));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenReturn(testLeaveRequest);

        // Act
        LeaveRequestDto result = leaveService.approveLeaveRequest(1L, approvalDto);

        // Assert
        assertNotNull(result);
        assertEquals("DENIED", result.getStatus());
        verify(leaveBalanceRepository, never()).save(any(LeaveBalance.class));
        verify(schedulingService, never()).flagShiftsForCoverage(anyLong(), any(LocalDate.class), any(LocalDate.class));
    }

    @Test
    @DisplayName("Test approve already approved request")
    public void testApproveLeaveRequest_AlreadyApproved_ThrowsConflictException() {
        // Arrange
        testLeaveRequest.setStatus("APPROVED");
        when(leaveRequestRepository.findById(1L)).thenReturn(Optional.of(testLeaveRequest));

        // Act & Assert
        assertThrows(ConflictException.class, () -> {
            leaveService.approveLeaveRequest(1L, approvalDto);
        });
    }

    @Test
    @DisplayName("Test approve non-existent request")
    public void testApproveLeaveRequest_NonExistentRequest_ThrowsResourceNotFoundException() {
        // Arrange
        when(leaveRequestRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            leaveService.approveLeaveRequest(999L, approvalDto);
        });
    }

    // ========== LEAVE BALANCE TESTS ==========

    @Test
    @DisplayName("Test get leave balance for employee")
    public void testGetLeaveBalance_ValidEmployee_Success() {
        // Arrange
        when(leaveBalanceRepository.findByEmployeeIdAndYear(1L, LocalDate.now().getYear()))
            .thenReturn(Arrays.asList(testLeaveBalance));

        // Act
        List<LeaveBalanceDto> result = leaveService.getLeaveBalance(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(15.0, result.get(0).getRemainingDays());
    }

    @Test
    @DisplayName("Test get leave balance for non-existent employee")
    public void testGetLeaveBalance_NonExistentEmployee_ReturnsEmptyList() {
        // Arrange
        when(leaveBalanceRepository.findByEmployeeIdAndYear(999L, LocalDate.now().getYear()))
            .thenReturn(Arrays.asList());

        // Act
        List<LeaveBalanceDto> result = leaveService.getLeaveBalance(999L);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    @DisplayName("Test update leave balance after approval")
    public void testUpdateLeaveBalance_AfterApproval_Success() {
        // Arrange
        when(leaveBalanceRepository.findByEmployeeIdAndLeaveTypeAndYear(1L, "VACATION", LocalDate.now().getYear()))
            .thenReturn(Optional.of(testLeaveBalance));
        when(leaveBalanceRepository.save(any(LeaveBalance.class))).thenReturn(testLeaveBalance);

        // Act
        leaveService.updateLeaveBalance(1L, "VACATION", 3.0);

        // Assert
        assertEquals(8.0, testLeaveBalance.getUsedDays());
        assertEquals(12.0, testLeaveBalance.getRemainingDays());
        verify(leaveBalanceRepository, times(1)).save(testLeaveBalance);
    }

    @Test
    @DisplayName("Test initialize leave balance for new employee")
    public void testInitializeLeaveBalance_NewEmployee_Success() {
        // Arrange
        when(leaveBalanceRepository.findByEmployeeIdAndYear(1L, LocalDate.now().getYear()))
            .thenReturn(Arrays.asList());
        when(leaveBalanceRepository.save(any(LeaveBalance.class))).thenReturn(testLeaveBalance);

        // Act
        leaveService.initializeLeaveBalance(1L);

        // Assert
        verify(leaveBalanceRepository, atLeast(1)).save(any(LeaveBalance.class));
    }

    // ========== LEAVE REQUEST QUERY TESTS ==========

    @Test
    @DisplayName("Test get pending leave requests for supervisor")
    public void testGetPendingLeaveRequests_ValidSupervisor_Success() {
        // Arrange
        when(leaveRequestRepository.findPendingRequestsByTeam(1L))
            .thenReturn(Arrays.asList(testLeaveRequest));

        // Act
        List<LeaveRequestDto> result = leaveService.getPendingLeaveRequests(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("PENDING", result.get(0).getStatus());
    }

    @Test
    @DisplayName("Test get leave history for employee")
    public void testGetLeaveHistory_ValidEmployee_Success() {
        // Arrange
        when(leaveRequestRepository.findByEmployeeIdOrderByStartDateDesc(1L))
            .thenReturn(Arrays.asList(testLeaveRequest));

        // Act
        List<LeaveRequestDto> result = leaveService.getLeaveHistory(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Test get leave requests by date range")
    public void testGetLeaveRequestsByDateRange_ValidRange_Success() {
        // Arrange
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().plusDays(30);
        when(leaveRequestRepository.findByDateRange(startDate, endDate))
            .thenReturn(Arrays.asList(testLeaveRequest));

        // Act
        List<LeaveRequestDto> result = leaveService.getLeaveRequestsByDateRange(startDate, endDate);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    // ========== CANCEL LEAVE REQUEST TESTS ==========

    @Test
    @DisplayName("Test cancel pending leave request")
    public void testCancelLeaveRequest_PendingRequest_Success() {
        // Arrange
        when(leaveRequestRepository.findById(1L)).thenReturn(Optional.of(testLeaveRequest));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenReturn(testLeaveRequest);

        // Act
        leaveService.cancelLeaveRequest(1L);

        // Assert
        assertEquals("CANCELLED", testLeaveRequest.getStatus());
        verify(leaveRequestRepository, times(1)).save(testLeaveRequest);
    }

    @Test
    @DisplayName("Test cancel approved leave request with balance restoration")
    public void testCancelLeaveRequest_ApprovedRequest_RestoresBalance() {
        // Arrange
        testLeaveRequest.setStatus("APPROVED");
        when(leaveRequestRepository.findById(1L)).thenReturn(Optional.of(testLeaveRequest));
        when(leaveBalanceRepository.findByEmployeeIdAndLeaveTypeAndYear(1L, "VACATION", LocalDate.now().getYear()))
            .thenReturn(Optional.of(testLeaveBalance));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenReturn(testLeaveRequest);
        when(leaveBalanceRepository.save(any(LeaveBalance.class))).thenReturn(testLeaveBalance);

        // Act
        leaveService.cancelLeaveRequest(1L);

        // Assert
        assertEquals("CANCELLED", testLeaveRequest.getStatus());
        assertEquals(2.0, testLeaveBalance.getUsedDays());
        assertEquals(18.0, testLeaveBalance.getRemainingDays());
    }

    @Test
    @DisplayName("Test cancel already cancelled request")
    public void testCancelLeaveRequest_AlreadyCancelled_ThrowsConflictException() {
        // Arrange
        testLeaveRequest.setStatus("CANCELLED");
        when(leaveRequestRepository.findById(1L)).thenReturn(Optional.of(testLeaveRequest));

        // Act & Assert
        assertThrows(ConflictException.class, () -> {
            leaveService.cancelLeaveRequest(1L);
        });
    }

    // ========== EDGE CASE TESTS ==========

    @Test
    @DisplayName("Test submit leave request for single day")
    public void testSubmitLeaveRequest_SingleDay_Success() {
        // Arrange
        leaveRequestDto.setStartDate(LocalDate.now().plusDays(7));
        leaveRequestDto.setEndDate(LocalDate.now().plusDays(7));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(leaveBalanceRepository.findByEmployeeIdAndLeaveTypeAndYear(1L, "VACATION", LocalDate.now().getYear()))
            .thenReturn(Optional.of(testLeaveBalance));
        when(leaveRequestRepository.findOverlappingRequests(anyLong(), any(LocalDate.class), any(LocalDate.class)))
            .thenReturn(Arrays.asList());
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenReturn(testLeaveRequest);

        // Act
        LeaveRequestDto result = leaveService.submitLeaveRequest(leaveRequestDto);

        // Assert
        assertNotNull(result);
        assertEquals(1.0, result.getDays());
    }

    @Test
    @DisplayName("Test submit leave request for maximum duration")
    public void testSubmitLeaveRequest_MaximumDuration_Success() {
        // Arrange
        leaveRequestDto.setStartDate(LocalDate.now().plusDays(7));
        leaveRequestDto.setEndDate(LocalDate.now().plusDays(21));
        testLeaveBalance.setRemainingDays(20.0);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(leaveBalanceRepository.findByEmployeeIdAndLeaveTypeAndYear(1L, "VACATION", LocalDate.now().getYear()))
            .thenReturn(Optional.of(testLeaveBalance));
        when(leaveRequestRepository.findOverlappingRequests(anyLong(), any(LocalDate.class), any(LocalDate.class)))
            .thenReturn(Arrays.asList());
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenReturn(testLeaveRequest);

        // Act
        LeaveRequestDto result = leaveService.submitLeaveRequest(leaveRequestDto);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Test submit unpaid leave without balance")
    public void testSubmitLeaveRequest_UnpaidLeave_NoBalanceRequired() {
        // Arrange
        leaveRequestDto.setLeaveType("UNPAID");
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(leaveRequestRepository.findOverlappingRequests(anyLong(), any(LocalDate.class), any(LocalDate.class)))
            .thenReturn(Arrays.asList());
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenReturn(testLeaveRequest);

        // Act
        LeaveRequestDto result = leaveService.submitLeaveRequest(leaveRequestDto);

        // Assert
        assertNotNull(result);
        verify(leaveBalanceRepository, never()).findByEmployeeIdAndLeaveTypeAndYear(anyLong(), anyString(), anyInt());
    }

    @Test
    @DisplayName("Test export approved leave data")
    public void testExportApprovedLeave_ValidDateRange_Success() {
        // Arrange
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().plusDays(30);
        testLeaveRequest.setStatus("APPROVED");
        when(leaveRequestRepository.findApprovedByDateRange(startDate, endDate))
            .thenReturn(Arrays.asList(testLeaveRequest));

        // Act
        String csvContent = leaveService.exportApprovedLeave(startDate, endDate);

        // Assert
        assertNotNull(csvContent);
        assertTrue(csvContent.contains("Employee ID"));
        assertTrue(csvContent.contains("Leave Type"));
        assertTrue(csvContent.contains("Start Date"));
    }

    @Test
    @DisplayName("Test leave balance accrual at year end")
    public void testAccrueLeaveBalance_YearEnd_Success() {
        // Arrange
        when(leaveBalanceRepository.findAll()).thenReturn(Arrays.asList(testLeaveBalance));
        when(leaveBalanceRepository.save(any(LeaveBalance.class))).thenReturn(testLeaveBalance);

        // Act
        leaveService.accrueLeaveBalances();

        // Assert
        verify(leaveBalanceRepository, atLeast(1)).save(any(LeaveBalance.class));
    }
}