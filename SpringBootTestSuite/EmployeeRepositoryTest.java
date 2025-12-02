package com.warehouse.management.employee.repository;

import com.warehouse.management.employee.entity.Employee;
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
 * Tests cover custom queries, pagination, and data access patterns
 */
@DataJpaTest
public class EmployeeRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private EmployeeRepository employeeRepository;

    private Employee employee1;
    private Employee employee2;
    private Employee employee3;

    @BeforeEach
    void setUp() {
        // Arrange: Create test employees
        employee1 = new Employee();
        employee1.setBadgeId("EMP001");
        employee1.setFirstName("John");
        employee1.setLastName("Doe");
        employee1.setEmail("john.doe@warehouse.com");
        employee1.setPhone("+1234567890");
        employee1.setRole("WORKER");
        employee1.setDepartment("SHIPPING");
        employee1.setShiftGroup("DAY");
        employee1.setHireDate(LocalDate.of(2023, 1, 15));
        employee1.setStatus("ACTIVE");
        employee1.setTenantId("TENANT001");
        employee1.setDeleted(false);

        employee2 = new Employee();
        employee2.setBadgeId("EMP002");
        employee2.setFirstName("Jane");
        employee2.setLastName("Smith");
        employee2.setEmail("jane.smith@warehouse.com");
        employee2.setPhone("+1987654321");
        employee2.setRole("SUPERVISOR");
        employee2.setDepartment("RECEIVING");
        employee2.setShiftGroup("NIGHT");
        employee2.setHireDate(LocalDate.of(2022, 6, 1));
        employee2.setStatus("ACTIVE");
        employee2.setTenantId("TENANT001");
        employee2.setDeleted(false);

        employee3 = new Employee();
        employee3.setBadgeId("EMP003");
        employee3.setFirstName("Bob");
        employee3.setLastName("Johnson");
        employee3.setEmail("bob.johnson@warehouse.com");
        employee3.setPhone("+1555555555");
        employee3.setRole("WORKER");
        employee3.setDepartment("SHIPPING");
        employee3.setShiftGroup("DAY");
        employee3.setHireDate(LocalDate.of(2023, 3, 1));
        employee3.setStatus("INACTIVE");
        employee3.setTenantId("TENANT002");
        employee3.setDeleted(true);

        // Persist test data
        entityManager.persist(employee1);
        entityManager.persist(employee2);
        entityManager.persist(employee3);
        entityManager.flush();
    }

    // ========== FIND BY BADGE ID TESTS ==========

    @Test
    void testFindByBadgeId_ExistingBadgeId_ReturnsEmployee() {
        // Act
        Optional<Employee> result = employeeRepository.findByBadgeId("EMP001");

        // Assert
        assertTrue(result.isPresent());
        assertEquals("EMP001", result.get().getBadgeId());
        assertEquals("John", result.get().getFirstName());
        assertEquals("Doe", result.get().getLastName());
    }

    @Test
    void testFindByBadgeId_NonExistingBadgeId_ReturnsEmpty() {
        // Act
        Optional<Employee> result = employeeRepository.findByBadgeId("EMP999");

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void testFindByBadgeId_NullBadgeId_ReturnsEmpty() {
        // Act
        Optional<Employee> result = employeeRepository.findByBadgeId(null);

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void testFindByBadgeId_EmptyBadgeId_ReturnsEmpty() {
        // Act
        Optional<Employee> result = employeeRepository.findByBadgeId("");

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void testFindByBadgeId_CaseSensitive_ReturnsEmpty() {
        // Act
        Optional<Employee> result = employeeRepository.findByBadgeId("emp001");

        // Assert
        assertFalse(result.isPresent());
    }

    // ========== FIND BY TENANT ID AND DELETED FALSE TESTS ==========

    @Test
    void testFindByTenantIdAndDeletedFalse_ValidTenant_ReturnsActiveEmployees() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Employee> result = employeeRepository.findByTenantIdAndDeletedFalse("TENANT001", pageable);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertTrue(result.getContent().stream().allMatch(e -> !e.isDeleted()));
        assertTrue(result.getContent().stream().allMatch(e -> "TENANT001".equals(e.getTenantId())));
    }

    @Test
    void testFindByTenantIdAndDeletedFalse_NonExistingTenant_ReturnsEmpty() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Employee> result = employeeRepository.findByTenantIdAndDeletedFalse("TENANT999", pageable);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getContent().size());
    }

    @Test
    void testFindByTenantIdAndDeletedFalse_TenantWithDeletedEmployees_ExcludesDeleted() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Employee> result = employeeRepository.findByTenantIdAndDeletedFalse("TENANT002", pageable);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getContent().size());
    }

    @Test
    void testFindByTenantIdAndDeletedFalse_WithPagination_ReturnsCorrectPage() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 1);

        // Act
        Page<Employee> result = employeeRepository.findByTenantIdAndDeletedFalse("TENANT001", pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(2, result.getTotalElements());
        assertEquals(2, result.getTotalPages());
    }

    @Test
    void testFindByTenantIdAndDeletedFalse_SecondPage_ReturnsCorrectData() {
        // Arrange
        Pageable pageable = PageRequest.of(1, 1);

        // Act
        Page<Employee> result = employeeRepository.findByTenantIdAndDeletedFalse("TENANT001", pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(2, result.getTotalElements());
    }

    // ========== FIND BY DEPARTMENT AND TENANT ID TESTS ==========

    @Test
    void testFindByDepartmentAndTenantId_ValidDepartment_ReturnsEmployees() {
        // Act
        List<Employee> result = employeeRepository.findByDepartmentAndTenantId("SHIPPING", "TENANT001");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("SHIPPING", result.get(0).getDepartment());
        assertEquals("TENANT001", result.get(0).getTenantId());
    }

    @Test
    void testFindByDepartmentAndTenantId_NonExistingDepartment_ReturnsEmpty() {
        // Act
        List<Employee> result = employeeRepository.findByDepartmentAndTenantId("NONEXISTENT", "TENANT001");

        // Assert
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void testFindByDepartmentAndTenantId_WrongTenant_ReturnsEmpty() {
        // Act
        List<Employee> result = employeeRepository.findByDepartmentAndTenantId("SHIPPING", "TENANT999");

        // Assert
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void testFindByDepartmentAndTenantId_IncludesDeletedEmployees_ReturnsAll() {
        // Act
        List<Employee> result = employeeRepository.findByDepartmentAndTenantId("SHIPPING", "TENANT002");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).isDeleted());
    }

    // ========== SAVE AND UPDATE TESTS ==========

    @Test
    void testSave_NewEmployee_PersistsSuccessfully() {
        // Arrange
        Employee newEmployee = new Employee();
        newEmployee.setBadgeId("EMP004");
        newEmployee.setFirstName("Alice");
        newEmployee.setLastName("Williams");
        newEmployee.setEmail("alice.williams@warehouse.com");
        newEmployee.setPhone("+1666666666");
        newEmployee.setRole("WORKER");
        newEmployee.setDepartment("PACKING");
        newEmployee.setShiftGroup("DAY");
        newEmployee.setHireDate(LocalDate.now());
        newEmployee.setStatus("ACTIVE");
        newEmployee.setTenantId("TENANT001");
        newEmployee.setDeleted(false);

        // Act
        Employee saved = employeeRepository.save(newEmployee);
        entityManager.flush();
        entityManager.clear();

        // Assert
        assertNotNull(saved.getId());
        Optional<Employee> found = employeeRepository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("EMP004", found.get().getBadgeId());
    }

    @Test
    void testSave_UpdateExistingEmployee_UpdatesSuccessfully() {
        // Arrange
        Employee existing = employeeRepository.findByBadgeId("EMP001").get();
        existing.setFirstName("John Updated");
        existing.setDepartment("RECEIVING");

        // Act
        Employee updated = employeeRepository.save(existing);
        entityManager.flush();
        entityManager.clear();

        // Assert
        Optional<Employee> found = employeeRepository.findById(updated.getId());
        assertTrue(found.isPresent());
        assertEquals("John Updated", found.get().getFirstName());
        assertEquals("RECEIVING", found.get().getDepartment());
    }

    // ========== DELETE TESTS ==========

    @Test
    void testDelete_ExistingEmployee_RemovesFromDatabase() {
        // Arrange
        Employee existing = employeeRepository.findByBadgeId("EMP001").get();
        Long id = existing.getId();

        // Act
        employeeRepository.delete(existing);
        entityManager.flush();
        entityManager.clear();

        // Assert
        Optional<Employee> found = employeeRepository.findById(id);
        assertFalse(found.isPresent());
    }

    @Test
    void testDeleteById_ExistingEmployee_RemovesFromDatabase() {
        // Arrange
        Employee existing = employeeRepository.findByBadgeId("EMP001").get();
        Long id = existing.getId();

        // Act
        employeeRepository.deleteById(id);
        entityManager.flush();
        entityManager.clear();

        // Assert
        Optional<Employee> found = employeeRepository.findById(id);
        assertFalse(found.isPresent());
    }

    // ========== FIND ALL TESTS ==========

    @Test
    void testFindAll_ReturnsAllEmployees() {
        // Act
        List<Employee> result = employeeRepository.findAll();

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
    }

    @Test
    void testFindAll_WithPagination_ReturnsCorrectPage() {
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

    // ========== COUNT TESTS ==========

    @Test
    void testCount_ReturnsCorrectCount() {
        // Act
        long count = employeeRepository.count();

        // Assert
        assertEquals(3, count);
    }

    // ========== EXISTS TESTS ==========

    @Test
    void testExistsById_ExistingId_ReturnsTrue() {
        // Arrange
        Employee existing = employeeRepository.findByBadgeId("EMP001").get();

        // Act
        boolean exists = employeeRepository.existsById(existing.getId());

        // Assert
        assertTrue(exists);
    }

    @Test
    void testExistsById_NonExistingId_ReturnsFalse() {
        // Act
        boolean exists = employeeRepository.existsById(999L);

        // Assert
        assertFalse(exists);
    }

    // ========== BOUNDARY CONDITION TESTS ==========

    @Test
    void testFindByBadgeId_SpecialCharacters_ReturnsEmployee() {
        // Arrange
        Employee specialEmployee = new Employee();
        specialEmployee.setBadgeId("EMP-001-A");
        specialEmployee.setFirstName("Special");
        specialEmployee.setLastName("Employee");
        specialEmployee.setEmail("special@warehouse.com");
        specialEmployee.setPhone("+1777777777");
        specialEmployee.setRole("WORKER");
        specialEmployee.setDepartment("SHIPPING");
        specialEmployee.setShiftGroup("DAY");
        specialEmployee.setHireDate(LocalDate.now());
        specialEmployee.setStatus("ACTIVE");
        specialEmployee.setTenantId("TENANT001");
        specialEmployee.setDeleted(false);
        entityManager.persist(specialEmployee);
        entityManager.flush();

        // Act
        Optional<Employee> result = employeeRepository.findByBadgeId("EMP-001-A");

        // Assert
        assertTrue(result.isPresent());
        assertEquals("EMP-001-A", result.get().getBadgeId());
    }

    @Test
    void testFindByTenantIdAndDeletedFalse_LargePage_ReturnsCorrectData() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 1000);

        // Act
        Page<Employee> result = employeeRepository.findByTenantIdAndDeletedFalse("TENANT001", pageable);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
    }

    @Test
    void testSave_MaxLengthFields_PersistsSuccessfully() {
        // Arrange
        Employee maxEmployee = new Employee();
        maxEmployee.setBadgeId("A".repeat(50));
        maxEmployee.setFirstName("B".repeat(100));
        maxEmployee.setLastName("C".repeat(100));
        maxEmployee.setEmail("max@warehouse.com");
        maxEmployee.setPhone("+1888888888");
        maxEmployee.setRole("WORKER");
        maxEmployee.setDepartment("SHIPPING");
        maxEmployee.setShiftGroup("DAY");
        maxEmployee.setHireDate(LocalDate.now());
        maxEmployee.setStatus("ACTIVE");
        maxEmployee.setTenantId("TENANT001");
        maxEmployee.setDeleted(false);

        // Act
        Employee saved = employeeRepository.save(maxEmployee);
        entityManager.flush();

        // Assert
        assertNotNull(saved.getId());
        assertEquals(50, saved.getBadgeId().length());
        assertEquals(100, saved.getFirstName().length());
    }
}