package com.company.wms.leave.service;

import com.company.wms.common.exception.EmployeeNotFoundException;
import com.company.wms.common.exception.InsufficientLeaveBalanceException;
import com.company.wms.common.exception.LeaveRequestNotFoundException;
import com.company.wms.employee.entity.Employee;
import com.company.wms.employee.entity.EmployeeRole;
import com.company.wms.employee.entity.EmployeeStatus;
import com.company.wms.employee.repository.EmployeeRepository;
import com.company.wms.leave.dto.LeaveRequestCreateDTO;
import com.company.wms.leave.dto.LeaveRequestDTO;
import com.company.wms.leave.entity.LeaveRequest;
import com.company.wms.leave.entity.LeaveStatus;
import com.company.wms.leave.entity.LeaveType;
import com.company.wms.leave.repository.LeaveRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive JUnit test suite for LeaveService
 * Covers leave request creation, approval, balance tracking, and edge cases
 */
@ExtendWith(MockitoExtension.class)
class LeaveServiceTest {

    @Mock
    private LeaveRepository leaveRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private LeaveService leaveService;

    private Employee testEmployee;
    private LeaveRequest testLeaveRequest;
    private LeaveRequestCreateDTO createDTO;

    @BeforeEach
    void setUp() {
        testEmployee = Employee.builder()
                .id(1L)
                .badgeId("EMP001")
                .name("John Doe")
                .role(EmployeeRole.WORKER)
                .department("Warehouse")
                .status(EmployeeStatus.ACTIVE)
                .deleted(false)
                .build();

        testLeaveRequest = new LeaveRequest();
        testLeaveRequest.setId(1L);
        testLeaveRequest.setEmployee(testEmployee);
        testLeaveRequest.setLeaveType(LeaveType.PTO);
        testLeaveRequest.setStartDate(LocalDate.now().plusDays(7));
        testLeaveRequest.setEndDate(LocalDate.now().plusDays(9));
        testLeaveRequest.setStatus(LeaveStatus.REQUESTED);
        testLeaveRequest.setReason("Vacation");
        testLeaveRequest.setAccrualBalance(10);

        createDTO = new LeaveRequestCreateDTO();
        createDTO.setEmployeeId(1L);
        createDTO.setLeaveType(LeaveType.PTO);
        createDTO.setStartDate(LocalDate.now().plusDays(7));
        createDTO.setEndDate(LocalDate.now().plusDays(9));
        createDTO.setReason("Vacation");
    }

    // ==================== CREATE LEAVE REQUEST TESTS ====================

    @Test
    void testCreateLeaveRequest_ValidInput_Success() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(leaveRepository.getAccrualBalance(1L, LeaveType.PTO)).thenReturn(10);
        when(leaveRepository.save(any(LeaveRequest.class))).thenReturn(testLeaveRequest);

        // Act
        LeaveRequestDTO result = leaveService.createLeaveRequest(createDTO);

