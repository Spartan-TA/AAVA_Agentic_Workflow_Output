package com.warehouse.employee.service;

import com.warehouse.employee.domain.Employee;
import com.warehouse.employee.dto.EmployeeCreateRequest;
import com.warehouse.employee.dto.EmployeeDTO;
import com.warehouse.employee.exception.DuplicateResourceException;
import com.warehouse.employee.exception.ResourceNotFoundException;
import com.warehouse.employee.repository.EmployeeRepository;
import org.junit.jupiter.api.*;
import org.mockito.*;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive JUnit 5 test suite for EmployeeServiceImpl.
 * Covers normal cases, edge cases, and exception handling for all public service methods.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class EmployeeServiceImplTest {
    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * Test createEmployee with valid input.
     */
    @Test
    @DisplayName("Create employee with valid input should return EmployeeDTO")
    void testCreateEmployee_ValidInput_ReturnsEmployeeDTO() {
        // Arrange
        EmployeeCreateRequest request = new EmployeeCreateRequest();
        request.setName("John Doe");
        request.setBadgeId("EMP12345");
        request.setRole("WORKER");
        request.setEmail("john.doe@example.com");
        request.setPhone("1234567890");
        request.setDepartment("Logistics");
        request.setHireDate(LocalDate.now());
        request.setStatus("ACTIVE");

        when(employeeRepository.existsByBadgeIdAndDeletedFalse("EMP12345")).thenReturn(false);
        when(employeeRepository.save(any(Employee.class))).thenAnswer(i -> {
            Employee emp = i.getArgument(0);
            emp.setId(1L);
            return emp;
        });

        // Act
        EmployeeDTO result = employeeService.createEmployee(request);

        // Assert
        assertNotNull(result);
        assertEquals("John Doe", result.getName());
        assertEquals("EMP12345", result.getBadgeId());
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    /**
     * Test createEmployee with duplicate badgeId.
     */
    @Test
    @DisplayName("Create employee with duplicate badgeId should throw DuplicateResourceException")
    void testCreateEmployee_DuplicateBadgeId_ThrowsException() {
        // Arrange
        EmployeeCreateRequest request = new EmployeeCreateRequest();
        request.setBadgeId("EMP12345");

        when(employeeRepository.existsByBadgeIdAndDeletedFalse("EMP12345")).thenReturn(true);

        // Act & Assert
        assertThrows(DuplicateResourceException.class, () -> {
            employeeService.createEmployee(request);
        });
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    /**
     * Test createEmployee with null name.
     */
    @Test
    @DisplayName("Create employee with null name should throw IllegalArgumentException")
    void testCreateEmployee_NullName_ThrowsException() {
        // Arrange
        EmployeeCreateRequest request = new EmployeeCreateRequest();
        request.setBadgeId("EMP12346");
        request.setName(null);
        request.setRole("WORKER");

        when(employeeRepository.existsByBadgeIdAndDeletedFalse("EMP12346")).thenReturn(false);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> employeeService.createEmployee(request));
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    /**
     * Test createEmployee with invalid role.
     */
    @Test
    @DisplayName("Create employee with invalid role should throw IllegalArgumentException")
    void testCreateEmployee_InvalidRole_ThrowsException() {
        // Arrange
        EmployeeCreateRequest request = new EmployeeCreateRequest();
        request.setBadgeId("EMP12347");
        request.setName("Jane Doe");
        request.setRole("INVALID_ROLE");

        when(employeeRepository.existsByBadgeIdAndDeletedFalse("EMP12347")).thenReturn(false);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> employeeService.createEmployee(request));
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    /**
     * Test createEmployee with invalid email format.
     */
    @Test
    @DisplayName("Create employee with invalid email format should throw IllegalArgumentException")
    void testCreateEmployee_InvalidEmailFormat_ThrowsException() {
        // Arrange
        EmployeeCreateRequest request = new EmployeeCreateRequest();
        request.setBadgeId("EMP12348");
        request.setName("Jake Doe");
        request.setRole("WORKER");
        request.setEmail("invalid-email");

        when(employeeRepository.existsByBadgeIdAndDeletedFalse("EMP12348")).thenReturn(false);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> employeeService.createEmployee(request));
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    /**
     * Test createEmployee with invalid phone format.
     */
    @Test
    @DisplayName("Create employee with invalid phone format should throw IllegalArgumentException")
    void testCreateEmployee_InvalidPhoneFormat_ThrowsException() {
        // Arrange
        EmployeeCreateRequest request = new EmployeeCreateRequest();
        request.setBadgeId("EMP12349");
        request.setName("Jill Doe");
        request.setRole("WORKER");
        request.setPhone("abcde");

        when(employeeRepository.existsByBadgeIdAndDeletedFalse("EMP12349")).thenReturn(false);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> employeeService.createEmployee(request));
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    /**
     * Test getEmployee with existing employee.
     */
    @Test
    @DisplayName("Get employee with valid id should return EmployeeDTO")
    void testGetEmployee_ExistingId_ReturnsEmployeeDTO() {
        // Arrange
        Employee employee = new Employee();
        employee.setId(1L);
        employee.setName("John Doe");
        employee.setBadgeId("EMP12345");
        employee.setDeleted(false);

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        // Act
        EmployeeDTO result = employeeService.getEmployee(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("John Doe", result.getName());
    }

    /**
     * Test getEmployee with non-existent employee.
     */
    @Test
    @DisplayName("Get employee with non-existent id should throw ResourceNotFoundException")
    void testGetEmployee_NonExistentId_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> employeeService.getEmployee(99L));
    }

    /**
     * Test getEmployee with deleted employee.
     */
    @Test
    @DisplayName("Get employee with deleted flag should throw ResourceNotFoundException")
    void testGetEmployee_DeletedEmployee_ThrowsException() {
        // Arrange
        Employee employee = new Employee();
        employee.setId(2L);
        employee.setDeleted(true);

        when(employeeRepository.findById(2L)).thenReturn(Optional.of(employee));

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> employeeService.getEmployee(2L));
    }

    /**
     * Test updateEmployee with valid updates.
     */
    @Test
    @DisplayName("Update employee with valid input should update and return EmployeeDTO")
    void testUpdateEmployee_ValidInput_ReturnsEmployeeDTO() {
        // Arrange
        Employee employee = new Employee();
        employee.setId(3L);
        employee.setName("Old Name");
        employee.setBadgeId("EMP12350");
        employee.setDeleted(false);

        EmployeeCreateRequest updateRequest = new EmployeeCreateRequest();
        updateRequest.setName("New Name");
        updateRequest.setBadgeId("EMP12350");

        when(employeeRepository.findById(3L)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(any(Employee.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        EmployeeDTO result = employeeService.updateEmployee(3L, updateRequest);

        // Assert
        assertNotNull(result);
        assertEquals("New Name", result.getName());
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    /**
     * Test updateEmployee with partial updates.
     */
    @Test
    @DisplayName("Update employee with partial input should update only provided fields")
    void testUpdateEmployee_PartialUpdate_UpdatesFields() {
        // Arrange
        Employee employee = new Employee();
        employee.setId(4L);
        employee.setName("Old Name");
        employee.setBadgeId("EMP12351");
        employee.setRole("WORKER");
        employee.setDeleted(false);

        EmployeeCreateRequest updateRequest = new EmployeeCreateRequest();
        updateRequest.setName("Partial Name"); // Only name is updated

        when(employeeRepository.findById(4L)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(any(Employee.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        EmployeeDTO result = employeeService.updateEmployee(4L, updateRequest);

        // Assert
        assertNotNull(result);
        assertEquals("Partial Name", result.getName());
        assertEquals("WORKER", result.getRole()); // Role remains unchanged
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    /**
     * Test updateEmployee with invalid status transition.
     */
    @Test
    @DisplayName("Update employee with invalid status transition should throw IllegalArgumentException")
    void testUpdateEmployee_InvalidStatusTransition_ThrowsException() {
        // Arrange
        Employee employee = new Employee();
        employee.setId(5L);
        employee.setStatus("ACTIVE");
        employee.setDeleted(false);

        EmployeeCreateRequest updateRequest = new EmployeeCreateRequest();
        updateRequest.setStatus("INVALID_STATUS");

        when(employeeRepository.findById(5L)).thenReturn(Optional.of(employee));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> employeeService.updateEmployee(5L, updateRequest));
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    /**
     * Test deleteEmployee with valid id (soft delete).
     */
    @Test
    @DisplayName("Delete employee with valid id should set deleted flag")
    void testDeleteEmployee_ValidId_SoftDelete() {
        // Arrange
        Employee employee = new Employee();
        employee.setId(6L);
        employee.setDeleted(false);

        when(employeeRepository.findById(6L)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(any(Employee.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        employeeService.deleteEmployee(6L);

        // Assert
        assertTrue(employee.isDeleted());
        verify(employeeRepository, times(1)).save(employee);
    }

    /**
     * Test deleteEmployee with already deleted employee.
     */
    @Test
    @DisplayName("Delete employee with already deleted flag should throw ResourceNotFoundException")
    void testDeleteEmployee_AlreadyDeleted_ThrowsException() {
        // Arrange
        Employee employee = new Employee();
        employee.setId(7L);
        employee.setDeleted(true);

        when(employeeRepository.findById(7L)).thenReturn(Optional.of(employee));

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> employeeService.deleteEmployee(7L));
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    /**
     * Test searchEmployees filtering by department.
     */
    @Test
    @DisplayName("Search employees by department should return filtered list")
    void testSearchEmployees_FilterByDepartment_ReturnsFilteredList() {
        // Arrange
        Employee emp1 = new Employee();
        emp1.setId(8L);
        emp1.setDepartment("Logistics");
        emp1.setDeleted(false);
        Employee emp2 = new Employee();
        emp2.setId(9L);
        emp2.setDepartment("HR");
        emp2.setDeleted(false);
        List<Employee> employees = Arrays.asList(emp1, emp2);

        when(employeeRepository.findByFilters("Logistics", null, null)).thenReturn(Collections.singletonList(emp1));

        // Act
        List<EmployeeDTO> result = employeeService.searchEmployees("Logistics", null, null);

        // Assert
        assertEquals(1, result.size());
        assertEquals("Logistics", result.get(0).getDepartment());
    }

    /**
     * Test searchEmployees with pagination.
     */
    @Test
    @DisplayName("Search employees with pagination should return correct page")
    void testSearchEmployees_Pagination_ReturnsCorrectPage() {
        // Arrange
        Employee emp1 = new Employee();
        emp1.setId(10L);
        emp1.setDepartment("Logistics");
        emp1.setDeleted(false);
        Employee emp2 = new Employee();
        emp2.setId(11L);
        emp2.setDepartment("Logistics");
        emp2.setDeleted(false);
        List<Employee> employees = Arrays.asList(emp1, emp2);

        when(employeeRepository.findByFilters("Logistics", null, null)).thenReturn(employees);

        // Act
        List<EmployeeDTO> result = employeeService.searchEmployees("Logistics", null, null, 0, 1);

        // Assert
        assertEquals(1, result.size());
        assertEquals(10L, result.get(0).getId());
    }

    /**
     * Test listEmployees returns all non-deleted employees.
     */
    @Test
    @DisplayName("List employees should return all non-deleted employees")
    void testListEmployees_ReturnsNonDeletedEmployees() {
        // Arrange
        Employee emp1 = new Employee();
        emp1.setId(12L);
        emp1.setDeleted(false);
        Employee emp2 = new Employee();
        emp2.setId(13L);
        emp2.setDeleted(true);
        List<Employee> employees = Arrays.asList(emp1, emp2);

        when(employeeRepository.findAllByDeletedFalse()).thenReturn(Collections.singletonList(emp1));

        // Act
        List<EmployeeDTO> result = employeeService.listEmployees();

        // Assert
        assertEquals(1, result.size());
        assertEquals(12L, result.get(0).getId());
    }

    /**
     * Test createEmployee with null request.
     */
    @Test
    @DisplayName("Create employee with null request should throw IllegalArgumentException")
    void testCreateEmployee_NullRequest_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> employeeService.createEmployee(null));
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    /**
     * Test updateEmployee with non-existent employee.
     */
    @Test
    @DisplayName("Update employee with non-existent id should throw ResourceNotFoundException")
    void testUpdateEmployee_NonExistentId_ThrowsException() {
        // Arrange
        EmployeeCreateRequest updateRequest = new EmployeeCreateRequest();
        updateRequest.setName("New Name");
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> employeeService.updateEmployee(99L, updateRequest));
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    /**