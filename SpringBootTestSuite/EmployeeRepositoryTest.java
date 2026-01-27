package com.warehouse.ems.repository.employee;

import com.warehouse.ems.domain.employee.Employee;
import com.warehouse.ems.domain.employee.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for EmployeeRepository.
 * Tests cover all database operations, queries, and edge cases.
 */
@DataJpaTest
public class EmployeeRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private EmployeeRepository employeeRepository;

    private Employee testEmployee;

    @BeforeEach
    public void setUp() {
        testEmployee = Employee.builder()
                .badgeId("EMP001")
                .name("John Doe")
                .role(Role.WORKER)
                .department("Warehouse")
                .shiftGroup("Morning")
                .hireDate(LocalDate.of(2023, 1, 15))
                .status("ACTIVE")
                .deleted(false)
                .build();
    }

    // ==================== SAVE TESTS ====================

    @Test
    public void testSave_ValidEmployee_Success() {
        // Act
        Employee savedEmployee = employeeRepository.save(testEmployee);

        // Assert
        assertNotNull(savedEmployee.getId());
        assertEquals("EMP001", savedEmployee.getBadgeId());
        assertEquals("John Doe", savedEmployee.getName());
        assertNotNull(savedEmployee.getCreatedAt());
        assertNotNull(savedEmployee.getUpdatedAt());
    }

    @Test
    public void testSave_DuplicateBadgeId_ThrowsException() {
        // Arrange
        employeeRepository.save(testEmployee);
        entityManager.flush();

        Employee duplicateEmployee = Employee.builder()
                .badgeId("EMP001")
                .name("Jane Doe")
                .role(Role.WORKER)
                .department("Warehouse")
                .build();

        // Act & Assert
        assertThrows(Exception.class, () -> {
            employeeRepository.save(duplicateEmployee);
            entityManager.flush();
        });
    }

    @Test
    public void testSave_NullBadgeId_ThrowsException() {
        // Arrange
        testEmployee.setBadgeId(null);

        // Act & Assert
        assertThrows(Exception.class, () -> {
            employeeRepository.save(testEmployee);
            entityManager.flush();
        });
    }

    @Test
    public void testSave_NullName_ThrowsException() {
        // Arrange
        testEmployee.setName(null);

        // Act & Assert
        assertThrows(Exception.class, () -> {
            employeeRepository.save(testEmployee);
            entityManager.flush();
        });
    }

    @Test
    public void testSave_AllRoles_Success() {
        // Test each role
        for (Role role : Role.values()) {
            Employee employee = Employee.builder()
                    .badgeId("EMP" + role.name())
                    .name("Test " + role.name())
                    .role(role)
                    .department("Warehouse")
                    .build();

            Employee saved = employeeRepository.save(employee);
            assertNotNull(saved.getId());
            assertEquals(role, saved.getRole());
        }
    }

    // ==================== FIND BY ID TESTS ====================

    @Test
    public void testFindById_ExistingEmployee_Success() {
        // Arrange
        Employee savedEmployee = employeeRepository.save(testEmployee);
        entityManager.flush();

        // Act
        Optional<Employee> found = employeeRepository.findById(savedEmployee.getId());

        // Assert
        assertTrue(found.isPresent());
        assertEquals("EMP001", found.get().getBadgeId());
    }

    @Test
    public void testFindById_NonExistentEmployee_ReturnsEmpty() {
        // Act
        Optional<Employee> found = employeeRepository.findById(999L);

        // Assert
        assertFalse(found.isPresent());
    }

    @Test
    public void testFindById_DeletedEmployee_NotFound() {
        // Arrange
        testEmployee.setDeleted(true);
        Employee savedEmployee = employeeRepository.save(testEmployee);
        entityManager.flush();

        // Act
        Optional<Employee> found = employeeRepository.findById(savedEmployee.getId());

        // Assert - @Where clause should filter out deleted records
        assertFalse(found.isPresent());
    }

    // ==================== FIND BY BADGE ID TESTS ====================

    @Test
    public void testFindByBadgeId_ExistingEmployee_Success() {
        // Arrange
        employeeRepository.save(testEmployee);
        entityManager.flush();

        // Act
        Optional<Employee> found = employeeRepository.findByBadgeId("EMP001");

        // Assert
        assertTrue(found.isPresent());
        assertEquals("John Doe", found.get().getName());
    }

    @Test
    public void testFindByBadgeId_NonExistentBadgeId_ReturnsEmpty() {
        // Act
        Optional<Employee> found = employeeRepository.findByBadgeId("NONEXISTENT");

        // Assert
        assertFalse(found.isPresent());
    }

    @Test
    public void testFindByBadgeId_DeletedEmployee_ReturnsEmpty() {
        // Arrange
        testEmployee.setDeleted(true);
        employeeRepository.save(testEmployee);
        entityManager.flush();

        // Act
        Optional<Employee> found = employeeRepository.findByBadgeId("EMP001");

        // Assert
        assertFalse(found.isPresent());
    }

    @Test
    public void testFindByBadgeId_CaseSensitive_Success() {
        // Arrange
        employeeRepository.save(testEmployee);
        entityManager.flush();

        // Act
        Optional<Employee> found = employeeRepository.findByBadgeId("emp001");

        // Assert - should be case-sensitive
        assertFalse(found.isPresent());
    }

    // ==================== FIND ALL BY DEPARTMENT TESTS ====================

    @Test
    public void testFindAllByDepartment_ExistingDepartment_Success() {
        // Arrange
        employeeRepository.save(testEmployee);
        
        Employee employee2 = Employee.builder()
                .badgeId("EMP002")
                .name("Jane Doe")
                .role(Role.WORKER)
                .department("Warehouse")
                .build();
        employeeRepository.save(employee2);
        entityManager.flush();

        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Employee> result = employeeRepository.findAllByDepartment("Warehouse", pageable);

        // Assert
        assertEquals(2, result.getTotalElements());
    }

    @Test
    public void testFindAllByDepartment_NonExistentDepartment_ReturnsEmpty() {
        // Arrange
        employeeRepository.save(testEmployee);
        entityManager.flush();

        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Employee> result = employeeRepository.findAllByDepartment("NonExistent", pageable);

        // Assert
        assertEquals(0, result.getTotalElements());
    }

    @Test
    public void testFindAllByDepartment_ExcludesDeleted_Success() {
        // Arrange
        employeeRepository.save(testEmployee);
        
        Employee deletedEmployee = Employee.builder()
                .badgeId("EMP002")
                .name("Deleted Employee")
                .role(Role.WORKER)
                .department("Warehouse")
                .deleted(true)
                .build();
        employeeRepository.save(deletedEmployee);
        entityManager.flush();

        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Employee> result = employeeRepository.findAllByDepartment("Warehouse", pageable);

        // Assert
        assertEquals(1, result.getTotalElements());
    }

    @Test
    public void testFindAllByDepartment_Pagination_Success() {
        // Arrange
        for (int i = 1; i <= 5; i++) {
            Employee employee = Employee.builder()
                    .badgeId("EMP00" + i)
                    .name("Employee " + i)
                    .role(Role.WORKER)
                    .department("Warehouse")
                    .build();
            employeeRepository.save(employee);
        }
        entityManager.flush();

        Pageable firstPage = PageRequest.of(0, 2);
        Pageable secondPage = PageRequest.of(1, 2);

        // Act
        Page<Employee> page1 = employeeRepository.findAllByDepartment("Warehouse", firstPage);
        Page<Employee> page2 = employeeRepository.findAllByDepartment("Warehouse", secondPage);

        // Assert
        assertEquals(2, page1.getNumberOfElements());
        assertEquals(2, page2.getNumberOfElements());
        assertEquals(5, page1.getTotalElements());
    }

    // ==================== FIND ALL TESTS ====================

    @Test
    public void testFindAll_MultipleEmployees_Success() {
        // Arrange
        employeeRepository.save(testEmployee);
        
        Employee employee2 = Employee.builder()
                .badgeId("EMP002")
                .name("Jane Doe")
                .role(Role.SUPERVISOR)
                .department("Office")
                .build();
        employeeRepository.save(employee2);
        entityManager.flush();

        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Employee> result = employeeRepository.findAll(pageable);

        // Assert
        assertEquals(2, result.getTotalElements());
    }

    @Test
    public void testFindAll_EmptyDatabase_ReturnsEmpty() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Employee> result = employeeRepository.findAll(pageable);

        // Assert
        assertEquals(0, result.getTotalElements());
    }

    @Test
    public void testFindAll_ExcludesDeleted_Success() {
        // Arrange
        employeeRepository.save(testEmployee);
        
        Employee deletedEmployee = Employee.builder()
                .badgeId("EMP002")
                .name("Deleted Employee")
                .role(Role.WORKER)
                .department("Warehouse")
                .deleted(true)
                .build();
        employeeRepository.save(deletedEmployee);
        entityManager.flush();

        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Employee> result = employeeRepository.findAll(pageable);

        // Assert
        assertEquals(1, result.getTotalElements());
    }

    // ==================== UPDATE TESTS ====================

    @Test
    public void testUpdate_ExistingEmployee_Success() {
        // Arrange
        Employee savedEmployee = employeeRepository.save(testEmployee);
        entityManager.flush();

        // Act
        savedEmployee.setName("Updated Name");
        savedEmployee.setDepartment("Updated Department");
        Employee updatedEmployee = employeeRepository.save(savedEmployee);
        entityManager.flush();

        // Assert
        assertEquals("Updated Name", updatedEmployee.getName());
        assertEquals("Updated Department", updatedEmployee.getDepartment());
        assertNotEquals(updatedEmployee.getCreatedAt(), updatedEmployee.getUpdatedAt());
    }

    @Test
    public void testUpdate_ChangeBadgeId_Success() {
        // Arrange
        Employee savedEmployee = employeeRepository.save(testEmployee);
        entityManager.flush();

        // Act
        savedEmployee.setBadgeId("EMP999");
        Employee updatedEmployee = employeeRepository.save(savedEmployee);
        entityManager.flush();

        // Assert
        assertEquals("EMP999", updatedEmployee.getBadgeId());
    }

    // ==================== DELETE TESTS ====================

    @Test
    public void testDelete_ExistingEmployee_SoftDelete() {
        // Arrange
        Employee savedEmployee = employeeRepository.save(testEmployee);
        entityManager.flush();
        Long employeeId = savedEmployee.getId();

        // Act
        employeeRepository.delete(savedEmployee);
        entityManager.flush();
        entityManager.clear();

        // Assert - @SQLDelete should set deleted=true
        Optional<Employee> found = employeeRepository.findById(employeeId);
        assertFalse(found.isPresent());
    }

    @Test
    public void testDeleteById_ExistingEmployee_SoftDelete() {
        // Arrange
        Employee savedEmployee = employeeRepository.save(testEmployee);
        entityManager.flush();
        Long employeeId = savedEmployee.getId();

        // Act
        employeeRepository.deleteById(employeeId);
        entityManager.flush();
        entityManager.clear();

        // Assert
        Optional<Employee> found = employeeRepository.findById(employeeId);
        assertFalse(found.isPresent());
    }

    // ==================== EDGE CASE TESTS ====================

    @Test
    public void testSave_MaxLengthBadgeId_Success() {
        // Arrange
        testEmployee.setBadgeId("A".repeat(32));

        // Act
        Employee savedEmployee = employeeRepository.save(testEmployee);

        // Assert
        assertNotNull(savedEmployee.getId());
        assertEquals(32, savedEmployee.getBadgeId().length());
    }

    @Test
    public void testSave_MaxLengthName_Success() {
        // Arrange
        testEmployee.setName("B".repeat(128));

        // Act
        Employee savedEmployee = employeeRepository.save(testEmployee);

        // Assert
        assertNotNull(savedEmployee.getId());
        assertEquals(128, savedEmployee.getName().length());
    }

    @Test
    public void testSave_SpecialCharactersInName_Success() {
        // Arrange
        testEmployee.setName("O'Brien-Smith Jr. (Temp)");

        // Act
        Employee savedEmployee = employeeRepository.save(testEmployee);

        // Assert
        assertNotNull(savedEmployee.getId());
        assertEquals("O'Brien-Smith Jr. (Temp)", savedEmployee.getName());
    }

    @Test
    public void testSave_FutureHireDate_Success() {
        // Arrange
        testEmployee.setHireDate(LocalDate.now().plusDays(30));

        // Act
        Employee savedEmployee = employeeRepository.save(testEmployee);

        // Assert
        assertNotNull(savedEmployee.getId());
        assertTrue(savedEmployee.getHireDate().isAfter(LocalDate.now()));
    }

    @Test
    public void testSave_PastHireDate_Success() {
        // Arrange
        testEmployee.setHireDate(LocalDate.of(2000, 1, 1));

        // Act
        Employee savedEmployee = employeeRepository.save(testEmployee);

        // Assert
        assertNotNull(savedEmployee.getId());
        assertEquals(LocalDate.of(2000, 1, 1), savedEmployee.getHireDate());
    }

    @Test
    public void testFindAll_LargePageSize_Success() {
        // Arrange
        for (int i = 1; i <= 100; i++) {
            Employee employee = Employee.builder()
                    .badgeId("EMP" + String.format("%03d", i))
                    .name("Employee " + i)
                    .role(Role.WORKER)
                    .department("Warehouse")
                    .build();
            employeeRepository.save(employee);
        }
        entityManager.flush();

        Pageable pageable = PageRequest.of(0, 1000);

        // Act
        Page<Employee> result = employeeRepository.findAll(pageable);

        // Assert
        assertEquals(100, result.getTotalElements());
    }
}