package com.warehouse.repository;

import com.warehouse.domain.Employee;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class EmployeeRepositoryTest {
    @Autowired
    private EmployeeRepository employeeRepository;

    private Employee employee;

    @BeforeEach
    void setUp() {
        employee = new Employee();
        employee.setBadgeId("B100");
        employee.setName("RepoTest");
        employee.setRole("Loader");
        employee.setDepartment("Receiving");
        employee.setHireDate(LocalDate.of(2022, 5, 5));
        employee.setStatus("Active");
        employee.setDeleted(false);
        employee = employeeRepository.save(employee);
    }

    @AfterEach
    void tearDown() {
        employeeRepository.deleteAll();
    }

    @Test
    void testFindById() {
        Optional<Employee> found = employeeRepository.findById(employee.getId());
        assertTrue(found.isPresent());
        assertEquals("RepoTest", found.get().getName());
    }

    @Test
    void testFindByBadgeId() {
        Optional<Employee> found = employeeRepository.findByBadgeId("B100");
        assertTrue(found.isPresent());
        assertEquals(employee.getId(), found.get().getId());
    }

    @Test
    void testFindByBadgeIdNotFound() {
        Optional<Employee> found = employeeRepository.findByBadgeId("NOPE");
        assertFalse(found.isPresent());
    }

    @Test
    void testSaveAndDelete() {
        Employee newEmp = new Employee();
        newEmp.setBadgeId("B101");
        newEmp.setName("ToDelete");
        newEmp.setRole("Picker");
        newEmp.setDepartment("Shipping");
        newEmp.setHireDate(LocalDate.now());
        newEmp.setStatus("Active");
        newEmp.setDeleted(false);
        Employee saved = employeeRepository.save(newEmp);
        assertNotNull(saved.getId());
        employeeRepository.delete(saved);
        assertFalse(employeeRepository.findById(saved.getId()).isPresent());
    }

    @Test
    void testSaveNullBadgeIdThrows() {
        Employee bad = new Employee();
        bad.setName("NullBadge");
        bad.setRole("Picker");
        bad.setDepartment("Shipping");
        bad.setHireDate(LocalDate.now());
        bad.setStatus("Active");
        bad.setDeleted(false);
        assertThrows(Exception.class, () -> employeeRepository.saveAndFlush(bad));
    }
}
