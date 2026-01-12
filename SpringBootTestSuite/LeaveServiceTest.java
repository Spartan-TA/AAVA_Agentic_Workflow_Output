package com.warehouse.ems.service;

import com.warehouse.ems.entity.LeaveRequest;
import com.warehouse.ems.repository.LeaveRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeaveServiceTest {

    @Mock
    private LeaveRepository leaveRepository;

    @InjectMocks
    private LeaveService leaveService;

    private LeaveRequest request;

    @BeforeEach
    void setUp() {
        request = new LeaveRequest(1L, 1L, "SICK", LocalDate.now(), LocalDate.now().plusDays(2), "PENDING", "Flu", 2L);
    }

    @Test
    void testRequestLeave_ValidInput_ReturnsRequest() {
        when(leaveRepository.save(any(LeaveRequest.class))).thenReturn(request);

        LeaveRequest result = leaveService.requestLeave(request);

        assertNotNull(result);
        assertEquals("SICK", result.getType());
    }

    @Test
    void testRequestLeave_NullInput_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> leaveService.requestLeave(null));
    }

    @Test
    void testApproveLeave_ValidInput_ReturnsApproved() {
        LeaveRequest approved = new LeaveRequest(1L, 1L, "SICK", LocalDate.now(), LocalDate.now().plusDays(2), "APPROVED", "Flu", 2L);
        when(leaveRepository.findById(1L)).thenReturn(Optional.of(request));
        when(leaveRepository.save(any(LeaveRequest.class))).thenReturn(approved);

        LeaveRequest result = leaveService.approveLeave(1L, 2L);

        assertEquals("APPROVED", result.getStatus());
    }

    @Test
    void testApproveLeave_NonExistingId_ThrowsException() {
        when(leaveRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> leaveService.approveLeave(2L, 2L));
    }

    @Test
    void testGetLeaveRequestsByEmployeeId_ReturnsList() {
        List<LeaveRequest> requests = Arrays.asList(request);
        when(leaveRepository.findByEmployeeId(1L)).thenReturn(requests);

        List<LeaveRequest> result = leaveService.getLeaveRequestsByEmployeeId(1L);

        assertEquals(1, result.size());
    }

    @Test
    void testGetLeaveRequestsByEmployeeId_EmptyList() {
        when(leaveRepository.findByEmployeeId(2L)).thenReturn(Collections.emptyList());

        List<LeaveRequest> result = leaveService.getLeaveRequestsByEmployeeId(2L);

        assertTrue(result.isEmpty());
    }
}