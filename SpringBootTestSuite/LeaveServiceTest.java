import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.time.LocalDate;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class LeaveServiceTest {
    @Mock
    private LeaveRepository leaveRepository;
    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private LeaveService leaveService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRequestLeave_Valid() {
        Employee employee = new Employee();
        employee.setId(1L);
        Leave leave = new Leave();
        leave.setEmployee(employee);
        leave.setLeaveType("PTO");
        leave.setStartDate(LocalDate.now().plusDays(1));
        leave.setEndDate(LocalDate.now().plusDays(3));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(leaveRepository.save(any(Leave.class))).thenReturn(leave);
        Leave result = leaveService.requestLeave(1L, leave);
        assertNotNull(result);
        assertEquals("PENDING", result.getStatus());
        verify(leaveRepository, times(1)).save(any(Leave.class));
    }

    @Test
    void testRequestLeave_EmployeeNotFound() {
        Leave leave = new Leave();
        when(employeeRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> leaveService.requestLeave(1L, leave));
    }

    @Test
    void testRequestLeave_InvalidDateRange() {
        Employee employee = new Employee();
        employee.setId(1L);
        Leave leave = new Leave();
        leave.setStartDate(LocalDate.now().plusDays(3));
        leave.setEndDate(LocalDate.now().plusDays(1));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        assertThrows(ValidationException.class, () -> leaveService.requestLeave(1L, leave));
    }

    @Test
    void testApproveLeave_Valid() {
        Leave leave = new Leave();
        leave.setId(1L);
        leave.setStatus("PENDING");
        when(leaveRepository.findById(1L)).thenReturn(Optional.of(leave));
        when(leaveRepository.save(any(Leave.class))).thenReturn(leave);
        Leave result = leaveService.approveLeave(1L, 2L);
        assertNotNull(result);
        assertEquals("APPROVED", result.getStatus());
        assertEquals(2L, result.getApprovedBy());
    }

    @Test
    void testApproveLeave_LeaveNotFound() {
        when(leaveRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> leaveService.approveLeave(1L, 2L));
    }

    @Test
    void testRejectLeave_Valid() {
        Leave leave = new Leave();
        leave.setId(1L);
        leave.setStatus("PENDING");
        when(leaveRepository.findById(1L)).thenReturn(Optional.of(leave));
        when(leaveRepository.save(any(Leave.class))).thenReturn(leave);
        Leave result = leaveService.rejectLeave(1L, 2L);
        assertNotNull(result);
        assertEquals("REJECTED", result.getStatus());
    }
}