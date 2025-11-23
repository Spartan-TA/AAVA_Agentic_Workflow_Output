package com.warehouse.ems.employee.repository;

import com.warehouse.ems.employee.entity.Employee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
 * Integration tests for EmployeeRepository
 * Tests JPA repository methods and custom queries
 */
@DataJpaTest
public class EmployeeRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private EmployeeRepository employeeRepository;

    private Employee testEmployee;

    @BeforeEach
    void setUp() {
        testEmployee = new Employee();
        testEmployee.setBadgeId("EMP001");
        testEmployee.setName("John Doe");
        testEmployee.setRole("WORKER");
        testEmployee.setDepartment("Warehouse");
        testEmployee.setShiftGroup("A");
        testEmployee.setHireDate(LocalDate.of(2023, 1, 15));
        testEmployee.setStatus("ACTIVE");
        testEmployee.setWarehouseId(1L);
        testEmployee.setDeleted(false);
    }

    // ========== SAVE TESTS ==========

    @Test
    void testSave_ValidEmployee_Success() {
        // Act
        Employee saved = employeeRepository.save(testEmployee);

        // Assert
        assertNotNull(saved.getId());
        assertEquals("EMP001", saved.getBadgeId());
        assertEquals("John Doe", saved.getName());
    }

    @Test
    void testSave_DuplicateBadgeId_ThrowsException() {
        // Arrange
        employeeRepository.save(testEmployee);
        Employee duplicate = new Employee();
        duplicate.setBadgeId("EMP001");
        duplicate.setName("Jane Doe");
        duplicate.setRole("WORKER");
        duplicate.setDepartment("Warehouse");
        duplicate.setHireDate(LocalDate.now());
        duplicate.setStatus("ACTIVE");

        // Act & Assert
        assertThrows(Exception.class, () -> {
            employeeRepository.save(duplicate);
            entityManager.flush();
        });
    }

    @Test
    void testSave_NullBadgeId_ThrowsException() {
        // Arrange
        testEmployee.setBadgeId(null);

        // Act & Assert
        assertThrows(Exception.class, () -> {
            employeeRepository.save(testEmployee);
            entityManager.flush();
        });
    }

    // ========== FIND BY ID TESTS ==========

    @Test
    void testFindById_ExistingEmployee_ReturnsEmployee() {
        // Arrange
        Employee saved = entityManager.persistAndFlush(testEmployee);

        // Act
        Optional<Employee> found = employeeRepository.findById(saved.getId());

        // Assert
        assertTrue(found.isPresent());
        assertEquals("EMP001", found.get().getBadgeId());
    }

    @Test
    void testFindById_NonExistentId_ReturnsEmpty() {
        // Act
        Optional<Employee> found = employeeRepository.findById(999L);

        // Assert
        assertFalse(found.isPresent());
    }

    @Test
    void testFindById_DeletedEmployee_ReturnsEmployee() {
        // Arrange
        testEmployee.setDeleted(true);
        Employee saved = entityManager.persistAndFlush(testEmployee);

        // Act
        Optional<Employee> found = employeeRepository.findById(saved.getId());

        // Assert
        assertTrue(found.isPresent());
        assertTrue(found.get().isDeleted());
    }

    // ========== FIND BY BADGE ID TESTS ==========

    @Test
    void testFindByBadgeId_ExistingBadgeId_ReturnsEmployee() {
        // Arrange
        entityManager.persistAndFlush(testEmployee);

        // Act
        Optional<Employee> found = employeeRepository.findByBadgeId("EMP001");

        // Assert
        assertTrue(found.isPresent());
        assertEquals("John Doe", found.get().getName());
    }

    @Test
    void testFindByBadgeId_NonExistentBadgeId_ReturnsEmpty() {
        // Act
        Optional<Employee> found = employeeRepository.findByBadgeId("NONEXISTENT");

        // Assert
        assertFalse(found.isPresent());
    }

    @Test
    void testFindByBadgeId_NullBadgeId_ReturnsEmpty() {
        // Act
        Optional<Employee> found = employeeRepository.findByBadgeId(null);

        // Assert
        assertFalse(found.isPresent());
    }

    @Test
    void testFindByBadgeId_EmptyBadgeId_ReturnsEmpty() {
        // Act
        Optional<Employee> found = employeeRepository.findByBadgeId("");

        // Assert
        assertFalse(found.isPresent());
    }

    // ========== FIND ALL ACTIVE TESTS ==========

    @Test
    void testFindAllByDeletedFalse_MultipleActiveEmployees_ReturnsAll() {
        // Arrange
        entityManager.persistAndFlush(testEmployee);
        Employee employee2 = new Employee();
        employee2.setBadgeId("EMP002");
        employee2.setName("Jane Doe");
        employee2.setRole("SUPERVISOR");
        employee2.setDepartment("Warehouse");
        employee2.setHireDate(LocalDate.now());
        employee2.setStatus("ACTIVE");
        employee2.setDeleted(false);
        entityManager.persistAndFlush(employee2);

        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Employee> result = employeeRepository.findAllByDeletedFalse(pageable);

        // Assert
        assertEquals(2, result.getTotalElements());
    }

    @Test
    void testFindAllByDeletedFalse_ExcludesDeletedEmployees() {
        // Arrange
        entityManager.persistAndFlush(testEmployee);
        Employee deletedEmployee = new Employee();
        deletedEmployee.setBadgeId("EMP002");
        deletedEmployee.setName("Deleted Employee");
        deletedEmployee.setRole("WORKER");
        deletedEmployee.setDepartment("Warehouse");
        deletedEmployee.setHireDate(LocalDate.now());
        deletedEmployee.setStatus("INACTIVE");
        deletedEmployee.setDeleted(true);
        entityManager.persistAndFlush(deletedEmployee);

        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Employee> result = employeeRepository.findAllByDeletedFalse(pageable);

        // Assert
        assertEquals(1, result.getTotalElements());
        assertEquals("EMP001", result.getContent().get(0).getBadgeId());
    }

    @Test
    void testFindAllByDeletedFalse_EmptyDatabase_ReturnsEmptyPage() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Employee> result = employeeRepository.findAllByDeletedFalse(pageable);

        // Assert
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
    }

    @Test
    void testFindAllByDeletedFalse_Pagination_ReturnsCorrectPage() {
        // Arrange
        for (int i = 1; i <= 15; i++) {
            Employee emp = new Employee();
            emp.setBadgeId("EMP" + String.format("%03d", i));
            emp.setName("Employee " + i);
            emp.setRole("WORKER");
            emp.setDepartment("Warehouse");
            emp.setHireDate(LocalDate.now());
            emp.setStatus("ACTIVE");
            emp.setDeleted(false);
            entityManager.persist(emp);
        }
        entityManager.flush();

        Pageable firstPage = PageRequest.of(0, 10);
        Pageable secondPage = PageRequest.of(1, 10);

        // Act
        Page<Employee> firstResult = employeeRepository.findAllByDeletedFalse(firstPage);
        Page<Employee> secondResult = employeeRepository.findAllByDeletedFalse(secondPage);

        // Assert
        assertEquals(15, firstResult.getTotalElements());
        assertEquals(10, firstResult.getContent().size());
        assertEquals(5, secondResult.getContent().size());
    }

    // ========== EXISTS BY BADGE ID TESTS ==========

    @Test
    void testExistsByBadgeIdAndDeletedFalse_ExistingActiveBadgeId_ReturnsTrue() {
        // Arrange
        entityManager.persistAndFlush(testEmployee);

        // Act
        boolean exists = employeeRepository.existsByBadgeIdAndDeletedFalse("EMP001");

        // Assert
        assertTrue(exists);
    }

    @Test
    void testExistsByBadgeIdAndDeletedFalse_DeletedBadgeId_ReturnsFalse() {
        // Arrange
        testEmployee.setDeleted(true);
        entityManager.persistAndFlush(testEmployee);

        // Act
        boolean exists = employeeRepository.existsByBadgeIdAndDeletedFalse("EMP001");

        // Assert
        assertFalse(exists);
    }

    @Test
    void testExistsByBadgeIdAndDeletedFalse_NonExistentBadgeId_ReturnsFalse() {
        // Act
        boolean exists = employeeRepository.existsByBadgeIdAndDeletedFalse("NONEXISTENT");

        // Assert
        assertFalse(exists);
    }

    @Test
    void testExistsByBadgeIdAndDeletedFalse_NullBadgeId_ReturnsFalse() {
        // Act
        boolean exists = employeeRepository.existsByBadgeIdAndDeletedFalse(null);

        // Assert
        assertFalse(exists);
    }

    // ========== DELETE TESTS ==========

    @Test
    void testDelete_ExistingEmployee_Success() {
        // Arrange
        Employee saved = entityManager.persistAndFlush(testEmployee);
        Long id = saved.getId();

        // Act
        employeeRepository.delete(saved);
        entityManager.flush();

        // Assert
        Optional<Employee> found = employeeRepository.findById(id);
        assertFalse(found.isPresent());
    }

    @Test
    void testDeleteById_ExistingEmployee_Success() {
        // Arrange
        Employee saved = entityManager.persistAndFlush(testEmployee);
        Long id = saved.getId();

        // Act
        employeeRepository.deleteById(id);
        entityManager.flush();

        // Assert
        Optional<Employee> found = employeeRepository.findById(id);
        assertFalse(found.isPresent());
    }

    // ========== UPDATE TESTS ==========

    @Test
    void testUpdate_ExistingEmployee_Success() {
        // Arrange
        Employee saved = entityManager.persistAndFlush(testEmployee);
        saved.setName("Updated Name");
        saved.setDepartment("Updated Department");

        // Act
        Employee updated = employeeRepository.save(saved);
        entityManager.flush();

        // Assert
        assertEquals("Updated Name", updated.getName());
        assertEquals("Updated Department", updated.getDepartment());
    }

    @Test
    void testUpdate_SoftDelete_Success() {
        // Arrange
        Employee saved = entityManager.persistAndFlush(testEmployee);
        saved.setDeleted(true);

        // Act
        Employee updated = employeeRepository.save(saved);
        entityManager.flush();

        // Assert
        assertTrue(updated.isDeleted());
        Optional<Employee> found = employeeRepository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertTrue(found.get().isDeleted());
    }

    // ========== BOUNDARY CONDITION TESTS ==========

    @Test
    void testSave_MaxLengthFields_Success() {
        // Arrange
        testEmployee.setName("A".repeat(255));
        testEmployee.setDepartment("B".repeat(100));

        // Act
        Employee saved = employeeRepository.save(testEmployee);
        entityManager.flush();

        // Assert
        assertNotNull(saved.getId());
        assertEquals(255, saved.getName().length());
    }

    @Test
    void testFindAllByDeletedFalse_LargePageSize_Success() {
        // Arrange
        for (int i = 1; i <= 5; i++) {
            Employee emp = new Employee();
            emp.setBadgeId("EMP" + String.format("%03d", i));
            emp.setName("Employee " + i);
            emp.setRole("WORKER");
            emp.setDepartment("Warehouse");
            emp.setHireDate(LocalDate.now());
            emp.setStatus("ACTIVE");
            emp.setDeleted(false);
            entityManager.persist(emp);
        }
        entityManager.flush();

        Pageable pageable = PageRequest.of(0, 1000);

        // Act
        Page<Employee> result = employeeRepository.findAllByDeletedFalse(pageable);

        // Assert
        assertEquals(5, result.getTotalElements());
    }

    @Test
    void testSave_MinimalRequiredFields_Success() {
        // Arrange
        Employee minimal = new Employee();
        minimal.setBadgeId("MIN001");
        minimal.setName("Minimal Employee");
        minimal.setRole("WORKER");
        minimal.setDepartment("Warehouse");
        minimal.setHireDate(LocalDate.now());
        minimal.setStatus("ACTIVE");

        // Act
        Employee saved = employeeRepository.save(minimal);
        entityManager.flush();

        // Assert
        assertNotNull(saved.getId());
        assertNull(saved.getShiftGroup());
        assertNull(saved.getWarehouseId());
    }
}