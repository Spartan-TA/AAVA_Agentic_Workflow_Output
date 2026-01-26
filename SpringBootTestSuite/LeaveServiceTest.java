package com.wms.ems.leave.service;

import com.wms.ems.leave.dto.LeaveRequestDTO;
import com.wms.ems.leave.dto.LeaveResponseDTO;
import com.wms.ems.leave.entity.LeaveRequest;
import com.wms.ems.leave.entity.LeaveBalance;
import com.wms.ems.leave.repository.LeaveRequestRepository;
import com.wms.ems.leave.repository.LeaveBalanceRepository;
import com.wms.ems.employee.entity.Employee;
import com.wms.ems.employee.repository.EmployeeRepository;
import com.wms.ems.exception.ResourceNotFoundException;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive JUnit test suite for LeaveService
 * Covers: Leave requests, approvals, accrual, balance tracking
 * Epic: E06 - Leave & Absence Management
 */
@ExtendWith(MockitoExtension.class)
public class LeaveServiceTest {

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
    private LeaveRequestDTO leaveRequestDTO;

    @BeforeEach
    public void setUp() {
        testEmployee = new Employee();
        testEmployee.setId(1L);
        testEmployee.setBadgeId("EMP001");
        testEmployee.setName("John Doe");
        testEmployee.setHireDate(LocalDate.of(2023, 1, 1));

        testLeaveRequest = new LeaveRequest();
        testLeaveRequest.setId(1L);
        testLeaveRequest.setEmployee(testEmployee);
        testLeaveRequest.setLeaveType("PTO");
        testLeaveRequest.setStartDate(LocalDate.of(2024, 6, 1));
        testLeaveRequest.setEndDate(LocalDate.of(2024, 6, 5));
        testLeaveRequest.setStatus("PENDING");
        testLeaveRequest.setReason("Vacation");

        testLeaveBalance = new LeaveBalance();
        testLeaveBalance.setId(1L);
        testLeaveBalance.setEmployee(testEmployee);
        testLeaveBalance.setPtoBalance(15.0);
        testLeaveBalance.setSickBalance(10.0);
        testLeaveBalance.setUnpaidBalance(0.0);

        leaveRequestDTO = new LeaveRequestDTO();
        leaveRequestDTO.setEmployeeId(1L);
        leaveRequestDTO.setLeaveType("PTO");
        leaveRequestDTO.setStartDate(LocalDate.of(2024, 6, 1));
        leaveRequestDTO.setEndDate(LocalDate.of(2024, 6, 5));
        leaveRequestDTO.setReason("Vacation");
    }

    // ========== CREATE LEAVE REQUEST TESTS ==========

    @Test
    public void testCreateLeaveRequest_ValidRequest_CreatesLeave() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(leaveBalanceRepository.findByEmployeeId(1L)).thenReturn(Optional.of(testLeaveBalance));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenReturn(testLeaveRequest);

        // Act
        LeaveResponseDTO result = leaveService.createLeaveRequest(leaveRequestDTO);

