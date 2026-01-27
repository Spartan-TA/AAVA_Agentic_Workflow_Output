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
    void testCreateEmployee_ValidInput_Success() {
        Employee employee = new Employee("John Doe", "B123", "WORKER", "Shipping", "A", new Date(), "ACTIVE");
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);
        Employee result = employeeService.createEmployee(employee);
        assertNotNull(result);
        assertEquals("John Doe", result.getName());
        assertEquals("B123", result.getBadgeId());
    }

    @Test
    void testCreateEmployee_DuplicateBadgeId_ThrowsException() {
        Employee employee = new Employee("Jane Doe", "B123", "HR", "HR", "B", new Date(), "ACTIVE");
        when(employeeRepository.existsByBadgeId("B123")).thenReturn(true);
        assertThrows(DuplicateBadgeIdException.class, () -> employeeService.createEmployee(employee));
    }

    @Test
    void testGetEmployeeById_ValidId_ReturnsEmployee() {
        Employee employee = new Employee("John Doe", "B123", "WORKER", "Shipping", "A", new Date(), "ACTIVE");
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        Employee result = employeeService.getEmployeeById(1L);
        assertNotNull(result);
        assertEquals("B123", result.getBadgeId());
    }

    @Test
    void testGetEmployeeById_InvalidId_ThrowsException() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(EmployeeNotFoundException.class, () -> employeeService.getEmployeeById(99L));
    }

    @Test
    void testUpdateEmployee_NullName_ThrowsException() {
        Employee employee = new Employee(null, "B124", "WORKER", "Shipping", "A", new Date(), "ACTIVE");
        assertThrows(InvalidEmployeeException.class, () -> employeeService.updateEmployee(1L, employee));
    }

    @Test
    void testDeleteEmployee_SoftDelete_Success() {
        Employee employee = new Employee("John Doe", "B123", "WORKER", "Shipping", "A", new Date(), "ACTIVE");
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        employeeService.deleteEmployee(1L);
        verify(employeeRepository).save(argThat(e -> "DELETED".equals(e.getStatus())));
    }

    @Test
    void testListEmployees_PaginationAndFiltering() {
        List<Employee> employees = Arrays.asList(
            new Employee("John Doe", "B123", "WORKER", "Shipping", "A", new Date(), "ACTIVE"),
            new Employee("Jane Doe", "B124", "HR", "HR", "B", new Date(), "ACTIVE")
        );
        when(employeeRepository.findAll(any())).thenReturn(employees);
        List<Employee> result = employeeService.listEmployees("ACTIVE", 0, 10);
        assertEquals(2, result.size());
    }

    @Test
    void testCreateEmployee_EmptyBadgeId_ThrowsException() {
        Employee employee = new Employee("John Doe", "", "WORKER", "Shipping", "A", new Date(), "ACTIVE");
        assertThrows(InvalidEmployeeException.class, () -> employeeService.createEmployee(employee));
    }

    @Test
    void testCreateEmployee_NullEmployee_ThrowsException() {
        assertThrows(InvalidEmployeeException.class, () -> employeeService.createEmployee(null));
    }

    // Integration scenario: Assign employee to shift
    @Test
    void testAssignEmployeeToShift_Valid_Success() {
        Employee employee = new Employee("John Doe", "B123", "WORKER", "Shipping", "A", new Date(), "ACTIVE");
        Shift shift = new Shift(1L, "Morning", "08:00", "16:00");
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(shiftRepository.findById(1L)).thenReturn(Optional.of(shift));
        employeeService.assignToShift(1L, 1L);
        verify(employeeRepository).save(employee);
    }

    // Edge case: Assign employee to non-existent shift
    @Test
    void testAssignEmployeeToShift_InvalidShift_ThrowsException() {
        Employee employee = new Employee("John Doe", "B123", "WORKER", "Shipping", "A", new Date(), "ACTIVE");
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(shiftRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ShiftNotFoundException.class, () -> employeeService.assignToShift(1L, 99L));
    }
}
