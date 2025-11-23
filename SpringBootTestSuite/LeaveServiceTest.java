package com.warehouse.ems.leave.service;

import com.warehouse.ems.leave.dto.LeaveDto;
import com.warehouse.ems.leave.entity.Leave;
import com.warehouse.ems.leave.entity.LeaveStatus;
import com.warehouse.ems.leave.entity.LeaveType;
import com.warehouse.ems.leave.repository.LeaveRepository;
import com.warehouse.ems.leave.service.impl.LeaveServiceImpl;
import com.warehouse.ems.employee.entity.Employee;
import com.warehouse.ems.employee.repository.EmployeeRepository;
import com.warehouse.ems.exception.ResourceNotFoundException;
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
 * Comprehensive unit tests for LeaveService
 * Tests leave request submission, approval workflow, and edge cases
 */
@ExtendWith(MockitoExtension.class)
public class LeaveServiceTest {

    @Mock
    private LeaveRepository leaveRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private LeaveServiceImpl leaveService;

    private Employee testEmployee;
    private Employee supervisor;
    private Leave testLeave;
    private LeaveDto testLeaveDto;

    @BeforeEach
    void setUp() {
        testEmployee = new Employee();
        testEmployee.setId(1L);
        testEmployee.setBadgeId("EMP001");
        testEmployee.setName("John Doe");

        supervisor = new Employee();
        supervisor.setId(2L);
        supervisor.setBadgeId("SUP001");
        supervisor.setName("Jane Supervisor");
        supervisor.setRole("SUPERVISOR");

        testLeave = new Leave();
        testLeave.setId(1L);
        testLeave.setEmployee(testEmployee);
        testLeave.setLeaveType(LeaveType.PTO);
        testLeave.setStartDate(LocalDate.now().plusDays(7));
        testLeave.setEndDate(LocalDate.now().plusDays(9));
        testLeave.setStatus(LeaveStatus.PENDING);
        testLeave.setReason("Family vacation");

        testLeaveDto = new LeaveDto();
        testLeaveDto.setEmployeeId(1L);
        testLeaveDto.setLeaveType(LeaveType.PTO);
        testLeaveDto.setStartDate(LocalDate.now().plusDays(7));
        testLeaveDto.setEndDate(LocalDate.now().plusDays(9));
        testLeaveDto.setReason("Family vacation");
    }

    // ========== SUBMIT LEAVE REQUEST TESTS ==========

    @Test
    void testSubmitLeaveRequest_ValidRequest_Success() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(leaveRepository.findOverlappingLeaves(anyLong(), any(), any())).thenReturn(Arrays.asList());
        when(leaveRepository.save(any(Leave.class))).thenReturn(testLeave);

        // Act
        LeaveDto result = leaveService.submitLeaveRequest(testLeaveDto);

