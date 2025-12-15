package com.warehouse.ems.employee.service;

import com.warehouse.ems.employee.domain.Employee;
import com.warehouse.ems.employee.dto.EmployeeRequestDTO;
import com.warehouse.ems.employee.dto.EmployeeResponseDTO;
import com.warehouse.ems.employee.repository.EmployeeRepository;
import com.warehouse.ems.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Comprehensive JUnit test class for EmployeeService.
 * Tests all CRUD operations, edge cases, boundary conditions, and exception scenarios.
 * Uses Mockito for mocking EmployeeRepository dependency.
 *
 * @author Automation Test Engineer
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
public class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeService employeeService;

    private Employee testEmployee;
    private EmployeeRequestDTO testRequestDTO;
    private EmployeeResponseDTO testResponseDTO;

    /**
     * Setup method to initialize test data before each test.
     * Creates sample Employee entity, request DTO, and response DTO.
     */
    @BeforeEach
    public void setUp() {
        // Arrange - Create test employee entity
        testEmployee = Employee.builder()
                .id(1L)
                .name("John Doe")
                .badgeId("EMP001")
                .email("john.doe@warehouse.com")
                .phone("+1-555-0100")
                .role("WORKER")
                .department("Shipping")
                .shiftGroup("Morning")
                .hireDate(LocalDate.of(2023, 1, 15))
                .status("ACTIVE")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .createdBy("admin")
                .updatedBy("admin")
                .build();

        // Arrange - Create test request DTO
        testRequestDTO = EmployeeRequestDTO.builder()
                .name("John Doe")
                .badgeId("EMP001")
                .email("john.doe@warehouse.com")
                .phone("+1-555-0100")
                .role("WORKER")
                .department("Shipping")
                .shiftGroup("Morning")
                .hireDate(LocalDate.of(2023, 1, 15))
                .status("ACTIVE")
                .build();

        // Arrange - Create test response DTO
        testResponseDTO = EmployeeResponseDTO.builder()
                .id(1L)
                .name("John Doe")
                .badgeId("EMP001")
                .email("john.doe@warehouse.com")
                .phone("+1-555-0100")
                .role("WORKER")
                .department("Shipping")
                .shiftGroup("Morning")
                .hireDate(LocalDate.of(2023, 1, 15))
                .status("ACTIVE")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .createdBy("admin")
                .updatedBy("admin")
                .build();
    }

    // ==================== CREATE EMPLOYEE TESTS ====================

    /**
     * Test creating an employee with valid input.
     * Expected: Employee is created successfully and response DTO is returned.
     */
    @Test
    public void testCreateEmployee_WithValidInput_ReturnsCreatedEmployee() {
        // Arrange
        when(employeeRepository.findByBadgeId(anyString())).thenReturn(Optional.empty());
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        EmployeeResponseDTO result = employeeService.createEmployee(testRequestDTO);

        // Assert
        assertNotNull(result);
        assertEquals("John Doe", result.getName());
        assertEquals("EMP001", result.getBadgeId());
        assertEquals("john.doe@warehouse.com", result.getEmail());
        verify(employeeRepository, times(1)).findByBadgeId("EMP001");
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    /**
     * Test creating an employee with null input.
     * Expected: NullPointerException or IllegalArgumentException is thrown.
     */
    @Test
    public void testCreateEmployee_WithNullInput_ThrowsException() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            employeeService.createEmployee(null);
        });
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    /**
     * Test creating an employee with duplicate badgeId.
     * Expected: IllegalArgumentException is thrown indicating badgeId already exists.
     */
    @Test
    public void testCreateEmployee_WithDuplicateBadgeId_ThrowsException() {
        // Arrange
        when(employeeRepository.findByBadgeId("EMP001")).thenReturn(Optional.of(testEmployee));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.createEmployee(testRequestDTO);
        });
        verify(employeeRepository, times(1)).findByBadgeId("EMP001");
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    /**
     * Test creating an employee with invalid email format.
     * Expected: Validation exception is thrown.
     */
    @Test
    public void testCreateEmployee_WithInvalidEmail_ThrowsException() {
        // Arrange
        testRequestDTO.setEmail("invalid-email");
        when(employeeRepository.findByBadgeId(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.createEmployee(testRequestDTO);
        });
    }

    /**
     * Test creating an employee with empty name.
     * Expected: IllegalArgumentException is thrown.
     */
    @Test
    public void testCreateEmployee_WithEmptyName_ThrowsException() {
        // Arrange
        testRequestDTO.setName("");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.createEmployee(testRequestDTO);
        });
    }

    /**
     * Test creating an employee with null badgeId.
     * Expected: IllegalArgumentException is thrown.
     */
    @Test
    public void testCreateEmployee_WithNullBadgeId_ThrowsException() {
        // Arrange
        testRequestDTO.setBadgeId(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.createEmployee(testRequestDTO);
        });
    }

    // ==================== GET EMPLOYEE BY ID TESTS ====================

    /**
     * Test retrieving an employee by valid ID.
     * Expected: Employee is found and response DTO is returned.
     */
    @Test
    public void testGetEmployeeById_WithValidId_ReturnsEmployee() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));

        // Act
        EmployeeResponseDTO result = employeeService.getEmployeeById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("John Doe", result.getName());
        assertEquals("EMP001", result.getBadgeId());
        verify(employeeRepository, times(1)).findById(1L);
    }

    /**
     * Test retrieving an employee with invalid/non-existent ID.
     * Expected: ResourceNotFoundException is thrown.
     */
    @Test
    public void testGetEmployeeById_WithInvalidId_ThrowsResourceNotFoundException() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            employeeService.getEmployeeById(999L);
        });
        verify(employeeRepository, times(1)).findById(999L);
    }

    /**
     * Test retrieving an employee with null ID.
     * Expected: IllegalArgumentException is thrown.
     */
    @Test
    public void testGetEmployeeById_WithNullId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.getEmployeeById(null);
        });
    }

    /**
     * Test retrieving an employee with negative ID.
     * Expected: IllegalArgumentException is thrown.
     */
    @Test
    public void testGetEmployeeById_WithNegativeId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.getEmployeeById(-1L);
        });
    }

    /**
     * Test retrieving an employee with zero ID.
     * Expected: IllegalArgumentException is thrown.
     */
    @Test
    public void testGetEmployeeById_WithZeroId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.getEmployeeById(0L);
        });
    }

    // ==================== GET EMPLOYEE BY BADGE ID TESTS ====================

    /**
     * Test retrieving an employee by valid badgeId.
     * Expected: Employee is found and response DTO is returned.
     */
    @Test
    public void testGetEmployeeByBadgeId_WithValidBadgeId_ReturnsEmployee() {
        // Arrange
        when(employeeRepository.findByBadgeId("EMP001")).thenReturn(Optional.of(testEmployee));

        // Act
        EmployeeResponseDTO result = employeeService.getEmployeeByBadgeId("EMP001");

        // Assert
        assertNotNull(result);
        assertEquals("EMP001", result.getBadgeId());
        assertEquals("John Doe", result.getName());
        verify(employeeRepository, times(1)).findByBadgeId("EMP001");
    }

    /**
     * Test retrieving an employee with non-existent badgeId.
     * Expected: ResourceNotFoundException is thrown.
     */
    @Test
    public void testGetEmployeeByBadgeId_WithNonExistentBadgeId_ThrowsResourceNotFoundException() {
        // Arrange
        when(employeeRepository.findByBadgeId("INVALID")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            employeeService.getEmployeeByBadgeId("INVALID");
        });
        verify(employeeRepository, times(1)).findByBadgeId("INVALID");
    }

    /**
     * Test retrieving an employee with null badgeId.
     * Expected: IllegalArgumentException is thrown.
     */
    @Test
    public void testGetEmployeeByBadgeId_WithNullBadgeId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.getEmployeeByBadgeId(null);
        });
    }

    /**
     * Test retrieving an employee with empty badgeId.
     * Expected: IllegalArgumentException is thrown.
     */
    @Test
    public void testGetEmployeeByBadgeId_WithEmptyBadgeId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.getEmployeeByBadgeId("");
        });
    }

    /**
     * Test retrieving an employee with whitespace-only badgeId.
     * Expected: IllegalArgumentException is thrown.
     */
    @Test
    public void testGetEmployeeByBadgeId_WithWhitespaceBadgeId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.getEmployeeByBadgeId("   ");
        });
    }

    // ==================== GET ALL EMPLOYEES TESTS ====================

    /**
     * Test retrieving all employees with valid pageable.
     * Expected: Paged results are returned.
     */
    @Test
    public void testGetAllEmployees_WithValidPageable_ReturnsPagedResults() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<Employee> employees = Arrays.asList(testEmployee);
        Page<Employee> employeePage = new PageImpl<>(employees, pageable, employees.size());
        when(employeeRepository.findAll(pageable)).thenReturn(employeePage);

        // Act
        Page<EmployeeResponseDTO> result = employeeService.getAllEmployees(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        assertEquals("John Doe", result.getContent().get(0).getName());
        verify(employeeRepository, times(1)).findAll(pageable);
    }

    /**
     * Test retrieving all employees when database is empty.
     * Expected: Empty page is returned.
     */
    @Test
    public void testGetAllEmployees_WithEmptyDatabase_ReturnsEmptyPage() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
        when(employeeRepository.findAll(pageable)).thenReturn(emptyPage);

        // Act
        Page<EmployeeResponseDTO> result = employeeService.getAllEmployees(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
        verify(employeeRepository, times(1)).findAll(pageable);
    }

    /**
     * Test retrieving all employees with null pageable.
     * Expected: IllegalArgumentException is thrown.
     */
    @Test
    public void testGetAllEmployees_WithNullPageable_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.getAllEmployees(null);
        });
    }

    /**
     * Test retrieving all employees with large page size.
     * Expected: Results are returned with specified page size.
     */
    @Test
    public void testGetAllEmployees_WithLargePageSize_ReturnsResults() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 1000);
        List<Employee> employees = Arrays.asList(testEmployee);
        Page<Employee> employeePage = new PageImpl<>(employees, pageable, employees.size());
        when(employeeRepository.findAll(pageable)).thenReturn(employeePage);

        // Act
        Page<EmployeeResponseDTO> result = employeeService.getAllEmployees(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(employeeRepository, times(1)).findAll(pageable);
    }

    // ==================== SEARCH EMPLOYEES TESTS ====================

    /**
     * Test searching employees with all filters.
     * Expected: Filtered results are returned.
     */
    @Test
    public void testSearchEmployees_WithAllFilters_ReturnsFilteredResults() {
        // Arrange
        List<Employee> employees = Arrays.asList(testEmployee);
        when(employeeRepository.findByNameContainingAndDepartmentAndStatus(
                "John", "Shipping", "ACTIVE")).thenReturn(employees);

        // Act
        List<EmployeeResponseDTO> result = employeeService.searchEmployees("John", "Shipping", "ACTIVE");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("John Doe", result.get(0).getName());
        verify(employeeRepository, times(1)).findByNameContainingAndDepartmentAndStatus(
                "John", "Shipping", "ACTIVE");
    }

    /**
     * Test searching employees with null filters.
     * Expected: All employees are returned.
     */
    @Test
    public void testSearchEmployees_WithNullFilters_ReturnsAllEmployees() {
        // Arrange
        List<Employee> employees = Arrays.asList(testEmployee);
        when(employeeRepository.findAll()).thenReturn(employees);

        // Act
        List<EmployeeResponseDTO> result = employeeService.searchEmployees(null, null, null);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(employeeRepository, times(1)).findAll();
    }

    /**
     * Test searching employees with no matches.
     * Expected: Empty list is returned.
     */
    @Test
    public void testSearchEmployees_WithNoMatches_ReturnsEmptyList() {
        // Arrange
        when(employeeRepository.findByNameContainingAndDepartmentAndStatus(
                "NonExistent", "InvalidDept", "INACTIVE")).thenReturn(Collections.emptyList());

        // Act
        List<EmployeeResponseDTO> result = employeeService.searchEmployees(
                "NonExistent", "InvalidDept", "INACTIVE");

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(employeeRepository, times(1)).findByNameContainingAndDepartmentAndStatus(
                "NonExistent", "InvalidDept", "INACTIVE");
    }

    /**
     * Test searching employees with partial name match.
     * Expected: Matching employees are returned.
     */
    @Test
    public void testSearchEmployees_WithPartialName_ReturnsMatches() {
        // Arrange
        List<Employee> employees = Arrays.asList(testEmployee);
        when(employeeRepository.findByNameContaining("John")).thenReturn(employees);

        // Act
        List<EmployeeResponseDTO> result = employeeService.searchEmployees("John", null, null);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("John Doe", result.get(0).getName());
    }

    // ==================== UPDATE EMPLOYEE TESTS ====================

    /**
     * Test updating an employee with valid input.
     * Expected: Employee is updated successfully.
     */
    @Test
    public void testUpdateEmployee_WithValidInput_ReturnsUpdatedEmployee() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);
        testRequestDTO.setDepartment("Receiving");

        // Act
        EmployeeResponseDTO result = employeeService.updateEmployee(1L, testRequestDTO);

        // Assert
        assertNotNull(result);
        verify(employeeRepository, times(1)).findById(1L);
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    /**
     * Test updating an employee with invalid ID.
     * Expected: ResourceNotFoundException is thrown.
     */
    @Test
    public void testUpdateEmployee_WithInvalidId_ThrowsResourceNotFoundException() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            employeeService.updateEmployee(999L, testRequestDTO);
        });
        verify(employeeRepository, times(1)).findById(999L);
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    /**
     * Test updating an employee with null fields.
     * Expected: IllegalArgumentException is thrown.
     */
    @Test
    public void testUpdateEmployee_WithNullFields_ThrowsException() {
        // Arrange
        testRequestDTO.setName(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.updateEmployee(1L, testRequestDTO);
        });
    }

    /**
     * Test updating an employee with null request DTO.
     * Expected: NullPointerException is thrown.
     */
    @Test
    public void testUpdateEmployee_WithNullRequestDTO_ThrowsException() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            employeeService.updateEmployee(1L, null);
        });
    }

    // ==================== UPDATE EMPLOYEE STATUS TESTS ====================

    /**
     * Test updating employee status with valid status.
     * Expected: Status is updated successfully.
     */
    @Test
    public void testUpdateEmployeeStatus_WithValidStatus_UpdatesSuccessfully() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        employeeService.updateEmployeeStatus(1L, "INACTIVE");

        // Assert
        verify(employeeRepository, times(1)).findById(1L);
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    /**
     * Test updating employee status with invalid status.
     * Expected: IllegalArgumentException is thrown.
     */
    @Test
    public void testUpdateEmployeeStatus_WithInvalidStatus_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.updateEmployeeStatus(1L, "INVALID_STATUS");
        });
    }

    /**
     * Test updating employee status with null status.
     * Expected: IllegalArgumentException is thrown.
     */
    @Test
    public void testUpdateEmployeeStatus_WithNullStatus_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.updateEmployeeStatus(1L, null);
        });
    }

    /**
     * Test updating employee status with empty status.
     * Expected: IllegalArgumentException is thrown.
     */
    @Test
    public void testUpdateEmployeeStatus_WithEmptyStatus_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.updateEmployeeStatus(1L, "");
        });
    }

    // ==================== DELETE EMPLOYEE TESTS ====================

    /**
     * Test soft deleting an employee with valid ID.
     * Expected: Employee is soft deleted (deletedAt is set).
     */
    @Test
    public void testDeleteEmployee_WithValidId_SoftDeletesEmployee() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        employeeService.deleteEmployee(1L);

        // Assert
        verify(employeeRepository, times(1)).findById(1L);
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    /**
     * Test deleting an employee with invalid ID.
     * Expected: ResourceNotFoundException is thrown.
     */
    @Test
    public void testDeleteEmployee_WithInvalidId_ThrowsResourceNotFoundException() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            employeeService.deleteEmployee(999L);
        });
        verify(employeeRepository, times(1)).findById(999L);
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    /**
     * Test deleting an already deleted employee.
     * Expected: Operation handles gracefully or throws appropriate exception.
     */
    @Test
    public void testDeleteEmployee_WithAlreadyDeletedEmployee_HandlesGracefully() {
        // Arrange
        testEmployee.setDeletedAt(LocalDateTime.now());
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            employeeService.deleteEmployee(1L);
        });
        verify(employeeRepository, times(1)).findById(1L);
    }

    /**
     * Test deleting an employee with null ID.
     * Expected: IllegalArgumentException is thrown.
     */
    @Test
    public void testDeleteEmployee_WithNullId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.deleteEmployee(null);
        });
    }

    /**
     * Test deleting an employee with negative ID.
     * Expected: IllegalArgumentException is thrown.
     */
    @Test
    public void testDeleteEmployee_WithNegativeId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.deleteEmployee(-1L);
        });
    }
}