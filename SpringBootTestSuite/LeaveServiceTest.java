package SpringBootTestSuite;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.service.LeaveService;
import com.example.repository.EmployeeRepository;
import com.example.repository.LeaveRepository;
import com.example.model.Employee;
import com.example.model.LeaveRequest;
import com.example.exception.EmployeeNotFoundException;
import com.example.exception.InsufficientLeaveBalanceException;
import com.example.exception.LeaveRequestNotFoundException;
import com.example.exception.LeaveAlreadyApprovedException;

@ExtendWith(MockitoExtension.class)
public class LeaveServiceTest {
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private LeaveRepository leaveRepository;
    @InjectMocks
    private LeaveService leaveService;

    private Employee employee;
    private LeaveRequest leaveRequest;

    @BeforeEach
    void setUp() {
        employee = new Employee();
        employee.setId(1L);
        employee.setLeaveBalance(10);
        leaveRequest = new LeaveRequest();
        leaveRequest.setId(1L);
        leaveRequest.setEmployee(employee);
        leaveRequest.setStatus("PENDING");
        leaveRequest.setDaysRequested(3);
    }

    @Test
    void testCreateLeaveRequest_Success() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(leaveRepository.save(any(LeaveRequest.class))).thenReturn(leaveRequest);
        LeaveRequest result = leaveService.createLeaveRequest(1L, 3);
        assertNotNull(result);
        assertEquals("PENDING", result.getStatus());
        verify(leaveRepository).save(any(LeaveRequest.class));
    }

    @Test
    void testCreateLeaveRequest_EmployeeNotFound_ThrowsException() {
        when(employeeRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(EmployeeNotFoundException.class, () -> leaveService.createLeaveRequest(2L, 3));
    }

    @Test
    void testCreateLeaveRequest_InsufficientBalance_ThrowsException() {
        employee.setLeaveBalance(2);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        assertThrows(InsufficientLeaveBalanceException.class, () -> leaveService.createLeaveRequest(1L, 3));
    }

    @Test
    void testApproveLeaveRequest_Success() {
        leaveRequest.setStatus("PENDING");
        when(leaveRepository.findById(1L)).thenReturn(Optional.of(leaveRequest));
        LeaveRequest result = leaveService.approveLeaveRequest(1L);
        assertEquals("APPROVED", result.getStatus());
        verify(leaveRepository).save(leaveRequest);
    }

    @Test
    void testApproveLeaveRequest_NotFound_ThrowsException() {
        when(leaveRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(LeaveRequestNotFoundException.class, () -> leaveService.approveLeaveRequest(2L));
    }

    @Test
    void testApproveLeaveRequest_AlreadyApproved_ThrowsException() {
        leaveRequest.setStatus("APPROVED");
        when(leaveRepository.findById(1L)).thenReturn(Optional.of(leaveRequest));
        assertThrows(LeaveAlreadyApprovedException.class, () -> leaveService.approveLeaveRequest(1L));
    }

    @Test
    void testRejectLeaveRequest_Success() {
        leaveRequest.setStatus("PENDING");
        when(leaveRepository.findById(1L)).thenReturn(Optional.of(leaveRequest));
        LeaveRequest result = leaveService.rejectLeaveRequest(1L);
        assertEquals("REJECTED", result.getStatus());
        verify(leaveRepository).save(leaveRequest);
    }

    @Test
    void testGetLeaveBalance_Success() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        int balance = leaveService.getLeaveBalance(1L);
        assertEquals(10, balance);
    }

    @Test
    void testGetLeaveBalance_EmployeeNotFound_ThrowsException() {
        when(employeeRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(EmployeeNotFoundException.class, () -> leaveService.getLeaveBalance(2L));
    }

    @Test
    void testGetPendingLeaveRequests_ReturnsCorrectList() {
        List<LeaveRequest> pending = Arrays.asList(leaveRequest);
        when(leaveRepository.findByStatus("PENDING")).thenReturn(pending);
        List<LeaveRequest> result = leaveService.getPendingLeaveRequests();
        assertEquals(1, result.size());
        assertEquals("PENDING", result.get(0).getStatus());
    }
}
