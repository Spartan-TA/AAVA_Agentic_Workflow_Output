package com.warehouse.ems.repository;

import com.warehouse.ems.entity.Employee;
import com.warehouse.ems.entity.Role;
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
 * Tests cover JPA queries, custom queries, and database operations
 */
@DataJpaTest
public class EmployeeRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private RoleRepository roleRepository;

    private Employee testEmployee1;
    private Employee testEmployee2;
    private Role testRole;

    @BeforeEach
    public void setUp() {
        // Arrange - Set up test data
        testRole = new Role();
        testRole.setName("WORKER");
        testRole.setDescription("Warehouse Worker");
        entityManager.persist(testRole);

        testEmployee1 = new Employee();
        testEmployee1.setBadgeId("EMP001");
        testEmployee1.setFirstName("John");
        testEmployee1.setLastName("Doe");
        testEmployee1.setEmail("john.doe@warehouse.com");
        testEmployee1.setRole(testRole);
        testEmployee1.setDepartment("Logistics");
        testEmployee1.setShiftGroup("A");
        testEmployee1.setHireDate(LocalDate.of(2023, 1, 15));
        testEmployee1.setStatus("ACTIVE");
        testEmployee1.setDeleted(false);
        entityManager.persist(testEmployee1);

        testEmployee2 = new Employee();
        testEmployee2.setBadgeId("EMP002");
        testEmployee2.setFirstName("Jane");
        testEmployee2.setLastName("Smith");
        testEmployee2.setEmail("jane.smith@warehouse.com");
        testEmployee2.setRole(testRole);
        testEmployee2.setDepartment("Shipping");
        testEmployee2.setShiftGroup("B");
        testEmployee2.setHireDate(LocalDate.of(2023, 2, 20));
        testEmployee2.setStatus("ACTIVE");
        testEmployee2.setDeleted(false);
        entityManager.persist(testEmployee2);

        entityManager.flush();
    }

    // ========== SAVE TESTS ==========

    @Test
    public void testSave_ValidEmployee_Success() {
        // Arrange
        Employee newEmployee = new Employee();
        newEmployee.setBadgeId("EMP003");
        newEmployee.setFirstName("Bob");
        newEmployee.setLastName("Johnson");
        newEmployee.setEmail("bob.johnson@warehouse.com");
        newEmployee.setRole(testRole);
        newEmployee.setDepartment("Logistics");
        newEmployee.setShiftGroup("A");
        newEmployee.setHireDate(LocalDate.now());
        newEmployee.setStatus("ACTIVE");

        // Act
        Employee savedEmployee = employeeRepository.save(newEmployee);

        // Assert
        assertNotNull(savedEmployee.getId());
        assertEquals("EMP003", savedEmployee.getBadgeId());
        assertEquals("Bob", savedEmployee.getFirstName());
    }

    @Test
    public void testSave_UpdateExistingEmployee_Success() {
        // Arrange
        testEmployee1.setFirstName("Johnny");

        // Act
        Employee updatedEmployee = employeeRepository.save(testEmployee1);

        // Assert
        assertEquals("Johnny", updatedEmployee.getFirstName());
        assertEquals(testEmployee1.getId(), updatedEmployee.getId());
    }

    @Test
    public void testSave_NullBadgeId_ThrowsException() {
        // Arrange
        Employee invalidEmployee = new Employee();
        invalidEmployee.setBadgeId(null);
        invalidEmployee.setFirstName("Test");
        invalidEmployee.setLastName("User");

        // Act & Assert
        assertThrows(Exception.class, () -> {
            employeeRepository.save(invalidEmployee);
            entityManager.flush();
        });
    }

    // ========== FIND BY ID TESTS ==========

    @Test
    public void testFindById_ExistingId_ReturnsEmployee() {
        // Act
        Optional<Employee> found = employeeRepository.findById(testEmployee1.getId());

        // Assert
        assertTrue(found.isPresent());
        assertEquals("EMP001", found.get().getBadgeId());
        assertEquals("John", found.get().getFirstName());
    }

    @Test
    public void testFindById_NonExistingId_ReturnsEmpty() {
        // Act
        Optional<Employee> found = employeeRepository.findById(999L);

        // Assert
        assertFalse(found.isPresent());
    }

    @Test
    public void testFindById_NullId_ReturnsEmpty() {
        // Act
        Optional<Employee> found = employeeRepository.findById(null);

        // Assert
        assertFalse(found.isPresent());
    }

    // ========== FIND BY BADGE ID TESTS ==========

    @Test
    public void testFindByBadgeId_ExistingBadge_ReturnsEmployee() {
        // Act
        Optional<Employee> found = employeeRepository.findByBadgeId("EMP001");

        // Assert
        assertTrue(found.isPresent());
        assertEquals("John", found.get().getFirstName());
        assertEquals("Doe", found.get().getLastName());
    }

    @Test
    public void testFindByBadgeId_NonExistingBadge_ReturnsEmpty() {
        // Act
        Optional<Employee> found = employeeRepository.findByBadgeId("INVALID");

        // Assert
        assertFalse(found.isPresent());
    }

    @Test
    public void testFindByBadgeId_NullBadge_ReturnsEmpty() {
        // Act
        Optional<Employee> found = employeeRepository.findByBadgeId(null);

        // Assert
        assertFalse(found.isPresent());
    }

    @Test
    public void testFindByBadgeId_EmptyBadge_ReturnsEmpty() {
        // Act
        Optional<Employee> found = employeeRepository.findByBadgeId("");

        // Assert
        assertFalse(found.isPresent());
    }

    @Test
    public void testFindByBadgeId_CaseSensitive_ReturnsEmpty() {
        // Act
        Optional<Employee> found = employeeRepository.findByBadgeId("emp001");

        // Assert
        assertFalse(found.isPresent());
    }

    // ========== FIND BY DEPARTMENT TESTS ==========

    @Test
    public void testFindByDepartment_ValidDepartment_ReturnsEmployees() {
        // Act
        List<Employee> employees = employeeRepository.findByDepartment("Logistics");

        // Assert
        assertNotNull(employees);
        assertEquals(1, employees.size());
        assertEquals("EMP001", employees.get(0).getBadgeId());
    }

    @Test
    public void testFindByDepartment_NonExistingDepartment_ReturnsEmptyList() {
        // Act
        List<Employee> employees = employeeRepository.findByDepartment("NonExistent");

        // Assert
        assertNotNull(employees);
        assertTrue(employees.isEmpty());
    }

    @Test
    public void testFindByDepartment_MultipleDepartments_ReturnsCorrectEmployees() {
        // Act
        List<Employee> logisticsEmployees = employeeRepository.findByDepartment("Logistics");
        List<Employee> shippingEmployees = employeeRepository.findByDepartment("Shipping");

        // Assert
        assertEquals(1, logisticsEmployees.size());
        assertEquals(1, shippingEmployees.size());
        assertEquals("EMP001", logisticsEmployees.get(0).getBadgeId());
        assertEquals("EMP002", shippingEmployees.get(0).getBadgeId());
    }

    @Test
    public void testFindByDepartment_NullDepartment_ReturnsEmptyList() {
        // Act
        List<Employee> employees = employeeRepository.findByDepartment(null);

        // Assert
        assertNotNull(employees);
        assertTrue(employees.isEmpty());
    }

    // ========== FIND BY STATUS TESTS ==========

    @Test
    public void testFindByStatus_ActiveEmployees_ReturnsActiveEmployees() {
        // Act
        List<Employee> employees = employeeRepository.findByStatus("ACTIVE");

        // Assert
        assertNotNull(employees);
        assertEquals(2, employees.size());
        assertTrue(employees.stream().allMatch(e -> "ACTIVE".equals(e.getStatus())));
    }

    @Test
    public void testFindByStatus_InactiveEmployees_ReturnsEmptyList() {
        // Act
        List<Employee> employees = employeeRepository.findByStatus("INACTIVE");

        // Assert
        assertNotNull(employees);
        assertTrue(employees.isEmpty());
    }

    @Test
    public void testFindByStatus_MixedStatus_ReturnsCorrectEmployees() {
        // Arrange
        testEmployee2.setStatus("INACTIVE");
        employeeRepository.save(testEmployee2);

        // Act
        List<Employee> activeEmployees = employeeRepository.findByStatus("ACTIVE");
        List<Employee> inactiveEmployees = employeeRepository.findByStatus("INACTIVE");

        // Assert
        assertEquals(1, activeEmployees.size());
        assertEquals(1, inactiveEmployees.size());
        assertEquals("EMP001", activeEmployees.get(0).getBadgeId());
        assertEquals("EMP002", inactiveEmployees.get(0).getBadgeId());
    }

    // ========== FIND BY SHIFT GROUP TESTS ==========

    @Test
    public void testFindByShiftGroup_ValidShiftGroup_ReturnsEmployees() {
        // Act
        List<Employee> employees = employeeRepository.findByShiftGroup("A");

        // Assert
        assertNotNull(employees);
        assertEquals(1, employees.size());
        assertEquals("EMP001", employees.get(0).getBadgeId());
    }

    @Test
    public void testFindByShiftGroup_NonExistingShiftGroup_ReturnsEmptyList() {
        // Act
        List<Employee> employees = employeeRepository.findByShiftGroup("Z");

        // Assert
        assertNotNull(employees);
        assertTrue(employees.isEmpty());
    }

    // ========== FIND ALL WITH PAGINATION TESTS ==========

    @Test
    public void testFindAll_WithPagination_ReturnsPagedResults() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Employee> employeePage = employeeRepository.findAll(pageable);

        // Assert
        assertNotNull(employeePage);
        assertEquals(2, employeePage.getTotalElements());
        assertEquals(1, employeePage.getTotalPages());
        assertEquals(2, employeePage.getContent().size());
    }

    @Test
    public void testFindAll_WithSmallPageSize_ReturnsMultiplePages() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 1);

        // Act
        Page<Employee> employeePage = employeeRepository.findAll(pageable);

        // Assert
        assertNotNull(employeePage);
        assertEquals(2, employeePage.getTotalElements());
        assertEquals(2, employeePage.getTotalPages());
        assertEquals(1, employeePage.getContent().size());
    }

    @Test
    public void testFindAll_SecondPage_ReturnsCorrectData() {
        // Arrange
        Pageable pageable = PageRequest.of(1, 1);

        // Act
        Page<Employee> employeePage = employeeRepository.findAll(pageable);

        // Assert
        assertNotNull(employeePage);
        assertEquals(2, employeePage.getTotalElements());
        assertEquals(1, employeePage.getContent().size());
        assertEquals("EMP002", employeePage.getContent().get(0).getBadgeId());
    }

    // ========== DELETE TESTS ==========

    @Test
    public void testDelete_ExistingEmployee_Success() {
        // Arrange
        Long employeeId = testEmployee1.getId();

        // Act
        employeeRepository.delete(testEmployee1);
        entityManager.flush();

        // Assert
        Optional<Employee> found = employeeRepository.findById(employeeId);
        assertFalse(found.isPresent());
    }

    @Test
    public void testDeleteById_ExistingId_Success() {
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
    public void testDelete_VerifyOtherEmployeesRemain() {
        // Act
        employeeRepository.delete(testEmployee1);
        entityManager.flush();

        // Assert
        Optional<Employee> found = employeeRepository.findById(testEmployee2.getId());
        assertTrue(found.isPresent());
        assertEquals("EMP002", found.get().getBadgeId());
    }

    // ========== COUNT TESTS ==========

    @Test
    public void testCount_ReturnsCorrectCount() {
        // Act
        long count = employeeRepository.count();

        // Assert
        assertEquals(2, count);
    }

    @Test
    public void testCount_AfterDelete_ReturnsUpdatedCount() {
        // Arrange
        employeeRepository.delete(testEmployee1);
        entityManager.flush();

        // Act
        long count = employeeRepository.count();

        // Assert
        assertEquals(1, count);
    }

    // ========== EXISTS TESTS ==========

    @Test
    public void testExistsById_ExistingId_ReturnsTrue() {
        // Act
        boolean exists = employeeRepository.existsById(testEmployee1.getId());

        // Assert
        assertTrue(exists);
    }

    @Test
    public void testExistsById_NonExistingId_ReturnsFalse() {
        // Act
        boolean exists = employeeRepository.existsById(999L);

        // Assert
        assertFalse(exists);
    }

    // ========== SOFT DELETE TESTS ==========

    @Test
    public void testSoftDelete_MarkAsDeleted_Success() {
        // Arrange
        testEmployee1.setDeleted(true);

        // Act
        Employee updated = employeeRepository.save(testEmployee1);
        entityManager.flush();

        // Assert
        assertTrue(updated.isDeleted());
        Optional<Employee> found = employeeRepository.findById(testEmployee1.getId());
        assertTrue(found.isPresent());
        assertTrue(found.get().isDeleted());
    }

    @Test
    public void testFindByDeletedFalse_ReturnsOnlyActiveEmployees() {
        // Arrange
        testEmployee1.setDeleted(true);
        employeeRepository.save(testEmployee1);
        entityManager.flush();

        // Act
        List<Employee> activeEmployees = employeeRepository.findByDeletedFalse();

        // Assert
        assertEquals(1, activeEmployees.size());
        assertEquals("EMP002", activeEmployees.get(0).getBadgeId());
    }
}