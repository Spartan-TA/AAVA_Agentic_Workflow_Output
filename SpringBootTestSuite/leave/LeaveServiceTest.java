package com.companyname.wems.leave.service;

import com.companyname.wems.leave.model.LeaveRequest;
import com.companyname.wems.leave.repository.LeaveRepository;
import com.companyname.wems.leave.exception.LeaveException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class LeaveServiceTest {
    @Mock
    private LeaveRepository leaveRepository;

    @InjectMocks
    private LeaveService leaveService;

    private LeaveRequest testLeaveRequest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testLeaveRequest = new LeaveRequest();
        testLeaveRequest.setId(1L);
        testLeaveRequest.setEmployeeId(100L);
        testLeaveRequest.setStartDate(LocalDate.of(2023, 6, 10));
        testLeaveRequest.setEndDate(LocalDate.of(2023, 6, 12));
        testLeaveRequest.setType("PTO");
        testLeaveRequest.setStatus("PENDING");
    }

    @Test
    void testRequestLeave_ValidInput_Success() {
        when(leaveRepository.save(any(LeaveRequest.class))).thenReturn(testLeaveRequest);
        LeaveRequest result = leaveService.requestLeave(100L, LocalDate.of(2023, 6, 10), LocalDate.of(2023, 6, 12), "PTO");
        assertNotNull(result);
        assertEquals("PENDING", result.getStatus());
    }

    @Test
    void testRequestLeave_NullEmployeeId_ThrowsException() {
        assertThrows(LeaveException.class, () -> leaveService.requestLeave(null, LocalDate.of(2023, 6, 10), LocalDate.of(2023, 6, 12), "PTO"));
    }

    @Test
    void testRequestLeave_InvalidDates_ThrowsException() {
        assertThrows(LeaveException.class, () -> leaveService.requestLeave(100L, null, LocalDate.of(2023, 6, 12), "PTO"));
        assertThrows(LeaveException.class, () -> leaveService.requestLeave(100L, LocalDate.of(2023, 6, 10), null, "PTO"));
    }

    @Test
    void testApproveLeave_ValidInput_Success() {
        testLeaveRequest.setStatus("APPROVED");
        when(leaveRepository.findById(1L)).thenReturn(Optional.of(testLeaveRequest));
        when(leaveRepository.save(any(LeaveRequest.class))).thenReturn(testLeaveRequest);
        LeaveRequest result = leaveService.approveLeave(1L);
        assertEquals("APPROVED", result.getStatus());
    }

    @Test
    void testApproveLeave_InvalidId_ThrowsException() {
        when(leaveRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(LeaveException.class, () -> leaveService.approveLeave(999L));
    }
}
