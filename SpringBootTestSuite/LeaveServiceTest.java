package com.warehouse.ems.service;

import com.warehouse.ems.dto.LeaveRequestDto;
import com.warehouse.ems.entity.Employee;
import com.warehouse.ems.entity.LeaveRequest;
import com.warehouse.ems.entity.LeaveBalance;
import com.warehouse.ems.exception.EntityNotFoundException;
import com.warehouse.ems.repository.EmployeeRepository;
import com.warehouse.ems.repository.LeaveRequestRepository;
import com.warehouse.ems.repository.LeaveBalanceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for LeaveService.
 * Covers normal operation, null/invalid input, business rules, and exception scenarios.
 */
@ExtendWith(MockitoExtension.class)
class LeaveServiceTest {

    @Mock
    private LeaveRequestRepository leaveRequestRepository;
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private LeaveBalanceRepository leaveBalanceRepository;
    @InjectMocks
    private LeaveService leaveService;

    private Employee employee;
    private LeaveRequest leaveRequest;
    private LeaveRequestDto leaveRequestDto;
    private LeaveBalance leaveBalance;

    @BeforeEach
    void setUp() {
        employee = new Employee();
        employee.setId(1L);
        employee.setBadgeId("BADGE123");

        leaveRequest = new LeaveRequest();
        leaveRequest.setId(1L);
        leaveRequest.setEmployee(employee);
        leaveRequest.setStartDate(LocalDate.now());
        leaveRequest.setEndDate(LocalDate.now().plusDays(2));
        leaveRequest.setLeaveType("PTO");
        leaveRequest.setStatus("PENDING");
        leaveRequest.setReason("Vacation");
        leaveRequest.setApprover(null);

        leaveRequestDto = new LeaveRequestDto();
        leaveRequestDto.setEmployeeId(1L);
        leaveRequestDto.setStartDate(LocalDate.now());
        leaveRequestDto.setEndDate(LocalDate.now().plusDays(2));
        leaveRequestDto.setLeaveType("PTO");
        leaveRequestDto.setReason("Vacation");

        leaveBalance = new LeaveBalance();
        leaveBalance.setEmployee(employee);
        leaveBalance.setLeaveType("PTO");
        leaveBalance.setBalance(10.0);
    }

    /**
     * Test createLeaveRequest with valid input returns LeaveRequest.
     */
    @Test
    void testCreateLeaveRequest_ValidInput_ReturnsLeaveRequest() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenReturn(leaveRequest);
        LeaveRequest result = leaveService.createLeaveRequest(leaveRequestDto);
        assertNotNull(result);
        assertEquals("PTO", result.getLeaveType());
    }

    /**
     * Test createLeaveRequest with null DTO throws exception.
     */
    @Test
    void testCreateLeaveRequest_NullDto_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                leaveService.createLeaveRequest(null));
    }

    /**
     * Test approveLeave with valid input returns LeaveRequest.
     */
    @Test
    void testApproveLeave_ValidInput_ReturnsLeaveRequest() {
        Employee approver = new Employee();
        approver.setId(2L);
        when(leaveRequestRepository.findById(1L)).thenReturn(Optional.of(leaveRequest));
        when(employeeRepository.findById(2L)).thenReturn(Optional.of(approver));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenReturn(leaveRequest);
        LeaveRequest result = leaveService.approveLeave(1L, 2L);
        assertNotNull(result);
        assertEquals("APPROVED", result.getStatus());
    }

    /**
     * Test approveLeave with non-existent request throws EntityNotFoundException.
     */
    @Test
    void testApproveLeave_NonExistentRequest_ThrowsEntityNotFoundException() {
        when(leaveRequestRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () ->
                leaveService.approveLeave(99L, 2L));
    }

    /**
     * Test rejectLeave with valid input returns LeaveRequest.
     */
    @Test
    void testRejectLeave_ValidInput_ReturnsLeaveRequest() {
        Employee approver = new Employee();
        approver.setId(2L);
        when(leaveRequestRepository.findById(1L)).thenReturn(Optional.of(leaveRequest));
        when(employeeRepository.findById(2L)).thenReturn(Optional.of(approver));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenReturn(leaveRequest);
        LeaveRequest result = leaveService.rejectLeave(1L, 2L, "Not eligible");
        assertNotNull(result);
        assertEquals("REJECTED", result.getStatus());
        assertEquals("Not eligible", result.getReason());
    }

    /**
     * Test rejectLeave with null reason throws exception.
     */
    @Test
    void testRejectLeave_NullReason_ThrowsIllegalArgumentException() {
        when(leaveRequestRepository.findById(1L)).thenReturn(Optional.of(leaveRequest));
        when(employeeRepository.findById(2L)).thenReturn(Optional.of(new Employee()));
        assertThrows(IllegalArgumentException.class, () ->
                leaveService.rejectLeave(1L, 2L, null));
    }

    /**
     * Test getLeaveBalance with valid input returns LeaveBalance.
     */
    @Test
    void testGetLeaveBalance_ValidInput_ReturnsLeaveBalance() {
        when(leaveBalanceRepository.findByEmployeeIdAndLeaveType(1L, "PTO")).thenReturn(Optional.of(leaveBalance));
        LeaveBalance result = leaveService.getLeaveBalance(1L, "PTO");
        assertNotNull(result);
        assertEquals(10.0, result.getBalance());
    }

    /**
     * Test getLeaveBalance with non-existent balance throws exception.
     */
    @Test
    void testGetLeaveBalance_NonExistentBalance_ThrowsEntityNotFoundException() {
        when(leaveBalanceRepository.findByEmployeeIdAndLeaveType(1L, "SICK")).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () ->
                leaveService.getLeaveBalance(1L, "SICK"));
    }

    /**
     * Test accrueLeave with valid input does not throw.
     */
    @Test
    void testAccrueLeave_ValidInput_DoesNotThrow() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        assertDoesNotThrow(() -> leaveService.accrueLeave(1L));
    }

    /**
     * Test accrueLeave with non-existent employee throws exception.
     */
    @Test
    void testAccrueLeave_NonExistentEmployee_ThrowsEntityNotFoundException() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () ->
                leaveService.accrueLeave(99L));
    }

    /**
     * Test createLeaveRequest with overlapping dates throws exception (business rule).
     */
    @Test
    void testCreateLeaveRequest_OverlappingDates_ThrowsIllegalArgumentException() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(leaveRequestRepository.existsByEmployeeIdAndDateRange(
                eq(1L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(true);
        assertThrows(IllegalArgumentException.class, () ->
                leaveService.createLeaveRequest(leaveRequestDto));
    }
}
