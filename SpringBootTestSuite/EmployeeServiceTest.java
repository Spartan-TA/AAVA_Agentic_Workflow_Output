package com.warehouse.ems.employee.service;

import com.warehouse.ems.employee.entity.Employee;
import com.warehouse.ems.employee.entity.Role;
import com.warehouse.ems.employee.repository.EmployeeRepository;
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
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for EmployeeService covering:
 * - Normal CRUD operations
 * - Unique badgeId validation
 * - Soft delete functionality
 * - Pagination and filtering
 * - Edge cases and boundary conditions
 * - Exception handling
 */
@ExtendWith(MockitoExtension.class)
public class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeService employeeService;

    private Employee testEmployee;
    private Role testRole;

    @BeforeEach
    public void setUp() {
        // Arrange: Create test data
        testRole = Role.builder()
                .id(1L)
                .name("WORKER")
                .build();

        testEmployee = Employee.builder()
                .id(1L)
                .badgeId("EMP001")
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@warehouse.com")
                .department("Shipping")
                .shiftGroup("Morning")
                .hireDate(LocalDate.of(2023, 1, 15))
                .status("ACTIVE")
                .role(testRole)
                .deleted(false)
                .build();
    }

    // ========== NORMAL CASE TESTS ==========

    @Test
    public void testCreateEmployee_WithValidData_ReturnsCreatedEmployee() {
        // Arrange
        when(employeeRepository.findByBadgeId(anyString())).thenReturn(Optional.empty());
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        Employee result = employeeService.create(testEmployee);

        // Assert
        assertNotNull(result, "Created employee should not be null");
        assertEquals("EMP001", result.getBadgeId(), "BadgeId should match");
        assertEquals("John", result.getFirstName(), "First name should match");
        assertEquals("Doe", result.getLastName(), "Last name should match");
        verify(employeeRepository, times(1)).findByBadgeId("EMP001");
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    public void testFindById_WithValidId_ReturnsEmployee() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));

        // Act
        Optional<Employee> result = employeeService.findById(1L);

        // Assert
        assertTrue(result.isPresent(), "Employee should be found");
        assertEquals(1L, result.get().getId(), "Employee ID should match");
        assertEquals("EMP001", result.get().getBadgeId(), "BadgeId should match");
        verify(employeeRepository, times(1)).findById(1L);
    }

    @Test
    public void testUpdateEmployee_WithValidData_ReturnsUpdatedEmployee() {
        // Arrange
        Employee updatedEmployee = Employee.builder()
                .id(1L)
                .badgeId("EMP001")
                .firstName("John")
                .lastName("Smith")
                .email("john.smith@warehouse.com")
                .department("Receiving")
                .shiftGroup("Evening")
                .hireDate(LocalDate.of(2023, 1, 15))
                .status("ACTIVE")
                .role(testRole)
                .deleted(false)
                .build();

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(updatedEmployee);

        // Act
        Employee result = employeeService.update(1L, updatedEmployee);

        // Assert
        assertNotNull(result, "Updated employee should not be null");
        assertEquals("Smith", result.getLastName(), "Last name should be updated");
        assertEquals("Receiving", result.getDepartment(), "Department should be updated");
        verify(employeeRepository, times(1)).findById(1L);
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    public void testSoftDelete_WithValidId_MarksEmployeeAsDeleted() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> {
            Employee emp = invocation.getArgument(0);
            emp.setDeleted(true);
            return emp;
        });

        // Act
        employeeService.softDelete(1L);

        // Assert
        verify(employeeRepository, times(1)).findById(1L);
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    public void testFindAll_WithPagination_ReturnsPagedEmployees() {
        // Arrange
        Employee employee2 = Employee.builder()
                .id(2L)
                .badgeId("EMP002")
                .firstName("Jane")
                .lastName("Smith")
                .email("jane.smith@warehouse.com")
                .department("Shipping")
                .status("ACTIVE")
                .deleted(false)
                .build();

        Page<Employee> employeePage = new PageImpl<>(Arrays.asList(testEmployee, employee2));
        Pageable pageable = PageRequest.of(0, 10);
        when(employeeRepository.findAllByDeletedFalse(pageable)).thenReturn(employeePage);

        // Act
        Page<Employee> result = employeeService.findAll(pageable);

        // Assert
        assertNotNull(result, "Result page should not be null");
        assertEquals(2, result.getContent().size(), "Should return 2 employees");
        assertEquals("EMP001", result.getContent().get(0).getBadgeId());
        assertEquals("EMP002", result.getContent().get(1).getBadgeId());
        verify(employeeRepository, times(1)).findAllByDeletedFalse(pageable);
    }

    // ========== EDGE CASE TESTS ==========

    @Test
    public void testCreateEmployee_WithDuplicateBadgeId_ThrowsException() {
        // Arrange
        when(employeeRepository.findByBadgeId("EMP001")).thenReturn(Optional.of(testEmployee));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.create(testEmployee);
        }, "Should throw IllegalArgumentException for duplicate badgeId");

        verify(employeeRepository, times(1)).findByBadgeId("EMP001");
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    public void testFindById_WithInvalidId_ReturnsEmpty() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Optional<Employee> result = employeeService.findById(999L);

        // Assert
        assertFalse(result.isPresent(), "Should return empty for invalid ID");
        verify(employeeRepository, times(1)).findById(999L);
    }

    @Test
    public void testCreateEmployee_WithNullBadgeId_ThrowsException() {
        // Arrange
        Employee invalidEmployee = Employee.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@warehouse.com")
                .build();

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.create(invalidEmployee);
        }, "Should throw exception for null badgeId");
    }

    @Test
    public void testCreateEmployee_WithEmptyBadgeId_ThrowsException() {
        // Arrange
        Employee invalidEmployee = Employee.builder()
                .badgeId("")
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@warehouse.com")
                .build();

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.create(invalidEmployee);
        }, "Should throw exception for empty badgeId");
    }

    @Test
    public void testCreateEmployee_WithNullFirstName_ThrowsException() {
        // Arrange
        Employee invalidEmployee = Employee.builder()
                .badgeId("EMP001")
                .lastName("Doe")
                .email("john.doe@warehouse.com")
                .build();

        when(employeeRepository.findByBadgeId("EMP001")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.create(invalidEmployee);
        }, "Should throw exception for null firstName");
    }

    @Test
    public void testCreateEmployee_WithNullLastName_ThrowsException() {
        // Arrange
        Employee invalidEmployee = Employee.builder()
                .badgeId("EMP001")
                .firstName("John")
                .email("john.doe@warehouse.com")
                .build();

        when(employeeRepository.findByBadgeId("EMP001")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.create(invalidEmployee);
        }, "Should throw exception for null lastName");
    }

    @Test
    public void testCreateEmployee_WithInvalidEmailFormat_ThrowsException() {
        // Arrange
        Employee invalidEmployee = Employee.builder()
                .badgeId("EMP001")
                .firstName("John")
                .lastName("Doe")
                .email("invalid-email")
                .build();

        when(employeeRepository.findByBadgeId("EMP001")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.create(invalidEmployee);
        }, "Should throw exception for invalid email format");
    }

    @Test
    public void testUpdateEmployee_WithNonExistentId_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.update(999L, testEmployee);
        }, "Should throw exception for non-existent employee ID");

        verify(employeeRepository, times(1)).findById(999L);
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    public void testSoftDelete_WithNonExistentId_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.softDelete(999L);
        }, "Should throw exception for non-existent employee ID");

        verify(employeeRepository, times(1)).findById(999L);
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    public void testSoftDelete_AlreadyDeletedEmployee_DoesNothing() {
        // Arrange
        testEmployee.setDeleted(true);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));

        // Act
        employeeService.softDelete(1L);

        // Assert
        verify(employeeRepository, times(1)).findById(1L);
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    // ========== BOUNDARY CONDITION TESTS ==========

    @Test
    public void testCreateEmployee_WithMaxLengthBadgeId_Success() {
        // Arrange
        String maxLengthBadgeId = "A".repeat(32); // Assuming max length is 32
        Employee employee = Employee.builder()
                .badgeId(maxLengthBadgeId)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@warehouse.com")
                .build();

        when(employeeRepository.findByBadgeId(maxLengthBadgeId)).thenReturn(Optional.empty());
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

        // Act
        Employee result = employeeService.create(employee);

        // Assert
        assertNotNull(result);
        assertEquals(maxLengthBadgeId, result.getBadgeId());
    }

    @Test
    public void testCreateEmployee_WithFutureHireDate_ThrowsException() {
        // Arrange
        Employee employee = Employee.builder()
                .badgeId("EMP001")
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@warehouse.com")
                .hireDate(LocalDate.now().plusDays(1))
                .build();

        when(employeeRepository.findByBadgeId("EMP001")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.create(employee);
        }, "Should throw exception for future hire date");
    }

    @Test
    public void testCreateEmployee_WithTodayHireDate_Success() {
        // Arrange
        Employee employee = Employee.builder()
                .badgeId("EMP001")
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@warehouse.com")
                .hireDate(LocalDate.now())
                .build();

        when(employeeRepository.findByBadgeId("EMP001")).thenReturn(Optional.empty());
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

        // Act
        Employee result = employeeService.create(employee);

        // Assert
        assertNotNull(result);
        assertEquals(LocalDate.now(), result.getHireDate());
    }

    @Test
    public void testFindAll_WithEmptyDatabase_ReturnsEmptyPage() {
        // Arrange
        Page<Employee> emptyPage = new PageImpl<>(Arrays.asList());
        Pageable pageable = PageRequest.of(0, 10);
        when(employeeRepository.findAllByDeletedFalse(pageable)).thenReturn(emptyPage);

        // Act
        Page<Employee> result = employeeService.findAll(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getContent().size());
        verify(employeeRepository, times(1)).findAllByDeletedFalse(pageable);
    }

    @Test
    public void testFindById_WithNegativeId_ReturnsEmpty() {
        // Arrange
        when(employeeRepository.findById(-1L)).thenReturn(Optional.empty());

        // Act
        Optional<Employee> result = employeeService.findById(-1L);

        // Assert
        assertFalse(result.isPresent());
        verify(employeeRepository, times(1)).findById(-1L);
    }

    @Test
    public void testFindById_WithZeroId_ReturnsEmpty() {
        // Arrange
        when(employeeRepository.findById(0L)).thenReturn(Optional.empty());

        // Act
        Optional<Employee> result = employeeService.findById(0L);

        // Assert
        assertFalse(result.isPresent());
        verify(employeeRepository, times(1)).findById(0L);
    }
}