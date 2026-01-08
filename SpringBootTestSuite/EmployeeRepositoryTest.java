package com.warehouseems.employee;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for EmployeeRepository.
 * Tests all database operations including custom queries, pagination, and filtering.
 */
@DataJpaTest
@ActiveProfiles("test")
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
        // Clear any existing data
        employeeRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();

        // Create test employees
        testEmployee1 = new Employee();
        testEmployee1.setName("John Doe");
        testEmployee1.setBadgeId("EMP001");
        testEmployee1.setRole("WORKER");
        testEmployee1.setDepartment("Shipping");
        testEmployee1.setShiftGroup("DAY_SHIFT");
        testEmployee1.setHireDate(LocalDate.of(2023, 1, 1));
        testEmployee1.setStatus("ACTIVE");
        testEmployee1.setEmail("john.doe@warehouse.com");
        testEmployee1.setPhone("+1234567890");
        testEmployee1.setDeleted(false);

        testEmployee2 = new Employee();
        testEmployee2.setName("Jane Smith");
        testEmployee2.setBadgeId("EMP002");
        testEmployee2.setRole("SUPERVISOR");
        testEmployee2.setDepartment("Receiving");
        testEmployee2.setShiftGroup("DAY_SHIFT");
        testEmployee2.setHireDate(LocalDate.of(2022, 6, 15));
        testEmployee2.setStatus("ACTIVE");
        testEmployee2.setEmail("jane.smith@warehouse.com");
        testEmployee2.setPhone("+1234567891");
        testEmployee2.setDeleted(false);

        testEmployee3 = new Employee();
        testEmployee3.setName("Bob Johnson");
        testEmployee3.setBadgeId("EMP003");
        testEmployee3.setRole("WORKER");
        testEmployee3.setDepartment("Shipping");
        testEmployee3.setShiftGroup("NIGHT_SHIFT");
        testEmployee3.setHireDate(LocalDate.of(2023, 3, 10));
        testEmployee3.setStatus("INACTIVE");
        testEmployee3.setEmail("bob.johnson@warehouse.com");
        testEmployee3.setPhone("+1234567892");
        testEmployee3.setDeleted(true); // Soft-deleted
    }

    // ==================== BASIC CRUD TESTS ====================

    @Nested
    @DisplayName("Basic CRUD Operations")
    class BasicCrudTests {

        @Test
        @DisplayName("Should save employee successfully")
        void testSave_Success() {
            Employee saved = employeeRepository.save(testEmployee1);

            assertNotNull(saved.getId());
            assertEquals("John Doe", saved.getName());
            assertEquals("EMP001", saved.getBadgeId());
            assertNotNull(saved.getCreatedAt());
        }

        @Test
        @DisplayName("Should find employee by ID")
        void testFindById_Success() {
            Employee saved = entityManager.persistAndFlush(testEmployee1);

            Optional<Employee> found = employeeRepository.findById(saved.getId());

            assertTrue(found.isPresent());
            assertEquals("John Doe", found.get().getName());
        }

        @Test
        @DisplayName("Should return empty when employee not found by ID")
        void testFindById_NotFound() {
            Optional<Employee> found = employeeRepository.findById(999L);

            assertFalse(found.isPresent());
        }

        @Test
        @DisplayName("Should update employee successfully")
        void testUpdate_Success() {
            Employee saved = entityManager.persistAndFlush(testEmployee1);
            saved.setName("John Updated");

            Employee updated = employeeRepository.save(saved);

            assertEquals("John Updated", updated.getName());
            assertNotNull(updated.getUpdatedAt());
        }

        @Test
        @DisplayName("Should delete employee successfully")
        void testDelete_Success() {
            Employee saved = entityManager.persistAndFlush(testEmployee1);

            employeeRepository.delete(saved);
            entityManager.flush();

            Optional<Employee> found = employeeRepository.findById(saved.getId());
            assertFalse(found.isPresent());
        }

        @Test
        @DisplayName("Should count all employees")
        void testCount_Success() {
            entityManager.persistAndFlush(testEmployee1);
            entityManager.persistAndFlush(testEmployee2);

            long count = employeeRepository.count();

            assertEquals(2, count);
        }
    }

    // ==================== UNIQUE CONSTRAINT TESTS ====================

    @Nested
    @DisplayName("Unique Constraint Tests")
    class UniqueConstraintTests {

        @Test
        @DisplayName("Should enforce unique badge ID constraint")
        void testUniqueBadgeId_Violation() {
            entityManager.persistAndFlush(testEmployee1);

            Employee duplicate = new Employee();
            duplicate.setName("Duplicate Employee");
            duplicate.setBadgeId("EMP001"); // Same badge ID
            duplicate.setRole("WORKER");
            duplicate.setDepartment("Shipping");
            duplicate.setHireDate(LocalDate.now());
            duplicate.setStatus("ACTIVE");
            duplicate.setDeleted(false);

            assertThrows(Exception.class, () -> {
                employeeRepository.save(duplicate);
                entityManager.flush();
            });
        }

        @Test
        @DisplayName("Should allow same badge ID for soft-deleted employee")
        void testUniqueBadgeId_SoftDeleted() {
            testEmployee1.setDeleted(true);
            entityManager.persistAndFlush(testEmployee1);

            Employee newEmployee = new Employee();
            newEmployee.setName("New Employee");
            newEmployee.setBadgeId("EMP001"); // Same badge ID as soft-deleted
            newEmployee.setRole("WORKER");
            newEmployee.setDepartment("Shipping");
            newEmployee.setHireDate(LocalDate.now());
            newEmployee.setStatus("ACTIVE");
            newEmployee.setDeleted(false);

            // This should succeed as the constraint only applies to non-deleted records
            Employee saved = employeeRepository.save(newEmployee);
            assertNotNull(saved.getId());
        }
    }

    // ==================== CUSTOM QUERY TESTS ====================

    @Nested
    @DisplayName("Custom Query Tests")
    class CustomQueryTests {

        @Test
        @DisplayName("Should find employee by badge ID excluding deleted")
        void testFindByBadgeIdAndDeletedFalse_Success() {
            entityManager.persistAndFlush(testEmployee1);

            Optional<Employee> found = employeeRepository.findByBadgeIdAndDeletedFalse("EMP001");

            assertTrue(found.isPresent());
            assertEquals("John Doe", found.get().getName());
        }

        @Test
        @DisplayName("Should not find soft-deleted employee by badge ID")
        void testFindByBadgeIdAndDeletedFalse_SoftDeleted() {
            entityManager.persistAndFlush(testEmployee3);

            Optional<Employee> found = employeeRepository.findByBadgeIdAndDeletedFalse("EMP003");

            assertFalse(found.isPresent());
        }

        @Test
        @DisplayName("Should find all active employees with pagination")
        void testFindAllByDeletedFalse_Success() {
            entityManager.persistAndFlush(testEmployee1);
            entityManager.persistAndFlush(testEmployee2);
            entityManager.persistAndFlush(testEmployee3); // Soft-deleted

            Pageable pageable = PageRequest.of(0, 10);
            Page<Employee> result = employeeRepository.findAllByDeletedFalse(pageable);

            assertEquals(2, result.getTotalElements());
            assertFalse(result.getContent().stream().anyMatch(Employee::isDeleted));
        }

        @Test
        @DisplayName("Should find employee by ID excluding deleted")
        void testFindByIdAndDeletedFalse_Success() {
            Employee saved = entityManager.persistAndFlush(testEmployee1);

            Optional<Employee> found = employeeRepository.findByIdAndDeletedFalse(saved.getId());

            assertTrue(found.isPresent());
            assertEquals("John Doe", found.get().getName());
        }

        @Test
        @DisplayName("Should not find soft-deleted employee by ID")
        void testFindByIdAndDeletedFalse_SoftDeleted() {
            Employee saved = entityManager.persistAndFlush(testEmployee3);

            Optional<Employee> found = employeeRepository.findByIdAndDeletedFalse(saved.getId());

            assertFalse(found.isPresent());
        }
    }

    // ==================== FILTER TESTS ====================

    @Nested
    @DisplayName("Filter Tests")
    class FilterTests {

        @Test
        @DisplayName("Should filter by department")
        void testFindByDepartmentAndDeletedFalse_Success() {
            entityManager.persistAndFlush(testEmployee1);
            entityManager.persistAndFlush(testEmployee2);

            Pageable pageable = PageRequest.of(0, 10);
            Page<Employee> result = employeeRepository.findByDepartmentAndDeletedFalse("Shipping", pageable);

            assertEquals(1, result.getTotalElements());
            assertEquals("Shipping", result.getContent().get(0).getDepartment());
        }

        @Test
        @DisplayName("Should filter by role")
        void testFindByRoleAndDeletedFalse_Success() {
            entityManager.persistAndFlush(testEmployee1);
            entityManager.persistAndFlush(testEmployee2);

            Pageable pageable = PageRequest.of(0, 10);
            Page<Employee> result = employeeRepository.findByRoleAndDeletedFalse("WORKER", pageable);

            assertEquals(1, result.getTotalElements());
            assertEquals("WORKER", result.getContent().get(0).getRole());
        }

        @Test
        @DisplayName("Should filter by status")
        void testFindByStatusAndDeletedFalse_Success() {
            entityManager.persistAndFlush(testEmployee1);
            testEmployee2.setStatus("INACTIVE");
            entityManager.persistAndFlush(testEmployee2);

            Pageable pageable = PageRequest.of(0, 10);
            Page<Employee> result = employeeRepository.findByStatusAndDeletedFalse("ACTIVE", pageable);

            assertEquals(1, result.getTotalElements());
            assertEquals("ACTIVE", result.getContent().get(0).getStatus());
        }

        @Test
        @DisplayName("Should filter by multiple criteria")
        void testFindByFilters_MultipleCriteria() {
            entityManager.persistAndFlush(testEmployee1);
            entityManager.persistAndFlush(testEmployee2);

            Pageable pageable = PageRequest.of(0, 10);
            Page<Employee> result = employeeRepository.findByFilters(
                    "Shipping", "WORKER", "ACTIVE", pageable);

            assertEquals(1, result.getTotalElements());
            Employee found = result.getContent().get(0);
            assertEquals("Shipping", found.getDepartment());
            assertEquals("WORKER", found.getRole());
            assertEquals("ACTIVE", found.getStatus());
        }

        @Test
        @DisplayName("Should filter with null criteria")
        void testFindByFilters_NullCriteria() {
            entityManager.persistAndFlush(testEmployee1);
            entityManager.persistAndFlush(testEmployee2);

            Pageable pageable = PageRequest.of(0, 10);
            Page<Employee> result = employeeRepository.findByFilters(null, null, null, pageable);

            assertEquals(2, result.getTotalElements());
        }

        @Test
        @DisplayName("Should return empty when no matches found")
        void testFindByFilters_NoMatches() {
            entityManager.persistAndFlush(testEmployee1);

            Pageable pageable = PageRequest.of(0, 10);
            Page<Employee> result = employeeRepository.findByFilters(
                    "NonExistent", "INVALID", "UNKNOWN", pageable);

            assertEquals(0, result.getTotalElements());
        }
    }

    // ==================== PAGINATION TESTS ====================

    @Nested
    @DisplayName("Pagination Tests")
    class PaginationTests {

        @Test
        @DisplayName("Should paginate results correctly")
        void testPagination_Success() {
            for (int i = 1; i <= 25; i++) {
                Employee emp = new Employee();
                emp.setName("Employee " + i);
                emp.setBadgeId("EMP" + String.format("%03d", i));
                emp.setRole("WORKER");
                emp.setDepartment("Shipping");
                emp.setHireDate(LocalDate.now());
                emp.setStatus("ACTIVE");
                emp.setDeleted(false);
                entityManager.persist(emp);
            }
            entityManager.flush();

            Pageable firstPage = PageRequest.of(0, 10);
            Page<Employee> result = employeeRepository.findAllByDeletedFalse(firstPage);

            assertEquals(25, result.getTotalElements());
            assertEquals(3, result.getTotalPages());
            assertEquals(10, result.getContent().size());
            assertTrue(result.hasNext());
        }

        @Test
        @DisplayName("Should handle last page correctly")
        void testPagination_LastPage() {
            for (int i = 1; i <= 25; i++) {
                Employee emp = new Employee();
                emp.setName("Employee " + i);
                emp.setBadgeId("EMP" + String.format("%03d", i));
                emp.setRole("WORKER");
                emp.setDepartment("Shipping");
                emp.setHireDate(LocalDate.now());
                emp.setStatus("ACTIVE");
                emp.setDeleted(false);
                entityManager.persist(emp);
            }
            entityManager.flush();

            Pageable lastPage = PageRequest.of(2, 10);
            Page<Employee> result = employeeRepository.findAllByDeletedFalse(lastPage);

            assertEquals(5, result.getContent().size());
            assertFalse(result.hasNext());
        }

        @Test
        @DisplayName("Should sort results correctly")
        void testPagination_WithSorting() {
            entityManager.persistAndFlush(testEmployee1);
            entityManager.persistAndFlush(testEmployee2);

            Pageable pageable = PageRequest.of(0, 10, Sort.by("name").ascending());
            Page<Employee> result = employeeRepository.findAllByDeletedFalse(pageable);

            assertEquals("Jane Smith", result.getContent().get(0).getName());
            assertEquals("John Doe", result.getContent().get(1).getName());
        }

        @Test
        @DisplayName("Should handle empty page")
        void testPagination_EmptyPage() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Employee> result = employeeRepository.findAllByDeletedFalse(pageable);

            assertEquals(0, result.getTotalElements());
            assertTrue(result.getContent().isEmpty());
        }
    }

    // ==================== EDGE CASE TESTS ====================

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle null values in optional fields")
        void testNullOptionalFields() {
            testEmployee1.setShiftGroup(null);
            testEmployee1.setEmail(null);
            testEmployee1.setPhone(null);
            testEmployee1.setAddress(null);

            Employee saved = employeeRepository.save(testEmployee1);

            assertNotNull(saved.getId());
            assertNull(saved.getShiftGroup());
            assertNull(saved.getEmail());
            assertNull(saved.getPhone());
            assertNull(saved.getAddress());
        }

        @Test
        @DisplayName("Should handle very long names")
        void testVeryLongName() {
            String longName = "A".repeat(255);
            testEmployee1.setName(longName);

            Employee saved = employeeRepository.save(testEmployee1);

            assertEquals(longName, saved.getName());
        }

        @Test
        @DisplayName("Should handle special characters in name")
        void testSpecialCharactersInName() {
            testEmployee1.setName("O'Brien-Smith Jr.");

            Employee saved = employeeRepository.save(testEmployee1);

            assertEquals("O'Brien-Smith Jr.", saved.getName());
        }

        @Test
        @DisplayName("Should handle future hire dates")
        void testFutureHireDate() {
            testEmployee1.setHireDate(LocalDate.now().plusDays(30));

            Employee saved = employeeRepository.save(testEmployee1);

            assertTrue(saved.getHireDate().isAfter(LocalDate.now()));
        }

        @Test
        @DisplayName("Should handle very old hire dates")
        void testVeryOldHireDate() {
            testEmployee1.setHireDate(LocalDate.of(1980, 1, 1));

            Employee saved = employeeRepository.save(testEmployee1);

            assertEquals(LocalDate.of(1980, 1, 1), saved.getHireDate());
        }

        @Test
        @DisplayName("Should handle case-sensitive badge IDs")
        void testCaseSensitiveBadgeId() {
            testEmployee1.setBadgeId("emp001");
            entityManager.persistAndFlush(testEmployee1);

            testEmployee2.setBadgeId("EMP001");
            Employee saved = employeeRepository.save(testEmployee2);

            assertNotNull(saved.getId());
        }

        @Test
        @DisplayName("Should handle concurrent modifications")
        void testConcurrentModification() {
            Employee saved = entityManager.persistAndFlush(testEmployee1);
            entityManager.clear();

            Employee emp1 = employeeRepository.findById(saved.getId()).get();
            Employee emp2 = employeeRepository.findById(saved.getId()).get();

            emp1.setName("Updated by User 1");
            employeeRepository.save(emp1);

            emp2.setName("Updated by User 2");
            Employee final2 = employeeRepository.save(emp2);

            assertEquals("Updated by User 2", final2.getName());
        }
    }

    // ==================== PERFORMANCE TESTS ====================

    @Nested
    @DisplayName("Performance Tests")
    class PerformanceTests {

        @Test
        @DisplayName("Should handle bulk insert efficiently")
        void testBulkInsert() {
            for (int i = 1; i <= 100; i++) {
                Employee emp = new Employee();
                emp.setName("Employee " + i);
                emp.setBadgeId("EMP" + String.format("%03d", i));
                emp.setRole("WORKER");
                emp.setDepartment("Shipping");
                emp.setHireDate(LocalDate.now());
                emp.setStatus("ACTIVE");
                emp.setDeleted(false);
                employeeRepository.save(emp);
            }

            long count = employeeRepository.count();
            assertEquals(100, count);
        }

        @Test
        @DisplayName("Should handle large result sets")
        void testLargeResultSet() {
            for (int i = 1; i <= 1000; i++) {
                Employee emp = new Employee();
                emp.setName("Employee " + i);
                emp.setBadgeId("EMP" + String.format("%04d", i));
                emp.setRole("WORKER");
                emp.setDepartment("Shipping");
                emp.setHireDate(LocalDate.now());
                emp.setStatus("ACTIVE");
                emp.setDeleted(false);
                entityManager.persist(emp);
                if (i % 50 == 0) {
                    entityManager.flush();
                    entityManager.clear();
                }
            }
            entityManager.flush();

            Pageable pageable = PageRequest.of(0, 100);
            Page<Employee> result = employeeRepository.findAllByDeletedFalse(pageable);

            assertEquals(1000, result.getTotalElements());
            assertEquals(100, result.getContent().size());
        }
    }
}