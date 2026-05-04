package com.warehouse.management.leave;

import com.warehouse.management.leave.LeaveService;
import com.warehouse.management.leave.LeaveRequest;
import com.warehouse.management.leave.LeaveBalance;
import com.warehouse.management.employee.Employee;
import org.junit.jupiter.api.*;
import org.mockito.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LeaveServiceTest {

    @Mock
    private LeaveRepository leaveRepository;

    @InjectMocks
    private LeaveService leaveService;

    private Employee employee;
    private LeaveRequest leaveRequest;
    private LeaveBalance leaveBalance;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        employee = new Employee(1L, "John Doe", "BADGE123", "WORKER", "Logistics", "A", new Date(), "ACTIVE");
        leaveRequest = new LeaveRequest(1L, employee, "PTO", new Date(), new Date(), "PENDING");
        leaveBalance = new LeaveBalance(employee, 10);
    }

    @Test
    void testRequestLeave_Valid() {
        when(leaveRepository.save(any(LeaveRequest.class))).thenReturn(leaveRequest);
        LeaveRequest result = leaveService.requestLeave(employee, "PTO", new Date(), new Date());
        assertNotNull(result);
        assertEquals("PTO", result.getType());
    }

    @Test
    void testRequestLeave_InsufficientBalance() {
        leaveBalance.setBalance(0);
        when(leaveService.getLeaveBalance(employee)).thenReturn(leaveBalance);
        assertThrows(IllegalStateException.class, () -> leaveService.requestLeave(employee, "PTO", new Date(), new Date()));
    }

    @Test
    void testApproveLeave_Valid() {
        when(leaveRepository.findById(1L)).thenReturn(Optional.of(leaveRequest));
        when(leaveRepository.save(any(LeaveRequest.class))).thenReturn(leaveRequest);
        LeaveRequest result = leaveService.approveLeave(1L);
        assertEquals("APPROVED", result.getStatus());
    }

    @Test
    void testApproveLeave_NonExistent() {
        when(leaveRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> leaveService.approveLeave(99L));
    }

    @Test
    void testUpdateBalance() {
        leaveBalance.setBalance(5);
        leaveService.updateBalance(employee, 10);
        assertEquals(10, leaveBalance.getBalance());
    }
}