package com.warehouse.employee;

import com.warehouse.employee.entity.Employee;
import com.warehouse.employee.repository.EmployeeRepository;
import com.warehouse.employee.service.EmployeeService;
import com.warehouse.employee.dto.EmployeeDTO;
import com.warehouse.employee.exception.ResourceNotFoundException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive JUnit test suite for EmployeeService
 * Covers all method signatures with normal, boundary, and edge cases
 * 
 * @author Automation Test Engineer
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeService employeeService;

    private Employee employee;
    private EmployeeDTO employeeDTO;

    @BeforeEach
    void setUp() {
        // Initialize test data
        employee = new Employee();
        employee.setId(1L);
        employee.setName("John Doe");
        employee.setBadgeId("EMP001");
        employee.setEmail("john.doe@warehouse.com");
        employee.setRole("WORKER");
        employee.setDepartment("Shipping");
        employee.setShiftGroup("Morning");
        employee.setHireDate(LocalDate.of(2023, 1, 15));
        employee.setStatus("ACTIVE");

        employeeDTO = new EmployeeDTO();
        employeeDTO.setId(1L);
        employeeDTO.setName("John Doe");
        employeeDTO.setBadgeId("EMP001");
        employeeDTO.setEmail("john.doe@warehouse.com");
        employeeDTO.setRole("WORKER");
        employeeDTO.setDepartment("Shipping");
    }

    @AfterEach
    void tearDown() {
        // Clean up resources if needed
        employee = null;
        employeeDTO = null;
    }

    // ==================== CREATE EMPLOYEE TESTS ====================

    @Test
    @DisplayName("Test createEmployee with valid input - should return created employee")
    void testCreateEmployee_ValidInput_ReturnsCreatedEmployee() {
        // Arrange
        when(employeeRepository.existsByBadgeId("EMP001")).thenReturn(false);
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

        // Act
        EmployeeDTO result = employeeService.createEmployee(employeeDTO);

        // Assert
        assertNotNull(result);
        assertEquals("John Doe", result.getName());
        assertEquals("EMP001", result.getBadgeId());
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    @DisplayName("Test createEmployee with null input - should throw IllegalArgumentException")
    void testCreateEmployee_NullInput_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.createEmployee(null);
        });
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    @DisplayName("Test createEmployee with duplicate badgeId - should throw IllegalArgumentException")
    void testCreateEmployee_DuplicateBadgeId_ThrowsException() {
        // Arrange
        when(employeeRepository.existsByBadgeId("EMP001")).thenReturn(true);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.createEmployee(employeeDTO);
        });
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    @DisplayName("Test createEmployee with empty name - should throw IllegalArgumentException")
    void testCreateEmployee_EmptyName_ThrowsException() {
        // Arrange
        employeeDTO.setName("");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.createEmployee(employeeDTO);
        });
    }

    @Test
    @DisplayName("Test createEmployee with null badgeId - should throw IllegalArgumentException")
    void testCreateEmployee_NullBadgeId_ThrowsException() {
        // Arrange
        employeeDTO.setBadgeId(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.createEmployee(employeeDTO);
        });
    }

    @Test
    @DisplayName("Test createEmployee with invalid email format - should throw IllegalArgumentException")
    void testCreateEmployee_InvalidEmail_ThrowsException() {
        // Arrange
        employeeDTO.setEmail("invalid-email");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.createEmployee(employeeDTO);
        });
    }

    // ==================== GET EMPLOYEE TESTS ====================

    @Test
    @DisplayName("Test getEmployeeById with valid ID - should return employee")
    void testGetEmployeeById_ValidId_ReturnsEmployee() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        // Act
        EmployeeDTO result = employeeService.getEmployeeById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("John Doe", result.getName());
        verify(employeeRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Test getEmployeeById with non-existent ID - should throw ResourceNotFoundException")
    void testGetEmployeeById_NonExistentId_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            employeeService.getEmployeeById(999L);
        });
    }

    @Test
    @DisplayName("Test getEmployeeById with null ID - should throw IllegalArgumentException")
    void testGetEmployeeById_NullId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.getEmployeeById(null);
        });
    }

    @Test
    @DisplayName("Test getEmployeeById with negative ID - should throw IllegalArgumentException")
    void testGetEmployeeById_NegativeId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.getEmployeeById(-1L);
        });
    }

    // ==================== UPDATE EMPLOYEE TESTS ====================

    @Test
    @DisplayName("Test updateEmployee with valid input - should return updated employee")
    void testUpdateEmployee_ValidInput_ReturnsUpdatedEmployee() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);
        
        EmployeeDTO updateDTO = new EmployeeDTO();
        updateDTO.setName("Jane Doe");
        updateDTO.setEmail("jane.doe@warehouse.com");
        updateDTO.setDepartment("Receiving");

        // Act
        EmployeeDTO result = employeeService.updateEmployee(1L, updateDTO);

        // Assert
        assertNotNull(result);
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    @DisplayName("Test updateEmployee with non-existent ID - should throw ResourceNotFoundException")
    void testUpdateEmployee_NonExistentId_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            employeeService.updateEmployee(999L, employeeDTO);
        });
    }

    @Test
    @DisplayName("Test updateEmployee with null DTO - should throw IllegalArgumentException")
    void testUpdateEmployee_NullDTO_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.updateEmployee(1L, null);
        });
    }

    // ==================== DELETE EMPLOYEE TESTS ====================

    @Test
    @DisplayName("Test deleteEmployee (soft delete) with valid ID - should mark as deleted")
    void testDeleteEmployee_ValidId_SoftDeletesEmployee() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

        // Act
        employeeService.deleteEmployee(1L);

        // Assert
        verify(employeeRepository, times(1)).save(argThat(e -> 
            "DELETED".equals(e.getStatus())
        ));
    }

    @Test
    @DisplayName("Test deleteEmployee with non-existent ID - should throw ResourceNotFoundException")
    void testDeleteEmployee_NonExistentId_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            employeeService.deleteEmployee(999L);
        });
    }

    @Test
    @DisplayName("Test deleteEmployee with null ID - should throw IllegalArgumentException")
    void testDeleteEmployee_NullId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.deleteEmployee(null);
        });
    }

    // ==================== LIST EMPLOYEES TESTS ====================

    @Test
    @DisplayName("Test getAllEmployees with pagination - should return paginated results")
    void testGetAllEmployees_WithPagination_ReturnsPaginatedResults() {
        // Arrange
        List<Employee> employees = Arrays.asList(employee);
        Page<Employee> page = new PageImpl<>(employees);
        Pageable pageable = PageRequest.of(0, 10);
        
        when(employeeRepository.findAll(pageable)).thenReturn(page);

        // Act
        Page<EmployeeDTO> result = employeeService.getAllEmployees(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        verify(employeeRepository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("Test getAllEmployees with empty repository - should return empty page")
    void testGetAllEmployees_EmptyRepository_ReturnsEmptyPage() {
        // Arrange
        Page<Employee> emptyPage = new PageImpl<>(Collections.emptyList());
        Pageable pageable = PageRequest.of(0, 10);
        
        when(employeeRepository.findAll(pageable)).thenReturn(emptyPage);

        // Act
        Page<EmployeeDTO> result = employeeService.getAllEmployees(pageable);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        assertEquals(0, result.getTotalElements());
    }

    @Test
    @DisplayName("Test getAllEmployees with null pageable - should throw IllegalArgumentException")
    void testGetAllEmployees_NullPageable_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.getAllEmployees(null);
        });
    }

    // ==================== FILTER EMPLOYEES TESTS ====================

    @Test
    @DisplayName("Test filterEmployeesByDepartment - should return filtered results")
    void testFilterEmployeesByDepartment_ValidDepartment_ReturnsFilteredResults() {
        // Arrange
        List<Employee> employees = Arrays.asList(employee);
        when(employeeRepository.findByDepartment("Shipping")).thenReturn(employees);

        // Act
        List<EmployeeDTO> result = employeeService.filterEmployeesByDepartment("Shipping");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Shipping", result.get(0).getDepartment());
    }

    @Test
    @DisplayName("Test filterEmployeesByDepartment with null department - should throw IllegalArgumentException")
    void testFilterEmployeesByDepartment_NullDepartment_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.filterEmployeesByDepartment(null);
        });
    }

    @Test
    @DisplayName("Test filterEmployeesByStatus - should return filtered results")
    void testFilterEmployeesByStatus_ValidStatus_ReturnsFilteredResults() {
        // Arrange
        List<Employee> employees = Arrays.asList(employee);
        when(employeeRepository.findByStatus("ACTIVE")).thenReturn(employees);

        // Act
        List<EmployeeDTO> result = employeeService.filterEmployeesByStatus("ACTIVE");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("ACTIVE", result.get(0).getStatus());
    }

    // ==================== SEARCH EMPLOYEES TESTS ====================

    @Test
    @DisplayName("Test searchEmployeesByName - should return matching results")
    void testSearchEmployeesByName_ValidName_ReturnsMatchingResults() {
        // Arrange
        List<Employee> employees = Arrays.asList(employee);
        when(employeeRepository.findByNameContainingIgnoreCase("John")).thenReturn(employees);

        // Act
        List<EmployeeDTO> result = employeeService.searchEmployeesByName("John");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).getName().contains("John"));
    }

    @Test
    @DisplayName("Test searchEmployeesByName with empty string - should return all employees")
    void testSearchEmployeesByName_EmptyString_ReturnsAllEmployees() {
        // Arrange
        List<Employee> employees = Arrays.asList(employee);
        when(employeeRepository.findAll()).thenReturn(employees);

        // Act
        List<EmployeeDTO> result = employeeService.searchEmployeesByName("");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    // ==================== BADGE ID VALIDATION TESTS ====================

    @Test
    @DisplayName("Test validateBadgeId with unique badge - should return true")
    void testValidateBadgeId_UniqueBadge_ReturnsTrue() {
        // Arrange
        when(employeeRepository.existsByBadgeId("EMP002")).thenReturn(false);

        // Act
        boolean result = employeeService.validateBadgeId("EMP002");

        // Assert
        assertTrue(result);
    }

    @Test
    @DisplayName("Test validateBadgeId with duplicate badge - should return false")
    void testValidateBadgeId_DuplicateBadge_ReturnsFalse() {
        // Arrange
        when(employeeRepository.existsByBadgeId("EMP001")).thenReturn(true);

        // Act
        boolean result = employeeService.validateBadgeId("EMP001");

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("Test validateBadgeId with null badge - should throw IllegalArgumentException")
    void testValidateBadgeId_NullBadge_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.validateBadgeId(null);
        });
    }

    // ==================== BOUNDARY CONDITION TESTS ====================

    @Test
    @DisplayName("Test createEmployee with maximum length name - should succeed")
    void testCreateEmployee_MaxLengthName_Succeeds() {
        // Arrange
        String maxLengthName = "A".repeat(255);
        employeeDTO.setName(maxLengthName);
        when(employeeRepository.existsByBadgeId(anyString())).thenReturn(false);
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

        // Act
        EmployeeDTO result = employeeService.createEmployee(employeeDTO);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Test createEmployee with name exceeding maximum length - should throw exception")
    void testCreateEmployee_NameExceedsMaxLength_ThrowsException() {
        // Arrange
        String tooLongName = "A".repeat(256);
        employeeDTO.setName(tooLongName);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.createEmployee(employeeDTO);
        });
    }

    @Test
    @DisplayName("Test getAllEmployees with page size at boundary - should handle correctly")
    void testGetAllEmployees_BoundaryPageSize_HandlesCorrectly() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 1000); // Large page size
        Page<Employee> page = new PageImpl<>(Arrays.asList(employee));
        when(employeeRepository.findAll(pageable)).thenReturn(page);

        // Act
        Page<EmployeeDTO> result = employeeService.getAllEmployees(pageable);

        // Assert
        assertNotNull(result);
    }

    // ==================== CONCURRENT MODIFICATION TESTS ====================

    @Test
    @DisplayName("Test updateEmployee with concurrent modification - should handle optimistic locking")
    void testUpdateEmployee_ConcurrentModification_HandlesOptimisticLocking() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(any(Employee.class)))
            .thenThrow(new org.springframework.orm.ObjectOptimisticLockingFailureException("Version mismatch", null));

        // Act & Assert
        assertThrows(org.springframework.orm.ObjectOptimisticLockingFailureException.class, () -> {
            employeeService.updateEmployee(1L, employeeDTO);
        });
    }

    // ==================== INTEGRATION TESTS ====================

    @Test
    @DisplayName("Test complete employee lifecycle - create, read, update, delete")
    void testEmployeeLifecycle_CompleteFlow_Succeeds() {
        // Arrange
        when(employeeRepository.existsByBadgeId(anyString())).thenReturn(false);
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        // Act - Create
        EmployeeDTO created = employeeService.createEmployee(employeeDTO);
        assertNotNull(created);

        // Act - Read
        EmployeeDTO retrieved = employeeService.getEmployeeById(1L);
        assertNotNull(retrieved);

        // Act - Update
        EmployeeDTO updated = employeeService.updateEmployee(1L, employeeDTO);
        assertNotNull(updated);

        // Act - Delete
        employeeService.deleteEmployee(1L);

        // Assert
        verify(employeeRepository, times(3)).save(any(Employee.class));
    }
}