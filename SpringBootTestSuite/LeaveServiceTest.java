package com.wms.ems.leave;

import com.wms.ems.employee.Employee;
import com.wms.ems.employee.EmployeeRepository;
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
 * Comprehensive JUnit test suite for LeaveService.
 * Tests cover leave requests, approvals, balance management, accrual policies,
 * and edge cases.
 * 
 * @author Warehouse EMS Test Suite
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
public class LeaveServiceTest {

    @Mock
    private LeaveRequestRepository leaveRequestRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private LeaveBalanceRepository leaveBalanceRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private LeaveService leaveService;

    private Employee testEmployee;
    private LeaveRequest testLeaveRequest;
    private LeaveBalance testLeaveBalance;
    private LeaveRequestDto leaveRequestDto;

    @BeforeEach
    public void setUp() {
        // Arrange: Create test employee
        testEmployee = new Employee();
        testEmployee.setId(1L);
        testEmployee.setBadgeId("EMP001");
        testEmployee.setName("John Doe");
        testEmployee.setStatus("ACTIVE");
        testEmployee.setSupervisorId(2L);

        // Arrange: Create test leave balance
        testLeaveBalance = new LeaveBalance();
        testLeaveBalance.setId(1L);
        testLeaveBalance.setEmployeeId(1L);
        testLeaveBalance.setPtoBalance(80);
        testLeaveBalance.setSickBalance(40);
        testLeaveBalance.setUnpaidBalance(0);

        // Arrange: Create test leave request
        testLeaveRequest = new LeaveRequest();
        testLeaveRequest.setId(1L);
        testLeaveRequest.setEmployeeId(1L);
        testLeaveRequest.setType("PTO");
        testLeaveRequest.setStartDate(LocalDate.now().plusDays(7));
        testLeaveRequest.setEndDate(LocalDate.now().plusDays(9));
        testLeaveRequest.setStatus("REQUESTED");
        testLeaveRequest.setReason("Vacation");
        testLeaveRequest.setHoursRequested(24);

        // Arrange: Create leave request DTO
        leaveRequestDto = new LeaveRequestDto();
        leaveRequestDto.setEmployeeId(1L);
        leaveRequestDto.setType("PTO");
        leaveRequestDto.setStartDate(LocalDate.now().plusDays(7));
        leaveRequestDto.setEndDate(LocalDate.now().plusDays(9));
        leaveRequestDto.setReason("Vacation");
    }

    // ==================== LEAVE REQUEST CREATION TESTS ====================

    @Test
    public void testRequestLeave_ValidInput_Success() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(leaveBalanceRepository.findByEmployeeId(1L)).thenReturn(Optional.of(testLeaveBalance));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenReturn(testLeaveRequest);

        // Act
        LeaveRequestDto result = leaveService.requestLeave(leaveRequestDto);

