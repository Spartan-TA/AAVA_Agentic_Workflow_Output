package com.warehouse.ems.domain.leave;

import com.warehouse.ems.domain.employee.Employee;
import com.warehouse.ems.domain.employee.EmployeeRepository;
import com.warehouse.ems.exception.BusinessException;
import com.warehouse.ems.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("Leave Service Test Suite")
public class LeaveServiceTest {

    @Mock
    private LeaveRequestRepository leaveRequestRepository;

    @Mock
    private LeaveBalanceRepository leaveBalanceRepository;

    @Mock
    private LeavePolicyRepository leavePolicyRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private LeaveServiceImpl leaveService;

    private Employee testEmployee;
    private LeaveRequest testLeaveRequest;
    private LeaveBalance testLeaveBalance;
    private LeavePolicy testLeavePolicy;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        testEmployee = new Employee();
        testEmployee.setId(1L);
        testEmployee.setBadgeId("EMP001");
        testEmployee.setName("John Doe");

        testLeavePolicy = new LeavePolicy();
        testLeavePolicy.setId(1L);
        testLeavePolicy.setLeaveType(LeaveType.PTO);
        testLeavePolicy.setAnnualAccrual(15.0);
        testLeavePolicy.setMaxCarryover(5.0);

        testLeaveBalance = new LeaveBalance();
        testLeaveBalance.setId(1L);
        testLeaveBalance.setEmployee(testEmployee);
        testLeaveBalance.setLeaveType(LeaveType.PTO);
        testLeaveBalance.setAvailableDays(10.0);
        testLeaveBalance.setUsedDays(5.0);

