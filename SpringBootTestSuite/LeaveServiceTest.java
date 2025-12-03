package com.company.wems.leave.service;

import com.company.wems.leave.dto.LeaveRequestDTO;
import com.company.wems.leave.entity.LeaveRequest;
import com.company.wems.leave.repository.LeaveRequestRepository;
import com.company.wems.employee.entity.Employee;
import com.company.wems.employee.repository.EmployeeRepository;
import com.company.wems.common.exception.ResourceNotFoundException;
import com.company.wems.common.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

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
 * Tests cover leave request management, approval workflow, and edge cases
 */
@DisplayName("Leave Service Tests")
public class LeaveServiceTest {

    @Mock
    private LeaveRequestRepository leaveRequestRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private LeaveService leaveService;

    private Employee validEmployee;
    private Employee validApprover;
    private LeaveRequest validLeaveRequest;
    private LeaveRequestDTO validLeaveRequestDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Setup valid employee
        validEmployee = new Employee();
        validEmployee.setId(1L);
        validEmployee.setBadgeId("EMP001");
        validEmployee.setFirstName("John");
        validEmployee.setLastName("Doe");
        validEmployee.setDeleted(false);
        
        // Setup valid approver
        validApprover = new Employee();
        validApprover.setId(2L);
        validApprover.setBadgeId("MGR001");
        validApprover.setFirstName("Jane");
        validApprover.setLastName("Smith");
        validApprover.setRole("SUPERVISOR");
        validApprover.setDeleted(false);
        
        // Setup valid leave request
        validLeaveRequest = new LeaveRequest();
        validLeaveRequest.setId(1L);
        validLeaveRequest.setEmployee(validEmployee);
        validLeaveRequest.setType(LeaveRequest.LeaveType.PTO);
        validLeaveRequest.setStartDate(LocalDate.now().plusDays(7));
        validLeaveRequest.setEndDate(LocalDate.now().plusDays(10));
        validLeaveRequest.setStatus(LeaveRequest.RequestStatus.PENDING);
        validLeaveRequest.setReason("Family vacation");
        
