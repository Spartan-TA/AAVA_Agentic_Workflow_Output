package com.warehouse.employee.service;

import com.warehouse.employee.domain.Employee;
import com.warehouse.employee.domain.LeaveRequest;
import com.warehouse.employee.domain.LeaveType;
import com.warehouse.employee.domain.LeaveStatus;
import com.warehouse.employee.dto.LeaveRequestDTO;
import com.warehouse.employee.repository.LeaveRequestRepository;
import com.warehouse.employee.repository.EmployeeRepository;
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
import static org.mockito.Mockito.*;

/**
 * Comprehensive JUnit test suite for LeaveService
 * Tests cover leave requests, approvals, denials, accrual balances, and edge cases
 */
@DisplayName("Leave Service Tests")
public class LeaveServiceTest {

    @Mock
    private LeaveRequestRepository leaveRequestRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private LeaveService leaveService;

    private Employee testEmployee;
    private LeaveRequest testLeaveRequest;
    private LeaveRequestDTO leaveRequestDTO;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Setup test employee
        testEmployee = new Employee();
        testEmployee.setId(1L);
        testEmployee.setBadgeId("EMP001");
        testEmployee.setName("John Doe");

        // Setup leave request
        testLeaveRequest = new LeaveRequest();
        testLeaveRequest.setId(1L);
        testLeaveRequest.setEmployee(testEmployee);
        testLeaveRequest.setType(LeaveType.PTO);
        testLeaveRequest.setStartDate(LocalDate.now().plusDays(7));
        testLeaveRequest.setEndDate(LocalDate.now().plusDays(9));
        testLeaveRequest.setStatus(LeaveStatus.REQUESTED);
        testLeaveRequest.setReason("Vacation");

