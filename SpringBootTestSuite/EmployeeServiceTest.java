package SpringBootTestSuite;

import com.example.warehouse.model.Employee;
import com.example.warehouse.repository.EmployeeRepository;
import com.example.warehouse.service.EmployeeService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for EmployeeService.
 * Covers all input method signatures and edge cases for employee management.
 */
@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {
    @Mock
    private EmployeeRepository employeeRepository;
    @InjectMocks
    private EmployeeService employeeService;

    private Employee employee;

    @BeforeEach
    void setUp() {
        employee = new Employee();
        employee.setId(1L);
        employee.setName("John Doe");
        employee.setBadgeId("BADGE123");
        employee.setRole("WORKER");
        employee.setDepartment("Shipping");
        employee.setHireDate(LocalDateTime.now().minusYears(1));
        employee.setStatus("ACTIVE");
        employee.setCreatedAt(LocalDateTime.now().minusYears(1));
        employee.setUpdatedAt(LocalDateTime.now().minusDays(1));
        employee.setDeletedAt(null);
    }

    @Test
    void testCreateEmployee_ValidInput_Success() {
        when(employeeRepository.findByBadgeId(anyString())).thenReturn(Optional.empty());
        when(employeeRepository.save(any(Employee.class))).thenAnswer(i -> i.getArgument(0));
        Employee created = employeeService.createEmployee(employee);
        assertNotNull(created);
        assertEquals("ACTIVE", created.getStatus());
        assertNotNull(created.getCreatedAt());
        verify(employeeRepository).save(any(Employee.class));
    }

    @Test
    void testCreateEmployee_DuplicateBadgeId_ThrowsException() {
        when(employeeRepository.findByBadgeId(anyString())).thenReturn(Optional.of(employee));
        assertThrows(RuntimeException.class, () -> employeeService.createEmployee(employee));
    }

    @Test
    void testCreateEmployee_NullEmployee_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> employeeService.createEmployee(null));
    }

    @Test
    void testCreateEmployee_NullBadgeId_ThrowsException() {
        employee.setBadgeId(null);
        assertThrows(IllegalArgumentException.class, () -> employeeService.createEmployee(employee));
    }

    @Test
    void testCreateEmployee_EmptyBadgeId_ThrowsException() {
        employee.setBadgeId("");
        assertThrows(IllegalArgumentException.class, () -> employeeService.createEmployee(employee));
    }

    @Test
    void testGetAllEmployees_ReturnsActiveOnly() {
        Employee e2 = new Employee();
        e2.setId(2L);
        e2.setDeletedAt(null);
        List<Employee> employees = Arrays.asList(employee, e2);
        when(employeeRepository.findAllActive()).thenReturn(employees);
        List<Employee> result = employeeService.getAllEmployees();
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(e -> e.getDeletedAt() == null));
    }

    @Test
    void testGetEmployeeById_ValidId_ReturnsEmployee() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        Optional<Employee> result = employeeService.getEmployeeById(1L);
        assertTrue(result.isPresent());
        assertEquals("John Doe", result.get().getName());
    }

    @Test
    void testGetEmployeeById_InvalidId_ReturnsEmpty() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());
        Optional<Employee> result = employeeService.getEmployeeById(99L);
        assertFalse(result.isPresent());
    }

    @Test
    void testGetEmployeeById_NullId_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> employeeService.getEmployeeById(null));
    }

    @Test
    void testGetEmployeeByBadgeId_ValidBadgeId_ReturnsEmployee() {
        when(employeeRepository.findByBadgeId("BADGE123")).thenReturn(Optional.of(employee));
        Optional<Employee> result = employeeService.getEmployeeByBadgeId("BADGE123");
        assertTrue(result.isPresent());
        assertEquals("John Doe", result.get().getName());
    }

    @Test
    void testGetEmployeeByBadgeId_InvalidBadgeId_ReturnsEmpty() {
        when(employeeRepository.findByBadgeId("BADGE999")).thenReturn(Optional.empty());
        Optional<Employee> result = employeeService.getEmployeeByBadgeId("BADGE999");
        assertFalse(result.isPresent());
    }

    @Test
    void testGetEmployeeByBadgeId_NullBadgeId_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> employeeService.getEmployeeByBadgeId(null));
    }

    @Test
    void testUpdateEmployee_ValidInput_Success() {
        Employee updated = new Employee();
        updated.setName("Jane Smith");
        updated.setBadgeId("BADGE123");
        updated.setRole("SUPERVISOR");
        updated.setDepartment("Receiving");
        updated.setStatus("ACTIVE");
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(any(Employee.class))).thenAnswer(i -> i.getArgument(0));
        Employee result = employeeService.updateEmployee(1L, updated);
        assertEquals("Jane Smith", result.getName());
        assertEquals("SUPERVISOR", result.getRole());
        verify(employeeRepository).save(any(Employee.class));
    }

    @Test
    void testUpdateEmployee_InvalidId_ThrowsException() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());
        Employee updated = new Employee();
        assertThrows(RuntimeException.class, () -> employeeService.updateEmployee(99L, updated));
    }

    @Test
    void testUpdateEmployee_NullEmployee_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> employeeService.updateEmployee(1L, null));
    }

    @Test
    void testPatchEmployee_ValidInput_Success() {
        Employee partial = new Employee();
        partial.setName("Patched Name");
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(any(Employee.class))).thenAnswer(i -> i.getArgument(0));
        Employee result = employeeService.patchEmployee(1L, partial);
        assertEquals("Patched Name", result.getName());
        verify(employeeRepository).save(any(Employee.class));
    }

    @Test
    void testPatchEmployee_PartialUpdate_OnlyUpdatesNonNullFields() {
        Employee partial = new Employee();
        partial.setDepartment("NewDept");
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(any(Employee.class))).thenAnswer(i -> i.getArgument(0));
        Employee result = employeeService.patchEmployee(1L, partial);
        assertEquals("NewDept", result.getDepartment());
        assertEquals("John Doe", result.getName());
    }

    @Test
    void testPatchEmployee_InvalidId_ThrowsException() {
        Employee partial = new Employee();
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> employeeService.patchEmployee(99L, partial));
    }

    @Test
    void testSoftDeleteEmployee_ValidId_SetsDeletedAtAndStatus() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(any(Employee.class))).thenAnswer(i -> i.getArgument(0));
        Employee result = employeeService.softDeleteEmployee(1L);
        assertNotNull(result.getDeletedAt());
        assertEquals("TERMINATED", result.getStatus());
        verify(employeeRepository).save(any(Employee.class));
    }

    @Test
    void testSoftDeleteEmployee_InvalidId_ThrowsException() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> employeeService.softDeleteEmployee(99L));
    }

    @Test
    void testGetEmployeesByDepartment_ValidDepartment_ReturnsEmployees() {
        when(employeeRepository.findByDepartment("Shipping")).thenReturn(Collections.singletonList(employee));
        List<Employee> result = employeeService.getEmployeesByDepartment("Shipping");
        assertEquals(1, result.size());
        assertEquals("Shipping", result.get(0).getDepartment());
    }

    @Test
    void testGetEmployeesByDepartment_NullDepartment_ReturnsEmpty() {
        List<Employee> result = employeeService.getEmployeesByDepartment(null);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetEmployeesByRole_ValidRole_ReturnsEmployees() {
        when(employeeRepository.findByRole("WORKER")).thenReturn(Collections.singletonList(employee));
        List<Employee> result = employeeService.getEmployeesByRole("WORKER");
        assertEquals(1, result.size());
        assertEquals("WORKER", result.get(0).getRole());
    }

    @Test
    void testGetEmployeesByRole_NullRole_ReturnsEmpty() {
        List<Employee> result = employeeService.getEmployeesByRole(null);
        assertTrue(result.isEmpty());
    }
}
