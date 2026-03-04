package com.wms.employee.repository;

import com.wms.employee.model.Employee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for EmployeeRepository.
 * Tests cover normal cases, boundary conditions, and edge cases for all repository methods.
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Employee Repository Tests")
public class EmployeeRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private EmployeeRepository employeeRepository;

    private Employee testEmployee;

    @BeforeEach
    public void setUp() {
        // Arrange: Create a test employee
        testEmployee = new Employee();
        testEmployee.setBadgeId("EMP001");
        testEmployee.setName("John Doe");
        testEmployee.setRole("WORKER");
        testEmployee.setDepartment("Warehouse");
        testEmployee.setShiftGroup("Day Shift");
        testEmployee.setHireDate(LocalDate.of(2023, 1, 15));
        testEmployee.setStatus("ACTIVE");
        testEmployee.setDeleted(false);
    }

    // ========== Tests for save() method ==========

    @Test
    @DisplayName("Test save employee with valid data")
    public void testSaveEmployee_ValidData_Success() {
        // Act
        Employee savedEmployee = employeeRepository.save(testEmployee);
        entityManager.flush();

        // Assert
        assertNotNull(savedEmployee.getId());
        assertEquals("EMP001", savedEmployee.getBadgeId());
        assertEquals("John Doe", savedEmployee.getName());
        assertEquals("WORKER", savedEmployee.getRole());
        assertEquals("Warehouse", savedEmployee.getDepartment());
        assertEquals("Day Shift", savedEmployee.getShiftGroup());
        assertEquals(LocalDate.of(2023, 1, 15), savedEmployee.getHireDate());
        assertEquals("ACTIVE", savedEmployee.getStatus());
        assertFalse(savedEmployee.isDeleted());
    }

    @Test
    @DisplayName("Test save employee with null name")
    public void testSaveEmployee_NullName_Success() {
        // Arrange
        testEmployee.setName(null);

        // Act
        Employee savedEmployee = employeeRepository.save(testEmployee);
        entityManager.flush();

        // Assert
        assertNotNull(savedEmployee.getId());
        assertNull(savedEmployee.getName());
    }

    @Test
    @DisplayName("Test save employee with empty string name")
    public void testSaveEmployee_EmptyName_Success() {
        // Arrange
        testEmployee.setName("");

        // Act
        Employee savedEmployee = employeeRepository.save(testEmployee);
        entityManager.flush();

        // Assert
        assertNotNull(savedEmployee.getId());
        assertEquals("", savedEmployee.getName());
    }

    @Test
    @DisplayName("Test save employee with very long name (boundary)")
    public void testSaveEmployee_VeryLongName_Success() {
        // Arrange
        String longName = "A".repeat(255);
        testEmployee.setName(longName);

        // Act
        Employee savedEmployee = employeeRepository.save(testEmployee);
        entityManager.flush();

        // Assert
        assertNotNull(savedEmployee.getId());
        assertEquals(longName, savedEmployee.getName());
    }

    @Test
    @DisplayName("Test save employee with special characters in name")
    public void testSaveEmployee_SpecialCharactersInName_Success() {
        // Arrange
        testEmployee.setName("JosÃ© MarÃ­a O'Brien-Smith");

        // Act
        Employee savedEmployee = employeeRepository.save(testEmployee);
        entityManager.flush();

        // Assert
        assertNotNull(savedEmployee.getId());
        assertEquals("JosÃ© MarÃ­a O'Brien-Smith", savedEmployee.getName());
    }

    @Test
    @DisplayName("Test save employee with duplicate badgeId throws exception")
    public void testSaveEmployee_DuplicateBadgeId_ThrowsException() {
        // Arrange
        employeeRepository.save(testEmployee);
        entityManager.flush();
        entityManager.clear();

        Employee duplicateEmployee = new Employee();
        duplicateEmployee.setBadgeId("EMP001"); // Same badgeId
        duplicateEmployee.setName("Jane Doe");
        duplicateEmployee.setRole("SUPERVISOR");
        duplicateEmployee.setDepartment("Warehouse");
        duplicateEmployee.setShiftGroup("Night Shift");
        duplicateEmployee.setHireDate(LocalDate.of(2023, 2, 1));
        duplicateEmployee.setStatus("ACTIVE");

        // Act & Assert
        assertThrows(Exception.class, () -> {
            employeeRepository.save(duplicateEmployee);
            entityManager.flush();
        });
    }

    @Test
    @DisplayName("Test save employee with null badgeId throws exception")
    public void testSaveEmployee_NullBadgeId_ThrowsException() {
        // Arrange
        testEmployee.setBadgeId(null);

        // Act & Assert
        assertThrows(Exception.class, () -> {
            employeeRepository.save(testEmployee);
            entityManager.flush();
        });
    }

    @Test
    @DisplayName("Test save employee with future hire date")
    public void testSaveEmployee_FutureHireDate_Success() {
        // Arrange
        testEmployee.setHireDate(LocalDate.now().plusDays(30));

        // Act
        Employee savedEmployee = employeeRepository.save(testEmployee);
        entityManager.flush();

        // Assert
        assertNotNull(savedEmployee.getId());
        assertTrue(savedEmployee.getHireDate().isAfter(LocalDate.now()));
    }

    @Test
    @DisplayName("Test save employee with past hire date")
    public void testSaveEmployee_PastHireDate_Success() {
        // Arrange
        testEmployee.setHireDate(LocalDate.of(2000, 1, 1));

        // Act
        Employee savedEmployee = employeeRepository.save(testEmployee);
        entityManager.flush();

        // Assert
        assertNotNull(savedEmployee.getId());
        assertEquals(LocalDate.of(2000, 1, 1), savedEmployee.getHireDate());
    }

    // ========== Tests for findById() method ==========

    @Test
    @DisplayName("Test find employee by valid ID")
    public void testFindById_ValidId_ReturnsEmployee() {
        // Arrange
        Employee savedEmployee = employeeRepository.save(testEmployee);
        entityManager.flush();
        Long employeeId = savedEmployee.getId();

        // Act
        Optional<Employee> foundEmployee = employeeRepository.findById(employeeId);

        // Assert
        assertTrue(foundEmployee.isPresent());
        assertEquals(employeeId, foundEmployee.get().getId());
        assertEquals("EMP001", foundEmployee.get().getBadgeId());
    }

    @Test
    @DisplayName("Test find employee by non-existent ID")
    public void testFindById_NonExistentId_ReturnsEmpty() {
        // Act
        Optional<Employee> foundEmployee = employeeRepository.findById(999L);

        // Assert
        assertFalse(foundEmployee.isPresent());
    }

    @Test
    @DisplayName("Test find employee by null ID")
    public void testFindById_NullId_ReturnsEmpty() {
        // Act
        Optional<Employee> foundEmployee = employeeRepository.findById(null);

        // Assert
        assertFalse(foundEmployee.isPresent());
    }

    @Test
    @DisplayName("Test find employee by negative ID")
    public void testFindById_NegativeId_ReturnsEmpty() {
        // Act
        Optional<Employee> foundEmployee = employeeRepository.findById(-1L);

        // Assert
        assertFalse(foundEmployee.isPresent());
    }

    @Test
    @DisplayName("Test find employee by zero ID")
    public void testFindById_ZeroId_ReturnsEmpty() {
        // Act
        Optional<Employee> foundEmployee = employeeRepository.findById(0L);

        // Assert
        assertFalse(foundEmployee.isPresent());
    }

    // ========== Tests for findByBadgeId() method ==========

    @Test
    @DisplayName("Test find employee by valid badgeId")
    public void testFindByBadgeId_ValidBadgeId_ReturnsEmployee() {
        // Arrange
        employeeRepository.save(testEmployee);
        entityManager.flush();

        // Act
        Optional<Employee> foundEmployee = employeeRepository.findByBadgeId("EMP001");

        // Assert
        assertTrue(foundEmployee.isPresent());
        assertEquals("EMP001", foundEmployee.get().getBadgeId());
        assertEquals("John Doe", foundEmployee.get().getName());
    }

    @Test
    @DisplayName("Test find employee by non-existent badgeId")
    public void testFindByBadgeId_NonExistentBadgeId_ReturnsEmpty() {
        // Act
        Optional<Employee> foundEmployee = employeeRepository.findByBadgeId("NONEXISTENT");

        // Assert
        assertFalse(foundEmployee.isPresent());
    }

    @Test
    @DisplayName("Test find employee by null badgeId")
    public void testFindByBadgeId_NullBadgeId_ReturnsEmpty() {
        // Act
        Optional<Employee> foundEmployee = employeeRepository.findByBadgeId(null);

        // Assert
        assertFalse(foundEmployee.isPresent());
    }

    @Test
    @DisplayName("Test find employee by empty badgeId")
    public void testFindByBadgeId_EmptyBadgeId_ReturnsEmpty() {
        // Act
        Optional<Employee> foundEmployee = employeeRepository.findByBadgeId("");

        // Assert
        assertFalse(foundEmployee.isPresent());
    }

    @Test
    @DisplayName("Test find employee by badgeId with whitespace")
    public void testFindByBadgeId_BadgeIdWithWhitespace_ReturnsEmpty() {
        // Act
        Optional<Employee> foundEmployee = employeeRepository.findByBadgeId("  EMP001  ");

        // Assert
        assertFalse(foundEmployee.isPresent());
    }

    @Test
    @DisplayName("Test find employee by case-sensitive badgeId")
    public void testFindByBadgeId_CaseSensitive_ReturnsEmpty() {
        // Arrange
        employeeRepository.save(testEmployee);
        entityManager.flush();

        // Act
        Optional<Employee> foundEmployee = employeeRepository.findByBadgeId("emp001");

        // Assert
        assertFalse(foundEmployee.isPresent());
    }

    // ========== Tests for findAllByDeletedFalse() method ==========

    @Test
    @DisplayName("Test find all active employees with pagination")
    public void testFindAllByDeletedFalse_WithPagination_ReturnsActiveEmployees() {
        // Arrange
        Employee employee1 = createEmployee("EMP001", "John Doe", false);
        Employee employee2 = createEmployee("EMP002", "Jane Smith", false);
        Employee employee3 = createEmployee("EMP003", "Bob Johnson", true); // Deleted
        
        employeeRepository.save(employee1);
        employeeRepository.save(employee2);
        employeeRepository.save(employee3);
        entityManager.flush();

        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Employee> result = employeeRepository.findAllByDeletedFalse(pageable);

        // Assert
        assertEquals(2, result.getTotalElements());
        assertEquals(2, result.getContent().size());
        assertTrue(result.getContent().stream().noneMatch(Employee::isDeleted));
    }

    @Test
    @DisplayName("Test find all active employees when all are deleted")
    public void testFindAllByDeletedFalse_AllDeleted_ReturnsEmpty() {
        // Arrange
        Employee employee1 = createEmployee("EMP001", "John Doe", true);
        Employee employee2 = createEmployee("EMP002", "Jane Smith", true);
        
        employeeRepository.save(employee1);
        employeeRepository.save(employee2);
        entityManager.flush();

        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Employee> result = employeeRepository.findAllByDeletedFalse(pageable);

        // Assert
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
    }

    @Test
    @DisplayName("Test find all active employees with no employees")
    public void testFindAllByDeletedFalse_NoEmployees_ReturnsEmpty() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Employee> result = employeeRepository.findAllByDeletedFalse(pageable);

        // Assert
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
    }

    @Test
    @DisplayName("Test find all active employees with pagination boundary (page size 1)")
    public void testFindAllByDeletedFalse_PageSizeOne_ReturnsOneEmployee() {
        // Arrange
        Employee employee1 = createEmployee("EMP001", "John Doe", false);
        Employee employee2 = createEmployee("EMP002", "Jane Smith", false);
        
        employeeRepository.save(employee1);
        employeeRepository.save(employee2);
        entityManager.flush();

        Pageable pageable = PageRequest.of(0, 1);

        // Act
        Page<Employee> result = employeeRepository.findAllByDeletedFalse(pageable);

        // Assert
        assertEquals(2, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        assertEquals(2, result.getTotalPages());
    }

    @Test
    @DisplayName("Test find all active employees with large page size")
    public void testFindAllByDeletedFalse_LargePageSize_ReturnsAllEmployees() {
        // Arrange
        for (int i = 1; i <= 50; i++) {
            Employee employee = createEmployee("EMP" + String.format("%03d", i), "Employee " + i, false);
            employeeRepository.save(employee);
        }
        entityManager.flush();

        Pageable pageable = PageRequest.of(0, 100);

        // Act
        Page<Employee> result = employeeRepository.findAllByDeletedFalse(pageable);

        // Assert
        assertEquals(50, result.getTotalElements());
        assertEquals(50, result.getContent().size());
    }

    @Test
    @DisplayName("Test find all active employees with page beyond total pages")
    public void testFindAllByDeletedFalse_PageBeyondTotal_ReturnsEmpty() {
        // Arrange
        Employee employee1 = createEmployee("EMP001", "John Doe", false);
        employeeRepository.save(employee1);
        entityManager.flush();

        Pageable pageable = PageRequest.of(10, 10); // Page 10, but only 1 employee

        // Act
        Page<Employee> result = employeeRepository.findAllByDeletedFalse(pageable);

        // Assert
        assertEquals(1, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
    }

    // ========== Tests for update operations ==========

    @Test
    @DisplayName("Test update employee name")
    public void testUpdateEmployee_Name_Success() {
        // Arrange
        Employee savedEmployee = employeeRepository.save(testEmployee);
        entityManager.flush();
        entityManager.clear();

        // Act
        savedEmployee.setName("Updated Name");
        Employee updatedEmployee = employeeRepository.save(savedEmployee);
        entityManager.flush();

        // Assert
        assertEquals("Updated Name", updatedEmployee.getName());
    }

    @Test
    @DisplayName("Test soft delete employee")
    public void testSoftDeleteEmployee_Success() {
        // Arrange
        Employee savedEmployee = employeeRepository.save(testEmployee);
        entityManager.flush();
        Long employeeId = savedEmployee.getId();

        // Act
        savedEmployee.setDeleted(true);
        employeeRepository.save(savedEmployee);
        entityManager.flush();
        entityManager.clear();

        // Assert
        Optional<Employee> foundEmployee = employeeRepository.findById(employeeId);
        assertTrue(foundEmployee.isPresent());
        assertTrue(foundEmployee.get().isDeleted());
    }

    @Test
    @DisplayName("Test update employee status")
    public void testUpdateEmployee_Status_Success() {
        // Arrange
        Employee savedEmployee = employeeRepository.save(testEmployee);
        entityManager.flush();

        // Act
        savedEmployee.setStatus("INACTIVE");
        Employee updatedEmployee = employeeRepository.save(savedEmployee);
        entityManager.flush();

        // Assert
        assertEquals("INACTIVE", updatedEmployee.getStatus());
    }

    // ========== Helper Methods ==========

    private Employee createEmployee(String badgeId, String name, boolean deleted) {
        Employee employee = new Employee();
        employee.setBadgeId(badgeId);
        employee.setName(name);
        employee.setRole("WORKER");
        employee.setDepartment("Warehouse");
        employee.setShiftGroup("Day Shift");
        employee.setHireDate(LocalDate.now());
        employee.setStatus("ACTIVE");
        employee.setDeleted(deleted);
        return employee;
    }
}