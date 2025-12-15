package com.warehouse.ems.employee.repository;

import com.warehouse.ems.employee.domain.Employee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive JUnit test class for EmployeeRepository.
 * Tests all repository methods including custom queries, pagination, and JPA operations.
 * Uses @DataJpaTest for repository layer testing with in-memory database.
 *
 * @author Automation Test Engineer
 * @version 1.0
 */
@DataJpaTest
public class EmployeeRepositoryTest {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Employee testEmployee1;
    private Employee testEmployee2;
    private Employee testEmployee3;

    /**
     * Setup method to initialize test data before each test.
     * Creates and persists sample employees in the test database.
     */
    @BeforeEach
    public void setUp() {
        // Arrange - Create test employee 1
        testEmployee1 = Employee.builder()
                .name("John Doe")
                .badgeId("EMP001")
                .email("john.doe@warehouse.com")
                .phone("+1-555-0100")
                .role("WORKER")
                .department("Shipping")
                .shiftGroup("Morning")
                .hireDate(LocalDate.of(2023, 1, 15))
                .status("ACTIVE")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .createdBy("admin")
                .updatedBy("admin")
                .build();

        // Arrange - Create test employee 2
        testEmployee2 = Employee.builder()
                .name("Jane Smith")
                .badgeId("EMP002")
                .email("jane.smith@warehouse.com")
                .phone("+1-555-0200")
                .role("SUPERVISOR")
                .department("Receiving")
                .shiftGroup("Evening")
                .hireDate(LocalDate.of(2022, 6, 1))
                .status("ACTIVE")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .createdBy("admin")
                .updatedBy("admin")
                .build();

        // Arrange - Create test employee 3 (inactive)
        testEmployee3 = Employee.builder()
                .name("Bob Johnson")
                .badgeId("EMP003")
                .email("bob.johnson@warehouse.com")
                .phone("+1-555-0300")
                .role("WORKER")
                .department("Shipping")
                .shiftGroup("Night")
                .hireDate(LocalDate.of(2021, 3, 10))
                .status("INACTIVE")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .createdBy("admin")
                .updatedBy("admin")
                .build();

        // Persist test employees
        entityManager.persist(testEmployee1);
        entityManager.persist(testEmployee2);
        entityManager.persist(testEmployee3);
        entityManager.flush();
    }

    // ==================== FIND BY BADGE ID TESTS ====================

    /**
     * Test finding an employee by valid badgeId.
     * Expected: Employee is found and returned.
     */
    @Test
    public void testFindByBadgeId_WithValidBadgeId_ReturnsEmployee() {
        // Act
        Optional<Employee> result = employeeRepository.findByBadgeId("EMP001");

        // Assert
        assertTrue(result.isPresent());
        assertEquals("John Doe", result.get().getName());
        assertEquals("EMP001", result.get().getBadgeId());
        assertEquals("Shipping", result.get().getDepartment());
    }

    /**
     * Test finding an employee by non-existent badgeId.
     * Expected: Empty Optional is returned.
     */
    @Test
    public void testFindByBadgeId_WithNonExistentBadgeId_ReturnsEmpty() {
        // Act
        Optional<Employee> result = employeeRepository.findByBadgeId("INVALID");

        // Assert
        assertFalse(result.isPresent());
    }

    /**
     * Test finding an employee by null badgeId.
     * Expected: Empty Optional is returned or exception is thrown.
     */
    @Test
    public void testFindByBadgeId_WithNullBadgeId_ReturnsEmpty() {
        // Act
        Optional<Employee> result = employeeRepository.findByBadgeId(null);

        // Assert
        assertFalse(result.isPresent());
    }

    /**
     * Test finding an employee by empty badgeId.
     * Expected: Empty Optional is returned.
     */
    @Test
    public void testFindByBadgeId_WithEmptyBadgeId_ReturnsEmpty() {
        // Act
        Optional<Employee> result = employeeRepository.findByBadgeId("");

        // Assert
        assertFalse(result.isPresent());
    }

    /**
     * Test badgeId uniqueness constraint.
     * Expected: Exception is thrown when trying to save duplicate badgeId.
     */
    @Test
    public void testFindByBadgeId_UniquenessConstraint_ThrowsException() {
        // Arrange
        Employee duplicateEmployee = Employee.builder()
                .name("Duplicate Employee")
                .badgeId("EMP001") // Duplicate badgeId
                .email("duplicate@warehouse.com")
                .role("WORKER")
                .department("Shipping")
                .hireDate(LocalDate.now())
                .status("ACTIVE")
                .build();

        // Act & Assert
        assertThrows(Exception.class, () -> {
            entityManager.persist(duplicateEmployee);
            entityManager.flush();
        });
    }

    // ==================== FIND BY DEPARTMENT TESTS ====================

