package com.company.wms.leave.service;

import com.company.wms.employee.model.Employee;
import com.company.wms.employee.repository.EmployeeRepository;
import com.company.wms.exception.BusinessException;
import com.company.wms.exception.ResourceNotFoundException;
import com.company.wms.leave.dto.LeaveRequestDTO;
import com.company.wms.leave.model.LeaveBalance;
import com.company.wms.leave.model.LeaveRequest;
import com.company.wms.leave.model.LeaveStatus;
import com.company.wms.leave.model.LeaveType;
import com.company.wms.leave.repository.LeaveBalanceRepository;
import com.company.wms.leave.repository.LeaveRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive JUnit test suite for LeaveService
 * Covers leave requests, approvals, balance management, and edge cases
 */
@ExtendWith(MockitoExtension.class)
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
    private Employee supervisor;
    private LeaveRequest testLeaveRequest;
    private LeaveBalance testLeaveBalance;

    @BeforeEach
    void setUp() {
        // Setup test employee
        testEmployee = new Employee();
        testEmployee.setId(1L);
        testEmployee.setName("John Doe");
        testEmployee.setBadgeId("EMP001");
        testEmployee.setDepartment("Warehouse");

        // Setup supervisor
        supervisor = new Employee();
        supervisor.setId(2L);
        supervisor.setName("Jane Smith");
        supervisor.setBadgeId("SUP001");
        supervisor.setRole("SUPERVISOR");

        // Setup leave request
        testLeaveRequest = new LeaveRequest();
        testLeaveRequest.setId(1L);
        testLeaveRequest.setEmployee(testEmployee);
        testLeaveRequest.setType(LeaveType.PTO);
        testLeaveRequest.setStartDate(LocalDate.now().plusDays(7));
        testLeaveRequest.setEndDate(LocalDate.now().plusDays(9));
        testLeaveRequest.setTotalDays(3);
        testLeaveRequest.setStatus(LeaveStatus.REQUESTED);
        testLeaveRequest.setReason("Family vacation");

        // Setup leave balance
        testLeaveBalance = new LeaveBalance();
        testLeaveBalance.setId(1L);
        testLeaveBalance.setEmployee(testEmployee);
        testLeaveBalance.setLeaveType(LeaveType.PTO);
        testLeaveBalance.setAccruedDays(new BigDecimal("15.00"));
        testLeaveBalance.setUsedDays(new BigDecimal("5.00"));
        testLeaveBalance.setRemainingDays(new BigDecimal("10.00"));
        testLeaveBalance.setYear(LocalDate.now().getYear());
    }

    // ========== REQUEST LEAVE TESTS ==========

    @Test
    void requestLeave_ValidInput_ReturnsLeaveRequestDTO() {
        // Arrange
        LeaveRequestDTO dto = new LeaveRequestDTO();
        dto.setEmployeeId(1L);
        dto.setLeaveType(LeaveType.PTO);
        dto.setStartDate(LocalDate.now().plusDays(7));
        dto.setEndDate(LocalDate.now().plusDays(9));
        dto.setReason("Family vacation");

        when(employeeRepository.findById(1L))
            .thenReturn(Optional.of(testEmployee));
        when(leaveBalanceRepository.findByEmployeeAndLeaveTypeAndYear(any(), any(), anyInt()))
            .thenReturn(Optional.of(testLeaveBalance));
        when(leaveRequestRepository.save(any(LeaveRequest.class)))
            .thenReturn(testLeaveRequest);

        // Act
        LeaveRequestDTO result = leaveService.requestLeave(dto);

        // Assert
        assertNotNull(result);
        assertEquals(LeaveStatus.REQUESTED, result.getStatus());
        assertEquals(3, result.getTotalDays());
        verify(leaveRequestRepository, times(1)).save(any(LeaveRequest.class));
    }

    @Test
    void requestLeave_EmployeeNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        LeaveRequestDTO dto = new LeaveRequestDTO();
        dto.setEmployeeId(999L);
        dto.setLeaveType(LeaveType.PTO);
        dto.setStartDate(LocalDate.now().plusDays(7));
        dto.setEndDate(LocalDate.now().plusDays(9));

        when(employeeRepository.findById(999L))
            .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            leaveService.requestLeave(dto);
        });
    }

    @Test
    void requestLeave_InsufficientBalance_ThrowsBusinessException() {
        // Arrange
        LeaveRequestDTO dto = new LeaveRequestDTO();
        dto.setEmployeeId(1L);
        dto.setLeaveType(LeaveType.PTO);
        dto.setStartDate(LocalDate.now().plusDays(7));
        dto.setEndDate(LocalDate.now().plusDays(20)); // 14 days
        dto.setReason("Extended vacation");

        testLeaveBalance.setRemainingDays(new BigDecimal("10.00")); // Only 10 days available

        when(employeeRepository.findById(1L))
            .thenReturn(Optional.of(testEmployee));
        when(leaveBalanceRepository.findByEmployeeAndLeaveTypeAndYear(any(), any(), anyInt()))
            .thenReturn(Optional.of(testLeaveBalance));

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            leaveService.requestLeave(dto);
        });
    }

    @Test
    void requestLeave_StartDateAfterEndDate_ThrowsBusinessException() {
        // Arrange
        LeaveRequestDTO dto = new LeaveRequestDTO();
        dto.setEmployeeId(1L);
        dto.setLeaveType(LeaveType.PTO);
        dto.setStartDate(LocalDate.now().plusDays(10));
        dto.setEndDate(LocalDate.now().plusDays(5)); // End before start

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            leaveService.requestLeave(dto);
        });
    }

    @Test
    void requestLeave_PastStartDate_ThrowsBusinessException() {
        // Arrange
        LeaveRequestDTO dto = new LeaveRequestDTO();
        dto.setEmployeeId(1L);
        dto.setLeaveType(LeaveType.PTO);
        dto.setStartDate(LocalDate.now().minusDays(1));
        dto.setEndDate(LocalDate.now().plusDays(2));

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            leaveService.requestLeave(dto);
        });
    }

    @Test
    void requestLeave_NoBalanceRecord_ThrowsResourceNotFoundException() {
        // Arrange
        LeaveRequestDTO dto = new LeaveRequestDTO();
        dto.setEmployeeId(1L);
        dto.setLeaveType(LeaveType.PTO);
        dto.setStartDate(LocalDate.now().plusDays(7));
        dto.setEndDate(LocalDate.now().plusDays(9));

        when(employeeRepository.findById(1L))
            .thenReturn(Optional.of(testEmployee));
        when(leaveBalanceRepository.findByEmployeeAndLeaveTypeAndYear(any(), any(), anyInt()))
            .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            leaveService.requestLeave(dto);
        });
    }

    @Test
    void requestLeave_SickLeave_NoBalanceCheck() {
        // Arrange
        LeaveRequestDTO dto = new LeaveRequestDTO();
        dto.setEmployeeId(1L);
        dto.setLeaveType(LeaveType.SICK);
        dto.setStartDate(LocalDate.now().plusDays(1));
        dto.setEndDate(LocalDate.now().plusDays(3));
        dto.setReason("Medical appointment");

        testLeaveRequest.setType(LeaveType.SICK);

        when(employeeRepository.findById(1L))
            .thenReturn(Optional.of(testEmployee));
        when(leaveRequestRepository.save(any(LeaveRequest.class)))
            .thenReturn(testLeaveRequest);

        // Act
        LeaveRequestDTO result = leaveService.requestLeave(dto);

        // Assert
        assertNotNull(result);
        verify(leaveBalanceRepository, never()).findByEmployeeAndLeaveTypeAndYear(any(), any(), anyInt());
    }

    // ========== APPROVE LEAVE TESTS ==========

    @Test
    void approveLeave_ValidInput_UpdatesStatusAndBalance() {
        // Arrange
        when(leaveRequestRepository.findById(1L))
            .thenReturn(Optional.of(testLeaveRequest));
        when(employeeRepository.findById(2L))
            .thenReturn(Optional.of(supervisor));
        when(leaveBalanceRepository.findByEmployeeAndLeaveTypeAndYear(any(), any(), anyInt()))
            .thenReturn(Optional.of(testLeaveBalance));
        when(leaveRequestRepository.save(any(LeaveRequest.class)))
            .thenReturn(testLeaveRequest);
        when(leaveBalanceRepository.save(any(LeaveBalance.class)))
            .thenReturn(testLeaveBalance);

        // Act
        LeaveRequestDTO result = leaveService.approveLeave(1L, 2L);

        // Assert
        assertNotNull(result);
        assertEquals(LeaveStatus.APPROVED, testLeaveRequest.getStatus());
        assertNotNull(testLeaveRequest.getApprovedAt());
        verify(leaveBalanceRepository, times(1)).save(any(LeaveBalance.class));
    }

    @Test
    void approveLeave_RequestNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(leaveRequestRepository.findById(999L))
            .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            leaveService.approveLeave(999L, 2L);
        });
    }

    @Test
    void approveLeave_ApproverNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(leaveRequestRepository.findById(1L))
            .thenReturn(Optional.of(testLeaveRequest));
        when(employeeRepository.findById(999L))
            .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            leaveService.approveLeave(1L, 999L);
        });
    }

    @Test
    void approveLeave_AlreadyApproved_ThrowsBusinessException() {
        // Arrange
        testLeaveRequest.setStatus(LeaveStatus.APPROVED);
        when(leaveRequestRepository.findById(1L))
            .thenReturn(Optional.of(testLeaveRequest));

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            leaveService.approveLeave(1L, 2L);
        });
    }

    @Test
    void approveLeave_AlreadyDenied_ThrowsBusinessException() {
        // Arrange
        testLeaveRequest.setStatus(LeaveStatus.DENIED);
        when(leaveRequestRepository.findById(1L))
            .thenReturn(Optional.of(testLeaveRequest));

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            leaveService.approveLeave(1L, 2L);
        });
    }

    // ========== DENY LEAVE TESTS ==========

    @Test
    void denyLeave_ValidInput_UpdatesStatusWithReason() {
        // Arrange
        String denialReason = "Insufficient staffing during requested period";
        
        when(leaveRequestRepository.findById(1L))
            .thenReturn(Optional.of(testLeaveRequest));
        when(employeeRepository.findById(2L))
            .thenReturn(Optional.of(supervisor));
        when(leaveRequestRepository.save(any(LeaveRequest.class)))
            .thenReturn(testLeaveRequest);

        // Act
        LeaveRequestDTO result = leaveService.denyLeave(1L, 2L, denialReason);

        // Assert
        assertNotNull(result);
        assertEquals(LeaveStatus.DENIED, testLeaveRequest.getStatus());
        assertEquals(denialReason, testLeaveRequest.getDenialReason());
        verify(leaveBalanceRepository, never()).save(any(LeaveBalance.class));
    }

    @Test
    void denyLeave_NullReason_ThrowsIllegalArgumentException() {
        // Arrange
        when(leaveRequestRepository.findById(1L))
            .thenReturn(Optional.of(testLeaveRequest));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            leaveService.denyLeave(1L, 2L, null);
        });
    }

    @Test
    void denyLeave_EmptyReason_ThrowsIllegalArgumentException() {
        // Arrange
        when(leaveRequestRepository.findById(1L))
            .thenReturn(Optional.of(testLeaveRequest));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            leaveService.denyLeave(1L, 2L, "");
        });
    }

    // ========== CANCEL LEAVE TESTS ==========

    @Test
    void cancelLeave_ValidInput_UpdatesStatusAndRestoresBalance() {
        // Arrange
        testLeaveRequest.setStatus(LeaveStatus.APPROVED);
        
        when(leaveRequestRepository.findById(1L))
            .thenReturn(Optional.of(testLeaveRequest));
        when(leaveBalanceRepository.findByEmployeeAndLeaveTypeAndYear(any(), any(), anyInt()))
            .thenReturn(Optional.of(testLeaveBalance));
        when(leaveRequestRepository.save(any(LeaveRequest.class)))
            .thenReturn(testLeaveRequest);
        when(leaveBalanceRepository.save(any(LeaveBalance.class)))
            .thenReturn(testLeaveBalance);

        // Act
        LeaveRequestDTO result = leaveService.cancelLeave(1L);

        // Assert
        assertNotNull(result);
        assertEquals(LeaveStatus.CANCELLED, testLeaveRequest.getStatus());
        verify(leaveBalanceRepository, times(1)).save(any(LeaveBalance.class));
    }

    @Test
    void cancelLeave_RequestedStatus_NoBalanceRestore() {
        // Arrange
        testLeaveRequest.setStatus(LeaveStatus.REQUESTED);
        
        when(leaveRequestRepository.findById(1L))
            .thenReturn(Optional.of(testLeaveRequest));
        when(leaveRequestRepository.save(any(LeaveRequest.class)))
            .thenReturn(testLeaveRequest);

        // Act
        LeaveRequestDTO result = leaveService.cancelLeave(1L);

        // Assert
        assertNotNull(result);
        assertEquals(LeaveStatus.CANCELLED, testLeaveRequest.getStatus());
        verify(leaveBalanceRepository, never()).save(any(LeaveBalance.class));
    }

    @Test
    void cancelLeave_AlreadyCancelled_ThrowsBusinessException() {
        // Arrange
        testLeaveRequest.setStatus(LeaveStatus.CANCELLED);
        when(leaveRequestRepository.findById(1L))
            .thenReturn(Optional.of(testLeaveRequest));

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            leaveService.cancelLeave(1L);
        });
    }

    // ========== GET LEAVE BALANCE TESTS ==========

    @Test
    void getLeaveBalance_ValidInput_ReturnsBalance() {
        // Arrange
        when(employeeRepository.findById(1L))
            .thenReturn(Optional.of(testEmployee));
        when(leaveBalanceRepository.findByEmployeeAndLeaveTypeAndYear(any(), eq(LeaveType.PTO), anyInt()))
            .thenReturn(Optional.of(testLeaveBalance));

        // Act
        LeaveBalance result = leaveService.getLeaveBalance(1L, LeaveType.PTO);

        // Assert
        assertNotNull(result);
        assertEquals(new BigDecimal("10.00"), result.getRemainingDays());
    }

    @Test
    void getLeaveBalance_EmployeeNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(employeeRepository.findById(999L))
            .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            leaveService.getLeaveBalance(999L, LeaveType.PTO);
        });
    }

    @Test
    void getLeaveBalance_NoBalanceRecord_ThrowsResourceNotFoundException() {
        // Arrange
        when(employeeRepository.findById(1L))
            .thenReturn(Optional.of(testEmployee));
        when(leaveBalanceRepository.findByEmployeeAndLeaveTypeAndYear(any(), any(), anyInt()))
            .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            leaveService.getLeaveBalance(1L, LeaveType.PTO);
        });
    }

    // ========== BOUNDARY CONDITION TESTS ==========

    @Test
    void requestLeave_SingleDay_Success() {
        // Arrange
        LeaveRequestDTO dto = new LeaveRequestDTO();
        dto.setEmployeeId(1L);
        dto.setLeaveType(LeaveType.PTO);
        dto.setStartDate(LocalDate.now().plusDays(7));
        dto.setEndDate(LocalDate.now().plusDays(7)); // Same day
        dto.setReason("Personal appointment");

        when(employeeRepository.findById(1L))
            .thenReturn(Optional.of(testEmployee));
        when(leaveBalanceRepository.findByEmployeeAndLeaveTypeAndYear(any(), any(), anyInt()))
            .thenReturn(Optional.of(testLeaveBalance));
        when(leaveRequestRepository.save(any(LeaveRequest.class)))
            .thenReturn(testLeaveRequest);

        // Act
        LeaveRequestDTO result = leaveService.requestLeave(dto);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalDays());
    }

    @Test
    void requestLeave_MaximumDuration_Success() {
        // Arrange
        LeaveRequestDTO dto = new LeaveRequestDTO();
        dto.setEmployeeId(1L);
        dto.setLeaveType(LeaveType.PTO);
        dto.setStartDate(LocalDate.now().plusDays(7));
        dto.setEndDate(LocalDate.now().plusDays(16)); // 10 days
        dto.setReason("Extended vacation");

        when(employeeRepository.findById(1L))
            .thenReturn(Optional.of(testEmployee));
        when(leaveBalanceRepository.findByEmployeeAndLeaveTypeAndYear(any(), any(), anyInt()))
            .thenReturn(Optional.of(testLeaveBalance));
        when(leaveRequestRepository.save(any(LeaveRequest.class)))
            .thenReturn(testLeaveRequest);

        // Act
        LeaveRequestDTO result = leaveService.requestLeave(dto);

        // Assert
        assertNotNull(result);
    }

    @Test
    void requestLeave_ExactBalanceMatch_Success() {
        // Arrange
        LeaveRequestDTO dto = new LeaveRequestDTO();
        dto.setEmployeeId(1L);
        dto.setLeaveType(LeaveType.PTO);
        dto.setStartDate(LocalDate.now().plusDays(7));
        dto.setEndDate(LocalDate.now().plusDays(16)); // 10 days
        dto.setReason("Using all remaining balance");

        testLeaveBalance.setRemainingDays(new BigDecimal("10.00"));

        when(employeeRepository.findById(1L))
            .thenReturn(Optional.of(testEmployee));
        when(leaveBalanceRepository.findByEmployeeAndLeaveTypeAndYear(any(), any(), anyInt()))
            .thenReturn(Optional.of(testLeaveBalance));
        when(leaveRequestRepository.save(any(LeaveRequest.class)))
            .thenReturn(testLeaveRequest);

        // Act
        LeaveRequestDTO result = leaveService.requestLeave(dto);

        // Assert
        assertNotNull(result);
    }

    @Test
    void requestLeave_ZeroBalance_ThrowsBusinessException() {
        // Arrange
        LeaveRequestDTO dto = new LeaveRequestDTO();
        dto.setEmployeeId(1L);
        dto.setLeaveType(LeaveType.PTO);
        dto.setStartDate(LocalDate.now().plusDays(7));
        dto.setEndDate(LocalDate.now().plusDays(9));

        testLeaveBalance.setRemainingDays(BigDecimal.ZERO);

        when(employeeRepository.findById(1L))
            .thenReturn(Optional.of(testEmployee));
        when(leaveBalanceRepository.findByEmployeeAndLeaveTypeAndYear(any(), any(), anyInt()))
            .thenReturn(Optional.of(testLeaveBalance));

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            leaveService.requestLeave(dto);
        });
    }

    @Test
    void approveLeave_UpdatesBalanceCorrectly() {
        // Arrange
        BigDecimal initialUsed = testLeaveBalance.getUsedDays();
        BigDecimal initialRemaining = testLeaveBalance.getRemainingDays();
        
        when(leaveRequestRepository.findById(1L))
            .thenReturn(Optional.of(testLeaveRequest));
        when(employeeRepository.findById(2L))
            .thenReturn(Optional.of(supervisor));
        when(leaveBalanceRepository.findByEmployeeAndLeaveTypeAndYear(any(), any(), anyInt()))
            .thenReturn(Optional.of(testLeaveBalance));
        when(leaveRequestRepository.save(any(LeaveRequest.class)))
            .thenReturn(testLeaveRequest);
        when(leaveBalanceRepository.save(any(LeaveBalance.class)))
            .thenReturn(testLeaveBalance);

        // Act
        leaveService.approveLeave(1L, 2L);

        // Assert
        assertEquals(initialUsed.add(new BigDecimal("3.00")), testLeaveBalance.getUsedDays());
        assertEquals(initialRemaining.subtract(new BigDecimal("3.00")), testLeaveBalance.getRemainingDays());
    }

    @Test
    void requestLeave_MaxLengthReason_Success() {
        // Arrange
        String longReason = "A".repeat(1000);
        LeaveRequestDTO dto = new LeaveRequestDTO();
        dto.setEmployeeId(1L);
        dto.setLeaveType(LeaveType.PTO);
        dto.setStartDate(LocalDate.now().plusDays(7));
        dto.setEndDate(LocalDate.now().plusDays(9));
        dto.setReason(longReason);

        when(employeeRepository.findById(1L))
            .thenReturn(Optional.of(testEmployee));
        when(leaveBalanceRepository.findByEmployeeAndLeaveTypeAndYear(any(), any(), anyInt()))
            .thenReturn(Optional.of(testLeaveBalance));
        when(leaveRequestRepository.save(any(LeaveRequest.class)))
            .thenReturn(testLeaveRequest);

        // Act
        LeaveRequestDTO result = leaveService.requestLeave(dto);

        // Assert
        assertNotNull(result);
    }
}