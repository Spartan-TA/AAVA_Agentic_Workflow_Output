package com.warehouse.employee.repository;

import com.warehouse.employee.entity.Employee;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDate;
import java.util.Optional;
import java.util.List;

/**
 * Comprehensive JUnit test class for EmployeeRepository
 * Tests all repository operations including CRUD, custom queries, and edge cases
 */
@DataJpaTest
class EmployeeRepositoryTest {

    @Autowired
    private EmployeeRepository employeeRepository;

    private Employee testEmployee;

    @BeforeEach
    void setUp() {
        employeeRepository.deleteAll();
        
        testEmployee = new Employee();
        testEmployee.setName("John Doe");
        testEmployee.setBadgeId("EMP001");
        testEmployee.setRole("WORKER");
        testEmployee.setDepartment("Warehouse");
        testEmployee.setShiftGroup("Morning");
        testEmployee.setHireDate(LocalDate.of(2024, 1, 15));
        testEmployee.setStatus("ACTIVE");
        testEmployee.setDeleted(false);
    }

    @Test
    void testSaveEmployee_ValidData_Success() {
        // Act
        Employee saved = employeeRepository.save(testEmployee);
        
        // Assert
        assertNotNull(saved.getId());
        assertEquals("John Doe", saved.getName());
        assertEquals("EMP001", saved.getBadgeId());
    }

    @Test
    void testFindById_ExistingEmployee_ReturnsEmployee() {
        // Arrange
        Employee saved = employeeRepository.save(testEmployee);
        
        // Act
        Optional<Employee> found = employeeRepository.findById(saved.getId());
        
        // Assert
        assertTrue(found.isPresent());
        assertEquals("John Doe", found.get().getName());
        assertEquals("EMP001", found.get().getBadgeId());
    }

    @Test
    void testFindById_NonExistingEmployee_ReturnsEmpty() {
        // Act
        Optional<Employee> found = employeeRepository.findById(999L);
        
        // Assert
        assertFalse(found.isPresent());
    }

    @Test
    void testFindByBadgeId_ExistingBadge_ReturnsEmployee() {
        // Arrange
        employeeRepository.save(testEmployee);
        
        // Act
        Optional<Employee> found = employeeRepository.findByBadgeId("EMP001");
        
        // Assert
        assertTrue(found.isPresent());
        assertEquals("John Doe", found.get().getName());
    }

    @Test
    void testFindByBadgeId_NonExistingBadge_ReturnsEmpty() {
        // Act
        Optional<Employee> found = employeeRepository.findByBadgeId("NONEXISTENT");
        
        // Assert
        assertFalse(found.isPresent());
    }

    @Test
    void testFindAllByDeletedFalse_ReturnsOnlyActiveEmployees() {
        // Arrange
        Employee activeEmployee = employeeRepository.save(testEmployee);
        
        Employee deletedEmployee = new Employee();
        deletedEmployee.setName("Jane Smith");
        deletedEmployee.setBadgeId("EMP002");
        deletedEmployee.setRole("WORKER");
        deletedEmployee.setDeleted(true);
        employeeRepository.save(deletedEmployee);
        
        Pageable pageable = PageRequest.of(0, 10);
        
        // Act
        Page<Employee> result = employeeRepository.findAllByDeletedFalse(pageable);
        
        // Assert
        assertEquals(1, result.getTotalElements());
        assertEquals("John Doe", result.getContent().get(0).getName());
    }

    @Test
    void testFindAll_WithPagination_ReturnsPagedResults() {
        // Arrange
        for (int i = 1; i <= 15; i++) {
            Employee emp = new Employee();
            emp.setName("Employee " + i);
            emp.setBadgeId("EMP" + String.format("%03d", i));
            emp.setRole("WORKER");
            emp.setDeleted(false);
            employeeRepository.save(emp);
        }
        
        Pageable pageable = PageRequest.of(0, 10);
        
        // Act
        Page<Employee> result = employeeRepository.findAll(pageable);
        
        // Assert
        assertEquals(15, result.getTotalElements());
        assertEquals(10, result.getContent().size());
        assertEquals(2, result.getTotalPages());
    }

