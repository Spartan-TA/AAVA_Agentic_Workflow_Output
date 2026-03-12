package SpringBootTestSuite;

import com.example.warehouse.employee.Employee;
import com.example.warehouse.employee.EmployeeService;
import com.example.warehouse.employee.EmployeeRepository;
import com.example.warehouse.exception.ResourceNotFoundException;
import com.example.warehouse.exception.ValidationException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
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
    public void createEmployee_ValidInput_ReturnsEmployee() {
        Employee employee = new Employee();
        employee.setEmployeeCode("EMP001");
        when(employeeRepository.save(any())).thenReturn(employee);
        Employee result = employeeService.createEmployee(employee);
        assertNotNull(result);
        assertEquals("EMP001", result.getEmployeeCode());
    }

    @Test
    public void createEmployee_NullInput_ThrowsValidationException() {
        assertThrows(ValidationException.class, () -> employeeService.createEmployee(null));
    }

    @Test
    public void getEmployeeById_ValidId_ReturnsEmployee() {
        Employee employee = new Employee();
        employee.setId(1L);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        Employee result = employeeService.getEmployeeById(1L);
        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    public void getEmployeeById_InvalidId_ThrowsResourceNotFoundException() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> employeeService.getEmployeeById(99L));
    }

    @Test
    public void updateEmployee_ValidInput_ReturnsUpdatedEmployee() {
        Employee existing = new Employee();
        existing.setId(1L);
        existing.setEmployeeCode("EMP001");
        Employee updated = new Employee();
        updated.setId(1L);
        updated.setEmployeeCode("EMP002");
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(employeeRepository.save(any())).thenReturn(updated);
        Employee result = employeeService.updateEmployee(1L, updated);
        assertNotNull(result);
        assertEquals("EMP002", result.getEmployeeCode());
    }

    @Test
    public void updateEmployee_NullInput_ThrowsValidationException() {
        assertThrows(ValidationException.class, () -> employeeService.updateEmployee(1L, null));
    }

    @Test
    public void deleteEmployee_ValidId_SoftDeletesEmployee() {
        Employee employee = new Employee();
        employee.setId(1L);
        employee.setStatus("ACTIVE");
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        doNothing().when(employeeRepository).delete(any());
        employeeService.deleteEmployee(1L);
        verify(employeeRepository, times(1)).delete(any());
    }

    @Test
    public void deleteEmployee_InvalidId_ThrowsResourceNotFoundException() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> employeeService.deleteEmployee(99L));
    }

    @Test
    public void getAllEmployees_ReturnsList() {
        Employee employee = new Employee();
        employee.setId(1L);
        when(employeeRepository.findAll()).thenReturn(Collections.singletonList(employee));
        List<Employee> result = employeeService.getAllEmployees();
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    public void getAllEmployees_Empty_ReturnsEmptyList() {
        when(employeeRepository.findAll()).thenReturn(Collections.emptyList());
        List<Employee> result = employeeService.getAllEmployees();
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void createEmployee_DuplicateBadgeId_ThrowsValidationException() {
        Employee employee = new Employee();
        employee.setBadgeId("BADGE123");
        when(employeeRepository.existsByBadgeId("BADGE123")).thenReturn(true);
        assertThrows(ValidationException.class, () -> employeeService.createEmployee(employee));
    }

    @Test
    public void createEmployee_EmptyFields_ThrowsValidationException() {
        Employee employee = new Employee();
        employee.setName("");
        employee.setBadgeId("");
        assertThrows(ValidationException.class, () -> employeeService.createEmployee(employee));
    }
}
