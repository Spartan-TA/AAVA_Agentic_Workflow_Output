package com.warehouse.employee.repository;

import com.warehouse.employee.domain.Employee;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDate;
import java.util.Optional;

/**
 * Comprehensive unit tests for EmployeeRepository.
 * Tests cover CRUD operations, custom queries, and database constraints.
 */
@DataJpaTest
@DisplayName("Employee Repository Tests")
public class EmployeeRepositoryTest {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Employee testEmployee;

    @BeforeEach
    public void setUp() {
        testEmployee = Employee.builder()
                .name("John Doe")
                .badgeId("BADGE001")
                .role("WORKER")
                .department("Warehouse")
                .shiftGroup("Morning")
                .hireDate(LocalDate.of(2023, 1, 15))
                .status("ACTIVE")
                .deleted(false)
                .build();
    }

    // ========== NORMAL CASES - CREATE ==========

    @Test
    @DisplayName("Test save employee with valid data")
    public void testSave_WithValidEmployee_Success() {
        // Act
        Employee saved = employeeRepository.save(testEmployee);
        entityManager.flush();

        // Assert
        assertNotNull(saved.getId());
        assertEquals("John Doe", saved.getName());
        assertEquals("BADGE001", saved.getBadgeId());
    }

    @Test
    @DisplayName("Test save multiple employees")
    public void testSave_MultipleEmployees_Success() {
        // Arrange
        Employee employee2 = Employee.builder()
                .name("Jane Smith")
                .badgeId("BADGE002")
                .role("SUPERVISOR")
                .department("Logistics")
                .shiftGroup("Evening")
                .hireDate(LocalDate.of(2022, 6, 1))
                .status("ACTIVE")
                .deleted(false)
                .build();

        // Act
        Employee saved1 = employeeRepository.save(testEmployee);
        Employee saved2 = employeeRepository.save(employee2);
        entityManager.flush();

        // Assert
        assertNotNull(saved1.getId());
        assertNotNull(saved2.getId());
        assertNotEquals(saved1.getId(), saved2.getId());
    }

    // ========== NORMAL CASES - READ ==========

    @Test
    @DisplayName("Test findById with existing employee")
    public void testFindById_WithExistingEmployee_ReturnsEmployee() {
        // Arrange
        Employee saved = entityManager.persistAndFlush(testEmployee);

        // Act
        Optional<Employee> found = employeeRepository.findById(saved.getId());

        // Assert
        assertTrue(found.isPresent());
        assertEquals(saved.getId(), found.get().getId());
        assertEquals("John Doe", found.get().getName());
    }

