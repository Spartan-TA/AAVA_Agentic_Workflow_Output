package com.warehouse.ems.repository;

import com.warehouse.ems.domain.Employee;
import org.junit.jupiter.api.*;
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

    private Employee activeEmployee;
    private Employee deletedEmployee;

    @BeforeEach
    void setUp() {
        employeeRepository.deleteAll();
        activeEmployee = Employee.builder()
                .badgeId("BADGE1")
                .name("Alice")
                .role("WORKER")
                .department("Logistics")
                .shiftGroup("A")
                .hireDate(LocalDate.of(2020, 1, 1))
                .status("ACTIVE")
                .deleted(false)
                .build();
        deletedEmployee = Employee.builder()
                .badgeId("BADGE2")
                .name("Bob")
                .role("HR")
                .department("HR")
                .shiftGroup("B")
                .hireDate(LocalDate.of(2019, 5, 10))
                .status("TERMINATED")
                .deleted(true)
                .build();
        employeeRepository.save(activeEmployee);
        employeeRepository.save(deletedEmployee);
    }

    @Test
    void testFindByBadgeIdAndDeletedFalse_FindsActive() {
        Optional<Employee> found = employeeRepository.findByBadgeIdAndDeletedFalse("BADGE1");
        assertTrue(found.isPresent(), "Should find active employee by badgeId");
        assertEquals("Alice", found.get().getName());
    }

    @Test
    void testFindByBadgeIdAndDeletedFalse_DoesNotFindDeleted() {
        Optional<Employee> found = employeeRepository.findByBadgeIdAndDeletedFalse("BADGE2");
        assertFalse(found.isPresent(), "Should not find deleted employee");
    }

    @Test
    void testFindAllActive_ReturnsOnlyActive() {
        List<Employee> active = employeeRepository.findAllActive();
        assertEquals(1, active.size(), "Should return only active employees");
        assertEquals("Alice", active.get(0).getName());
    }

    @Test
    void testExistsByBadgeId_TrueForExisting() {
        assertTrue(employeeRepository.existsByBadgeId("BADGE1"));
        assertTrue(employeeRepository.existsByBadgeId("BADGE2"));
    }

    @Test
    void testExistsByBadgeId_FalseForNonExisting() {
        assertFalse(employeeRepository.existsByBadgeId("BADGE3"));
    }

    @Test
    void testFindByDepartment_ReturnsCorrectEmployees() {
        List<Employee> hr = employeeRepository.findByDepartment("HR");
        assertEquals(0, hr.size(), "Should not return deleted employees");
        List<Employee> logistics = employeeRepository.findByDepartment("Logistics");
        assertEquals(1, logistics.size());
        assertEquals("Alice", logistics.get(0).getName());
    }

    @Test
    void testFindByStatus_ReturnsCorrectEmployees() {
        List<Employee> active = employeeRepository.findByStatus("ACTIVE");
        assertEquals(1, active.size());
        assertEquals("Alice", active.get(0).getName());
        List<Employee> terminated = employeeRepository.findByStatus("TERMINATED");
        assertEquals(0, terminated.size());
    }

    @Test
    void testSaveAndFindById() {
        Employee emp = Employee.builder()
                .badgeId("BADGE3")
                .name("Charlie")
                .role("SUPERVISOR")
                .department("Ops")
                .shiftGroup("C")
                .hireDate(LocalDate.of(2021, 2, 2))
                .status("ACTIVE")
                .deleted(false)
                .build();
        Employee saved = employeeRepository.save(emp);
        Optional<Employee> found = employeeRepository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("Charlie", found.get().getName());
    }

    @Test
    void testDuplicateBadgeIdThrows() {
        Employee emp = Employee.builder()
                .badgeId("BADGE1")
                .name("Duplicate")
                .role("WORKER")
                .department("Logistics")
                .shiftGroup("A")
                .hireDate(LocalDate.of(2022, 1, 1))
                .status("ACTIVE")
                .deleted(false)
                .build();
        assertThrows(Exception.class, () -> employeeRepository.save(emp), "Should throw on duplicate badgeId");
    }

    @Test
    void testBoundaryBadgeIdLength() {
        String badgeId = "B".repeat(32);
        Employee emp = Employee.builder()
                .badgeId(badgeId)
                .name("Boundary")
                .role("WORKER")
                .department("Logistics")
                .shiftGroup("A")
                .hireDate(LocalDate.of(2022, 1, 1))
                .status("ACTIVE")
                .deleted(false)
                .build();
        Employee saved = employeeRepository.save(emp);
        assertEquals(badgeId, saved.getBadgeId());
    }

    @Test
    void testNullDepartmentAndShiftGroup() {
        Employee emp = Employee.builder()
                .badgeId("BADGE4")
                .name("Nulls")
                .role("WORKER")
                .department(null)
                .shiftGroup(null)
                .hireDate(LocalDate.of(2022, 1, 1))
                .status("ACTIVE")
                .deleted(false)
                .build();
        Employee saved = employeeRepository.save(emp);
        assertNull(saved.getDepartment());
        assertNull(saved.getShiftGroup());
    }

    @Test
    void testSqlInjectionAttempt() {
        Employee emp = Employee.builder()
                .badgeId("'; DROP TABLE employee; --")
                .name("SQL Injection")
                .role("WORKER")
                .department("Logistics")
                .shiftGroup("A")
                .hireDate(LocalDate.of(2022, 1, 1))
                .status("ACTIVE")
                .deleted(false)
                .build();
        Employee saved = employeeRepository.save(emp);
        assertEquals("'; DROP TABLE employee; --", saved.getBadgeId());
    }

    @Test
    void testXssAttempt() {
        Employee emp = Employee.builder()
                .badgeId("BADGEXSS")
                .name("<script>alert('xss')</script>")
                .role("WORKER")
                .department("Logistics")
                .shiftGroup("A")
                .hireDate(LocalDate.of(2022, 1, 1))
                .status("ACTIVE")
                .deleted(false)
                .build();
        Employee saved = employeeRepository.save(emp);
        assertEquals("<script>alert('xss')</script>", saved.getName());
    }

    @Test
    void testFindAllActive_EmptyWhenAllDeleted() {
        activeEmployee.setDeleted(true);
        employeeRepository.save(activeEmployee);
        List<Employee> active = employeeRepository.findAllActive();
        assertEquals(0, active.size(), "Should return empty when all employees are deleted");
    }

    @Test
    void testFindByDepartment_EmptyForNonExisting() {
        List<Employee> none = employeeRepository.findByDepartment("NonExistingDept");
        assertEquals(0, none.size());
    }

    @Test
    void testFindByStatus_EmptyForNonExisting() {
        List<Employee> none = employeeRepository.findByStatus("NONEXISTENT");
        assertEquals(0, none.size());
    }
}
