package com.warehouse.employee;

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
 * Tests cover all custom query methods with normal cases, boundary conditions, and edge cases
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
        // Arrange: Set up valid test data
        validEmployee = new Employee();
        validEmployee.setBadgeId("EMP001");
        validEmployee.setFirstName("John");
        validEmployee.setLastName("Doe");
        validEmployee.setEmail("john.doe@warehouse.com");
        validEmployee.setRole(EmployeeRole.WORKER);
        validEmployee.setDepartment("Warehouse");
        validEmployee.setShiftGroup("Morning");
        validEmployee.setHireDate(LocalDate.now());
        validEmployee.setStatus(EmployeeStatus.ACTIVE);
        validEmployee.setDeleted(false);
    }

    // ========== SAVE TESTS ==========

    @Test
    public void testSave_WithValidEmployee_ShouldPersistEmployee() {
        // Act
        Employee savedEmployee = employeeRepository.save(validEmployee);
        entityManager.flush();

        // Assert
        assertNotNull(savedEmployee.getId());
        assertEquals("EMP001", savedEmployee.getBadgeId());
        assertEquals("John", savedEmployee.getFirstName());
        assertEquals("Doe", savedEmployee.getLastName());
    }

    @Test
    public void testSave_WithDuplicateBadgeId_ShouldThrowException() {
        // Arrange
        employeeRepository.save(validEmployee);
        entityManager.flush();

        Employee duplicateEmployee = new Employee();
        duplicateEmployee.setBadgeId("EMP001"); // Same badge ID
        duplicateEmployee.setFirstName("Jane");
        duplicateEmployee.setLastName("Smith");
        duplicateEmployee.setEmail("jane.smith@warehouse.com");
        duplicateEmployee.setRole(EmployeeRole.WORKER);
        duplicateEmployee.setDepartment("Warehouse");
        duplicateEmployee.setShiftGroup("Morning");
        duplicateEmployee.setHireDate(LocalDate.now());
        duplicateEmployee.setStatus(EmployeeStatus.ACTIVE);
        duplicateEmployee.setDeleted(false);

        // Act & Assert
        assertThrows(Exception.class, () -> {
            employeeRepository.save(duplicateEmployee);
            entityManager.flush();
        });
    }

    @Test
    public void testSave_WithNullBadgeId_ShouldThrowException() {
        // Arrange
        validEmployee.setBadgeId(null);

        // Act & Assert
        assertThrows(Exception.class, () -> {
            employeeRepository.save(validEmployee);
            entityManager.flush();
        });
    }

    @Test
    public void testSave_WithNullFirstName_ShouldThrowException() {
        // Arrange
        validEmployee.setFirstName(null);

        // Act & Assert
        assertThrows(Exception.class, () -> {
            employeeRepository.save(validEmployee);
            entityManager.flush();
        });
    }

    // ========== FIND BY ID TESTS ==========

    @Test
    public void testFindById_WithValidId_ShouldReturnEmployee() {
        // Arrange
        Employee savedEmployee = employeeRepository.save(validEmployee);
        entityManager.flush();

        // Act
        Optional<Employee> foundEmployee = employeeRepository.findById(savedEmployee.getId());

        // Assert
        assertTrue(foundEmployee.isPresent());
        assertEquals("EMP001", foundEmployee.get().getBadgeId());
    }

    @Test
    public void testFindById_WithNonExistentId_ShouldReturnEmpty() {
        // Act
        Optional<Employee> foundEmployee = employeeRepository.findById(999L);

        // Assert
        assertFalse(foundEmployee.isPresent());
    }

    @Test
    public void testFindById_WithNullId_ShouldThrowException() {
        // Act & Assert
        assertThrows(Exception.class, () -> {
            employeeRepository.findById(null);
        });
    }

    // ========== FIND BY BADGE ID TESTS ==========

    @Test
    public void testFindByBadgeId_WithValidBadgeId_ShouldReturnEmployee() {
        // Arrange
        employeeRepository.save(validEmployee);
        entityManager.flush();

        // Act
        Optional<Employee> foundEmployee = employeeRepository.findByBadgeId("EMP001");

        // Assert
        assertTrue(foundEmployee.isPresent());
        assertEquals("John", foundEmployee.get().getFirstName());
    }

    @Test
    public void testFindByBadgeId_WithNonExistentBadgeId_ShouldReturnEmpty() {
        // Act
        Optional<Employee> foundEmployee = employeeRepository.findByBadgeId("NONEXISTENT");

        // Assert
        assertFalse(foundEmployee.isPresent());
    }

    @Test
    public void testFindByBadgeId_WithNullBadgeId_ShouldReturnEmpty() {
        // Act
        Optional<Employee> foundEmployee = employeeRepository.findByBadgeId(null);

        // Assert
        assertFalse(foundEmployee.isPresent());
    }

    @Test
    public void testFindByBadgeId_WithEmptyBadgeId_ShouldReturnEmpty() {
        // Act
        Optional<Employee> foundEmployee = employeeRepository.findByBadgeId("");

        // Assert
        assertFalse(foundEmployee.isPresent());
    }

    @Test
    public void testFindByBadgeId_CaseSensitive_ShouldNotMatch() {
        // Arrange
        employeeRepository.save(validEmployee);
        entityManager.flush();

        // Act
        Optional<Employee> foundEmployee = employeeRepository.findByBadgeId("emp001"); // lowercase

        // Assert
        assertFalse(foundEmployee.isPresent());
    }

    // ========== FIND BY EMAIL TESTS ==========

    @Test
    public void testFindByEmail_WithValidEmail_ShouldReturnEmployee() {
        // Arrange
        employeeRepository.save(validEmployee);
        entityManager.flush();

        // Act
        Optional<Employee> foundEmployee = employeeRepository.findByEmail("john.doe@warehouse.com");

        // Assert
        assertTrue(foundEmployee.isPresent());
        assertEquals("EMP001", foundEmployee.get().getBadgeId());
    }

    @Test
    public void testFindByEmail_WithNonExistentEmail_ShouldReturnEmpty() {
        // Act
        Optional<Employee> foundEmployee = employeeRepository.findByEmail("nonexistent@warehouse.com");

        // Assert
        assertFalse(foundEmployee.isPresent());
    }

    @Test
    public void testFindByEmail_WithNullEmail_ShouldReturnEmpty() {
        // Act
        Optional<Employee> foundEmployee = employeeRepository.findByEmail(null);

        // Assert
        assertFalse(foundEmployee.isPresent());
    }

    // ========== FIND BY DEPARTMENT TESTS ==========

    @Test
    public void testFindByDepartment_WithValidDepartment_ShouldReturnEmployees() {
        // Arrange
        employeeRepository.save(validEmployee);
        
        Employee employee2 = new Employee();
        employee2.setBadgeId("EMP002");
        employee2.setFirstName("Jane");
        employee2.setLastName("Smith");
        employee2.setEmail("jane.smith@warehouse.com");
        employee2.setRole(EmployeeRole.WORKER);
        employee2.setDepartment("Warehouse");
        employee2.setShiftGroup("Evening");
        employee2.setHireDate(LocalDate.now());
        employee2.setStatus(EmployeeStatus.ACTIVE);
        employee2.setDeleted(false);
        employeeRepository.save(employee2);
        entityManager.flush();

        // Act
        List<Employee> employees = employeeRepository.findByDepartment("Warehouse");

        // Assert
        assertEquals(2, employees.size());
        assertTrue(employees.stream().allMatch(e -> "Warehouse".equals(e.getDepartment())));
    }

    @Test
    public void testFindByDepartment_WithNonExistentDepartment_ShouldReturnEmptyList() {
        // Act
        List<Employee> employees = employeeRepository.findByDepartment("NonExistent");

        // Assert
        assertTrue(employees.isEmpty());
    }

    @Test
    public void testFindByDepartment_WithNullDepartment_ShouldReturnEmptyList() {
        // Act
        List<Employee> employees = employeeRepository.findByDepartment(null);

        // Assert
        assertTrue(employees.isEmpty());
    }

    // ========== FIND BY ROLE TESTS ==========

    @Test
    public void testFindByRole_WithValidRole_ShouldReturnEmployees() {
        // Arrange
        employeeRepository.save(validEmployee);
        
        Employee supervisor = new Employee();
        supervisor.setBadgeId("EMP002");
        supervisor.setFirstName("Jane");
        supervisor.setLastName("Smith");
        supervisor.setEmail("jane.smith@warehouse.com");
        supervisor.setRole(EmployeeRole.SUPERVISOR);
        supervisor.setDepartment("Warehouse");
        supervisor.setShiftGroup("Morning");
        supervisor.setHireDate(LocalDate.now());
        supervisor.setStatus(EmployeeStatus.ACTIVE);
        supervisor.setDeleted(false);
        employeeRepository.save(supervisor);
        entityManager.flush();

        // Act
        List<Employee> workers = employeeRepository.findByRole(EmployeeRole.WORKER);
        List<Employee> supervisors = employeeRepository.findByRole(EmployeeRole.SUPERVISOR);

        // Assert
        assertEquals(1, workers.size());
        assertEquals(1, supervisors.size());
        assertEquals(EmployeeRole.WORKER, workers.get(0).getRole());
        assertEquals(EmployeeRole.SUPERVISOR, supervisors.get(0).getRole());
    }

    @Test
    public void testFindByRole_WithNoMatchingRole_ShouldReturnEmptyList() {
        // Arrange
        employeeRepository.save(validEmployee);
        entityManager.flush();

        // Act
        List<Employee> admins = employeeRepository.findByRole(EmployeeRole.ADMIN);

        // Assert
        assertTrue(admins.isEmpty());
    }

    @Test
    public void testFindByRole_WithNullRole_ShouldReturnEmptyList() {
        // Act
        List<Employee> employees = employeeRepository.findByRole(null);

        // Assert
        assertTrue(employees.isEmpty());
    }

    // ========== FIND BY STATUS TESTS ==========

    @Test
    public void testFindByStatus_WithValidStatus_ShouldReturnEmployees() {
        // Arrange
        employeeRepository.save(validEmployee);
        
        Employee inactiveEmployee = new Employee();
        inactiveEmployee.setBadgeId("EMP002");
        inactiveEmployee.setFirstName("Jane");
        inactiveEmployee.setLastName("Smith");
        inactiveEmployee.setEmail("jane.smith@warehouse.com");
        inactiveEmployee.setRole(EmployeeRole.WORKER);
        inactiveEmployee.setDepartment("Warehouse");
        inactiveEmployee.setShiftGroup("Morning");
        inactiveEmployee.setHireDate(LocalDate.now());
        inactiveEmployee.setStatus(EmployeeStatus.INACTIVE);
        inactiveEmployee.setDeleted(false);
        employeeRepository.save(inactiveEmployee);
        entityManager.flush();

        // Act
        List<Employee> activeEmployees = employeeRepository.findByStatus(EmployeeStatus.ACTIVE);
        List<Employee> inactiveEmployees = employeeRepository.findByStatus(EmployeeStatus.INACTIVE);

        // Assert
        assertEquals(1, activeEmployees.size());
        assertEquals(1, inactiveEmployees.size());
        assertEquals(EmployeeStatus.ACTIVE, activeEmployees.get(0).getStatus());
        assertEquals(EmployeeStatus.INACTIVE, inactiveEmployees.get(0).getStatus());
    }

    @Test
    public void testFindByStatus_WithNoMatchingStatus_ShouldReturnEmptyList() {
        // Arrange
        employeeRepository.save(validEmployee);
        entityManager.flush();

        // Act
        List<Employee> terminatedEmployees = employeeRepository.findByStatus(EmployeeStatus.TERMINATED);

        // Assert
        assertTrue(terminatedEmployees.isEmpty());
    }

    // ========== FIND BY DELETED FALSE TESTS ==========

    @Test
    public void testFindByDeletedFalse_ShouldReturnOnlyActiveEmployees() {
        // Arrange
        employeeRepository.save(validEmployee);
        
        Employee deletedEmployee = new Employee();
        deletedEmployee.setBadgeId("EMP002");
        deletedEmployee.setFirstName("Jane");
        deletedEmployee.setLastName("Smith");
        deletedEmployee.setEmail("jane.smith@warehouse.com");
        deletedEmployee.setRole(EmployeeRole.WORKER);
        deletedEmployee.setDepartment("Warehouse");
        deletedEmployee.setShiftGroup("Morning");
        deletedEmployee.setHireDate(LocalDate.now());
        deletedEmployee.setStatus(EmployeeStatus.ACTIVE);
        deletedEmployee.setDeleted(true); // Soft deleted
        employeeRepository.save(deletedEmployee);
        entityManager.flush();

        // Act
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> activeEmployees = employeeRepository.findByDeletedFalse(pageable);

        // Assert
        assertEquals(1, activeEmployees.getTotalElements());
        assertFalse(activeEmployees.getContent().get(0).getDeleted());
    }

    @Test
    public void testFindByDeletedFalse_WithAllDeletedEmployees_ShouldReturnEmptyPage() {
        // Arrange
        validEmployee.setDeleted(true);
        employeeRepository.save(validEmployee);
        entityManager.flush();

        // Act
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> activeEmployees = employeeRepository.findByDeletedFalse(pageable);

        // Assert
        assertEquals(0, activeEmployees.getTotalElements());
        assertTrue(activeEmployees.getContent().isEmpty());
    }

    @Test
    public void testFindByDeletedFalse_WithPagination_ShouldReturnCorrectPage() {
        // Arrange
        for (int i = 1; i <= 25; i++) {
            Employee employee = new Employee();
            employee.setBadgeId("EMP" + String.format("%03d", i));
            employee.setFirstName("Employee" + i);
            employee.setLastName("Test");
            employee.setEmail("employee" + i + "@warehouse.com");
            employee.setRole(EmployeeRole.WORKER);
            employee.setDepartment("Warehouse");
            employee.setShiftGroup("Morning");
            employee.setHireDate(LocalDate.now());
            employee.setStatus(EmployeeStatus.ACTIVE);
            employee.setDeleted(false);
            employeeRepository.save(employee);
        }
        entityManager.flush();

        // Act
        Pageable firstPage = PageRequest.of(0, 10);
        Pageable secondPage = PageRequest.of(1, 10);
        Page<Employee> page1 = employeeRepository.findByDeletedFalse(firstPage);
        Page<Employee> page2 = employeeRepository.findByDeletedFalse(secondPage);

        // Assert
        assertEquals(25, page1.getTotalElements());
        assertEquals(10, page1.getContent().size());
        assertEquals(10, page2.getContent().size());
        assertEquals(3, page1.getTotalPages());
    }

    // ========== DELETE TESTS ==========

    @Test
    public void testDelete_WithValidEmployee_ShouldRemoveFromDatabase() {
        // Arrange
        Employee savedEmployee = employeeRepository.save(validEmployee);
        entityManager.flush();
        Long employeeId = savedEmployee.getId();

        // Act
        employeeRepository.delete(savedEmployee);
        entityManager.flush();

        // Assert
        Optional<Employee> deletedEmployee = employeeRepository.findById(employeeId);
        assertFalse(deletedEmployee.isPresent());
    }

    // ========== COUNT TESTS ==========

    @Test
    public void testCount_ShouldReturnCorrectCount() {
        // Arrange
        employeeRepository.save(validEmployee);
        
        Employee employee2 = new Employee();
        employee2.setBadgeId("EMP002");
        employee2.setFirstName("Jane");
        employee2.setLastName("Smith");
        employee2.setEmail("jane.smith@warehouse.com");
        employee2.setRole(EmployeeRole.WORKER);
        employee2.setDepartment("Warehouse");
        employee2.setShiftGroup("Morning");
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

    // ========== BOUNDARY CONDITION TESTS ==========

    @Test
    public void testSave_WithMaxLengthFields_ShouldSucceed() {
        // Arrange
        validEmployee.setFirstName("A".repeat(50));
        validEmployee.setLastName("B".repeat(50));

        // Act
        Employee savedEmployee = employeeRepository.save(validEmployee);
        entityManager.flush();

        // Assert
        assertNotNull(savedEmployee.getId());
        assertEquals(50, savedEmployee.getFirstName().length());
    }

    @Test
    public void testSave_WithMinLengthFields_ShouldSucceed() {
        // Arrange
        validEmployee.setFirstName("A");
        validEmployee.setLastName("B");

        // Act
        Employee savedEmployee = employeeRepository.save(validEmployee);
        entityManager.flush();

        // Assert
        assertNotNull(savedEmployee.getId());
        assertEquals(1, savedEmployee.getFirstName().length());
    }

    @Test
    public void testSave_WithSpecialCharactersInName_ShouldSucceed() {
        // Arrange
        validEmployee.setFirstName("O'Brien");
        validEmployee.setLastName("Smith-Jones");

        // Act
        Employee savedEmployee = employeeRepository.save(validEmployee);
        entityManager.flush();

        // Assert
        assertNotNull(savedEmployee.getId());
        assertEquals("O'Brien", savedEmployee.getFirstName());
        assertEquals("Smith-Jones", savedEmployee.getLastName());
    }

    @Test
    public void testSave_WithPastHireDate_ShouldSucceed() {
        // Arrange
        validEmployee.setHireDate(LocalDate.now().minusYears(5));

        // Act
        Employee savedEmployee = employeeRepository.save(validEmployee);
        entityManager.flush();

        // Assert
        assertNotNull(savedEmployee.getId());
        assertTrue(savedEmployee.getHireDate().isBefore(LocalDate.now()));
    }
}