    @Test
    @DisplayName("Test findById with non-existing employee")
    public void testFindById_WithNonExistingEmployee_ReturnsEmpty() {
        // Act
        Optional<Employee> found = employeeRepository.findById(999L);

        // Assert
        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("Test findByBadgeId with existing badge")
    public void testFindByBadgeId_WithExistingBadge_ReturnsEmployee() {
        // Arrange
        entityManager.persistAndFlush(testEmployee);

        // Act
        Optional<Employee> found = employeeRepository.findByBadgeId("BADGE001");

        // Assert
        assertTrue(found.isPresent());
        assertEquals("BADGE001", found.get().getBadgeId());
        assertEquals("John Doe", found.get().getName());
    }

    @Test
    @DisplayName("Test findByBadgeId with non-existing badge")
    public void testFindByBadgeId_WithNonExistingBadge_ReturnsEmpty() {
        // Act
        Optional<Employee> found = employeeRepository.findByBadgeId("NONEXISTENT");

        // Assert
        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("Test findAll returns all employees")
    public void testFindAll_WithMultipleEmployees_ReturnsAll() {
        // Arrange
        Employee employee2 = Employee.builder()
                .name("Jane Smith")
                .badgeId("BADGE002")
                .role("SUPERVISOR")
                .department("Logistics")
                .shiftGroup("Evening")
                .hireDate(LocalDate.of(2022, 6, 1))
                .status("ACTIVE")
                .deleted(false)
                .build();

        entityManager.persistAndFlush(testEmployee);
        entityManager.persistAndFlush(employee2);

        // Act
        var employees = employeeRepository.findAll();

        // Assert
        assertEquals(2, employees.size());
    }

    // ========== NORMAL CASES - UPDATE ==========

    @Test
    @DisplayName("Test update employee name")
    public void testUpdate_EmployeeName_Success() {
        // Arrange
        Employee saved = entityManager.persistAndFlush(testEmployee);

        // Act
        saved.setName("Updated Name");
        Employee updated = employeeRepository.save(saved);
        entityManager.flush();

        // Assert
        assertEquals("Updated Name", updated.getName());
        assertEquals(saved.getId(), updated.getId());
    }

    @Test
    @DisplayName("Test update employee status")
    public void testUpdate_EmployeeStatus_Success() {
        // Arrange
        Employee saved = entityManager.persistAndFlush(testEmployee);

        // Act
        saved.setStatus("INACTIVE");
        Employee updated = employeeRepository.save(saved);
        entityManager.flush();

        // Assert
        assertEquals("INACTIVE", updated.getStatus());
    }

    @Test
    @DisplayName("Test soft delete employee")
    public void testUpdate_SoftDeleteEmployee_Success() {
        // Arrange
        Employee saved = entityManager.persistAndFlush(testEmployee);

        // Act
        saved.setDeleted(true);
        Employee updated = employeeRepository.save(saved);
        entityManager.flush();

        // Assert
        assertTrue(updated.isDeleted());
    }

    // ========== NORMAL CASES - DELETE ==========

    @Test
    @DisplayName("Test delete employee by ID")
    public void testDelete_ByEmployeeId_Success() {
        // Arrange
        Employee saved = entityManager.persistAndFlush(testEmployee);
        Long employeeId = saved.getId();

        // Act
        employeeRepository.deleteById(employeeId);
        entityManager.flush();

        // Assert
        Optional<Employee> found = employeeRepository.findById(employeeId);
        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("Test delete employee entity")
    public void testDelete_EmployeeEntity_Success() {
        // Arrange
        Employee saved = entityManager.persistAndFlush(testEmployee);
        Long employeeId = saved.getId();

        // Act
        employeeRepository.delete(saved);
        entityManager.flush();

        // Assert
        Optional<Employee> found = employeeRepository.findById(employeeId);
        assertFalse(found.isPresent());
    }

    // ========== EDGE CASES ==========

    @Test
    @DisplayName("Test save employee with null name")
    public void testSave_WithNullName_ThrowsException() {
        // Arrange
        testEmployee.setName(null);

        // Act & Assert
        assertThrows(Exception.class, () -> {
            employeeRepository.save(testEmployee);
            entityManager.flush();
        });
    }

    @Test
    @DisplayName("Test save employee with null badgeId")
    public void testSave_WithNullBadgeId_ThrowsException() {
        // Arrange
        testEmployee.setBadgeId(null);

        // Act & Assert
        assertThrows(Exception.class, () -> {
            employeeRepository.save(testEmployee);
            entityManager.flush();
        });
    }

    @Test
    @DisplayName("Test save employee with duplicate badgeId")
    public void testSave_WithDuplicateBadgeId_ThrowsException() {
        // Arrange
        entityManager.persistAndFlush(testEmployee);

        Employee duplicate = Employee.builder()
                .name("Different Name")
                .badgeId("BADGE001") // Same badge ID
                .role("WORKER")
                .department("Warehouse")
                .shiftGroup("Morning")
                .hireDate(LocalDate.of(2023, 1, 15))
                .status("ACTIVE")
                .deleted(false)
                .build();

        // Act & Assert
        assertThrows(DataIntegrityViolationException.class, () -> {
            employeeRepository.save(duplicate);
            entityManager.flush();
        });
    }

    @Test
    @DisplayName("Test findByBadgeId with null badgeId")
    public void testFindByBadgeId_WithNull_ReturnsEmpty() {
        // Act
        Optional<Employee> found = employeeRepository.findByBadgeId(null);

        // Assert
        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("Test findByBadgeId with empty string")
    public void testFindByBadgeId_WithEmptyString_ReturnsEmpty() {
        // Act
        Optional<Employee> found = employeeRepository.findByBadgeId("");

        // Assert
        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("Test findByBadgeId with whitespace")
    public void testFindByBadgeId_WithWhitespace_ReturnsEmpty() {
        // Act
        Optional<Employee> found = employeeRepository.findByBadgeId("   ");

        // Assert
        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("Test findById with null ID")
    public void testFindById_WithNull_ThrowsException() {
        // Act & Assert
        assertThrows(Exception.class, () -> {
            employeeRepository.findById(null);
        });
    }

    @Test
    @DisplayName("Test save employee with empty name")
    public void testSave_WithEmptyName_Success() {
        // Arrange
        testEmployee.setName("");

        // Act
        Employee saved = employeeRepository.save(testEmployee);
        entityManager.flush();

        // Assert
        assertNotNull(saved.getId());
        assertEquals("", saved.getName());
    }

    @Test
    @DisplayName("Test save employee with very long name")
    public void testSave_WithVeryLongName_Success() {
        // Arrange
        String longName = "A".repeat(255);
        testEmployee.setName(longName);

        // Act
        Employee saved = employeeRepository.save(testEmployee);
        entityManager.flush();

        // Assert
        assertNotNull(saved.getId());
        assertEquals(longName, saved.getName());
    }

    // ========== BOUNDARY CONDITIONS ==========

    @Test
    @DisplayName("Test findById with zero ID")
    public void testFindById_WithZeroId_ReturnsEmpty() {
        // Act
        Optional<Employee> found = employeeRepository.findById(0L);

        // Assert
        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("Test findById with negative ID")
    public void testFindById_WithNegativeId_ReturnsEmpty() {
        // Act
        Optional<Employee> found = employeeRepository.findById(-1L);

        // Assert
        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("Test findById with maximum Long value")
    public void testFindById_WithMaxLongValue_ReturnsEmpty() {
        // Act
        Optional<Employee> found = employeeRepository.findById(Long.MAX_VALUE);

        // Assert
        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("Test save employee with future hire date")
    public void testSave_WithFutureHireDate_Success() {
        // Arrange
        testEmployee.setHireDate(LocalDate.now().plusYears(1));

        // Act
        Employee saved = employeeRepository.save(testEmployee);
        entityManager.flush();

        // Assert
        assertNotNull(saved.getId());
        assertTrue(saved.getHireDate().isAfter(LocalDate.now()));
    }

    @Test
    @DisplayName("Test save employee with very old hire date")
    public void testSave_WithVeryOldHireDate_Success() {
        // Arrange
        testEmployee.setHireDate(LocalDate.of(1950, 1, 1));

        // Act
        Employee saved = employeeRepository.save(testEmployee);
        entityManager.flush();

        // Assert
        assertNotNull(saved.getId());
        assertEquals(LocalDate.of(1950, 1, 1), saved.getHireDate());
    }

    @Test
    @DisplayName("Test count employees")
    public void testCount_WithMultipleEmployees_ReturnsCorrectCount() {
        // Arrange
        Employee employee2 = Employee.builder()
                .name("Jane Smith")
                .badgeId("BADGE002")
                .role("SUPERVISOR")
                .department("Logistics")
                .shiftGroup("Evening")
                .hireDate(LocalDate.of(2022, 6, 1))
                .status("ACTIVE")
                .deleted(false)
                .build();

        entityManager.persistAndFlush(testEmployee);
        entityManager.persistAndFlush(employee2);

        // Act
        long count = employeeRepository.count();

        // Assert
        assertEquals(2, count);
    }

    @Test
    @DisplayName("Test existsById with existing employee")
    public void testExistsById_WithExistingEmployee_ReturnsTrue() {
        // Arrange
        Employee saved = entityManager.persistAndFlush(testEmployee);

        // Act
        boolean exists = employeeRepository.existsById(saved.getId());

        // Assert
        assertTrue(exists);
    }

    @Test
    @DisplayName("Test existsById with non-existing employee")
    public void testExistsById_WithNonExistingEmployee_ReturnsFalse() {
        // Act
        boolean exists = employeeRepository.existsById(999L);

        // Assert
        assertFalse(exists);
    }
}
