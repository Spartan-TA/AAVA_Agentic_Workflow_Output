package com.wms.ems.employee.repository;

import com.wms.ems.employee.domain.Employee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class EmployeeRepositoryTest {
    @Autowired
    private EmployeeRepository employeeRepository;

    private Employee employee1;
    private Employee employee2;
    private Employee deletedEmployee;

    @BeforeEach
    void setUp() {
        employee1 = new Employee();
        employee1.setId(UUID.randomUUID());
        employee1.setName("Alice");
        employee1.setBadgeId("BADGE11111");
        employee1.setRole("WORKER");
        employee1.setDepartment("Logistics");
        employee1.setShiftGroup("A");
        employee1.setHireDate(LocalDate.of(2022, 1, 1));
        employee1.setStatus("ACTIVE");
        employee1.setDeleted(false);
        employee1.setCreatedAt(LocalDateTime.now());
        employee1.setUpdatedAt(LocalDateTime.now());

        employee2 = new Employee();
        employee2.setId(UUID.randomUUID());
        employee2.setName("Bob");
        employee2.setBadgeId("BADGE22222");
        employee2.setRole("HR");
        employee2.setDepartment("HR");
        employee2.setShiftGroup("B");
        employee2.setHireDate(LocalDate.of(2023, 2, 2));
        employee2.setStatus("ACTIVE");
        employee2.setDeleted(false);
        employee2.setCreatedAt(LocalDateTime.now());
        employee2.setUpdatedAt(LocalDateTime.now());

        deletedEmployee = new Employee();
        deletedEmployee.setId(UUID.randomUUID());
        deletedEmployee.setName("Charlie");
        deletedEmployee.setBadgeId("BADGE33333");
        deletedEmployee.setRole("SUPERVISOR");
        deletedEmployee.setDepartment("Logistics");
        deletedEmployee.setShiftGroup("C");
        deletedEmployee.setHireDate(LocalDate.of(2021, 3, 3));
        deletedEmployee.setStatus("INACTIVE");
        deletedEmployee.setDeleted(true);
        deletedEmployee.setCreatedAt(LocalDateTime.now());
        deletedEmployee.setUpdatedAt(LocalDateTime.now());

        employeeRepository.save(employee1);
        employeeRepository.save(employee2);
        employeeRepository.save(deletedEmployee);
    }

    @Test
    void testFindByBadgeId_WithExistingBadgeId_ReturnsEmployee() {
        Optional<Employee> found = employeeRepository.findByBadgeId("BADGE11111");
        assertTrue(found.isPresent());
        assertEquals("Alice", found.get().getName());
    }

    @Test
    void testFindByBadgeId_WithNonExistentBadgeId_ReturnsEmpty() {
        Optional<Employee> found = employeeRepository.findByBadgeId("BADGENOTFOUND");
        assertFalse(found.isPresent());
    }

    @Test
    void testFindByDepartment_WithValidDepartment_ReturnsEmployees() {
        List<Employee> found = employeeRepository.findByDepartment("Logistics");
        assertFalse(found.isEmpty());
        assertTrue(found.stream().anyMatch(e -> e.getName().equals("Alice")));
    }

    @Test
    void testFindByStatus_WithActiveStatus_ReturnsActiveEmployees() {
        List<Employee> found = employeeRepository.findByStatus("ACTIVE");
        assertEquals(2, found.size());
        assertTrue(found.stream().anyMatch(e -> e.getName().equals("Alice")));
        assertTrue(found.stream().anyMatch(e -> e.getName().equals("Bob")));
    }

    @Test
    void testFindAllActive_ExcludesDeletedEmployees() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> page = employeeRepository.findAllActive(pageable);
        assertEquals(2, page.getTotalElements());
        assertTrue(page.getContent().stream().noneMatch(Employee::getDeleted));
    }

    @Test
    void testExistsByBadgeIdAndNotDeleted_WithExistingBadgeId_ReturnsTrue() {
        boolean exists = employeeRepository.existsByBadgeIdAndNotDeleted("BADGE11111");
        assertTrue(exists);
    }

    @Test
    void testExistsByBadgeIdAndNotDeleted_WithDeletedEmployee_ReturnsFalse() {
        boolean exists = employeeRepository.existsByBadgeIdAndNotDeleted("BADGE33333");
        assertFalse(exists);
    }
}
