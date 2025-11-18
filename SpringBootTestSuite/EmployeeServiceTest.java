import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateEmployeeValid() {
        Employee employee = new Employee();
        employee.setName("John Doe");
        employee.setBadgeId("12345");
        employee.setRole("Worker");
        employee.setDepartment("Warehouse");
        employee.setHireDate(LocalDate.now());
        employee.setStatus("Active");

        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

        Employee createdEmployee = employeeService.createEmployee(employee);

        assertNotNull(createdEmployee);
        assertEquals("John Doe", createdEmployee.getName());
        assertEquals("12345", createdEmployee.getBadgeId());
    }

    @Test
    void testCreateEmployeeDuplicateBadgeId() {
        Employee employee = new Employee();
        employee.setBadgeId("12345");

        when(employeeRepository.findByBadgeId("12345")).thenReturn(Optional.of(employee));

        assertThrows(IllegalArgumentException.class, () -> employeeService.createEmployee(employee));
    }

    @Test
    void testUpdateEmployeeValid() {
        Employee existingEmployee = new Employee();
        existingEmployee.setId(1L);
        existingEmployee.setName("Jane Doe");

        Employee updatedEmployee = new Employee();
        updatedEmployee.setName("Jane Smith");

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(existingEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(updatedEmployee);

        Employee result = employeeService.updateEmployee(1L, updatedEmployee);

        assertNotNull(result);
        assertEquals("Jane Smith", result.getName());
    }

    @Test
    void testUpdateEmployeeNotFound() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.empty());

        Employee updatedEmployee = new Employee();
        updatedEmployee.setName("Jane Smith");

        assertThrows(ResourceNotFoundException.class, () -> employeeService.updateEmployee(1L, updatedEmployee));
    }

    @Test
    void testDeleteEmployeeValid() {
        Employee existingEmployee = new Employee();
        existingEmployee.setId(1L);

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(existingEmployee));

        assertDoesNotThrow(() -> employeeService.deleteEmployee(1L));
        verify(employeeRepository, times(1)).delete(existingEmployee);
    }

    @Test
    void testDeleteEmployeeNotFound() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> employeeService.deleteEmployee(1L));
    }

    @Test
    void testGetEmployeeByIdFound() {
        Employee employee = new Employee();
        employee.setId(1L);
        employee.setName("John Doe");

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        Optional<Employee> result = employeeService.getEmployeeById(1L);

        assertTrue(result.isPresent());
        assertEquals("John Doe", result.get().getName());
    }

    @Test
    void testGetEmployeeByIdNotFound() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<Employee> result = employeeService.getEmployeeById(1L);

        assertFalse(result.isPresent());
    }
}