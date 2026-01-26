package com.company.warehouse.leave.service;

import com.company.warehouse.leave.domain.*;
import com.company.warehouse.leave.dto.*;
import com.company.warehouse.leave.repository.*;
import com.company.warehouse.employee.domain.Employee;
import com.company.warehouse.employee.repository.EmployeeRepository;
import com.company.warehouse.common.exception.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("Leave Service Tests")
public class LeaveServiceTest {
    @Mock private LeaveRequestRepository leaveRequestRepository;
    @Mock private LeaveBalanceRepository leaveBalanceRepository;
    @Mock private EmployeeRepository employeeRepository;
    @InjectMocks private LeaveService leaveService;
    private Employee testEmployee;
    private LeaveRequest leaveRequest;
    private LeaveBalance leaveBalance;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        testEmployee = new Employee();
        testEmployee.setId(1L);
        testEmployee.setBadgeId("EMP001");
        leaveBalance = new LeaveBalance();
        leaveBalance.setEmployee(testEmployee);
        leaveBalance.setLeaveType(LeaveType.PTO);
        leaveBalance.setBalance(new BigDecimal("80.00"));
        leaveRequest = new LeaveRequest();
        leaveRequest.setId(1L);
        leaveRequest.setEmployee(testEmployee);
        leaveRequest.setLeaveType(LeaveType.PTO);
        leaveRequest.setStartDate(LocalDate.now().plusDays(7));
        leaveRequest.setEndDate(LocalDate.now().plusDays(9));
        leaveRequest.setStatus(LeaveStatus.PENDING);
    }

    @Test
    @DisplayName("Test requestLeave with valid data")
    public void testRequestLeave_ValidData() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(leaveBalanceRepository.findByEmployeeAndLeaveType(any(), any())).thenReturn(Optional.of(leaveBalance));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenReturn(leaveRequest);
        LeaveRequestDTO result = leaveService.requestLeave(new LeaveRequestCreateDTO());
        assertNotNull(result);
        verify(leaveRequestRepository, times(1)).save(any(LeaveRequest.class));
    }

    @Test
    @DisplayName("Test requestLeave with insufficient balance")
    public void testRequestLeave_InsufficientBalance() {
        leaveBalance.setBalance(new BigDecimal("1.00"));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(leaveBalanceRepository.findByEmployeeAndLeaveType(any(), any())).thenReturn(Optional.of(leaveBalance));
        LeaveRequestCreateDTO dto = new LeaveRequestCreateDTO();
        dto.setStartDate(LocalDate.now());
        dto.setEndDate(LocalDate.now().plusDays(5));
        assertThrows(BusinessException.class, () -> leaveService.requestLeave(dto));
    }

    @Test
    @DisplayName("Test approveLeave with valid request")
    public void testApproveLeave_ValidRequest() {
        when(leaveRequestRepository.findById(1L)).thenReturn(Optional.of(leaveRequest));
        when(leaveBalanceRepository.findByEmployeeAndLeaveType(any(), any())).thenReturn(Optional.of(leaveBalance));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenReturn(leaveRequest);
        LeaveRequestDTO result = leaveService.approveLeave(1L);
        assertNotNull(result);
        assertEquals(LeaveStatus.APPROVED, leaveRequest.getStatus());
    }

    @Test
    @DisplayName("Test denyLeave with valid request")
    public void testDenyLeave_ValidRequest() {
        when(leaveRequestRepository.findById(1L)).thenReturn(Optional.of(leaveRequest));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenReturn(leaveRequest);
        LeaveRequestDTO result = leaveService.denyLeave(1L, "Insufficient coverage");
        assertNotNull(result);
        assertEquals(LeaveStatus.DENIED, leaveRequest.getStatus());
    }

    @Test
    @DisplayName("Test approveLeave with non-existent request")
    public void testApproveLeave_NonExistentRequest() {
        when(leaveRequestRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> leaveService.approveLeave(999L));
    }

    @Test
    @DisplayName("Test getLeaveBalance for employee")
    public void testGetLeaveBalance_ValidEmployee() {
        when(leaveBalanceRepository.findByEmployeeAndLeaveType(any(), any())).thenReturn(Optional.of(leaveBalance));
        LeaveBalanceDTO result = leaveService.getLeaveBalance(1L, LeaveType.PTO);
        assertNotNull(result);
        assertEquals(new BigDecimal("80.00"), result.getBalance());
    }

    @Test
    @DisplayName("Test requestLeave with past dates")
    public void testRequestLeave_PastDates() {
        LeaveRequestCreateDTO dto = new LeaveRequestCreateDTO();
        dto.setStartDate(LocalDate.now().minusDays(5));
        dto.setEndDate(LocalDate.now().minusDays(3));
        assertThrows(BusinessException.class, () -> leaveService.requestLeave(dto));
    }