    /**
     * Test finding employees by existing department.
     * Expected: List of employees in that department is returned.
     */
    @Test
    public void testFindByDepartment_WithExistingDepartment_ReturnsEmployees() {
        // Act
        List<Employee> result = employeeRepository.findByDepartment("Shipping");

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(e -> "Shipping".equals(e.getDepartment())));
    }

    /**
     * Test finding employees by non-existing department.
     * Expected: Empty list is returned.
     */
    @Test
    public void testFindByDepartment_WithNonExistingDepartment_ReturnsEmptyList() {
        // Act
        List<Employee> result = employeeRepository.findByDepartment("NonExistent");

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Test finding employees by null department.
     * Expected: Empty list is returned.
     */
    @Test
    public void testFindByDepartment_WithNullDepartment_ReturnsEmptyList() {
        // Act
        List<Employee> result = employeeRepository.findByDepartment(null);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Test finding employees by department with single employee.
     * Expected: List with one employee is returned.
     */
    @Test
    public void testFindByDepartment_WithSingleEmployee_ReturnsSingletonList() {
        // Act
        List<Employee> result = employeeRepository.findByDepartment("Receiving");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Jane Smith", result.get(0).getName());
    }

    // ==================== FIND BY STATUS TESTS ====================

    /**
     * Test finding employees by ACTIVE status.
     * Expected: List of active employees is returned.
     */
    @Test
    public void testFindByStatus_WithActiveStatus_ReturnsActiveEmployees() {
        // Act
        List<Employee> result = employeeRepository.findByStatus("ACTIVE");

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(e -> "ACTIVE".equals(e.getStatus())));
    }

    /**
     * Test finding employees by INACTIVE status.
     * Expected: List of inactive employees is returned.
     */
    @Test
    public void testFindByStatus_WithInactiveStatus_ReturnsInactiveEmployees() {
        // Act
        List<Employee> result = employeeRepository.findByStatus("INACTIVE");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Bob Johnson", result.get(0).getName());
    }

    /**
     * Test finding employees by invalid status.
     * Expected: Empty list is returned.
     */
    @Test
    public void testFindByStatus_WithInvalidStatus_ReturnsEmptyList() {
        // Act
        List<Employee> result = employeeRepository.findByStatus("INVALID_STATUS");

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Test finding employees by null status.
     * Expected: Empty list is returned.
     */
    @Test
    public void testFindByStatus_WithNullStatus_ReturnsEmptyList() {
        // Act
        List<Employee> result = employeeRepository.findByStatus(null);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ==================== FIND BY DELETED AT IS NULL TESTS ====================

    /**
     * Test finding non-deleted employees.
     * Expected: All employees without deletedAt timestamp are returned.
     */
    @Test
    public void testFindByDeletedAtIsNull_ReturnsNonDeletedEmployees() {
        // Act
        List<Employee> result = employeeRepository.findByDeletedAtIsNull();

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        assertTrue(result.stream().allMatch(e -> e.getDeletedAt() == null));
    }

    /**
     * Test finding non-deleted employees after soft delete.
     * Expected: Soft deleted employee is excluded from results.
     */
    @Test
    public void testFindByDeletedAtIsNull_AfterSoftDelete_ExcludesDeletedEmployee() {
        // Arrange - Soft delete one employee
        testEmployee1.setDeletedAt(LocalDateTime.now());
        entityManager.persist(testEmployee1);
        entityManager.flush();

        // Act
        List<Employee> result = employeeRepository.findByDeletedAtIsNull();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertFalse(result.stream().anyMatch(e -> "EMP001".equals(e.getBadgeId())));
    }

    /**
     * Test finding non-deleted employees when all are deleted.
     * Expected: Empty list is returned.
     */
    @Test
    public void testFindByDeletedAtIsNull_WhenAllDeleted_ReturnsEmptyList() {
        // Arrange - Soft delete all employees
        testEmployee1.setDeletedAt(LocalDateTime.now());
        testEmployee2.setDeletedAt(LocalDateTime.now());
        testEmployee3.setDeletedAt(LocalDateTime.now());
        entityManager.persist(testEmployee1);
        entityManager.persist(testEmployee2);
        entityManager.persist(testEmployee3);
        entityManager.flush();

        // Act
        List<Employee> result = employeeRepository.findByDeletedAtIsNull();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ==================== SAVE AND FIND BY ID TESTS ====================

    /**
     * Test saving a new employee.
     * Expected: Employee is saved and can be retrieved.
     */
    @Test
    public void testSave_NewEmployee_SavesSuccessfully() {
        // Arrange
        Employee newEmployee = Employee.builder()
                .name("New Employee")
                .badgeId("EMP999")
                .email("new@warehouse.com")
                .role("WORKER")
                .department("Packing")
                .hireDate(LocalDate.now())
                .status("ACTIVE")
                .build();

        // Act
        Employee saved = employeeRepository.save(newEmployee);
        entityManager.flush();

        // Assert
        assertNotNull(saved.getId());
        assertEquals("New Employee", saved.getName());
        assertEquals("EMP999", saved.getBadgeId());
    }

    /**
     * Test finding an employee by valid ID.
     * Expected: Employee is found and returned.
     */
    @Test
    public void testFindById_WithValidId_ReturnsEmployee() {
        // Act
        Optional<Employee> result = employeeRepository.findById(testEmployee1.getId());

        // Assert
        assertTrue(result.isPresent());
        assertEquals("John Doe", result.get().getName());
    }

    /**
     * Test finding an employee by invalid ID.
     * Expected: Empty Optional is returned.
     */
    @Test
    public void testFindById_WithInvalidId_ReturnsEmpty() {
        // Act
        Optional<Employee> result = employeeRepository.findById(99999L);

        // Assert
        assertFalse(result.isPresent());
    }

    // ==================== PAGINATION AND SORTING TESTS ====================

    /**
     * Test pagination with findAll.
     * Expected: Paged results are returned correctly.
     */
    @Test
    public void testFindAll_WithPagination_ReturnsPagedResults() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 2);

        // Act
        Page<Employee> result = employeeRepository.findAll(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals(3, result.getTotalElements());
        assertEquals(2, result.getTotalPages());
    }

    /**
     * Test pagination with second page.
     * Expected: Second page with remaining employee is returned.
     */
    @Test
    public void testFindAll_WithSecondPage_ReturnsRemainingResults() {
        // Arrange
        Pageable pageable = PageRequest.of(1, 2);

        // Act
        Page<Employee> result = employeeRepository.findAll(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(3, result.getTotalElements());
    }

    /**
     * Test sorting by name ascending.
     * Expected: Employees are returned in alphabetical order by name.
     */
    @Test
    public void testFindAll_WithSortByNameAsc_ReturnsSortedResults() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10, Sort.by("name").ascending());

        // Act
        Page<Employee> result = employeeRepository.findAll(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.getContent().size());
        assertEquals("Bob Johnson", result.getContent().get(0).getName());
        assertEquals("Jane Smith", result.getContent().get(1).getName());
        assertEquals("John Doe", result.getContent().get(2).getName());
    }

    /**
     * Test sorting by hire date descending.
     * Expected: Employees are returned with most recent hire date first.
     */
    @Test
    public void testFindAll_WithSortByHireDateDesc_ReturnsSortedResults() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10, Sort.by("hireDate").descending());

        // Act
        Page<Employee> result = employeeRepository.findAll(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.getContent().size());
        assertEquals("John Doe", result.getContent().get(0).getName()); // 2023-01-15
        assertEquals("Jane Smith", result.getContent().get(1).getName()); // 2022-06-01
        assertEquals("Bob Johnson", result.getContent().get(2).getName()); // 2021-03-10
    }

    /**
     * Test pagination with large page size.
     * Expected: All employees are returned in single page.
     */
    @Test
    public void testFindAll_WithLargePageSize_ReturnsAllResults() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 100);

        // Act
        Page<Employee> result = employeeRepository.findAll(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.getContent().size());
        assertEquals(1, result.getTotalPages());
    }

    // ==================== UPDATE TESTS ====================

    /**
     * Test updating an existing employee.
     * Expected: Employee is updated successfully.
     */
    @Test
    public void testUpdate_ExistingEmployee_UpdatesSuccessfully() {
        // Arrange
        testEmployee1.setDepartment("Packing");
        testEmployee1.setStatus("INACTIVE");

        // Act
        Employee updated = employeeRepository.save(testEmployee1);
        entityManager.flush();

        // Assert
        assertEquals("Packing", updated.getDepartment());
        assertEquals("INACTIVE", updated.getStatus());
    }

    // ==================== DELETE TESTS ====================

    /**
     * Test deleting an employee by ID.
     * Expected: Employee is deleted and cannot be found.
     */
    @Test
    public void testDelete_ExistingEmployee_DeletesSuccessfully() {
        // Arrange
        Long employeeId = testEmployee1.getId();

        // Act
        employeeRepository.deleteById(employeeId);
        entityManager.flush();

        // Assert
        Optional<Employee> result = employeeRepository.findById(employeeId);
        assertFalse(result.isPresent());
    }

    /**
     * Test count after delete.
     * Expected: Total count is reduced by one.
     */
    @Test
    public void testCount_AfterDelete_ReturnsCorrectCount() {
        // Arrange
        long initialCount = employeeRepository.count();

        // Act
        employeeRepository.deleteById(testEmployee1.getId());
        entityManager.flush();
        long finalCount = employeeRepository.count();

        // Assert
        assertEquals(initialCount - 1, finalCount);
    }
}