import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
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
    void testRequestLeave_ValidInput() {
        Employee employee = new Employee("John Doe", "B123", "WORKER", "Shipping", "A", new Date(), "ACTIVE");
        LeaveRequest request = new LeaveRequest(employee, "PTO", new Date(), new Date(), "Pending");
        when(employeeRepository.findByBadgeId("B123")).thenReturn(Optional.of(employee));
        when(leaveRepository.save(any(LeaveRequest.class))).thenReturn(request);
        LeaveRequest result = leaveService.requestLeave("B123", "PTO", new Date(), new Date());
        assertEquals("PTO", result.getType());
        assertEquals("Pending", result.getStatus());
    }

    @Test
    void testRequestLeave_NullInput() {
        assertThrows(IllegalArgumentException.class, () -> leaveService.requestLeave(null, "PTO", new Date(), new Date()));
    }

    @Test
    void testRequestLeave_InvalidBadgeId() {
        when(employeeRepository.findByBadgeId("BADGE999")).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> leaveService.requestLeave("BADGE999", "PTO", new Date(), new Date()));
    }

    @Test
    void testRequestLeave_InvalidDateRange() {
        Employee employee = new Employee("John Doe", "B123", "WORKER", "Shipping", "A", new Date(), "ACTIVE");
        when(employeeRepository.findByBadgeId("B123")).thenReturn(Optional.of(employee));
        Date start = new Date();
        Date end = new Date(System.currentTimeMillis() - 86400000); // yesterday
        assertThrows(ValidationException.class, () -> leaveService.requestLeave("B123", "PTO", start, end));
    }

    @Test
    void testApproveLeave_ValidInput() {
        LeaveRequest request = new LeaveRequest(null, "PTO", new Date(), new Date(), "Pending");
        when(leaveRepository.findById(1L)).thenReturn(Optional.of(request));
        when(leaveRepository.save(any(LeaveRequest.class))).thenReturn(request);
        LeaveRequest result = leaveService.approveLeave(1L);
        assertEquals("Approved", result.getStatus());
    }

    @Test
    void testApproveLeave_InvalidId() {
        when(leaveRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> leaveService.approveLeave(999L));
    }

    @Test
    void testUpdateLeaveBalance_ValidInput() {
        Employee employee = new Employee("John Doe", "B123", "WORKER", "Shipping", "A", new Date(), "ACTIVE");
        when(employeeRepository.findByBadgeId("B123")).thenReturn(Optional.of(employee));
        assertDoesNotThrow(() -> leaveService.updateLeaveBalance("B123", 5));
    }

    @Test
    void testUpdateLeaveBalance_NegativeBalance() {
        Employee employee = new Employee("John Doe", "B123", "WORKER", "Shipping", "A", new Date(), "ACTIVE");
        when(employeeRepository.findByBadgeId("B123")).thenReturn(Optional.of(employee));
        assertThrows(ValidationException.class, () -> leaveService.updateLeaveBalance("B123", -1));
    }

    @Test
    void testRequestLeave_BoundaryValues() {
        Employee employee = new Employee("A", "B126", "WORKER", "Shipping", "A", new Date(), "ACTIVE");
        when(employeeRepository.findByBadgeId("B126")).thenReturn(Optional.of(employee));
        assertDoesNotThrow(() -> leaveService.requestLeave("B126", "PTO", new Date(), new Date()));
    }

    @Test
    void testRequestLeave_EmptyType() {
        Employee employee = new Employee("John Doe", "B123", "WORKER", "Shipping", "A", new Date(), "ACTIVE");
        when(employeeRepository.findByBadgeId("B123")).thenReturn(Optional.of(employee));
        assertThrows(ValidationException.class, () -> leaveService.requestLeave("B123", "", new Date(), new Date()));
    }
}
