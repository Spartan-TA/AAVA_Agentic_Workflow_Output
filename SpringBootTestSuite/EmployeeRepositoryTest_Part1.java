package com.example.warehouse.employee;

import com.example.warehouse.employee.entity.Employee;
import com.example.warehouse.employee.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class EmployeeRepositoryTest_Part1 {
    @Autowired
    private EmployeeRepository employeeRepository;

    private Employee employee1;
    private Employee employee2;
    private Employee employee3;

    @BeforeEach
    void setUp() {
        employeeRepository.deleteAll();
        employee1 = new Employee(null, "Alice Smith", "BADGE001", "WORKER", "Receiving", "A", LocalDate.now().minusYears(2), "ACTIVE", false);
        employee2 = new Employee(null, "Bob Jones", "BADGE002", "SUPERVISOR", "Shipping", "B", LocalDate.now().minusYears(1), "ACTIVE", false);
        employee3 = new Employee(null, "Charlie Brown", "BADGE003", "HR", "HR", "C", LocalDate.now().minusMonths(6), "INACTIVE", false);
        employeeRepository.saveAll(List.of(employee1, employee2, employee3));
    }

    @Test
    @DisplayName("Test find all employees")
    void testFindAllEmployees() {
        List<Employee> employees = employeeRepository.findAll();
        assertEquals(3, employees.size());
    }

    @Test
    @DisplayName("Test find by id")
    void testFindById() {
        Optional<Employee> found = employeeRepository.findById(employee1.getId());
        assertTrue(found.isPresent());
        assertEquals("Alice Smith", found.get().getName());
    }

    @Test
    @DisplayName("Test save employee")
    void testSaveEmployee() {
        Employee newEmp = new Employee(null, "Dana White", "BADGE004", "WORKER", "Packing", "D", LocalDate.now(), "ACTIVE", false);
        Employee saved = employeeRepository.save(newEmp);
        assertNotNull(saved.getId());
        assertEquals("Dana White", saved.getName());
    }

    @Test
    @DisplayName("Test update employee")
    void testUpdateEmployee() {
        employee1.setDepartment("Packing");
        Employee updated = employeeRepository.save(employee1);
        assertEquals("Packing", updated.getDepartment());
    }

    @Test
    @DisplayName("Test delete employee")
    void testDeleteEmployee() {
        employeeRepository.delete(employee2);
        assertFalse(employeeRepository.findById(employee2.getId()).isPresent());
    }
}