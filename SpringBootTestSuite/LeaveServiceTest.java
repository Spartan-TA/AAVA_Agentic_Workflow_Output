package SpringBootTestSuite;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.*;

public class LeaveServiceTest {
    @Mock
    private LeaveRepository leaveRepository;
    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private LeaveService leaveService;

    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    void testRequestLeave_Valid_Success() {
        Employee employee = new Employee("John Doe", "B123", "WORKER", "Shipping", "A", new Date(), "ACTIVE");
        LeaveRequest request = new LeaveRequest(1L, 1L, "PTO", new Date(), new Date(), "PENDING");
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(leaveRepository.save(any(LeaveRequest.class))).thenReturn(request);
        LeaveRequest result = leaveService.requestLeave(1L, "PTO", new Date(), new Date());
        assertNotNull(result);
        assertEquals("PENDING", result.getStatus());
    }

    @Test
    void testRequestLeave_InsufficientBalance_ThrowsException() {
        Employee employee = new Employee("Jane Doe", "B124", "WORKER", "Shipping", "A", new Date(), "ACTIVE");
        when(employeeRepository.findById(2L)).thenReturn(Optional.of(employee));
        when(leaveRepository.getLeaveBalance(2L, "PTO")).thenReturn(0.0);
        assertThrows(InsufficientLeaveBalanceException.class, () -> leaveService.requestLeave(2L, "PTO", new Date(), new Date()));
    }

    @Test
    void testApproveLeave_Valid_Success() {
        LeaveRequest request = new LeaveRequest(1L, 1L, "PTO", new Date(), new Date(), "PENDING");
        when(leaveRepository.findById(1L)).thenReturn(Optional.of(request));
        when(leaveRepository.save(any(LeaveRequest.class))).thenReturn(request);
        LeaveRequest result = leaveService.approveLeave(1L);
        assertNotNull(result);
        assertEquals("APPROVED", result.getStatus());
    }

    @Test
    void testApproveLeave_InvalidId_ThrowsException() {
        when(leaveRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(LeaveRequestNotFoundException.class, () -> leaveService.approveLeave(99L));
    }

    @Test
    void testGetLeaveBalance_Valid_Success() {
        when(leaveRepository.getLeaveBalance(1L, "PTO")).thenReturn(5.0);
        double balance = leaveService.getLeaveBalance(1L, "PTO");
        assertEquals(5.0, balance);
    }

    @Test
    void testGetLeaveBalance_NegativeBalance_ThrowsException() {
        when(leaveRepository.getLeaveBalance(1L, "PTO")).thenReturn(-1.0);
        assertThrows(InvalidLeaveBalanceException.class, () -> leaveService.getLeaveBalance(1L, "PTO"));
    }

    @Test
    void testRequestLeave_NullType_ThrowsException() {
        assertThrows(InvalidLeaveRequestException.class, () -> leaveService.requestLeave(1L, null, new Date(), new Date()));
    }

    @Test
    void testRequestLeave_EmptyType_ThrowsException() {
        assertThrows(InvalidLeaveRequestException.class, () -> leaveService.requestLeave(1L, "", new Date(), new Date()));
    }

    // Integration scenario: Leave request auto-flags scheduled shifts
    @Test
    void testLeaveRequestAutoFlagsScheduledShifts_Success() {
        LeaveRequest request = new LeaveRequest(1L, 1L, "PTO", new Date(), new Date(), "APPROVED");
        when(leaveRepository.findById(1L)).thenReturn(Optional.of(request));
        doNothing().when(shiftService).flagShiftsForCoverage(1L, request.getStartDate(), request.getEndDate());
        leaveService.flagShiftsForLeave(1L);
        verify(shiftService).flagShiftsForCoverage(1L, request.getStartDate(), request.getEndDate());
    }
}
