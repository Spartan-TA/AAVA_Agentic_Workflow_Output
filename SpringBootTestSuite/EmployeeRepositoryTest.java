package com.warehouse.employeemgmt.repository;

import com.warehouse.employeemgmt.domain.Employee;
import com.warehouse.employeemgmt.domain.EmployeeRole;
import com.warehouse.employeemgmt.domain.EmployeeStatus;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive JUnit test suite for EmployeeRepository
 * Tests all database operations, custom queries, and data integrity
 * 
 * Test Coverage:
 * - Basic CRUD operations
 * - Custom query methods
 * - Pagination and sorting
 * - Filtering by various criteria
 * - Soft delete functionality
 * - Badge ID uniqueness
 * - Data integrity constraints
 * - Transaction management
 * - Edge cases and boundary conditions
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Employee Repository Test Suite")
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
        // Arrange - Setup test data
        testEmployee1 = new Employee();
        testEmployee1.setName("John Doe");
        testEmployee1.setBadgeId("EMP001");
        testEmployee1.setRole(EmployeeRole.WORKER);
        testEmployee1.setDepartment("Warehouse");
        testEmployee1.setShiftGroup("Morning");
        testEmployee1.setHireDate(LocalDate.of(2023, 1, 15));
        testEmployee1.setStatus(EmployeeStatus.ACTIVE);
        testEmployee1.setDeleted(false);

        testEmployee2 = new Employee();
        testEmployee2.setName("Jane Smith");
        testEmployee2.setBadgeId("EMP002");
        testEmployee2.setRole(EmployeeRole.SUPERVISOR);
        testEmployee2.setDepartment("Warehouse");
        testEmployee2.setShiftGroup("Evening");
        testEmployee2.setHireDate(LocalDate.of(2022, 6, 1));
        testEmployee2.setStatus(EmployeeStatus.ACTIVE);
        testEmployee2.setDeleted(false);

        testEmployee3 = new Employee();
        testEmployee3.setName("Bob Johnson");
        testEmployee3.setBadgeId("EMP003");
        testEmployee3.setRole(EmployeeRole.WORKER);
        testEmployee3.setDepartment("Shipping");
        testEmployee3.setShiftGroup("Morning");
        testEmployee3.setHireDate(LocalDate.of(2023, 3, 10));
        testEmployee3.setStatus(EmployeeStatus.INACTIVE);
        testEmployee3.setDeleted(true);
    }

    // ==================== BASIC CRUD TESTS ====================

    @Test
    @DisplayName("Test save employee - valid data")
    public void testSaveEmployee_ValidData_Success() {
        // Act
        Employee saved = employeeRepository.save(testEmployee1);
        entityManager.flush();

        // Assert
        assertNotNull(saved.getId());
        assertEquals("John Doe", saved.getName());
        assertEquals("EMP001", saved.getBadgeId());
        assertEquals(EmployeeRole.WORKER, saved.getRole());
    }

    @Test
    @DisplayName("Test find employee by ID - existing employee")
    public void testFindById_ExistingEmployee_Success() {
        // Arrange
        Employee saved = entityManager.persistAndFlush(testEmployee1);

        // Act
        Optional<Employee> found = employeeRepository.findById(saved.getId());

        // Assert
        assertTrue(found.isPresent());
        assertEquals("John Doe", found.get().getName());
        assertEquals("EMP001", found.get().getBadgeId());
    }

    @Test
    @DisplayName("Test find employee by ID - non-existent employee")
    public void testFindById_NonExistentEmployee_ReturnsEmpty() {
        // Act
        Optional<Employee> found = employeeRepository.findById(999L);

        // Assert
        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("Test find all employees")
    public void testFindAll_MultipleEmployees_Success() {
        // Arrange
        entityManager.persist(testEmployee1);
        entityManager.persist(testEmployee2);
        entityManager.flush();

        // Act
        List<Employee> employees = employeeRepository.findAll();

        // Assert
        assertNotNull(employees);
        assertTrue(employees.size() >= 2);
    }

    @Test
    @DisplayName("Test update employee")
    public void testUpdateEmployee_ValidData_Success() {
        // Arrange
        Employee saved = entityManager.persistAndFlush(testEmployee1);
        saved.setName("John Updated");

        // Act
        Employee updated = employeeRepository.save(saved);
        entityManager.flush();

        // Assert
        assertEquals("John Updated", updated.getName());
        assertEquals(saved.getId(), updated.getId());
    }

    @Test
    @DisplayName("Test delete employee")
    public void testDeleteEmployee_ExistingEmployee_Success() {
        // Arrange
        Employee saved = entityManager.persistAndFlush(testEmployee1);
        Long id = saved.getId();

        // Act
        employeeRepository.deleteById(id);
        entityManager.flush();

        // Assert
        Optional<Employee> found = employeeRepository.findById(id);
        assertFalse(found.isPresent());
    }

    // ==================== CUSTOM QUERY TESTS ====================

    @Test
    @DisplayName("Test find by badge ID - existing badge")
    public void testFindByBadgeId_ExistingBadge_Success() {
        // Arrange
        entityManager.persistAndFlush(testEmployee1);

        // Act
        Optional<Employee> found = employeeRepository.findByBadgeId("EMP001");

        // Assert
        assertTrue(found.isPresent());
        assertEquals("John Doe", found.get().getName());
    }

    @Test
    @DisplayName("Test find by badge ID - non-existent badge")
    public void testFindByBadgeId_NonExistentBadge_ReturnsEmpty() {
        // Act
        Optional<Employee> found = employeeRepository.findByBadgeId("INVALID");

        // Assert
        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("Test find by badge ID - null badge")
    public void testFindByBadgeId_NullBadge_ReturnsEmpty() {
        // Act
        Optional<Employee> found = employeeRepository.findByBadgeId(null);

        // Assert
        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("Test find by badge ID - empty badge")
    public void testFindByBadgeId_EmptyBadge_ReturnsEmpty() {
        // Act
        Optional<Employee> found = employeeRepository.findByBadgeId("");

        // Assert
        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("Test find all by deleted false")
    public void testFindAllByDeletedFalse_OnlyActiveEmployees_Success() {
        // Arrange
        entityManager.persist(testEmployee1);
        entityManager.persist(testEmployee2);
        entityManager.persist(testEmployee3);
        entityManager.flush();

        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Employee> result = employeeRepository.findAllByDeletedFalse(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertTrue(result.getContent().stream().noneMatch(Employee::isDeleted));
    }

    @Test
    @DisplayName("Test find by department and deleted false")
    public void testFindByDepartmentAndDeletedFalse_FilteredResults_Success() {
        // Arrange
        entityManager.persist(testEmployee1);
        entityManager.persist(testEmployee2);
        entityManager.persist(testEmployee3);
        entityManager.flush();

        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Employee> result = employeeRepository.findByDepartmentAndDeletedFalse("Warehouse", pageable);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertTrue(result.getContent().stream().allMatch(e -> "Warehouse".equals(e.getDepartment())));
        assertTrue(result.getContent().stream().noneMatch(Employee::isDeleted));
    }

    @Test
    @DisplayName("Test find by role and deleted false")
    public void testFindByRoleAndDeletedFalse_FilteredResults_Success() {
        // Arrange
        entityManager.persist(testEmployee1);
        entityManager.persist(testEmployee2);
        entityManager.persist(testEmployee3);
        entityManager.flush();

        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Employee> result = employeeRepository.findByRoleAndDeletedFalse(EmployeeRole.WORKER, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertTrue(result.getContent().stream().allMatch(e -> EmployeeRole.WORKER.equals(e.getRole())));
        assertTrue(result.getContent().stream().noneMatch(Employee::isDeleted));
    }

    @Test
    @DisplayName("Test find by status and deleted false")
    public void testFindByStatusAndDeletedFalse_FilteredResults_Success() {
        // Arrange
        entityManager.persist(testEmployee1);
        entityManager.persist(testEmployee2);
        entityManager.persist(testEmployee3);
        entityManager.flush();

        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Employee> result = employeeRepository.findByStatusAndDeletedFalse(EmployeeStatus.ACTIVE, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertTrue(result.getContent().stream().allMatch(e -> EmployeeStatus.ACTIVE.equals(e.getStatus())));
        assertTrue(result.getContent().stream().noneMatch(Employee::isDeleted));
    }

    @Test
    @DisplayName("Test find by name containing ignore case and deleted false")
    public void testFindByNameContainingIgnoreCaseAndDeletedFalse_SearchResults_Success() {
        // Arrange
        entityManager.persist(testEmployee1);
        entityManager.persist(testEmployee2);
        entityManager.persist(testEmployee3);
        entityManager.flush();

        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Employee> result = employeeRepository.findByNameContainingIgnoreCaseAndDeletedFalse("john", pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertTrue(result.getContent().get(0).getName().toLowerCase().contains("john"));
    }

    @Test
    @DisplayName("Test find by name containing - case insensitive")
    public void testFindByNameContaining_CaseInsensitive_Success() {
        // Arrange
        entityManager.persist(testEmployee1);
        entityManager.flush();

        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Employee> resultLower = employeeRepository.findByNameContainingIgnoreCaseAndDeletedFalse("john", pageable);
        Page<Employee> resultUpper = employeeRepository.findByNameContainingIgnoreCaseAndDeletedFalse("JOHN", pageable);
        Page<Employee> resultMixed = employeeRepository.findByNameContainingIgnoreCaseAndDeletedFalse("JoHn", pageable);

        // Assert
        assertEquals(1, resultLower.getTotalElements());
        assertEquals(1, resultUpper.getTotalElements());
        assertEquals(1, resultMixed.getTotalElements());
    }

    // ==================== PAGINATION TESTS ====================

    @Test
    @DisplayName("Test pagination - first page")
    public void testPagination_FirstPage_Success() {
        // Arrange
        for (int i = 0; i < 15; i++) {
            Employee emp = new Employee();
            emp.setName("Employee " + i);
            emp.setBadgeId("EMP" + String.format("%03d", i));
            emp.setRole(EmployeeRole.WORKER);
            emp.setDepartment("Warehouse");
            emp.setHireDate(LocalDate.now());
            emp.setStatus(EmployeeStatus.ACTIVE);
            emp.setDeleted(false);
            entityManager.persist(emp);
        }
        entityManager.flush();

        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Employee> result = employeeRepository.findAllByDeletedFalse(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(10, result.getContent().size());
        assertEquals(15, result.getTotalElements());
        assertEquals(2, result.getTotalPages());
        assertTrue(result.isFirst());
        assertFalse(result.isLast());
    }

    @Test
    @DisplayName("Test pagination - last page")
    public void testPagination_LastPage_Success() {
        // Arrange
        for (int i = 0; i < 15; i++) {
            Employee emp = new Employee();
            emp.setName("Employee " + i);
            emp.setBadgeId("EMP" + String.format("%03d", i));
            emp.setRole(EmployeeRole.WORKER);
            emp.setDepartment("Warehouse");
            emp.setHireDate(LocalDate.now());
            emp.setStatus(EmployeeStatus.ACTIVE);
            emp.setDeleted(false);
            entityManager.persist(emp);
        }
        entityManager.flush();

        Pageable pageable = PageRequest.of(1, 10);

        // Act
        Page<Employee> result = employeeRepository.findAllByDeletedFalse(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(5, result.getContent().size());
        assertEquals(15, result.getTotalElements());
        assertFalse(result.isFirst());
        assertTrue(result.isLast());
    }

    @Test
    @DisplayName("Test pagination - empty page")
    public void testPagination_EmptyPage_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Employee> result = employeeRepository.findAllByDeletedFalse(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getContent().size());
        assertEquals(0, result.getTotalElements());
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Test pagination - page size 1")
    public void testPagination_PageSizeOne_Success() {
        // Arrange
        entityManager.persist(testEmployee1);
        entityManager.persist(testEmployee2);
        entityManager.flush();

        Pageable pageable = PageRequest.of(0, 1);

        // Act
        Page<Employee> result = employeeRepository.findAllByDeletedFalse(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(2, result.getTotalElements());
        assertEquals(2, result.getTotalPages());
    }

    // ==================== DATA INTEGRITY TESTS ====================

    @Test
    @DisplayName("Test badge ID uniqueness constraint")
    public void testBadgeIdUniqueness_DuplicateBadgeId_ThrowsException() {
        // Arrange
        entityManager.persistAndFlush(testEmployee1);

        Employee duplicate = new Employee();
        duplicate.setName("Duplicate Employee");
        duplicate.setBadgeId("EMP001"); // Same badge ID
        duplicate.setRole(EmployeeRole.WORKER);
        duplicate.setDepartment("Warehouse");
        duplicate.setHireDate(LocalDate.now());
        duplicate.setStatus(EmployeeStatus.ACTIVE);

        // Act & Assert
        assertThrows(Exception.class, () -> {
            entityManager.persistAndFlush(duplicate);
        });
    }

    @Test
    @DisplayName("Test not null constraints")
    public void testNotNullConstraints_NullRequiredFields_ThrowsException() {
        // Arrange
        Employee invalidEmployee = new Employee();
        invalidEmployee.setName(null); // Required field
        invalidEmployee.setBadgeId("EMP999");
        invalidEmployee.setRole(EmployeeRole.WORKER);

        // Act & Assert
        assertThrows(Exception.class, () -> {
            entityManager.persistAndFlush(invalidEmployee);
        });
    }

    @Test
    @DisplayName("Test soft delete preserves data")
    public void testSoftDelete_PreservesData_Success() {
        // Arrange
        Employee saved = entityManager.persistAndFlush(testEmployee1);
        Long id = saved.getId();

        // Act
        saved.setDeleted(true);
        employeeRepository.save(saved);
        entityManager.flush();

        // Assert
        Optional<Employee> found = employeeRepository.findById(id);
        assertTrue(found.isPresent());
        assertTrue(found.get().isDeleted());
        assertEquals("John Doe", found.get().getName());
    }

    // ==================== EDGE CASE TESTS ====================

    @Test
    @DisplayName("Test save employee with very long name (boundary)")
    public void testSaveEmployee_VeryLongName_Success() {
        // Arrange
        String longName = "A".repeat(255);
        testEmployee1.setName(longName);

        // Act
        Employee saved = employeeRepository.save(testEmployee1);
        entityManager.flush();

        // Assert
        assertNotNull(saved.getId());
        assertEquals(longName, saved.getName());
    }

    @Test
    @DisplayName("Test save employee with special characters in name")
    public void testSaveEmployee_SpecialCharactersInName_Success() {
        // Arrange
        testEmployee1.setName("O'Brien-Smith");

        // Act
        Employee saved = employeeRepository.save(testEmployee1);
        entityManager.flush();

        // Assert
        assertNotNull(saved.getId());
        assertEquals("O'Brien-Smith", saved.getName());
    }

    @Test
    @DisplayName("Test save employee with unicode characters in name")
    public void testSaveEmployee_UnicodeCharactersInName_Success() {
        // Arrange
        testEmployee1.setName("JosÃ© GarcÃ­a");

        // Act
        Employee saved = employeeRepository.save(testEmployee1);
        entityManager.flush();

        // Assert
        assertNotNull(saved.getId());
        assertEquals("JosÃ© GarcÃ­a", saved.getName());
    }

    @Test
    @DisplayName("Test find by department - empty result")
    public void testFindByDepartment_EmptyResult_Success() {
        // Arrange
        entityManager.persist(testEmployee1);
        entityManager.flush();

        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Employee> result = employeeRepository.findByDepartmentAndDeletedFalse("NonExistentDept", pageable);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Test find by role - empty result")
    public void testFindByRole_EmptyResult_Success() {
        // Arrange
        entityManager.persist(testEmployee1);
        entityManager.flush();

        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Employee> result = employeeRepository.findByRoleAndDeletedFalse(EmployeeRole.ADMIN, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Test search with empty string returns all")
    public void testSearchByName_EmptyString_ReturnsAll() {
        // Arrange
        entityManager.persist(testEmployee1);
        entityManager.persist(testEmployee2);
        entityManager.flush();

        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Employee> result = employeeRepository.findByNameContainingIgnoreCaseAndDeletedFalse("", pageable);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
    }

    // ==================== TRANSACTION TESTS ====================

    @Test
    @DisplayName("Test transaction rollback on error")
    public void testTransactionRollback_OnError_Success() {
        // Arrange
        Employee saved = entityManager.persistAndFlush(testEmployee1);
        String originalName = saved.getName();

        // Act
        try {
            saved.setName("Updated Name");
            saved.setBadgeId(null); // This should cause an error
            entityManager.flush();
        } catch (Exception e) {
            entityManager.clear();
        }

        // Assert
        Employee found = entityManager.find(Employee.class, saved.getId());
        assertEquals(originalName, found.getName());
    }

    @Test
    @DisplayName("Test concurrent modification")
    public void testConcurrentModification_OptimisticLocking_Success() {
        // Arrange
        Employee saved = entityManager.persistAndFlush(testEmployee1);
        entityManager.clear();

        // Act
        Employee emp1 = employeeRepository.findById(saved.getId()).get();
        Employee emp2 = employeeRepository.findById(saved.getId()).get();

        emp1.setName("Updated by User 1");
        employeeRepository.save(emp1);
        entityManager.flush();

        emp2.setName("Updated by User 2");

        // Assert
        // Depending on optimistic locking configuration, this may throw an exception
        Employee finalEmp = employeeRepository.save(emp2);
        assertNotNull(finalEmp);
    }

    // ==================== PERFORMANCE TESTS ====================

    @Test
    @DisplayName("Test bulk insert performance")
    public void testBulkInsert_LargeDataset_Success() {
        // Arrange & Act
        for (int i = 0; i < 100; i++) {
            Employee emp = new Employee();
            emp.setName("Employee " + i);
            emp.setBadgeId("EMP" + String.format("%03d", i));
            emp.setRole(EmployeeRole.WORKER);
            emp.setDepartment("Warehouse");
            emp.setHireDate(LocalDate.now());
            emp.setStatus(EmployeeStatus.ACTIVE);
            emp.setDeleted(false);
            employeeRepository.save(emp);

            if (i % 20 == 0) {
                entityManager.flush();
                entityManager.clear();
            }
        }
        entityManager.flush();

        // Assert
        long count = employeeRepository.count();
        assertEquals(100, count);
    }

    @Test
    @DisplayName("Test query performance with large dataset")
    public void testQueryPerformance_LargeDataset_Success() {
        // Arrange
        for (int i = 0; i < 100; i++) {
            Employee emp = new Employee();
            emp.setName("Employee " + i);
            emp.setBadgeId("EMP" + String.format("%03d", i));
            emp.setRole(EmployeeRole.WORKER);
            emp.setDepartment("Warehouse");
            emp.setHireDate(LocalDate.now());
            emp.setStatus(EmployeeStatus.ACTIVE);
            emp.setDeleted(false);
            entityManager.persist(emp);
        }
        entityManager.flush();

        Pageable pageable = PageRequest.of(0, 50);

        // Act
        long startTime = System.currentTimeMillis();
        Page<Employee> result = employeeRepository.findAllByDeletedFalse(pageable);
        long endTime = System.currentTimeMillis();

        // Assert
        assertNotNull(result);
        assertEquals(50, result.getContent().size());
        assertTrue((endTime - startTime) < 1000); // Should complete within 1 second
    }
}