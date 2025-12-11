package SpringBootTestSuite;

import com.warehouse.employee.entity.Employee;
import com.warehouse.employee.repository.EmployeeRepository;
import com.warehouse.employee.service.EmployeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for EmployeeService covering normal cases, boundary conditions, edge cases, and exception handling.
 */
@ExtendWith(MockitoExtension.class)
public class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeService employeeService;

    private Employee validEmployee;

    @BeforeEach
    public void setUp() {
        validEmployee = new Employee();
        validEmployee.setId(1L);
        validEmployee.setName("John Doe");
        validEmployee.setBadgeId("EMP001");
        validEmployee.setEmail("john.doe@warehouse.com");
        validEmployee.setRole("WORKER");
        validEmployee.setDepartment("Logistics");
        validEmployee.setShiftGroup("A");
        validEmployee.setHireDate(LocalDate.of(2023, 1, 1));
        validEmployee.setStatus("ACTIVE");
        validEmployee.setIsActive(true);
    }

    @AfterEach
    public void tearDown() {
        // Clean up if needed
    }

    // Normal case: create employee with valid data
    @Test
    public void testCreateEmployee_WithValidData_ShouldReturnSavedEmployee() {
        when(employeeRepository.findByBadgeId("EMP001")).thenReturn(Optional.empty());
        when(employeeRepository.save(any(Employee.class))).thenReturn(validEmployee);

        Employee result = employeeService.createEmployee(validEmployee);

        assertNotNull(result);
        assertEquals("John Doe", result.getName());
        assertEquals("EMP001", result.getBadgeId());
        verify(employeeRepository, times(1)).save(validEmployee);
    }

    // Edge case: create employee with duplicate badgeId
    @Test
    public void testCreateEmployee_WithDuplicateBadgeId_ShouldThrowException() {
        when(employeeRepository.findByBadgeId("EMP001")).thenReturn(Optional.of(validEmployee));

        Employee newEmployee = new Employee();
        newEmployee.setBadgeId("EMP001");

        assertThrows(IllegalArgumentException.class, () -> employeeService.createEmployee(newEmployee));
    }

    // Boundary case: create employee with empty name
    @Test
    public void testCreateEmployee_WithEmptyName_ShouldThrowException() {
        Employee employee = new Employee();
        employee.setName("");
        employee.setBadgeId("EMP002");
        employee.setEmail("empty.name@warehouse.com");

        when(employeeRepository.findByBadgeId("EMP002")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> employeeService.createEmployee(employee));
    }

    // Edge case: create employee with invalid email
    @Test
    public void testCreateEmployee_WithInvalidEmail_ShouldThrowException() {
        Employee employee = new Employee();
        employee.setName("Jane Doe");
        employee.setBadgeId("EMP003");
        employee.setEmail("invalid-email");

        when(employeeRepository.findByBadgeId("EMP003")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> employeeService.createEmployee(employee));
    }

    // Normal case: get employee by ID
    @Test
    public void testGetEmployeeById_WithValidId_ShouldReturnEmployee() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));

        Employee result = employeeService.getEmployeeById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("John Doe", result.getName());
    }

    // Edge case: get employee by non-existent ID
    @Test
    public void testGetEmployeeById_WithNonExistentId_ShouldThrowException() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> employeeService.getEmployeeById(99L));
    }

    // Normal case: get employee by badgeId
    @Test
    public void testGetEmployeeByBadgeId_WithValidBadgeId_ShouldReturnEmployee() {
        when(employeeRepository.findByBadgeId("EMP001")).thenReturn(Optional.of(validEmployee));

        Employee result = employeeService.getEmployeeByBadgeId("EMP001");

        assertNotNull(result);
        assertEquals("EMP001", result.getBadgeId());
    }

    // Edge case: get employee by non-existent badgeId
    @Test
    public void testGetEmployeeByBadgeId_WithNonExistentBadgeId_ShouldThrowException() {
        when(employeeRepository.findByBadgeId("BADGE404")).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> employeeService.getEmployeeByBadgeId("BADGE404"));
    }

    // Normal case: get all employees
    @Test
    public void testGetAllEmployees_ShouldReturnActiveEmployees() {
        List<Employee> employees = Arrays.asList(validEmployee);
        when(employeeRepository.findByIsActiveTrue()).thenReturn(employees);

        List<Employee> result = employeeService.getAllEmployees();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).getIsActive());
    }

    // Boundary case: get employees with pagination
    @Test
    public void testGetEmployees_WithPagination_ShouldReturnPagedEmployees() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> page = new PageImpl<>(Collections.singletonList(validEmployee));
        when(employeeRepository.findByIsActiveTrue(pageable)).thenReturn(page);

        Page<Employee> result = employeeService.getEmployees(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    // Edge case: get employees with large page number
    @Test
    public void testGetEmployees_WithLargePageNumber_ShouldReturnEmptyPage() {
        Pageable pageable = PageRequest.of(100, 10);
        Page<Employee> page = new PageImpl<>(Collections.emptyList());
        when(employeeRepository.findByIsActiveTrue(pageable)).thenReturn(page);

        Page<Employee> result = employeeService.getEmployees(pageable);

        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
    }

    // Normal case: get employees by department
    @Test
    public void testGetEmployeesByDepartment_ShouldReturnEmployees() {
        List<Employee> employees = Arrays.asList(validEmployee);
        when(employeeRepository.findByDepartment("Logistics")).thenReturn(employees);

        List<Employee> result = employeeService.getEmployeesByDepartment("Logistics");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Logistics", result.get(0).getDepartment());
    }

    // Edge case: get employees by invalid department
    @Test
    public void testGetEmployeesByDepartment_WithInvalidDepartment_ShouldReturnEmptyList() {
        when(employeeRepository.findByDepartment("UnknownDept")).thenReturn(Collections.emptyList());

        List<Employee> result = employeeService.getEmployeesByDepartment("UnknownDept");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // Normal case: update employee (full update)
    @Test
    public void testUpdateEmployee_WithValidData_ShouldReturnUpdatedEmployee() {
        Employee updatedDetails = new Employee();
        updatedDetails.setName("Jane Smith");
        updatedDetails.setBadgeId("EMP001");
        updatedDetails.setEmail("jane.smith@warehouse.com");
        updatedDetails.setRole("SUPERVISOR");
        updatedDetails.setDepartment("Packing");
        updatedDetails.setShiftGroup("B");
        updatedDetails.setHireDate(LocalDate.of(2024, 1, 1));
        updatedDetails.setStatus("ACTIVE");
        updatedDetails.setIsActive(true);

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(updatedDetails);

        Employee result = employeeService.updateEmployee(1L, updatedDetails);

        assertNotNull(result);
        assertEquals("Jane Smith", result.getName());
        assertEquals("SUPERVISOR", result.getRole());
    }

    // Edge case: update employee with null details
    @Test
    public void testUpdateEmployee_WithNullDetails_ShouldThrowException() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));

        assertThrows(IllegalArgumentException.class, () -> employeeService.updateEmployee(1L, null));
    }

    // Edge case: update non-existent employee
    @Test
    public void testUpdateEmployee_WithNonExistentId_ShouldThrowException() {
        Employee updatedDetails = new Employee();
        updatedDetails.setName("Jane Smith");
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> employeeService.updateEmployee(99L, updatedDetails));
    }

    // Normal case: partial update employee
    @Test
    public void testPartialUpdateEmployee_WithValidUpdates_ShouldReturnUpdatedEmployee() {
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", "Updated Name");
        updates.put("email", "updated.email@warehouse.com");

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(validEmployee);

        Employee result = employeeService.partialUpdateEmployee(1L, updates);

        assertNotNull(result);
        assertEquals("Updated Name", result.getName());
        assertEquals("updated.email@warehouse.com", result.getEmail());
    }

    // Edge case: partial update with invalid field
    @Test
    public void testPartialUpdateEmployee_WithInvalidField_ShouldIgnoreField() {
        Map<String, Object> updates = new HashMap<>();
        updates.put("nonExistentField", "value");

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(validEmployee);

        Employee result = employeeService.partialUpdateEmployee(1L, updates);

        assertNotNull(result);
        // Should not throw, and nonExistentField should be ignored
        assertEquals("John Doe", result.getName());
    }

    // Edge case: partial update on non-existent employee
    @Test
    public void testPartialUpdateEmployee_WithNonExistentId_ShouldThrowException() {
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", "Updated Name");

        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> employeeService.partialUpdateEmployee(99L, updates));
    }

    // Normal case: soft delete employee
    @Test
    public void testDeleteEmployee_WithValidId_ShouldSetIsActiveFalse() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(validEmployee);

        employeeService.deleteEmployee(1L);

        assertFalse(validEmployee.getIsActive());
        verify(employeeRepository, times(1)).save(validEmployee);
    }

    // Edge case: soft delete non-existent employee
    @Test
    public void testDeleteEmployee_WithNonExistentId_ShouldThrowException() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> employeeService.deleteEmployee(99L));
    }

    // Normal case: hard delete employee
    @Test
    public void testHardDeleteEmployee_WithValidId_ShouldDeleteEmployee() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        doNothing().when(employeeRepository).delete(validEmployee);

        employeeService.hardDeleteEmployee(1L);

        verify(employeeRepository, times(1)).delete(validEmployee);
    }

    // Edge case: hard delete non-existent employee
    @Test
    public void testHardDeleteEmployee_WithNonExistentId_ShouldThrowException() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> employeeService.hardDeleteEmployee(99L));
    }

    // Edge case: concurrent updates (simulate by calling update twice)
    @Test
    public void testConcurrentUpdateEmployee_ShouldHandleGracefully() {
        Employee updatedDetails1 = new Employee();
        updatedDetails1.setName("Concurrent One");
        Employee updatedDetails2 = new Employee();
        updatedDetails2.setName("Concurrent Two");

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(updatedDetails1).thenReturn(updatedDetails2);

        Employee result1 = employeeService.updateEmployee(1L, updatedDetails1);
        Employee result2 = employeeService.updateEmployee(1L, updatedDetails2);

        assertEquals("Concurrent One", result1.getName());
        assertEquals("Concurrent Two", result2.getName());
    }

    // Edge case: retrieve soft-deleted employee
    @Test
    public void testGetEmployeeById_SoftDeleted_ShouldThrowException() {
        validEmployee.setIsActive(false);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));

        assertThrows(NoSuchElementException.class, () -> employeeService.getEmployeeById(1L));
    }

    // Exception handling: transaction rollback on error (simulate by throwing exception)
    @Test
    public void testCreateEmployee_WhenRepositoryThrows_ShouldPropagateException() {
        when(employeeRepository.findByBadgeId("EMP001")).thenReturn(Optional.empty());
        when(employeeRepository.save(any(Employee.class))).thenThrow(new RuntimeException("DB error"));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> employeeService.createEmployee(validEmployee));
        assertEquals("DB error", ex.getMessage());
    }
}