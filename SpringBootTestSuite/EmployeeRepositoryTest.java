package com.company.wem.employee;

import com.company.wem.employee.entity.Employee;
import com.company.wem.employee.repository.EmployeeRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class EmployeeRepositoryTest {

    @Autowired
    private EmployeeRepository employeeRepository;

    private Employee employee;

    @BeforeEach
    void setUp() {
        employee = new Employee(null, "EMP100", "Test User", "WORKER", "Warehouse", "A", LocalDate.now(), "ACTIVE");
        employeeRepository.save(employee);
    }

    @AfterEach
    void tearDown() {
        employeeRepository.deleteAll();
    }

    @Test
    void testFindByBadgeId_Success() {
        Optional<Employee> found = employeeRepository.findByBadgeId("EMP100");
        assertTrue(found.isPresent());
        assertEquals("Test User", found.get().getName());
    }

    @Test
    void testFindByBadgeId_NotFound() {
        Optional<Employee> found = employeeRepository.findByBadgeId("EMP999");
        assertFalse(found.isPresent());
    }

    @Test
    void testSaveEmployee_DuplicateBadgeId_ThrowsException() {
        Employee duplicate = new Employee(null, "EMP100", "Another User", "WORKER", "Warehouse", "A", LocalDate.now(), "ACTIVE");
        assertThrows(DataIntegrityViolationException.class, () -> employeeRepository.saveAndFlush(duplicate));
    }

    @Test
    void testSaveEmployee_NullName_ThrowsException() {
        Employee invalid = new Employee(null, "EMP101", null, "WORKER", "Warehouse", "A", LocalDate.now(), "ACTIVE");
        assertThrows(DataIntegrityViolationException.class, () -> employeeRepository.saveAndFlush(invalid));
    }

    @Test
    void testFindAllByDepartmentAndStatus_Success() {
        List<Employee> results = employeeRepository.findAllByDepartmentAndStatus("Warehouse", "ACTIVE");
        assertFalse(results.isEmpty());
        assertEquals("EMP100", results.get(0).getBadgeId());
    }

    @Test
    void testFindAll_Pagination() {
        for (int i = 1; i <= 20; i++) {
            employeeRepository.save(new Employee(null, "EMP" + (100 + i), "User" + i, "WORKER", "Warehouse", "A", LocalDate.now(), "ACTIVE"));
        }
        List<Employee> page = employeeRepository.findAll(PageRequest.of(0, 10)).getContent();
        assertEquals(10, page.size());
    }

    @Test
    void testDeleteEmployee_SoftDelete() {
        employee.setStatus("DELETED");
        employeeRepository.save(employee);
        Optional<Employee> found = employeeRepository.findByBadgeId("EMP100");
        assertEquals("DELETED", found.get().getStatus());
    }

    @Test
    void testUpdateEmployee_Success() {
        employee.setName("Updated Name");
        employeeRepository.save(employee);
        Optional<Employee> found = employeeRepository.findByBadgeId("EMP100");
        assertEquals("Updated Name", found.get().getName());
    }

    @Test
    void testFindByHireDateBetween() {
        LocalDate start = LocalDate.now().minusDays(1);
        LocalDate end = LocalDate.now().plusDays(1);
        List<Employee> results = employeeRepository.findByHireDateBetween(start, end);
        assertFalse(results.isEmpty());
    }

    @Test
    void testFindByRole() {
        List<Employee> results = employeeRepository.findByRole("WORKER");
        assertFalse(results.isEmpty());
        assertEquals("WORKER", results.get(0).getRole());
    }

    @Test
    void testFindByStatus() {
        List<Employee> results = employeeRepository.findByStatus("ACTIVE");
        assertFalse(results.isEmpty());
        assertEquals("ACTIVE", results.get(0).getStatus());
    }

    @Test
    void testFindByShiftGroup() {
        List<Employee> results = employeeRepository.findByShiftGroup("A");
        assertFalse(results.isEmpty());
        assertEquals("A", results.get(0).getShiftGroup());
    }

    @Test
    void testFindByDepartment() {
        List<Employee> results = employeeRepository.findByDepartment("Warehouse");
        assertFalse(results.isEmpty());
        assertEquals("Warehouse", results.get(0).getDepartment());
    }
}