        testLeaveRequest = new LeaveRequest();
        testLeaveRequest.setId(1L);
        testLeaveRequest.setEmployee(testEmployee);
        testLeaveRequest.setLeaveType(LeaveType.PTO);
        testLeaveRequest.setStartDate(LocalDate.now().plusDays(7));
        testLeaveRequest.setEndDate(LocalDate.now().plusDays(9));
        testLeaveRequest.setDaysRequested(3.0);
        testLeaveRequest.setStatus(LeaveRequestStatus.PENDING);
    }

    @Test
    @DisplayName("Test create leave request with valid data")
    public void testCreateLeaveRequestWithValidData() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(leaveBalanceRepository.findByEmployeeAndLeaveType(1L, LeaveType.PTO))
            .thenReturn(Optional.of(testLeaveBalance));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenReturn(testLeaveRequest);

        LeaveRequestDto dto = new LeaveRequestDto();
        dto.setEmployeeId(1L);
        dto.setLeaveType(LeaveType.PTO);
        dto.setStartDate(LocalDate.now().plusDays(7));
        dto.setEndDate(LocalDate.now().plusDays(9));

        // Act
        LeaveRequestDto result = leaveService.createLeaveRequest(dto);

        // Assert
        assertNotNull(result);
        verify(leaveRequestRepository, times(1)).save(any(LeaveRequest.class));
        verify(eventPublisher, times(1)).publishEvent(any());
    }

    @Test
    @DisplayName("Test create leave request with insufficient balance")
    public void testCreateLeaveRequestWithInsufficientBalance() {
        // Arrange
        testLeaveBalance.setAvailableDays(2.0);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(leaveBalanceRepository.findByEmployeeAndLeaveType(1L, LeaveType.PTO))
            .thenReturn(Optional.of(testLeaveBalance));

        LeaveRequestDto dto = new LeaveRequestDto();
        dto.setEmployeeId(1L);
        dto.setLeaveType(LeaveType.PTO);
        dto.setStartDate(LocalDate.now().plusDays(7));
        dto.setEndDate(LocalDate.now().plusDays(9));

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            leaveService.createLeaveRequest(dto);
        });
        assertTrue(exception.getMessage().contains("Insufficient leave balance"));
    }

    @Test
    @DisplayName("Test create leave request with null employee ID")
    public void testCreateLeaveRequestWithNullEmployeeId() {
        // Arrange
        LeaveRequestDto dto = new LeaveRequestDto();
        dto.setEmployeeId(null);

        // Act & Assert
        assertThrows(Exception.class, () -> {
            leaveService.createLeaveRequest(dto);
        });
    }

    @Test
    @DisplayName("Test create leave request with null start date")
    public void testCreateLeaveRequestWithNullStartDate() {
        // Arrange
        LeaveRequestDto dto = new LeaveRequestDto();
        dto.setEmployeeId(1L);
        dto.setStartDate(null);

        // Act & Assert
        assertThrows(Exception.class, () -> {
            leaveService.createLeaveRequest(dto);
        });
    }

    @Test
    @DisplayName("Test create leave request with null end date")
    public void testCreateLeaveRequestWithNullEndDate() {
        // Arrange
        LeaveRequestDto dto = new LeaveRequestDto();
        dto.setEmployeeId(1L);
        dto.setStartDate(LocalDate.now().plusDays(7));
        dto.setEndDate(null);

        // Act & Assert
        assertThrows(Exception.class, () -> {
            leaveService.createLeaveRequest(dto);
        });
    }

    @Test
    @DisplayName("Test create leave request with end date before start date")
    public void testCreateLeaveRequestWithInvalidDateRange() {
        // Arrange
        LeaveRequestDto dto = new LeaveRequestDto();
        dto.setEmployeeId(1L);
        dto.setStartDate(LocalDate.now().plusDays(9));
        dto.setEndDate(LocalDate.now().plusDays(7));

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            leaveService.createLeaveRequest(dto);
        });
    }

    @Test
    @DisplayName("Test create leave request with past start date")
    public void testCreateLeaveRequestWithPastStartDate() {
        // Arrange
        LeaveRequestDto dto = new LeaveRequestDto();
        dto.setEmployeeId(1L);
        dto.setStartDate(LocalDate.now().minusDays(1));
        dto.setEndDate(LocalDate.now().plusDays(2));

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            leaveService.createLeaveRequest(dto);
        });
    }

    @Test
    @DisplayName("Test approve leave request - success")
    public void testApproveLeaveRequestSuccess() {
        // Arrange
        when(leaveRequestRepository.findById(1L)).thenReturn(Optional.of(testLeaveRequest));
        when(leaveBalanceRepository.findByEmployeeAndLeaveType(1L, LeaveType.PTO))
            .thenReturn(Optional.of(testLeaveBalance));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenReturn(testLeaveRequest);

        // Act
        LeaveRequestDto result = leaveService.approveLeaveRequest(1L, "Approved by supervisor");

        // Assert
        assertNotNull(result);
        verify(leaveRequestRepository, times(1)).save(any(LeaveRequest.class));
        verify(leaveBalanceRepository, times(1)).save(any(LeaveBalance.class));
        verify(eventPublisher, times(1)).publishEvent(any());
    }

    @Test
    @DisplayName("Test approve non-existent leave request")
    public void testApproveNonExistentLeaveRequest() {
        // Arrange
        when(leaveRequestRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            leaveService.approveLeaveRequest(999L, "Approved");
        });
    }

    @Test
    @DisplayName("Test approve already approved leave request")
    public void testApproveAlreadyApprovedLeaveRequest() {
        // Arrange
        testLeaveRequest.setStatus(LeaveRequestStatus.APPROVED);
        when(leaveRequestRepository.findById(1L)).thenReturn(Optional.of(testLeaveRequest));

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            leaveService.approveLeaveRequest(1L, "Approved");
        });
    }

    @Test
    @DisplayName("Test deny leave request - success")
    public void testDenyLeaveRequestSuccess() {
        // Arrange
        when(leaveRequestRepository.findById(1L)).thenReturn(Optional.of(testLeaveRequest));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenReturn(testLeaveRequest);

        // Act
        LeaveRequestDto result = leaveService.denyLeaveRequest(1L, "Insufficient staffing");

        // Assert
        assertNotNull(result);
        verify(leaveRequestRepository, times(1)).save(any(LeaveRequest.class));
        verify(eventPublisher, times(1)).publishEvent(any());
    }

    @Test
    @DisplayName("Test deny leave request with null reason")
    public void testDenyLeaveRequestWithNullReason() {
        // Arrange
        when(leaveRequestRepository.findById(1L)).thenReturn(Optional.of(testLeaveRequest));

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            leaveService.denyLeaveRequest(1L, null);
        });
    }

    @Test
    @DisplayName("Test deny leave request with empty reason")
    public void testDenyLeaveRequestWithEmptyReason() {
        // Arrange
        when(leaveRequestRepository.findById(1L)).thenReturn(Optional.of(testLeaveRequest));

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            leaveService.denyLeaveRequest(1L, "");
        });
    }

    @Test
    @DisplayName("Test cancel leave request - success")
    public void testCancelLeaveRequestSuccess() {
        // Arrange
        testLeaveRequest.setStatus(LeaveRequestStatus.APPROVED);
        when(leaveRequestRepository.findById(1L)).thenReturn(Optional.of(testLeaveRequest));
        when(leaveBalanceRepository.findByEmployeeAndLeaveType(1L, LeaveType.PTO))
            .thenReturn(Optional.of(testLeaveBalance));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenReturn(testLeaveRequest);

        // Act
        LeaveRequestDto result = leaveService.cancelLeaveRequest(1L);

        // Assert
        assertNotNull(result);
        verify(leaveBalanceRepository, times(1)).save(any(LeaveBalance.class));
    }

    @Test
    @DisplayName("Test get leave balance for employee")
    public void testGetLeaveBalanceForEmployee() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(leaveBalanceRepository.findByEmployeeAndLeaveType(1L, LeaveType.PTO))
            .thenReturn(Optional.of(testLeaveBalance));

        // Act
        LeaveBalanceDto result = leaveService.getLeaveBalance(1L, LeaveType.PTO);

        // Assert
        assertNotNull(result);
        assertEquals(10.0, result.getAvailableDays());
    }

    @Test
    @DisplayName("Test get leave balance for non-existent employee")
    public void testGetLeaveBalanceForNonExistentEmployee() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            leaveService.getLeaveBalance(999L, LeaveType.PTO);
        });
    }

    @Test
    @DisplayName("Test accrue leave balance")
    public void testAccrueLeaveBalance() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(leaveBalanceRepository.findByEmployeeAndLeaveType(1L, LeaveType.PTO))
            .thenReturn(Optional.of(testLeaveBalance));
        when(leavePolicyRepository.findByLeaveType(LeaveType.PTO))
            .thenReturn(Optional.of(testLeavePolicy));
        when(leaveBalanceRepository.save(any(LeaveBalance.class))).thenReturn(testLeaveBalance);

        // Act
        leaveService.accrueLeaveBalance(1L, LeaveType.PTO, 1.25);

        // Assert
        verify(leaveBalanceRepository, times(1)).save(any(LeaveBalance.class));
    }

    @Test
    @DisplayName("Test accrue leave balance with negative amount")
    public void testAccrueLeaveBalanceWithNegativeAmount() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            leaveService.accrueLeaveBalance(1L, LeaveType.PTO, -1.0);
        });
    }

    @Test
    @DisplayName("Test create leave request with same day start and end")
    public void testCreateLeaveRequestWithSameDay() {
        // Arrange
        LocalDate sameDay = LocalDate.now().plusDays(7);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(leaveBalanceRepository.findByEmployeeAndLeaveType(1L, LeaveType.PTO))
            .thenReturn(Optional.of(testLeaveBalance));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenReturn(testLeaveRequest);

        LeaveRequestDto dto = new LeaveRequestDto();
        dto.setEmployeeId(1L);
        dto.setLeaveType(LeaveType.PTO);
        dto.setStartDate(sameDay);
        dto.setEndDate(sameDay);

        // Act
        LeaveRequestDto result = leaveService.createLeaveRequest(dto);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Test create leave request with maximum allowed days")
    public void testCreateLeaveRequestWithMaxDays() {
        // Arrange
        testLeaveBalance.setAvailableDays(30.0);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(leaveBalanceRepository.findByEmployeeAndLeaveType(1L, LeaveType.PTO))
            .thenReturn(Optional.of(testLeaveBalance));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenReturn(testLeaveRequest);

        LeaveRequestDto dto = new LeaveRequestDto();
        dto.setEmployeeId(1L);
        dto.setLeaveType(LeaveType.PTO);
        dto.setStartDate(LocalDate.now().plusDays(7));
        dto.setEndDate(LocalDate.now().plusDays(36));

        // Act
        LeaveRequestDto result = leaveService.createLeaveRequest(dto);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Test create sick leave request")
    public void testCreateSickLeaveRequest() {
        // Arrange
        testLeaveBalance.setLeaveType(LeaveType.SICK);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(leaveBalanceRepository.findByEmployeeAndLeaveType(1L, LeaveType.SICK))
            .thenReturn(Optional.of(testLeaveBalance));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenReturn(testLeaveRequest);

        LeaveRequestDto dto = new LeaveRequestDto();
        dto.setEmployeeId(1L);
        dto.setLeaveType(LeaveType.SICK);
        dto.setStartDate(LocalDate.now());
        dto.setEndDate(LocalDate.now().plusDays(2));

        // Act
        LeaveRequestDto result = leaveService.createLeaveRequest(dto);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Test create unpaid leave request")
    public void testCreateUnpaidLeaveRequest() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenReturn(testLeaveRequest);

        LeaveRequestDto dto = new LeaveRequestDto();
        dto.setEmployeeId(1L);
        dto.setLeaveType(LeaveType.UNPAID);
        dto.setStartDate(LocalDate.now().plusDays(7));
        dto.setEndDate(LocalDate.now().plusDays(9));

        // Act
        LeaveRequestDto result = leaveService.createLeaveRequest(dto);

        // Assert
        assertNotNull(result);
    }
}