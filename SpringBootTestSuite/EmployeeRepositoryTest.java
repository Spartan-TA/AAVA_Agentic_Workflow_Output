package SpringBootTestSuite;

import com.example.warehouse.model.Employee;
import com.example.warehouse.repository.EmployeeRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Repository tests for EmployeeRepository.
 * Uses @DataJpaTest for all custom queries and edge cases.
 */
@DataJpaTest
class EmployeeRepositoryTest {
    @Autowired
    private EmployeeRepository employeeRepository;

    private Employee employee;

    @BeforeEach
    void setUp() {
        employee = new Employee();
        employee.setName("John Doe");
        employee.setBadgeId("BADGE123");
        employee.setRole("WORKER");
        employee.setDepartment("Shipping");
        employee.setStatus("ACTIVE");
        employee.setCreatedAt(LocalDateTime.now());
        employee.setDeletedAt(null);
        employeeRepository.save(employee);
    }

    @Test
    void testFindByBadgeId_ValidBadgeId_ReturnsEmployee() {
        Optional<Employee> result = employeeRepository.findByBadgeId("BADGE123");
        assertTrue(result.isPresent());
        assertEquals("John Doe", result.get().getName());
    }

    @Test
    void testFindByBadgeId_InvalidBadgeId_ReturnsEmpty() {
        Optional<Employee> result = employeeRepository.findByBadgeId("BADGE999");
        assertFalse(result.isPresent());
    }

    @Test
    void testFindAllActive_ReturnsOnlyActiveEmployees() {
        List<Employee> result = employeeRepository.findAllActive();
        assertEquals(1, result.size());
        assertNull(result.get(0).getDeletedAt());
    }

    @Test
    void testFindAllActive_ExcludesSoftDeleted() {
        Employee terminated = new Employee();
        terminated.setName("Jane Smith");
        terminated.setBadgeId("BADGE124");
        terminated.setRole("SUPERVISOR");
        terminated.setDepartment("Receiving");
        terminated.setStatus("TERMINATED");
        terminated.setCreatedAt(LocalDateTime.now());
        terminated.setDeletedAt(LocalDateTime.now());
        employeeRepository.save(terminated);
        List<Employee> result = employeeRepository.findAllActive();
        assertEquals(1, result.size());
        assertEquals("John Doe", result.get(0).getName());
    }

    @Test
    void testFindByDepartment_ValidDepartment_ReturnsEmployees() {
        List<Employee> result = employeeRepository.findByDepartment("Shipping");
        assertEquals(1, result.size());
        assertEquals("Shipping", result.get(0).getDepartment());
    }

    @Test
    void testFindByDepartment_InvalidDepartment_ReturnsEmpty() {
        List<Employee> result = employeeRepository.findByDepartment("Nonexistent");
        assertTrue(result.isEmpty());
    }

    @Test
    void testFindByRole_ValidRole_ReturnsEmployees() {
        List<Employee> result = employeeRepository.findByRole("WORKER");
        assertEquals(1, result.size());
        assertEquals("WORKER", result.get(0).getRole());
    }

    @Test
    void testFindByRole_InvalidRole_ReturnsEmpty() {
        List<Employee> result = employeeRepository.findByRole("MANAGER");
        assertTrue(result.isEmpty());
    }

    @Test
    void testFindByStatus_ValidStatus_ReturnsEmployees() {
        List<Employee> result = employeeRepository.findByStatus("ACTIVE");
        assertEquals(1, result.size());
        assertEquals("ACTIVE", result.get(0).getStatus());
    }

    @Test
    void testSave_ValidEmployee_Success() {
        Employee newEmp = new Employee();
        newEmp.setName("Alice");
        newEmp.setBadgeId("BADGE125");
        newEmp.setRole("HR");
        newEmp.setDepartment("HR");
        newEmp.setStatus("ACTIVE");
        newEmp.setCreatedAt(LocalDateTime.now());
        newEmp.setDeletedAt(null);
        Employee saved = employeeRepository.save(newEmp);
        assertNotNull(saved);
        assertEquals("Alice", saved.getName());
    }

    @Test
    void testSave_DuplicateBadgeId_ThrowsException() {
        Employee duplicate = new Employee();
        duplicate.setName("Duplicate");
        duplicate.setBadgeId("BADGE123");
        duplicate.setRole("WORKER");
        duplicate.setDepartment("Shipping");
        duplicate.setStatus("ACTIVE");
        duplicate.setCreatedAt(LocalDateTime.now());
        duplicate.setDeletedAt(null);
        assertThrows(Exception.class, () -> employeeRepository.save(duplicate));
    }
}
