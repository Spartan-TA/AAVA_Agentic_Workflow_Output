package com.warehouse.employee.management.repository;

import com.warehouse.employee.management.domain.Employee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive JUnit test suite for EmployeeRepository
 * Tests all custom query methods and soft-delete behavior
 * 
 * @author Automation Test Engineer
 * @version 1.0
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("EmployeeRepository Test Suite")
public class EmployeeRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private EmployeeRepository employeeRepository;

    private Employee testEmployee1;
    private Employee testEmployee2;
    private Employee deletedEmployee;

    @BeforeEach
    void setUp() {
        // Clear any existing data
        employeeRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();

        // Initialize test employee 1
        testEmployee1 = new Employee();
        testEmployee1.setBadgeId("EMP001");
        testEmployee1.setFirstName("John");
        testEmployee1.setLastName("Doe");
        testEmployee1.setEmail("john.doe@warehouse.com");
        testEmployee1.setPhoneNumber("+1234567890");
        testEmployee1.setRole("WORKER");
        testEmployee1.setDepartment("Shipping");
        testEmployee1.setShiftGroup("Morning");
        testEmployee1.setHireDate(LocalDate.of(2023, 1, 15));
        testEmployee1.setStatus("ACTIVE");
        testEmployee1.setDeleted(false);
        testEmployee1.setCreatedAt(LocalDateTime.now());
        testEmployee1.setUpdatedAt(LocalDateTime.now());

        // Initialize test employee 2
        testEmployee2 = new Employee();
        testEmployee2.setBadgeId("EMP002");
        testEmployee2.setFirstName("Jane");
        testEmployee2.setLastName("Smith");
        testEmployee2.setEmail("jane.smith@warehouse.com");
        testEmployee2.setPhoneNumber("+9876543210");
        testEmployee2.setRole("SUPERVISOR");
        testEmployee2.setDepartment("Receiving");
        testEmployee2.setShiftGroup("Evening");
        testEmployee2.setHireDate(LocalDate.of(2022, 6, 1));
        testEmployee2.setStatus("ACTIVE");
        testEmployee2.setDeleted(false);
        testEmployee2.setCreatedAt(LocalDateTime.now());
        testEmployee2.setUpdatedAt(LocalDateTime.now());

        // Initialize deleted employee
        deletedEmployee = new Employee();
        deletedEmployee.setBadgeId("EMP003");
        deletedEmployee.setFirstName("Bob");
        deletedEmployee.setLastName("Johnson");
        deletedEmployee.setEmail("bob.johnson@warehouse.com");
        deletedEmployee.setPhoneNumber("+5555555555");
        deletedEmployee.setRole("WORKER");
        deletedEmployee.setDepartment("Shipping");
        deletedEmployee.setShiftGroup("Night");
        deletedEmployee.setHireDate(LocalDate.of(2021, 3, 10));
        deletedEmployee.setStatus("INACTIVE");
        deletedEmployee.setDeleted(true);
        deletedEmployee.setCreatedAt(LocalDateTime.now());
        deletedEmployee.setUpdatedAt(LocalDateTime.now());
    }

    // ==================== SAVE AND FIND BY ID TESTS ====================

    @Test
    @DisplayName("Test save - Valid Employee - Persists Successfully")
    void testSave_ValidEmployee_PersistsSuccessfully() {
        // Act
        Employee savedEmployee = employeeRepository.save(testEmployee1);
        entityManager.flush();

        // Assert
        assertNotNull(savedEmployee.getId());
        assertEquals("EMP001", savedEmployee.getBadgeId());
        assertEquals("John", savedEmployee.getFirstName());
        assertEquals("Doe", savedEmployee.getLastName());
        assertFalse(savedEmployee.getDeleted());
    }

    @Test
    @DisplayName("Test findById - Existing Employee - Returns Employee")
    void testFindById_ExistingEmployee_ReturnsEmployee() {
        // Arrange
        Employee savedEmployee = entityManager.persistAndFlush(testEmployee1);

        // Act
        Optional<Employee> found = employeeRepository.findById(savedEmployee.getId());

        // Assert
        assertTrue(found.isPresent());
        assertEquals("EMP001", found.get().getBadgeId());
    }

    @Test
    @DisplayName("Test findById - Non-Existing ID - Returns Empty")
    void testFindById_NonExistingId_ReturnsEmpty() {
        // Act
        Optional<Employee> found = employeeRepository.findById(999L);

        // Assert
        assertFalse(found.isPresent());
    }

    // ==================== FIND BY DELETED FALSE TESTS ====================

    @Test
    @DisplayName("Test findByDeletedFalse - Active Employees Only - Returns Active List")
    void testFindByDeletedFalse_ActiveEmployeesOnly_ReturnsActiveList() {
        // Arrange
        entityManager.persist(testEmployee1);
        entityManager.persist(testEmployee2);
        entityManager.persist(deletedEmployee);
        entityManager.flush();

        // Act
        List<Employee> activeEmployees = employeeRepository.findByDeletedFalse();

        // Assert
        assertNotNull(activeEmployees);
        assertEquals(2, activeEmployees.size());
        assertTrue(activeEmployees.stream().noneMatch(Employee::getDeleted));
        assertTrue(activeEmployees.stream().anyMatch(e -> e.getBadgeId().equals("EMP001")));
        assertTrue(activeEmployees.stream().anyMatch(e -> e.getBadgeId().equals("EMP002")));
        assertFalse(activeEmployees.stream().anyMatch(e -> e.getBadgeId().equals("EMP003")));
    }

    @Test
    @DisplayName("Test findByDeletedFalse - No Active Employees - Returns Empty List")
    void testFindByDeletedFalse_NoActiveEmployees_ReturnsEmptyList() {
        // Arrange
        entityManager.persist(deletedEmployee);
        entityManager.flush();

        // Act
        List<Employee> activeEmployees = employeeRepository.findByDeletedFalse();

        // Assert
        assertNotNull(activeEmployees);
        assertTrue(activeEmployees.isEmpty());
    }

    @Test
    @DisplayName("Test findByDeletedFalse - Empty Database - Returns Empty List")
    void testFindByDeletedFalse_EmptyDatabase_ReturnsEmptyList() {
        // Act
        List<Employee> activeEmployees = employeeRepository.findByDeletedFalse();

        // Assert
        assertNotNull(activeEmployees);
        assertTrue(activeEmployees.isEmpty());
    }

    // ==================== FIND BY ID AND DELETED FALSE TESTS ====================

    @Test
    @DisplayName("Test findByIdAndDeletedFalse - Active Employee - Returns Employee")
    void testFindByIdAndDeletedFalse_ActiveEmployee_ReturnsEmployee() {
        // Arrange
        Employee savedEmployee = entityManager.persistAndFlush(testEmployee1);

        // Act
        Optional<Employee> found = employeeRepository.findByIdAndDeletedFalse(savedEmployee.getId());

        // Assert
        assertTrue(found.isPresent());
        assertEquals("EMP001", found.get().getBadgeId());
        assertFalse(found.get().getDeleted());
    }

    @Test
    @DisplayName("Test findByIdAndDeletedFalse - Deleted Employee - Returns Empty")
    void testFindByIdAndDeletedFalse_DeletedEmployee_ReturnsEmpty() {
        // Arrange
        Employee savedEmployee = entityManager.persistAndFlush(deletedEmployee);

        // Act
        Optional<Employee> found = employeeRepository.findByIdAndDeletedFalse(savedEmployee.getId());

        // Assert
        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("Test findByIdAndDeletedFalse - Non-Existing ID - Returns Empty")
    void testFindByIdAndDeletedFalse_NonExistingId_ReturnsEmpty() {
        // Act
        Optional<Employee> found = employeeRepository.findByIdAndDeletedFalse(999L);

        // Assert
        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("Test findByIdAndDeletedFalse - Null ID - Returns Empty")
    void testFindByIdAndDeletedFalse_NullId_ReturnsEmpty() {
        // Act
        Optional<Employee> found = employeeRepository.findByIdAndDeletedFalse(null);

        // Assert
        assertFalse(found.isPresent());
    }

    // ==================== FIND BY BADGE ID AND DELETED FALSE TESTS ====================

    @Test
    @DisplayName("Test findByBadgeIdAndDeletedFalse - Existing BadgeId - Returns Employee")
    void testFindByBadgeIdAndDeletedFalse_ExistingBadgeId_ReturnsEmployee() {
        // Arrange
        entityManager.persistAndFlush(testEmployee1);

        // Act
        Optional<Employee> found = employeeRepository.findByBadgeIdAndDeletedFalse("EMP001");

        // Assert
        assertTrue(found.isPresent());
        assertEquals("John", found.get().getFirstName());
        assertEquals("Doe", found.get().getLastName());
    }

    @Test
    @DisplayName("Test findByBadgeIdAndDeletedFalse - Deleted Employee BadgeId - Returns Empty")
    void testFindByBadgeIdAndDeletedFalse_DeletedEmployeeBadgeId_ReturnsEmpty() {
        // Arrange
        entityManager.persistAndFlush(deletedEmployee);

        // Act
        Optional<Employee> found = employeeRepository.findByBadgeIdAndDeletedFalse("EMP003");

        // Assert
        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("Test findByBadgeIdAndDeletedFalse - Non-Existing BadgeId - Returns Empty")
    void testFindByBadgeIdAndDeletedFalse_NonExistingBadgeId_ReturnsEmpty() {
        // Act
        Optional<Employee> found = employeeRepository.findByBadgeIdAndDeletedFalse("NONEXISTENT");

        // Assert
        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("Test findByBadgeIdAndDeletedFalse - Null BadgeId - Returns Empty")
    void testFindByBadgeIdAndDeletedFalse_NullBadgeId_ReturnsEmpty() {
        // Act
        Optional<Employee> found = employeeRepository.findByBadgeIdAndDeletedFalse(null);

        // Assert
        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("Test findByBadgeIdAndDeletedFalse - Empty BadgeId - Returns Empty")
    void testFindByBadgeIdAndDeletedFalse_EmptyBadgeId_ReturnsEmpty() {
        // Act
        Optional<Employee> found = employeeRepository.findByBadgeIdAndDeletedFalse("");

        // Assert
        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("Test findByBadgeIdAndDeletedFalse - Case Sensitive - Returns Empty")
    void testFindByBadgeIdAndDeletedFalse_CaseSensitive_ReturnsEmpty() {
        // Arrange
        entityManager.persistAndFlush(testEmployee1);

        // Act
        Optional<Employee> found = employeeRepository.findByBadgeIdAndDeletedFalse("emp001");

        // Assert
        assertFalse(found.isPresent());
    }

    // ==================== FIND BY DEPARTMENT AND DELETED FALSE TESTS ====================

    @Test
    @DisplayName("Test findByDepartmentAndDeletedFalse - Existing Department - Returns Employee List")
    void testFindByDepartmentAndDeletedFalse_ExistingDepartment_ReturnsEmployeeList() {
        // Arrange
        entityManager.persist(testEmployee1);
        entityManager.persist(deletedEmployee);
        entityManager.flush();

        // Act
        List<Employee> shippingEmployees = employeeRepository.findByDepartmentAndDeletedFalse("Shipping");

        // Assert
        assertNotNull(shippingEmployees);
        assertEquals(1, shippingEmployees.size());
        assertEquals("EMP001", shippingEmployees.get(0).getBadgeId());
        assertFalse(shippingEmployees.get(0).getDeleted());
    }

    @Test
    @DisplayName("Test findByDepartmentAndDeletedFalse - Multiple Employees Same Department - Returns All")
    void testFindByDepartmentAndDeletedFalse_MultipleEmployeesSameDepartment_ReturnsAll() {
        // Arrange
        Employee employee3 = new Employee();
        employee3.setBadgeId("EMP004");
        employee3.setFirstName("Alice");
        employee3.setLastName("Williams");
        employee3.setEmail("alice.williams@warehouse.com");
        employee3.setDepartment("Shipping");
        employee3.setDeleted(false);

        entityManager.persist(testEmployee1);
        entityManager.persist(employee3);
        entityManager.flush();

        // Act
        List<Employee> shippingEmployees = employeeRepository.findByDepartmentAndDeletedFalse("Shipping");

        // Assert
        assertNotNull(shippingEmployees);
        assertEquals(2, shippingEmployees.size());
        assertTrue(shippingEmployees.stream().allMatch(e -> e.getDepartment().equals("Shipping")));
        assertTrue(shippingEmployees.stream().noneMatch(Employee::getDeleted));
    }

    @Test
    @DisplayName("Test findByDepartmentAndDeletedFalse - Non-Existing Department - Returns Empty List")
    void testFindByDepartmentAndDeletedFalse_NonExistingDepartment_ReturnsEmptyList() {
        // Arrange
        entityManager.persistAndFlush(testEmployee1);

        // Act
        List<Employee> employees = employeeRepository.findByDepartmentAndDeletedFalse("NonExistent");

        // Assert
        assertNotNull(employees);
        assertTrue(employees.isEmpty());
    }

    @Test
    @DisplayName("Test findByDepartmentAndDeletedFalse - Null Department - Returns Empty List")
    void testFindByDepartmentAndDeletedFalse_NullDepartment_ReturnsEmptyList() {
        // Act
        List<Employee> employees = employeeRepository.findByDepartmentAndDeletedFalse(null);

        // Assert
        assertNotNull(employees);
        assertTrue(employees.isEmpty());
    }

    @Test
    @DisplayName("Test findByDepartmentAndDeletedFalse - Only Deleted Employees in Department - Returns Empty")
    void testFindByDepartmentAndDeletedFalse_OnlyDeletedEmployeesInDepartment_ReturnsEmpty() {
        // Arrange
        entityManager.persistAndFlush(deletedEmployee);

        // Act
        List<Employee> employees = employeeRepository.findByDepartmentAndDeletedFalse("Shipping");

        // Assert
        assertNotNull(employees);
        assertTrue(employees.isEmpty());
    }

    // ==================== SOFT DELETE BEHAVIOR TESTS ====================

    @Test
    @DisplayName("Test Soft Delete - Mark as Deleted - Does Not Remove from Database")
    void testSoftDelete_MarkAsDeleted_DoesNotRemoveFromDatabase() {
        // Arrange
        Employee savedEmployee = entityManager.persistAndFlush(testEmployee1);
        Long employeeId = savedEmployee.getId();

        // Act
        savedEmployee.setDeleted(true);
        employeeRepository.save(savedEmployee);
        entityManager.flush();
        entityManager.clear();

        // Assert
        Optional<Employee> found = employeeRepository.findById(employeeId);
        assertTrue(found.isPresent());
        assertTrue(found.get().getDeleted());

        // Verify not returned by findByDeletedFalse
        List<Employee> activeEmployees = employeeRepository.findByDeletedFalse();
        assertFalse(activeEmployees.stream().anyMatch(e -> e.getId().equals(employeeId)));
    }

    @Test
    @DisplayName("Test Soft Delete - Restore Deleted Employee - Appears in Active List")
    void testSoftDelete_RestoreDeletedEmployee_AppearsInActiveList() {
        // Arrange
        Employee savedEmployee = entityManager.persistAndFlush(deletedEmployee);
        Long employeeId = savedEmployee.getId();

        // Act
        savedEmployee.setDeleted(false);
        employeeRepository.save(savedEmployee);
        entityManager.flush();

        // Assert
        List<Employee> activeEmployees = employeeRepository.findByDeletedFalse();
        assertTrue(activeEmployees.stream().anyMatch(e -> e.getId().equals(employeeId)));
    }

    // ==================== UNIQUE CONSTRAINT TESTS ====================

    @Test
    @DisplayName("Test Unique BadgeId Constraint - Duplicate BadgeId - Throws Exception")
    void testUniqueBadgeIdConstraint_DuplicateBadgeId_ThrowsException() {
        // Arrange
        entityManager.persistAndFlush(testEmployee1);

        Employee duplicateEmployee = new Employee();
        duplicateEmployee.setBadgeId("EMP001");
        duplicateEmployee.setFirstName("Duplicate");
        duplicateEmployee.setLastName("Employee");
        duplicateEmployee.setEmail("duplicate@warehouse.com");
        duplicateEmployee.setDeleted(false);

        // Act & Assert
        assertThrows(Exception.class, () -> {
            entityManager.persistAndFlush(duplicateEmployee);
        });
    }

    // ==================== BOUNDARY AND EDGE CASE TESTS ====================

    @Test
    @DisplayName("Test Save - Maximum Length Fields - Persists Successfully")
    void testSave_MaximumLengthFields_PersistsSuccessfully() {
        // Arrange
        String longString = "A".repeat(255);
        testEmployee1.setFirstName(longString);
        testEmployee1.setLastName(longString);

        // Act
        Employee savedEmployee = employeeRepository.save(testEmployee1);
        entityManager.flush();

        // Assert
        assertNotNull(savedEmployee.getId());
        assertEquals(longString, savedEmployee.getFirstName());
    }

    @Test
    @DisplayName("Test Save - Special Characters in Name - Persists Successfully")
    void testSave_SpecialCharactersInName_PersistsSuccessfully() {
        // Arrange
        testEmployee1.setFirstName("Jean-Pierre");
        testEmployee1.setLastName("O'Connor");

        // Act
        Employee savedEmployee = employeeRepository.save(testEmployee1);
        entityManager.flush();

        // Assert
        assertNotNull(savedEmployee.getId());
        assertEquals("Jean-Pierre", savedEmployee.getFirstName());
        assertEquals("O'Connor", savedEmployee.getLastName());
    }

    @Test
    @DisplayName("Test findAll - Large Dataset - Returns All Employees")
    void testFindAll_LargeDataset_ReturnsAllEmployees() {
        // Arrange
        for (int i = 0; i < 100; i++) {
            Employee employee = new Employee();
            employee.setBadgeId("EMP" + String.format("%03d", i));
            employee.setFirstName("Employee" + i);
            employee.setLastName("Test" + i);
            employee.setEmail("employee" + i + "@warehouse.com");
            employee.setDeleted(false);
            entityManager.persist(employee);
        }
        entityManager.flush();

        // Act
        List<Employee> allEmployees = employeeRepository.findByDeletedFalse();

        // Assert
        assertNotNull(allEmployees);
        assertEquals(100, allEmployees.size());
    }

    @Test
    @DisplayName("Test Save - Future Hire Date - Persists Successfully")
    void testSave_FutureHireDate_PersistsSuccessfully() {
        // Arrange
        testEmployee1.setHireDate(LocalDate.now().plusDays(30));

        // Act
        Employee savedEmployee = employeeRepository.save(testEmployee1);
        entityManager.flush();

        // Assert
        assertNotNull(savedEmployee.getId());
        assertTrue(savedEmployee.getHireDate().isAfter(LocalDate.now()));
    }

    @Test
    @DisplayName("Test Save - Past Hire Date - Persists Successfully")
    void testSave_PastHireDate_PersistsSuccessfully() {
        // Arrange
        testEmployee1.setHireDate(LocalDate.of(2000, 1, 1));

        // Act
        Employee savedEmployee = employeeRepository.save(testEmployee1);
        entityManager.flush();

        // Assert
        assertNotNull(savedEmployee.getId());
        assertTrue(savedEmployee.getHireDate().isBefore(LocalDate.now()));
    }
}