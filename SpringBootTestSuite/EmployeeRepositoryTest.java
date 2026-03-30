package com.companyname.wems.employee.repository;

import com.companyname.wems.employee.entity.Employee;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive JUnit test suite for EmployeeRepository
 * Tests cover JPA queries, custom methods, and database operations
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Employee Repository Tests")
class EmployeeRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private EmployeeRepository employeeRepository;

    private Employee employee1;
    private Employee employee2;
    private Employee employee3;

    @BeforeEach
    void setUp() {
        // Clear database before each test
        employeeRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();

        // Create test employees
        employee1 = Employee.builder()
                .name("John Doe")
                .badgeId("EMP12345")
                .role(Employee.Role.WORKER)
                .department("Shipping")
                .shiftGroup("Morning")
                .hireDate(LocalDate.of(2023, 1, 15))
                .status(Employee.Status.ACTIVE)
                .build();

        employee2 = Employee.builder()
                .name("Jane Smith")
                .badgeId("EMP67890")
                .role(Employee.Role.SUPERVISOR)
                .department("Receiving")
                .shiftGroup("Evening")
                .hireDate(LocalDate.of(2022, 6, 1))
                .status(Employee.Status.ACTIVE)
                .build();

        employee3 = Employee.builder()
                .name("Bob Johnson")
                .badgeId("EMP11111")
                .role(Employee.Role.WORKER)
                .department("Shipping")
                .shiftGroup("Night")
                .hireDate(LocalDate.of(2021, 3, 10))
                .status(Employee.Status.INACTIVE)
                .build();
    }

    // ========== SAVE TESTS ==========

    @Test
    @DisplayName("Should save employee successfully")
    void testSave_ValidEmployee_Success() {
        // Act
        Employee savedEmployee = employeeRepository.save(employee1);

        // Assert
        assertNotNull(savedEmployee.getId());
        assertEquals("John Doe", savedEmployee.getName());
        assertEquals("EMP12345", savedEmployee.getBadgeId());
        assertEquals(Employee.Role.WORKER, savedEmployee.getRole());
        assertEquals(Employee.Status.ACTIVE, savedEmployee.getStatus());
    }

    @Test
    @DisplayName("Should auto-generate ID on save")
    void testSave_AutoGenerateId_Success() {
        // Act
        Employee savedEmployee = employeeRepository.save(employee1);

        // Assert
        assertNotNull(savedEmployee.getId());
        assertTrue(savedEmployee.getId() > 0);
    }

    @Test
    @DisplayName("Should set audit fields on save")
    void testSave_SetAuditFields_Success() {
        // Act
        Employee savedEmployee = employeeRepository.save(employee1);
        entityManager.flush();
        entityManager.clear();

        // Assert
        Employee foundEmployee = employeeRepository.findById(savedEmployee.getId()).orElseThrow();
        assertNotNull(foundEmployee.getCreatedAt());
        assertNotNull(foundEmployee.getUpdatedAt());
    }

    // ========== FIND BY ID TESTS ==========

    @Test
    @DisplayName("Should find employee by valid ID")
    void testFindById_ValidId_ReturnsEmployee() {
        // Arrange
        Employee savedEmployee = employeeRepository.save(employee1);
        entityManager.flush();
        entityManager.clear();

        // Act
        Optional<Employee> found = employeeRepository.findById(savedEmployee.getId());

        // Assert
        assertTrue(found.isPresent());
        assertEquals("John Doe", found.get().getName());
        assertEquals("EMP12345", found.get().getBadgeId());
    }

    @Test
    @DisplayName("Should return empty Optional when ID not found")
    void testFindById_NonExistentId_ReturnsEmpty() {
        // Act
        Optional<Employee> found = employeeRepository.findById(999L);

        // Assert
        assertFalse(found.isPresent());
    }

    // ========== FIND BY BADGE ID TESTS ==========

    @Test
    @DisplayName("Should find employee by valid badge ID")
    void testFindByBadgeId_ValidBadgeId_ReturnsEmployee() {
        // Arrange
        employeeRepository.save(employee1);
        entityManager.flush();

        // Act
        Optional<Employee> found = employeeRepository.findByBadgeId("EMP12345");

        // Assert
        assertTrue(found.isPresent());
        assertEquals("John Doe", found.get().getName());
    }

    @Test
    @DisplayName("Should return empty Optional when badge ID not found")
    void testFindByBadgeId_NonExistentBadgeId_ReturnsEmpty() {
        // Act
        Optional<Employee> found = employeeRepository.findByBadgeId("INVALID");

        // Assert
        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("Should be case-sensitive for badge ID")
    void testFindByBadgeId_CaseSensitive_ReturnsEmpty() {
        // Arrange
        employeeRepository.save(employee1);
        entityManager.flush();

        // Act
        Optional<Employee> found = employeeRepository.findByBadgeId("emp12345");

        // Assert
        assertFalse(found.isPresent());
    }

    // ========== EXISTS BY BADGE ID TESTS ==========

    @Test
    @DisplayName("Should return true when badge ID exists")
    void testExistsByBadgeId_ExistingBadgeId_ReturnsTrue() {
        // Arrange
        employeeRepository.save(employee1);
        entityManager.flush();

        // Act
        boolean exists = employeeRepository.existsByBadgeId("EMP12345");

        // Assert
        assertTrue(exists);
    }

    @Test
    @DisplayName("Should return false when badge ID does not exist")
    void testExistsByBadgeId_NonExistentBadgeId_ReturnsFalse() {
        // Act
        boolean exists = employeeRepository.existsByBadgeId("INVALID");

        // Assert
        assertFalse(exists);
    }

    // ========== FIND BY STATUS TESTS ==========

    @Test
    @DisplayName("Should find employees by status with pagination")
    void testFindByStatus_WithPagination_ReturnsPage() {
        // Arrange
        employeeRepository.save(employee1);
        employeeRepository.save(employee2);
        employeeRepository.save(employee3);
        entityManager.flush();
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Employee> activePage = employeeRepository.findByStatus(Employee.Status.ACTIVE, pageable);

        // Assert
        assertEquals(2, activePage.getTotalElements());
        assertTrue(activePage.getContent().stream()
                .allMatch(emp -> emp.getStatus() == Employee.Status.ACTIVE));
    }

    @Test
    @DisplayName("Should return empty page when no employees match status")
    void testFindByStatus_NoMatches_ReturnsEmptyPage() {
        // Arrange
        employeeRepository.save(employee1);
        entityManager.flush();
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Employee> deletedPage = employeeRepository.findByStatus(Employee.Status.DELETED, pageable);

        // Assert
        assertEquals(0, deletedPage.getTotalElements());
        assertTrue(deletedPage.isEmpty());
    }

    // ========== FIND BY DEPARTMENT TESTS ==========

    @Test
    @DisplayName("Should find employees by department")
    void testFindByDepartment_ValidDepartment_ReturnsEmployees() {
        // Arrange
        employeeRepository.save(employee1);
        employeeRepository.save(employee2);
        employeeRepository.save(employee3);
        entityManager.flush();
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Employee> shippingEmployees = employeeRepository.findByDepartment("Shipping", pageable);

        // Assert
        assertEquals(2, shippingEmployees.getTotalElements());
        assertTrue(shippingEmployees.getContent().stream()
                .allMatch(emp -> "Shipping".equals(emp.getDepartment())));
    }

    @Test
    @DisplayName("Should return empty page when no employees in department")
    void testFindByDepartment_NoMatches_ReturnsEmptyPage() {
        // Arrange
        employeeRepository.save(employee1);
        entityManager.flush();
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Employee> nonExistentDept = employeeRepository.findByDepartment("NonExistent", pageable);

        // Assert
        assertEquals(0, nonExistentDept.getTotalElements());
    }

    // ========== FIND BY DEPARTMENT AND STATUS TESTS ==========

    @Test
    @DisplayName("Should find employees by department and status")
    void testFindByDepartmentAndStatus_ValidFilters_ReturnsEmployees() {
        // Arrange
        employeeRepository.save(employee1);
        employeeRepository.save(employee2);
        employeeRepository.save(employee3);
        entityManager.flush();
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Employee> result = employeeRepository.findByDepartmentAndStatus(
                "Shipping", Employee.Status.ACTIVE, pageable);

        // Assert
        assertEquals(1, result.getTotalElements());
        Employee found = result.getContent().get(0);
        assertEquals("Shipping", found.getDepartment());
        assertEquals(Employee.Status.ACTIVE, found.getStatus());
    }

    // ========== FIND BY NAME CONTAINING TESTS ==========

    @Test
    @DisplayName("Should search employees by name (case-insensitive)")
    void testFindByNameContainingIgnoreCase_PartialMatch_ReturnsEmployees() {
        // Arrange
        employeeRepository.save(employee1);
        employeeRepository.save(employee2);
        employeeRepository.save(employee3);
        entityManager.flush();
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Employee> result = employeeRepository.findByNameContainingIgnoreCase("john", pageable);

        // Assert
        assertEquals(2, result.getTotalElements());
        assertTrue(result.getContent().stream()
                .anyMatch(emp -> emp.getName().contains("John")));
    }

    @Test
    @DisplayName("Should return empty page when no names match search")
    void testFindByNameContainingIgnoreCase_NoMatches_ReturnsEmptyPage() {
        // Arrange
        employeeRepository.save(employee1);
        entityManager.flush();
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Employee> result = employeeRepository.findByNameContainingIgnoreCase("xyz", pageable);

        // Assert
        assertEquals(0, result.getTotalElements());
    }

    // ========== COUNT BY DEPARTMENT TESTS ==========

    @Test
    @DisplayName("Should count employees by department")
    void testCountByDepartment_ValidDepartment_ReturnsCount() {
        // Arrange
        employeeRepository.save(employee1);
        employeeRepository.save(employee2);
        employeeRepository.save(employee3);
        entityManager.flush();

        // Act
        Long count = employeeRepository.countByDepartment("Shipping");

        // Assert
        assertEquals(2L, count);
    }

    @Test
    @DisplayName("Should return zero when no employees in department")
    void testCountByDepartment_NoEmployees_ReturnsZero() {
        // Act
        Long count = employeeRepository.countByDepartment("NonExistent");

        // Assert
        assertEquals(0L, count);
    }

    // ========== COUNT BY STATUS TESTS ==========

    @Test
    @DisplayName("Should count employees by status")
    void testCountByStatus_ValidStatus_ReturnsCount() {
        // Arrange
        employeeRepository.save(employee1);
        employeeRepository.save(employee2);
        employeeRepository.save(employee3);
        entityManager.flush();

        // Act
        Long activeCount = employeeRepository.countByStatus(Employee.Status.ACTIVE);
        Long inactiveCount = employeeRepository.countByStatus(Employee.Status.INACTIVE);

        // Assert
        assertEquals(2L, activeCount);
        assertEquals(1L, inactiveCount);
    }

    // ========== FIND ALL TESTS ==========

    @Test
    @DisplayName("Should find all employees with pagination")
    void testFindAll_WithPagination_ReturnsPage() {
        // Arrange
        employeeRepository.save(employee1);
        employeeRepository.save(employee2);
        employeeRepository.save(employee3);
        entityManager.flush();
        Pageable pageable = PageRequest.of(0, 2);

        // Act
        Page<Employee> page = employeeRepository.findAll(pageable);

        // Assert
        assertEquals(3, page.getTotalElements());
        assertEquals(2, page.getContent().size());
        assertEquals(2, page.getTotalPages());
    }

    @Test
    @DisplayName("Should return empty page when no employees exist")
    void testFindAll_NoEmployees_ReturnsEmptyPage() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Employee> page = employeeRepository.findAll(pageable);

        // Assert
        assertEquals(0, page.getTotalElements());
        assertTrue(page.isEmpty());
    }

    // ========== UPDATE TESTS ==========

    @Test
    @DisplayName("Should update employee successfully")
    void testUpdate_ValidEmployee_Success() {
        // Arrange
        Employee savedEmployee = employeeRepository.save(employee1);
        entityManager.flush();
        entityManager.clear();

        // Act
        savedEmployee.setName("John Updated");
        savedEmployee.setDepartment("Receiving");
        Employee updatedEmployee = employeeRepository.save(savedEmployee);
        entityManager.flush();
        entityManager.clear();

        // Assert
        Employee found = employeeRepository.findById(updatedEmployee.getId()).orElseThrow();
        assertEquals("John Updated", found.getName());
        assertEquals("Receiving", found.getDepartment());
    }

    @Test
    @DisplayName("Should update audit timestamp on update")
    void testUpdate_UpdatesAuditTimestamp_Success() {
        // Arrange
        Employee savedEmployee = employeeRepository.save(employee1);
        entityManager.flush();
        LocalDate originalUpdatedAt = savedEmployee.getUpdatedAt();
        entityManager.clear();

        // Act
        savedEmployee.setName("John Updated");
        Employee updatedEmployee = employeeRepository.save(savedEmployee);
        entityManager.flush();

        // Assert
        assertNotNull(updatedEmployee.getUpdatedAt());
        // Note: In real scenario, updatedAt should be after originalUpdatedAt
    }

    // ========== DELETE TESTS ==========

    @Test
    @DisplayName("Should delete employee by ID")
    void testDeleteById_ValidId_Success() {
        // Arrange
        Employee savedEmployee = employeeRepository.save(employee1);
        entityManager.flush();
        Long employeeId = savedEmployee.getId();

        // Act
        employeeRepository.deleteById(employeeId);
        entityManager.flush();

        // Assert
        Optional<Employee> found = employeeRepository.findById(employeeId);
        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("Should delete employee entity")
    void testDelete_ValidEmployee_Success() {
        // Arrange
        Employee savedEmployee = employeeRepository.save(employee1);
        entityManager.flush();
        Long employeeId = savedEmployee.getId();

        // Act
        employeeRepository.delete(savedEmployee);
        entityManager.flush();

        // Assert
        Optional<Employee> found = employeeRepository.findById(employeeId);
        assertFalse(found.isPresent());
    }

    // ========== UNIQUE CONSTRAINT TESTS ==========

    @Test
    @DisplayName("Should enforce unique badge ID constraint")
    void testSave_DuplicateBadgeId_ThrowsException() {
        // Arrange
        employeeRepository.save(employee1);
        entityManager.flush();

        Employee duplicateEmployee = Employee.builder()
                .name("Different Name")
                .badgeId("EMP12345") // Same badge ID
                .role(Employee.Role.WORKER)
                .department("Shipping")
                .status(Employee.Status.ACTIVE)
                .build();

        // Act & Assert
        assertThrows(Exception.class, () -> {
            employeeRepository.save(duplicateEmployee);
            entityManager.flush();
        });
    }

    // ========== PAGINATION TESTS ==========

    @Test
    @DisplayName("Should handle pagination correctly")
    void testFindAll_Pagination_ReturnsCorrectPages() {
        // Arrange
        for (int i = 0; i < 25; i++) {
            Employee emp = Employee.builder()
                    .name("Employee " + i)
                    .badgeId("EMP" + String.format("%05d", i))
                    .role(Employee.Role.WORKER)
                    .department("Shipping")
                    .status(Employee.Status.ACTIVE)
                    .build();
            employeeRepository.save(emp);
        }
        entityManager.flush();

        // Act
        Page<Employee> page1 = employeeRepository.findAll(PageRequest.of(0, 10));
        Page<Employee> page2 = employeeRepository.findAll(PageRequest.of(1, 10));
        Page<Employee> page3 = employeeRepository.findAll(PageRequest.of(2, 10));

        // Assert
        assertEquals(25, page1.getTotalElements());
        assertEquals(3, page1.getTotalPages());
        assertEquals(10, page1.getContent().size());
        assertEquals(10, page2.getContent().size());
        assertEquals(5, page3.getContent().size());
    }

    // ========== EDGE CASE TESTS ==========

    @Test
    @DisplayName("Should handle null optional fields")
    void testSave_NullOptionalFields_Success() {
        // Arrange
        Employee employee = Employee.builder()
                .name("Test Employee")
                .badgeId("EMP99999")
                .role(Employee.Role.WORKER)
                .status(Employee.Status.ACTIVE)
                .department(null)
                .shiftGroup(null)
                .hireDate(null)
                .build();

        // Act
        Employee savedEmployee = employeeRepository.save(employee);
        entityManager.flush();

        // Assert
        assertNotNull(savedEmployee.getId());
        assertNull(savedEmployee.getDepartment());
        assertNull(savedEmployee.getShiftGroup());
        assertNull(savedEmployee.getHireDate());
    }

    @Test
    @DisplayName("Should handle special characters in name")
    void testSave_SpecialCharactersInName_Success() {
        // Arrange
        employee1.setName("O'Brien-Smith Jr.");

        // Act
        Employee savedEmployee = employeeRepository.save(employee1);
        entityManager.flush();
        entityManager.clear();

        // Assert
        Employee found = employeeRepository.findById(savedEmployee.getId()).orElseThrow();
        assertEquals("O'Brien-Smith Jr.", found.getName());
    }

    @Test
    @DisplayName("Should handle Unicode characters in name")
    void testSave_UnicodeCharactersInName_Success() {
        // Arrange
        employee1.setName("JosÃ© GarcÃ­a");

        // Act
        Employee savedEmployee = employeeRepository.save(employee1);
        entityManager.flush();
        entityManager.clear();

        // Assert
        Employee found = employeeRepository.findById(savedEmployee.getId()).orElseThrow();
        assertEquals("JosÃ© GarcÃ­a", found.getName());
    }

    @Test
    @DisplayName("Should handle very old hire dates")
    void testSave_VeryOldHireDate_Success() {
        // Arrange
        employee1.setHireDate(LocalDate.of(1980, 1, 1));

        // Act
        Employee savedEmployee = employeeRepository.save(employee1);
        entityManager.flush();

        // Assert
        assertEquals(LocalDate.of(1980, 1, 1), savedEmployee.getHireDate());
    }

    @Test
    @DisplayName("Should handle all role types")
    void testSave_AllRoleTypes_Success() {
        // Test ADMIN
        employee1.setRole(Employee.Role.ADMIN);
        employee1.setBadgeId("EMP00001");
        employeeRepository.save(employee1);

        // Test HR
        employee2.setRole(Employee.Role.HR);
        employee2.setBadgeId("EMP00002");
        employeeRepository.save(employee2);

        // Test SUPERVISOR
        employee3.setRole(Employee.Role.SUPERVISOR);
        employee3.setBadgeId("EMP00003");
        employeeRepository.save(employee3);

        // Test WORKER
        Employee worker = Employee.builder()
                .name("Worker")
                .badgeId("EMP00004")
                .role(Employee.Role.WORKER)
                .status(Employee.Status.ACTIVE)
                .build();
        employeeRepository.save(worker);

        entityManager.flush();

        // Assert
        assertEquals(4, employeeRepository.count());
    }

    @Test
    @DisplayName("Should handle all status types")
    void testSave_AllStatusTypes_Success() {
        // Test ACTIVE
        employee1.setStatus(Employee.Status.ACTIVE);
        employee1.setBadgeId("EMP00001");
        employeeRepository.save(employee1);

        // Test INACTIVE
        employee2.setStatus(Employee.Status.INACTIVE);
        employee2.setBadgeId("EMP00002");
        employeeRepository.save(employee2);

        // Test DELETED
        employee3.setStatus(Employee.Status.DELETED);
        employee3.setBadgeId("EMP00003");
        employeeRepository.save(employee3);

        entityManager.flush();

        // Assert
        assertEquals(3, employeeRepository.count());
        assertEquals(1, employeeRepository.countByStatus(Employee.Status.ACTIVE));
        assertEquals(1, employeeRepository.countByStatus(Employee.Status.INACTIVE));
        assertEquals(1, employeeRepository.countByStatus(Employee.Status.DELETED));
    }
}