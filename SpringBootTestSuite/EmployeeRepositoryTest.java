package com.company.warehousemgmt.repository;

import com.company.warehousemgmt.domain.Employee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive JUnit test suite for EmployeeRepository
 * Tests cover JPA operations, custom queries, and database constraints
 */
@DataJpaTest
class EmployeeRepositoryTest {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Employee testEmployee;

    @BeforeEach
    void setUp() {
        // Clear database before each test
        employeeRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();

        // Create test employee
        testEmployee = new Employee();
        testEmployee.setBadgeId("EMP001");
        testEmployee.setName("John Doe");
        testEmployee.setRole("WORKER");
        testEmployee.setDepartment("Warehouse");
        testEmployee.setShiftGroup("A");
        testEmployee.setHireDate(LocalDate.of(2023, 1, 15));
        testEmployee.setStatus("ACTIVE");
        testEmployee.setDeleted(false);
    }

    // ========== Save Tests ==========

    @Test
    void testSave_WithValidEmployee_PersistsEmployee() {
        // Act
        Employee saved = employeeRepository.save(testEmployee);
        entityManager.flush();

        // Assert
        assertNotNull(saved.getId());
        assertEquals("EMP001", saved.getBadgeId());
        assertEquals("John Doe", saved.getName());
        assertEquals("WORKER", saved.getRole());
    }

    @Test
    void testSave_WithNullBadgeId_ThrowsException() {
        // Arrange
        testEmployee.setBadgeId(null);

        // Act & Assert
        assertThrows(Exception.class, () -> {
            employeeRepository.save(testEmployee);
            entityManager.flush();
        });
    }

    @Test
    void testSave_WithNullName_ThrowsException() {
        // Arrange
        testEmployee.setName(null);

        // Act & Assert
        assertThrows(Exception.class, () -> {
            employeeRepository.save(testEmployee);
            entityManager.flush();
        });
    }

    @Test
    void testSave_WithDuplicateBadgeId_ThrowsException() {
        // Arrange
        employeeRepository.save(testEmployee);
        entityManager.flush();

        Employee duplicate = new Employee();
        duplicate.setBadgeId("EMP001");
        duplicate.setName("Jane Doe");
        duplicate.setRole("WORKER");
        duplicate.setDepartment("Warehouse");
        duplicate.setShiftGroup("A");
        duplicate.setHireDate(LocalDate.of(2023, 1, 15));
        duplicate.setStatus("ACTIVE");
        duplicate.setDeleted(false);

        // Act & Assert
        assertThrows(Exception.class, () -> {
            employeeRepository.save(duplicate);
            entityManager.flush();
        });
    }

    @Test
    void testSave_WithMaxLengthFields_PersistsEmployee() {
        // Arrange
        testEmployee.setName("A".repeat(100));
        testEmployee.setDepartment("B".repeat(100));

        // Act
        Employee saved = employeeRepository.save(testEmployee);
        entityManager.flush();

        // Assert
        assertNotNull(saved.getId());
        assertEquals(100, saved.getName().length());
        assertEquals(100, saved.getDepartment().length());
    }

    @Test
    void testSave_WithSpecialCharacters_PersistsEmployee() {
        // Arrange
        testEmployee.setName("John O'Brien-Smith");

        // Act
        Employee saved = employeeRepository.save(testEmployee);
        entityManager.flush();

        // Assert
        assertNotNull(saved.getId());
        assertEquals("John O'Brien-Smith", saved.getName());
    }

    @Test
    void testSave_WithUnicodeCharacters_PersistsEmployee() {
        // Arrange
        testEmployee.setName("JosÃ© GarcÃ­a");

        // Act
        Employee saved = employeeRepository.save(testEmployee);
        entityManager.flush();

        // Assert
        assertNotNull(saved.getId());
        assertEquals("JosÃ© GarcÃ­a", saved.getName());
    }

    // ========== FindById Tests ==========

    @Test
    void testFindById_WithExistingId_ReturnsEmployee() {
        // Arrange
        Employee saved = employeeRepository.save(testEmployee);
        entityManager.flush();
        entityManager.clear();

        // Act
        Optional<Employee> found = employeeRepository.findById(saved.getId());

        // Assert
        assertTrue(found.isPresent());
        assertEquals("EMP001", found.get().getBadgeId());
        assertEquals("John Doe", found.get().getName());
    }

    @Test
    void testFindById_WithNonExistentId_ReturnsEmpty() {
        // Act
        Optional<Employee> found = employeeRepository.findById(999L);

        // Assert
        assertFalse(found.isPresent());
    }

    @Test
    void testFindById_WithNullId_ReturnsEmpty() {
        // Act
        Optional<Employee> found = employeeRepository.findById(null);

        // Assert
        assertFalse(found.isPresent());
    }

    // ========== FindAll Tests ==========

