package com.company.wms.leave.service;

import com.company.wms.leave.domain.LeaveRequest;
import com.company.wms.leave.domain.LeaveRequest.LeaveStatus;
import com.company.wms.leave.domain.LeaveRequest.LeaveType;
import com.company.wms.leave.repository.LeaveRepository;
import com.company.wms.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for LeaveService.
 * Tests leave request creation, approval/denial workflow, and balance calculations.
 */
@ExtendWith(MockitoExtension.class)
class LeaveServiceTest {

    @Mock
    private LeaveRepository leaveRepository;

    @InjectMocks
    private LeaveService leaveService;

    private LeaveRequest testLeaveRequest;
    private Long employeeId = 1L;
    private Long approverId = 2L;

    @BeforeEach
    void setUp() {
        testLeaveRequest = LeaveRequest.builder()
            .id(1L)
            .employeeId(employeeId)
            .leaveType(LeaveType.PTO)
            .startDate(LocalDate.now().plusDays(7))
            .endDate(LocalDate.now().plusDays(10))
            .reason("Vacation")
            .status(LeaveStatus.PENDING)
            .build();
    }

    @Test
    void testCreateLeaveRequest_Success() {
        // Given
        when(leaveRepository.findOverlappingLeaveRequests(
            anyLong(), any(LocalDate.class), any(LocalDate.class), any(LeaveStatus.class)
        )).thenReturn(Arrays.asList());
        when(leaveRepository.save(any(LeaveRequest.class))).thenReturn(testLeaveRequest);

        // When
        LeaveRequest created = leaveService.createLeaveRequest(testLeaveRequest);

        // Then
        assertNotNull(created);
        assertEquals(LeaveStatus.PENDING, created.getStatus());
        verify(leaveRepository, times(1)).save(any(LeaveRequest.class));
    }