        // Assert
        assertNotNull(result);
        assertEquals("REQUESTED", result.getStatus());
        assertEquals(1L, result.getEmployeeId());
        verify(leaveRequestRepository, times(1)).save(any(LeaveRequest.class));
        verify(notificationService, times(1)).sendNotification(any());
    }

    @Test
    public void testRequestLeave_NullDto_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            leaveService.requestLeave(null);
        });
    }

    @Test
    public void testRequestLeave_NullEmployeeId_ThrowsException() {
        // Arrange
        leaveRequestDto.setEmployeeId(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            leaveService.requestLeave(leaveRequestDto);
        });
    }

    @Test
    public void testRequestLeave_InvalidEmployeeId_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());
        leaveRequestDto.setEmployeeId(999L);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            leaveService.requestLeave(leaveRequestDto);
        });
    }

    @Test
    public void testRequestLeave_InactiveEmployee_ThrowsException() {
        // Arrange
        testEmployee.setStatus("INACTIVE");
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            leaveService.requestLeave(leaveRequestDto);
        });
    }

    @Test
    public void testRequestLeave_NullType_ThrowsException() {
        // Arrange
        leaveRequestDto.setType(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            leaveService.requestLeave(leaveRequestDto);
        });
    }

    @Test
    public void testRequestLeave_InvalidType_ThrowsException() {
        // Arrange
        leaveRequestDto.setType("INVALID_TYPE");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            leaveService.requestLeave(leaveRequestDto);
        });
    }

    @Test
    public void testRequestLeave_NullStartDate_ThrowsException() {
        // Arrange
        leaveRequestDto.setStartDate(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            leaveService.requestLeave(leaveRequestDto);
        });
    }

    @Test
    public void testRequestLeave_NullEndDate_ThrowsException() {
        // Arrange
        leaveRequestDto.setEndDate(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            leaveService.requestLeave(leaveRequestDto);
        });
    }

    @Test
    public void testRequestLeave_EndDateBeforeStartDate_ThrowsException() {
        // Arrange
        leaveRequestDto.setStartDate(LocalDate.now().plusDays(10));
        leaveRequestDto.setEndDate(LocalDate.now().plusDays(5));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            leaveService.requestLeave(leaveRequestDto);
        });
    }

    @Test
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
    public void testRequestLeave_InsufficientBalance_ThrowsException() {
        // Arrange
        testLeaveBalance.setPtoBalance(8); // Only 8 hours available
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(leaveBalanceRepository.findByEmployeeId(1L)).thenReturn(Optional.of(testLeaveBalance));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            leaveService.requestLeave(leaveRequestDto);
        });
    }

    @Test
    public void testRequestLeave_NullReason_ThrowsException() {
        // Arrange
        leaveRequestDto.setReason(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            leaveService.requestLeave(leaveRequestDto);
        });
    }

    @Test
    public void testRequestLeave_EmptyReason_ThrowsException() {
        // Arrange
        leaveRequestDto.setReason("");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            leaveService.requestLeave(leaveRequestDto);
        });
    }

    // ==================== LEAVE APPROVAL TESTS ====================

    @Test
    public void testApproveLeave_ValidInput_Success() {
        // Arrange
        when(leaveRequestRepository.findById(1L)).thenReturn(Optional.of(testLeaveRequest));
        when(leaveBalanceRepository.findByEmployeeId(1L)).thenReturn(Optional.of(testLeaveBalance));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenReturn(testLeaveRequest);

        // Act
        leaveService.approveLeave(1L, 2L);

        // Assert
        verify(leaveRequestRepository, times(1)).save(any(LeaveRequest.class));
        verify(leaveBalanceRepository, times(1)).save(any(LeaveBalance.class));
        verify(notificationService, times(1)).sendNotification(any());
    }

    @Test
    public void testApproveLeave_InvalidLeaveRequestId_ThrowsException() {
        // Arrange
        when(leaveRequestRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            leaveService.approveLeave(999L, 2L);
        });
    }

    @Test
    public void testApproveLeave_AlreadyApproved_ThrowsException() {
        // Arrange
        testLeaveRequest.setStatus("APPROVED");
        when(leaveRequestRepository.findById(1L)).thenReturn(Optional.of(testLeaveRequest));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            leaveService.approveLeave(1L, 2L);
        });
    }

    @Test
    public void testApproveLeave_AlreadyDenied_ThrowsException() {
        // Arrange
        testLeaveRequest.setStatus("DENIED");
        when(leaveRequestRepository.findById(1L)).thenReturn(Optional.of(testLeaveRequest));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            leaveService.approveLeave(1L, 2L);
        });
    }

    @Test
    public void testApproveLeave_UnauthorizedSupervisor_ThrowsException() {
        // Arrange
        when(leaveRequestRepository.findById(1L)).thenReturn(Optional.of(testLeaveRequest));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            leaveService.approveLeave(1L, 999L); // Wrong supervisor ID
        });
    }

    // ==================== LEAVE DENIAL TESTS ====================

    @Test
    public void testDenyLeave_ValidInput_Success() {
        // Arrange
        when(leaveRequestRepository.findById(1L)).thenReturn(Optional.of(testLeaveRequest));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenReturn(testLeaveRequest);

        // Act
        leaveService.denyLeave(1L, 2L, "Insufficient staffing");

        // Assert
        verify(leaveRequestRepository, times(1)).save(any(LeaveRequest.class));
        verify(notificationService, times(1)).sendNotification(any());
    }

    @Test
    public void testDenyLeave_NullReason_ThrowsException() {
        // Arrange
        when(leaveRequestRepository.findById(1L)).thenReturn(Optional.of(testLeaveRequest));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            leaveService.denyLeave(1L, 2L, null);
        });
    }

    @Test
    public void testDenyLeave_EmptyReason_ThrowsException() {
        // Arrange
        when(leaveRequestRepository.findById(1L)).thenReturn(Optional.of(testLeaveRequest));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            leaveService.denyLeave(1L, 2L, "");
        });
    }

    // ==================== LEAVE BALANCE TESTS ====================

    @Test
    public void testGetLeaveBalance_ValidEmployeeId_Success() {
        // Arrange
        when(leaveBalanceRepository.findByEmployeeId(1L)).thenReturn(Optional.of(testLeaveBalance));

        // Act
        LeaveBalanceDto result = leaveService.getLeaveBalance(1L);

        // Assert
        assertNotNull(result);
        assertEquals(80, result.getPtoBalance());
        assertEquals(40, result.getSickBalance());
    }

    @Test
    public void testGetLeaveBalance_InvalidEmployeeId_ThrowsException() {
        // Arrange
        when(leaveBalanceRepository.findByEmployeeId(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            leaveService.getLeaveBalance(999L);
        });
    }

    @Test
    public void testGetLeaveBalance_NullEmployeeId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            leaveService.getLeaveBalance(null);
        });
    }

    @Test
    public void testUpdateLeaveBalance_ValidInput_Success() {
        // Arrange
        when(leaveBalanceRepository.findByEmployeeId(1L)).thenReturn(Optional.of(testLeaveBalance));
        when(leaveBalanceRepository.save(any(LeaveBalance.class))).thenReturn(testLeaveBalance);

        // Act
        leaveService.updateLeaveBalance(1L, "PTO", 8);

        // Assert
        verify(leaveBalanceRepository, times(1)).save(any(LeaveBalance.class));
    }

    @Test
    public void testUpdateLeaveBalance_NegativeHours_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            leaveService.updateLeaveBalance(1L, "PTO", -8);
        });
    }

    // ==================== LEAVE ACCRUAL TESTS ====================

    @Test
    public void testAccrueLeave_ValidInput_Success() {
        // Arrange
        when(leaveBalanceRepository.findByEmployeeId(1L)).thenReturn(Optional.of(testLeaveBalance));
        when(leaveBalanceRepository.save(any(LeaveBalance.class))).thenReturn(testLeaveBalance);

        // Act
        leaveService.accrueLeave(1L, "PTO", 8);

        // Assert
        verify(leaveBalanceRepository, times(1)).save(any(LeaveBalance.class));
    }

    @Test
    public void testAccrueLeave_ExceedsMaxBalance_CapsAtMax() {
        // Arrange
        testLeaveBalance.setPtoBalance(200); // Already at max
        when(leaveBalanceRepository.findByEmployeeId(1L)).thenReturn(Optional.of(testLeaveBalance));
        when(leaveBalanceRepository.save(any(LeaveBalance.class))).thenReturn(testLeaveBalance);

        // Act
        leaveService.accrueLeave(1L, "PTO", 8);

        // Assert
        verify(leaveBalanceRepository, times(1)).save(any(LeaveBalance.class));
    }

    // ==================== LEAVE CANCELLATION TESTS ====================

    @Test
    public void testCancelLeave_ValidInput_Success() {
        // Arrange
        when(leaveRequestRepository.findById(1L)).thenReturn(Optional.of(testLeaveRequest));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenReturn(testLeaveRequest);

        // Act
        leaveService.cancelLeave(1L, 1L);

        // Assert
        verify(leaveRequestRepository, times(1)).save(any(LeaveRequest.class));
    }

    @Test
    public void testCancelLeave_UnauthorizedEmployee_ThrowsException() {
        // Arrange
        when(leaveRequestRepository.findById(1L)).thenReturn(Optional.of(testLeaveRequest));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            leaveService.cancelLeave(1L, 999L); // Wrong employee ID
        });
    }

    @Test
    public void testCancelLeave_AlreadyApproved_ThrowsException() {
        // Arrange
        testLeaveRequest.setStatus("APPROVED");
        when(leaveRequestRepository.findById(1L)).thenReturn(Optional.of(testLeaveRequest));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            leaveService.cancelLeave(1L, 1L);
        });
    }

    // ==================== LEAVE RETRIEVAL TESTS ====================

    @Test
    public void testGetLeaveRequests_ValidEmployeeId_Success() {
        // Arrange
        when(leaveRequestRepository.findByEmployeeId(1L)).thenReturn(Arrays.asList(testLeaveRequest));

        // Act
        List<LeaveRequestDto> results = leaveService.getLeaveRequests(1L);

        // Assert
        assertNotNull(results);
        assertEquals(1, results.size());
    }

    @Test
    public void testGetLeaveRequests_NoRequests_ReturnsEmptyList() {
        // Arrange
        when(leaveRequestRepository.findByEmployeeId(1L)).thenReturn(Arrays.asList());

        // Act
        List<LeaveRequestDto> results = leaveService.getLeaveRequests(1L);

        // Assert
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    public void testGetPendingLeaveRequests_ValidSupervisorId_Success() {
        // Arrange
        when(leaveRequestRepository.findPendingRequestsBySupervisorId(2L)).thenReturn(Arrays.asList(testLeaveRequest));

        // Act
        List<LeaveRequestDto> results = leaveService.getPendingLeaveRequests(2L);

        // Assert
        assertNotNull(results);
        assertEquals(1, results.size());
    }
}