    @Test
    void testFindAll_WithMultipleEmployees_ReturnsAllEmployees() {
        // Arrange
        employeeRepository.save(testEmployee);

        Employee employee2 = new Employee();
        employee2.setBadgeId("EMP002");
        employee2.setName("Jane Doe");
        employee2.setRole("SUPERVISOR");
        employee2.setDepartment("Warehouse");
        employee2.setShiftGroup("B");
        employee2.setHireDate(LocalDate.of(2023, 2, 1));
        employee2.setStatus("ACTIVE");
        employee2.setDeleted(false);
        employeeRepository.save(employee2);

        entityManager.flush();
        entityManager.clear();

        // Act
        List<Employee> employees = employeeRepository.findAll();

        // Assert
        assertEquals(2, employees.size());
    }

    @Test
    void testFindAll_WithEmptyDatabase_ReturnsEmptyList() {
        // Act
        List<Employee> employees = employeeRepository.findAll();

        // Assert
        assertTrue(employees.isEmpty());
    }

    @Test
    void testFindAll_WithPageable_ReturnsPagedResults() {
        // Arrange
        for (int i = 1; i <= 15; i++) {
            Employee emp = new Employee();
            emp.setBadgeId("EMP" + String.format("%03d", i));
            emp.setName("Employee " + i);
            emp.setRole("WORKER");
            emp.setDepartment("Warehouse");
            emp.setShiftGroup("A");
            emp.setHireDate(LocalDate.of(2023, 1, 1));
            emp.setStatus("ACTIVE");
            emp.setDeleted(false);
            employeeRepository.save(emp);
        }
        entityManager.flush();
        entityManager.clear();

        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Employee> page = employeeRepository.findAll(pageable);

        // Assert
        assertEquals(10, page.getContent().size());
        assertEquals(15, page.getTotalElements());
        assertEquals(2, page.getTotalPages());
    }

    // ========== Custom Query Tests ==========

    @Test
    void testFindByBadgeId_WithExistingBadgeId_ReturnsEmployee() {
        // Arrange
        employeeRepository.save(testEmployee);
        entityManager.flush();
        entityManager.clear();

        // Act
        Optional<Employee> found = employeeRepository.findByBadgeId("EMP001");

        // Assert
        assertTrue(found.isPresent());
        assertEquals("John Doe", found.get().getName());
    }

    @Test
    void testFindByBadgeId_WithNonExistentBadgeId_ReturnsEmpty() {
        // Act
        Optional<Employee> found = employeeRepository.findByBadgeId("NONEXISTENT");

        // Assert
        assertFalse(found.isPresent());
    }

    @Test
    void testFindByDepartment_WithExistingDepartment_ReturnsEmployees() {
        // Arrange
        employeeRepository.save(testEmployee);

        Employee employee2 = new Employee();
        employee2.setBadgeId("EMP002");
        employee2.setName("Jane Doe");
        employee2.setRole("WORKER");
        employee2.setDepartment("Warehouse");
        employee2.setShiftGroup("B");
        employee2.setHireDate(LocalDate.of(2023, 2, 1));
        employee2.setStatus("ACTIVE");
        employee2.setDeleted(false);
        employeeRepository.save(employee2);

        entityManager.flush();
        entityManager.clear();

        // Act
        List<Employee> employees = employeeRepository.findByDepartment("Warehouse");

        // Assert
        assertEquals(2, employees.size());
    }

    @Test
    void testFindByDepartment_WithNonExistentDepartment_ReturnsEmptyList() {
        // Act
        List<Employee> employees = employeeRepository.findByDepartment("NonExistent");

        // Assert
        assertTrue(employees.isEmpty());
    }

    @Test
    void testFindByRole_WithExistingRole_ReturnsEmployees() {
        // Arrange
        employeeRepository.save(testEmployee);
        entityManager.flush();
        entityManager.clear();

        // Act
        List<Employee> employees = employeeRepository.findByRole("WORKER");

        // Assert
        assertEquals(1, employees.size());
        assertEquals("John Doe", employees.get(0).getName());
    }

    @Test
    void testExistsByBadgeId_WithExistingBadgeId_ReturnsTrue() {
        // Arrange
        employeeRepository.save(testEmployee);
        entityManager.flush();

        // Act
        boolean exists = employeeRepository.existsByBadgeId("EMP001");

        // Assert
        assertTrue(exists);
    }

    @Test
    void testExistsByBadgeId_WithNonExistentBadgeId_ReturnsFalse() {
        // Act
        boolean exists = employeeRepository.existsByBadgeId("NONEXISTENT");

        // Assert
        assertFalse(exists);
    }

    // ========== Update Tests ==========

    @Test
    void testUpdate_WithValidChanges_UpdatesEmployee() {
        // Arrange
        Employee saved = employeeRepository.save(testEmployee);
        entityManager.flush();
        entityManager.clear();

        // Act
        Employee toUpdate = employeeRepository.findById(saved.getId()).get();
        toUpdate.setName("Jane Doe");
        toUpdate.setRole("SUPERVISOR");
        Employee updated = employeeRepository.save(toUpdate);
        entityManager.flush();
        entityManager.clear();

        // Assert
        Employee found = employeeRepository.findById(saved.getId()).get();
        assertEquals("Jane Doe", found.getName());
        assertEquals("SUPERVISOR", found.getRole());
    }