        // Setup leave request DTO
        leaveRequestDTO = new LeaveRequestDTO();
        leaveRequestDTO.setEmployeeId(1L);
        leaveRequestDTO.setType("PTO");
        leaveRequestDTO.setStartDate(LocalDate.now().plusDays(7));
        leaveRequestDTO.setEndDate(LocalDate.now().plusDays(9));
        leaveRequestDTO.setReason("Vacation");
    }

    // ========== REQUEST LEAVE TESTS ==========

    @Test
    @DisplayName("Test request leave - valid data - success")
    public void testRequestLeave_ValidData_Success() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenReturn(testLeaveRequest);

        // Act
        LeaveRequestDTO result = leaveService.requestLeave(1L, leaveRequestDTO);

        // Assert
        assertNotNull(result);
        assertEquals("PTO", result.getType());
        assertEquals(LeaveStatus.REQUESTED.toString(), result.getStatus());
        verify(leaveRequestRepository, times(1)).save(any(LeaveRequest.class));
    }

    @Test
    @DisplayName("Test request leave - null employee ID - throws exception")
    public void testRequestLeave_NullEmployeeId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            leaveService.requestLeave(null, leaveRequestDTO);
        });
    }

    @Test
    @DisplayName("Test request leave - non-existent employee - throws exception")
    public void testRequestLeave_NonExistentEmployee_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            leaveService.requestLeave(999L, leaveRequestDTO);
        });
    }

    @Test
    @DisplayName("Test request leave - null start date - throws exception")
    public void testRequestLeave_NullStartDate_ThrowsException() {
        // Arrange
        leaveRequestDTO.setStartDate(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            leaveService.requestLeave(1L, leaveRequestDTO);
        });
    }

    @Test
    @DisplayName("Test request leave - null end date - throws exception")
    public void testRequestLeave_NullEndDate_ThrowsException() {
        // Arrange
        leaveRequestDTO.setEndDate(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            leaveService.requestLeave(1L, leaveRequestDTO);
        });
    }

    @Test
    @DisplayName("Test request leave - end date before start date - throws exception")
    public void testRequestLeave_EndDateBeforeStartDate_ThrowsException() {
        // Arrange
        leaveRequestDTO.setStartDate(LocalDate.now().plusDays(10));
        leaveRequestDTO.setEndDate(LocalDate.now().plusDays(5));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            leaveService.requestLeave(1L, leaveRequestDTO);
        });
    }

    @Test
    @DisplayName("Test request leave - past start date - throws exception")
    public void testRequestLeave_PastStartDate_ThrowsException() {
        // Arrange
        leaveRequestDTO.setStartDate(LocalDate.now().minusDays(1));
        leaveRequestDTO.setEndDate(LocalDate.now().plusDays(2));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            leaveService.requestLeave(1L, leaveRequestDTO);
        });
    }

    @Test
    @DisplayName("Test request leave - invalid leave type - throws exception")
    public void testRequestLeave_InvalidLeaveType_ThrowsException() {
        // Arrange
        leaveRequestDTO.setType("INVALID_TYPE");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            leaveService.requestLeave(1L, leaveRequestDTO);
        });
    }

    @Test
    @DisplayName("Test request leave - empty reason - throws exception")
    public void testRequestLeave_EmptyReason_ThrowsException() {
        // Arrange
        leaveRequestDTO.setReason("");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            leaveService.requestLeave(1L, leaveRequestDTO);
        });
    }

    @Test
    @DisplayName("Test request leave - overlapping dates - throws exception")
    public void testRequestLeave_OverlappingDates_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(leaveRequestRepository.findOverlappingLeaves(any(), any(), any()))
                .thenReturn(Arrays.asList(testLeaveRequest));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            leaveService.requestLeave(1L, leaveRequestDTO);
        });
    }

    @Test
    @DisplayName("Test request leave - insufficient balance - throws exception")
    public void testRequestLeave_InsufficientBalance_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(leaveService.getLeaveBalance(1L, LeaveType.PTO)).thenReturn(1.0);
        leaveRequestDTO.setStartDate(LocalDate.now().plusDays(1));
        leaveRequestDTO.setEndDate(LocalDate.now().plusDays(5));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            leaveService.requestLeave(1L, leaveRequestDTO);
        });
    }

    // ========== APPROVE LEAVE TESTS ==========

    @Test
    @DisplayName("Test approve leave - valid request - success")
    public void testApproveLeave_ValidRequest_Success() {
        // Arrange
        when(leaveRequestRepository.findById(1L)).thenReturn(Optional.of(testLeaveRequest));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenReturn(testLeaveRequest);

        // Act
        LeaveRequestDTO result = leaveService.approveLeave(1L);

        // Assert
        assertNotNull(result);
        assertEquals(LeaveStatus.APPROVED.toString(), result.getStatus());
        verify(leaveRequestRepository, times(1)).save(any(LeaveRequest.class));
    }

    @Test
    @DisplayName("Test approve leave - null request ID - throws exception")
    public void testApproveLeave_NullRequestId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            leaveService.approveLeave(null);
        });
    }

    @Test
    @DisplayName("Test approve leave - non-existent request - throws exception")
    public void testApproveLeave_NonExistentRequest_ThrowsException() {
        // Arrange
        when(leaveRequestRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            leaveService.approveLeave(999L);
        });
    }

    @Test
    @DisplayName("Test approve leave - already approved - throws exception")
    public void testApproveLeave_AlreadyApproved_ThrowsException() {
        // Arrange
        testLeaveRequest.setStatus(LeaveStatus.APPROVED);
        when(leaveRequestRepository.findById(1L)).thenReturn(Optional.of(testLeaveRequest));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            leaveService.approveLeave(1L);
        });
    }

    @Test
    @DisplayName("Test approve leave - already denied - throws exception")
    public void testApproveLeave_AlreadyDenied_ThrowsException() {
        // Arrange
        testLeaveRequest.setStatus(LeaveStatus.DENIED);
        when(leaveRequestRepository.findById(1L)).thenReturn(Optional.of(testLeaveRequest));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            leaveService.approveLeave(1L);
        });
    }

    // ========== DENY LEAVE TESTS ==========

    @Test
    @DisplayName("Test deny leave - valid request - success")
    public void testDenyLeave_ValidRequest_Success() {
        // Arrange
        when(leaveRequestRepository.findById(1L)).thenReturn(Optional.of(testLeaveRequest));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenReturn(testLeaveRequest);

        // Act
        LeaveRequestDTO result = leaveService.denyLeave(1L, "Insufficient staffing");

        // Assert
        assertNotNull(result);
        assertEquals(LeaveStatus.DENIED.toString(), result.getStatus());
        verify(leaveRequestRepository, times(1)).save(any(LeaveRequest.class));
    }

    @Test
    @DisplayName("Test deny leave - null reason - throws exception")
    public void testDenyLeave_NullReason_ThrowsException() {
        // Arrange
        when(leaveRequestRepository.findById(1L)).thenReturn(Optional.of(testLeaveRequest));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            leaveService.denyLeave(1L, null);
        });
    }

    @Test
    @DisplayName("Test deny leave - empty reason - throws exception")
    public void testDenyLeave_EmptyReason_ThrowsException() {
        // Arrange
        when(leaveRequestRepository.findById(1L)).thenReturn(Optional.of(testLeaveRequest));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            leaveService.denyLeave(1L, "");
        });
    }

    // ========== CANCEL LEAVE TESTS ==========

    @Test
    @DisplayName("Test cancel leave - valid request - success")
    public void testCancelLeave_ValidRequest_Success() {
        // Arrange
        testLeaveRequest.setStatus(LeaveStatus.APPROVED);
        when(leaveRequestRepository.findById(1L)).thenReturn(Optional.of(testLeaveRequest));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenReturn(testLeaveRequest);

        // Act
        LeaveRequestDTO result = leaveService.cancelLeave(1L);

        // Assert
        assertNotNull(result);
        assertEquals(LeaveStatus.CANCELLED.toString(), result.getStatus());
    }

    @Test
    @DisplayName("Test cancel leave - past start date - throws exception")
    public void testCancelLeave_PastStartDate_ThrowsException() {
        // Arrange
        testLeaveRequest.setStartDate(LocalDate.now().minusDays(1));
        when(leaveRequestRepository.findById(1L)).thenReturn(Optional.of(testLeaveRequest));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            leaveService.cancelLeave(1L);
        });
    }

    // ========== LEAVE BALANCE TESTS ==========

    @Test
    @DisplayName("Test get leave balance - PTO - success")
    public void testGetLeaveBalance_PTO_Success() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));

        // Act
        double balance = leaveService.getLeaveBalance(1L, LeaveType.PTO);

        // Assert
        assertTrue(balance >= 0);
    }

    @Test
    @DisplayName("Test get leave balance - sick leave - success")
    public void testGetLeaveBalance_SickLeave_Success() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));

        // Act
        double balance = leaveService.getLeaveBalance(1L, LeaveType.SICK);

        // Assert
        assertTrue(balance >= 0);
    }

    @Test
    @DisplayName("Test get leave balance - unpaid leave - returns unlimited")
    public void testGetLeaveBalance_UnpaidLeave_ReturnsUnlimited() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));

        // Act
        double balance = leaveService.getLeaveBalance(1L, LeaveType.UNPAID);

        // Assert
        assertEquals(Double.MAX_VALUE, balance);
    }

    @Test
    @DisplayName("Test get leave balance - null employee ID - throws exception")
    public void testGetLeaveBalance_NullEmployeeId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            leaveService.getLeaveBalance(null, LeaveType.PTO);
        });
    }

    // ========== ACCRUAL TESTS ==========

    @Test
    @DisplayName("Test accrue leave - monthly accrual - success")
    public void testAccrueLeave_MonthlyAccrual_Success() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));

        // Act
        leaveService.accrueLeave(1L, LeaveType.PTO, 1.67);

        // Assert
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    @DisplayName("Test accrue leave - negative amount - throws exception")
    public void testAccrueLeave_NegativeAmount_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            leaveService.accrueLeave(1L, LeaveType.PTO, -1.0);
        });
    }

    @Test
    @DisplayName("Test accrue leave - zero amount - throws exception")
    public void testAccrueLeave_ZeroAmount_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            leaveService.accrueLeave(1L, LeaveType.PTO, 0.0);
        });
    }

    // ========== GET EMPLOYEE LEAVES TESTS ==========

    @Test
    @DisplayName("Test get employee leaves - success")
    public void testGetEmployeeLeaves_Success() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(leaveRequestRepository.findByEmployee(testEmployee))
                .thenReturn(Arrays.asList(testLeaveRequest));

        // Act
        List<LeaveRequestDTO> leaves = leaveService.getEmployeeLeaves(1L);

        // Assert
        assertNotNull(leaves);
        assertEquals(1, leaves.size());
    }

    @Test
    @DisplayName("Test get employee leaves - no leaves - returns empty list")
    public void testGetEmployeeLeaves_NoLeaves_ReturnsEmptyList() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(leaveRequestRepository.findByEmployee(testEmployee))
                .thenReturn(Arrays.asList());

        // Act
        List<LeaveRequestDTO> leaves = leaveService.getEmployeeLeaves(1L);

        // Assert
        assertNotNull(leaves);
        assertTrue(leaves.isEmpty());
    }

    // ========== BOUNDARY CONDITION TESTS ==========

    @Test
    @DisplayName("Test request leave - same day leave - success")
    public void testRequestLeave_SameDayLeave_Success() {
        // Arrange
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        leaveRequestDTO.setStartDate(tomorrow);
        leaveRequestDTO.setEndDate(tomorrow);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenReturn(testLeaveRequest);

        // Act
        LeaveRequestDTO result = leaveService.requestLeave(1L, leaveRequestDTO);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Test request leave - maximum duration - success")
    public void testRequestLeave_MaximumDuration_Success() {
        // Arrange
        leaveRequestDTO.setStartDate(LocalDate.now().plusDays(1));
        leaveRequestDTO.setEndDate(LocalDate.now().plusDays(30));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenReturn(testLeaveRequest);

        // Act
        LeaveRequestDTO result = leaveService.requestLeave(1L, leaveRequestDTO);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Test request leave - all leave types - success")
    public void testRequestLeave_AllLeaveTypes_Success() {
        // Test PTO
        leaveRequestDTO.setType("PTO");
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenReturn(testLeaveRequest);
        assertNotNull(leaveService.requestLeave(1L, leaveRequestDTO));

        // Test SICK
        leaveRequestDTO.setType("SICK");
        assertNotNull(leaveService.requestLeave(1L, leaveRequestDTO));

        // Test UNPAID
        leaveRequestDTO.setType("UNPAID");
        assertNotNull(leaveService.requestLeave(1L, leaveRequestDTO));
    }
}