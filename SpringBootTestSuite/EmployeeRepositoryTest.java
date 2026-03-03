package com.warehouse.employeemgmt.employee;

import com.warehouse.employeemgmt.employee.enums.EmployeeRole;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for EmployeeRepository covering normal, boundary, edge, and exception scenarios.
 */
@DataJpaTest
class EmployeeRepositoryTest {
    @Autowired
    private EmployeeRepository employeeRepository;

    private Employee employee1;
    private Employee employee2;
    private Employee employee3;

    @BeforeEach
    void setUp() {
        employee1 = Employee.builder()
                .badgeId("BADGE1")
                .name("Alice")
                .role(EmployeeRole.ADMIN)
                .department("HR")
                .shiftGroup("Morning")
                .hireDate(LocalDate.now().minusYears(2))
                .status("ACTIVE")
                .email("alice@warehouse.com")
                .deleted(false)
                .build();
        employee2 = Employee.builder()
                .badgeId("BADGE2")
                .name("Bob")
                .role(EmployeeRole.WORKER)
                .department("Logistics")
                .shiftGroup("Night")
                .hireDate(LocalDate.now().minusYears(1))
                .status("ACTIVE")
                .email("bob@warehouse.com")
                .deleted(false)
                .build();
        employee3 = Employee.builder()
                .badgeId("BADGE3")
                .name("Charlie")
                .role(EmployeeRole.HR)
                .department("HR")
                .shiftGroup("Morning")
                .hireDate(LocalDate.now().minusMonths(6))
                .status("INACTIVE")
                .email("charlie@warehouse.com")
                .deleted(true)
                .build();
        employeeRepository.save(employee1);
        employeeRepository.save(employee2);
        employeeRepository.save(employee3);
    }

    @Test
    @DisplayName("findByBadgeIdAndDeletedFalse returns correct employee")
    void findByBadgeIdAndDeletedFalse_returnsCorrectEmployee() {
        Optional<Employee> found = employeeRepository.findByBadgeIdAndDeletedFalse("BADGE1");
        assertTrue(found.isPresent());
        assertEquals("Alice", found.get().getName());
    }

    @Test
    @DisplayName("findByBadgeIdAndDeletedFalse returns empty for deleted employee")
    void findByBadgeIdAndDeletedFalse_returnsEmptyForDeleted() {
        Optional<Employee> found = employeeRepository.findByBadgeIdAndDeletedFalse("BADGE3");
        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("findAllByDeletedFalse returns only non-deleted employees")
    void findAllByDeletedFalse_returnsNonDeleted() {
        Page<Employee> page = employeeRepository.findAllByDeletedFalse(PageRequest.of(0, 10));
        assertEquals(2, page.getTotalElements());
    }

    @Test
    @DisplayName("findByDepartmentAndDeletedFalse returns correct employees")
    void findByDepartmentAndDeletedFalse_returnsCorrectEmployees() {
        Page<Employee> page = employeeRepository.findByDepartmentAndDeletedFalse("HR", PageRequest.of(0, 10));
        assertEquals(1, page.getTotalElements());
        assertEquals("Alice", page.getContent().get(0).getName());
    }

    @Test
    @DisplayName("findByRoleAndDeletedFalse returns correct employees")
    void findByRoleAndDeletedFalse_returnsCorrectEmployees() {
        Page<Employee> page = employeeRepository.findByRoleAndDeletedFalse(EmployeeRole.WORKER, PageRequest.of(0, 10));
        assertEquals(1, page.getTotalElements());
        assertEquals("Bob", page.getContent().get(0).getName());
    }

    @Test
    @DisplayName("findByStatusAndDeletedFalse returns correct employees")
    void findByStatusAndDeletedFalse_returnsCorrectEmployees() {
        Page<Employee> page = employeeRepository.findByStatusAndDeletedFalse("ACTIVE", PageRequest.of(0, 10));
        assertEquals(2, page.getTotalElements());
    }

    @Test
    @DisplayName("findByFilters returns correct employees for department filter")
    void findByFilters_departmentFilter() {
        Page<Employee> page = employeeRepository.findByFilters("HR", null, null, PageRequest.of(0, 10));
        assertEquals(1, page.getTotalElements());
        assertEquals("Alice", page.getContent().get(0).getName());
    }

    @Test
    @DisplayName("findByFilters returns correct employees for role filter")
    void findByFilters_roleFilter() {
        Page<Employee> page = employeeRepository.findByFilters(null, EmployeeRole.WORKER, null, PageRequest.of(0, 10));
        assertEquals(1, page.getTotalElements());
        assertEquals("Bob", page.getContent().get(0).getName());
    }

    @Test
    @DisplayName("findByFilters returns correct employees for status filter")
    void findByFilters_statusFilter() {
        Page<Employee> page = employeeRepository.findByFilters(null, null, "ACTIVE", PageRequest.of(0, 10));
        assertEquals(2, page.getTotalElements());
    }

    @Test
    @DisplayName("findByFilters returns all when filters are null")
    void findByFilters_allNull_returnsAll() {
        Page<Employee> page = employeeRepository.findByFilters(null, null, null, PageRequest.of(0, 10));
        assertEquals(2, page.getTotalElements());
    }

    @Test
    @DisplayName("countByDepartmentAndDeletedFalse returns correct count")
    void countByDepartmentAndDeletedFalse_returnsCorrectCount() {
        long count = employeeRepository.countByDepartmentAndDeletedFalse("HR");
        assertEquals(1, count);
    }

    @Test
    @DisplayName("findByShiftGroupAndDeletedFalse returns correct employees")
    void findByShiftGroupAndDeletedFalse_returnsCorrectEmployees() {
        List<Employee> list = employeeRepository.findByShiftGroupAndDeletedFalse("Morning");
        assertEquals(1, list.size());
        assertEquals("Alice", list.get(0).getName());
    }

    @Test
    @DisplayName("findByBadgeIdAndDeletedFalse returns empty for non-existent badgeId")
    void findByBadgeIdAndDeletedFalse_nonExistentBadgeId() {
        Optional<Employee> found = employeeRepository.findByBadgeIdAndDeletedFalse("BADGE999");
        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("findByDepartmentAndDeletedFalse returns empty for non-existent department")
    void findByDepartmentAndDeletedFalse_nonExistentDepartment() {
        Page<Employee> page = employeeRepository.findByDepartmentAndDeletedFalse("NonExistent", PageRequest.of(0, 10));
        assertEquals(0, page.getTotalElements());
    }

    @Test
    @DisplayName("findByRoleAndDeletedFalse returns empty for non-existent role")
    void findByRoleAndDeletedFalse_nonExistentRole() {
        Page<Employee> page = employeeRepository.findByRoleAndDeletedFalse(EmployeeRole.SUPERVISOR, PageRequest.of(0, 10));
        assertEquals(0, page.getTotalElements());
    }
}