    @Test
    void testUpdate_WithSoftDelete_UpdatesDeletedFlag() {
        // Arrange
        Employee saved = employeeRepository.save(testEmployee);
        entityManager.flush();
        entityManager.clear();

        // Act
        Employee toUpdate = employeeRepository.findById(saved.getId()).get();
        toUpdate.setDeleted(true);
        employeeRepository.save(toUpdate);
        entityManager.flush();
        entityManager.clear();

        // Assert
        Employee found = employeeRepository.findById(saved.getId()).get();
        assertTrue(found.isDeleted());
    }

    // ========== Delete Tests ==========

    @Test
    void testDelete_WithExistingEmployee_RemovesEmployee() {
        // Arrange
        Employee saved = employeeRepository.save(testEmployee);
        entityManager.flush();
        Long id = saved.getId();

        // Act
        employeeRepository.delete(saved);
        entityManager.flush();

        // Assert
        Optional<Employee> found = employeeRepository.findById(id);
        assertFalse(found.isPresent());
    }

    @Test
    void testDeleteById_WithExistingId_RemovesEmployee() {
        // Arrange
        Employee saved = employeeRepository.save(testEmployee);
        entityManager.flush();
        Long id = saved.getId();

        // Act
        employeeRepository.deleteById(id);
        entityManager.flush();

        // Assert
        Optional<Employee> found = employeeRepository.findById(id);
        assertFalse(found.isPresent());
    }

    @Test
    void testDeleteAll_WithMultipleEmployees_RemovesAllEmployees() {
        // Arrange
        employeeRepository.save(testEmployee);

        Employee employee2 = new Employee();
        employee2.setBadgeId("EMP002");
        employee2.setName("Jane Doe");
        employee2.setRole("WORKER");
        employee2.setDepartment("Warehouse");
        employee2.setShiftGroup("B");
        employee2.setHireDate(LocalDate.of(2023, 2, 1));
        employee2.setStatus("ACTIVE");
        employee2.setDeleted(false);
        employeeRepository.save(employee2);

        entityManager.flush();

        // Act
        employeeRepository.deleteAll();
        entityManager.flush();

        // Assert
        List<Employee> employees = employeeRepository.findAll();
        assertTrue(employees.isEmpty());
    }

    // ========== Edge Case Tests ==========

    @Test
    void testSave_WithMinimumRequiredFields_PersistsEmployee() {
        // Arrange
        Employee minimal = new Employee();
        minimal.setBadgeId("MIN001");
        minimal.setName("Minimal Employee");
        minimal.setRole("WORKER");
        minimal.setDepartment("Warehouse");
        minimal.setHireDate(LocalDate.now());
        minimal.setStatus("ACTIVE");
        minimal.setDeleted(false);

        // Act
        Employee saved = employeeRepository.save(minimal);
        entityManager.flush();

        // Assert
        assertNotNull(saved.getId());
        assertEquals("MIN001", saved.getBadgeId());
    }

    @Test
    void testFindAll_WithDeletedEmployees_ReturnsAllIncludingDeleted() {
        // Arrange
        employeeRepository.save(testEmployee);

        Employee deleted = new Employee();
        deleted.setBadgeId("EMP002");
        deleted.setName("Deleted Employee");
        deleted.setRole("WORKER");
        deleted.setDepartment("Warehouse");
        deleted.setShiftGroup("B");
        deleted.setHireDate(LocalDate.of(2023, 2, 1));
        deleted.setStatus("INACTIVE");
        deleted.setDeleted(true);
        employeeRepository.save(deleted);

        entityManager.flush();
        entityManager.clear();

        // Act
        List<Employee> employees = employeeRepository.findAll();

        // Assert
        assertEquals(2, employees.size());
    }

    @Test
    void testSave_WithBoundaryHireDate_PersistsEmployee() {
        // Arrange
        testEmployee.setHireDate(LocalDate.of(1900, 1, 1));

        // Act
        Employee saved = employeeRepository.save(testEmployee);
        entityManager.flush();

        // Assert
        assertNotNull(saved.getId());
        assertEquals(LocalDate.of(1900, 1, 1), saved.getHireDate());
    }

    @Test
    void testCount_WithMultipleEmployees_ReturnsCorrectCount() {
        // Arrange
        employeeRepository.save(testEmployee);

        Employee employee2 = new Employee();
        employee2.setBadgeId("EMP002");
        employee2.setName("Jane Doe");
        employee2.setRole("WORKER");
        employee2.setDepartment("Warehouse");
        employee2.setShiftGroup("B");
        employee2.setHireDate(LocalDate.of(2023, 2, 1));
        employee2.setStatus("ACTIVE");
        employee2.setDeleted(false);
        employeeRepository.save(employee2);

        entityManager.flush();

        // Act
        long count = employeeRepository.count();

        // Assert
        assertEquals(2, count);
    }
}