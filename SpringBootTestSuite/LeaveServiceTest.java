package com.warehouse.ems.service;

import com.warehouse.ems.entity.Employee;
import com.warehouse.ems.entity.Leave;
import com.warehouse.ems.exception.InsufficientLeaveBalanceException;
import com.warehouse.ems.exception.ResourceNotFoundException;
import com.warehouse.ems.repository.EmployeeRepository;
import com.warehouse.ems.repository.LeaveRepository;
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
 * Comprehensive JUnit test suite for LeaveService
 * Tests cover leave requests, approvals, balance tracking, and edge cases
 */
@ExtendWith(MockitoExtension.class)
public class LeaveServiceTest {

    @Mock
    private LeaveRepository leaveRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private LeaveService leaveService;

    private Employee testEmployee;
    private Leave testLeave;

    @BeforeEach
    public void setUp() {
        testEmployee = new Employee();
        testEmployee.setId(1L);
        testEmployee.setBadgeId("EMP001");
        testEmployee.setFirstName("John");
        testEmployee.setLastName("Doe");
        testEmployee.setPtoBalance(15.0);
        testEmployee.setSickBalance(10.0);

        testLeave = new Leave();
        testLeave.setId(1L);
        testLeave.setEmployee(testEmployee);
        testLeave.setLeaveType("PTO");
        testLeave.setStartDate(LocalDate.now().plusDays(7));
        testLeave.setEndDate(LocalDate.now().plusDays(9));
        testLeave.setDaysRequested(3.0);
        testLeave.setStatus("PENDING");
        testLeave.setReason("Vacation");
    }

    // ========== CREATE LEAVE REQUEST TESTS ==========

    @Test
    public void testCreateLeaveRequest_ValidRequest_Success() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(leaveRepository.save(any(Leave.class))).thenReturn(testLeave);

        // Act
        Leave result = leaveService.createLeaveRequest(1L, "PTO", 
                LocalDate.now().plusDays(7), LocalDate.now().plusDays(9), "Vacation");