        // Setup valid DTO
        validLeaveRequestDTO = new LeaveRequestDTO();
        validLeaveRequestDTO.setEmployeeId(1L);
        validLeaveRequestDTO.setType("PTO");
        validLeaveRequestDTO.setStartDate(LocalDate.now().plusDays(7));
        validLeaveRequestDTO.setEndDate(LocalDate.now().plusDays(10));
        validLeaveRequestDTO.setReason("Family vacation");
    }

    // ==================== CREATE LEAVE REQUEST TESTS ====================

    @Test
    @DisplayName("Create Leave Request - Valid Input - Should Create Successfully")
    void testCreateLeaveRequest_WithValidInput_ShouldCreateSuccessfully() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenReturn(validLeaveRequest);

        // Act
        LeaveRequestDTO result = leaveService.createLeaveRequest(validLeaveRequestDTO);

        // Assert
        assertNotNull(result);
        verify(leaveRequestRepository, times(1)).save(any(LeaveRequest.class));
    }

    @Test
    @DisplayName("Create Leave Request - Non-Existent Employee - Should Throw ResourceNotFoundException")
    void testCreateLeaveRequest_WithNonExistentEmployee_ShouldThrowException() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());
        validLeaveRequestDTO.setEmployeeId(999L);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            leaveService.createLeaveRequest(validLeaveRequestDTO);
        });
        verify(leaveRequestRepository, never()).save(any(LeaveRequest.class));
    }

    @Test
    @DisplayName("Create Leave Request - Start Date After End Date - Should Throw BusinessException")
    void testCreateLeaveRequest_WithStartDateAfterEndDate_ShouldThrowException() {
        // Arrange
        validLeaveRequestDTO.setStartDate(LocalDate.now().plusDays(10));
        validLeaveRequestDTO.setEndDate(LocalDate.now().plusDays(7));

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            leaveService.createLeaveRequest(validLeaveRequestDTO);
        });
    }

    @Test
    @DisplayName("Create Leave Request - Past Start Date - Should Throw BusinessException")
    void testCreateLeaveRequest_WithPastStartDate_ShouldThrowException() {
        // Arrange
        validLeaveRequestDTO.setStartDate(LocalDate.now().minusDays(1));
        validLeaveRequestDTO.setEndDate(LocalDate.now().plusDays(3));

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            leaveService.createLeaveRequest(validLeaveRequestDTO);
        });
    }

    @Test
    @DisplayName("Create Leave Request - Null Start Date - Should Throw Exception")
    void testCreateLeaveRequest_WithNullStartDate_ShouldThrowException() {
        // Arrange
        validLeaveRequestDTO.setStartDate(null);

        // Act & Assert
        assertThrows(Exception.class, () -> {
            leaveService.createLeaveRequest(validLeaveRequestDTO);
        });
    }

    @Test
    @DisplayName("Create Leave Request - Null End Date - Should Throw Exception")
    void testCreateLeaveRequest_WithNullEndDate_ShouldThrowException() {
        // Arrange
        validLeaveRequestDTO.setEndDate(null);

        // Act & Assert
        assertThrows(Exception.class, () -> {
            leaveService.createLeaveRequest(validLeaveRequestDTO);
        });
    }

    @Test
    @DisplayName("Create Leave Request - Empty Reason - Should Create Successfully")
    void testCreateLeaveRequest_WithEmptyReason_ShouldCreateSuccessfully() {
        // Arrange
        validLeaveRequestDTO.setReason("");
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenReturn(validLeaveRequest);

        // Act
        LeaveRequestDTO result = leaveService.createLeaveRequest(validLeaveRequestDTO);

        // Assert
        assertNotNull(result);
    }

    // ==================== APPROVE LEAVE REQUEST TESTS ====================

    @Test
    @DisplayName("Approve Leave Request - Valid Request - Should Approve Successfully")
    void testApproveLeaveRequest_WithValidRequest_ShouldApproveSuccessfully() {
        // Arrange
        Long requestId = 1L;
        Long approverId = 2L;
        when(leaveRequestRepository.findById(requestId)).thenReturn(Optional.of(validLeaveRequest));
        when(employeeRepository.findById(approverId)).thenReturn(Optional.of(validApprover));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenReturn(validLeaveRequest);

        // Act
        LeaveRequestDTO result = leaveService.approveLeaveRequest(requestId, approverId);

        // Assert
        assertNotNull(result);
        assertEquals(LeaveRequest.RequestStatus.APPROVED, validLeaveRequest.getStatus());
        assertEquals(validApprover, validLeaveRequest.getApprover());
        verify(leaveRequestRepository, times(1)).save(validLeaveRequest);
    }

    @Test
    @DisplayName("Approve Leave Request - Non-Existent Request - Should Throw ResourceNotFoundException")
    void testApproveLeaveRequest_WithNonExistentRequest_ShouldThrowException() {
        // Arrange
        Long requestId = 999L;
        Long approverId = 2L;
        when(leaveRequestRepository.findById(requestId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            leaveService.approveLeaveRequest(requestId, approverId);
        });
    }

    @Test
    @DisplayName("Approve Leave Request - Already Approved - Should Throw BusinessException")
    void testApproveLeaveRequest_WhenAlreadyApproved_ShouldThrowException() {
        // Arrange
        Long requestId = 1L;
        Long approverId = 2L;
        validLeaveRequest.setStatus(LeaveRequest.RequestStatus.APPROVED);
        when(leaveRequestRepository.findById(requestId)).thenReturn(Optional.of(validLeaveRequest));

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            leaveService.approveLeaveRequest(requestId, approverId);
        });
    }

    @Test
    @DisplayName("Approve Leave Request - Non-Existent Approver - Should Throw ResourceNotFoundException")
    void testApproveLeaveRequest_WithNonExistentApprover_ShouldThrowException() {
        // Arrange
        Long requestId = 1L;
        Long approverId = 999L;
        when(leaveRequestRepository.findById(requestId)).thenReturn(Optional.of(validLeaveRequest));
        when(employeeRepository.findById(approverId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            leaveService.approveLeaveRequest(requestId, approverId);
        });
    }

    // ==================== REJECT LEAVE REQUEST TESTS ====================

    @Test
    @DisplayName("Reject Leave Request - Valid Request - Should Reject Successfully")
    void testRejectLeaveRequest_WithValidRequest_ShouldRejectSuccessfully() {
        // Arrange
        Long requestId = 1L;
        Long approverId = 2L;
        String rejectionReason = "Insufficient staffing";
        when(leaveRequestRepository.findById(requestId)).thenReturn(Optional.of(validLeaveRequest));
        when(employeeRepository.findById(approverId)).thenReturn(Optional.of(validApprover));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenReturn(validLeaveRequest);

        // Act
        LeaveRequestDTO result = leaveService.rejectLeaveRequest(requestId, approverId, rejectionReason);

        // Assert
        assertNotNull(result);
        assertEquals(LeaveRequest.RequestStatus.REJECTED, validLeaveRequest.getStatus());
        verify(leaveRequestRepository, times(1)).save(validLeaveRequest);
    }

    @Test
    @DisplayName("Reject Leave Request - Already Rejected - Should Throw BusinessException")
    void testRejectLeaveRequest_WhenAlreadyRejected_ShouldThrowException() {
        // Arrange
        Long requestId = 1L;
        Long approverId = 2L;
        validLeaveRequest.setStatus(LeaveRequest.RequestStatus.REJECTED);
        when(leaveRequestRepository.findById(requestId)).thenReturn(Optional.of(validLeaveRequest));

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            leaveService.rejectLeaveRequest(requestId, approverId, "Reason");
        });
    }

    @Test
    @DisplayName("Reject Leave Request - Null Rejection Reason - Should Reject with Null Reason")
    void testRejectLeaveRequest_WithNullReason_ShouldRejectSuccessfully() {
        // Arrange
        Long requestId = 1L;
        Long approverId = 2L;
        when(leaveRequestRepository.findById(requestId)).thenReturn(Optional.of(validLeaveRequest));
        when(employeeRepository.findById(approverId)).thenReturn(Optional.of(validApprover));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenReturn(validLeaveRequest);

        // Act
        LeaveRequestDTO result = leaveService.rejectLeaveRequest(requestId, approverId, null);

        // Assert
        assertNotNull(result);
        assertEquals(LeaveRequest.RequestStatus.REJECTED, validLeaveRequest.getStatus());
    }

    // ==================== CANCEL LEAVE REQUEST TESTS ====================

    @Test
    @DisplayName("Cancel Leave Request - Valid Request - Should Cancel Successfully")
    void testCancelLeaveRequest_WithValidRequest_ShouldCancelSuccessfully() {
        // Arrange
        Long requestId = 1L;
        when(leaveRequestRepository.findById(requestId)).thenReturn(Optional.of(validLeaveRequest));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenReturn(validLeaveRequest);

        // Act
        LeaveRequestDTO result = leaveService.cancelLeaveRequest(requestId);

        // Assert
        assertNotNull(result);
        assertEquals(LeaveRequest.RequestStatus.CANCELLED, validLeaveRequest.getStatus());
        verify(leaveRequestRepository, times(1)).save(validLeaveRequest);
    }

    @Test
    @DisplayName("Cancel Leave Request - Already Cancelled - Should Throw BusinessException")
    void testCancelLeaveRequest_WhenAlreadyCancelled_ShouldThrowException() {
        // Arrange
        Long requestId = 1L;
        validLeaveRequest.setStatus(LeaveRequest.RequestStatus.CANCELLED);
        when(leaveRequestRepository.findById(requestId)).thenReturn(Optional.of(validLeaveRequest));

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            leaveService.cancelLeaveRequest(requestId);
        });
    }

    @Test
    @DisplayName("Cancel Leave Request - Already Approved - Should Throw BusinessException")
    void testCancelLeaveRequest_WhenAlreadyApproved_ShouldThrowException() {
        // Arrange
        Long requestId = 1L;
        validLeaveRequest.setStatus(LeaveRequest.RequestStatus.APPROVED);
        when(leaveRequestRepository.findById(requestId)).thenReturn(Optional.of(validLeaveRequest));

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            leaveService.cancelLeaveRequest(requestId);
        });
    }

    // ==================== GET LEAVE REQUESTS TESTS ====================

    @Test
    @DisplayName("Get Leave Requests By Employee - Valid Employee - Should Return List")
    void testGetLeaveRequestsByEmployee_WithValidEmployee_ShouldReturnList() {
        // Arrange
        Long employeeId = 1L;
        List<LeaveRequest> requests = Arrays.asList(validLeaveRequest);
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(validEmployee));
        when(leaveRequestRepository.findByEmployeeIdOrderByStartDateDesc(employeeId)).thenReturn(requests);

        // Act
        List<LeaveRequestDTO> result = leaveService.getLeaveRequestsByEmployee(employeeId);

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Get Leave Requests By Employee - Non-Existent Employee - Should Throw ResourceNotFoundException")
    void testGetLeaveRequestsByEmployee_WithNonExistentEmployee_ShouldThrowException() {
        // Arrange
        Long employeeId = 999L;
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            leaveService.getLeaveRequestsByEmployee(employeeId);
        });
    }

    @Test
    @DisplayName("Get Leave Requests By Employee - No Requests - Should Return Empty List")
    void testGetLeaveRequestsByEmployee_WithNoRequests_ShouldReturnEmptyList() {
        // Arrange
        Long employeeId = 1L;
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(validEmployee));
        when(leaveRequestRepository.findByEmployeeIdOrderByStartDateDesc(employeeId)).thenReturn(Arrays.asList());

        // Act
        List<LeaveRequestDTO> result = leaveService.getLeaveRequestsByEmployee(employeeId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ==================== BOUNDARY AND EDGE CASE TESTS ====================

    @Test
    @DisplayName("Create Leave Request - Single Day Leave - Should Create Successfully")
    void testCreateLeaveRequest_ForSingleDay_ShouldCreateSuccessfully() {
        // Arrange
        LocalDate singleDay = LocalDate.now().plusDays(7);
        validLeaveRequestDTO.setStartDate(singleDay);
        validLeaveRequestDTO.setEndDate(singleDay);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenReturn(validLeaveRequest);

        // Act
        LeaveRequestDTO result = leaveService.createLeaveRequest(validLeaveRequestDTO);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Create Leave Request - Maximum Reason Length - Should Create Successfully")
    void testCreateLeaveRequest_WithMaxReasonLength_ShouldCreateSuccessfully() {
        // Arrange
        String maxLengthReason = "R".repeat(1000);
        validLeaveRequestDTO.setReason(maxLengthReason);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenReturn(validLeaveRequest);

        // Act
        LeaveRequestDTO result = leaveService.createLeaveRequest(validLeaveRequestDTO);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Create Leave Request - All Leave Types - Should Create Successfully")
    void testCreateLeaveRequest_WithAllLeaveTypes_ShouldCreateSuccessfully() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenReturn(validLeaveRequest);

        // Test each leave type
        String[] leaveTypes = {"PTO", "SICK", "UNPAID", "BEREAVEMENT", "MATERNITY", "PATERNITY"};
        for (String type : leaveTypes) {
            validLeaveRequestDTO.setType(type);
            LeaveRequestDTO result = leaveService.createLeaveRequest(validLeaveRequestDTO);
            assertNotNull(result);
        }
    }
}