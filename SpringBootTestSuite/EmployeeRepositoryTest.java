package com.warehousemgmt.repository;

import com.warehousemgmt.domain.Employee;
import com.warehousemgmt.domain.EmployeeStatus;
import com.warehousemgmt.domain.Role;
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
 * Comprehensive JUnit test suite for EmployeeRepository
 * Covers JPA queries, pagination, data integrity, and database constraints
 */
@DataJpaTest
public class EmployeeRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private EmployeeRepository employeeRepository;

    private Employee validEmployee;

    @BeforeEach
    public void setUp() {
        validEmployee = new Employee();
        validEmployee.setName("John Doe");
        validEmployee.setBadgeId("EMP001");
        validEmployee.setRole(Role.WORKER);
        validEmployee.setDepartment("Warehouse");
        validEmployee.setShiftGroup("Morning");
        validEmployee.setHireDate(LocalDate.of(2023, 1, 15));
        validEmployee.setStatus(EmployeeStatus.ACTIVE);
        validEmployee.setDeleted(false);
    }

    // ========== SAVE TESTS ==========

    @Test
    public void testSaveEmployee_ValidData_Success() {
        // Act
        Employee saved = employeeRepository.save(validEmployee);
        entityManager.flush();

        // Assert
        assertNotNull(saved.getId());
        assertEquals("John Doe", saved.getName());
        assertEquals("EMP001", saved.getBadgeId());
    }

    @Test
    public void testSaveEmployee_DuplicateBadgeId_ThrowsException() {
        // Arrange
        employeeRepository.save(validEmployee);
        entityManager.flush();

        Employee duplicate = new Employee();
        duplicate.setName("Jane Doe");
        duplicate.setBadgeId("EMP001"); // Duplicate badge ID
        duplicate.setRole(Role.WORKER);
        duplicate.setDepartment("Warehouse");
        duplicate.setHireDate(LocalDate.now());
        duplicate.setStatus(EmployeeStatus.ACTIVE);

        // Act & Assert
        assertThrows(Exception.class, () -> {
            employeeRepository.save(duplicate);
            entityManager.flush();
        });
    }

    @Test
    public void testSaveEmployee_NullName_ThrowsException() {
        // Arrange
        validEmployee.setName(null);

        // Act & Assert
        assertThrows(Exception.class, () -> {
            employeeRepository.save(validEmployee);
            entityManager.flush();
        });
    }

    @Test
    public void testSaveEmployee_NullBadgeId_ThrowsException() {
        // Arrange
        validEmployee.setBadgeId(null);

        // Act & Assert
        assertThrows(Exception.class, () -> {
            employeeRepository.save(validEmployee);
            entityManager.flush();
        });
    }

    @Test
    public void testSaveEmployee_MaxLengthFields_Success() {
        // Arrange
        validEmployee.setName("A".repeat(255));
        validEmployee.setDepartment("D".repeat(100));

        // Act
        Employee saved = employeeRepository.save(validEmployee);
        entityManager.flush();

        // Assert
        assertNotNull(saved.getId());
        assertEquals(255, saved.getName().length());
    }

    // ========== FIND BY BADGE ID TESTS ==========

    @Test
    public void testFindByBadgeId_ExistingBadgeId_ReturnsEmployee() {
        // Arrange
        employeeRepository.save(validEmployee);
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
    public void testFindByBadgeId_NullBadgeId_ReturnsEmpty() {
        // Act
        Optional<Employee> found = employeeRepository.findByBadgeId(null);

        // Assert
        assertFalse(found.isPresent());
    }

    @Test
    public void testFindByBadgeId_EmptyBadgeId_ReturnsEmpty() {
        // Act
        Optional<Employee> found = employeeRepository.findByBadgeId("");

        // Assert
        assertFalse(found.isPresent());
    }

    @Test
    public void testFindByBadgeId_CaseSensitive_ReturnsEmpty() {
        // Arrange
        employeeRepository.save(validEmployee);
        entityManager.flush();

        // Act
        Optional<Employee> found = employeeRepository.findByBadgeId("emp001");

        // Assert
        assertFalse(found.isPresent());
    }

    // ========== FIND ALL BY DELETED FALSE TESTS ==========

    @Test
    public void testFindAllByDeletedFalse_ActiveEmployees_ReturnsPage() {
        // Arrange
        employeeRepository.save(validEmployee);
        
        Employee employee2 = new Employee();
        employee2.setName("Jane Doe");
        employee2.setBadgeId("EMP002");
        employee2.setRole(Role.SUPERVISOR);
        employee2.setDepartment("Warehouse");
        employee2.setHireDate(LocalDate.now());
        employee2.setStatus(EmployeeStatus.ACTIVE);
        employee2.setDeleted(false);
        employeeRepository.save(employee2);
        
        entityManager.flush();
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Employee> page = employeeRepository.findAllByDeletedFalse(pageable);

        // Assert
        assertEquals(2, page.getTotalElements());
        assertEquals(2, page.getContent().size());
    }

    @Test
    public void testFindAllByDeletedFalse_ExcludesDeletedEmployees_ReturnsOnlyActive() {
        // Arrange
        employeeRepository.save(validEmployee);
        
        Employee deletedEmployee = new Employee();
        deletedEmployee.setName("Deleted Employee");
        deletedEmployee.setBadgeId("EMP999");
        deletedEmployee.setRole(Role.WORKER);
        deletedEmployee.setDepartment("Warehouse");
        deletedEmployee.setHireDate(LocalDate.now());
        deletedEmployee.setStatus(EmployeeStatus.INACTIVE);
        deletedEmployee.setDeleted(true);
        employeeRepository.save(deletedEmployee);
        
        entityManager.flush();
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Employee> page = employeeRepository.findAllByDeletedFalse(pageable);

        // Assert
        assertEquals(1, page.getTotalElements());
        assertFalse(page.getContent().get(0).isDeleted());
    }

    @Test
    public void testFindAllByDeletedFalse_EmptyDatabase_ReturnsEmptyPage() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Employee> page = employeeRepository.findAllByDeletedFalse(pageable);

        // Assert
        assertEquals(0, page.getTotalElements());
        assertTrue(page.getContent().isEmpty());
    }

    @Test
    public void testFindAllByDeletedFalse_Pagination_ReturnsCorrectPage() {
        // Arrange
        for (int i = 1; i <= 25; i++) {
            Employee emp = new Employee();
            emp.setName("Employee " + i);
            emp.setBadgeId("EMP" + String.format("%03d", i));
            emp.setRole(Role.WORKER);
            emp.setDepartment("Warehouse");
            emp.setHireDate(LocalDate.now());
            emp.setStatus(EmployeeStatus.ACTIVE);
            emp.setDeleted(false);
            employeeRepository.save(emp);
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
        assertEquals(3, page1.getTotalPages());
    }

    // ========== FIND BY DEPARTMENT TESTS ==========

    @Test
    public void testFindByDepartmentAndDeletedFalse_ValidDepartment_ReturnsEmployees() {
        // Arrange
        employeeRepository.save(validEmployee);
        
        Employee employee2 = new Employee();
        employee2.setName("Jane Doe");
        employee2.setBadgeId("EMP002");
        employee2.setRole(Role.WORKER);
        employee2.setDepartment("Logistics");
        employee2.setHireDate(LocalDate.now());
        employee2.setStatus(EmployeeStatus.ACTIVE);
        employee2.setDeleted(false);
        employeeRepository.save(employee2);
        
        entityManager.flush();
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Employee> page = employeeRepository.findByDepartmentAndDeletedFalse("Warehouse", pageable);

        // Assert
        assertEquals(1, page.getTotalElements());
        assertEquals("Warehouse", page.getContent().get(0).getDepartment());
    }

    @Test
    public void testFindByDepartmentAndDeletedFalse_NonExistentDepartment_ReturnsEmpty() {
        // Arrange
        employeeRepository.save(validEmployee);
        entityManager.flush();
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Employee> page = employeeRepository.findByDepartmentAndDeletedFalse("NonExistent", pageable);

        // Assert
        assertEquals(0, page.getTotalElements());
    }

    // ========== UPDATE TESTS ==========

    @Test
    public void testUpdateEmployee_ValidUpdate_Success() {
        // Arrange
        Employee saved = employeeRepository.save(validEmployee);
        entityManager.flush();
        Long id = saved.getId();

        // Act
        saved.setName("Updated Name");
        saved.setDepartment("Logistics");
        employeeRepository.save(saved);
        entityManager.flush();
        entityManager.clear();

        // Assert
        Optional<Employee> updated = employeeRepository.findById(id);
        assertTrue(updated.isPresent());
        assertEquals("Updated Name", updated.get().getName());
        assertEquals("Logistics", updated.get().getDepartment());
    }

    @Test
    public void testSoftDelete_SetDeletedFlag_Success() {
        // Arrange
        Employee saved = employeeRepository.save(validEmployee);
        entityManager.flush();
        Long id = saved.getId();

        // Act
        saved.setDeleted(true);
        employeeRepository.save(saved);
        entityManager.flush();
        entityManager.clear();

        // Assert
        Optional<Employee> deleted = employeeRepository.findById(id);
        assertTrue(deleted.isPresent());
        assertTrue(deleted.get().isDeleted());
        
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> activePage = employeeRepository.findAllByDeletedFalse(pageable);
        assertEquals(0, activePage.getTotalElements());
    }

    // ========== DELETE TESTS ==========

    @Test
    public void testDeleteEmployee_HardDelete_Success() {
        // Arrange
        Employee saved = employeeRepository.save(validEmployee);
        entityManager.flush();
        Long id = saved.getId();

        // Act
        employeeRepository.deleteById(id);
        entityManager.flush();

        // Assert
        Optional<Employee> deleted = employeeRepository.findById(id);
        assertFalse(deleted.isPresent());
    }

    // ========== FIND BY ID TESTS ==========

    @Test
    public void testFindById_ExistingId_ReturnsEmployee() {
        // Arrange
        Employee saved = employeeRepository.save(validEmployee);
        entityManager.flush();
        Long id = saved.getId();

        // Act
        Optional<Employee> found = employeeRepository.findById(id);

        // Assert
        assertTrue(found.isPresent());
        assertEquals(id, found.get().getId());
    }

    @Test
    public void testFindById_NonExistentId_ReturnsEmpty() {
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

    // ========== COUNT TESTS ==========

    @Test
    public void testCount_MultipleEmployees_ReturnsCorrectCount() {
        // Arrange
        employeeRepository.save(validEmployee);
        
        Employee employee2 = new Employee();
        employee2.setName("Jane Doe");
        employee2.setBadgeId("EMP002");
        employee2.setRole(Role.SUPERVISOR);
        employee2.setDepartment("Warehouse");
        employee2.setHireDate(LocalDate.now());
        employee2.setStatus(EmployeeStatus.ACTIVE);
        employee2.setDeleted(false);
        employeeRepository.save(employee2);
        
        entityManager.flush();

        // Act
        long count = employeeRepository.count();

        // Assert
        assertEquals(2, count);
    }

    @Test
    public void testCount_EmptyDatabase_ReturnsZero() {
        // Act
        long count = employeeRepository.count();

        // Assert
        assertEquals(0, count);
    }

    // ========== EXISTS TESTS ==========

    @Test
    public void testExistsById_ExistingId_ReturnsTrue() {
        // Arrange
        Employee saved = employeeRepository.save(validEmployee);
        entityManager.flush();
        Long id = saved.getId();

        // Act
        boolean exists = employeeRepository.existsById(id);

        // Assert
        assertTrue(exists);
    }

    @Test
    public void testExistsById_NonExistentId_ReturnsFalse() {
        // Act
        boolean exists = employeeRepository.existsById(999L);

        // Assert
        assertFalse(exists);
    }
}