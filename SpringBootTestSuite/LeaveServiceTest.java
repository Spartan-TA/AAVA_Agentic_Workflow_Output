package com.wems.leave.service;

import com.wems.leave.domain.LeaveRequest;
import com.wems.leave.domain.LeaveBalance;
import com.wems.leave.domain.LeaveType;
import com.wems.leave.domain.LeaveStatus;
import com.wems.leave.dto.LeaveRequestDto;
import com.wems.leave.repository.LeaveRequestRepository;
import com.wems.leave.repository.LeaveBalanceRepository;
import com.wems.employee.domain.Employee;
import com.wems.employee.repository.EmployeeRepository;
import com.wems.scheduling.service.ScheduleService;
import com.wems.common.exception.BusinessValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Leave Service Tests")
class LeaveServiceTest {

    @Mock
    private LeaveRequestRepository leaveRequestRepository;
    @Mock
    private LeaveBalanceRepository leaveBalanceRepository;
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private ScheduleService scheduleService;
    @Mock
    private NotificationService notificationService;
    @InjectMocks
    private LeaveService leaveService;

    private Employee validEmployee;
    private LeaveRequestDto validLeaveRequestDto;
    private LeaveBalance validLeaveBalance;
    private LeaveRequest validLeaveRequest;

    @BeforeEach
    void setUp() {
        validEmployee = new Employee();
        validEmployee.setId(1L);
        validEmployee.setBadgeId("EMP001");
        validEmployee.setName("John Doe");

        validLeaveRequestDto = new LeaveRequestDto();
        validLeaveRequestDto.setType(LeaveType.PTO);
        validLeaveRequestDto.setStartDate(LocalDate.of(2024, 2, 1));
        validLeaveRequestDto.setEndDate(LocalDate.of(2024, 2, 5));
        validLeaveRequestDto.setReason("Family vacation");

        validLeaveBalance = new LeaveBalance();
        validLeaveBalance.setId(1L);
        validLeaveBalance.setEmployee(validEmployee);
        validLeaveBalance.setLeaveType(LeaveType.PTO);
        validLeaveBalance.setBalance(new BigDecimal("20.00"));

        validLeaveRequest = new LeaveRequest();
        validLeaveRequest.setId(1L);
        validLeaveRequest.setEmployee(validEmployee);
        validLeaveRequest.setType(LeaveType.PTO);
        validLeaveRequest.setStartDate(LocalDate.of(2024, 2, 1));
        validLeaveRequest.setEndDate(LocalDate.of(2024, 2, 5));
        validLeaveRequest.setTotalDays(new BigDecimal("5.00"));
        validLeaveRequest.setStatus(LeaveStatus.PENDING);
    }

    @Test
    @DisplayName("Test requestLeave with valid PTO creates leave request")
    void testRequestLeave_WithValidPTO_CreatesLeaveRequest() {
        when(leaveBalanceRepository.findByEmployeeAndLeaveType(validEmployee, LeaveType.PTO))
            .thenReturn(Optional.of(validLeaveBalance));
        when(leaveRequestRepository.existsOverlappingRequest(any(), any(), any(), any()))
            .thenReturn(false);
        when(leaveRequestRepository.save(any(LeaveRequest.class)))
            .thenReturn(validLeaveRequest);

        LeaveRequest result = leaveService.requestLeave(validLeaveRequestDto, validEmployee);

        assertNotNull(result);
        assertEquals(LeaveType.PTO, result.getType());
        assertEquals(LeaveStatus.PENDING, result.getStatus());
        verify(leaveRequestRepository, times(1)).save(any(LeaveRequest.class));
        verify(notificationService, times(1)).notifyLeaveRequest(any(LeaveRequest.class));
    }

