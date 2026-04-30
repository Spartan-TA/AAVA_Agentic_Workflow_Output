package com.warehouse.ems.employee.repository;

import com.warehouse.ems.employee.entity.Employee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive JUnit test suite for EmployeeRepository
 * Tests cover database operations, queries, pagination, and data integrity
 */
@DataJpaTest
@DisplayName("Employee Repository Tests")
public class EmployeeRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private EmployeeRepository employeeRepository;

    private Employee testEmployee1;
    private Employee testEmployee2;
    private Employee testEmployee3;

    @BeforeEach
    public void setUp() {
        // Setup test employee 1
        testEmployee1 = new Employee();
        testEmployee1.setBadgeId("EMP001");
        testEmployee1.setName("John Doe");
        testEmployee1.setRole("WORKER");
        testEmployee1.setDepartment("Warehouse");
        testEmployee1.setShiftGroup("Morning");
        testEmployee1.setHireDate(LocalDate.of(2023, 1, 15));
        testEmployee1.setStatus("ACTIVE");

        // Setup test employee 2
        testEmployee2 = new Employee();
        testEmployee2.setBadgeId("EMP002");
        testEmployee2.setName("Jane Smith");
        testEmployee2.setRole("SUPERVISOR");
        testEmployee2.setDepartment("Warehouse");
        testEmployee2.setShiftGroup("Evening");
        testEmployee2.setHireDate(LocalDate.of(2022, 6, 1));
        testEmployee2.setStatus("ACTIVE");

        // Setup test employee 3 (deleted)
        testEmployee3 = new Employee();
        testEmployee3.setBadgeId("EMP003");
        testEmployee3.setName("Bob Johnson");
        testEmployee3.setRole("WORKER");
        testEmployee3.setDepartment("Shipping");
        testEmployee3.setShiftGroup("Night");
        testEmployee3.setHireDate(LocalDate.of(2021, 3, 10));
        testEmployee3.setStatus("DELETED");
    }

    // ========== SAVE TESTS ==========

    @Test
    @DisplayName("Test save employee with valid data")
    public void testSaveEmployee_ValidData_Success() {
        // Act
        Employee saved = employeeRepository.save(testEmployee1);
        entityManager.flush();

        // Assert
        assertNotNull(saved.getId());
        assertEquals("EMP001", saved.getBadgeId());
        assertEquals("John Doe", saved.getName());
        assertEquals("ACTIVE", saved.getStatus());
        assertNotNull(saved.getCreatedAt());
        assertNotNull(saved.getUpdatedAt());
    }

    @Test
    @DisplayName("Test save employee with null badge ID throws exception")
    public void testSaveEmployee_NullBadgeId_ThrowsException() {
        // Arrange
        testEmployee1.setBadgeId(null);

        // Act & Assert
        assertThrows(Exception.class, () -> {
            employeeRepository.save(testEmployee1);
            entityManager.flush();
        });
    }

    @Test
    @DisplayName("Test save employee with duplicate badge ID throws exception")
    public void testSaveEmployee_DuplicateBadgeId_ThrowsException() {
        // Arrange
        employeeRepository.save(testEmployee1);
        entityManager.flush();

        Employee duplicate = new Employee();
        duplicate.setBadgeId("EMP001");
        duplicate.setName("Duplicate Employee");
        duplicate.setRole("WORKER");
        duplicate.setHireDate(LocalDate.now());

        // Act & Assert
        assertThrows(Exception.class, () -> {
            employeeRepository.save(duplicate);
            entityManager.flush();
        });
    }

    @Test
    @DisplayName("Test save employee with null name throws exception")
    public void testSaveEmployee_NullName_ThrowsException() {
        // Arrange
        testEmployee1.setName(null);

        // Act & Assert
        assertThrows(Exception.class, () -> {
            employeeRepository.save(testEmployee1);
            entityManager.flush();
        });
    }

    @Test
    @DisplayName("Test save employee with maximum length fields")
    public void testSaveEmployee_MaxLengthFields_Success() {
        // Arrange
        testEmployee1.setName("A".repeat(128));
        testEmployee1.setBadgeId("B".repeat(32));

        // Act
        Employee saved = employeeRepository.save(testEmployee1);
        entityManager.flush();

        // Assert
        assertNotNull(saved.getId());
        assertEquals(128, saved.getName().length());
        assertEquals(32, saved.getBadgeId().length());
    }

    // ========== FIND TESTS ==========

    @Test
    @DisplayName("Test find by badge ID with existing employee")
    public void testFindByBadgeId_ExistingEmployee_Success() {
        // Arrange
        employeeRepository.save(testEmployee1);
        entityManager.flush();

        // Act
        Optional<Employee> found = employeeRepository.findByBadgeId("EMP001");

        // Assert
        assertTrue(found.isPresent());
        assertEquals("John Doe", found.get().getName());
    }

    @Test
    @DisplayName("Test find by badge ID with non-existent employee")
    public void testFindByBadgeId_NonExistentEmployee_ReturnsEmpty() {
        // Act
        Optional<Employee> found = employeeRepository.findByBadgeId("NONEXISTENT");

        // Assert
        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("Test find by badge ID with null badge ID")
    public void testFindByBadgeId_NullBadgeId_ReturnsEmpty() {
        // Act
        Optional<Employee> found = employeeRepository.findByBadgeId(null);

        // Assert
        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("Test find by badge ID with empty string")
    public void testFindByBadgeId_EmptyString_ReturnsEmpty() {
        // Act
        Optional<Employee> found = employeeRepository.findByBadgeId("");

        // Assert
        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("Test exists by badge ID with existing employee")
    public void testExistsByBadgeId_ExistingEmployee_ReturnsTrue() {
        // Arrange
        employeeRepository.save(testEmployee1);
        entityManager.flush();

        // Act
        boolean exists = employeeRepository.existsByBadgeId("EMP001");

        // Assert
        assertTrue(exists);
    }

    @Test
    @DisplayName("Test exists by badge ID with non-existent employee")
    public void testExistsByBadgeId_NonExistentEmployee_ReturnsFalse() {
        // Act
        boolean exists = employeeRepository.existsByBadgeId("NONEXISTENT");

        // Assert
        assertFalse(exists);
    }

    // ========== PAGINATION TESTS ==========

    @Test
    @DisplayName("Test find all active employees with pagination")
    public void testFindAllActive_WithPagination_Success() {
        // Arrange
        employeeRepository.save(testEmployee1);
        employeeRepository.save(testEmployee2);
        employeeRepository.save(testEmployee3);
        entityManager.flush();

        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Employee> result = employeeRepository.findAllActive(pageable);

        // Assert
        assertEquals(2, result.getTotalElements());
        assertTrue(result.getContent().stream()
            .noneMatch(e -> "DELETED".equals(e.getStatus())));
    }

    @Test
    @DisplayName("Test find all by status with pagination")
    public void testFindAllByStatus_WithPagination_Success() {
        // Arrange
        employeeRepository.save(testEmployee1);
        employeeRepository.save(testEmployee2);
        employeeRepository.save(testEmployee3);
        entityManager.flush();

        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Employee> activeEmployees = employeeRepository.findAllByStatus("ACTIVE", pageable);
        Page<Employee> deletedEmployees = employeeRepository.findAllByStatus("DELETED", pageable);

        // Assert
        assertEquals(2, activeEmployees.getTotalElements());
        assertEquals(1, deletedEmployees.getTotalElements());
    }

    @Test
    @DisplayName("Test find all by department and status with pagination")
    public void testFindAllByDepartmentAndStatus_WithPagination_Success() {
        // Arrange
        employeeRepository.save(testEmployee1);
        employeeRepository.save(testEmployee2);
        employeeRepository.save(testEmployee3);
        entityManager.flush();

        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Employee> warehouseEmployees = employeeRepository
            .findAllByDepartmentAndStatus("Warehouse", "ACTIVE", pageable);
        Page<Employee> shippingEmployees = employeeRepository
            .findAllByDepartmentAndStatus("Shipping", "DELETED", pageable);

        // Assert
        assertEquals(2, warehouseEmployees.getTotalElements());
        assertEquals(1, shippingEmployees.getTotalElements());
    }

    @Test
    @DisplayName("Test pagination with empty result")
    public void testFindAllActive_EmptyResult_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Employee> result = employeeRepository.findAllActive(pageable);

        // Assert
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
    }

    @Test
    @DisplayName("Test pagination with large page size")
    public void testFindAllActive_LargePageSize_Success() {
        // Arrange
        for (int i = 1; i <= 100; i++) {
            Employee emp = new Employee();
            emp.setBadgeId("EMP" + String.format("%03d", i));
            emp.setName("Employee " + i);
            emp.setRole("WORKER");
            emp.setDepartment("Warehouse");
            emp.setHireDate(LocalDate.now());
            emp.setStatus("ACTIVE");
            employeeRepository.save(emp);
        }
        entityManager.flush();

        Pageable pageable = PageRequest.of(0, 1000);

        // Act
        Page<Employee> result = employeeRepository.findAllActive(pageable);

        // Assert
        assertEquals(100, result.getTotalElements());
    }

    // ========== UPDATE TESTS ==========

    @Test
    @DisplayName("Test update employee fields")
    public void testUpdateEmployee_ValidData_Success() {
        // Arrange
        Employee saved = employeeRepository.save(testEmployee1);
        entityManager.flush();
        entityManager.clear();

        // Act
        Employee toUpdate = employeeRepository.findById(saved.getId()).get();
        toUpdate.setName("Updated Name");
        toUpdate.setRole("SUPERVISOR");
        Employee updated = employeeRepository.save(toUpdate);
        entityManager.flush();

        // Assert
        assertEquals("Updated Name", updated.getName());
        assertEquals("SUPERVISOR", updated.getRole());
        assertNotEquals(saved.getUpdatedAt(), updated.getUpdatedAt());
    }

    @Test
    @DisplayName("Test soft delete employee")
    public void testSoftDeleteEmployee_Success() {
        // Arrange
        Employee saved = employeeRepository.save(testEmployee1);
        entityManager.flush();

        // Act
        saved.setStatus("DELETED");
        Employee deleted = employeeRepository.save(saved);
        entityManager.flush();

        // Assert
        assertEquals("DELETED", deleted.getStatus());
        assertTrue(employeeRepository.findById(deleted.getId()).isPresent());
    }

    // ========== DELETE TESTS ==========

    @Test
    @DisplayName("Test hard delete employee")
    public void testHardDeleteEmployee_Success() {
        // Arrange
        Employee saved = employeeRepository.save(testEmployee1);
        entityManager.flush();
        Long id = saved.getId();

        // Act
        employeeRepository.deleteById(id);
        entityManager.flush();

        // Assert
        assertFalse(employeeRepository.findById(id).isPresent());
    }

    // ========== BOUNDARY CONDITION TESTS ==========

    @Test
    @DisplayName("Test save employee with special characters in name")
    public void testSaveEmployee_SpecialCharactersInName_Success() {
        // Arrange
        testEmployee1.setName("O'Brien-Smith Jr.");

        // Act
        Employee saved = employeeRepository.save(testEmployee1);
        entityManager.flush();

        // Assert
        assertEquals("O'Brien-Smith Jr.", saved.getName());
    }

    @Test
    @DisplayName("Test save employee with future hire date")
    public void testSaveEmployee_FutureHireDate_Success() {
        // Arrange
        testEmployee1.setHireDate(LocalDate.now().plusDays(30));

        // Act
        Employee saved = employeeRepository.save(testEmployee1);
        entityManager.flush();

        // Assert
        assertTrue(saved.getHireDate().isAfter(LocalDate.now()));
    }

    @Test
    @DisplayName("Test save employee with past hire date")
    public void testSaveEmployee_PastHireDate_Success() {
        // Arrange
        testEmployee1.setHireDate(LocalDate.of(1990, 1, 1));

        // Act
        Employee saved = employeeRepository.save(testEmployee1);
        entityManager.flush();

        // Assert
        assertTrue(saved.getHireDate().isBefore(LocalDate.now()));
    }

    @Test
    @DisplayName("Test find all with multiple filters")
    public void testFindAll_MultipleFilters_Success() {
        // Arrange
        employeeRepository.save(testEmployee1);
        employeeRepository.save(testEmployee2);
        employeeRepository.save(testEmployee3);
        entityManager.flush();

        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Employee> result = employeeRepository
            .findAllByDepartmentAndStatus("Warehouse", "ACTIVE", pageable);

        // Assert
        assertEquals(2, result.getTotalElements());
        assertTrue(result.getContent().stream()
            .allMatch(e -> "Warehouse".equals(e.getDepartment()) && "ACTIVE".equals(e.getStatus())));
    }
}