        // Assert
        assertNotNull(result);
        assertEquals("PTO", result.getLeaveType());
        assertEquals("PENDING", result.getStatus());
        verify(leaveRequestRepository, times(1)).save(any(LeaveRequest.class));
    }

    @Test
    public void testCreateLeaveRequest_NullRequest_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            leaveService.createLeaveRequest(null);
        });
    }

    @Test
    public void testCreateLeaveRequest_InvalidEmployeeId_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());
        leaveRequestDTO.setEmployeeId(999L);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            leaveService.createLeaveRequest(leaveRequestDTO);
        });
    }

    @Test
    public void testCreateLeaveRequest_InsufficientBalance_ThrowsException() {
        // Arrange
        testLeaveBalance.setPtoBalance(2.0); // Only 2 days available
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(leaveBalanceRepository.findByEmployeeId(1L)).thenReturn(Optional.of(testLeaveBalance));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            leaveService.createLeaveRequest(leaveRequestDTO);
        });
        assertTrue(exception.getMessage().contains("Insufficient leave balance"));
    }

    @Test
    public void testCreateLeaveRequest_EndDateBeforeStartDate_ThrowsException() {
        // Arrange
        leaveRequestDTO.setStartDate(LocalDate.of(2024, 6, 5));
        leaveRequestDTO.setEndDate(LocalDate.of(2024, 6, 1));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            leaveService.createLeaveRequest(leaveRequestDTO);
        });
    }

    @Test
    public void testCreateLeaveRequest_PastStartDate_ThrowsException() {
        // Arrange
        leaveRequestDTO.setStartDate(LocalDate.now().minusDays(1));
        leaveRequestDTO.setEndDate(LocalDate.now().plusDays(5));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            leaveService.createLeaveRequest(leaveRequestDTO);
        });
    }

    @Test
    public void testCreateLeaveRequest_EmptyReason_ThrowsException() {
        // Arrange
        leaveRequestDTO.setReason("");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            leaveService.createLeaveRequest(leaveRequestDTO);
        });
    }

    @Test
    public void testCreateLeaveRequest_InvalidLeaveType_ThrowsException() {
        // Arrange
        leaveRequestDTO.setLeaveType("INVALID_TYPE");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            leaveService.createLeaveRequest(leaveRequestDTO);
        });
    }

    // ========== APPROVE LEAVE REQUEST TESTS ==========

    @Test
    public void testApproveLeaveRequest_ValidRequest_ApprovesLeave() {
        // Arrange
        when(leaveRequestRepository.findById(1L)).thenReturn(Optional.of(testLeaveRequest));
        when(leaveBalanceRepository.findByEmployeeId(1L)).thenReturn(Optional.of(testLeaveBalance));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenReturn(testLeaveRequest);

        // Act
        LeaveResponseDTO result = leaveService.approveLeaveRequest(1L, "Approved by manager");

        // Assert
        assertNotNull(result);
        assertEquals("APPROVED", result.getStatus());
        verify(leaveBalanceRepository, times(1)).save(any(LeaveBalance.class));
    }

    @Test
    public void testApproveLeaveRequest_InvalidId_ThrowsException() {
        // Arrange
        when(leaveRequestRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            leaveService.approveLeaveRequest(999L, "Approved");
        });
    }

    @Test
    public void testApproveLeaveRequest_AlreadyApproved_ThrowsException() {
        // Arrange
        testLeaveRequest.setStatus("APPROVED");
        when(leaveRequestRepository.findById(1L)).thenReturn(Optional.of(testLeaveRequest));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            leaveService.approveLeaveRequest(1L, "Approved");
        });
    }

    @Test
    public void testApproveLeaveRequest_NullComment_ThrowsException() {
        // Arrange
        when(leaveRequestRepository.findById(1L)).thenReturn(Optional.of(testLeaveRequest));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            leaveService.approveLeaveRequest(1L, null);
        });
    }

    // ========== DENY LEAVE REQUEST TESTS ==========

    @Test
    public void testDenyLeaveRequest_ValidRequest_DeniesLeave() {
        // Arrange
        when(leaveRequestRepository.findById(1L)).thenReturn(Optional.of(testLeaveRequest));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenReturn(testLeaveRequest);

        // Act
        LeaveResponseDTO result = leaveService.denyLeaveRequest(1L, "Denied due to staffing");

        // Assert
        assertNotNull(result);
        assertEquals("DENIED", result.getStatus());
        verify(leaveRequestRepository, times(1)).save(any(LeaveRequest.class));
    }

    @Test
    public void testDenyLeaveRequest_InvalidId_ThrowsException() {
        // Arrange
        when(leaveRequestRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            leaveService.denyLeaveRequest(999L, "Denied");
        });
    }

    @Test
    public void testDenyLeaveRequest_EmptyReason_ThrowsException() {
        // Arrange
        when(leaveRequestRepository.findById(1L)).thenReturn(Optional.of(testLeaveRequest));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            leaveService.denyLeaveRequest(1L, "");
        });
    }

    // ========== GET LEAVE BALANCE TESTS ==========

    @Test
    public void testGetLeaveBalance_ValidEmployeeId_ReturnsBalance() {
        // Arrange
        when(leaveBalanceRepository.findByEmployeeId(1L)).thenReturn(Optional.of(testLeaveBalance));

        // Act
        LeaveBalance result = leaveService.getLeaveBalance(1L);

        // Assert
        assertNotNull(result);
        assertEquals(15.0, result.getPtoBalance());
        assertEquals(10.0, result.getSickBalance());
    }

    @Test
    public void testGetLeaveBalance_InvalidEmployeeId_ThrowsException() {
        // Arrange
        when(leaveBalanceRepository.findByEmployeeId(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
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

    // ========== CALCULATE ACCRUAL TESTS ==========

    @Test
    public void testCalculateAccrual_OneYearEmployment_ReturnsCorrectAccrual() {
        // Arrange
        testEmployee.setHireDate(LocalDate.now().minusYears(1));

        // Act
        double accrual = leaveService.calculatePTOAccrual(testEmployee);

        // Assert
        assertTrue(accrual > 0);
        assertEquals(15.0, accrual, 0.1); // Assuming 15 days per year
    }

    @Test
    public void testCalculateAccrual_NewEmployee_ReturnsZero() {
        // Arrange
        testEmployee.setHireDate(LocalDate.now());

        // Act
        double accrual = leaveService.calculatePTOAccrual(testEmployee);

        // Assert
        assertEquals(0.0, accrual, 0.1);
    }

    @Test
    public void testCalculateAccrual_FiveYearsEmployment_ReturnsIncreasedAccrual() {
        // Arrange
        testEmployee.setHireDate(LocalDate.now().minusYears(5));

        // Act
        double accrual = leaveService.calculatePTOAccrual(testEmployee);

        // Assert
        assertTrue(accrual >= 20.0); // Increased accrual after 5 years
    }

    // ========== GET LEAVE HISTORY TESTS ==========

    @Test
    public void testGetLeaveHistory_ValidEmployeeId_ReturnsHistory() {
        // Arrange
        List<LeaveRequest> requests = Arrays.asList(testLeaveRequest);
        when(leaveRequestRepository.findByEmployeeIdOrderByStartDateDesc(1L)).thenReturn(requests);

        // Act
        List<LeaveResponseDTO> result = leaveService.getLeaveHistory(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    public void testGetLeaveHistory_InvalidEmployeeId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            leaveService.getLeaveHistory(null);
        });
    }

    // ========== CALCULATE LEAVE DAYS TESTS ==========

    @Test
    public void testCalculateLeaveDays_FiveDays_ReturnsFive() {
        // Arrange
        LocalDate startDate = LocalDate.of(2024, 6, 1);
        LocalDate endDate = LocalDate.of(2024, 6, 5);

        // Act
        int days = leaveService.calculateLeaveDays(startDate, endDate);

        // Assert
        assertEquals(5, days);
    }

    @Test
    public void testCalculateLeaveDays_OneDay_ReturnsOne() {
        // Arrange
        LocalDate startDate = LocalDate.of(2024, 6, 1);
        LocalDate endDate = LocalDate.of(2024, 6, 1);

        // Act
        int days = leaveService.calculateLeaveDays(startDate, endDate);

        // Assert
        assertEquals(1, days);
    }

    @Test
    public void testCalculateLeaveDays_IncludesWeekends_ReturnsCorrectCount() {
        // Arrange
        LocalDate startDate = LocalDate.of(2024, 6, 1); // Saturday
        LocalDate endDate = LocalDate.of(2024, 6, 9); // Sunday

        // Act
        int days = leaveService.calculateLeaveDays(startDate, endDate);

        // Assert
        assertEquals(9, days); // Includes weekends
    }

    // ========== EDGE CASE TESTS ==========

    @Test
    public void testCreateLeaveRequest_SickLeave_NoBalanceCheck() {
        // Arrange
        leaveRequestDTO.setLeaveType("SICK");
        testLeaveBalance.setSickBalance(0.0); // No sick balance
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(leaveBalanceRepository.findByEmployeeId(1L)).thenReturn(Optional.of(testLeaveBalance));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenReturn(testLeaveRequest);

        // Act
        LeaveResponseDTO result = leaveService.createLeaveRequest(leaveRequestDTO);

        // Assert
        assertNotNull(result);
        assertEquals("SICK", result.getLeaveType());
    }

    @Test
    public void testCreateLeaveRequest_UnpaidLeave_NoBalanceRequired() {
        // Arrange
        leaveRequestDTO.setLeaveType("UNPAID");
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(leaveBalanceRepository.findByEmployeeId(1L)).thenReturn(Optional.of(testLeaveBalance));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenReturn(testLeaveRequest);

        // Act
        LeaveResponseDTO result = leaveService.createLeaveRequest(leaveRequestDTO);

        // Assert
        assertNotNull(result);
        assertEquals("UNPAID", result.getLeaveType());
    }

    @Test
    public void testApproveLeaveRequest_DeductsCorrectBalance() {
        // Arrange
        double initialBalance = testLeaveBalance.getPtoBalance();
        when(leaveRequestRepository.findById(1L)).thenReturn(Optional.of(testLeaveRequest));
        when(leaveBalanceRepository.findByEmployeeId(1L)).thenReturn(Optional.of(testLeaveBalance));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenReturn(testLeaveRequest);

        // Act
        leaveService.approveLeaveRequest(1L, "Approved");

        // Assert
        assertEquals(initialBalance - 5.0, testLeaveBalance.getPtoBalance(), 0.1);
    }
}