package com.warehouse.leave.service;

import com.warehouse.leave.entity.LeaveRequest;
import com.warehouse.leave.repository.LeaveRepository;
import com.warehouse.leave.dto.CreateLeaveRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.time.LocalDate;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LeaveServiceTest {
    @Mock
    private LeaveRepository leaveRepository;

    @InjectMocks
    private LeaveService leaveService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRequestLeave() {
        CreateLeaveRequest req = CreateLeaveRequest.builder()
                .type(LeaveRequest.LeaveType.SICK)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(2))
                .employeeId(1L)
                .build();
        LeaveRequest leave = LeaveRequest.builder()
                .type(req.getType())
                .startDate(req.getStartDate())
                .endDate(req.getEndDate())
                .status(LeaveRequest.LeaveStatus.REQUESTED)
                .balance(3.0)
                .employeeId(req.getEmployeeId())
                .build();
        when(leaveRepository.save(any(LeaveRequest.class))).thenReturn(leave);
        LeaveRequest result = leaveService.requestLeave(req);
        assertEquals(LeaveRequest.LeaveStatus.REQUESTED, result.getStatus());
        assertEquals(3.0, result.getBalance());
    }
}