        // Assert
        assertNotNull(result);
        assertEquals(LeaveType.PTO, result.getLeaveType());
        assertEquals(LeaveStatus.REQUESTED, result.getStatus());
        verify(leaveRepository).save(any(LeaveRequest.class));
    }

    @Test
    void testCreateLeaveRequest_EmployeeNotFound_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        EmployeeNotFoundException exception = assertThrows(
                EmployeeNotFoundException.class,
                () -> leaveService.createLeaveRequest(createDTO)
        );

        assertTrue(exception.getMessage().contains("Employee not found"));
        verify(leaveRepository, never()).save(any(LeaveRequest.class));
    }

    @Test
    void testCreateLeaveRequest_InsufficientBalance_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(leaveRepository.getAccrualBalance(1L, LeaveType.PTO)).thenReturn(1); // Only 1 day available

        // Act & Assert
        InsufficientLeaveBalanceException exception = assertThrows(
                InsufficientLeaveBalanceException.class,
                () -> leaveService.createLeaveRequest(createDTO)
        );

        assertTrue(exception.getMessage().contains("Insufficient leave balance"));
        verify(leaveRepository, never()).save(any(LeaveRequest.class));
    }

    @Test
    void testCreateLeaveRequest_StartDateInPast_ThrowsException() {
        // Arrange
        createDTO.setStartDate(LocalDate.now().minusDays(1));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> leaveService.createLeaveRequest(createDTO)
        );

        assertTrue(exception.getMessage().contains("Start date cannot be in the past"));
    }

    @Test
    void testCreateLeaveRequest_EndDateBeforeStartDate_ThrowsException() {
        // Arrange
        createDTO.setStartDate(LocalDate.now().plusDays(10));
        createDTO.setEndDate(LocalDate.now().plusDays(5));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> leaveService.createLeaveRequest(createDTO)
        );

        assertTrue(exception.getMessage().contains("End date must be after start date"));
    }

    @Test
    void testCreateLeaveRequest_SickLeave_NoBalanceCheck() {
        // Arrange
        createDTO.setLeaveType(LeaveType.SICK);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(leaveRepository.save(any(LeaveRequest.class))).thenReturn(testLeaveRequest);

        // Act
        LeaveRequestDTO result = leaveService.createLeaveRequest(createDTO);

        // Assert
        assertNotNull(result);
        verify(leaveRepository, never()).getAccrualBalance(anyLong(), any(LeaveType.class));
        verify(leaveRepository).save(any(LeaveRequest.class));
    }

    @Test
    void testCreateLeaveRequest_UnpaidLeave_NoBalanceCheck() {
        // Arrange
        createDTO.setLeaveType(LeaveType.UNPAID);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(leaveRepository.save(any(LeaveRequest.class))).thenReturn(testLeaveRequest);

        // Act
        LeaveRequestDTO result = leaveService.createLeaveRequest(createDTO);

        // Assert
        assertNotNull(result);
        verify(leaveRepository).save(any(LeaveRequest.class));
    }

    @Test
    void testCreateLeaveRequest_SingleDayLeave_Success() {
        // Arrange
        createDTO.setStartDate(LocalDate.now().plusDays(7));
        createDTO.setEndDate(LocalDate.now().plusDays(7)); // Same day
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(leaveRepository.getAccrualBalance(1L, LeaveType.PTO)).thenReturn(10);
        when(leaveRepository.save(any(LeaveRequest.class))).thenReturn(testLeaveRequest);

        // Act
        LeaveRequestDTO result = leaveService.createLeaveRequest(createDTO);

        // Assert
        assertNotNull(result);
        verify(leaveRepository).save(any(LeaveRequest.class));
    }

    // ==================== APPROVE LEAVE REQUEST TESTS ====================

    @Test
    void testApproveLeaveRequest_ValidRequest_Success() {
        // Arrange
        when(leaveRepository.findById(1L)).thenReturn(Optional.of(testLeaveRequest));
        when(leaveRepository.save(any(LeaveRequest.class))).thenReturn(testLeaveRequest);

        // Act
        LeaveRequestDTO result = leaveService.approveLeaveRequest(1L, "supervisor@company.com");

        // Assert
        assertNotNull(result);
        verify(leaveRepository).save(argThat(leave -> 
            leave.getStatus() == LeaveStatus.APPROVED &&
            leave.getApprovedBy().equals("supervisor@company.com") &&
            leave.getApprovedAt() != null
        ));
    }

    @Test
    void testApproveLeaveRequest_RequestNotFound_ThrowsException() {
        // Arrange
        when(leaveRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        LeaveRequestNotFoundException exception = assertThrows(
                LeaveRequestNotFoundException.class,
                () -> leaveService.approveLeaveRequest(999L, "supervisor@company.com")
        );

        assertTrue(exception.getMessage().contains("Leave request not found"));
        verify(leaveRepository, never()).save(any(LeaveRequest.class));
    }

    @Test
    void testApproveLeaveRequest_AlreadyApproved_ThrowsException() {
        // Arrange
        testLeaveRequest.setStatus(LeaveStatus.APPROVED);
        when(leaveRepository.findById(1L)).thenReturn(Optional.of(testLeaveRequest));

        // Act & Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> leaveService.approveLeaveRequest(1L, "supervisor@company.com")
        );

        assertTrue(exception.getMessage().contains("already been processed"));
    }

    @Test
    void testApproveLeaveRequest_AlreadyDenied_ThrowsException() {
        // Arrange
        testLeaveRequest.setStatus(LeaveStatus.DENIED);
        when(leaveRepository.findById(1L)).thenReturn(Optional.of(testLeaveRequest));

        // Act & Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> leaveService.approveLeaveRequest(1L, "supervisor@company.com")
        );

        assertTrue(exception.getMessage().contains("already been processed"));
    }

    @Test
    void testApproveLeaveRequest_NullApprover_ThrowsException() {
        // Arrange
        when(leaveRepository.findById(1L)).thenReturn(Optional.of(testLeaveRequest));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            leaveService.approveLeaveRequest(1L, null);
        });
    }

    // ==================== DENY LEAVE REQUEST TESTS ====================

    @Test
    void testDenyLeaveRequest_ValidRequest_Success() {
        // Arrange
        when(leaveRepository.findById(1L)).thenReturn(Optional.of(testLeaveRequest));
        when(leaveRepository.save(any(LeaveRequest.class))).thenReturn(testLeaveRequest);

        // Act
        LeaveRequestDTO result = leaveService.denyLeaveRequest(1L, "supervisor@company.com", "Insufficient coverage");

        // Assert
        assertNotNull(result);
        verify(leaveRepository).save(argThat(leave -> 
            leave.getStatus() == LeaveStatus.DENIED
        ));
    }

    @Test
    void testDenyLeaveRequest_RequestNotFound_ThrowsException() {
        // Arrange
        when(leaveRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        LeaveRequestNotFoundException exception = assertThrows(
                LeaveRequestNotFoundException.class,
                () -> leaveService.denyLeaveRequest(999L, "supervisor@company.com", "Reason")
        );

        assertTrue(exception.getMessage().contains("Leave request not found"));
    }

    // ==================== CANCEL LEAVE REQUEST TESTS ====================

    @Test
    void testCancelLeaveRequest_ValidRequest_Success() {
        // Arrange
        when(leaveRepository.findById(1L)).thenReturn(Optional.of(testLeaveRequest));
        when(leaveRepository.save(any(LeaveRequest.class))).thenReturn(testLeaveRequest);

        // Act
        LeaveRequestDTO result = leaveService.cancelLeaveRequest(1L);

        // Assert
        assertNotNull(result);
        verify(leaveRepository).save(argThat(leave -> 
            leave.getStatus() == LeaveStatus.CANCELLED
        ));
    }

    @Test
    void testCancelLeaveRequest_AlreadyApproved_ThrowsException() {
        // Arrange
        testLeaveRequest.setStatus(LeaveStatus.APPROVED);
        testLeaveRequest.setStartDate(LocalDate.now().minusDays(1)); // Already started
        when(leaveRepository.findById(1L)).thenReturn(Optional.of(testLeaveRequest));

        // Act & Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> leaveService.cancelLeaveRequest(1L)
        );

        assertTrue(exception.getMessage().contains("Cannot cancel leave that has already started"));
    }

    // ==================== GET ACCRUAL BALANCE TESTS ====================

    @Test
    void testGetAccrualBalance_ValidEmployee_ReturnsBalance() {
        // Arrange
        when(leaveRepository.getAccrualBalance(1L, LeaveType.PTO)).thenReturn(15);

        // Act
        int balance = leaveService.getAccrualBalance(1L, LeaveType.PTO);

        // Assert
        assertEquals(15, balance);
        verify(leaveRepository).getAccrualBalance(1L, LeaveType.PTO);
    }

    @Test
    void testGetAccrualBalance_NoBalance_ReturnsZero() {
        // Arrange
        when(leaveRepository.getAccrualBalance(1L, LeaveType.PTO)).thenReturn(0);

        // Act
        int balance = leaveService.getAccrualBalance(1L, LeaveType.PTO);

        // Assert
        assertEquals(0, balance);
    }

    // ==================== EDGE CASE TESTS ====================

    @Test
    void testCreateLeaveRequest_LongLeave_Success() {
        // Arrange - 30 day leave
        createDTO.setStartDate(LocalDate.now().plusDays(7));
        createDTO.setEndDate(LocalDate.now().plusDays(37));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(leaveRepository.getAccrualBalance(1L, LeaveType.PTO)).thenReturn(40);
        when(leaveRepository.save(any(LeaveRequest.class))).thenReturn(testLeaveRequest);

        // Act
        LeaveRequestDTO result = leaveService.createLeaveRequest(createDTO);

        // Assert
        assertNotNull(result);
        verify(leaveRepository).save(any(LeaveRequest.class));
    }

    @Test
    void testCreateLeaveRequest_BereavementLeave_NoBalanceCheck() {
        // Arrange
        createDTO.setLeaveType(LeaveType.BEREAVEMENT);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(leaveRepository.save(any(LeaveRequest.class))).thenReturn(testLeaveRequest);

        // Act
        LeaveRequestDTO result = leaveService.createLeaveRequest(createDTO);

        // Assert
        assertNotNull(result);
        verify(leaveRepository).save(any(LeaveRequest.class));
    }

    @Test
    void testCreateLeaveRequest_JuryDutyLeave_NoBalanceCheck() {
        // Arrange
        createDTO.setLeaveType(LeaveType.JURY_DUTY);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(leaveRepository.save(any(LeaveRequest.class))).thenReturn(testLeaveRequest);

        // Act
        LeaveRequestDTO result = leaveService.createLeaveRequest(createDTO);

        // Assert
        assertNotNull(result);
        verify(leaveRepository).save(any(LeaveRequest.class));
    }

    @Test
    void testApproveLeaveRequest_EmptyApprover_ThrowsException() {
        // Arrange
        when(leaveRepository.findById(1L)).thenReturn(Optional.of(testLeaveRequest));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            leaveService.approveLeaveRequest(1L, "");
        });
    }

    @Test
    void testCreateLeaveRequest_VeryLongReason_Success() {
        // Arrange
        createDTO.setReason("A".repeat(500)); // 500 character reason
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(leaveRepository.getAccrualBalance(1L, LeaveType.PTO)).thenReturn(10);
        when(leaveRepository.save(any(LeaveRequest.class))).thenReturn(testLeaveRequest);

        // Act
        LeaveRequestDTO result = leaveService.createLeaveRequest(createDTO);

        // Assert
        assertNotNull(result);
        verify(leaveRepository).save(any(LeaveRequest.class));
    }
}
