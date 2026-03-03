import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class LeaveServiceTest {
    @Mock
    private LeaveRequestRepository repository;
    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private LeaveService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Should request leave")
    void testRequestLeave_NormalCase() {
        Employee employee = new Employee(1L, "B123", "John Doe", "WORKER", "Shipping", "A", LocalDate.now(), EmployeeStatus.ACTIVE, false);
        LeaveRequest request = new LeaveRequest(null, employee, LeaveType.PTO, LocalDate.now(), LocalDate.now().plusDays(2), LeaveStatus.PENDING, "Vacation");

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(repository.save(any(LeaveRequest.class))).thenReturn(request);

        LeaveRequest result = service.requestLeave(1L, LeaveType.PTO, LocalDate.now(), LocalDate.now().plusDays(2), "Vacation");

        assertNotNull(result);
        assertEquals(LeaveStatus.PENDING, result.getStatus());
    }

    @Test
    @DisplayName("Should approve leave")
    void testApproveLeave_NormalCase() {
        LeaveRequest request = new LeaveRequest(1L, null, LeaveType.PTO, LocalDate.now(), LocalDate.now().plusDays(2), LeaveStatus.PENDING, "Vacation");

        when(repository.findById(1L)).thenReturn(Optional.of(request));
        when(repository.save(any(LeaveRequest.class))).thenReturn(request);

        LeaveRequest result = service.approveLeave(1L);

        assertEquals(LeaveStatus.APPROVED, result.getStatus());
    }

    @Test
    @DisplayName("Should deny leave")
    void testDenyLeave_NormalCase() {
        LeaveRequest request = new LeaveRequest(1L, null, LeaveType.PTO, LocalDate.now(), LocalDate.now().plusDays(2), LeaveStatus.PENDING, "Vacation");

        when(repository.findById(1L)).thenReturn(Optional.of(request));
        when(repository.save(any(LeaveRequest.class))).thenReturn(request);

        LeaveRequest result = service.denyLeave(1L);

        assertEquals(LeaveStatus.DENIED, result.getStatus());
    }

    @Test
    @DisplayName("Should calculate leave balance")
    void testCalculateBalance_NormalCase() {
        Employee employee = new Employee(1L, "B123", "John Doe", "WORKER", "Shipping", "A", LocalDate.now(), EmployeeStatus.ACTIVE, false);
        List<LeaveRequest> requests = Arrays.asList(
            new LeaveRequest(1L, employee, LeaveType.PTO, LocalDate.now(), LocalDate.now().plusDays(2), LeaveStatus.APPROVED, "Vacation"),
            new LeaveRequest(2L, employee, LeaveType.PTO, LocalDate.now().plusDays(10), LocalDate.now().plusDays(12), LeaveStatus.APPROVED, "Vacation")
        );
        when(repository.findByEmployeeIdAndStatus(1L, LeaveStatus.APPROVED)).thenReturn(requests);

        int balance = service.calculateBalance(1L);

        assertTrue(balance >= 0);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException for unknown leave request")
    void testApproveLeave_ResourceNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.approveLeave(99L));
    }

    @Test
    @DisplayName("Should handle null input for requestLeave")
    void testRequestLeave_NullInput() {
        assertThrows(ValidationException.class, () -> service.requestLeave(null, null, null, null, null));
    }

    @Test
    @DisplayName("Should handle empty leave requests in calculateBalance")
    void testCalculateBalance_EmptyRequests() {
        when(repository.findByEmployeeIdAndStatus(1L, LeaveStatus.APPROVED)).thenReturn(Collections.emptyList());

        int balance = service.calculateBalance(1L);

        assertEquals(0, balance);
    }
}