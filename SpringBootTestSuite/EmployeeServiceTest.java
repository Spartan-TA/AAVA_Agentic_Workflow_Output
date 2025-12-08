import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.*;

public class EmployeeServiceTest {
    @Mock
    private EmployeeRepository employeeRepository;

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
    void testCreateEmployee_ValidInput() {
        Employee employee = new Employee("John Doe", "B123", "WORKER", "Shipping", "A", new Date(), "ACTIVE");
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);
        Employee result = employeeService.createEmployee(employee);
        assertEquals("John Doe", result.getName());
        assertEquals("B123", result.getBadgeId());
    }

    @Test
    void testCreateEmployee_NullInput() {
        assertThrows(IllegalArgumentException.class, () -> employeeService.createEmployee(null));
    }

    @Test
    void testCreateEmployee_EmptyName() {
        Employee employee = new Employee("", "B124", "WORKER", "Shipping", "A", new Date(), "ACTIVE");
        assertThrows(ValidationException.class, () -> employeeService.createEmployee(employee));
    }

    @Test
    void testCreateEmployee_DuplicateBadgeId() {
        Employee employee = new Employee("Jane Doe", "B125", "HR", "HR", "B", new Date(), "ACTIVE");
        when(employeeRepository.existsByBadgeId("B125")).thenReturn(true);
        assertThrows(ConstraintViolationException.class, () -> employeeService.createEmployee(employee));
    }

    @Test
    void testGetEmployeeById_ValidId() {
        Employee employee = new Employee("John Doe", "B123", "WORKER", "Shipping", "A", new Date(), "ACTIVE");
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        Employee result = employeeService.getEmployeeById(1L);
        assertEquals("John Doe", result.getName());
    }

    @Test
    void testGetEmployeeById_InvalidId() {
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> employeeService.getEmployeeById(999L));
    }

    @Test
    void testUpdateEmployee_ValidInput() {
        Employee existing = new Employee("John Doe", "B123", "WORKER", "Shipping", "A", new Date(), "ACTIVE");
        Employee updated = new Employee("John Smith", "B123", "SUPERVISOR", "Shipping", "A", new Date(), "ACTIVE");
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(employeeRepository.save(any(Employee.class))).thenReturn(updated);
        Employee result = employeeService.updateEmployee(1L, updated);
        assertEquals("John Smith", result.getName());
        assertEquals("SUPERVISOR", result.getRole());
    }

    @Test
    void testUpdateEmployee_NullInput() {
        assertThrows(IllegalArgumentException.class, () -> employeeService.updateEmployee(1L, null));
    }

    @Test
    void testUpdateEmployee_InvalidId() {
        Employee updated = new Employee("John Smith", "B123", "SUPERVISOR", "Shipping", "A", new Date(), "ACTIVE");
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> employeeService.updateEmployee(999L, updated));
    }

    @Test
    void testDeleteEmployee_ValidId() {
        Employee employee = new Employee("John Doe", "B123", "WORKER", "Shipping", "A", new Date(), "ACTIVE");
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        doNothing().when(employeeRepository).softDelete(1L);
        assertDoesNotThrow(() -> employeeService.deleteEmployee(1L));
    }

    @Test
    void testDeleteEmployee_InvalidId() {
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> employeeService.deleteEmployee(999L));
    }

    @Test
    void testListEmployees_PaginationAndFiltering() {
        List<Employee> employees = Arrays.asList(
            new Employee("John Doe", "B123", "WORKER", "Shipping", "A", new Date(), "ACTIVE"),
            new Employee("Jane Doe", "B124", "HR", "HR", "B", new Date(), "ACTIVE")
        );
        when(employeeRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(employees));
        Page<Employee> result = employeeService.listEmployees(PageRequest.of(0, 10));
        assertEquals(2, result.getTotalElements());
    }

    @Test
    void testCreateEmployee_MinMaxBoundaryValues() {
        Employee minEmployee = new Employee("A", "B126", "WORKER", "Shipping", "A", new Date(), "ACTIVE");
        Employee maxEmployee = new Employee("A very long name exceeding normal limits", "B127", "WORKER", "Shipping", "A", new Date(), "ACTIVE");
        when(employeeRepository.save(any(Employee.class))).thenReturn(minEmployee).thenReturn(maxEmployee);
        assertDoesNotThrow(() -> employeeService.createEmployee(minEmployee));
        assertDoesNotThrow(() -> employeeService.createEmployee(maxEmployee));
    }
}
