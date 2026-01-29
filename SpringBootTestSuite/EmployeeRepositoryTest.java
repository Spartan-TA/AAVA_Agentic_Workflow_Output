package com.wms.ems.employee;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class EmployeeRepositoryTest {
    @Autowired
    private EmployeeRepository employeeRepository;

    private Employee employee1;
    private Employee employee2;
    private Employee employee3;

    @BeforeEach
    void setUp() {
        employeeRepository.deleteAll();
        employee1 = Employee.builder()
                .badgeId("B001")
                .name("Alice")
                .role("Worker")
                .department("Packing")
                .shiftGroup("A")
                .hireDate(LocalDate.of(2020, 1, 1))
                .status("Active")
                .deleted(false)
                .build();
        employee2 = Employee.builder()
                .badgeId("B002")
                .name("Bob")
                .role("Supervisor")
                .department("Shipping")
                .shiftGroup("B")
                .hireDate(LocalDate.of(2021, 2, 2))
                .status("Inactive")
                .deleted(false)
                .build();
        employee3 = Employee.builder()
                .badgeId("B003")
                .name("Charlie")
                .role("Worker")
                .department("Packing")
                .shiftGroup("A")
                .hireDate(LocalDate.of(2022, 3, 3))
                .status("Active")
                .deleted(false)
                .build();
        employeeRepository.save(employee1);
        employeeRepository.save(employee2);
        employeeRepository.save(employee3);
    }

    // Test findByBadgeId normal case
    @Test
    void testFindByBadgeId_Exists_ReturnsEmployee() {
        Optional<Employee> result = employeeRepository.findByBadgeId("B001");
        assertTrue(result.isPresent());
        assertEquals("Alice", result.get().getName());
    }

    // Test findByBadgeId not found
    @Test
    void testFindByBadgeId_NotExists_ReturnsEmpty() {
        Optional<Employee> result = employeeRepository.findByBadgeId("B999");
        assertFalse(result.isPresent());
    }

    // Test findByDepartmentAndStatus
    @Test
    void testFindByDepartmentAndStatus_ReturnsCorrectEmployees() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> page = employeeRepository.findByDepartmentAndStatus("Packing", "Active", pageable);
        assertEquals(2, page.getTotalElements());
    }

    // Test findByDepartment
    @Test
    void testFindByDepartment_ReturnsEmployees() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> page = employeeRepository.findByDepartment("Shipping", pageable);
        assertEquals(1, page.getTotalElements());
        assertEquals("Bob", page.getContent().get(0).getName());
    }

    // Test findByRole
    @Test
    void testFindByRole_ReturnsEmployees() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> page = employeeRepository.findByRole("Worker", pageable);
        assertEquals(2, page.getTotalElements());
    }

    // Test existsByBadgeId true
    @Test
    void testExistsByBadgeId_Exists_ReturnsTrue() {
        assertTrue(employeeRepository.existsByBadgeId("B002"));
    }

    // Test existsByBadgeId false
    @Test
    void testExistsByBadgeId_NotExists_ReturnsFalse() {
        assertFalse(employeeRepository.existsByBadgeId("B999"));
    }

    // Test searchByName partial match
    @Test
    void testSearchByName_PartialMatch_ReturnsEmployees() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> page = employeeRepository.searchByName("li", pageable);
        assertEquals(2, page.getTotalElements()); // Alice and Charlie
    }

    // Test searchByName no match
    @Test
    void testSearchByName_NoMatch_ReturnsEmpty() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> page = employeeRepository.searchByName("ZZZ", pageable);
        assertEquals(0, page.getTotalElements());
    }

    // Test soft delete (SQLDelete/Where)
    @Test
    void testSoftDelete_EmployeeNotReturnedAfterDelete() {
        employeeRepository.delete(employee1);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> page = employeeRepository.findAll(pageable);
        assertEquals(2, page.getTotalElements());
        assertFalse(page.getContent().stream().anyMatch(e -> e.getBadgeId().equals("B001")));
    }
}
