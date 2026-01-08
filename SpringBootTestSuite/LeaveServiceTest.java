package com.example.warehouse.service;

import com.example.warehouse.entity.Employee;
import com.example.warehouse.entity.LeaveRequest;
import com.example.warehouse.repository.EmployeeRepository;
import com.example.warehouse.repository.LeaveRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for LeaveService.
 * 
 * Tests cover:
 * - Leave request creation and retrieval
 * - Status update workflow (PENDING, APPROVED, REJECTED)
 * - Normal cases, boundary conditions, and edge cases
 * - Exception handling for non-existent requests and employees
 * - Date validation scenarios
 * 
 * @author Warehouse Test Team
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
    private LeaveRequest pendingLeaveRequest;
    private LeaveRequest approvedLeaveRequest;
    private LeaveRequest rejectedLeaveRequest;

    /**
     * Set up test data before each test method.
     */
    @BeforeEach
    public void setUp() {
        testEmployee = Employee.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@warehouse.com")
                .position("Warehouse Associate")
                .hireDate(LocalDate.of(2024, 1, 15))
                .active(true)
                .build();

        pendingLeaveRequest = LeaveRequest.builder()
                .id(1L)
                .employee(testEmployee)
                .startDate(LocalDate.now().plusDays(7))
                .endDate(LocalDate.now().plusDays(10))
                .status(LeaveRequest.Status.PENDING)
                .build();

        approvedLeaveRequest = LeaveRequest.builder()
                .id(2L)
                .employee(testEmployee)
                .startDate(LocalDate.now().plusDays(14))
                .endDate(LocalDate.now().plusDays(17))
                .status(LeaveRequest.Status.APPROVED)
                .build();

        rejectedLeaveRequest = LeaveRequest.builder()
                .id(3L)
                .employee(testEmployee)
                .startDate(LocalDate.now().plusDays(21))
                .endDate(LocalDate.now().plusDays(24))
                .status(LeaveRequest.Status.REJECTED)
                .build();
    }

    // ==================== GET ALL LEAVE REQUESTS TESTS ====================

    /**
     * Test getAllLeaveRequests with multiple requests - Normal case.
     */
    @Test
    public void testGetAllLeaveRequests_WithMultipleRequests_Success() {
        // Arrange
        List<LeaveRequest> requests = Arrays.asList(pendingLeaveRequest, approvedLeaveRequest, rejectedLeaveRequest);
        when(leaveRepository.findAll()).thenReturn(requests);

        // Act
        List<LeaveRequest> result = leaveService.getAllLeaveRequests();

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(LeaveRequest.Status.PENDING, result.get(0).getStatus());
        assertEquals(LeaveRequest.Status.APPROVED, result.get(1).getStatus());
        assertEquals(LeaveRequest.Status.REJECTED, result.get(2).getStatus());
        verify(leaveRepository, times(1)).findAll();
    }

    /**
     * Test getAllLeaveRequests with empty list - Boundary condition.
     */
    @Test
    public void testGetAllLeaveRequests_EmptyList_ReturnsEmptyList() {
        // Arrange
        when(leaveRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<LeaveRequest> result = leaveService.getAllLeaveRequests();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(leaveRepository, times(1)).findAll();
    }

    /**
     * Test getAllLeaveRequests with single request - Edge case.
     */
    @Test
    public void testGetAllLeaveRequests_SingleRequest_Success() {
        // Arrange
        when(leaveRepository.findAll()).thenReturn(Collections.singletonList(pendingLeaveRequest));

        // Act
        List<LeaveRequest> result = leaveService.getAllLeaveRequests();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(LeaveRequest.Status.PENDING, result.get(0).getStatus());
        verify(leaveRepository, times(1)).findAll();
    }

    // ==================== GET LEAVE REQUEST BY ID TESTS ====================

    /**
     * Test getLeaveRequestById with valid ID - Normal case.
     */
    @Test
    public void testGetLeaveRequestById_ValidId_Success() {
        // Arrange
        when(leaveRepository.findById(1L)).thenReturn(Optional.of(pendingLeaveRequest));

        // Act
        LeaveRequest result = leaveService.getLeaveRequestById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(LeaveRequest.Status.PENDING, result.getStatus());
        assertEquals(testEmployee, result.getEmployee());
        verify(leaveRepository, times(1)).findById(1L);
    }

    /**
     * Test getLeaveRequestById with non-existent ID - Edge case.
     */
    @Test
    public void testGetLeaveRequestById_NonExistentId_ThrowsException() {
        // Arrange
        when(leaveRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            leaveService.getLeaveRequestById(999L);
        });
        assertEquals("Leave request not found", exception.getMessage());
        verify(leaveRepository, times(1)).findById(999L);
    }

    /**
     * Test getLeaveRequestById with null ID - Boundary condition.
     */
    @Test
    public void testGetLeaveRequestById_NullId_ThrowsException() {
        // Arrange
        when(leaveRepository.findById(null)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            leaveService.getLeaveRequestById(null);
        });
    }

    /**
     * Test getLeaveRequestById with negative ID - Edge case.
     */
    @Test
    public void testGetLeaveRequestById_NegativeId_ThrowsException() {
        // Arrange
        when(leaveRepository.findById(-1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            leaveService.getLeaveRequestById(-1L);
        });
    }

    // ==================== CREATE LEAVE REQUEST TESTS ====================

    /**
     * Test createLeaveRequest with valid data - Normal case.
     */
    @Test
    public void testCreateLeaveRequest_ValidData_Success() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(leaveRepository.save(any(LeaveRequest.class))).thenReturn(pendingLeaveRequest);

        // Act
        LeaveRequest result = leaveService.createLeaveRequest(1L, pendingLeaveRequest);

        // Assert
        assertNotNull(result);
        assertEquals(testEmployee, result.getEmployee());
        assertEquals(LeaveRequest.Status.PENDING, result.getStatus());
        assertNotNull(result.getStartDate());
        assertNotNull(result.getEndDate());
        verify(employeeRepository, times(1)).findById(1L);
        verify(leaveRepository, times(1)).save(any(LeaveRequest.class));
    }

    /**
     * Test createLeaveRequest with non-existent employee - Edge case.
     */
    @Test
    public void testCreateLeaveRequest_NonExistentEmployee_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            leaveService.createLeaveRequest(999L, pendingLeaveRequest);
        });
        assertEquals("Employee not found", exception.getMessage());
        verify(employeeRepository, times(1)).findById(999L);
        verify(leaveRepository, never()).save(any(LeaveRequest.class));
    }

    /**
     * Test createLeaveRequest with null employee ID - Boundary condition.
     */
    @Test
    public void testCreateLeaveRequest_NullEmployeeId_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(null)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            leaveService.createLeaveRequest(null, pendingLeaveRequest);
        });
        verify(leaveRepository, never()).save(any(LeaveRequest.class));
    }

    /**
     * Test createLeaveRequest with same start and end date - Edge case.
     */
    @Test
    public void testCreateLeaveRequest_SameStartEndDate_Success() {
        // Arrange
        LocalDate sameDate = LocalDate.now().plusDays(5);
        LeaveRequest sameDateRequest = LeaveRequest.builder()
                .startDate(sameDate)
                .endDate(sameDate)
                .status(LeaveRequest.Status.PENDING)
                .build();
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        sameDateRequest.setEmployee(testEmployee);
        when(leaveRepository.save(any(LeaveRequest.class))).thenReturn(sameDateRequest);

        // Act
        LeaveRequest result = leaveService.createLeaveRequest(1L, sameDateRequest);

        // Assert
        assertNotNull(result);
        assertEquals(result.getStartDate(), result.getEndDate());
        verify(leaveRepository, times(1)).save(any(LeaveRequest.class));
    }

    /**
     * Test createLeaveRequest with end date before start date - Edge case.
     */
    @Test
    public void testCreateLeaveRequest_EndDateBeforeStartDate_Success() {
        // Arrange
        LeaveRequest invalidDateRequest = LeaveRequest.builder()
                .startDate(LocalDate.now().plusDays(10))
                .endDate(LocalDate.now().plusDays(5))
                .status(LeaveRequest.Status.PENDING)
                .build();
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        invalidDateRequest.setEmployee(testEmployee);
        when(leaveRepository.save(any(LeaveRequest.class))).thenReturn(invalidDateRequest);

        // Act
        LeaveRequest result = leaveService.createLeaveRequest(1L, invalidDateRequest);

        // Assert
        assertNotNull(result);
        assertTrue(result.getStartDate().isAfter(result.getEndDate()));
        verify(leaveRepository, times(1)).save(any(LeaveRequest.class));
    }

    /**
     * Test createLeaveRequest with past dates - Edge case.
     */
    @Test
    public void testCreateLeaveRequest_PastDates_Success() {
        // Arrange
        LeaveRequest pastDateRequest = LeaveRequest.builder()
                .startDate(LocalDate.now().minusDays(10))
                .endDate(LocalDate.now().minusDays(5))
                .status(LeaveRequest.Status.PENDING)
                .build();
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        pastDateRequest.setEmployee(testEmployee);
        when(leaveRepository.save(any(LeaveRequest.class))).thenReturn(pastDateRequest);

        // Act
        LeaveRequest result = leaveService.createLeaveRequest(1L, pastDateRequest);

        // Assert
        assertNotNull(result);
        assertTrue(result.getStartDate().isBefore(LocalDate.now()));
        verify(leaveRepository, times(1)).save(any(LeaveRequest.class));
    }

    /**
     * Test createLeaveRequest with very long duration - Edge case.
     */
    @Test
    public void testCreateLeaveRequest_LongDuration_Success() {
        // Arrange
        LeaveRequest longDurationRequest = LeaveRequest.builder()
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(90))
                .status(LeaveRequest.Status.PENDING)
                .build();
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        longDurationRequest.setEmployee(testEmployee);
        when(leaveRepository.save(any(LeaveRequest.class))).thenReturn(longDurationRequest);

        // Act
        LeaveRequest result = leaveService.createLeaveRequest(1L, longDurationRequest);

        // Assert
        assertNotNull(result);
        long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(result.getStartDate(), result.getEndDate());
        assertEquals(89, daysBetween);
        verify(leaveRepository, times(1)).save(any(LeaveRequest.class));
    }

    // ==================== UPDATE LEAVE STATUS TESTS ====================

    /**
     * Test updateLeaveStatus to APPROVED - Normal case.
     */
    @Test
    public void testUpdateLeaveStatus_ToApproved_Success() {
        // Arrange
        when(leaveRepository.findById(1L)).thenReturn(Optional.of(pendingLeaveRequest));
        LeaveRequest approvedVersion = LeaveRequest.builder()
                .id(1L)
                .employee(testEmployee)
                .startDate(pendingLeaveRequest.getStartDate())
                .endDate(pendingLeaveRequest.getEndDate())
                .status(LeaveRequest.Status.APPROVED)
                .build();
        when(leaveRepository.save(any(LeaveRequest.class))).thenReturn(approvedVersion);

        // Act
        LeaveRequest result = leaveService.updateLeaveStatus(1L, LeaveRequest.Status.APPROVED);

        // Assert
        assertNotNull(result);
        assertEquals(LeaveRequest.Status.APPROVED, result.getStatus());
        verify(leaveRepository, times(1)).findById(1L);
        verify(leaveRepository, times(1)).save(any(LeaveRequest.class));
    }

    /**
     * Test updateLeaveStatus to REJECTED - Normal case.
     */
    @Test
    public void testUpdateLeaveStatus_ToRejected_Success() {
        // Arrange
        when(leaveRepository.findById(1L)).thenReturn(Optional.of(pendingLeaveRequest));
        LeaveRequest rejectedVersion = LeaveRequest.builder()
                .id(1L)
                .employee(testEmployee)
                .startDate(pendingLeaveRequest.getStartDate())
                .endDate(pendingLeaveRequest.getEndDate())
                .status(LeaveRequest.Status.REJECTED)
                .build();
        when(leaveRepository.save(any(LeaveRequest.class))).thenReturn(rejectedVersion);

        // Act
        LeaveRequest result = leaveService.updateLeaveStatus(1L, LeaveRequest.Status.REJECTED);

        // Assert
        assertNotNull(result);
        assertEquals(LeaveRequest.Status.REJECTED, result.getStatus());
        verify(leaveRepository, times(1)).save(any(LeaveRequest.class));
    }

    /**
     * Test updateLeaveStatus with non-existent ID - Edge case.
     */
    @Test
    public void testUpdateLeaveStatus_NonExistentId_ThrowsException() {
        // Arrange
        when(leaveRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            leaveService.updateLeaveStatus(999L, LeaveRequest.Status.APPROVED);
        });
        assertEquals("Leave request not found", exception.getMessage());
        verify(leaveRepository, times(1)).findById(999L);
        verify(leaveRepository, never()).save(any(LeaveRequest.class));
    }

    /**
     * Test updateLeaveStatus from APPROVED back to PENDING - Edge case.
     */
    @Test
    public void testUpdateLeaveStatus_ApprovedToPending_Success() {
        // Arrange
        when(leaveRepository.findById(2L)).thenReturn(Optional.of(approvedLeaveRequest));
        LeaveRequest pendingVersion = LeaveRequest.builder()
                .id(2L)
                .employee(testEmployee)
                .startDate(approvedLeaveRequest.getStartDate())
                .endDate(approvedLeaveRequest.getEndDate())
                .status(LeaveRequest.Status.PENDING)
                .build();
        when(leaveRepository.save(any(LeaveRequest.class))).thenReturn(pendingVersion);

        // Act
        LeaveRequest result = leaveService.updateLeaveStatus(2L, LeaveRequest.Status.PENDING);

        // Assert
        assertNotNull(result);
        assertEquals(LeaveRequest.Status.PENDING, result.getStatus());
        verify(leaveRepository, times(1)).save(any(LeaveRequest.class));
    }

    /**
     * Test updateLeaveStatus with null status - Boundary condition.
     */
    @Test
    public void testUpdateLeaveStatus_NullStatus_Success() {
        // Arrange
        when(leaveRepository.findById(1L)).thenReturn(Optional.of(pendingLeaveRequest));
        LeaveRequest nullStatusVersion = LeaveRequest.builder()
                .id(1L)
                .employee(testEmployee)
                .startDate(pendingLeaveRequest.getStartDate())
                .endDate(pendingLeaveRequest.getEndDate())
                .status(null)
                .build();
        when(leaveRepository.save(any(LeaveRequest.class))).thenReturn(nullStatusVersion);

        // Act
        LeaveRequest result = leaveService.updateLeaveStatus(1L, null);

        // Assert
        assertNotNull(result);
        assertNull(result.getStatus());
        verify(leaveRepository, times(1)).save(any(LeaveRequest.class));
    }

    /**
     * Test updateLeaveStatus with null ID - Boundary condition.
     */
    @Test
    public void testUpdateLeaveStatus_NullId_ThrowsException() {
        // Arrange
        when(leaveRepository.findById(null)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            leaveService.updateLeaveStatus(null, LeaveRequest.Status.APPROVED);
        });
        verify(leaveRepository, never()).save(any(LeaveRequest.class));
    }

    /**
     * Test createLeaveRequest for inactive employee - Edge case.
     */
    @Test
    public void testCreateLeaveRequest_InactiveEmployee_Success() {
        // Arrange
        testEmployee.setActive(false);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(leaveRepository.save(any(LeaveRequest.class))).thenReturn(pendingLeaveRequest);

        // Act
        LeaveRequest result = leaveService.createLeaveRequest(1L, pendingLeaveRequest);

        // Assert
        assertNotNull(result);
        assertFalse(result.getEmployee().isActive());
        verify(leaveRepository, times(1)).save(any(LeaveRequest.class));
    }
}