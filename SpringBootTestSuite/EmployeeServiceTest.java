package com.warehouse.service;

import com.warehouse.entity.Employee;
import com.warehouse.entity.Warehouse;
import com.warehouse.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Comprehensive JUnit test suite for EmployeeService.
 * Tests cover CRUD operations, business logic validation, and exception handling.
 * 
 * @author Warehouse EMS Test Team
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("EmployeeService Tests")
public class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeService employeeService;

    private Employee testEmployee;
    private Warehouse testWarehouse;

    @BeforeEach
    public void setUp() {
        testWarehouse = Warehouse.builder()
                .id(1L)
                .name("Main Warehouse")
                .timezone("America/New_York")
                .build();

        testEmployee = Employee.builder()
                .id(1L)
                .name("John Doe")
                .badgeId("EMP001")
                .role("WORKER")
                .department("Shipping")
                .shiftGroup("Day Shift")
                .hireDate(LocalDate.of(2023, 1, 15))
                .status("ACTIVE")
                .deleted(false)
                .warehouse(testWarehouse)
                .build();
    }

    // ========== CREATE OPERATION TESTS ==========

    @Test
    @DisplayName("Test create employee with valid data")
    public void testCreateEmployeeWithValidData() {
        // Arrange
        when(employeeRepository.findByBadgeId(anyString())).thenReturn(Optional.empty());
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        Employee createdEmployee = employeeService.createEmployee(testEmployee);

        // Assert
        assertNotNull(createdEmployee);
        assertEquals("John Doe", createdEmployee.getName());
        assertEquals("EMP001", createdEmployee.getBadgeId());
        verify(employeeRepository, times(1)).findByBadgeId("EMP001");
        verify(employeeRepository, times(1)).save(testEmployee);
    }

    @Test
    @DisplayName("Test create employee with duplicate badge ID throws exception")
    public void testCreateEmployeeWithDuplicateBadgeId() {
        // Arrange
        when(employeeRepository.findByBadgeId("EMP001")).thenReturn(Optional.of(testEmployee));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.createEmployee(testEmployee);
        });
        verify(employeeRepository, times(1)).findByBadgeId("EMP001");
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    @DisplayName("Test create employee with null name throws exception")
    public void testCreateEmployeeWithNullName() {
        // Arrange
        testEmployee.setName(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.createEmployee(testEmployee);
        });
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    @DisplayName("Test create employee with empty name throws exception")
    public void testCreateEmployeeWithEmptyName() {
        // Arrange
        testEmployee.setName("");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.createEmployee(testEmployee);
        });
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    @DisplayName("Test create employee with null badge ID throws exception")
    public void testCreateEmployeeWithNullBadgeId() {
        // Arrange
        testEmployee.setBadgeId(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.createEmployee(testEmployee);
        });
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    @DisplayName("Test create employee with null role throws exception")
    public void testCreateEmployeeWithNullRole() {
        // Arrange
        testEmployee.setRole(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.createEmployee(testEmployee);
        });
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    // ========== READ OPERATION TESTS ==========

    @Test
    @DisplayName("Test find employee by ID with valid ID")
    public void testFindEmployeeByIdWithValidId() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));

        // Act
        Optional<Employee> foundEmployee = employeeService.findEmployeeById(1L);

        // Assert
        assertTrue(foundEmployee.isPresent());
        assertEquals("John Doe", foundEmployee.get().getName());
        assertEquals("EMP001", foundEmployee.get().getBadgeId());
        verify(employeeRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Test find employee by ID with non-existent ID")
    public void testFindEmployeeByIdWithNonExistentId() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Optional<Employee> foundEmployee = employeeService.findEmployeeById(999L);

        // Assert
        assertFalse(foundEmployee.isPresent());
        verify(employeeRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("Test find employee by ID with null ID throws exception")
    public void testFindEmployeeByIdWithNullId() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.findEmployeeById(null);
        });
        verify(employeeRepository, never()).findById(any());
    }

    @Test
    @DisplayName("Test find employee by badge ID with valid badge ID")
    public void testFindEmployeeByBadgeIdWithValidBadgeId() {
        // Arrange
        when(employeeRepository.findByBadgeId("EMP001")).thenReturn(Optional.of(testEmployee));

        // Act
        Optional<Employee> foundEmployee = employeeService.findEmployeeByBadgeId("EMP001");

        // Assert
        assertTrue(foundEmployee.isPresent());
        assertEquals("John Doe", foundEmployee.get().getName());
        verify(employeeRepository, times(1)).findByBadgeId("EMP001");
    }

    @Test
    @DisplayName("Test find employee by badge ID with non-existent badge ID")
    public void testFindEmployeeByBadgeIdWithNonExistentBadgeId() {
        // Arrange
        when(employeeRepository.findByBadgeId("INVALID")).thenReturn(Optional.empty());

        // Act
        Optional<Employee> foundEmployee = employeeService.findEmployeeByBadgeId("INVALID");

        // Assert
        assertFalse(foundEmployee.isPresent());
        verify(employeeRepository, times(1)).findByBadgeId("INVALID");
    }

    @Test
    @DisplayName("Test list all employees with pagination")
    public void testListAllEmployeesWithPagination() {
        // Arrange
        Employee employee2 = Employee.builder()
                .id(2L)
                .name("Jane Smith")
                .badgeId("EMP002")
                .role("SUPERVISOR")
                .build();

        List<Employee> employees = Arrays.asList(testEmployee, employee2);
        Page<Employee> employeePage = new PageImpl<>(employees);
        Pageable pageable = PageRequest.of(0, 10);

        when(employeeRepository.findAllByDeletedFalse(pageable)).thenReturn(employeePage);

        // Act
        Page<Employee> result = employeeService.listAllEmployees(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals("John Doe", result.getContent().get(0).getName());
        assertEquals("Jane Smith", result.getContent().get(1).getName());
        verify(employeeRepository, times(1)).findAllByDeletedFalse(pageable);
    }

    @Test
    @DisplayName("Test list all employees returns empty page when no employees")
    public void testListAllEmployeesReturnsEmptyPage() {
        // Arrange
        Page<Employee> emptyPage = new PageImpl<>(Arrays.asList());
        Pageable pageable = PageRequest.of(0, 10);

        when(employeeRepository.findAllByDeletedFalse(pageable)).thenReturn(emptyPage);

        // Act
        Page<Employee> result = employeeService.listAllEmployees(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getContent().size());
        verify(employeeRepository, times(1)).findAllByDeletedFalse(pageable);
    }

    // ========== UPDATE OPERATION TESTS ==========

    @Test
    @DisplayName("Test update employee with valid data")
    public void testUpdateEmployeeWithValidData() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        testEmployee.setName("John Updated");
        testEmployee.setDepartment("Receiving");

        // Act
        Employee updatedEmployee = employeeService.updateEmployee(1L, testEmployee);

        // Assert
        assertNotNull(updatedEmployee);
        assertEquals("John Updated", updatedEmployee.getName());
        assertEquals("Receiving", updatedEmployee.getDepartment());
        verify(employeeRepository, times(1)).findById(1L);
        verify(employeeRepository, times(1)).save(testEmployee);
    }

    @Test
    @DisplayName("Test update employee with non-existent ID throws exception")
    public void testUpdateEmployeeWithNonExistentId() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.updateEmployee(999L, testEmployee);
        });
        verify(employeeRepository, times(1)).findById(999L);
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    @DisplayName("Test update employee with null ID throws exception")
    public void testUpdateEmployeeWithNullId() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.updateEmployee(null, testEmployee);
        });
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    @DisplayName("Test partial update employee")
    public void testPartialUpdateEmployee() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act: Update only department
        testEmployee.setDepartment("Quality Control");
        Employee updatedEmployee = employeeService.updateEmployee(1L, testEmployee);

        // Assert
        assertEquals("Quality Control", updatedEmployee.getDepartment());
        assertEquals("John Doe", updatedEmployee.getName()); // Name unchanged
        verify(employeeRepository, times(1)).save(testEmployee);
    }

    // ========== DELETE OPERATION TESTS ==========

    @Test
    @DisplayName("Test soft delete employee with valid ID")
    public void testSoftDeleteEmployeeWithValidId() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        employeeService.softDeleteEmployee(1L);

        // Assert
        assertTrue(testEmployee.getDeleted());
        assertEquals("INACTIVE", testEmployee.getStatus());
        verify(employeeRepository, times(1)).findById(1L);
        verify(employeeRepository, times(1)).save(testEmployee);
    }

    @Test
    @DisplayName("Test soft delete employee with non-existent ID throws exception")
    public void testSoftDeleteEmployeeWithNonExistentId() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.softDeleteEmployee(999L);
        });
        verify(employeeRepository, times(1)).findById(999L);
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    @DisplayName("Test soft delete employee with null ID throws exception")
    public void testSoftDeleteEmployeeWithNullId() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.softDeleteEmployee(null);
        });
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    @DisplayName("Test soft delete already deleted employee is idempotent")
    public void testSoftDeleteAlreadyDeletedEmployee() {
        // Arrange
        testEmployee.softDelete();
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        employeeService.softDeleteEmployee(1L);

        // Assert
        assertTrue(testEmployee.getDeleted());
        assertEquals("INACTIVE", testEmployee.getStatus());
        verify(employeeRepository, times(1)).save(testEmployee);
    }

    // ========== BUSINESS LOGIC TESTS ==========

    @Test
    @DisplayName("Test validate badge ID uniqueness")
    public void testValidateBadgeIdUniqueness() {
        // Arrange
        when(employeeRepository.findByBadgeId("EMP001")).thenReturn(Optional.of(testEmployee));

        // Act
        boolean isUnique = employeeService.isBadgeIdUnique("EMP001");

        // Assert
        assertFalse(isUnique);
        verify(employeeRepository, times(1)).findByBadgeId("EMP001");
    }

    @Test
    @DisplayName("Test validate badge ID uniqueness with new badge ID")
    public void testValidateBadgeIdUniquenessWithNewBadgeId() {
        // Arrange
        when(employeeRepository.findByBadgeId("EMP999")).thenReturn(Optional.empty());

        // Act
        boolean isUnique = employeeService.isBadgeIdUnique("EMP999");

        // Assert
        assertTrue(isUnique);
        verify(employeeRepository, times(1)).findByBadgeId("EMP999");
    }

    @Test
    @DisplayName("Test find active employees only")
    public void testFindActiveEmployeesOnly() {
        // Arrange
        Employee activeEmployee = Employee.builder()
                .id(1L)
                .name("Active Employee")
                .badgeId("EMP001")
                .role("WORKER")
                .status("ACTIVE")
                .deleted(false)
                .build();

        List<Employee> activeEmployees = Arrays.asList(activeEmployee);
        when(employeeRepository.findByStatusAndDeletedFalse("ACTIVE")).thenReturn(activeEmployees);

        // Act
        List<Employee> result = employeeService.findActiveEmployees();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).isActive());
        verify(employeeRepository, times(1)).findByStatusAndDeletedFalse("ACTIVE");
    }

    @Test
    @DisplayName("Test find employees by department")
    public void testFindEmployeesByDepartment() {
        // Arrange
        List<Employee> shippingEmployees = Arrays.asList(testEmployee);
        when(employeeRepository.findByDepartmentAndDeletedFalse("Shipping")).thenReturn(shippingEmployees);

        // Act
        List<Employee> result = employeeService.findEmployeesByDepartment("Shipping");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Shipping", result.get(0).getDepartment());
        verify(employeeRepository, times(1)).findByDepartmentAndDeletedFalse("Shipping");
    }

    @Test
    @DisplayName("Test find employees by role")
    public void testFindEmployeesByRole() {
        // Arrange
        List<Employee> workers = Arrays.asList(testEmployee);
        when(employeeRepository.findByRoleAndDeletedFalse("WORKER")).thenReturn(workers);

        // Act
        List<Employee> result = employeeService.findEmployeesByRole("WORKER");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("WORKER", result.get(0).getRole());
        verify(employeeRepository, times(1)).findByRoleAndDeletedFalse("WORKER");
    }

    @Test
    @DisplayName("Test count active employees")
    public void testCountActiveEmployees() {
        // Arrange
        when(employeeRepository.countByStatusAndDeletedFalse("ACTIVE")).thenReturn(10L);

        // Act
        long count = employeeService.countActiveEmployees();

        // Assert
        assertEquals(10L, count);
        verify(employeeRepository, times(1)).countByStatusAndDeletedFalse("ACTIVE");
    }

    @Test
    @DisplayName("Test employee status change validation")
    public void testEmployeeStatusChangeValidation() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act: Change status from ACTIVE to ON_LEAVE
        testEmployee.setStatus("ON_LEAVE");
        Employee updatedEmployee = employeeService.updateEmployee(1L, testEmployee);

        // Assert
        assertEquals("ON_LEAVE", updatedEmployee.getStatus());
        verify(employeeRepository, times(1)).save(testEmployee);
    }

    @Test
    @DisplayName("Test employee with future hire date validation")
    public void testEmployeeWithFutureHireDateValidation() {
        // Arrange
        LocalDate futureDate = LocalDate.now().plusDays(30);
        testEmployee.setHireDate(futureDate);
        when(employeeRepository.findByBadgeId(anyString())).thenReturn(Optional.empty());
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        Employee createdEmployee = employeeService.createEmployee(testEmployee);

        // Assert
        assertNotNull(createdEmployee);
        assertEquals(futureDate, createdEmployee.getHireDate());
    }
}
