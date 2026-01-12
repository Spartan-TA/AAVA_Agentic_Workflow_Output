package com.warehouse.ems.repository;

import com.warehouse.ems.entity.Employee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class EmployeeRepositoryTest {

    @Autowired
    private EmployeeRepository employeeRepository;

    private Employee employee;

    @BeforeEach
    void setUp() {
        employee = new Employee(null, "John Doe", "B123", "WORKER", "Logistics", "A", LocalDate.now(), "ACTIVE", false);
        employeeRepository.save(employee);
    }

    @Test
    void testFindById_ExistingId_ReturnsEmployee() {
        Optional<Employee> found = employeeRepository.findById(employee.getId());
        assertTrue(found.isPresent());
        assertEquals("John Doe", found.get().getName());
    }

    @Test
    void testFindById_NonExistingId_ReturnsEmpty() {
        Optional<Employee> found = employeeRepository.findById(999L);
        assertFalse(found.isPresent());
    }

    @Test
    void testFindAll_ReturnsList() {
        List<Employee> employees = employeeRepository.findAll();
        assertFalse(employees.isEmpty());
    }

    @Test
    void testSave_AndDelete() {
        Employee newEmp = new Employee(null, "Alice", "B124", "SUPERVISOR", "Ops", "B", LocalDate.now(), "ACTIVE", false);
        Employee saved = employeeRepository.save(newEmp);
        assertNotNull(saved.getId());

        employeeRepository.delete(saved);
        assertFalse(employeeRepository.findById(saved.getId()).isPresent());
    }
}