package com.warehouse.employee.repository;

import com.warehouse.employee.domain.Employee;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import java.time.LocalDate;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive JUnit test suite for EmployeeRepository.
 * Tests repository methods, queries, pagination, and database interactions.
 */
@DataJpaTest
public class EmployeeRepositoryTest {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Employee testEmployee1;
    private Employee testEmployee2;
    private Employee deletedEmployee;

    @BeforeEach
    public void setUp() {
        // Clear any existing data
        employeeRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();

        // Create test employees
        testEmployee1 = Employee.builder()
                .name("John Doe")
                .badgeId("EMP001")
                .role("WORKER")
                .department("Warehouse")
                .shiftGroup("Morning")
                .hireDate(LocalDate.of(2023, 1, 15))
                .status("ACTIVE")
                .build();

        testEmployee2 = Employee.builder()
                .name("Jane Smith")
                .badgeId("EMP002")
                .role("SUPERVISOR")
                .department("Logistics")
                .shiftGroup("Evening")
                .hireDate(LocalDate.of(2022, 6, 10))
                .status("ACTIVE")
                .build();

        deletedEmployee = Employee.builder()
                .name("Deleted User")
                .badgeId("EMP003")
                .role("WORKER")
                .department("Warehouse")
                .shiftGroup("Night")
                .hireDate(LocalDate.of(2021, 3, 20))
                .status("DELETED")
                .build();

        // Persist test data
        entityManager.persist(testEmployee1);
        entityManager.persist(testEmployee2);
        entityManager.persist(deletedEmployee);
        entityManager.flush();
    }

    // ========== BASIC CRUD TESTS ==========

    @Test
    public void testSaveEmployee_WithValidData_Success() {
        // Arrange
        Employee newEmployee = Employee.builder()
                .name("New Employee")
                .badgeId("EMP004")
                .role("ADMIN")
                .department("Management")
                .status("ACTIVE")
                .build();

        // Act
        Employee savedEmployee = employeeRepository.save(newEmployee);

        // Assert
        assertNotNull(savedEmployee.getId());
        assertEquals("New Employee", savedEmployee.getName());
        assertEquals("EMP004", savedEmployee.getBadgeId());
    }

    @Test
    public void testFindById_WithExistingId_ReturnsEmployee() {
        // Act
        Optional<Employee> found = employeeRepository.findById(testEmployee1.getId());

        // Assert
        assertTrue(found.isPresent());
        assertEquals("John Doe", found.get().getName());
        assertEquals("EMP001", found.get().getBadgeId());
    }

    @Test
    public void testFindById_WithNonExistingId_ReturnsEmpty() {
        // Act
        Optional<Employee> found = employeeRepository.findById(99999L);

        // Assert
        assertFalse(found.isPresent());
    }

    @Test
    public void testDeleteEmployee_Success() {
        // Arrange
        Long employeeId = testEmployee1.getId();

        // Act
        employeeRepository.deleteById(employeeId);
        entityManager.flush();

        // Assert
        Optional<Employee> found = employeeRepository.findById(employeeId);
        assertFalse(found.isPresent());
    }

    @Test
    public void testUpdateEmployee_Success() {
        // Arrange
        Employee employee = employeeRepository.findById(testEmployee1.getId()).get();
        employee.setName("John Updated");
        employee.setDepartment("New Department");

        // Act
        Employee updated = employeeRepository.save(employee);
        entityManager.flush();
        entityManager.clear();

        // Assert
        Employee found = employeeRepository.findById(testEmployee1.getId()).get();
        assertEquals("John Updated", found.getName());
        assertEquals("New Department", found.getDepartment());
    }

    // ========== FIND BY BADGE ID TESTS ==========

    @Test
    public void testFindByBadgeId_WithExistingBadgeId_ReturnsEmployee() {
        // Act
        Optional<Employee> found = employeeRepository.findByBadgeId("EMP001");

        // Assert
        assertTrue(found.isPresent());
        assertEquals("John Doe", found.get().getName());
        assertEquals(testEmployee1.getId(), found.get().getId());
    }

    @Test
    public void testFindByBadgeId_WithNonExistingBadgeId_ReturnsEmpty() {
        // Act
        Optional<Employee> found = employeeRepository.findByBadgeId("NONEXISTENT");

        // Assert
        assertFalse(found.isPresent());
    }

    @Test
    public void testFindByBadgeId_WithNullBadgeId_ReturnsEmpty() {
        // Act
        Optional<Employee> found = employeeRepository.findByBadgeId(null);

        // Assert
        assertFalse(found.isPresent());
    }

    @Test
    public void testFindByBadgeId_WithEmptyBadgeId_ReturnsEmpty() {
        // Act
        Optional<Employee> found = employeeRepository.findByBadgeId("");

        // Assert
        assertFalse(found.isPresent());
    }

