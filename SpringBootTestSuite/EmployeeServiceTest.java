import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.access.AccessDeniedException;
import java.util.*;

public class EmployeeServiceTest {
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private AuditLogService auditLogService;
    @InjectMocks
    private EmployeeService employeeService;
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
    void testCreateEmployeeWithValidData() {
        Employee employee = new Employee("12345", "John Doe", "WORKER", "Shipping", "A", new Date(), "ACTIVE");
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);
        Employee result = employeeService.createEmployee(employee);
        assertEquals("12345", result.getBadgeId());
        assertEquals("John Doe", result.getName());
        verify(auditLogService).logCreate(any(), eq(employee));
    }

    @Test
    void testCreateEmployeeWithDuplicateBadgeId() {
        Employee employee = new Employee("12345", "Jane Doe", "WORKER", "Receiving", "B", new Date(), "ACTIVE");
        when(employeeRepository.existsByBadgeId("12345")).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> employeeService.createEmployee(employee));
    }

    @Test
    void testCreateEmployeeWithNullFields() {
        Employee employee = new Employee(null, null, null, null, null, null, null);
        assertThrows(IllegalArgumentException.class, () -> employeeService.createEmployee(employee));
    }

    @Test
    void testGetEmployeeByIdExists() {
        Employee employee = new Employee("12345", "John Doe", "WORKER", "Shipping", "A", new Date(), "ACTIVE");
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        Employee result = employeeService.getEmployeeById(1L);
        assertEquals("John Doe", result.getName());
    }

    @Test
    void testGetEmployeeByIdNotFound() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> employeeService.getEmployeeById(99L));
    }

    @Test
    void testUpdateEmployeeValid() {
        Employee existing = new Employee("12345", "John Doe", "WORKER", "Shipping", "A", new Date(), "ACTIVE");
        Employee update = new Employee("12345", "John Smith", "WORKER", "Shipping", "A", new Date(), "ACTIVE");
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(employeeRepository.save(any(Employee.class))).thenReturn(update);
        Employee result = employeeService.updateEmployee(1L, update);
        assertEquals("John Smith", result.getName());
        verify(auditLogService).logUpdate(any(), eq(existing), eq(update));
    }

    @Test
    void testUpdateEmployeeNotFound() {
        Employee update = new Employee("12345", "John Smith", "WORKER", "Shipping", "A", new Date(), "ACTIVE");
        when(employeeRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> employeeService.updateEmployee(2L, update));
    }

    @Test
    void testDeleteEmployeeSoftDelete() {
        Employee employee = new Employee("12345", "John Doe", "WORKER", "Shipping", "A", new Date(), "ACTIVE");
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        employeeService.deleteEmployee(1L);
        assertEquals("DELETED", employee.getStatus());
        verify(employeeRepository).save(employee);
        verify(auditLogService).logDelete(any(), eq(employee));
    }

    @Test
    void testDeleteEmployeeNotFound() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> employeeService.deleteEmployee(99L));
    }

    @Test
    void testListEmployeesWithPaginationAndFiltering() {
        List<Employee> employees = Arrays.asList(
            new Employee("12345", "John Doe", "WORKER", "Shipping", "A", new Date(), "ACTIVE"),
            new Employee("23456", "Jane Smith", "SUPERVISOR", "Receiving", "B", new Date(), "ACTIVE")
        );
        when(employeeRepository.findAll(any(), any())).thenReturn(employees);
        List<Employee> result = employeeService.listEmployees("ACTIVE", "Shipping", 0, 10);
        assertEquals(2, result.size());
        assertEquals("John Doe", result.get(0).getName());
    }

    @Test
    void testListEmployeesEmptyResult() {
        when(employeeRepository.findAll(any(), any())).thenReturn(Collections.emptyList());
        List<Employee> result = employeeService.listEmployees("INACTIVE", "Unknown", 0, 10);
        assertTrue(result.isEmpty());
    }

    @Test
    void testAccessControlAdminCanCreate() {
        Employee employee = new Employee("12345", "John Doe", "WORKER", "Shipping", "A", new Date(), "ACTIVE");
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);
        // Simulate ADMIN role
        employeeService.setCurrentUserRole("ADMIN");
        Employee result = employeeService.createEmployee(employee);
        assertEquals("12345", result.getBadgeId());
    }

    @Test
    void testAccessControlWorkerCannotDelete() {
        Employee employee = new Employee("12345", "John Doe", "WORKER", "Shipping", "A", new Date(), "ACTIVE");
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        employeeService.setCurrentUserRole("WORKER");
        assertThrows(AccessDeniedException.class, () -> employeeService.deleteEmployee(1L));
    }

    @Test
    void testCreateEmployeeWithEmptyName() {
        Employee employee = new Employee("12346", "", "WORKER", "Shipping", "A", new Date(), "ACTIVE");
        assertThrows(IllegalArgumentException.class, () -> employeeService.createEmployee(employee));
    }

    @Test
    void testCreateEmployeeWithInvalidRole() {
        Employee employee = new Employee("12347", "John Doe", "INVALID_ROLE", "Shipping", "A", new Date(), "ACTIVE");
        assertThrows(IllegalArgumentException.class, () -> employeeService.createEmployee(employee));
    }

    @Test
    void testCreateEmployeeWithFutureHireDate() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, 10);
        Employee employee = new Employee("12348", "John Doe", "WORKER", "Shipping", "A", cal.getTime(), "ACTIVE");
        assertThrows(IllegalArgumentException.class, () -> employeeService.createEmployee(employee));
    }

    // Add more edge case tests as needed for boundary conditions, filtering, etc.
}
