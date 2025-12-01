package com.warehouse.employee.service;

import com.warehouse.employee.domain.Employee;
import com.warehouse.employee.repository.EmployeeRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;

/**
 * Comprehensive unit tests for EmployeeService.
 * Tests cover all business logic, validation, and error handling.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Employee Service Tests")
public class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeService employeeService;

    private Employee testEmployee;
    private LocalDate testHireDate;

    @BeforeEach
    public void setUp() {
        testHireDate = LocalDate.of(2023, 1, 15);
        testEmployee = Employee.builder()
                .id(1L)
                .name("John Doe")
                .badgeId("BADGE001")
                .role("WORKER")
                .department("Warehouse")
                .shiftGroup("Morning")
                .hireDate(testHireDate)
                .status("ACTIVE")
                .deleted(false)
                .build();
    }

    // ========== NORMAL CASES - GET ALL EMPLOYEES ==========

    @Test
    @DisplayName("Test getAllEmployees with valid pageable")
    public void testGetAllEmployees_WithValidPageable_ReturnsPage() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> expectedPage = new PageImpl<>(Arrays.asList(testEmployee));
        when(employeeRepository.findAll(pageable)).thenReturn(expectedPage);

        // Act
        Page<Employee> result = employeeService.getAllEmployees(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testEmployee.getId(), result.getContent().get(0).getId());
        verify(employeeRepository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("Test getAllEmployees with empty result")
    public void testGetAllEmployees_WithEmptyResult_ReturnsEmptyPage() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> emptyPage = new PageImpl<>(Arrays.asList());
        when(employeeRepository.findAll(pageable)).thenReturn(emptyPage);

        // Act
        Page<Employee> result = employeeService.getAllEmployees(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        verify(employeeRepository, times(1)).findAll(pageable);
    }

    // ========== NORMAL CASES - GET EMPLOYEE ==========

    @Test
    @DisplayName("Test getEmployee with existing ID")
    public void testGetEmployee_WithExistingId_ReturnsEmployee() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));

        // Act
        Employee result = employeeService.getEmployee(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("John Doe", result.getName());
        verify(employeeRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Test getEmployee with non-existing ID throws exception")
    public void testGetEmployee_WithNonExistingId_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            employeeService.getEmployee(999L);
        });
        
        assertTrue(exception.getMessage().contains("Employee not found with ID: 999"));
        verify(employeeRepository, times(1)).findById(999L);
    }

    // ========== NORMAL CASES - CREATE EMPLOYEE ==========

    @Test
    @DisplayName("Test createEmployee with valid data")
    public void testCreateEmployee_WithValidData_Success() {
        // Arrange
        when(employeeRepository.findByBadgeId("BADGE001")).thenReturn(Optional.empty());
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        Employee result = employeeService.createEmployee(testEmployee);

        // Assert
        assertNotNull(result);
        assertEquals("BADGE001", result.getBadgeId());
        verify(employeeRepository, times(1)).findByBadgeId("BADGE001");
        verify(employeeRepository, times(1)).save(testEmployee);
    }

    @Test
    @DisplayName("Test createEmployee with duplicate badgeId throws exception")
    public void testCreateEmployee_WithDuplicateBadgeId_ThrowsException() {
        // Arrange
        when(employeeRepository.findByBadgeId("BADGE001")).thenReturn(Optional.of(testEmployee));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            employeeService.createEmployee(testEmployee);
        });
        
        assertTrue(exception.getMessage().contains("Badge ID already exists: BADGE001"));
        verify(employeeRepository, times(1)).findByBadgeId("BADGE001");
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    // ========== NORMAL CASES - UPDATE EMPLOYEE ==========

    @Test
    @DisplayName("Test updateEmployee with valid data")
    public void testUpdateEmployee_WithValidData_Success() {
        // Arrange
        Employee updatedData = Employee.builder()
                .name("Updated Name")
                .badgeId("BADGE001")
                .role("SUPERVISOR")
                .department("Logistics")
                .shiftGroup("Evening")
                .hireDate(testHireDate)
                .status("ACTIVE")
                .deleted(false)
                .build();

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        Employee result = employeeService.updateEmployee(1L, updatedData);

        // Assert
        assertNotNull(result);
        verify(employeeRepository, times(1)).findById(1L);
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    @DisplayName("Test updateEmployee with non-existing ID throws exception")
    public void testUpdateEmployee_WithNonExistingId_ThrowsException() {
        // Arrange
        Employee updatedData = Employee.builder().name("Updated Name").build();
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> {
            employeeService.updateEmployee(999L, updatedData);
        });
        
        verify(employeeRepository, times(1)).findById(999L);
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    // ========== NORMAL CASES - DELETE EMPLOYEE ==========

    @Test
    @DisplayName("Test deleteEmployee soft deletes employee")
    public void testDeleteEmployee_WithExistingId_SoftDeletes() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        employeeService.deleteEmployee(1L);

        // Assert
        verify(employeeRepository, times(1)).findById(1L);
        verify(employeeRepository, times(1)).save(argThat(emp -> emp.isDeleted()));
    }

    @Test
    @DisplayName("Test deleteEmployee with non-existing ID throws exception")
    public void testDeleteEmployee_WithNonExistingId_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> {
            employeeService.deleteEmployee(999L);
        });
        
        verify(employeeRepository, times(1)).findById(999L);
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    // ========== NORMAL CASES - FIND BY BADGE ID ==========

    @Test
    @DisplayName("Test findByBadgeId with existing badge")
    public void testFindByBadgeId_WithExistingBadge_ReturnsEmployee() {
        // Arrange
        when(employeeRepository.findByBadgeId("BADGE001")).thenReturn(Optional.of(testEmployee));

        // Act
        Optional<Employee> result = employeeService.findByBadgeId("BADGE001");

        // Assert
        assertTrue(result.isPresent());
        assertEquals("BADGE001", result.get().getBadgeId());
        verify(employeeRepository, times(1)).findByBadgeId("BADGE001");
    }

    @Test
    @DisplayName("Test findByBadgeId with non-existing badge")
    public void testFindByBadgeId_WithNonExistingBadge_ReturnsEmpty() {
        // Arrange
        when(employeeRepository.findByBadgeId("NONEXISTENT")).thenReturn(Optional.empty());

        // Act
        Optional<Employee> result = employeeService.findByBadgeId("NONEXISTENT");

        // Assert
        assertFalse(result.isPresent());
        verify(employeeRepository, times(1)).findByBadgeId("NONEXISTENT");
    }

    // ========== EDGE CASES ==========

    @Test
    @DisplayName("Test getEmployee with null ID throws exception")
    public void testGetEmployee_WithNullId_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(null)).thenThrow(new IllegalArgumentException("ID cannot be null"));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.getEmployee(null);
        });
    }

    @Test
    @DisplayName("Test createEmployee with null employee throws exception")
    public void testCreateEmployee_WithNullEmployee_ThrowsException() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            employeeService.createEmployee(null);
        });
    }

    @Test
    @DisplayName("Test createEmployee with null badgeId")
    public void testCreateEmployee_WithNullBadgeId_CallsRepository() {
        // Arrange
        testEmployee.setBadgeId(null);
        when(employeeRepository.findByBadgeId(null)).thenReturn(Optional.empty());
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        Employee result = employeeService.createEmployee(testEmployee);

        // Assert
        assertNotNull(result);
        verify(employeeRepository, times(1)).findByBadgeId(null);
    }

    @Test
    @DisplayName("Test createEmployee with empty badgeId")
    public void testCreateEmployee_WithEmptyBadgeId_Success() {
        // Arrange
        testEmployee.setBadgeId("");
        when(employeeRepository.findByBadgeId("")).thenReturn(Optional.empty());
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        Employee result = employeeService.createEmployee(testEmployee);

        // Assert
        assertNotNull(result);
        verify(employeeRepository, times(1)).findByBadgeId("");
    }

    @Test
    @DisplayName("Test updateEmployee with null updated data throws exception")
    public void testUpdateEmployee_WithNullUpdatedData_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));

        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            employeeService.updateEmployee(1L, null);
        });
    }

    @Test
    @DisplayName("Test findByBadgeId with null badgeId")
    public void testFindByBadgeId_WithNull_ReturnsEmpty() {
        // Arrange
        when(employeeRepository.findByBadgeId(null)).thenReturn(Optional.empty());

        // Act
        Optional<Employee> result = employeeService.findByBadgeId(null);

        // Assert
        assertFalse(result.isPresent());
        verify(employeeRepository, times(1)).findByBadgeId(null);
    }

    @Test
    @DisplayName("Test findByBadgeId with empty string")
    public void testFindByBadgeId_WithEmptyString_ReturnsEmpty() {
        // Arrange
        when(employeeRepository.findByBadgeId("")).thenReturn(Optional.empty());

        // Act
        Optional<Employee> result = employeeService.findByBadgeId("");

        // Assert
        assertFalse(result.isPresent());
        verify(employeeRepository, times(1)).findByBadgeId("");
    }

    // ========== BOUNDARY CONDITIONS ==========

    @Test
    @DisplayName("Test getAllEmployees with page size 1")
    public void testGetAllEmployees_WithPageSize1_ReturnsOnePage() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 1);
        Page<Employee> expectedPage = new PageImpl<>(Arrays.asList(testEmployee));
        when(employeeRepository.findAll(pageable)).thenReturn(expectedPage);

        // Act
        Page<Employee> result = employeeService.getAllEmployees(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getSize());
        verify(employeeRepository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("Test getAllEmployees with large page size")
    public void testGetAllEmployees_WithLargePageSize_ReturnsPage() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 1000);
        Page<Employee> expectedPage = new PageImpl<>(Arrays.asList(testEmployee));
        when(employeeRepository.findAll(pageable)).thenReturn(expectedPage);

        // Act
        Page<Employee> result = employeeService.getAllEmployees(pageable);

        // Assert
        assertNotNull(result);
        verify(employeeRepository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("Test getEmployee with zero ID")
    public void testGetEmployee_WithZeroId_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(0L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> {
            employeeService.getEmployee(0L);
        });
    }

    @Test
    @DisplayName("Test getEmployee with negative ID")
    public void testGetEmployee_WithNegativeId_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(-1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> {
            employeeService.getEmployee(-1L);
        });
    }

    @Test
    @DisplayName("Test getEmployee with maximum Long value")
    public void testGetEmployee_WithMaxLongValue_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(Long.MAX_VALUE)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> {
            employeeService.getEmployee(Long.MAX_VALUE);
        });
    }

    @Test
    @DisplayName("Test createEmployee with very long name")
    public void testCreateEmployee_WithVeryLongName_Success() {
        // Arrange
        String longName = "A".repeat(500);
        testEmployee.setName(longName);
        when(employeeRepository.findByBadgeId("BADGE001")).thenReturn(Optional.empty());
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        Employee result = employeeService.createEmployee(testEmployee);

        // Assert
        assertNotNull(result);
        assertEquals(longName, result.getName());
    }

    @Test
    @DisplayName("Test updateEmployee preserves ID")
    public void testUpdateEmployee_PreservesId_Success() {
        // Arrange
        Employee updatedData = Employee.builder()
                .id(999L) // Different ID
                .name("Updated Name")
                .badgeId("BADGE001")
                .role("SUPERVISOR")
                .department("Logistics")
                .shiftGroup("Evening")
                .hireDate(testHireDate)
                .status("ACTIVE")
                .deleted(false)
                .build();

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        Employee result = employeeService.updateEmployee(1L, updatedData);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId()); // Original ID preserved
        verify(employeeRepository, times(1)).save(argThat(emp -> emp.getId().equals(1L)));
    }

    @Test
    @DisplayName("Test deleteEmployee sets deleted flag to true")
    public void testDeleteEmployee_SetsDeletedFlag_Success() {
        // Arrange
        assertFalse(testEmployee.isDeleted());
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> {
            Employee emp = invocation.getArgument(0);
            assertTrue(emp.isDeleted());
            return emp;
        });

        // Act
        employeeService.deleteEmployee(1L);

        // Assert
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }
}