    @Test
    void testDeleteEmployee_SoftDelete_MarksAsDeleted() {
        // Arrange
        Employee saved = employeeRepository.save(testEmployee);
        
        // Act
        saved.setDeleted(true);
        employeeRepository.save(saved);
        
        // Assert
        Optional<Employee> found = employeeRepository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertTrue(found.get().isDeleted());
    }

    @Test
    void testSaveEmployee_DuplicateBadgeId_ThrowsException() {
        // Arrange
        employeeRepository.save(testEmployee);
        
        Employee duplicate = new Employee();
        duplicate.setName("Jane Smith");
        duplicate.setBadgeId("EMP001"); // Same badge ID
        duplicate.setRole("WORKER");
        
        // Act & Assert
        assertThrows(Exception.class, () -> {
            employeeRepository.save(duplicate);
            employeeRepository.flush();
        });
    }

    @Test
    void testUpdateEmployee_ModifyFields_Success() {
        // Arrange
        Employee saved = employeeRepository.save(testEmployee);
        
        // Act
        saved.setName("John Updated");
        saved.setDepartment("Logistics");
        Employee updated = employeeRepository.save(saved);
        
        // Assert
        assertEquals("John Updated", updated.getName());
        assertEquals("Logistics", updated.getDepartment());
    }

    @Test
    void testFindByDepartment_ReturnsMatchingEmployees() {
        // Arrange
        employeeRepository.save(testEmployee);
        
        Employee emp2 = new Employee();
        emp2.setName("Jane Smith");
        emp2.setBadgeId("EMP002");
        emp2.setRole("WORKER");
        emp2.setDepartment("Warehouse");
        employeeRepository.save(emp2);
        
        Employee emp3 = new Employee();
        emp3.setName("Bob Johnson");
        emp3.setBadgeId("EMP003");
        emp3.setRole("WORKER");
        emp3.setDepartment("Logistics");
        employeeRepository.save(emp3);
        
        // Act
        List<Employee> warehouseEmployees = employeeRepository.findByDepartment("Warehouse");
        
        // Assert
        assertEquals(2, warehouseEmployees.size());
    }

    @Test
    void testFindByRole_ReturnsMatchingEmployees() {
        // Arrange
        employeeRepository.save(testEmployee);
        
        Employee supervisor = new Employee();
        supervisor.setName("Jane Supervisor");
        supervisor.setBadgeId("EMP002");
        supervisor.setRole("SUPERVISOR");
        employeeRepository.save(supervisor);
        
        // Act
        List<Employee> workers = employeeRepository.findByRole("WORKER");
        List<Employee> supervisors = employeeRepository.findByRole("SUPERVISOR");
        
        // Assert
        assertEquals(1, workers.size());
        assertEquals(1, supervisors.size());
    }

    @Test
    void testCount_ReturnsCorrectCount() {
        // Arrange
        employeeRepository.save(testEmployee);
        
        Employee emp2 = new Employee();
        emp2.setName("Jane Smith");
        emp2.setBadgeId("EMP002");
        emp2.setRole("WORKER");
        employeeRepository.save(emp2);
        
        // Act
        long count = employeeRepository.count();
        
        // Assert
        assertEquals(2, count);
    }

    @Test
    void testExistsById_ExistingEmployee_ReturnsTrue() {
        // Arrange
        Employee saved = employeeRepository.save(testEmployee);
        
        // Act
        boolean exists = employeeRepository.existsById(saved.getId());
        
        // Assert
        assertTrue(exists);
    }

    @Test
    void testExistsById_NonExistingEmployee_ReturnsFalse() {
        // Act
        boolean exists = employeeRepository.existsById(999L);
        
        // Assert
        assertFalse(exists);
    }
}