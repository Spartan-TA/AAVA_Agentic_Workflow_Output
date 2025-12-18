package com.warehouse.ems.employee;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class EmployeeRepositoryTest {

    @Autowired
    private EmployeeRepository employeeRepository;

    private Employee activeEmployee;
    private Employee deletedEmployee;

    @BeforeEach
    public void setUp() {
        activeEmployee = Employee.builder()
                .name("Alice")
                .badgeId("A1")
                .role("HR")
                .department("HR")
                .hireDate(LocalDate.now())
                .status("ACTIVE")
                .deleted(false)
                .build();

        deletedEmployee = Employee.builder()
                .name("Bob")
                .badgeId("B1")
                .role("WORKER")
                .department("Logistics")
                .hireDate(LocalDate.now())
                .status("INACTIVE")
                .deleted(true)
                .build();

        employeeRepository.save(activeEmployee);
        employeeRepository.save(deletedEmployee);
    }

    @Test
    public void testFindByDeletedFalseReturnsOnlyNonDeletedEmployees() {
        List<Employee> result = employeeRepository.findByDeletedFalse();
        assertTrue(result.stream().allMatch(e -> !e.isDeleted()));
        assertTrue(result.stream().anyMatch(e -> e.getBadgeId().equals("A1")));
        assertFalse(result.stream().anyMatch(e -> e.getBadgeId().equals("B1")));
    }

    @Test
    public void testFindByNameContainingIgnoreCaseAndDeletedFalse() {
        List<Employee> result = employeeRepository.findByNameContainingIgnoreCaseAndDeletedFalse("ali");
        assertTrue(result.stream().anyMatch(e -> e.getName().equals("Alice")));
        assertTrue(result.stream().allMatch(e -> !e.isDeleted()));
    }

    @Test
    public void testFindByDepartmentAndDeletedFalseWithValidDepartment() {
        List<Employee> result = employeeRepository.findByDepartmentAndDeletedFalse("HR");
        assertTrue(result.stream().anyMatch(e -> e.getDepartment().equals("HR")));
        assertTrue(result.stream().allMatch(e -> !e.isDeleted()));
    }

    @Test
    public void testFindByDepartmentAndDeletedFalseWithInvalidDepartment() {
        List<Employee> result = employeeRepository.findByDepartmentAndDeletedFalse("Finance");
        assertTrue(result.isEmpty());
    }

    @Test
    public void testFindByRoleAndDeletedFalseWithValidRole() {
        List<Employee> result = employeeRepository.findByRoleAndDeletedFalse("HR");
        assertTrue(result.stream().anyMatch(e -> e.getRole().equals("HR")));
        assertTrue(result.stream().allMatch(e -> !e.isDeleted()));
    }

    @Test
    public void testFindByRoleAndDeletedFalseWithInvalidRole() {
        List<Employee> result = employeeRepository.findByRoleAndDeletedFalse("CEO");
        assertTrue(result.isEmpty());
    }

    @Test
    public void testFindByBadgeIdAndDeletedFalseWithExistingBadgeId() {
        Optional<Employee> result = employeeRepository.findByBadgeIdAndDeletedFalse("A1");
        assertTrue(result.isPresent());
        assertEquals("A1", result.get().getBadgeId());
    }

    @Test
    public void testFindByBadgeIdAndDeletedFalseWithNonExistingBadgeId() {
        Optional<Employee> result = employeeRepository.findByBadgeIdAndDeletedFalse("Z9");
        assertTrue(result.isEmpty());
    }

    @Test
    public void testDeletedEmployeesAreExcludedFromAllQueries() {
        assertFalse(employeeRepository.findByDeletedFalse().stream().anyMatch(e -> e.isDeleted()));
        assertTrue(employeeRepository.findByBadgeIdAndDeletedFalse("B1").isEmpty());
    }
}