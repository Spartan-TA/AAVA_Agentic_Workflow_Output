package com.warehouse.management.repository;

import com.warehouse.management.entity.Employee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive repository tests for EmployeeRepository
 * Tests custom queries, boundary conditions, and edge cases
 */
@DataJpaTest
@ActiveProfiles("test")
public class EmployeeRepositoryTest {

    @Autowired
    private EmployeeRepository employeeRepository;

    @BeforeEach
    void setUp() {
        employeeRepository.deleteAll();
        Employee emp1 = new Employee("John Doe", "john@warehouse.com", "WORKER");
        Employee emp2 = new Employee("Jane Smith", "jane@warehouse.com", "SUPERVISOR");
        employeeRepository.save(emp1);
        employeeRepository.save(emp2);
    }

    @Test
    @DisplayName("Test findByEmail returns correct employee")
    void testFindByEmail_ReturnsEmployee() {
        Optional<Employee> result = employeeRepository.findByEmail("john@warehouse.com");
        assertTrue(result.isPresent());
        assertEquals("John Doe", result.get().getName());
    }

    @Test
    @DisplayName("Test findByEmail with non-existent email returns empty")
    void testFindByEmail_NonExistent_ReturnsEmpty() {
        Optional<Employee> result = employeeRepository.findByEmail("notfound@warehouse.com");
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Test findByRole returns correct employees")
    void testFindByRole_ReturnsEmployees() {
        List<Employee> result = employeeRepository.findByRole("SUPERVISOR");
        assertEquals(1, result.size());
        assertEquals("Jane Smith", result.get(0).getName());
    }

    @Test
    @DisplayName("Test findByRole with empty role returns empty list")
    void testFindByRole_EmptyRole_ReturnsEmpty() {
        List<Employee> result = employeeRepository.findByRole("");
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Test save and retrieve employee")
    void testSaveAndRetrieveEmployee() {
        Employee emp = new Employee("Alice", "alice@warehouse.com", "WORKER");
        employeeRepository.save(emp);
        Optional<Employee> result = employeeRepository.findByEmail("alice@warehouse.com");
        assertTrue(result.isPresent());
        assertEquals("Alice", result.get().getName());
    }

    @Test
    @DisplayName("Test delete employee")
    void testDeleteEmployee() {
        Optional<Employee> emp = employeeRepository.findByEmail("john@warehouse.com");
        assertTrue(emp.isPresent());
        employeeRepository.delete(emp.get());
        Optional<Employee> result = employeeRepository.findByEmail("john@warehouse.com");
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Test findAll returns all employees")
    void testFindAll_ReturnsAllEmployees() {
        List<Employee> result = employeeRepository.findAll();
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("Test save employee with null name throws exception")
    void testSaveEmployee_NullName_ThrowsException() {
        Employee emp = new Employee(null, "nullname@warehouse.com", "WORKER");
        assertThrows(Exception.class, () -> employeeRepository.save(emp));
    }

    @Test
    @DisplayName("Test findByEmail with null input throws exception")
    void testFindByEmail_NullInput_ThrowsException() {
        assertThrows(Exception.class, () -> employeeRepository.findByEmail(null));
    }
}
