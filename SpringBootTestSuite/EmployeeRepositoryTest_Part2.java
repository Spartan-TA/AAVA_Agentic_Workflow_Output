package com.example.warehouse.employee;

import com.example.warehouse.employee.entity.Employee;
import com.example.warehouse.employee.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class EmployeeRepositoryTest_Part2 {
    @Autowired
    private EmployeeRepository employeeRepository;

    private Employee employee1;
    private Employee employee2;
    private Employee employee3;

    @BeforeEach
    void setUp() {
        employeeRepository.deleteAll();
        employee1 = new Employee(null, "Alice Smith", "BADGE001", "WORKER", "Receiving", "A", java.time.LocalDate.now().minusYears(2), "ACTIVE", false);
        employee2 = new Employee(null, "Bob Jones", "BADGE002", "SUPERVISOR", "Shipping", "B", java.time.LocalDate.now().minusYears(1), "ACTIVE", false);
        employee3 = new Employee(null, "Charlie Brown", "BADGE003", "HR", "HR", "C", java.time.LocalDate.now().minusMonths(6), "INACTIVE", false);
        employeeRepository.saveAll(List.of(employee1, employee2, employee3));
    }

    @Test
    @DisplayName("Test pagination")
    void testPagination() {
        Pageable pageable = PageRequest.of(0, 2);
        Page<Employee> page = employeeRepository.findAll(pageable);
        assertEquals(2, page.getContent().size());
        assertEquals(3, page.getTotalElements());
    }

    @Test
    @DisplayName("Test filter by department")
    void testFilterByDepartment() {
        List<Employee> hrEmployees = employeeRepository.findByDepartment("HR");
        assertEquals(1, hrEmployees.size());
        assertEquals("Charlie Brown", hrEmployees.get(0).getName());
    }

    @Test
    @DisplayName("Test filter by role")
    void testFilterByRole() {
        List<Employee> supervisors = employeeRepository.findByRole("SUPERVISOR");
        assertEquals(1, supervisors.size());
        assertEquals("Bob Jones", supervisors.get(0).getName());
    }

    @Test
    @DisplayName("Test filter by status")
    void testFilterByStatus() {
        List<Employee> activeEmployees = employeeRepository.findByStatus("ACTIVE");
        assertEquals(2, activeEmployees.size());
    }

    @Test
    @DisplayName("Test custom query: find by badgeId")
    void testFindByBadgeId() {
        Optional<Employee> found = employeeRepository.findByBadgeId("BADGE001");
        assertTrue(found.isPresent());
        assertEquals("Alice Smith", found.get().getName());
    }

    @Test
    @DisplayName("Test soft delete query")
    void testSoftDeleteQuery() {
        employee1.setDeleted(true);
        employeeRepository.save(employee1);
        List<Employee> notDeleted = employeeRepository.findByDeletedFalse();
        assertEquals(2, notDeleted.size());
        assertFalse(notDeleted.stream().anyMatch(e -> e.getId().equals(employee1.getId())));
    }

    @Test
    @DisplayName("Test find by null department returns empty")
    void testFindByNullDepartment() {
        List<Employee> result = employeeRepository.findByDepartment(null);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Test find by empty role returns empty")
    void testFindByEmptyRole() {
        List<Employee> result = employeeRepository.findByRole("");
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Test find by invalid badgeId returns empty")
    void testFindByInvalidBadgeId() {
        Optional<Employee> found = employeeRepository.findByBadgeId("INVALID");
        assertTrue(found.isEmpty());
    }
}