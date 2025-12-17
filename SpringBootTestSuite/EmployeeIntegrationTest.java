package com.warehouse.management.integration;

import com.warehouse.management.entity.Employee;
import com.warehouse.management.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive integration tests for Employee workflows
 * Covers end-to-end scenarios and edge cases
 */
@SpringBootTest
@Transactional
public class EmployeeIntegrationTest {

    @Autowired
    private EmployeeRepository employeeRepository;

    @BeforeEach
    void setUp() {
        employeeRepository.deleteAll();
    }

    @Test
    @DisplayName("Test create and retrieve employee end-to-end")
    void testCreateAndRetrieveEmployee_EndToEnd() {
        Employee emp = new Employee("Bob", "bob@warehouse.com", "WORKER");
        employeeRepository.save(emp);
        Optional<Employee> result = employeeRepository.findByEmail("bob@warehouse.com");
        assertTrue(result.isPresent());
        assertEquals("Bob", result.get().getName());
    }

    @Test
    @DisplayName("Test update employee role end-to-end")
    void testUpdateEmployeeRole_EndToEnd() {
        Employee emp = new Employee("Carol", "carol@warehouse.com", "WORKER");
        employeeRepository.save(emp);
        Optional<Employee> result = employeeRepository.findByEmail("carol@warehouse.com");
        assertTrue(result.isPresent());
        Employee updated = result.get();
        updated.setRole("SUPERVISOR");
        employeeRepository.save(updated);
        Optional<Employee> result2 = employeeRepository.findByEmail("carol@warehouse.com");
        assertEquals("SUPERVISOR", result2.get().getRole());
    }

    @Test
    @DisplayName("Test delete employee end-to-end")
    void testDeleteEmployee_EndToEnd() {
        Employee emp = new Employee("Dave", "dave@warehouse.com", "WORKER");
        employeeRepository.save(emp);
        Optional<Employee> result = employeeRepository.findByEmail("dave@warehouse.com");
        assertTrue(result.isPresent());
        employeeRepository.delete(result.get());
        Optional<Employee> result2 = employeeRepository.findByEmail("dave@warehouse.com");
        assertFalse(result2.isPresent());
    }

    @Test
    @DisplayName("Test find employee by role end-to-end")
    void testFindEmployeeByRole_EndToEnd() {
        Employee emp1 = new Employee("Eve", "eve@warehouse.com", "WORKER");
        Employee emp2 = new Employee("Frank", "frank@warehouse.com", "SUPERVISOR");
        employeeRepository.save(emp1);
        employeeRepository.save(emp2);
        assertEquals(1, employeeRepository.findByRole("SUPERVISOR").size());
        assertEquals(1, employeeRepository.findByRole("WORKER").size());
    }

    @Test
    @DisplayName("Test save employee with null email throws exception end-to-end")
    void testSaveEmployee_NullEmail_ThrowsException_EndToEnd() {
        Employee emp = new Employee("Grace", null, "WORKER");
        assertThrows(Exception.class, () -> employeeRepository.save(emp));
    }

    @Test
    @DisplayName("Test findByEmail with null input throws exception end-to-end")
    void testFindByEmail_NullInput_ThrowsException_EndToEnd() {
        assertThrows(Exception.class, () -> employeeRepository.findByEmail(null));
    }
}