        // Assert
        assertNotNull(result);
        assertEquals("PTO", result.getLeaveType());
        assertEquals("PENDING", result.getStatus());
        assertEquals(3.0, result.getDaysRequested());
        verify(leaveRepository, times(1)).save(any(Leave.class));
    }

    @Test
    public void testCreateLeaveRequest_InsufficientBalance_ThrowsException() {
        // Arrange
        testEmployee.setPtoBalance(2.0); // Less than requested 3 days
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));

        // Act & Assert
        assertThrows(InsufficientLeaveBalanceException.class, () -> {
            leaveService.createLeaveRequest(1L, "PTO", 
                    LocalDate.now().plusDays(7), LocalDate.now().plusDays(9), "Vacation");
        });
        verify(leaveRepository, never()).save(any(Leave.class));
    }

    @Test
    public void testCreateLeaveRequest_NonExistentEmployee_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            leaveService.createLeaveRequest(999L, "PTO", 
                    LocalDate.now().plusDays(7), LocalDate.now().plusDays(9), "Vacation");
        });
    }

    @Test
    public void testCreateLeaveRequest_NullEmployeeId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            leaveService.createLeaveRequest(null, "PTO", 
                    LocalDate.now().plusDays(7), LocalDate.now().plusDays(9), "Vacation");
        });
    }

    @Test
    public void testCreateLeaveRequest_NullLeaveType_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            leaveService.createLeaveRequest(1L, null, 
                    LocalDate.now().plusDays(7), LocalDate.now().plusDays(9), "Vacation");
        });
    }

    @Test
    public void testCreateLeaveRequest_InvalidLeaveType_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            leaveService.createLeaveRequest(1L, "INVALID_TYPE", 
                    LocalDate.now().plusDays(7), LocalDate.now().plusDays(9), "Vacation");
        });
    }

    @Test
    public void testCreateLeaveRequest_EndDateBeforeStartDate_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            leaveService.createLeaveRequest(1L, "PTO", 
                    LocalDate.now().plusDays(9), LocalDate.now().plusDays(7), "Vacation");
        });
    }

    @Test
    public void testCreateLeaveRequest_PastStartDate_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            leaveService.createLeaveRequest(1L, "PTO", 
                    LocalDate.now().minusDays(1), LocalDate.now().plusDays(2), "Vacation");
        });
    }

    @Test
    public void testCreateLeaveRequest_EmptyReason_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            leaveService.createLeaveRequest(1L, "PTO", 
                    LocalDate.now().plusDays(7), LocalDate.now().plusDays(9), "");
        });
    }

    @Test
    public void testCreateLeaveRequest_SickLeave_Success() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        testLeave.setLeaveType("SICK");
        when(leaveRepository.save(any(Leave.class))).thenReturn(testLeave);

        // Act
        Leave result = leaveService.createLeaveRequest(1L, "SICK", 
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(2), "Illness");

        // Assert
        assertNotNull(result);
        assertEquals("SICK", result.getLeaveType());
        verify(leaveRepository, times(1)).save(any(Leave.class));
    }

    @Test
    public void testCreateLeaveRequest_UnpaidLeave_Success() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        testLeave.setLeaveType("UNPAID");
        when(leaveRepository.save(any(Leave.class))).thenReturn(testLeave);

        // Act
        Leave result = leaveService.createLeaveRequest(1L, "UNPAID", 
                LocalDate.now().plusDays(7), LocalDate.now().plusDays(9), "Personal");

        // Assert
        assertNotNull(result);
        assertEquals("UNPAID", result.getLeaveType());
        verify(leaveRepository, times(1)).save(any(Leave.class));
    }

    // ========== APPROVE LEAVE TESTS ==========

    @Test
    public void testApproveLeave_ValidRequest_Success() {
        // Arrange
        when(leaveRepository.findById(1L)).thenReturn(Optional.of(testLeave));
        when(leaveRepository.save(any(Leave.class))).thenReturn(testLeave);

        // Act
        Leave result = leaveService.approveLeave(1L, 2L, "Approved");

        // Assert
        assertNotNull(result);
        assertEquals("APPROVED", result.getStatus());
        assertEquals(2L, result.getApprovedBy());
        assertNotNull(result.getApprovedDate());
        verify(leaveRepository, times(1)).save(any(Leave.class));
    }

    @Test
    public void testApproveLeave_NonExistentLeave_ThrowsException() {
        // Arrange
        when(leaveRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            leaveService.approveLeave(999L, 2L, "Approved");
        });
    }

    @Test
    public void testApproveLeave_AlreadyApproved_ThrowsException() {
        // Arrange
        testLeave.setStatus("APPROVED");
        when(leaveRepository.findById(1L)).thenReturn(Optional.of(testLeave));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            leaveService.approveLeave(1L, 2L, "Approved");
        });
    }

    @Test
    public void testApproveLeave_NullLeaveId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            leaveService.approveLeave(null, 2L, "Approved");
        });
    }

    @Test
    public void testApproveLeave_NullApproverId_ThrowsException() {
        // Arrange
        when(leaveRepository.findById(1L)).thenReturn(Optional.of(testLeave));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            leaveService.approveLeave(1L, null, "Approved");
        });
    }

    @Test
    public void testApproveLeave_DeductsBalance_Success() {
        // Arrange
        double initialBalance = testEmployee.getPtoBalance();
        when(leaveRepository.findById(1L)).thenReturn(Optional.of(testLeave));
        when(leaveRepository.save(any(Leave.class))).thenReturn(testLeave);
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        leaveService.approveLeave(1L, 2L, "Approved");

        // Assert
        assertEquals(initialBalance - testLeave.getDaysRequested(), testEmployee.getPtoBalance());
        verify(employeeRepository, times(1)).save(testEmployee);
    }

    // ========== REJECT LEAVE TESTS ==========

    @Test
    public void testRejectLeave_ValidRequest_Success() {
        // Arrange
        when(leaveRepository.findById(1L)).thenReturn(Optional.of(testLeave));
        when(leaveRepository.save(any(Leave.class))).thenReturn(testLeave);

        // Act
        Leave result = leaveService.rejectLeave(1L, 2L, "Insufficient coverage");

        // Assert
        assertNotNull(result);
        assertEquals("REJECTED", result.getStatus());
        assertEquals(2L, result.getApprovedBy());
        assertEquals("Insufficient coverage", result.getComments());
        verify(leaveRepository, times(1)).save(any(Leave.class));
    }

    @Test
    public void testRejectLeave_NonExistentLeave_ThrowsException() {
        // Arrange
        when(leaveRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            leaveService.rejectLeave(999L, 2L, "Rejected");
        });
    }

    @Test
    public void testRejectLeave_AlreadyRejected_ThrowsException() {
        // Arrange
        testLeave.setStatus("REJECTED");
        when(leaveRepository.findById(1L)).thenReturn(Optional.of(testLeave));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            leaveService.rejectLeave(1L, 2L, "Rejected");
        });
    }

    @Test
    public void testRejectLeave_DoesNotDeductBalance() {
        // Arrange
        double initialBalance = testEmployee.getPtoBalance();
        when(leaveRepository.findById(1L)).thenReturn(Optional.of(testLeave));
        when(leaveRepository.save(any(Leave.class))).thenReturn(testLeave);

        // Act
        leaveService.rejectLeave(1L, 2L, "Rejected");

        // Assert
        assertEquals(initialBalance, testEmployee.getPtoBalance());
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    // ========== GET LEAVE REQUESTS TESTS ==========

    @Test
    public void testGetLeaveRequestsByEmployee_ValidEmployee_ReturnsLeaves() {
        // Arrange
        List<Leave> leaves = Arrays.asList(testLeave);
        when(leaveRepository.findByEmployeeId(1L)).thenReturn(leaves);

        // Act
        List<Leave> result = leaveService.getLeaveRequestsByEmployee(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testEmployee.getId(), result.get(0).getEmployee().getId());
    }

    @Test
    public void testGetLeaveRequestsByEmployee_NoLeaves_ReturnsEmptyList() {
        // Arrange
        when(leaveRepository.findByEmployeeId(1L)).thenReturn(Arrays.asList());

        // Act
        List<Leave> result = leaveService.getLeaveRequestsByEmployee(1L);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetLeaveRequestsByEmployee_NullEmployeeId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            leaveService.getLeaveRequestsByEmployee(null);
        });
    }

    @Test
    public void testGetPendingLeaveRequests_ReturnsPendingOnly() {
        // Arrange
        List<Leave> pendingLeaves = Arrays.asList(testLeave);
        when(leaveRepository.findByStatus("PENDING")).thenReturn(pendingLeaves);

        // Act
        List<Leave> result = leaveService.getPendingLeaveRequests();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("PENDING", result.get(0).getStatus());
    }

    // ========== LEAVE BALANCE TESTS ==========

    @Test
    public void testGetLeaveBalance_ValidEmployee_ReturnsBalance() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));

        // Act
        double ptoBalance = leaveService.getPtoBalance(1L);
        double sickBalance = leaveService.getSickBalance(1L);

        // Assert
        assertEquals(15.0, ptoBalance);
        assertEquals(10.0, sickBalance);
    }

    @Test
    public void testGetLeaveBalance_NonExistentEmployee_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            leaveService.getPtoBalance(999L);
        });
    }

    @Test
    public void testAccrueLeave_ValidEmployee_IncreasesBalance() {
        // Arrange
        double initialBalance = testEmployee.getPtoBalance();
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        leaveService.accrueLeave(1L, "PTO", 1.25);

        // Assert
        assertEquals(initialBalance + 1.25, testEmployee.getPtoBalance());
        verify(employeeRepository, times(1)).save(testEmployee);
    }

    @Test
    public void testAccrueLeave_NegativeAmount_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            leaveService.accrueLeave(1L, "PTO", -1.0);
        });
    }

    @Test
    public void testAccrueLeave_ZeroAmount_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            leaveService.accrueLeave(1L, "PTO", 0.0);
        });
    }

    // ========== CANCEL LEAVE TESTS ==========

    @Test
    public void testCancelLeave_PendingLeave_Success() {
        // Arrange
        when(leaveRepository.findById(1L)).thenReturn(Optional.of(testLeave));
        when(leaveRepository.save(any(Leave.class))).thenReturn(testLeave);

        // Act
        Leave result = leaveService.cancelLeave(1L);

        // Assert
        assertNotNull(result);
        assertEquals("CANCELLED", result.getStatus());
        verify(leaveRepository, times(1)).save(any(Leave.class));
    }

    @Test
    public void testCancelLeave_ApprovedLeave_RestoresBalance() {
        // Arrange
        testLeave.setStatus("APPROVED");
        double initialBalance = testEmployee.getPtoBalance();
        when(leaveRepository.findById(1L)).thenReturn(Optional.of(testLeave));
        when(leaveRepository.save(any(Leave.class))).thenReturn(testLeave);
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        leaveService.cancelLeave(1L);

        // Assert
        assertEquals(initialBalance + testLeave.getDaysRequested(), testEmployee.getPtoBalance());
        verify(employeeRepository, times(1)).save(testEmployee);
    }

    @Test
    public void testCancelLeave_AlreadyCancelled_ThrowsException() {
        // Arrange
        testLeave.setStatus("CANCELLED");
        when(leaveRepository.findById(1L)).thenReturn(Optional.of(testLeave));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            leaveService.cancelLeave(1L);
        });
    }
}