package com.warehouse.employee.repository;

import com.warehouse.employee.domain.Employee;
import com.warehouse.employee.domain.EmployeeRole;
import com.warehouse.employee.domain.EmployeeStatus;
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
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive JUnit test suite for EmployeeRepository
 * Tests cover database operations, custom queries, pagination, and constraints
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
        testEmployee1.setRole(EmployeeRole.WORKER);
        testEmployee1.setDepartment("Warehouse");
        testEmployee1.setShiftGroup("Day Shift");
        testEmployee1.setHireDate(LocalDate.of(2024, 1, 1));
        testEmployee1.setStatus(EmployeeStatus.ACTIVE);
        testEmployee1.setDeleted(false);

        // Setup test employee 2
        testEmployee2 = new Employee();
        testEmployee2.setBadgeId("EMP002");
        testEmployee2.setName("Jane Smith");
        testEmployee2.setRole(EmployeeRole.SUPERVISOR);
        testEmployee2.setDepartment("Warehouse");
        testEmployee2.setShiftGroup("Night Shift");
        testEmployee2.setHireDate(LocalDate.of(2023, 6, 15));
        testEmployee2.setStatus(EmployeeStatus.ACTIVE);
        testEmployee2.setDeleted(false);

        // Setup test employee 3 (deleted)
        testEmployee3 = new Employee();
        testEmployee3.setBadgeId("EMP003");
        testEmployee3.setName("Bob Johnson");
        testEmployee3.setRole(EmployeeRole.WORKER);
        testEmployee3.setDepartment("Shipping");
        testEmployee3.setShiftGroup("Day Shift");
        testEmployee3.setHireDate(LocalDate.of(2022, 3, 10));
        testEmployee3.setStatus(EmployeeStatus.INACTIVE);
        testEmployee3.setDeleted(true);
    }

    // ========== SAVE TESTS ==========

    @Test
    @DisplayName("Test save employee - success")
    public void testSaveEmployee_Success() {
        // Act
        Employee saved = employeeRepository.save(testEmployee1);
        entityManager.flush();

        // Assert
        assertNotNull(saved.getId());
        assertEquals("EMP001", saved.getBadgeId());
        assertEquals("John Doe", saved.getName());
    }

    @Test
    @DisplayName("Test save employee with duplicate badge ID - throws exception")
    public void testSaveEmployee_DuplicateBadgeId_ThrowsException() {
        // Arrange
        employeeRepository.save(testEmployee1);
        entityManager.flush();

        Employee duplicate = new Employee();
        duplicate.setBadgeId("EMP001");
        duplicate.setName("Duplicate Employee");
        duplicate.setRole(EmployeeRole.WORKER);
        duplicate.setDepartment("Warehouse");
        duplicate.setShiftGroup("Day Shift");
        duplicate.setHireDate(LocalDate.now());
        duplicate.setStatus(EmployeeStatus.ACTIVE);

        // Act & Assert
        assertThrows(Exception.class, () -> {
            employeeRepository.save(duplicate);
            entityManager.flush();
        });
    }

    @Test
    @DisplayName("Test save employee with null badge ID - throws exception")
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
    @DisplayName("Test save employee with null name - throws exception")
    public void testSaveEmployee_NullName_ThrowsException() {
        // Arrange
        testEmployee1.setName(null);

        // Act & Assert
        assertThrows(Exception.class, () -> {
            employeeRepository.save(testEmployee1);
            entityManager.flush();
        });
    }

    // ========== FIND TESTS ==========

    @Test
    @DisplayName("Test find by ID - success")
    public void testFindById_Success() {
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
    @DisplayName("Test find by ID - not found")
    public void testFindById_NotFound() {
        // Act
        Optional<Employee> found = employeeRepository.findById(999L);

        // Assert
        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("Test find by badge ID - success")
    public void testFindByBadgeId_Success() {
        // Arrange
        employeeRepository.save(testEmployee1);
        entityManager.flush();

        // Act
        Optional<Employee> found = employeeRepository.findByBadgeIdAndDeletedFalse("EMP001");

        // Assert
        assertTrue(found.isPresent());
        assertEquals("John Doe", found.get().getName());
    }

    @Test
    @DisplayName("Test find by badge ID - not found")
    public void testFindByBadgeId_NotFound() {
        // Act
        Optional<Employee> found = employeeRepository.findByBadgeIdAndDeletedFalse("NONEXISTENT");

        // Assert
        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("Test find by badge ID - excludes deleted")
    public void testFindByBadgeId_ExcludesDeleted() {
        // Arrange
        employeeRepository.save(testEmployee3);
        entityManager.flush();

        // Act
        Optional<Employee> found = employeeRepository.findByBadgeIdAndDeletedFalse("EMP003");

        // Assert
        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("Test find all active employees - success")
    public void testFindAllActive_Success() {
        // Arrange
        employeeRepository.save(testEmployee1);
        employeeRepository.save(testEmployee2);
        employeeRepository.save(testEmployee3);
        entityManager.flush();

        // Act
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> found = employeeRepository.findAllByDeletedFalse(pageable);

        // Assert
        assertEquals(2, found.getTotalElements());
        assertTrue(found.getContent().stream().noneMatch(Employee::isDeleted));
    }

    @Test
    @DisplayName("Test find all - empty result")
    public void testFindAll_EmptyResult() {
        // Act
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> found = employeeRepository.findAllByDeletedFalse(pageable);

        // Assert
        assertEquals(0, found.getTotalElements());
    }

    // ========== EXISTS TESTS ==========

    @Test
    @DisplayName("Test exists by badge ID - true")
    public void testExistsByBadgeId_True() {
        // Arrange
        employeeRepository.save(testEmployee1);
        entityManager.flush();

        // Act
        boolean exists = employeeRepository.existsByBadgeIdAndDeletedFalse("EMP001");

        // Assert
        assertTrue(exists);
    }

    @Test
    @DisplayName("Test exists by badge ID - false")
    public void testExistsByBadgeId_False() {
        // Act
        boolean exists = employeeRepository.existsByBadgeIdAndDeletedFalse("NONEXISTENT");

        // Assert
        assertFalse(exists);
    }

    @Test
    @DisplayName("Test exists by badge ID - excludes deleted")
    public void testExistsByBadgeId_ExcludesDeleted() {
        // Arrange
        employeeRepository.save(testEmployee3);
        entityManager.flush();

        // Act
        boolean exists = employeeRepository.existsByBadgeIdAndDeletedFalse("EMP003");

        // Assert
        assertFalse(exists);
    }

    // ========== UPDATE TESTS ==========

    @Test
    @DisplayName("Test update employee - success")
    public void testUpdateEmployee_Success() {
        // Arrange
        Employee saved = employeeRepository.save(testEmployee1);
        entityManager.flush();

        // Act
        saved.setName("John Doe Updated");
        saved.setRole(EmployeeRole.SUPERVISOR);
        Employee updated = employeeRepository.save(saved);
        entityManager.flush();

        // Assert
        assertEquals("John Doe Updated", updated.getName());
        assertEquals(EmployeeRole.SUPERVISOR, updated.getRole());
    }

    @Test
    @DisplayName("Test update employee badge ID to duplicate - throws exception")
    public void testUpdateEmployee_DuplicateBadgeId_ThrowsException() {
        // Arrange
        employeeRepository.save(testEmployee1);
        Employee saved2 = employeeRepository.save(testEmployee2);
        entityManager.flush();

        // Act & Assert
        assertThrows(Exception.class, () -> {
            saved2.setBadgeId("EMP001");
            employeeRepository.save(saved2);
            entityManager.flush();
        });
    }

    // ========== DELETE TESTS ==========

    @Test
    @DisplayName("Test soft delete employee - success")
    public void testSoftDeleteEmployee_Success() {
        // Arrange
        Employee saved = employeeRepository.save(testEmployee1);
        entityManager.flush();

        // Act
        saved.setDeleted(true);
        employeeRepository.save(saved);
        entityManager.flush();

        // Assert
        Optional<Employee> found = employeeRepository.findByBadgeIdAndDeletedFalse("EMP001");
        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("Test hard delete employee - success")
    public void testHardDeleteEmployee_Success() {
        // Arrange
        Employee saved = employeeRepository.save(testEmployee1);
        entityManager.flush();
        Long id = saved.getId();

        // Act
        employeeRepository.deleteById(id);
        entityManager.flush();

        // Assert
        Optional<Employee> found = employeeRepository.findById(id);
        assertFalse(found.isPresent());
    }

    // ========== CUSTOM QUERY TESTS ==========

    @Test
    @DisplayName("Test find by department - success")
    public void testFindByDepartment_Success() {
        // Arrange
        employeeRepository.save(testEmployee1);
        employeeRepository.save(testEmployee2);
        employeeRepository.save(testEmployee3);
        entityManager.flush();

        // Act
        List<Employee> found = employeeRepository.findByDepartmentAndDeletedFalse("Warehouse");

        // Assert
        assertEquals(2, found.size());
        assertTrue(found.stream().allMatch(e -> "Warehouse".equals(e.getDepartment())));
    }

    @Test
    @DisplayName("Test find by role - success")
    public void testFindByRole_Success() {
        // Arrange
        employeeRepository.save(testEmployee1);
        employeeRepository.save(testEmployee2);
        entityManager.flush();

        // Act
        List<Employee> found = employeeRepository.findByRoleAndDeletedFalse(EmployeeRole.WORKER);

        // Assert
        assertEquals(1, found.size());
        assertEquals(EmployeeRole.WORKER, found.get(0).getRole());
    }

    @Test
    @DisplayName("Test find by status - success")
    public void testFindByStatus_Success() {
        // Arrange
        employeeRepository.save(testEmployee1);
        employeeRepository.save(testEmployee2);
        entityManager.flush();

        // Act
        List<Employee> found = employeeRepository.findByStatusAndDeletedFalse(EmployeeStatus.ACTIVE);

        // Assert
        assertEquals(2, found.size());
        assertTrue(found.stream().allMatch(e -> EmployeeStatus.ACTIVE.equals(e.getStatus())));
    }

    @Test
    @DisplayName("Test find by hire date range - success")
    public void testFindByHireDateRange_Success() {
        // Arrange
        employeeRepository.save(testEmployee1);
        employeeRepository.save(testEmployee2);
        employeeRepository.save(testEmployee3);
        entityManager.flush();

        // Act
        LocalDate startDate = LocalDate.of(2023, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 12, 31);
        List<Employee> found = employeeRepository.findByHireDateBetweenAndDeletedFalse(startDate, endDate);

        // Assert
        assertEquals(2, found.size());
    }

    // ========== PAGINATION TESTS ==========

    @Test
    @DisplayName("Test pagination - first page")
    public void testPagination_FirstPage() {
        // Arrange
        for (int i = 1; i <= 15; i++) {
            Employee emp = new Employee();
            emp.setBadgeId("EMP" + String.format("%03d", i));
            emp.setName("Employee " + i);
            emp.setRole(EmployeeRole.WORKER);
            emp.setDepartment("Warehouse");
            emp.setShiftGroup("Day Shift");
            emp.setHireDate(LocalDate.now());
            emp.setStatus(EmployeeStatus.ACTIVE);
            emp.setDeleted(false);
            employeeRepository.save(emp);
        }
        entityManager.flush();

        // Act
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> page = employeeRepository.findAllByDeletedFalse(pageable);

        // Assert
        assertEquals(10, page.getContent().size());
        assertEquals(15, page.getTotalElements());
        assertEquals(2, page.getTotalPages());
        assertTrue(page.isFirst());
        assertFalse(page.isLast());
    }

    @Test
    @DisplayName("Test pagination - last page")
    public void testPagination_LastPage() {
        // Arrange
        for (int i = 1; i <= 15; i++) {
            Employee emp = new Employee();
            emp.setBadgeId("EMP" + String.format("%03d", i));
            emp.setName("Employee " + i);
            emp.setRole(EmployeeRole.WORKER);
            emp.setDepartment("Warehouse");
            emp.setShiftGroup("Day Shift");
            emp.setHireDate(LocalDate.now());
            emp.setStatus(EmployeeStatus.ACTIVE);
            emp.setDeleted(false);
            employeeRepository.save(emp);
        }
        entityManager.flush();

        // Act
        Pageable pageable = PageRequest.of(1, 10);
        Page<Employee> page = employeeRepository.findAllByDeletedFalse(pageable);

        // Assert
        assertEquals(5, page.getContent().size());
        assertEquals(15, page.getTotalElements());
        assertFalse(page.isFirst());
        assertTrue(page.isLast());
    }

    // ========== BOUNDARY CONDITION TESTS ==========

    @Test
    @DisplayName("Test save employee with maximum length name")
    public void testSaveEmployee_MaxLengthName() {
        // Arrange
        testEmployee1.setName("A".repeat(255));

        // Act
        Employee saved = employeeRepository.save(testEmployee1);
        entityManager.flush();

        // Assert
        assertNotNull(saved.getId());
        assertEquals(255, saved.getName().length());
    }

    @Test
    @DisplayName("Test save employee with special characters")
    public void testSaveEmployee_SpecialCharacters() {
        // Arrange
        testEmployee1.setName("O'Brien-Smith Jr.");

        // Act
        Employee saved = employeeRepository.save(testEmployee1);
        entityManager.flush();

        // Assert
        assertNotNull(saved.getId());
        assertEquals("O'Brien-Smith Jr.", saved.getName());
    }

    @Test
    @DisplayName("Test save employee with unicode characters")
    public void testSaveEmployee_UnicodeCharacters() {
        // Arrange
        testEmployee1.setName("JosÃ© GarcÃ­a");

        // Act
        Employee saved = employeeRepository.save(testEmployee1);
        entityManager.flush();

        // Assert
        assertNotNull(saved.getId());
        assertEquals("JosÃ© GarcÃ­a", saved.getName());
    }
}