    @Test
    @DisplayName("Test requestLeave with insufficient balance throws BusinessValidationException")
    void testRequestLeave_WithInsufficientBalance_ThrowsBusinessValidationException() {
        validLeaveBalance.setBalance(new BigDecimal("2.00"));
        when(leaveBalanceRepository.findByEmployeeAndLeaveType(validEmployee, LeaveType.PTO))
            .thenReturn(Optional.of(validLeaveBalance));

        BusinessValidationException exception = assertThrows(
            BusinessValidationException.class,
            () -> leaveService.requestLeave(validLeaveRequestDto, validEmployee)
        );
        assertTrue(exception.getMessage().contains("Insufficient leave balance"));
        verify(leaveRequestRepository, never()).save(any(LeaveRequest.class));
    }

    @Test
    @DisplayName("Test requestLeave with overlapping dates throws BusinessValidationException")
    void testRequestLeave_WithOverlappingDates_ThrowsBusinessValidationException() {
        when(leaveBalanceRepository.findByEmployeeAndLeaveType(validEmployee, LeaveType.PTO))
            .thenReturn(Optional.of(validLeaveBalance));
        when(leaveRequestRepository.existsOverlappingRequest(
            validEmployee, 
            validLeaveRequestDto.getStartDate(), 
            validLeaveRequestDto.getEndDate(), 
            LeaveStatus.APPROVED))
            .thenReturn(true);

        BusinessValidationException exception = assertThrows(
            BusinessValidationException.class,
            () -> leaveService.requestLeave(validLeaveRequestDto, validEmployee)
        );
        assertTrue(exception.getMessage().contains("Overlapping leave request exists"));
    }

    @Test
    @DisplayName("Test approveLeave with valid request updates balance and schedule")
    void testApproveLeave_WithValidRequest_UpdatesBalanceAndSchedule() {
        validLeaveRequest.setStatus(LeaveStatus.PENDING);
        when(leaveRequestRepository.findById(1L)).thenReturn(Optional.of(validLeaveRequest));
        when(leaveBalanceRepository.findByEmployeeAndLeaveType(validEmployee, LeaveType.PTO))
            .thenReturn(Optional.of(validLeaveBalance));
        when(leaveBalanceRepository.save(any(LeaveBalance.class))).thenReturn(validLeaveBalance);
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenReturn(validLeaveRequest);

        LeaveRequest result = leaveService.approveLeave(1L, validEmployee, "Approved");

        assertNotNull(result);
        assertEquals(LeaveStatus.APPROVED, result.getStatus());
        assertNotNull(result.getApprovedBy());
        assertNotNull(result.getApprovedAt());
        verify(leaveBalanceRepository, times(1)).save(any(LeaveBalance.class));
        verify(scheduleService, times(1)).flagSchedulesForCoverage(any(), any(), any());
        verify(notificationService, times(1)).notifyLeaveApproval(any(LeaveRequest.class));
    }

    @Test
    @DisplayName("Test denyLeave with valid request updates status and notifies")
    void testDenyLeave_WithValidRequest_UpdatesStatusAndNotifies() {
        validLeaveRequest.setStatus(LeaveStatus.PENDING);
        when(leaveRequestRepository.findById(1L)).thenReturn(Optional.of(validLeaveRequest));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenReturn(validLeaveRequest);

        LeaveRequest result = leaveService.denyLeave(1L, validEmployee, "Insufficient staffing");

        assertNotNull(result);
        assertEquals(LeaveStatus.DENIED, result.getStatus());
        assertEquals("Insufficient staffing", result.getApprovalNotes());
        verify(notificationService, times(1)).notifyLeaveDenial(any(LeaveRequest.class));
    }

    @Test
    @DisplayName("Test getBalance returns correct balance for leave type")
    void testGetBalance_ForLeaveType_ReturnsCorrectBalance() {
        when(leaveBalanceRepository.findByEmployeeAndLeaveType(validEmployee, LeaveType.PTO))
            .thenReturn(Optional.of(validLeaveBalance));

        LeaveBalance result = leaveService.getBalance(validEmployee, LeaveType.PTO);

        assertNotNull(result);
        assertEquals(new BigDecimal("20.00"), result.getBalance());
        assertEquals(LeaveType.PTO, result.getLeaveType());
    }