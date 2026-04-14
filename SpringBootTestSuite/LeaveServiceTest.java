package com.wms.ems.service;

import com.wms.ems.dto.LeaveRequestCreateDTO;
import com.wms.ems.dto.LeaveRequestDTO;
import com.wms.ems.dto.LeaveApprovalDTO;
import com.wms.ems.dto.LeaveBalanceDTO;
import com.wms.ems.entity.LeaveRequest;
import com.wms.ems.entity.LeaveBalance;
import com.wms.ems.entity.Employee;
import com.wms.ems.entity.enums.LeaveType;
import com.wms.ems.entity.enums.LeaveStatus;
import com.wms.ems.exception.EntityNotFoundException;
import com.wms.ems.exception.ValidationException;
import com.wms.ems.exception.ConflictException;
import com.wms.ems.repository.LeaveRequestRepository;
import com.wms.ems.repository.LeaveBalanceRepository;
import com.wms.ems.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive JUnit test suite for LeaveService.
 * Tests cover leave requests, approvals, balance management, and all edge cases.
 * 
 * @author EMS Test Suite Generator
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Leave Service Tests")
class LeaveServiceTest {

    @Mock
    private LeaveRequestRepository leaveRequestRepository;

    @Mock
    private LeaveBalanceRepository leaveBalanceRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private LeaveService leaveService;

    private Employee testEmployee;
    private LeaveRequest testLeaveRequest;
    private LeaveBalance testLeaveBalance;
    private LeaveRequestCreateDTO createDTO;
    private LeaveApprovalDTO approvalDTO;

    @BeforeEach
    void setUp() {
        // Setup test employee
        testEmployee = new Employee();
        testEmployee.setId(1L);
        testEmployee.setBadgeId("EMP001");
        testEmployee.setFirstName("John");
        testEmployee.setLastName("Doe");

        // Setup test leave balance
        testLeaveBalance = new LeaveBalance();
        testLeaveBalance.setId(1L);
        testLeaveBalance.setEmployee(testEmployee);
        testLeaveBalance.setLeaveType(LeaveType.VACATION);
        testLeaveBalance.setTotalDays(20.0);
        testLeaveBalance.setUsedDays(5.0);
        testLeaveBalance.setRemainingDays(15.0);

        // Setup test leave request
        testLeaveRequest = new LeaveRequest();
        testLeaveRequest.setId(1L);
        testLeaveRequest.setEmployee(testEmployee);
        testLeaveRequest.setLeaveType(LeaveType.VACATION);
        testLeaveRequest.setStartDate(LocalDate.now().plusDays(7));
        testLeaveRequest.setEndDate(LocalDate.now().plusDays(10));
        testLeaveRequest.setTotalDays(4.0);
        testLeaveRequest.setReason("Family vacation");
        testLeaveRequest.setStatus(LeaveStatus.PENDING);

        // Setup create DTO
        createDTO = LeaveRequestCreateDTO.builder()
                .employeeId(1L)
                .leaveType(LeaveType.VACATION)
                .startDate(LocalDate.now().plusDays(7))
                .endDate(LocalDate.now().plusDays(10))
                .reason("Family vacation")
                .build();

        // Setup approval DTO
        approvalDTO = LeaveApprovalDTO.builder()
                .leaveRequestId(1L)
                .approved(true)
                .approverComments("Approved")
                .build();
    }

    // ==================== CREATE LEAVE REQUEST TESTS ====================

    @Test
    @DisplayName("Create Leave Request - Valid Input - Success")
    void testCreateLeaveRequest_ValidInput_Success() {
        // Arrange
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.of(testEmployee));
        when(leaveBalanceRepository.findByEmployeeAndLeaveType(anyLong(), any(LeaveType.class)))
                .thenReturn(Optional.of(testLeaveBalance));
        when(leaveRequestRepository.findOverlappingRequests(anyLong(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Arrays.asList());
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenReturn(testLeaveRequest);

        // Act
        LeaveRequestDTO result = leaveService.createLeaveRequest(createDTO);

        // Assert
        assertNotNull(result);
        assertEquals(LeaveType.VACATION, result.getLeaveType());
        assertEquals(LeaveStatus.PENDING, result.getStatus());
        verify(leaveRequestRepository, times(1)).save(any(LeaveRequest.class));
    }

    @Test
    @DisplayName("Create Leave Request - Invalid Employee ID - Throws EntityNotFoundException")
    void testCreateLeaveRequest_InvalidEmployeeId_ThrowsEntityNotFoundException() {
        // Arrange
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> {
            leaveService.createLeaveRequest(createDTO);
        });
        verify(leaveRequestRepository, never()).save(any(LeaveRequest.class));
    }

