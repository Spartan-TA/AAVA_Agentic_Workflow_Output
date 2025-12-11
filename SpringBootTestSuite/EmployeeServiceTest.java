import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.wms.employee.Employee;
import com.wms.employee.EmployeeRepository;
import com.wms.employee.EmployeeService;

import org.junit.jupiter.api.*;
import org.mockito.*;
import java.time.LocalDate;
import java.util.*;

public class EmployeeServiceTest {
    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeService employeeService;

    private Employee validEmployee;
    private Employee inactiveEmployee;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        validEmployee = new Employee("12345", "John", "Doe", "john.doe@example.com", "555-1234", "Logistics", "WORKER", LocalDate.of(2020,1,1), null, true);
        inactiveEmployee = new Employee("54321", "Jane", "Smith", "jane.smith@example.com", "555-4321", "HR", "ADMIN", LocalDate.of(2019,5,10), LocalDate.of(2023,1,1), false);
    }

    @AfterEach
    public void tearDown() {
        // Clean up resources if needed
    }

    @Test
    public void testCreateEmployee_WithValidInput_ReturnsEmployee() {
        when(employeeRepository.save(any(Employee.class))).thenReturn(validEmployee);
        Employee result = employeeService.createEmployee(validEmployee);
        assertNotNull(result);
        assertEquals("12345", result.getBadgeId());
    }

    @Test
    public void testCreateEmployee_WithDuplicateBadgeId_ThrowsException() {
        when(employeeRepository.findByBadgeId("12345")).thenReturn(Optional.of(validEmployee));
        assertThrows(IllegalArgumentException.class, () -> employeeService.createEmployee(validEmployee));
    }

    @Test
    public void testCreateEmployee_WithNullInput_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> employeeService.createEmployee(null));
    }

    @Test
    public void testGetEmployeeById_WithValidId_ReturnsEmployee() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        Employee result = employeeService.getEmployeeById(1L);
        assertNotNull(result);
        assertEquals("John", result.getFirstName());
    }

    @Test
    public void testGetEmployeeById_WithInvalidId_ReturnsNull() {
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());
        Employee result = employeeService.getEmployeeById(999L);
        assertNull(result);
    }

    @Test
    public void testGetEmployeeByBadgeId_WithValidBadgeId_ReturnsEmployee() {
        when(employeeRepository.findByBadgeId("12345")).thenReturn(Optional.of(validEmployee));
        Employee result = employeeService.getEmployeeByBadgeId("12345");
        assertNotNull(result);
        assertEquals("John", result.getFirstName());
    }

    @Test
    public void testGetEmployeeByBadgeId_WithNullBadgeId_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> employeeService.getEmployeeByBadgeId(null));
    }

    @Test
    public void testGetAllEmployees_ReturnsList() {
        List<Employee> employees = Arrays.asList(validEmployee, inactiveEmployee);
        when(employeeRepository.findAll()).thenReturn(employees);
        List<Employee> result = employeeService.getAllEmployees();
        assertEquals(2, result.size());
    }

    @Test
    public void testGetEmployeesByDepartment_WithValidDepartment_ReturnsEmployees() {
        List<Employee> employees = Collections.singletonList(validEmployee);
        when(employeeRepository.findByDepartment("Logistics")).thenReturn(employees);
        List<Employee> result = employeeService.getEmployeesByDepartment("Logistics");
        assertEquals(1, result.size());
        assertEquals("Logistics", result.get(0).getDepartment());
    }

    @Test
    public void testGetEmployeesByDepartment_WithEmptyDepartment_ReturnsEmptyList() {
        when(employeeRepository.findByDepartment("")).thenReturn(Collections.emptyList());
        List<Employee> result = employeeService.getEmployeesByDepartment("");
        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetEmployeesByRole_WithValidRole_ReturnsEmployees() {
        List<Employee> employees = Collections.singletonList(validEmployee);
        when(employeeRepository.findByRole("WORKER")).thenReturn(employees);
        List<Employee> result = employeeService.getEmployeesByRole("WORKER");
        assertEquals(1, result.size());
        assertEquals("WORKER", result.get(0).getRole());
    }

    @Test
    public void testGetEmployeesByRole_WithInvalidRole_ReturnsEmptyList() {
        when(employeeRepository.findByRole("INVALID_ROLE")).thenReturn(Collections.emptyList());
        List<Employee> result = employeeService.getEmployeesByRole("INVALID_ROLE");
        assertTrue(result.isEmpty());
    }

    @Test
    public void testUpdateEmployee_WithValidInput_ReturnsUpdatedEmployee() {
        Employee updated = new Employee("12345", "John", "Doe", "john.doe@newmail.com", "555-1234", "Logistics", "WORKER", LocalDate.of(2020,1,1), null, true);
        when(employeeRepository.findByBadgeId("12345")).thenReturn(Optional.of(validEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(updated);
        Employee result = employeeService.updateEmployee("12345", updated);
        assertNotNull(result);
        assertEquals("john.doe@newmail.com", result.getEmail());
    }

    @Test
    public void testUpdateEmployee_WithNonexistentBadgeId_ThrowsException() {
        Employee updated = new Employee("99999", "Ghost", "User", "ghost@example.com", "555-0000", "IT", "WORKER", LocalDate.of(2021,1,1), null, true);
        when(employeeRepository.findByBadgeId("99999")).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> employeeService.updateEmployee("99999", updated));
    }

    @Test
    public void testSoftDeleteEmployee_WithValidBadgeId_SetsInactive() {
        when(employeeRepository.findByBadgeId("12345")).thenReturn(Optional.of(validEmployee));
        Employee inactive = new Employee("12345", "John", "Doe", "john.doe@example.com", "555-1234", "Logistics", "WORKER", LocalDate.of(2020,1,1), LocalDate.now(), false);
        when(employeeRepository.save(any(Employee.class))).thenReturn(inactive);
        Employee result = employeeService.softDeleteEmployee("12345");
        assertFalse(result.isActive());
        assertEquals(LocalDate.now(), result.getTerminationDate());
    }

    @Test
    public void testSoftDeleteEmployee_WithNonexistentBadgeId_ThrowsException() {
        when(employeeRepository.findByBadgeId("99999")).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> employeeService.softDeleteEmployee("99999"));
    }

    @Test
    public void testGetAllEmployees_ExcludesInactiveEmployees() {
        List<Employee> employees = Arrays.asList(validEmployee, inactiveEmployee);
        when(employeeRepository.findAll()).thenReturn(employees);
        List<Employee> result = employeeService.getAllEmployees();
        assertTrue(result.stream().anyMatch(Employee::isActive));
        assertTrue(result.stream().anyMatch(e -> !e.isActive()));
    }

    @Test
    public void testCreateEmployee_WithEmptyFields_ThrowsException() {
        Employee emptyFields = new Employee("", "", "", "", "", "", "", null, null, true);
        assertThrows(IllegalArgumentException.class, () -> employeeService.createEmployee(emptyFields));
    }

    @Test
    public void testCreateEmployee_WithInvalidEmailFormat_ThrowsException() {
        Employee invalidEmail = new Employee("12346", "Alice", "Wonder", "not-an-email", "555-9999", "IT", "WORKER", LocalDate.of(2022,2,2), null, true);
        assertThrows(IllegalArgumentException.class, () -> employeeService.createEmployee(invalidEmail));
    }
}