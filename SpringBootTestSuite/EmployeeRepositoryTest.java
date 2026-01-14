package com.warehouse.ems.employee;

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
 * Tests cover JPA operations, custom queries, and database interactions
 */
@DataJpaTest
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
        // Setup first test employee
        testEmployee1 = new Employee();
        testEmployee1.setBadgeId("EMP001");
        testEmployee1.setFirstName("John");
        testEmployee1.setLastName("Doe");
        testEmployee1.setEmail("john.doe@warehouse.com");
        testEmployee1.setPhone("+1234567890");
        testEmployee1.setDepartment("Warehouse");
        testEmployee1.setRole("WORKER");
        testEmployee1.setHireDate(LocalDate.now());
        testEmployee1.setStatus("ACTIVE");
        testEmployee1.setTenantId("TENANT001");

        // Setup second test employee
        testEmployee2 = new Employee();
        testEmployee2.setBadgeId("EMP002");
        testEmployee2.setFirstName("Jane");
        testEmployee2.setLastName("Smith");
        testEmployee2.setEmail("jane.smith@warehouse.com");
        testEmployee2.setPhone("+1234567891");
        testEmployee2.setDepartment("Warehouse");
        testEmployee2.setRole("SUPERVISOR");
        testEmployee2.setHireDate(LocalDate.now());
        testEmployee2.setStatus("ACTIVE");
        testEmployee2.setTenantId("TENANT001");

        // Setup third test employee (different department)
        testEmployee3 = new Employee();
        testEmployee3.setBadgeId("EMP003");
        testEmployee3.setFirstName("Bob");
        testEmployee3.setLastName("Johnson");
        testEmployee3.setEmail("bob.johnson@warehouse.com");
        testEmployee3.setPhone("+1234567892");
        testEmployee3.setDepartment("Logistics");
        testEmployee3.setRole("WORKER");
        testEmployee3.setHireDate(LocalDate.now());
        testEmployee3.setStatus("ACTIVE");
        testEmployee3.setTenantId("TENANT002");
    }

    // ========== SAVE TESTS ==========

    @Test
    public void testSave_ValidEmployee_Success() {
        // Act
        Employee savedEmployee = employeeRepository.save(testEmployee1);

        // Assert
        assertNotNull(savedEmployee.getId());
        assertEquals("EMP001", savedEmployee.getBadgeId());
        assertEquals("John", savedEmployee.getFirstName());
        assertEquals("Doe", savedEmployee.getLastName());
    }

    @Test
    public void testSave_MultipleEmployees_Success() {
        // Act
        Employee saved1 = employeeRepository.save(testEmployee1);
        Employee saved2 = employeeRepository.save(testEmployee2);

        // Assert
        assertNotNull(saved1.getId());
        assertNotNull(saved2.getId());
        assertNotEquals(saved1.getId(), saved2.getId());
    }

    @Test
    public void testSave_UpdateExistingEmployee_Success() {
        // Arrange
        Employee savedEmployee = employeeRepository.save(testEmployee1);
        Long employeeId = savedEmployee.getId();

        // Act
        savedEmployee.setFirstName("John Updated");
        Employee updatedEmployee = employeeRepository.save(savedEmployee);

        // Assert
        assertEquals(employeeId, updatedEmployee.getId());
        assertEquals("John Updated", updatedEmployee.getFirstName());
    }

    // ========== FIND BY ID TESTS ==========

    @Test
    public void testFindById_ExistingEmployee_ReturnsEmployee() {
        // Arrange
        Employee savedEmployee = employeeRepository.save(testEmployee1);

        // Act
        Optional<Employee> foundEmployee = employeeRepository.findById(savedEmployee.getId());

        // Assert
        assertTrue(foundEmployee.isPresent());
        assertEquals("EMP001", foundEmployee.get().getBadgeId());
    }

    @Test
    public void testFindById_NonExistingEmployee_ReturnsEmpty() {
        // Act
        Optional<Employee> foundEmployee = employeeRepository.findById(999L);

        // Assert
        assertFalse(foundEmployee.isPresent());
    }

    @Test
    public void testFindById_NullId_ReturnsEmpty() {
        // Act
        Optional<Employee> foundEmployee = employeeRepository.findById(null);

        // Assert
        assertFalse(foundEmployee.isPresent());
    }

    // ========== FIND BY BADGE ID TESTS ==========

    @Test
    public void testFindByBadgeId_ExistingBadgeId_ReturnsEmployee() {
        // Arrange
        employeeRepository.save(testEmployee1);

        // Act
        Optional<Employee> foundEmployee = employeeRepository.findByBadgeId("EMP001");

        // Assert
        assertTrue(foundEmployee.isPresent());
        assertEquals("John", foundEmployee.get().getFirstName());
    }

    @Test
    public void testFindByBadgeId_NonExistingBadgeId_ReturnsEmpty() {
        // Act
        Optional<Employee> foundEmployee = employeeRepository.findByBadgeId("INVALID");

        // Assert
        assertFalse(foundEmployee.isPresent());
    }

    @Test
    public void testFindByBadgeId_NullBadgeId_ReturnsEmpty() {
        // Act
        Optional<Employee> foundEmployee = employeeRepository.findByBadgeId(null);

        // Assert
        assertFalse(foundEmployee.isPresent());
    }

    @Test
    public void testFindByBadgeId_EmptyBadgeId_ReturnsEmpty() {
        // Act
        Optional<Employee> foundEmployee = employeeRepository.findByBadgeId("");

        // Assert
        assertFalse(foundEmployee.isPresent());
    }

    // ========== EXISTS BY BADGE ID TESTS ==========

    @Test
    public void testExistsByBadgeId_ExistingBadgeId_ReturnsTrue() {
        // Arrange
        employeeRepository.save(testEmployee1);

        // Act
        boolean exists = employeeRepository.existsByBadgeId("EMP001");

        // Assert
        assertTrue(exists);
    }

    @Test
    public void testExistsByBadgeId_NonExistingBadgeId_ReturnsFalse() {
        // Act
        boolean exists = employeeRepository.existsByBadgeId("INVALID");

        // Assert
        assertFalse(exists);
    }

    @Test
    public void testExistsByBadgeId_NullBadgeId_ReturnsFalse() {
        // Act
        boolean exists = employeeRepository.existsByBadgeId(null);

        // Assert
        assertFalse(exists);
    }

    @Test
    public void testExistsByBadgeId_CaseSensitive_ReturnsCorrectResult() {
        // Arrange
        employeeRepository.save(testEmployee1);

        // Act
        boolean existsUpperCase = employeeRepository.existsByBadgeId("EMP001");
        boolean existsLowerCase = employeeRepository.existsByBadgeId("emp001");

        // Assert
        assertTrue(existsUpperCase);
        assertFalse(existsLowerCase); // Assuming case-sensitive
    }

    // ========== FIND BY DEPARTMENT TESTS ==========

    @Test
    public void testFindByDepartment_ExistingDepartment_ReturnsEmployees() {
        // Arrange
        employeeRepository.save(testEmployee1);
        employeeRepository.save(testEmployee2);
        employeeRepository.save(testEmployee3);

        // Act
        List<Employee> warehouseEmployees = employeeRepository.findByDepartment("Warehouse");

        // Assert
        assertEquals(2, warehouseEmployees.size());
        assertTrue(warehouseEmployees.stream().allMatch(e -> "Warehouse".equals(e.getDepartment())));
    }

    @Test
    public void testFindByDepartment_NonExistingDepartment_ReturnsEmpty() {
        // Arrange
        employeeRepository.save(testEmployee1);

        // Act
        List<Employee> employees = employeeRepository.findByDepartment("NonExistent");

        // Assert
        assertTrue(employees.isEmpty());
    }

    @Test
    public void testFindByDepartment_NullDepartment_ReturnsEmpty() {
        // Act
        List<Employee> employees = employeeRepository.findByDepartment(null);

        // Assert
        assertTrue(employees.isEmpty());
    }

    // ========== FIND BY TENANT ID TESTS ==========

    @Test
    public void testFindByTenantId_ExistingTenant_ReturnsEmployees() {
        // Arrange
        employeeRepository.save(testEmployee1);
        employeeRepository.save(testEmployee2);
        employeeRepository.save(testEmployee3);

        // Act
        List<Employee> tenant1Employees = employeeRepository.findByTenantId("TENANT001");

        // Assert
        assertEquals(2, tenant1Employees.size());
        assertTrue(tenant1Employees.stream().allMatch(e -> "TENANT001".equals(e.getTenantId())));
    }

    @Test
    public void testFindByTenantId_NonExistingTenant_ReturnsEmpty() {
        // Arrange
        employeeRepository.save(testEmployee1);

        // Act
        List<Employee> employees = employeeRepository.findByTenantId("INVALID_TENANT");

        // Assert
        assertTrue(employees.isEmpty());
    }

    @Test
    public void testFindByTenantId_TenantIsolation_Success() {
        // Arrange
        employeeRepository.save(testEmployee1); // TENANT001
        employeeRepository.save(testEmployee3); // TENANT002

        // Act
        List<Employee> tenant1Employees = employeeRepository.findByTenantId("TENANT001");
        List<Employee> tenant2Employees = employeeRepository.findByTenantId("TENANT002");

        // Assert
        assertEquals(1, tenant1Employees.size());
        assertEquals(1, tenant2Employees.size());
        assertEquals("EMP001", tenant1Employees.get(0).getBadgeId());
        assertEquals("EMP003", tenant2Employees.get(0).getBadgeId());
    }

    // ========== FIND ALL WITH PAGINATION TESTS ==========

    @Test
    public void testFindAll_WithPagination_ReturnsPage() {
        // Arrange
        employeeRepository.save(testEmployee1);
        employeeRepository.save(testEmployee2);
        employeeRepository.save(testEmployee3);
        Pageable pageable = PageRequest.of(0, 2);

        // Act
        Page<Employee> employeePage = employeeRepository.findAll(pageable);

        // Assert
        assertEquals(2, employeePage.getContent().size());
        assertEquals(3, employeePage.getTotalElements());
        assertEquals(2, employeePage.getTotalPages());
    }

    @Test
    public void testFindAll_SecondPage_ReturnsCorrectPage() {
        // Arrange
        employeeRepository.save(testEmployee1);
        employeeRepository.save(testEmployee2);
        employeeRepository.save(testEmployee3);
        Pageable pageable = PageRequest.of(1, 2);

        // Act
        Page<Employee> employeePage = employeeRepository.findAll(pageable);

        // Assert
        assertEquals(1, employeePage.getContent().size());
        assertEquals(3, employeePage.getTotalElements());
    }

    @Test
    public void testFindAll_EmptyDatabase_ReturnsEmptyPage() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Employee> employeePage = employeeRepository.findAll(pageable);

        // Assert
        assertTrue(employeePage.getContent().isEmpty());
        assertEquals(0, employeePage.getTotalElements());
    }

    // ========== DELETE TESTS ==========

    @Test
    public void testDelete_ExistingEmployee_Success() {
        // Arrange
        Employee savedEmployee = employeeRepository.save(testEmployee1);
        Long employeeId = savedEmployee.getId();

        // Act
        employeeRepository.delete(savedEmployee);

        // Assert
        Optional<Employee> deletedEmployee = employeeRepository.findById(employeeId);
        assertFalse(deletedEmployee.isPresent());
    }

    @Test
    public void testDeleteById_ExistingEmployee_Success() {
        // Arrange
        Employee savedEmployee = employeeRepository.save(testEmployee1);
        Long employeeId = savedEmployee.getId();

        // Act
        employeeRepository.deleteById(employeeId);

        // Assert
        Optional<Employee> deletedEmployee = employeeRepository.findById(employeeId);
        assertFalse(deletedEmployee.isPresent());
    }

    @Test
    public void testDeleteAll_MultipleEmployees_Success() {
        // Arrange
        employeeRepository.save(testEmployee1);
        employeeRepository.save(testEmployee2);

        // Act
        employeeRepository.deleteAll();

        // Assert
        List<Employee> allEmployees = employeeRepository.findAll();
        assertTrue(allEmployees.isEmpty());
    }

    // ========== COUNT TESTS ==========

    @Test
    public void testCount_MultipleEmployees_ReturnsCorrectCount() {
        // Arrange
        employeeRepository.save(testEmployee1);
        employeeRepository.save(testEmployee2);
        employeeRepository.save(testEmployee3);

        // Act
        long count = employeeRepository.count();

        // Assert
        assertEquals(3, count);
    }

    @Test
    public void testCount_EmptyDatabase_ReturnsZero() {
        // Act
        long count = employeeRepository.count();

        // Assert
        assertEquals(0, count);
    }

    // ========== FIND BY STATUS TESTS ==========

    @Test
    public void testFindByStatus_ActiveEmployees_ReturnsActive() {
        // Arrange
        employeeRepository.save(testEmployee1);
        testEmployee2.setStatus("INACTIVE");
        employeeRepository.save(testEmployee2);

        // Act
        List<Employee> activeEmployees = employeeRepository.findByStatus("ACTIVE");

        // Assert
        assertEquals(1, activeEmployees.size());
        assertEquals("ACTIVE", activeEmployees.get(0).getStatus());
    }

    @Test
    public void testFindByStatus_InactiveEmployees_ReturnsInactive() {
        // Arrange
        testEmployee1.setStatus("INACTIVE");
        employeeRepository.save(testEmployee1);
        employeeRepository.save(testEmployee2);

        // Act
        List<Employee> inactiveEmployees = employeeRepository.findByStatus("INACTIVE");

        // Assert
        assertEquals(1, inactiveEmployees.size());
        assertEquals("INACTIVE", inactiveEmployees.get(0).getStatus());
    }

    // ========== FIND BY ROLE TESTS ==========

    @Test
    public void testFindByRole_Workers_ReturnsWorkers() {
        // Arrange
        employeeRepository.save(testEmployee1); // WORKER
        employeeRepository.save(testEmployee2); // SUPERVISOR
        employeeRepository.save(testEmployee3); // WORKER

        // Act
        List<Employee> workers = employeeRepository.findByRole("WORKER");

        // Assert
        assertEquals(2, workers.size());
        assertTrue(workers.stream().allMatch(e -> "WORKER".equals(e.getRole())));
    }

    @Test
    public void testFindByRole_Supervisors_ReturnsSupervisors() {
        // Arrange
        employeeRepository.save(testEmployee1);
        employeeRepository.save(testEmployee2);

        // Act
        List<Employee> supervisors = employeeRepository.findByRole("SUPERVISOR");

        // Assert
        assertEquals(1, supervisors.size());
        assertEquals("SUPERVISOR", supervisors.get(0).getRole());
    }

    // ========== BOUNDARY CONDITION TESTS ==========

    @Test
    public void testSave_MaxLengthFields_Success() {
        // Arrange
        String maxLengthString = "A".repeat(255);
        testEmployee1.setFirstName(maxLengthString);
        testEmployee1.setLastName(maxLengthString);

        // Act
        Employee savedEmployee = employeeRepository.save(testEmployee1);

        // Assert
        assertNotNull(savedEmployee.getId());
        assertEquals(maxLengthString, savedEmployee.getFirstName());
    }

    @Test
    public void testSave_MinimumValidData_Success() {
        // Arrange
        Employee minEmployee = new Employee();
        minEmployee.setBadgeId("MIN");
        minEmployee.setFirstName("A");
        minEmployee.setLastName("B");
        minEmployee.setEmail("a@b.c");

        // Act
        Employee savedEmployee = employeeRepository.save(minEmployee);

        // Assert
        assertNotNull(savedEmployee.getId());
        assertEquals("MIN", savedEmployee.getBadgeId());
    }

    // ========== SPECIAL CHARACTER TESTS ==========

    @Test
    public void testSave_SpecialCharactersInName_Success() {
        // Arrange
        testEmployee1.setFirstName("Jean-Pierre");
        testEmployee1.setLastName("O'Connor");

        // Act
        Employee savedEmployee = employeeRepository.save(testEmployee1);

        // Assert
        assertNotNull(savedEmployee.getId());
        assertEquals("Jean-Pierre", savedEmployee.getFirstName());
        assertEquals("O'Connor", savedEmployee.getLastName());
    }

    @Test
    public void testSave_InternationalCharacters_Success() {
        // Arrange
        testEmployee1.setFirstName("JosÃ©");
        testEmployee1.setLastName("MÃ¼ller");

        // Act
        Employee savedEmployee = employeeRepository.save(testEmployee1);

        // Assert
        assertNotNull(savedEmployee.getId());
        assertEquals("JosÃ©", savedEmployee.getFirstName());
        assertEquals("MÃ¼ller", savedEmployee.getLastName());
    }
}