    @Test
    @DisplayName("Create Leave Request - Insufficient Balance - Throws ValidationException")
    void testCreateLeaveRequest_InsufficientBalance_ThrowsValidationException() {
        // Arrange
        testLeaveBalance.setRemainingDays(2.0);
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.of(testEmployee));
        when(leaveBalanceRepository.findByEmployeeAndLeaveType(anyLong(), any(LeaveType.class)))
                .thenReturn(Optional.of(testLeaveBalance));

        // Act & Assert
        assertThrows(ValidationException.class, () -> {
            leaveService.createLeaveRequest(createDTO);
        });
    }

    @Test
    @DisplayName("Create Leave Request - Overlapping Dates - Throws ConflictException")
    void testCreateLeaveRequest_OverlappingDates_ThrowsConflictException() {
        // Arrange
        LeaveRequest existingRequest = new LeaveRequest();
        existingRequest.setStartDate(LocalDate.now().plusDays(8));
        existingRequest.setEndDate(LocalDate.now().plusDays(12));
        existingRequest.setStatus(LeaveStatus.APPROVED);

        when(employeeRepository.findById(anyLong())).thenReturn(Optional.of(testEmployee));
        when(leaveBalanceRepository.findByEmployeeAndLeaveType(anyLong(), any(LeaveType.class)))
                .thenReturn(Optional.of(testLeaveBalance));
        when(leaveRequestRepository.findOverlappingRequests(anyLong(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Arrays.asList(existingRequest));

        // Act & Assert
        assertThrows(ConflictException.class, () -> {
            leaveService.createLeaveRequest(createDTO);
        });
    }

    @Test
    @DisplayName("Create Leave Request - Null Employee ID - Throws ValidationException")
    void testCreateLeaveRequest_NullEmployeeId_ThrowsValidationException() {
        // Arrange
        createDTO.setEmployeeId(null);

        // Act & Assert
        assertThrows(ValidationException.class, () -> {
            leaveService.createLeaveRequest(createDTO);
        });
    }

    @Test
    @DisplayName("Create Leave Request - Null Leave Type - Throws ValidationException")
    void testCreateLeaveRequest_NullLeaveType_ThrowsValidationException() {
        // Arrange
        createDTO.setLeaveType(null);

        // Act & Assert
        assertThrows(ValidationException.class, () -> {
            leaveService.createLeaveRequest(createDTO);
        });
    }

    @Test
    @DisplayName("Create Leave Request - Null Start Date - Throws ValidationException")
    void testCreateLeaveRequest_NullStartDate_ThrowsValidationException() {
        // Arrange
        createDTO.setStartDate(null);

        // Act & Assert
        assertThrows(ValidationException.class, () -> {
            leaveService.createLeaveRequest(createDTO);
        });
    }

    @Test
    @DisplayName("Create Leave Request - Null End Date - Throws ValidationException")
    void testCreateLeaveRequest_NullEndDate_ThrowsValidationException() {
        // Arrange
        createDTO.setEndDate(null);

        // Act & Assert
        assertThrows(ValidationException.class, () -> {
            leaveService.createLeaveRequest(createDTO);
        });
    }

    @Test
    @DisplayName("Create Leave Request - End Date Before Start Date - Throws ValidationException")
    void testCreateLeaveRequest_EndDateBeforeStartDate_ThrowsValidationException() {
        // Arrange
        createDTO.setStartDate(LocalDate.now().plusDays(10));
        createDTO.setEndDate(LocalDate.now().plusDays(7));

        // Act & Assert
        assertThrows(ValidationException.class, () -> {
            leaveService.createLeaveRequest(createDTO);
        });
    }

    @Test
    @DisplayName("Create Leave Request - Past Start Date - Throws ValidationException")
    void testCreateLeaveRequest_PastStartDate_ThrowsValidationException() {
        // Arrange
        createDTO.setStartDate(LocalDate.now().minusDays(1));
        createDTO.setEndDate(LocalDate.now().plusDays(3));

        // Act & Assert
        assertThrows(ValidationException.class, () -> {
            leaveService.createLeaveRequest(createDTO);
        });
    }

    @Test
    @DisplayName("Create Leave Request - Empty Reason - Throws ValidationException")
    void testCreateLeaveRequest_EmptyReason_ThrowsValidationException() {
        // Arrange
        createDTO.setReason("");

        // Act & Assert
        assertThrows(ValidationException.class, () -> {
            leaveService.createLeaveRequest(createDTO);
        });
    }

    @Test
    @DisplayName("Create Leave Request - Sick Leave Without Balance Check - Success")
    void testCreateLeaveRequest_SickLeaveWithoutBalanceCheck_Success() {
        // Arrange
        createDTO.setLeaveType(LeaveType.SICK);
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.of(testEmployee));
        when(leaveRequestRepository.findOverlappingRequests(anyLong(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Arrays.asList());
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenReturn(testLeaveRequest);

        // Act
        LeaveRequestDTO result = leaveService.createLeaveRequest(createDTO);

        // Assert
        assertNotNull(result);
        verify(leaveBalanceRepository, never()).findByEmployeeAndLeaveType(anyLong(), any(LeaveType.class));
    }

    // ==================== APPROVE/DENY LEAVE REQUEST TESTS ====================

    @Test
    @DisplayName("Approve Leave Request - Valid Input - Success")
    void testApproveLeaveRequest_ValidInput_Success() {
        // Arrange
        when(leaveRequestRepository.findById(anyLong())).thenReturn(Optional.of(testLeaveRequest));
        when(leaveBalanceRepository.findByEmployeeAndLeaveType(anyLong(), any(LeaveType.class)))
                .thenReturn(Optional.of(testLeaveBalance));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenReturn(testLeaveRequest);
        when(leaveBalanceRepository.save(any(LeaveBalance.class))).thenReturn(testLeaveBalance);

        // Act
        LeaveRequestDTO result = leaveService.approveOrDenyLeaveRequest(approvalDTO);

        // Assert
        assertNotNull(result);
        assertEquals(LeaveStatus.APPROVED, result.getStatus());
        verify(leaveBalanceRepository, times(1)).save(any(LeaveBalance.class));
    }

    @Test
    @DisplayName("Deny Leave Request - Valid Input - Success")
    void testDenyLeaveRequest_ValidInput_Success() {
        // Arrange
        approvalDTO.setApproved(false);
        approvalDTO.setApproverComments("Denied due to staffing needs");
        when(leaveRequestRepository.findById(anyLong())).thenReturn(Optional.of(testLeaveRequest));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenReturn(testLeaveRequest);

        // Act
        LeaveRequestDTO result = leaveService.approveOrDenyLeaveRequest(approvalDTO);

        // Assert
        assertNotNull(result);
        assertEquals(LeaveStatus.REJECTED, result.getStatus());
        verify(leaveBalanceRepository, never()).save(any(LeaveBalance.class));
    }

    @Test
    @DisplayName("Approve Leave Request - Invalid Request ID - Throws EntityNotFoundException")
    void testApproveLeaveRequest_InvalidRequestId_ThrowsEntityNotFoundException() {
        // Arrange
        when(leaveRequestRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> {
            leaveService.approveOrDenyLeaveRequest(approvalDTO);
        });
    }

    @Test
    @DisplayName("Approve Leave Request - Already Approved - Throws ValidationException")
    void testApproveLeaveRequest_AlreadyApproved_ThrowsValidationException() {
        // Arrange
        testLeaveRequest.setStatus(LeaveStatus.APPROVED);
        when(leaveRequestRepository.findById(anyLong())).thenReturn(Optional.of(testLeaveRequest));

        // Act & Assert
        assertThrows(ValidationException.class, () -> {
            leaveService.approveOrDenyLeaveRequest(approvalDTO);
        });
    }

    @Test
    @DisplayName("Approve Leave Request - Already Rejected - Throws ValidationException")
    void testApproveLeaveRequest_AlreadyRejected_ThrowsValidationException() {
        // Arrange
        testLeaveRequest.setStatus(LeaveStatus.REJECTED);
        when(leaveRequestRepository.findById(anyLong())).thenReturn(Optional.of(testLeaveRequest));

        // Act & Assert
        assertThrows(ValidationException.class, () -> {
            leaveService.approveOrDenyLeaveRequest(approvalDTO);
        });
    }

    @Test
    @DisplayName("Approve Leave Request - Null Approver Comments - Throws ValidationException")
    void testApproveLeaveRequest_NullApproverComments_ThrowsValidationException() {
        // Arrange
        approvalDTO.setApproverComments(null);

        // Act & Assert
        assertThrows(ValidationException.class, () -> {
            leaveService.approveOrDenyLeaveRequest(approvalDTO);
        });
    }

    // ==================== GET LEAVE BALANCE TESTS ====================

    @Test
    @DisplayName("Get Leave Balance - Valid Employee and Type - Success")
    void testGetLeaveBalance_ValidEmployeeAndType_Success() {
        // Arrange
        when(leaveBalanceRepository.findByEmployeeAndLeaveType(anyLong(), any(LeaveType.class)))
                .thenReturn(Optional.of(testLeaveBalance));

        // Act
        LeaveBalanceDTO result = leaveService.getLeaveBalance(1L, LeaveType.VACATION);

        // Assert
        assertNotNull(result);
        assertEquals(20.0, result.getTotalDays());
        assertEquals(5.0, result.getUsedDays());
        assertEquals(15.0, result.getRemainingDays());
    }

    @Test
    @DisplayName("Get Leave Balance - No Balance Record - Returns Zero Balance")
    void testGetLeaveBalance_NoBalanceRecord_ReturnsZeroBalance() {
        // Arrange
        when(leaveBalanceRepository.findByEmployeeAndLeaveType(anyLong(), any(LeaveType.class)))
                .thenReturn(Optional.empty());

        // Act
        LeaveBalanceDTO result = leaveService.getLeaveBalance(1L, LeaveType.VACATION);

        // Assert
        assertNotNull(result);
        assertEquals(0.0, result.getTotalDays());
        assertEquals(0.0, result.getUsedDays());
        assertEquals(0.0, result.getRemainingDays());
    }

    @Test
    @DisplayName("Get All Leave Balances For Employee - Valid Employee - Success")
    void testGetAllLeaveBalancesForEmployee_ValidEmployee_Success() {
        // Arrange
        when(leaveBalanceRepository.findByEmployee(anyLong()))
                .thenReturn(Arrays.asList(testLeaveBalance));

        // Act
        List<LeaveBalanceDTO> results = leaveService.getAllLeaveBalancesForEmployee(1L);

        // Assert
        assertNotNull(results);
        assertEquals(1, results.size());
    }

    // ==================== GET LEAVE REQUESTS TESTS ====================

    @Test
    @DisplayName("Get Leave Request By ID - Valid ID - Success")
    void testGetLeaveRequestById_ValidId_Success() {
        // Arrange
        when(leaveRequestRepository.findById(anyLong())).thenReturn(Optional.of(testLeaveRequest));

        // Act
        LeaveRequestDTO result = leaveService.getLeaveRequestById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(LeaveType.VACATION, result.getLeaveType());
    }

    @Test
    @DisplayName("Get Leave Request By ID - Invalid ID - Throws EntityNotFoundException")
    void testGetLeaveRequestById_InvalidId_ThrowsEntityNotFoundException() {
        // Arrange
        when(leaveRequestRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> {
            leaveService.getLeaveRequestById(999L);
        });
    }

    @Test
    @DisplayName("Get Leave Requests By Employee - Valid Employee - Success")
    void testGetLeaveRequestsByEmployee_ValidEmployee_Success() {
        // Arrange
        when(leaveRequestRepository.findByEmployee(anyLong()))
                .thenReturn(Arrays.asList(testLeaveRequest));

        // Act
        List<LeaveRequestDTO> results = leaveService.getLeaveRequestsByEmployee(1L);

        // Assert
        assertNotNull(results);
        assertEquals(1, results.size());
    }

    @Test
    @DisplayName("Get Pending Leave Requests - Returns Pending Only")
    void testGetPendingLeaveRequests_ReturnsPendingOnly() {
        // Arrange
        when(leaveRequestRepository.findByStatus(LeaveStatus.PENDING))
                .thenReturn(Arrays.asList(testLeaveRequest));

        // Act
        List<LeaveRequestDTO> results = leaveService.getPendingLeaveRequests();

        // Assert
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(LeaveStatus.PENDING, results.get(0).getStatus());
    }

    // ==================== CANCEL LEAVE REQUEST TESTS ====================

    @Test
    @DisplayName("Cancel Leave Request - Valid Pending Request - Success")
    void testCancelLeaveRequest_ValidPendingRequest_Success() {
        // Arrange
        when(leaveRequestRepository.findById(anyLong())).thenReturn(Optional.of(testLeaveRequest));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenReturn(testLeaveRequest);

        // Act
        LeaveRequestDTO result = leaveService.cancelLeaveRequest(1L);

        // Assert
        assertNotNull(result);
        assertEquals(LeaveStatus.CANCELLED, result.getStatus());
    }

    @Test
    @DisplayName("Cancel Leave Request - Already Approved - Restores Balance")
    void testCancelLeaveRequest_AlreadyApproved_RestoresBalance() {
        // Arrange
        testLeaveRequest.setStatus(LeaveStatus.APPROVED);
        when(leaveRequestRepository.findById(anyLong())).thenReturn(Optional.of(testLeaveRequest));
        when(leaveBalanceRepository.findByEmployeeAndLeaveType(anyLong(), any(LeaveType.class)))
                .thenReturn(Optional.of(testLeaveBalance));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenReturn(testLeaveRequest);
        when(leaveBalanceRepository.save(any(LeaveBalance.class))).thenReturn(testLeaveBalance);

        // Act
        LeaveRequestDTO result = leaveService.cancelLeaveRequest(1L);

        // Assert
        assertNotNull(result);
        verify(leaveBalanceRepository, times(1)).save(any(LeaveBalance.class));
    }

    @Test
    @DisplayName("Cancel Leave Request - Already Cancelled - Throws ValidationException")
    void testCancelLeaveRequest_AlreadyCancelled_ThrowsValidationException() {
        // Arrange
        testLeaveRequest.setStatus(LeaveStatus.CANCELLED);
        when(leaveRequestRepository.findById(anyLong())).thenReturn(Optional.of(testLeaveRequest));

        // Act & Assert
        assertThrows(ValidationException.class, () -> {
            leaveService.cancelLeaveRequest(1L);
        });
    }

    // ==================== BOUNDARY CONDITION TESTS ====================

    @Test
    @DisplayName("Create Leave Request - Single Day Leave - Success")
    void testCreateLeaveRequest_SingleDayLeave_Success() {
        // Arrange
        createDTO.setStartDate(LocalDate.now().plusDays(7));
        createDTO.setEndDate(LocalDate.now().plusDays(7));
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.of(testEmployee));
        when(leaveBalanceRepository.findByEmployeeAndLeaveType(anyLong(), any(LeaveType.class)))
                .thenReturn(Optional.of(testLeaveBalance));
        when(leaveRequestRepository.findOverlappingRequests(anyLong(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Arrays.asList());
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenReturn(testLeaveRequest);

        // Act
        LeaveRequestDTO result = leaveService.createLeaveRequest(createDTO);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Create Leave Request - Maximum Duration - Success")
    void testCreateLeaveRequest_MaximumDuration_Success() {
        // Arrange
        createDTO.setStartDate(LocalDate.now().plusDays(7));
        createDTO.setEndDate(LocalDate.now().plusDays(21)); // 15 days
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.of(testEmployee));
        when(leaveBalanceRepository.findByEmployeeAndLeaveType(anyLong(), any(LeaveType.class)))
                .thenReturn(Optional.of(testLeaveBalance));
        when(leaveRequestRepository.findOverlappingRequests(anyLong(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Arrays.asList());
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenReturn(testLeaveRequest);

        // Act
        LeaveRequestDTO result = leaveService.createLeaveRequest(createDTO);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Create Leave Request - Exact Balance Match - Success")
    void testCreateLeaveRequest_ExactBalanceMatch_Success() {
        // Arrange
        testLeaveBalance.setRemainingDays(4.0);
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.of(testEmployee));
        when(leaveBalanceRepository.findByEmployeeAndLeaveType(anyLong(), any(LeaveType.class)))
                .thenReturn(Optional.of(testLeaveBalance));
        when(leaveRequestRepository.findOverlappingRequests(anyLong(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Arrays.asList());
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenReturn(testLeaveRequest);

        // Act
        LeaveRequestDTO result = leaveService.createLeaveRequest(createDTO);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Create Leave Request - Zero Remaining Balance - Throws ValidationException")
    void testCreateLeaveRequest_ZeroRemainingBalance_ThrowsValidationException() {
        // Arrange
        testLeaveBalance.setRemainingDays(0.0);
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.of(testEmployee));
        when(leaveBalanceRepository.findByEmployeeAndLeaveType(anyLong(), any(LeaveType.class)))
                .thenReturn(Optional.of(testLeaveBalance));

        // Act & Assert
        assertThrows(ValidationException.class, () -> {
            leaveService.createLeaveRequest(createDTO);
        });
    }

    // ==================== EDGE CASE TESTS ====================

    @Test
    @DisplayName("Create Leave Request - All Leave Types - Success")
    void testCreateLeaveRequest_AllLeaveTypes_Success() {
        // Test all leave types
        for (LeaveType leaveType : LeaveType.values()) {
            // Arrange
            createDTO.setLeaveType(leaveType);
            when(employeeRepository.findById(anyLong())).thenReturn(Optional.of(testEmployee));
            if (leaveType == LeaveType.VACATION) {
                when(leaveBalanceRepository.findByEmployeeAndLeaveType(anyLong(), any(LeaveType.class)))
                        .thenReturn(Optional.of(testLeaveBalance));
            }
            when(leaveRequestRepository.findOverlappingRequests(anyLong(), any(LocalDate.class), any(LocalDate.class)))
                    .thenReturn(Arrays.asList());
            when(leaveRequestRepository.save(any(LeaveRequest.class))).thenReturn(testLeaveRequest);

            // Act
            LeaveRequestDTO result = leaveService.createLeaveRequest(createDTO);

            // Assert
            assertNotNull(result);
        }
    }

    @Test
    @DisplayName("Create Leave Request - Reason With Special Characters - Success")
    void testCreateLeaveRequest_ReasonWithSpecialCharacters_Success() {
        // Arrange
        createDTO.setReason("Family emergency - urgent care needed!");
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.of(testEmployee));
        when(leaveBalanceRepository.findByEmployeeAndLeaveType(anyLong(), any(LeaveType.class)))
                .thenReturn(Optional.of(testLeaveBalance));
        when(leaveRequestRepository.findOverlappingRequests(anyLong(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Arrays.asList());
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenReturn(testLeaveRequest);

        // Act
        LeaveRequestDTO result = leaveService.createLeaveRequest(createDTO);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Create Leave Request - Very Long Reason - Success")
    void testCreateLeaveRequest_VeryLongReason_Success() {
        // Arrange
        String longReason = "A".repeat(500);
        createDTO.setReason(longReason);
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.of(testEmployee));
        when(leaveBalanceRepository.findByEmployeeAndLeaveType(anyLong(), any(LeaveType.class)))
                .thenReturn(Optional.of(testLeaveBalance));
        when(leaveRequestRepository.findOverlappingRequests(anyLong(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Arrays.asList());
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenReturn(testLeaveRequest);

        // Act
        LeaveRequestDTO result = leaveService.createLeaveRequest(createDTO);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Approve Leave Request - Multiple Concurrent Approvals - Handles Gracefully")
    void testApproveLeaveRequest_MultipleConcurrentApprovals_HandlesGracefully() {
        // Arrange
        when(leaveRequestRepository.findById(anyLong())).thenReturn(Optional.of(testLeaveRequest));
        when(leaveBalanceRepository.findByEmployeeAndLeaveType(anyLong(), any(LeaveType.class)))
                .thenReturn(Optional.of(testLeaveBalance));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenReturn(testLeaveRequest);
        when(leaveBalanceRepository.save(any(LeaveBalance.class))).thenReturn(testLeaveBalance);

        // Act
        LeaveRequestDTO result = leaveService.approveOrDenyLeaveRequest(approvalDTO);

        // Assert
        assertNotNull(result);
    }
}