package SpringBootTestSuite;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.*;

class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeService employeeService;

    private Employee validEmployee;
    private Employee invalidEmployee;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        validEmployee = new Employee(1L, "John Doe", "BADGE123", "WORKER", "Logistics", "A", new Date(), "ACTIVE");
        invalidEmployee = new Employee(null, "", "", "", "", "", null, null);
    }

    @AfterEach
    void tearDown() {
        // Clean up resources if needed
    }

    @Test
    void testCreateEmployee_ValidInput() {
        when(employeeRepository.save(any(Employee.class))).thenReturn(validEmployee);
        Employee result = employeeService.createEmployee(validEmployee);
        assertNotNull(result);
        assertEquals("John Doe", result.getName());
        assertEquals("BADGE123", result.getBadgeId());
    }

    @Test
    void testCreateEmployee_InvalidInput_ThrowsException() {
        when(employeeRepository.save(any(Employee.class))).thenThrow(new DataIntegrityViolationException("Invalid data"));
        assertThrows(DataIntegrityViolationException.class, () -> employeeService.createEmployee(invalidEmployee));
    }

    @Test
    void testGetEmployeeById_ValidId() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        Employee result = employeeService.getEmployeeById(1L);
        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void testGetEmployeeById_InvalidId() {
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(EmployeeNotFoundException.class, () -> employeeService.getEmployeeById(999L));
    }

    @Test
    void testUpdateEmployee_ValidInput() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(validEmployee);
        Employee updated = new Employee(1L, "Jane Doe", "BADGE123", "HR", "HR", "B", new Date(), "ACTIVE");
        Employee result = employeeService.updateEmployee(1L, updated);
        assertNotNull(result);
        assertEquals("Jane Doe", result.getName());
        assertEquals("HR", result.getRole());
    }

    @Test
    void testUpdateEmployee_InvalidId() {
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());
        Employee updated = new Employee(999L, "Jane Doe", "BADGE999", "HR", "HR", "B", new Date(), "ACTIVE");
        assertThrows(EmployeeNotFoundException.class, () -> employeeService.updateEmployee(999L, updated));
    }

    @Test
    void testDeleteEmployee_ValidId() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        doNothing().when(employeeRepository).deleteById(1L);
        assertDoesNotThrow(() -> employeeService.deleteEmployee(1L));
    }

    @Test
    void testDeleteEmployee_InvalidId() {
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(EmployeeNotFoundException.class, () -> employeeService.deleteEmployee(999L));
    }

    @Test
    void testGetAllEmployees_Pagination() {
        List<Employee> employees = Arrays.asList(validEmployee);
        when(employeeRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(employees));
        Page<Employee> result = employeeService.getAllEmployees(PageRequest.of(0, 10));
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void testGetEmployeeByBadgeId_UniqueConstraint() {
        when(employeeRepository.findByBadgeId("BADGE123")).thenReturn(Optional.of(validEmployee));
        Employee result = employeeService.getEmployeeByBadgeId("BADGE123");
        assertNotNull(result);
        assertEquals("BADGE123", result.getBadgeId());
    }

    @Test
    void testGetEmployeeByBadgeId_NotFound() {
        when(employeeRepository.findByBadgeId("BADGE999")).thenReturn(Optional.empty());
        assertThrows(EmployeeNotFoundException.class, () -> employeeService.getEmployeeByBadgeId("BADGE999"));
    }

    @Test
    void testCreateEmployee_NullValues() {
        Employee nullEmployee = new Employee(null, null, null, null, null, null, null, null);
        when(employeeRepository.save(any(Employee.class))).thenThrow(new NullPointerException("Null values"));
        assertThrows(NullPointerException.class, () -> employeeService.createEmployee(nullEmployee));
    }

    @Test
    void testCreateEmployee_EmptyStrings() {
        Employee emptyEmployee = new Employee(null, "", "", "", "", "", null, "");
        when(employeeRepository.save(any(Employee.class))).thenThrow(new IllegalArgumentException("Empty fields"));
        assertThrows(IllegalArgumentException.class, () -> employeeService.createEmployee(emptyEmployee));
    }

    @Test
    void testCreateEmployee_MaxValues() {
        String longName = "A".repeat(255);
        Employee maxEmployee = new Employee(2L, longName, "BADGE999", "SUPERVISOR", "Operations", "C", new Date(), "ACTIVE");
        when(employeeRepository.save(any(Employee.class))).thenReturn(maxEmployee);
        Employee result = employeeService.createEmployee(maxEmployee);
        assertNotNull(result);
        assertEquals(longName, result.getName());
    }
}