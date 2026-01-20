package com.warehouse.ems.domain.employee;

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
        testEmployee1 = new Employee();
        testEmployee1.setBadgeId("EMP001");
        testEmployee1.setName("John Doe");
        testEmployee1.setRole(Role.WORKER);
        testEmployee1.setDepartment("Warehouse");
        testEmployee1.setShiftGroup("A");
        testEmployee1.setHireDate(LocalDate.of(2023, 1, 15));
        testEmployee1.setStatus(EmployeeStatus.ACTIVE);
        testEmployee1.setDeleted(false);

        testEmployee2 = new Employee();
        testEmployee2.setBadgeId("EMP002");
        testEmployee2.setName("Jane Smith");
        testEmployee2.setRole(Role.SUPERVISOR);
        testEmployee2.setDepartment("Warehouse");
        testEmployee2.setShiftGroup("B");
        testEmployee2.setHireDate(LocalDate.of(2022, 6, 1));
        testEmployee2.setStatus(EmployeeStatus.ACTIVE);
        testEmployee2.setDeleted(false);

        testEmployee3 = new Employee();
        testEmployee3.setBadgeId("EMP003");
        testEmployee3.setName("Bob Johnson");
        testEmployee3.setRole(Role.WORKER);
        testEmployee3.setDepartment("Shipping");
        testEmployee3.setShiftGroup("A");
        testEmployee3.setHireDate(LocalDate.of(2023, 3, 10));
        testEmployee3.setStatus(EmployeeStatus.TERMINATED);
        testEmployee3.setDeleted(true);
    }

    @Test
    @DisplayName("Test save employee")
    public void testSaveEmployee() {
        // Act
        Employee saved = employeeRepository.save(testEmployee1);

        // Assert
        assertNotNull(saved.getId());
        assertEquals("EMP001", saved.getBadgeId());
        assertEquals("John Doe", saved.getName());
    }

    @Test
    @DisplayName("Test find employee by ID")
    public void testFindEmployeeById() {
        // Arrange
        Employee saved = entityManager.persistAndFlush(testEmployee1);

        // Act
        Optional<Employee> found = employeeRepository.findById(saved.getId());

        // Assert
        assertTrue(found.isPresent());
        assertEquals("EMP001", found.get().getBadgeId());
    }

    @Test
    @DisplayName("Test find employee by badge ID and not deleted")
    public void testFindByBadgeIdAndDeletedFalse() {
        // Arrange
        entityManager.persistAndFlush(testEmployee1);

        // Act
        Optional<Employee> found = employeeRepository.findByBadgeIdAndDeletedFalse("EMP001");

        // Assert
        assertTrue(found.isPresent());
        assertEquals("John Doe", found.get().getName());
    }

    @Test
    @DisplayName("Test find by badge ID excludes deleted employees")
    public void testFindByBadgeIdExcludesDeleted() {
        // Arrange
        entityManager.persistAndFlush(testEmployee3);

        // Act
        Optional<Employee> found = employeeRepository.findByBadgeIdAndDeletedFalse("EMP003");

        // Assert
        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("Test exists by badge ID and not deleted")
    public void testExistsByBadgeIdAndDeletedFalse() {
        // Arrange
        entityManager.persistAndFlush(testEmployee1);

        // Act
        boolean exists = employeeRepository.existsByBadgeIdAndDeletedFalse("EMP001");

        // Assert
        assertTrue(exists);
    }

    @Test
    @DisplayName("Test exists by badge ID returns false for deleted")
    public void testExistsByBadgeIdReturnsFalseForDeleted() {
        // Arrange
        entityManager.persistAndFlush(testEmployee3);

        // Act
        boolean exists = employeeRepository.existsByBadgeIdAndDeletedFalse("EMP003");

        // Assert
        assertFalse(exists);
    }

    @Test
    @DisplayName("Test find all by deleted false with pagination")
    public void testFindAllByDeletedFalseWithPagination() {
        // Arrange
        entityManager.persistAndFlush(testEmployee1);
        entityManager.persistAndFlush(testEmployee2);
        entityManager.persistAndFlush(testEmployee3);

        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Employee> page = employeeRepository.findAllByDeletedFalse(pageable);

        // Assert
        assertEquals(2, page.getTotalElements());
        assertFalse(page.getContent().stream().anyMatch(Employee::isDeleted));
    }

    @Test
    @DisplayName("Test find all employees")
    public void testFindAllEmployees() {
        // Arrange
        entityManager.persistAndFlush(testEmployee1);
        entityManager.persistAndFlush(testEmployee2);

        // Act
        List<Employee> employees = employeeRepository.findAll();

        // Assert
        assertTrue(employees.size() >= 2);
    }

    @Test
    @DisplayName("Test update employee")
    public void testUpdateEmployee() {
        // Arrange
        Employee saved = entityManager.persistAndFlush(testEmployee1);
        saved.setName("John Updated");

        // Act
        Employee updated = employeeRepository.save(saved);

        // Assert
        assertEquals("John Updated", updated.getName());
    }

    @Test
    @DisplayName("Test soft delete employee")
    public void testSoftDeleteEmployee() {
        // Arrange
        Employee saved = entityManager.persistAndFlush(testEmployee1);
        saved.setDeleted(true);
        saved.setStatus(EmployeeStatus.TERMINATED);

        // Act
        Employee updated = employeeRepository.save(saved);

        // Assert
        assertTrue(updated.isDeleted());
        assertEquals(EmployeeStatus.TERMINATED, updated.getStatus());
    }

    @Test
    @DisplayName("Test find by department")
    public void testFindByDepartment() {
        // Arrange
        entityManager.persistAndFlush(testEmployee1);
        entityManager.persistAndFlush(testEmployee2);

        // Act
        List<Employee> warehouseEmployees = employeeRepository.findByDepartmentAndDeletedFalse("Warehouse");

        // Assert
        assertEquals(2, warehouseEmployees.size());
    }

    @Test
    @DisplayName("Test find by role")
    public void testFindByRole() {
        // Arrange
        entityManager.persistAndFlush(testEmployee1);
        entityManager.persistAndFlush(testEmployee2);

        // Act
        List<Employee> workers = employeeRepository.findByRoleAndDeletedFalse(Role.WORKER);

        // Assert
        assertEquals(1, workers.size());
        assertEquals(Role.WORKER, workers.get(0).getRole());
    }

    @Test
    @DisplayName("Test find by shift group")
    public void testFindByShiftGroup() {
        // Arrange
        entityManager.persistAndFlush(testEmployee1);
        entityManager.persistAndFlush(testEmployee2);

        // Act
        List<Employee> shiftAEmployees = employeeRepository.findByShiftGroupAndDeletedFalse("A");

        // Assert
        assertEquals(1, shiftAEmployees.size());
        assertEquals("A", shiftAEmployees.get(0).getShiftGroup());
    }

    @Test
    @DisplayName("Test find by status")
    public void testFindByStatus() {
        // Arrange
        entityManager.persistAndFlush(testEmployee1);
        entityManager.persistAndFlush(testEmployee2);

        // Act
        List<Employee> activeEmployees = employeeRepository.findByStatusAndDeletedFalse(EmployeeStatus.ACTIVE);

        // Assert
        assertEquals(2, activeEmployees.size());
    }

    @Test
    @DisplayName("Test find by hire date range")
    public void testFindByHireDateRange() {
        // Arrange
        entityManager.persistAndFlush(testEmployee1);
        entityManager.persistAndFlush(testEmployee2);

        LocalDate startDate = LocalDate.of(2023, 1, 1);
        LocalDate endDate = LocalDate.of(2023, 12, 31);

        // Act
        List<Employee> employees = employeeRepository.findByHireDateBetweenAndDeletedFalse(startDate, endDate);

        // Assert
        assertEquals(1, employees.size());
        assertEquals("EMP001", employees.get(0).getBadgeId());
    }

    @Test
    @DisplayName("Test count by department")
    public void testCountByDepartment() {
        // Arrange
        entityManager.persistAndFlush(testEmployee1);
        entityManager.persistAndFlush(testEmployee2);

        // Act
        long count = employeeRepository.countByDepartmentAndDeletedFalse("Warehouse");

        // Assert
        assertEquals(2, count);
    }

    @Test
    @DisplayName("Test count by status")
    public void testCountByStatus() {
        // Arrange
        entityManager.persistAndFlush(testEmployee1);
        entityManager.persistAndFlush(testEmployee2);

        // Act
        long count = employeeRepository.countByStatusAndDeletedFalse(EmployeeStatus.ACTIVE);

        // Assert
        assertEquals(2, count);
    }

    @Test
    @DisplayName("Test find by name containing")
    public void testFindByNameContaining() {
        // Arrange
        entityManager.persistAndFlush(testEmployee1);
        entityManager.persistAndFlush(testEmployee2);

        // Act
        List<Employee> employees = employeeRepository.findByNameContainingIgnoreCaseAndDeletedFalse("john");

        // Assert
        assertEquals(1, employees.size());
        assertTrue(employees.get(0).getName().toLowerCase().contains("john"));
    }

    @Test
    @DisplayName("Test unique badge ID constraint")
    public void testUniqueBadgeIdConstraint() {
        // Arrange
        entityManager.persistAndFlush(testEmployee1);

        Employee duplicate = new Employee();
        duplicate.setBadgeId("EMP001");
        duplicate.setName("Duplicate");
        duplicate.setRole(Role.WORKER);
        duplicate.setDepartment("Test");
        duplicate.setHireDate(LocalDate.now());
        duplicate.setStatus(EmployeeStatus.ACTIVE);

        // Act & Assert
        assertThrows(Exception.class, () -> {
            entityManager.persistAndFlush(duplicate);
        });
    }

    @Test
    @DisplayName("Test cascade operations")
    public void testCascadeOperations() {
        // Arrange
        Employee saved = entityManager.persistAndFlush(testEmployee1);

        // Act
        entityManager.remove(saved);
        entityManager.flush();

        // Assert
        Optional<Employee> found = employeeRepository.findById(saved.getId());
        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("Test audit fields are populated")
    public void testAuditFieldsArePopulated() {
        // Act
        Employee saved = employeeRepository.save(testEmployee1);
        entityManager.flush();

        // Assert
        assertNotNull(saved.getCreatedAt());
        assertNotNull(saved.getUpdatedAt());
    }

    @Test
    @DisplayName("Test updated at changes on update")
    public void testUpdatedAtChangesOnUpdate() throws InterruptedException {
        // Arrange
        Employee saved = employeeRepository.save(testEmployee1);
        entityManager.flush();
        
        Thread.sleep(100);
        
        saved.setName("Updated Name");

        // Act
        Employee updated = employeeRepository.save(saved);
        entityManager.flush();

        // Assert
        assertTrue(updated.getUpdatedAt().isAfter(saved.getCreatedAt()));
    }

    @Test
    @DisplayName("Test find all with sorting")
    public void testFindAllWithSorting() {
        // Arrange
        entityManager.persistAndFlush(testEmployee1);
        entityManager.persistAndFlush(testEmployee2);

        Pageable pageable = PageRequest.of(0, 10, org.springframework.data.domain.Sort.by("name").ascending());

        // Act
        Page<Employee> page = employeeRepository.findAllByDeletedFalse(pageable);

        // Assert
        assertEquals(2, page.getTotalElements());
        assertEquals("Jane Smith", page.getContent().get(0).getName());
    }

    @Test
    @DisplayName("Test pagination with multiple pages")
    public void testPaginationWithMultiplePages() {
        // Arrange
        for (int i = 1; i <= 25; i++) {
            Employee emp = new Employee();
            emp.setBadgeId("EMP" + String.format("%03d", i));
            emp.setName("Employee " + i);
            emp.setRole(Role.WORKER);
            emp.setDepartment("Warehouse");
            emp.setHireDate(LocalDate.now());
            emp.setStatus(EmployeeStatus.ACTIVE);
            emp.setDeleted(false);
            entityManager.persist(emp);
        }
        entityManager.flush();

        Pageable firstPage = PageRequest.of(0, 10);
        Pageable secondPage = PageRequest.of(1, 10);

        // Act
        Page<Employee> page1 = employeeRepository.findAllByDeletedFalse(firstPage);
        Page<Employee> page2 = employeeRepository.findAllByDeletedFalse(secondPage);

        // Assert
        assertEquals(25, page1.getTotalElements());
        assertEquals(10, page1.getContent().size());
        assertEquals(10, page2.getContent().size());
        assertTrue(page1.hasNext());
    }
}