        // Assert
        assertNotNull(result);
        assertEquals(LeaveStatus.PENDING, result.getStatus());
        assertEquals(LeaveType.PTO, result.getLeaveType());
        verify(leaveRepository, times(1)).save(any(Leave.class));
    }

    @Test
    void testSubmitLeaveRequest_NonExistentEmployee_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());
        testLeaveDto.setEmployeeId(999L);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> leaveService.submitLeaveRequest(testLeaveDto));
        verify(leaveRepository, never()).save(any(Leave.class));
    }

    @Test
    void testSubmitLeaveRequest_OverlappingLeave_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(leaveRepository.findOverlappingLeaves(anyLong(), any(), any()))
            .thenReturn(Arrays.asList(testLeave));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> leaveService.submitLeaveRequest(testLeaveDto));
        verify(leaveRepository, never()).save(any(Leave.class));
    }

    @Test
    void testSubmitLeaveRequest_EndDateBeforeStartDate_ThrowsException() {
        // Arrange
        testLeaveDto.setStartDate(LocalDate.now().plusDays(10));
        testLeaveDto.setEndDate(LocalDate.now().plusDays(5));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> leaveService.submitLeaveRequest(testLeaveDto));
    }

    @Test
    void testSubmitLeaveRequest_PastStartDate_ThrowsException() {
        // Arrange
        testLeaveDto.setStartDate(LocalDate.now().minusDays(1));
        testLeaveDto.setEndDate(LocalDate.now().plusDays(1));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> leaveService.submitLeaveRequest(testLeaveDto));
    }

    @Test
    void testSubmitLeaveRequest_NullEmployeeId_ThrowsException() {
        // Arrange
        testLeaveDto.setEmployeeId(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> leaveService.submitLeaveRequest(testLeaveDto));
    }

    @Test
    void testSubmitLeaveRequest_NullLeaveType_ThrowsException() {
        // Arrange
        testLeaveDto.setLeaveType(null);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> leaveService.submitLeaveRequest(testLeaveDto));
    }

    @Test
    void testSubmitLeaveRequest_EmptyReason_ThrowsException() {
        // Arrange
        testLeaveDto.setReason("");
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> leaveService.submitLeaveRequest(testLeaveDto));
    }

    @Test
    void testSubmitLeaveRequest_SickLeave_Success() {
        // Arrange
        testLeaveDto.setLeaveType(LeaveType.SICK);
        testLeave.setLeaveType(LeaveType.SICK);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(leaveRepository.findOverlappingLeaves(anyLong(), any(), any())).thenReturn(Arrays.asList());
        when(leaveRepository.save(any(Leave.class))).thenReturn(testLeave);

        // Act
        LeaveDto result = leaveService.submitLeaveRequest(testLeaveDto);

        // Assert
        assertNotNull(result);
        assertEquals(LeaveType.SICK, result.getLeaveType());
    }

    @Test
    void testSubmitLeaveRequest_UnpaidLeave_Success() {
        // Arrange
        testLeaveDto.setLeaveType(LeaveType.UNPAID);
        testLeave.setLeaveType(LeaveType.UNPAID);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(leaveRepository.findOverlappingLeaves(anyLong(), any(), any())).thenReturn(Arrays.asList());
        when(leaveRepository.save(any(Leave.class))).thenReturn(testLeave);

        // Act
        LeaveDto result = leaveService.submitLeaveRequest(testLeaveDto);

        // Assert
        assertNotNull(result);
        assertEquals(LeaveType.UNPAID, result.getLeaveType());
    }

    // ========== APPROVE LEAVE TESTS ==========

    @Test
    void testApproveLeave_ValidRequest_Success() {
        // Arrange
        when(leaveRepository.findById(1L)).thenReturn(Optional.of(testLeave));
        when(employeeRepository.findById(2L)).thenReturn(Optional.of(supervisor));
        when(leaveRepository.save(any(Leave.class))).thenReturn(testLeave);

        // Act
        LeaveDto result = leaveService.approveLeave(1L, 2L);

        // Assert
        assertNotNull(result);
        assertEquals(LeaveStatus.APPROVED, result.getStatus());
        assertEquals(2L, result.getApprovedBy());
        verify(leaveRepository, times(1)).save(any(Leave.class));
    }

    @Test
    void testApproveLeave_NonExistentLeave_ThrowsException() {
        // Arrange
        when(leaveRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> leaveService.approveLeave(999L, 2L));
    }

    @Test
    void testApproveLeave_NonExistentSupervisor_ThrowsException() {
        // Arrange
        when(leaveRepository.findById(1L)).thenReturn(Optional.of(testLeave));
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> leaveService.approveLeave(1L, 999L));
    }

    @Test
    void testApproveLeave_AlreadyApproved_ThrowsException() {
        // Arrange
        testLeave.setStatus(LeaveStatus.APPROVED);
        when(leaveRepository.findById(1L)).thenReturn(Optional.of(testLeave));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> leaveService.approveLeave(1L, 2L));
    }

    @Test
    void testApproveLeave_AlreadyDenied_ThrowsException() {
        // Arrange
        testLeave.setStatus(LeaveStatus.DENIED);
        when(leaveRepository.findById(1L)).thenReturn(Optional.of(testLeave));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> leaveService.approveLeave(1L, 2L));
    }

    @Test
    void testApproveLeave_NullLeaveId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> leaveService.approveLeave(null, 2L));
    }

    @Test
    void testApproveLeave_NullSupervisorId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> leaveService.approveLeave(1L, null));
    }

    // ========== DENY LEAVE TESTS ==========

    @Test
    void testDenyLeave_ValidRequest_Success() {
        // Arrange
        when(leaveRepository.findById(1L)).thenReturn(Optional.of(testLeave));
        when(employeeRepository.findById(2L)).thenReturn(Optional.of(supervisor));
        when(leaveRepository.save(any(Leave.class))).thenReturn(testLeave);

        // Act
        LeaveDto result = leaveService.denyLeave(1L, 2L, "Insufficient staffing");

        // Assert
        assertNotNull(result);
        assertEquals(LeaveStatus.DENIED, result.getStatus());
        assertEquals(2L, result.getApprovedBy());
        verify(leaveRepository, times(1)).save(any(Leave.class));
    }

    @Test
    void testDenyLeave_NonExistentLeave_ThrowsException() {
        // Arrange
        when(leaveRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, 
            () -> leaveService.denyLeave(999L, 2L, "Reason"));
    }

    @Test
    void testDenyLeave_EmptyReason_ThrowsException() {
        // Arrange
        when(leaveRepository.findById(1L)).thenReturn(Optional.of(testLeave));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, 
            () -> leaveService.denyLeave(1L, 2L, ""));
    }

    @Test
    void testDenyLeave_NullReason_ThrowsException() {
        // Arrange
        when(leaveRepository.findById(1L)).thenReturn(Optional.of(testLeave));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, 
            () -> leaveService.denyLeave(1L, 2L, null));
    }

    // ========== GET LEAVE TESTS ==========

    @Test
    void testGetLeaveById_ValidId_ReturnsLeave() {
        // Arrange
        when(leaveRepository.findById(1L)).thenReturn(Optional.of(testLeave));

        // Act
        LeaveDto result = leaveService.getLeaveById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(LeaveType.PTO, result.getLeaveType());
    }

    @Test
    void testGetLeaveById_NonExistentId_ThrowsException() {
        // Arrange
        when(leaveRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> leaveService.getLeaveById(999L));
    }

    @Test
    void testGetLeavesByEmployee_ValidEmployee_ReturnsLeaveList() {
        // Arrange
        List<Leave> leaveList = Arrays.asList(testLeave);
        when(leaveRepository.findByEmployeeId(1L)).thenReturn(leaveList);

        // Act
        List<LeaveDto> result = leaveService.getLeavesByEmployee(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getEmployeeId());
    }

    @Test
    void testGetLeavesByEmployee_NoRecords_ReturnsEmptyList() {
        // Arrange
        when(leaveRepository.findByEmployeeId(1L)).thenReturn(Arrays.asList());

        // Act
        List<LeaveDto> result = leaveService.getLeavesByEmployee(1L);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetPendingLeaves_ReturnsOnlyPending() {
        // Arrange
        List<Leave> pendingLeaves = Arrays.asList(testLeave);
        when(leaveRepository.findByStatus(LeaveStatus.PENDING)).thenReturn(pendingLeaves);

        // Act
        List<LeaveDto> result = leaveService.getPendingLeaves();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(LeaveStatus.PENDING, result.get(0).getStatus());
    }

    // ========== CANCEL LEAVE TESTS ==========

    @Test
    void testCancelLeave_ValidPendingLeave_Success() {
        // Arrange
        when(leaveRepository.findById(1L)).thenReturn(Optional.of(testLeave));
        when(leaveRepository.save(any(Leave.class))).thenReturn(testLeave);

        // Act
        LeaveDto result = leaveService.cancelLeave(1L, 1L);

        // Assert
        assertNotNull(result);
        assertEquals(LeaveStatus.CANCELLED, result.getStatus());
    }

    @Test
    void testCancelLeave_ApprovedLeave_ThrowsException() {
        // Arrange
        testLeave.setStatus(LeaveStatus.APPROVED);
        when(leaveRepository.findById(1L)).thenReturn(Optional.of(testLeave));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> leaveService.cancelLeave(1L, 1L));
    }

    @Test
    void testCancelLeave_UnauthorizedEmployee_ThrowsException() {
        // Arrange
        when(leaveRepository.findById(1L)).thenReturn(Optional.of(testLeave));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> leaveService.cancelLeave(1L, 999L));
    }

    // ========== BOUNDARY CONDITION TESTS ==========

    @Test
    void testSubmitLeaveRequest_SingleDayLeave_Success() {
        // Arrange
        testLeaveDto.setStartDate(LocalDate.now().plusDays(7));
        testLeaveDto.setEndDate(LocalDate.now().plusDays(7));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(leaveRepository.findOverlappingLeaves(anyLong(), any(), any())).thenReturn(Arrays.asList());
        when(leaveRepository.save(any(Leave.class))).thenReturn(testLeave);

        // Act
        LeaveDto result = leaveService.submitLeaveRequest(testLeaveDto);

        // Assert
        assertNotNull(result);
    }

    @Test
    void testSubmitLeaveRequest_ExtendedLeave_Success() {
        // Arrange
        testLeaveDto.setStartDate(LocalDate.now().plusDays(7));
        testLeaveDto.setEndDate(LocalDate.now().plusDays(37));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(leaveRepository.findOverlappingLeaves(anyLong(), any(), any())).thenReturn(Arrays.asList());
        when(leaveRepository.save(any(Leave.class))).thenReturn(testLeave);

        // Act
        LeaveDto result = leaveService.submitLeaveRequest(testLeaveDto);

        // Assert
        assertNotNull(result);
    }

    @Test
    void testSubmitLeaveRequest_MaxLengthReason_Success() {
        // Arrange
        testLeaveDto.setReason("A".repeat(500));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(leaveRepository.findOverlappingLeaves(anyLong(), any(), any())).thenReturn(Arrays.asList());
        when(leaveRepository.save(any(Leave.class))).thenReturn(testLeave);

        // Act
        LeaveDto result = leaveService.submitLeaveRequest(testLeaveDto);

        // Assert
        assertNotNull(result);
    }

    @Test
    void testGetLeavesByEmployee_MultipleLeaves_ReturnsAll() {
        // Arrange
        Leave leave2 = new Leave();
        leave2.setId(2L);
        leave2.setEmployee(testEmployee);
        List<Leave> leaveList = Arrays.asList(testLeave, leave2);
        when(leaveRepository.findByEmployeeId(1L)).thenReturn(leaveList);

        // Act
        List<LeaveDto> result = leaveService.getLeavesByEmployee(1L);

        // Assert
        assertEquals(2, result.size());
    }
}