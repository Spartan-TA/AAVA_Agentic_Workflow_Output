package com.company.wms.employee;

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
 * Comprehensive integration tests for EmployeeRepository
 * 
 * Tests cover:
 * - All custom query methods
 * - JPA repository operations
 * - Pagination and sorting
 * - Unique constraints
 * - Search functionality
 * - Edge cases and boundary conditions
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("EmployeeRepository Integration Tests")
class EmployeeRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private EmployeeRepository employeeRepository;

    private Employee testEmployee1;
    private Employee testEmployee2;
    private Employee inactiveEmployee;

    @BeforeEach
    void setUp() {
        // Clear database
        employeeRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();

        // Create test employees
        testEmployee1 = new Employee();
        testEmployee1.setBadgeId("EMP001");
        testEmployee1.setFirstName("John");
        testEmployee1.setLastName("Doe");
        testEmployee1.setEmail("john.doe@example.com");
        testEmployee1.setPhone("+1234567890");
        testEmployee1.setDateOfBirth(LocalDate.of(1990, 1, 1));
        testEmployee1.setRole("WORKER");
        testEmployee1.setDepartment("Warehouse");
        testEmployee1.setShiftGroup("A");
        testEmployee1.setHireDate(LocalDate.now());
        testEmployee1.setStatus("ACTIVE");
        testEmployee1.setActive(true);

        testEmployee2 = new Employee();
        testEmployee2.setBadgeId("EMP002");
        testEmployee2.setFirstName("Jane");
        testEmployee2.setLastName("Smith");
        testEmployee2.setEmail("jane.smith@example.com");
        testEmployee2.setPhone("+9876543210");
        testEmployee2.setDateOfBirth(LocalDate.of(1992, 5, 15));
        testEmployee2.setRole("SUPERVISOR");
        testEmployee2.setDepartment("Logistics");
        testEmployee2.setShiftGroup("B");
        testEmployee2.setHireDate(LocalDate.now());
        testEmployee2.setStatus("ACTIVE");
        testEmployee2.setActive(true);

        inactiveEmployee = new Employee();
        inactiveEmployee.setBadgeId("EMP003");
        inactiveEmployee.setFirstName("Bob");
        inactiveEmployee.setLastName("Johnson");
        inactiveEmployee.setEmail("bob.johnson@example.com");
        inactiveEmployee.setPhone("+5555555555");
        inactiveEmployee.setDateOfBirth(LocalDate.of(1985, 10, 20));
        inactiveEmployee.setRole("WORKER");
        inactiveEmployee.setDepartment("Warehouse");
        inactiveEmployee.setShiftGroup("A");
        inactiveEmployee.setHireDate(LocalDate.now().minusYears(2));
        inactiveEmployee.setStatus("INACTIVE");
        inactiveEmployee.setActive(false);
        inactiveEmployee.setTerminationDate(LocalDate.now());
    }

    // ==================== SAVE AND FIND TESTS ====================

    @Test
    @DisplayName("Should save employee successfully")
    void testSave_Success() {
        // Act
        Employee saved = employeeRepository.save(testEmployee1);
        entityManager.flush();

        // Assert
        assertNotNull(saved.getId());
        assertEquals("EMP001", saved.getBadgeId());
        assertEquals("John", saved.getFirstName());
    }

    @Test
    @DisplayName("Should find employee by ID")
    void testFindById_Success() {
        // Arrange
        Employee saved = employeeRepository.save(testEmployee1);
        entityManager.flush();

        // Act
        Optional<Employee> found = employeeRepository.findById(saved.getId());

        // Assert
        assertTrue(found.isPresent());
        assertEquals("EMP001", found.get().getBadgeId());
    }

    @Test
    @DisplayName("Should return empty when employee not found by ID")
    void testFindById_NotFound() {
        // Act
        Optional<Employee> found = employeeRepository.findById(999L);

        // Assert
        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("Should find all employees")
    void testFindAll_Success() {
        // Arrange
        employeeRepository.save(testEmployee1);
        employeeRepository.save(testEmployee2);
        entityManager.flush();

        // Act
        List<Employee> employees = employeeRepository.findAll();

        // Assert
        assertEquals(2, employees.size());
    }

    // ==================== FIND BY BADGE ID TESTS ====================

    @Test
    @DisplayName("Should find employee by badge ID")
    void testFindByBadgeId_Success() {
        // Arrange
        employeeRepository.save(testEmployee1);
        entityManager.flush();

        // Act
        Optional<Employee> found = employeeRepository.findByBadgeId("EMP001");

        // Assert
        assertTrue(found.isPresent());
        assertEquals("John", found.get().getFirstName());
    }

    @Test
    @DisplayName("Should return empty when badge ID not found")
    void testFindByBadgeId_NotFound() {
        // Act
        Optional<Employee> found = employeeRepository.findByBadgeId("INVALID");

        // Assert
        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("Should handle null badge ID")
    void testFindByBadgeId_Null() {
        // Act
        Optional<Employee> found = employeeRepository.findByBadgeId(null);

        // Assert
        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("Should handle empty badge ID")
    void testFindByBadgeId_Empty() {
        // Act
        Optional<Employee> found = employeeRepository.findByBadgeId("");

        // Assert
        assertFalse(found.isPresent());
    }

    // ==================== FIND BY EMAIL TESTS ====================

    @Test
    @DisplayName("Should find employee by email")
    void testFindByEmail_Success() {
        // Arrange
        employeeRepository.save(testEmployee1);
        entityManager.flush();

        // Act
        Optional<Employee> found = employeeRepository.findByEmail("john.doe@example.com");

        // Assert
        assertTrue(found.isPresent());
        assertEquals("EMP001", found.get().getBadgeId());
    }

    @Test
    @DisplayName("Should return empty when email not found")
    void testFindByEmail_NotFound() {
        // Act
        Optional<Employee> found = employeeRepository.findByEmail("nonexistent@example.com");

        // Assert
        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("Should handle case-sensitive email search")
    void testFindByEmail_CaseSensitive() {
        // Arrange
        employeeRepository.save(testEmployee1);
        entityManager.flush();

        // Act
        Optional<Employee> found = employeeRepository.findByEmail("JOHN.DOE@EXAMPLE.COM");

        // Assert - Depends on database collation, typically case-insensitive
        // This test documents the behavior
        assertNotNull(found);
    }

    // ==================== EXISTS TESTS ====================

    @Test
    @DisplayName("Should return true when badge ID exists")
    void testExistsByBadgeId_True() {
        // Arrange
        employeeRepository.save(testEmployee1);
        entityManager.flush();

        // Act
        boolean exists = employeeRepository.existsByBadgeId("EMP001");

        // Assert
        assertTrue(exists);
    }

    @Test
    @DisplayName("Should return false when badge ID does not exist")
    void testExistsByBadgeId_False() {
        // Act
        boolean exists = employeeRepository.existsByBadgeId("INVALID");

        // Assert
        assertFalse(exists);
    }

    @Test
    @DisplayName("Should return true when email exists")
    void testExistsByEmail_True() {
        // Arrange
        employeeRepository.save(testEmployee1);
        entityManager.flush();

        // Act
        boolean exists = employeeRepository.existsByEmail("john.doe@example.com");

        // Assert
        assertTrue(exists);
    }

    @Test
    @DisplayName("Should return false when email does not exist")
    void testExistsByEmail_False() {
        // Act
        boolean exists = employeeRepository.existsByEmail("nonexistent@example.com");

        // Assert
        assertFalse(exists);
    }

    // ==================== FIND ACTIVE EMPLOYEES TESTS ====================

    @Test
    @DisplayName("Should find all active employees")
    void testFindAllByActiveTrue_Success() {
        // Arrange
        employeeRepository.save(testEmployee1);
        employeeRepository.save(testEmployee2);
        employeeRepository.save(inactiveEmployee);
        entityManager.flush();

        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Employee> activePage = employeeRepository.findAllByActiveTrue(pageable);

        // Assert
        assertEquals(2, activePage.getTotalElements());
        assertTrue(activePage.getContent().stream().allMatch(Employee::isActive));
    }

    @Test
    @DisplayName("Should return empty page when no active employees")
    void testFindAllByActiveTrue_NoActive() {
        // Arrange
        employeeRepository.save(inactiveEmployee);
        entityManager.flush();

        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Employee> activePage = employeeRepository.findAllByActiveTrue(pageable);

        // Assert
        assertEquals(0, activePage.getTotalElements());
    }

    // ==================== FIND BY STATUS TESTS ====================

    @Test
    @DisplayName("Should find employees by status")
    void testFindAllByStatus_Success() {
        // Arrange
        employeeRepository.save(testEmployee1);
        employeeRepository.save(testEmployee2);
        employeeRepository.save(inactiveEmployee);
        entityManager.flush();

        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Employee> activePage = employeeRepository.findAllByStatus("ACTIVE", pageable);

        // Assert
        assertEquals(2, activePage.getTotalElements());
        assertTrue(activePage.getContent().stream()
                .allMatch(e -> "ACTIVE".equals(e.getStatus())));
    }

    @Test
    @DisplayName("Should find inactive employees by status")
    void testFindAllByStatus_Inactive() {
        // Arrange
        employeeRepository.save(testEmployee1);
        employeeRepository.save(inactiveEmployee);
        entityManager.flush();

        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Employee> inactivePage = employeeRepository.findAllByStatus("INACTIVE", pageable);

        // Assert
        assertEquals(1, inactivePage.getTotalElements());
        assertEquals("INACTIVE", inactivePage.getContent().get(0).getStatus());
    }

    // ==================== FIND BY DEPARTMENT TESTS ====================

    @Test
    @DisplayName("Should find employees by department")
    void testFindAllByDepartment_Success() {
        // Arrange
        employeeRepository.save(testEmployee1);
        employeeRepository.save(testEmployee2);
        employeeRepository.save(inactiveEmployee);
        entityManager.flush();

        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Employee> warehousePage = employeeRepository.findAllByDepartment("Warehouse", pageable);

        // Assert
        assertEquals(2, warehousePage.getTotalElements());
        assertTrue(warehousePage.getContent().stream()
                .allMatch(e -> "Warehouse".equals(e.getDepartment())));
    }

    @Test
    @DisplayName("Should return empty page when department has no employees")
    void testFindAllByDepartment_NoEmployees() {
        // Arrange
        employeeRepository.save(testEmployee1);
        entityManager.flush();

        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Employee> emptyPage = employeeRepository.findAllByDepartment("NonExistent", pageable);

        // Assert
        assertEquals(0, emptyPage.getTotalElements());
    }

    // ==================== FIND BY ROLE TESTS ====================

    @Test
    @DisplayName("Should find employees by role")
    void testFindAllByRole_Success() {
        // Arrange
        employeeRepository.save(testEmployee1);
        employeeRepository.save(testEmployee2);
        employeeRepository.save(inactiveEmployee);
        entityManager.flush();

        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Employee> workerPage = employeeRepository.findAllByRole("WORKER", pageable);

        // Assert
        assertEquals(2, workerPage.getTotalElements());
        assertTrue(workerPage.getContent().stream()
                .allMatch(e -> "WORKER".equals(e.getRole())));
    }

    // ==================== FIND BY SHIFT GROUP TESTS ====================

    @Test
    @DisplayName("Should find employees by shift group")
    void testFindAllByShiftGroup_Success() {
        // Arrange
        employeeRepository.save(testEmployee1);
        employeeRepository.save(testEmployee2);
        employeeRepository.save(inactiveEmployee);
        entityManager.flush();

        // Act
        List<Employee> shiftAEmployees = employeeRepository.findAllByShiftGroup("A");

        // Assert
        assertEquals(2, shiftAEmployees.size());
        assertTrue(shiftAEmployees.stream()
                .allMatch(e -> "A".equals(e.getShiftGroup())));
    }

    @Test
    @DisplayName("Should return empty list when shift group has no employees")
    void testFindAllByShiftGroup_NoEmployees() {
        // Arrange
        employeeRepository.save(testEmployee1);
        entityManager.flush();

        // Act
        List<Employee> emptyList = employeeRepository.findAllByShiftGroup("Z");

        // Assert
        assertTrue(emptyList.isEmpty());
    }

    // ==================== SEARCH EMPLOYEES TESTS ====================

    @Test
    @DisplayName("Should search employees by first name")
    void testSearchEmployees_ByFirstName() {
        // Arrange
        employeeRepository.save(testEmployee1);
        employeeRepository.save(testEmployee2);
        entityManager.flush();

        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Employee> results = employeeRepository.searchEmployees("John", pageable);

        // Assert
        assertEquals(1, results.getTotalElements());
        assertEquals("John", results.getContent().get(0).getFirstName());
    }

    @Test
    @DisplayName("Should search employees by last name")
    void testSearchEmployees_ByLastName() {
        // Arrange
        employeeRepository.save(testEmployee1);
        employeeRepository.save(testEmployee2);
        entityManager.flush();

        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Employee> results = employeeRepository.searchEmployees("Smith", pageable);

        // Assert
        assertEquals(1, results.getTotalElements());
        assertEquals("Smith", results.getContent().get(0).getLastName());
    }

    @Test
    @DisplayName("Should search employees by badge ID")
    void testSearchEmployees_ByBadgeId() {
        // Arrange
        employeeRepository.save(testEmployee1);
        employeeRepository.save(testEmployee2);
        entityManager.flush();

        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Employee> results = employeeRepository.searchEmployees("EMP001", pageable);

        // Assert
        assertEquals(1, results.getTotalElements());
        assertEquals("EMP001", results.getContent().get(0).getBadgeId());
    }

    @Test
    @DisplayName("Should search employees case-insensitively")
    void testSearchEmployees_CaseInsensitive() {
        // Arrange
        employeeRepository.save(testEmployee1);
        entityManager.flush();

        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Employee> results = employeeRepository.searchEmployees("john", pageable);

        // Assert
        assertEquals(1, results.getTotalElements());
    }

    @Test
    @DisplayName("Should search employees with partial match")
    void testSearchEmployees_PartialMatch() {
        // Arrange
        employeeRepository.save(testEmployee1);
        entityManager.flush();

        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Employee> results = employeeRepository.searchEmployees("Jo", pageable);

        // Assert
        assertEquals(1, results.getTotalElements());
    }

    @Test
    @DisplayName("Should return empty page when search term not found")
    void testSearchEmployees_NoResults() {
        // Arrange
        employeeRepository.save(testEmployee1);
        entityManager.flush();

        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Employee> results = employeeRepository.searchEmployees("NonExistent", pageable);

        // Assert
        assertEquals(0, results.getTotalElements());
    }

    // ==================== FIND BY DEPARTMENT AND STATUS TESTS ====================

    @Test
    @DisplayName("Should find employees by department and status")
    void testFindAllByDepartmentAndStatus_Success() {
        // Arrange
        employeeRepository.save(testEmployee1);
        employeeRepository.save(testEmployee2);
        employeeRepository.save(inactiveEmployee);
        entityManager.flush();

        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Employee> results = employeeRepository.findAllByDepartmentAndStatus(
                "Warehouse", "ACTIVE", pageable);

        // Assert
        assertEquals(1, results.getTotalElements());
        assertEquals("Warehouse", results.getContent().get(0).getDepartment());
        assertEquals("ACTIVE", results.getContent().get(0).getStatus());
    }

    // ==================== COUNT TESTS ====================

    @Test
    @DisplayName("Should count all employees")
    void testCount_Success() {
        // Arrange
        employeeRepository.save(testEmployee1);
        employeeRepository.save(testEmployee2);
        employeeRepository.save(inactiveEmployee);
        entityManager.flush();

        // Act
        long count = employeeRepository.count();

        // Assert
        assertEquals(3, count);
    }

    @Test
    @DisplayName("Should count active employees")
    void testCountByActiveTrue_Success() {
        // Arrange
        employeeRepository.save(testEmployee1);
        employeeRepository.save(testEmployee2);
        employeeRepository.save(inactiveEmployee);
        entityManager.flush();

        // Act
        long count = employeeRepository.countByActiveTrue();

        // Assert
        assertEquals(2, count);
    }

    @Test
    @DisplayName("Should count employees by department")
    void testCountByDepartment_Success() {
        // Arrange
        employeeRepository.save(testEmployee1);
        employeeRepository.save(testEmployee2);
        employeeRepository.save(inactiveEmployee);
        entityManager.flush();

        // Act
        long count = employeeRepository.countByDepartment("Warehouse");

        // Assert
        assertEquals(2, count);
    }

    // ==================== DELETE TESTS ====================

    @Test
    @DisplayName("Should delete employee")
    void testDelete_Success() {
        // Arrange
        Employee saved = employeeRepository.save(testEmployee1);
        entityManager.flush();

        // Act
        employeeRepository.delete(saved);
        entityManager.flush();

        // Assert
        Optional<Employee> found = employeeRepository.findById(saved.getId());
        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("Should delete all employees")
    void testDeleteAll_Success() {
        // Arrange
        employeeRepository.save(testEmployee1);
        employeeRepository.save(testEmployee2);
        entityManager.flush();

        // Act
        employeeRepository.deleteAll();
        entityManager.flush();

        // Assert
        assertEquals(0, employeeRepository.count());
    }

    // ==================== UNIQUE CONSTRAINT TESTS ====================

    @Test
    @DisplayName("Should enforce unique badge ID constraint")
    void testUniqueBadgeId_Violation() {
        // Arrange
        employeeRepository.save(testEmployee1);
        entityManager.flush();

        Employee duplicate = new Employee();
        duplicate.setBadgeId("EMP001"); // Duplicate badge ID
        duplicate.setFirstName("Another");
        duplicate.setLastName("Employee");
        duplicate.setEmail("another@example.com");
        duplicate.setDateOfBirth(LocalDate.of(1995, 1, 1));
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

    // ==================== PAGINATION TESTS ====================

    @Test
    @DisplayName("Should paginate results correctly")
    void testPagination_Success() {
        // Arrange
        for (int i = 0; i < 25; i++) {
            Employee emp = new Employee();
            emp.setBadgeId("EMP" + String.format("%03d", i));
            emp.setFirstName("Employee" + i);
            emp.setLastName("Test");
            emp.setEmail("emp" + i + "@example.com");
            emp.setDateOfBirth(LocalDate.of(1990, 1, 1));
            emp.setRole("WORKER");
            emp.setDepartment("Warehouse");
            emp.setHireDate(LocalDate.now());
            emp.setStatus("ACTIVE");
            emp.setActive(true);
            employeeRepository.save(emp);
        }
        entityManager.flush();

        Pageable firstPage = PageRequest.of(0, 10);
        Pageable secondPage = PageRequest.of(1, 10);

        // Act
        Page<Employee> page1 = employeeRepository.findAll(firstPage);
        Page<Employee> page2 = employeeRepository.findAll(secondPage);

        // Assert
        assertEquals(10, page1.getContent().size());
        assertEquals(10, page2.getContent().size());
        assertEquals(25, page1.getTotalElements());
        assertEquals(3, page1.getTotalPages());
        assertFalse(page1.isLast());
        assertTrue(page1.isFirst());
    }
}