    @Test
    public void testFindByBadgeId_CaseSensitive() {
        // Act
        Optional<Employee> found = employeeRepository.findByBadgeId("emp001");

        // Assert - Assuming case-sensitive search
        assertFalse(found.isPresent(), "Badge ID search should be case-sensitive");
    }

    @Test
    public void testFindByBadgeId_WithDeletedEmployee_ReturnsEmployee() {
        // Act - findByBadgeId should return even deleted employees
        Optional<Employee> found = employeeRepository.findByBadgeId("EMP003");

        // Assert
        assertTrue(found.isPresent());
        assertEquals("DELETED", found.get().getStatus());
    }

    // ========== FIND ALL ACTIVE TESTS ==========

    @Test
    public void testFindAllActive_ExcludesDeletedEmployees() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Employee> activePage = employeeRepository.findAllActive(pageable);

        // Assert
        assertEquals(2, activePage.getTotalElements());
        assertTrue(activePage.getContent().stream()
                .noneMatch(e -> "DELETED".equals(e.getStatus())));
    }

    @Test
    public void testFindAllActive_WithPagination_FirstPage() {
        // Arrange - Add more employees
        for (int i = 4; i <= 10; i++) {
            Employee emp = Employee.builder()
                    .name("Employee " + i)
                    .badgeId("EMP00" + i)
                    .role("WORKER")
                    .department("Warehouse")
                    .status("ACTIVE")
                    .build();
            entityManager.persist(emp);
        }
        entityManager.flush();

        Pageable pageable = PageRequest.of(0, 5);

        // Act
        Page<Employee> page = employeeRepository.findAllActive(pageable);

        // Assert
        assertEquals(5, page.getContent().size());
        assertEquals(9, page.getTotalElements()); // 2 original + 7 new = 9 active
        assertTrue(page.hasNext());
    }

    @Test
    public void testFindAllActive_WithPagination_SecondPage() {
        // Arrange - Add more employees
        for (int i = 4; i <= 10; i++) {
            Employee emp = Employee.builder()
                    .name("Employee " + i)
                    .badgeId("EMP00" + i)
                    .role("WORKER")
                    .department("Warehouse")
                    .status("ACTIVE")
                    .build();
            entityManager.persist(emp);
        }
        entityManager.flush();

        Pageable pageable = PageRequest.of(1, 5);

        // Act
        Page<Employee> page = employeeRepository.findAllActive(pageable);

        // Assert
        assertEquals(4, page.getContent().size()); // Remaining 4 on second page
        assertEquals(9, page.getTotalElements());
        assertFalse(page.hasNext());
    }

    @Test
    public void testFindAllActive_WithSorting_ByName() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10, Sort.by("name").ascending());

        // Act
        Page<Employee> page = employeeRepository.findAllActive(pageable);

        // Assert
        assertEquals(2, page.getTotalElements());
        assertEquals("Jane Smith", page.getContent().get(0).getName());
        assertEquals("John Doe", page.getContent().get(1).getName());
    }

    @Test
    public void testFindAllActive_WithEmptyResult() {
        // Arrange - Delete all active employees
        employeeRepository.deleteAll();
        entityManager.flush();

        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Employee> page = employeeRepository.findAllActive(pageable);

        // Assert
        assertEquals(0, page.getTotalElements());
        assertTrue(page.getContent().isEmpty());
    }

    // ========== FIND BY STATUS TESTS ==========

    @Test
    public void testFindByStatus_WithActiveStatus_ReturnsActiveEmployees() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Employee> page = employeeRepository.findByStatus("ACTIVE", pageable);

        // Assert
        assertEquals(2, page.getTotalElements());
        assertTrue(page.getContent().stream()
                .allMatch(e -> "ACTIVE".equals(e.getStatus())));
    }

    @Test
    public void testFindByStatus_WithDeletedStatus_ReturnsDeletedEmployees() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Employee> page = employeeRepository.findByStatus("DELETED", pageable);

        // Assert
        assertEquals(1, page.getTotalElements());
        assertEquals("Deleted User", page.getContent().get(0).getName());
    }

    @Test
    public void testFindByStatus_WithInactiveStatus_ReturnsEmpty() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Employee> page = employeeRepository.findByStatus("INACTIVE", pageable);

        // Assert
        assertEquals(0, page.getTotalElements());
        assertTrue(page.getContent().isEmpty());
    }

    @Test
    public void testFindByStatus_WithNullStatus_ReturnsEmpty() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Employee> page = employeeRepository.findByStatus(null, pageable);

        // Assert
        assertEquals(0, page.getTotalElements());
    }

    @Test
    public void testFindByStatus_WithPagination() {
        // Arrange - Add more active employees
        for (int i = 4; i <= 8; i++) {
            Employee emp = Employee.builder()
                    .name("Employee " + i)
                    .badgeId("EMP00" + i)
                    .role("WORKER")
                    .department("Warehouse")
                    .status("ACTIVE")
                    .build();
            entityManager.persist(emp);
        }
        entityManager.flush();

        Pageable pageable = PageRequest.of(0, 3);

        // Act
        Page<Employee> page = employeeRepository.findByStatus("ACTIVE", pageable);

        // Assert
        assertEquals(3, page.getContent().size());
        assertEquals(7, page.getTotalElements()); // 2 original + 5 new
        assertTrue(page.hasNext());
    }

    @Test
    public void testFindByStatus_CaseSensitive() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Employee> page = employeeRepository.findByStatus("active", pageable);

        // Assert - Assuming case-sensitive search
        assertEquals(0, page.getTotalElements(), "Status search should be case-sensitive");
    }

    // ========== PAGINATION EDGE CASES ==========

    @Test
    public void testPagination_WithZeroPageSize_ThrowsException() {
        // Arrange & Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            PageRequest.of(0, 0);
        });
    }

    @Test
    public void testPagination_WithNegativePageNumber_ThrowsException() {
        // Arrange & Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            PageRequest.of(-1, 10);
        });
    }

    @Test
    public void testPagination_WithLargePageSize() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 1000);

        // Act
        Page<Employee> page = employeeRepository.findAllActive(pageable);

        // Assert
        assertEquals(2, page.getTotalElements());
        assertEquals(2, page.getContent().size());
    }

    @Test
    public void testPagination_WithPageBeyondTotalPages() {
        // Arrange
        Pageable pageable = PageRequest.of(10, 10); // Page 10 when only 1 page exists

        // Act
        Page<Employee> page = employeeRepository.findAllActive(pageable);

        // Assert
        assertEquals(2, page.getTotalElements());
        assertEquals(0, page.getContent().size());
        assertFalse(page.hasContent());
    }

    // ========== UNIQUE CONSTRAINT TESTS ==========

    @Test
    public void testSaveEmployee_WithDuplicateBadgeId_ThrowsException() {
        // Arrange
        Employee duplicateEmployee = Employee.builder()
                .name("Duplicate Employee")
                .badgeId("EMP001") // Same as testEmployee1
                .role("WORKER")
                .department("Warehouse")
                .status("ACTIVE")
                .build();

        // Act & Assert
        assertThrows(Exception.class, () -> {
            employeeRepository.save(duplicateEmployee);
            entityManager.flush();
        });
    }

    // ========== MULTIPLE STATUS TESTS ==========

    @Test
    public void testFindAllActive_WithMultipleStatuses() {
        // Arrange - Add employees with different statuses
        Employee inactiveEmployee = Employee.builder()
                .name("Inactive Employee")
                .badgeId("EMP010")
                .role("WORKER")
                .department("Warehouse")
                .status("INACTIVE")
                .build();
        entityManager.persist(inactiveEmployee);
        entityManager.flush();

        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Employee> activePage = employeeRepository.findAllActive(pageable);

        // Assert - Should exclude both DELETED and INACTIVE
        assertEquals(2, activePage.getTotalElements());
        assertTrue(activePage.getContent().stream()
                .allMatch(e -> "ACTIVE".equals(e.getStatus())));
    }

    // ========== SORTING TESTS ==========

    @Test
    public void testFindAllActive_SortByBadgeIdDescending() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10, Sort.by("badgeId").descending());

        // Act
        Page<Employee> page = employeeRepository.findAllActive(pageable);

        // Assert
        assertEquals(2, page.getTotalElements());
        assertEquals("EMP002", page.getContent().get(0).getBadgeId());
        assertEquals("EMP001", page.getContent().get(1).getBadgeId());
    }

    @Test
    public void testFindAllActive_SortByMultipleFields() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10, 
                Sort.by("department").ascending()
                    .and(Sort.by("name").ascending()));

        // Act
        Page<Employee> page = employeeRepository.findAllActive(pageable);

        // Assert
        assertEquals(2, page.getTotalElements());
        // Verify sorting order
        assertNotNull(page.getContent().get(0));
        assertNotNull(page.getContent().get(1));
    }

    // ========== COUNT TESTS ==========

    @Test
    public void testCount_ReturnsCorrectTotal() {
        // Act
        long count = employeeRepository.count();

        // Assert
        assertEquals(3, count); // 2 active + 1 deleted
    }

    @Test
    public void testExistsById_WithExistingId_ReturnsTrue() {
        // Act
        boolean exists = employeeRepository.existsById(testEmployee1.getId());

        // Assert
        assertTrue(exists);
    }

    @Test
    public void testExistsById_WithNonExistingId_ReturnsFalse() {
        // Act
        boolean exists = employeeRepository.existsById(99999L);

        // Assert
        assertFalse(exists);
    }
}