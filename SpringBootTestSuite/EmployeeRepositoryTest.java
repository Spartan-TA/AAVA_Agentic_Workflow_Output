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
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive JUnit test suite for EmployeeRepository
 * Tests cover database operations, custom queries, and specifications
 */
@DataJpaTest
class EmployeeRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private EmployeeRepository employeeRepository;

    private Employee testEmployee1;
    private Employee testEmployee2;
    private Employee testEmployee3;

    @BeforeEach
    void setUp() {
        // Arrange - Setup test data
        testEmployee1 = Employee.builder()
                .name("John Doe")
                .badgeId("EMP001")
                .role("WORKER")
                .department("Warehouse")
                .shiftGroup("Day Shift")
                .hireDate(LocalDate.of(2023, 1, 15))
                .status("ACTIVE")
                .build();

        testEmployee2 = Employee.builder()
                .name("Jane Smith")
                .badgeId("EMP002")
                .role("SUPERVISOR")
                .department("Logistics")
                .shiftGroup("Night Shift")
                .hireDate(LocalDate.of(2022, 6, 10))
                .status("ACTIVE")
                .build();

        testEmployee3 = Employee.builder()
                .name("Bob Johnson")
                .badgeId("EMP003")
                .role("WORKER")
                .department("Warehouse")
                .shiftGroup("Day Shift")
                .hireDate(LocalDate.of(2021, 3, 20))
                .status("INACTIVE")
                .build();

        entityManager.persist(testEmployee1);
        entityManager.persist(testEmployee2);
        entityManager.persist(testEmployee3);
        entityManager.flush();
    }

    // ========== SAVE TESTS ==========

    @Test
    void testSave_ValidEmployee_Success() {
        // Arrange
        Employee newEmployee = Employee.builder()
                .name("Alice Brown")
                .badgeId("EMP004")
                .role("HR")
                .department("Administration")
                .shiftGroup("Day Shift")
                .hireDate(LocalDate.now())
                .status("ACTIVE")
                .build();

        // Act
        Employee savedEmployee = employeeRepository.save(newEmployee);

        // Assert
        assertNotNull(savedEmployee.getId());
        assertEquals("Alice Brown", savedEmployee.getName());
        assertEquals("EMP004", savedEmployee.getBadgeId());
    }

    @Test
    void testSave_NullName_ThrowsException() {
        // Arrange
        Employee invalidEmployee = Employee.builder()
                .name(null)
                .badgeId("EMP005")
                .role("WORKER")
                .department("Warehouse")
                .build();

        // Act & Assert
        assertThrows(Exception.class, () -> {
            employeeRepository.save(invalidEmployee);
            entityManager.flush();
        });
    }

    @Test
    void testSave_DuplicateBadgeId_ThrowsException() {
        // Arrange
        Employee duplicateEmployee = Employee.builder()
                .name("Duplicate Employee")
                .badgeId("EMP001") // Duplicate badge ID
                .role("WORKER")
                .department("Warehouse")
                .hireDate(LocalDate.now())
                .status("ACTIVE")
                .build();

        // Act & Assert
        assertThrows(Exception.class, () -> {
            employeeRepository.save(duplicateEmployee);
            entityManager.flush();
        });
    }

    // ========== FIND BY ID TESTS ==========

    @Test
    void testFindById_ExistingId_Success() {
        // Act
        Optional<Employee> found = employeeRepository.findById(testEmployee1.getId());

        // Assert
        assertTrue(found.isPresent());
        assertEquals("John Doe", found.get().getName());
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
    void testFindById_NullId_ReturnsEmpty() {
        // Act
        Optional<Employee> found = employeeRepository.findById(null);

        // Assert
        assertFalse(found.isPresent());
    }

    // ========== FIND BY BADGE ID TESTS ==========

    @Test
    void testFindByBadgeId_ExistingBadgeId_Success() {
        // Act
        Optional<Employee> found = employeeRepository.findByBadgeId("EMP001");

        // Assert
        assertTrue(found.isPresent());
        assertEquals("John Doe", found.get().getName());
    }

    @Test
    void testFindByBadgeId_NonExistentBadgeId_ReturnsEmpty() {
        // Act
        Optional<Employee> found = employeeRepository.findByBadgeId("INVALID");

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

    // ========== EXISTS BY BADGE ID TESTS ==========

    @Test
    void testExistsByBadgeId_ExistingBadgeId_ReturnsTrue() {
        // Act
        boolean exists = employeeRepository.existsByBadgeId("EMP001");

        // Assert
        assertTrue(exists);
    }

    @Test
    void testExistsByBadgeId_NonExistentBadgeId_ReturnsFalse() {
        // Act
        boolean exists = employeeRepository.existsByBadgeId("INVALID");

        // Assert
        assertFalse(exists);
    }

    @Test
    void testExistsByBadgeId_NullBadgeId_ReturnsFalse() {
        // Act
        boolean exists = employeeRepository.existsByBadgeId(null);

        // Assert
        assertFalse(exists);
    }

    // ========== EXISTS BY BADGE ID AND ID NOT TESTS ==========

    @Test
    void testExistsByBadgeIdAndIdNot_DifferentEmployee_ReturnsTrue() {
        // Act
        boolean exists = employeeRepository.existsByBadgeIdAndIdNot("EMP001", testEmployee2.getId());

        // Assert
        assertTrue(exists);
    }

    @Test
    void testExistsByBadgeIdAndIdNot_SameEmployee_ReturnsFalse() {
        // Act
        boolean exists = employeeRepository.existsByBadgeIdAndIdNot("EMP001", testEmployee1.getId());

        // Assert
        assertFalse(exists);
    }

    @Test
    void testExistsByBadgeIdAndIdNot_NonExistentBadgeId_ReturnsFalse() {
        // Act
        boolean exists = employeeRepository.existsByBadgeIdAndIdNot("INVALID", testEmployee1.getId());

        // Assert
        assertFalse(exists);
    }

    // ========== FIND BY DEPARTMENT TESTS ==========

    @Test
    void testFindByDepartment_ExistingDepartment_Success() {
        // Act
        List<Employee> employees = employeeRepository.findByDepartment("Warehouse");

        // Assert
        assertEquals(2, employees.size());
        assertTrue(employees.stream().allMatch(e -> "Warehouse".equals(e.getDepartment())));
    }

    @Test
    void testFindByDepartment_NonExistentDepartment_ReturnsEmpty() {
        // Act
        List<Employee> employees = employeeRepository.findByDepartment("NonExistent");

        // Assert
        assertTrue(employees.isEmpty());
    }

    @Test
    void testFindByDepartment_NullDepartment_ReturnsEmpty() {
        // Act
        List<Employee> employees = employeeRepository.findByDepartment(null);

        // Assert
        assertTrue(employees.isEmpty());
    }

    // ========== FIND BY STATUS TESTS ==========

    @Test
    void testFindByStatus_ActiveEmployees_Success() {
        // Act
        List<Employee> employees = employeeRepository.findByStatus("ACTIVE");

        // Assert
        assertEquals(2, employees.size());
        assertTrue(employees.stream().allMatch(e -> "ACTIVE".equals(e.getStatus())));
    }

    @Test
    void testFindByStatus_InactiveEmployees_Success() {
        // Act
        List<Employee> employees = employeeRepository.findByStatus("INACTIVE");

        // Assert
        assertEquals(1, employees.size());
        assertEquals("Bob Johnson", employees.get(0).getName());
    }

    @Test
    void testFindByStatus_NonExistentStatus_ReturnsEmpty() {
        // Act
        List<Employee> employees = employeeRepository.findByStatus("INVALID");

        // Assert
        assertTrue(employees.isEmpty());
    }

    // ========== FIND BY ROLE TESTS ==========

    @Test
    void testFindByRole_WorkerRole_Success() {
        // Act
        List<Employee> employees = employeeRepository.findByRole("WORKER");

        // Assert
        assertEquals(2, employees.size());
        assertTrue(employees.stream().allMatch(e -> "WORKER".equals(e.getRole())));
    }

    @Test
    void testFindByRole_SupervisorRole_Success() {
        // Act
        List<Employee> employees = employeeRepository.findByRole("SUPERVISOR");

        // Assert
        assertEquals(1, employees.size());
        assertEquals("Jane Smith", employees.get(0).getName());
    }

    @Test
    void testFindByRole_NonExistentRole_ReturnsEmpty() {
        // Act
        List<Employee> employees = employeeRepository.findByRole("ADMIN");

        // Assert
        assertTrue(employees.isEmpty());
    }

    // ========== FIND ALL WITH SPECIFICATION TESTS ==========

    @Test
    void testFindAll_WithDepartmentSpecification_Success() {
        // Arrange
        Specification<Employee> spec = (root, query, cb) -> 
            cb.equal(root.get("department"), "Warehouse");
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Employee> result = employeeRepository.findAll(spec, pageable);

        // Assert
        assertEquals(2, result.getTotalElements());
        assertTrue(result.getContent().stream().allMatch(e -> "Warehouse".equals(e.getDepartment())));
    }

    @Test
    void testFindAll_WithStatusSpecification_Success() {
        // Arrange
        Specification<Employee> spec = (root, query, cb) -> 
            cb.equal(root.get("status"), "ACTIVE");
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Employee> result = employeeRepository.findAll(spec, pageable);

        // Assert
        assertEquals(2, result.getTotalElements());
        assertTrue(result.getContent().stream().allMatch(e -> "ACTIVE".equals(e.getStatus())));
    }

    @Test
    void testFindAll_WithMultipleSpecifications_Success() {
        // Arrange
        Specification<Employee> spec = (root, query, cb) -> 
            cb.and(
                cb.equal(root.get("department"), "Warehouse"),
                cb.equal(root.get("status"), "ACTIVE")
            );
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Employee> result = employeeRepository.findAll(spec, pageable);

        // Assert
        assertEquals(1, result.getTotalElements());
        assertEquals("John Doe", result.getContent().get(0).getName());
    }

    @Test
    void testFindAll_WithPagination_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 2);

        // Act
        Page<Employee> result = employeeRepository.findAll(pageable);

        // Assert
        assertEquals(3, result.getTotalElements());
        assertEquals(2, result.getContent().size());
        assertEquals(2, result.getTotalPages());
    }

    @Test
    void testFindAll_SecondPage_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(1, 2);

        // Act
        Page<Employee> result = employeeRepository.findAll(pageable);

        // Assert
        assertEquals(3, result.getTotalElements());
        assertEquals(1, result.getContent().size());
    }

    // ========== DELETE TESTS ==========

    @Test
    void testDelete_ExistingEmployee_Success() {
        // Arrange
        Long employeeId = testEmployee1.getId();

        // Act
        employeeRepository.deleteById(employeeId);
        entityManager.flush();

        // Assert
        Optional<Employee> deleted = employeeRepository.findById(employeeId);
        assertFalse(deleted.isPresent());
    }

    @Test
    void testDeleteById_NonExistentId_NoException() {
        // Act & Assert - Should not throw exception
        assertDoesNotThrow(() -> {
            employeeRepository.deleteById(999L);
            entityManager.flush();
        });
    }

    // ========== COUNT TESTS ==========

    @Test
    void testCount_AllEmployees_Success() {
        // Act
        long count = employeeRepository.count();

        // Assert
        assertEquals(3, count);
    }

    @Test
    void testCount_WithSpecification_Success() {
        // Arrange
        Specification<Employee> spec = (root, query, cb) -> 
            cb.equal(root.get("status"), "ACTIVE");

        // Act
        long count = employeeRepository.count(spec);

        // Assert
        assertEquals(2, count);
    }

    // ========== UPDATE TESTS ==========

    @Test
    void testUpdate_ExistingEmployee_Success() {
        // Arrange
        Employee employee = employeeRepository.findById(testEmployee1.getId()).orElseThrow();
        employee.setName("John Updated");
        employee.setDepartment("Logistics");

        // Act
        Employee updated = employeeRepository.save(employee);
        entityManager.flush();

        // Assert
        assertEquals("John Updated", updated.getName());
        assertEquals("Logistics", updated.getDepartment());
    }

    @Test
    void testUpdate_ChangeStatus_Success() {
        // Arrange
        Employee employee = employeeRepository.findById(testEmployee1.getId()).orElseThrow();
        employee.setStatus("INACTIVE");

        // Act
        Employee updated = employeeRepository.save(employee);
        entityManager.flush();

        // Assert
        assertEquals("INACTIVE", updated.getStatus());
    }
}