    @Test
    void testCreateLeaveRequest_InvalidDates() {
        // Given
        testLeaveRequest.setEndDate(LocalDate.now().minusDays(1));

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            leaveService.createLeaveRequest(testLeaveRequest);
        });
        verify(leaveRepository, never()).save(any(LeaveRequest.class));
    }

    @Test
    void testCreateLeaveRequest_OverlappingLeave() {
        // Given
        LeaveRequest overlapping = LeaveRequest.builder()
            .id(2L)
            .employeeId(employeeId)
            .startDate(LocalDate.now().plusDays(8))
            .endDate(LocalDate.now().plusDays(12))
            .status(LeaveStatus.APPROVED)
            .build();
        
        when(leaveRepository.findOverlappingLeaveRequests(
            anyLong(), any(LocalDate.class), any(LocalDate.class), any(LeaveStatus.class)
        )).thenReturn(Arrays.asList(overlapping));

        // When & Then
        assertThrows(IllegalStateException.class, () -> {
            leaveService.createLeaveRequest(testLeaveRequest);
        });
        verify(leaveRepository, never()).save(any(LeaveRequest.class));
    }

    @Test
    void testApproveLeaveRequest_Success() {
        // Given
        when(leaveRepository.findById(1L)).thenReturn(Optional.of(testLeaveRequest));
        when(leaveRepository.save(any(LeaveRequest.class))).thenReturn(testLeaveRequest);

        // When
        LeaveRequest approved = leaveService.approveLeaveRequest(1L, approverId, "Approved");

        // Then
        assertNotNull(approved);
        assertEquals(LeaveStatus.APPROVED, approved.getStatus());
        assertEquals(approverId, approved.getApproverId());
        assertNotNull(approved.getApprovedAt());
        verify(leaveRepository, times(1)).save(any(LeaveRequest.class));
    }

    @Test
    void testApproveLeaveRequest_NotFound() {
        // Given
        when(leaveRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            leaveService.approveLeaveRequest(1L, approverId, "Approved");
        });
        verify(leaveRepository, never()).save(any(LeaveRequest.class));
    }

    @Test
    void testApproveLeaveRequest_AlreadyApproved() {
        // Given
        testLeaveRequest.setStatus(LeaveStatus.APPROVED);
        when(leaveRepository.findById(1L)).thenReturn(Optional.of(testLeaveRequest));

        // When & Then
        assertThrows(IllegalStateException.class, () -> {
            leaveService.approveLeaveRequest(1L, approverId, "Approved");
        });
        verify(leaveRepository, never()).save(any(LeaveRequest.class));
    }

    @Test
    void testDenyLeaveRequest_Success() {
        // Given
        when(leaveRepository.findById(1L)).thenReturn(Optional.of(testLeaveRequest));
        when(leaveRepository.save(any(LeaveRequest.class))).thenReturn(testLeaveRequest);

        // When
        LeaveRequest denied = leaveService.denyLeaveRequest(1L, approverId, "Insufficient coverage");

        // Then
        assertNotNull(denied);
        assertEquals(LeaveStatus.DENIED, denied.getStatus());
        assertEquals(approverId, denied.getApproverId());
        assertEquals("Insufficient coverage", denied.getApproverComments());
        verify(leaveRepository, times(1)).save(any(LeaveRequest.class));
    }

    @Test
    void testDenyLeaveRequest_MissingReason() {
        // Given
        when(leaveRepository.findById(1L)).thenReturn(Optional.of(testLeaveRequest));

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            leaveService.denyLeaveRequest(1L, approverId, null);
        });
        verify(leaveRepository, never()).save(any(LeaveRequest.class));
    }

    @Test
    void testGetLeaveRequestsByEmployee() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<LeaveRequest> page = new PageImpl<>(Arrays.asList(testLeaveRequest));
        when(leaveRepository.findByEmployeeIdOrderByCreatedAtDesc(employeeId, pageable)).thenReturn(page);

        // When
        Page<LeaveRequest> result = leaveService.getLeaveRequestsByEmployee(employeeId, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(leaveRepository, times(1)).findByEmployeeIdOrderByCreatedAtDesc(employeeId, pageable);
    }

    @Test
    void testGetPendingLeaveRequests() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<LeaveRequest> page = new PageImpl<>(Arrays.asList(testLeaveRequest));
        when(leaveRepository.findByStatusOrderByCreatedAtAsc(LeaveStatus.PENDING, pageable)).thenReturn(page);

        // When
        Page<LeaveRequest> result = leaveService.getPendingLeaveRequests(pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(leaveRepository, times(1)).findByStatusOrderByCreatedAtAsc(LeaveStatus.PENDING, pageable);
    }

    @Test
    void testCancelLeaveRequest_Success() {
        // Given
        when(leaveRepository.findById(1L)).thenReturn(Optional.of(testLeaveRequest));
        when(leaveRepository.save(any(LeaveRequest.class))).thenReturn(testLeaveRequest);

        // When
        LeaveRequest cancelled = leaveService.cancelLeaveRequest(1L, employeeId);

        // Then
        assertNotNull(cancelled);
        assertEquals(LeaveStatus.CANCELLED, cancelled.getStatus());
        verify(leaveRepository, times(1)).save(any(LeaveRequest.class));
    }

    @Test
    void testCancelLeaveRequest_UnauthorizedEmployee() {
        // Given
        when(leaveRepository.findById(1L)).thenReturn(Optional.of(testLeaveRequest));

        // When & Then
        assertThrows(IllegalStateException.class, () -> {
            leaveService.cancelLeaveRequest(1L, 999L);
        });
        verify(leaveRepository, never()).save(any(LeaveRequest.class));
    }

    @Test
    void testCalculateLeaveBalance() {
        // Given
        LeaveRequest approved1 = LeaveRequest.builder()
            .startDate(LocalDate.now().minusDays(10))
            .endDate(LocalDate.now().minusDays(8))
            .build();
        LeaveRequest approved2 = LeaveRequest.builder()
            .startDate(LocalDate.now().minusDays(5))
            .endDate(LocalDate.now().minusDays(3))
            .build();
        
        when(leaveRepository.findByEmployeeIdAndLeaveTypeAndStatus(
            employeeId, LeaveType.PTO, LeaveStatus.APPROVED
        )).thenReturn(Arrays.asList(approved1, approved2));

        // When
        double balance = leaveService.calculateLeaveBalance(employeeId, LeaveType.PTO);

        // Then
        assertTrue(balance >= 0);
        assertTrue(balance <= 15.0);
        verify(leaveRepository, times(1)).findByEmployeeIdAndLeaveTypeAndStatus(
            employeeId, LeaveType.PTO, LeaveStatus.APPROVED
        );
    }
}