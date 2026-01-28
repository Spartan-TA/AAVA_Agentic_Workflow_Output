package com.wms.ems.employee;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.dao.DataIntegrityViolationException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive repository tests for Employee entity
 * Tests cover:
 * - CRUD operations (Create, Read, Update, Delete)
 * - Pagination and sorting
 * - Filtering by various criteria
 * - Unique constraint enforcement
 * - Soft delete queries
 * - Custom query methods
 */
@DataJpaTest
@DisplayName("Employee Repository Tests")
public class EmployeeRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private EmployeeRepository employeeRepository;

    private Employee testEmployee;

    @BeforeEach
    public void setUp() {
        testEmployee = new Employee();
        testEmployee.setName("John Doe");
        testEmployee.setBadgeId("EMP001");
        testEmployee.setRole("WORKER");
        testEmployee.setDepartment("Warehouse");
        testEmployee.setShiftGroup("Morning");
        testEmployee.setHireDate(LocalDate.now());
        testEmployee.setStatus("ACTIVE");
        testEmployee.setDeleted(false);
    }

    // ========== CREATE OPERATIONS ==========

    @Test
    @DisplayName("Should save employee successfully")
    public void testSaveEmployee() {
        // Act
        Employee savedEmployee = employeeRepository.save(testEmployee);
        
        // Assert
        assertNotNull(savedEmployee.getId());
        assertEquals("John Doe", savedEmployee.getName());
        assertEquals("EMP001", savedEmployee.getBadgeId());
    }

    @Test
    @DisplayName("Should enforce unique badge ID constraint")
    public void testUniqueBadgeIdConstraint() {
        // Arrange
        employeeRepository.save(testEmployee);
        
        Employee duplicateEmployee = new Employee();
        duplicateEmployee.setName("Jane Smith");
        duplicateEmployee.setBadgeId("EMP001"); // Duplicate
        duplicateEmployee.setRole("WORKER");
        duplicateEmployee.setDepartment("Warehouse");
        duplicateEmployee.setHireDate(LocalDate.now());
        duplicateEmployee.setStatus("ACTIVE");
        
        // Act & Assert
        assertThrows(DataIntegrityViolationException.class, () -> {
            employeeRepository.save(duplicateEmployee);
            entityManager.flush();
        });
    }

    @Test
    @DisplayName("Should save multiple employees with different badge IDs")
    public void testSaveMultipleEmployees() {
        // Arrange
        Employee employee2 = new Employee();
        employee2.setName("Jane Smith");
        employee2.setBadgeId("EMP002");
        employee2.setRole("SUPERVISOR");
        employee2.setDepartment("Logistics");
        employee2.setHireDate(LocalDate.now());
        employee2.setStatus("ACTIVE");
        
        // Act
        employeeRepository.save(testEmployee);
        employeeRepository.save(employee2);
        
        // Assert
        assertEquals(2, employeeRepository.count());
    }

    // ========== READ OPERATIONS ==========

    @Test
    @DisplayName("Should find employee by ID")
    public void testFindById() {
        // Arrange
        Employee savedEmployee = employeeRepository.save(testEmployee);
        
        // Act
        Optional<Employee> foundEmployee = employeeRepository.findById(savedEmployee.getId());
        
        // Assert
        assertTrue(foundEmployee.isPresent());
        assertEquals("John Doe", foundEmployee.get().getName());
    }

    @Test
    @DisplayName("Should return empty when employee not found by ID")
    public void testFindByIdNotFound() {
        // Act
        Optional<Employee> foundEmployee = employeeRepository.findById(999L);
        
        // Assert
        assertFalse(foundEmployee.isPresent());
    }

    @Test
    @DisplayName("Should find employee by badge ID")
    public void testFindByBadgeId() {
        // Arrange
        employeeRepository.save(testEmployee);
        
        // Act
        Optional<Employee> foundEmployee = employeeRepository.findByBadgeId("EMP001");
        
        // Assert
        assertTrue(foundEmployee.isPresent());
        assertEquals("John Doe", foundEmployee.get().getName());
    }

    @Test
    @DisplayName("Should find all employees")
    public void testFindAll() {
        // Arrange
        employeeRepository.save(testEmployee);
        
        Employee employee2 = new Employee();
        employee2.setName("Jane Smith");
        employee2.setBadgeId("EMP002");
        employee2.setRole("SUPERVISOR");
        employee2.setDepartment("Logistics");
        employee2.setHireDate(LocalDate.now());
        employee2.setStatus("ACTIVE");
        employeeRepository.save(employee2);
        
        // Act
        List<Employee> employees = employeeRepository.findAll();
        
        // Assert
        assertEquals(2, employees.size());
    }

    // ========== UPDATE OPERATIONS ==========

    @Test
    @DisplayName("Should update employee successfully")
    public void testUpdateEmployee() {
        // Arrange
        Employee savedEmployee = employeeRepository.save(testEmployee);
        
        // Act
        savedEmployee.setName("John Updated");
        savedEmployee.setDepartment("Logistics");
        Employee updatedEmployee = employeeRepository.save(savedEmployee);
        
        // Assert
        assertEquals("John Updated", updatedEmployee.getName());
        assertEquals("Logistics", updatedEmployee.getDepartment());
    }

    @Test
    @DisplayName("Should update employee status")
    public void testUpdateEmployeeStatus() {
        // Arrange
        Employee savedEmployee = employeeRepository.save(testEmployee);
        
        // Act
        savedEmployee.setStatus("INACTIVE");
        Employee updatedEmployee = employeeRepository.save(savedEmployee);
        
        // Assert
        assertEquals("INACTIVE", updatedEmployee.getStatus());
    }

    // ========== DELETE OPERATIONS ==========

    @Test
    @DisplayName("Should delete employee by ID")
    public void testDeleteById() {
        // Arrange
        Employee savedEmployee = employeeRepository.save(testEmployee);
        Long employeeId = savedEmployee.getId();
        
        // Act
        employeeRepository.deleteById(employeeId);
        
        // Assert
        assertFalse(employeeRepository.findById(employeeId).isPresent());
    }

    @Test
    @DisplayName("Should soft delete employee")
    public void testSoftDelete() {
        // Arrange
        Employee savedEmployee = employeeRepository.save(testEmployee);
        
        // Act
        savedEmployee.setDeleted(true);
        employeeRepository.save(savedEmployee);
        
        // Assert
        Optional<Employee> foundEmployee = employeeRepository.findById(savedEmployee.getId());
        assertTrue(foundEmployee.isPresent());
        assertTrue(foundEmployee.get().isDeleted());
    }

    // ========== PAGINATION TESTS ==========

    @Test
    @DisplayName("Should paginate employees correctly")
    public void testPagination() {
        // Arrange
        for (int i = 1; i <= 10; i++) {
            Employee emp = new Employee();
            emp.setName("Employee " + i);
            emp.setBadgeId("EMP" + String.format("%03d", i));
            emp.setRole("WORKER");
            emp.setDepartment("Warehouse");
            emp.setHireDate(LocalDate.now());
            emp.setStatus("ACTIVE");
            employeeRepository.save(emp);
        }
        
        // Act
        Pageable pageable = PageRequest.of(0, 5);
        Page<Employee> page = employeeRepository.findAll(pageable);
        
        // Assert
        assertEquals(5, page.getContent().size());
        assertEquals(10, page.getTotalElements());
        assertEquals(2, page.getTotalPages());
    }

    @Test
    @DisplayName("Should handle empty page")
    public void testEmptyPage() {
        // Act
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> page = employeeRepository.findAll(pageable);
        
        // Assert
        assertTrue(page.getContent().isEmpty());
        assertEquals(0, page.getTotalElements());
    }

    // ========== FILTERING TESTS ==========

    @Test
    @DisplayName("Should find employees by department")
    public void testFindByDepartment() {
        // Arrange
        employeeRepository.save(testEmployee);
        
        Employee employee2 = new Employee();
        employee2.setName("Jane Smith");
        employee2.setBadgeId("EMP002");
        employee2.setRole("SUPERVISOR");
        employee2.setDepartment("Logistics");
        employee2.setHireDate(LocalDate.now());
        employee2.setStatus("ACTIVE");
        employeeRepository.save(employee2);
        
        // Act
        List<Employee> warehouseEmployees = employeeRepository.findByDepartment("Warehouse");
        
        // Assert
        assertEquals(1, warehouseEmployees.size());
        assertEquals("John Doe", warehouseEmployees.get(0).getName());
    }

    @Test
    @DisplayName("Should find employees by role")
    public void testFindByRole() {
        // Arrange
        employeeRepository.save(testEmployee);
        
        Employee employee2 = new Employee();
        employee2.setName("Jane Smith");
        employee2.setBadgeId("EMP002");
        employee2.setRole("SUPERVISOR");
        employee2.setDepartment("Warehouse");
        employee2.setHireDate(LocalDate.now());
        employee2.setStatus("ACTIVE");
        employeeRepository.save(employee2);
        
        // Act
        List<Employee> workers = employeeRepository.findByRole("WORKER");
        
        // Assert
        assertEquals(1, workers.size());
        assertEquals("WORKER", workers.get(0).getRole());
    }

    @Test
    @DisplayName("Should find employees by status")
    public void testFindByStatus() {
        // Arrange
        employeeRepository.save(testEmployee);
        
        Employee employee2 = new Employee();
        employee2.setName("Jane Smith");
        employee2.setBadgeId("EMP002");
        employee2.setRole("WORKER");
        employee2.setDepartment("Warehouse");
        employee2.setHireDate(LocalDate.now());
        employee2.setStatus("INACTIVE");
        employeeRepository.save(employee2);
        
        // Act
        List<Employee> activeEmployees = employeeRepository.findByStatus("ACTIVE");
        
        // Assert
        assertEquals(1, activeEmployees.size());
        assertEquals("ACTIVE", activeEmployees.get(0).getStatus());
    }

    @Test
    @DisplayName("Should find non-deleted employees")
    public void testFindByDeletedFalse() {
        // Arrange
        employeeRepository.save(testEmployee);
        
        Employee employee2 = new Employee();
        employee2.setName("Jane Smith");
        employee2.setBadgeId("EMP002");
        employee2.setRole("WORKER");
        employee2.setDepartment("Warehouse");
        employee2.setHireDate(LocalDate.now());
        employee2.setStatus("ACTIVE");
        employee2.setDeleted(true);
        employeeRepository.save(employee2);
        
        // Act
        List<Employee> activeEmployees = employeeRepository.findByDeletedFalse();
        
        // Assert
        assertEquals(1, activeEmployees.size());
        assertFalse(activeEmployees.get(0).isDeleted());
    }

    // ========== EDGE CASES ==========

    @Test
    @DisplayName("Should handle null badge ID in query")
    public void testFindByNullBadgeId() {
        // Act
        Optional<Employee> foundEmployee = employeeRepository.findByBadgeId(null);
        
        // Assert
        assertFalse(foundEmployee.isPresent());
    }

    @Test
    @DisplayName("Should handle empty department filter")
    public void testFindByEmptyDepartment() {
        // Act
        List<Employee> employees = employeeRepository.findByDepartment("");
        
        // Assert
        assertTrue(employees.isEmpty());
    }

    @Test
    @DisplayName("Should count employees correctly")
    public void testCountEmployees() {
        // Arrange
        employeeRepository.save(testEmployee);
        
        Employee employee2 = new Employee();
        employee2.setName("Jane Smith");
        employee2.setBadgeId("EMP002");
        employee2.setRole("WORKER");
        employee2.setDepartment("Warehouse");
        employee2.setHireDate(LocalDate.now());
        employee2.setStatus("ACTIVE");
        employeeRepository.save(employee2);
        
        // Act
        long count = employeeRepository.count();
        
        // Assert
        assertEquals(2, count);
    }

    @Test
    @DisplayName("Should check if employee exists by badge ID")
    public void testExistsByBadgeId() {
        // Arrange
        employeeRepository.save(testEmployee);
        
        // Act
        boolean exists = employeeRepository.existsByBadgeId("EMP001");
        boolean notExists = employeeRepository.existsByBadgeId("EMP999");
        
        // Assert
        assertTrue(exists);
        assertFalse(notExists);
    }