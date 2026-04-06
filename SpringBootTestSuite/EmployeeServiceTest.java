import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private EmployeeService employeeService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCreateEmployee_ValidInput_ReturnsEmployee() {
        Employee employee = new Employee();
        employee.setBadgeId("EMP001");
        employee.setName("John Doe");
        employee.setRole("WORKER");
        employee.setDepartment("Logistics");
        employee.setShiftGroup("A");
        employee.setHireDate(LocalDate.now());
        employee.setStatus("ACTIVE");
        employee.setDeleted(false);
        employee.setCreatedAt(LocalDateTime.now());
        employee.setUpdatedAt(LocalDateTime.now());

        when(employeeRepository.findByBadgeId("EMP001")).thenReturn(Optional.empty());
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

        Employee result = employeeService.createEmployee(employee);

        assertNotNull(result);
        assertEquals("EMP001", result.getBadgeId());
        assertEquals("John Doe", result.getName());
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    public void testCreateEmployee_NullInput_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.createEmployee(null);
        });
    }

    @Test
    public void testCreateEmployee_EmptyBadgeId_ThrowsException() {
        Employee employee = new Employee();
        employee.setBadgeId("");
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.createEmployee(employee);
        });
    }

    @Test
    public void testCreateEmployee_DuplicateBadgeId_ThrowsException() {
        Employee employee = new Employee();
        employee.setBadgeId("EMP001");
        when(employeeRepository.findByBadgeId("EMP001")).thenReturn(Optional.of(new Employee()));
        assertThrows(DuplicateBadgeIdException.class, () -> {
            employeeService.createEmployee(employee);
        });
    }

    @Test
    public void testGetEmployeeById_ValidId_ReturnsEmployee() {
        Employee employee = new Employee();
        employee.setId(1L);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        Employee result = employeeService.getEmployeeById(1L);
        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    public void testGetEmployeeById_InvalidId_ThrowsResourceNotFoundException() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> {
            employeeService.getEmployeeById(99L);
        });
    }

    @Test
    public void testUpdateEmployee_ValidInput_UpdatesEmployee() {
        Employee existing = new Employee();
        existing.setId(1L);
        existing.setBadgeId("EMP001");
        Employee update = new Employee();
        update.setBadgeId("EMP001");
        update.setName("Jane Doe");
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(employeeRepository.save(any(Employee.class))).thenReturn(update);
        Employee result = employeeService.updateEmployee(1L, update);
        assertNotNull(result);
        assertEquals("Jane Doe", result.getName());
    }

    @Test
    public void testUpdateEmployee_NullInput_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.updateEmployee(1L, null);
        });
    }

    @Test
    public void testUpdateEmployee_InvalidId_ThrowsResourceNotFoundException() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());
        Employee update = new Employee();
        update.setBadgeId("EMP002");
        assertThrows(ResourceNotFoundException.class, () -> {
            employeeService.updateEmployee(99L, update);
        });
    }

    @Test
    public void testDeleteEmployee_ValidId_SoftDeletesEmployee() {
        Employee employee = new Employee();
        employee.setId(1L);
        employee.setDeleted(false);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);
        employeeService.deleteEmployee(1L);
        assertTrue(employee.isDeleted());
        verify(employeeRepository, times(1)).save(employee);
    }

    @Test
    public void testDeleteEmployee_InvalidId_ThrowsResourceNotFoundException() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> {
            employeeService.deleteEmployee(99L);
        });
    }

    @Test
    public void testGetAllEmployees_Pagination_ReturnsPage() {
        List<Employee> employees = Arrays.asList(new Employee(), new Employee());
        Page<Employee> page = new PageImpl<>(employees);
        Pageable pageable = PageRequest.of(0, 2);
        when(employeeRepository.findAll(pageable)).thenReturn(page);
        Page<Employee> result = employeeService.getAllEmployees(pageable);
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
    }

    @Test
    public void testGetEmployeesByDepartment_ValidDepartment_ReturnsList() {
        List<Employee> employees = Arrays.asList(new Employee(), new Employee());
        when(employeeRepository.findByDepartment("Logistics")).thenReturn(employees);
        List<Employee> result = employeeService.getEmployeesByDepartment("Logistics");
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    public void testGetEmployeesByDepartment_EmptyDepartment_ReturnsEmptyList() {
        when(employeeRepository.findByDepartment("")).thenReturn(Collections.emptyList());
        List<Employee> result = employeeService.getEmployeesByDepartment("");
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetEmployeesByStatus_ValidStatus_ReturnsList() {
        List<Employee> employees = Arrays.asList(new Employee());
        when(employeeRepository.findByStatus("ACTIVE")).thenReturn(employees);
        List<Employee> result = employeeService.getEmployeesByStatus("ACTIVE");
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    public void testGetEmployeeByBadgeId_ValidBadgeId_ReturnsEmployee() {
        Employee employee = new Employee();
        employee.setBadgeId("EMP001");
        when(employeeRepository.findByBadgeId("EMP001")).thenReturn(Optional.of(employee));
        Employee result = employeeService.getEmployeeByBadgeId("EMP001");
        assertNotNull(result);
        assertEquals("EMP001", result.getBadgeId());
    }

    @Test
    public void testGetEmployeeByBadgeId_InvalidBadgeId_ThrowsResourceNotFoundException() {
        when(employeeRepository.findByBadgeId("INVALID")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> {
            employeeService.getEmployeeByBadgeId("INVALID");
        });
    }

    @Test
    public void testGetEmployeeByBadgeId_DuplicateBadgeId_ThrowsException() {
        Employee employee1 = new Employee();
        Employee employee2 = new Employee();
        when(employeeRepository.findByBadgeId("EMP001")).thenReturn(Optional.of(employee1));
        // Simulate duplicate badgeId scenario
        when(employeeRepository.findAllByBadgeId("EMP001")).thenReturn(Arrays.asList(employee1, employee2));
        assertThrows(DuplicateBadgeIdException.class, () -> {
            employeeService.getEmployeeByBadgeId("EMP001");
        });
    }

    @Test
    public void testCreateEmployee_MaxLengthBadgeId_Valid() {
        Employee employee = new Employee();
        String badgeId = "EMP" + "0".repeat(50); // assuming max length is 50
        employee.setBadgeId(badgeId);
        when(employeeRepository.findByBadgeId(badgeId)).thenReturn(Optional.empty());
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);
        Employee result = employeeService.createEmployee(employee);
        assertNotNull(result);
        assertEquals(badgeId, result.getBadgeId());
    }

    @Test
    public void testCreateEmployee_NegativeId_ThrowsException() {
        Employee employee = new Employee();
        employee.setId(-1L);
        employee.setBadgeId("EMP002");
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.createEmployee(employee);
        });
    }

    @Test
    public void testCreateEmployee_NullBadgeId_ThrowsException() {
        Employee employee = new Employee();
        employee.setBadgeId(null);
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.createEmployee(employee);
        });
    }

    @Test
    public void testCreateEmployee_InvalidStatus_ThrowsException() {
        Employee employee = new Employee();
        employee.setBadgeId("EMP003");
        employee.setStatus("INVALID_STATUS");
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.createEmployee(employee);
        });
    }

    // Add more tests as needed for edge cases and business logic
}