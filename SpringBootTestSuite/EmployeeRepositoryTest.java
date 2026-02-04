package com.company.warehouse.employee.repository;

import com.company.warehouse.employee.entity.Employee;
import com.company.warehouse.employee.entity.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class EmployeeRepositoryTest {
    @Autowired
    private EmployeeRepository employeeRepository;

    private Employee employee1;
    private Employee employee2;
    private Employee employeeDeleted;

    @BeforeEach
    void setUp() {
        employee1 = new Employee();
        employee1.setName("John Doe");
        employee1.setBadgeId("EMP001");
        employee1.setRole(Role.WORKER);
        employee1.setDepartment("Logistics");
        employee1.setHireDate(LocalDate.now());
        employee1.setStatus("ACTIVE");
        employee1.setDeleted(false);
        employee1.setCreatedAt(LocalDateTime.now());

        employee2 = new Employee();
        employee2.setName("Jane Smith");
        employee2.setBadgeId("EMP002");
        employee2.setRole(Role.HR);
        employee2.setDepartment("HR");
        employee2.setHireDate(LocalDate.now());
        employee2.setStatus("INACTIVE");
        employee2.setDeleted(false);
        employee2.setCreatedAt(LocalDateTime.now());

        employeeDeleted = new Employee();
        employeeDeleted.setName("Deleted User");
        employeeDeleted.setBadgeId("EMP003");
        employeeDeleted.setRole(Role.WORKER);
        employeeDeleted.setDepartment("Logistics");
        employeeDeleted.setHireDate(LocalDate.now());
        employeeDeleted.setStatus("ACTIVE");
        employeeDeleted.setDeleted(true);
        employeeDeleted.setCreatedAt(LocalDateTime.now());

        employeeRepository.save(employee1);
        employeeRepository.save(employee2);
        employeeRepository.save(employeeDeleted);
    }

    @Test
    void findByBadgeId_ExistingBadgeId_ReturnsEmployee() {
        Optional<Employee> result = employeeRepository.findByBadgeId("EMP001");
        assertTrue(result.isPresent());
        assertEquals("John Doe", result.get().getName());
    }

    @Test
    void findByBadgeId_NonExistingBadgeId_ReturnsEmpty() {
        Optional<Employee> result = employeeRepository.findByBadgeId("NONEXISTENT");
        assertFalse(result.isPresent());
    }

    @Test
    void findAllActive_ReturnsOnlyActiveEmployees() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> result = employeeRepository.findAllActive(pageable);
        assertEquals(2, result.getTotalElements());
        assertTrue(result.getContent().stream().noneMatch(Employee::isDeleted));
    }

    @Test
    void findByFilters_DepartmentAndStatus_ReturnsFilteredEmployees() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> result = employeeRepository.findByFilters("Logistics", "ACTIVE", pageable);
        assertEquals(1, result.getTotalElements());
        assertEquals("John Doe", result.getContent().get(0).getName());
    }

    @Test
    void findByFilters_NullDepartment_ReturnsAllWithStatus() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> result = employeeRepository.findByFilters(null, "INACTIVE", pageable);
        assertEquals(1, result.getTotalElements());
        assertEquals("Jane Smith", result.getContent().get(0).getName());
    }

    @Test
    void findByFilters_NullStatus_ReturnsAllWithDepartment() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> result = employeeRepository.findByFilters("HR", null, pageable);
        assertEquals(1, result.getTotalElements());
        assertEquals("Jane Smith", result.getContent().get(0).getName());
    }

    @Test
    void findByFilters_NullDepartmentAndStatus_ReturnsAllActive() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> result = employeeRepository.findByFilters(null, null, pageable);
        assertEquals(2, result.getTotalElements());
    }

    @Test
    void findAllActive_DeletedEmployeesNotReturned() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> result = employeeRepository.findAllActive(pageable);
        assertTrue(result.getContent().stream().noneMatch(e -> e.getBadgeId().equals("EMP003")));
    }
}
