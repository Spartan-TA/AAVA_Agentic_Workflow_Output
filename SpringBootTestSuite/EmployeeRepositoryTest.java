package com.warehouse.employee.management;

import com.warehouse.employee.management.domain.Employee;
import com.warehouse.employee.management.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class EmployeeRepositoryTest {
    @Autowired
    private EmployeeRepository employeeRepository;

    private Employee employee;

    @BeforeEach
    public void setUp() {
        employee = new Employee();
        employee.setName("John Doe");
        employee.setBadgeId("BADGE123");
        employee.setRole("WORKER");
        employee.setDepartment("Logistics");
        employee.setShiftGroup("A");
        employee.setHireDate(LocalDate.of(2022, 1, 1));
        employee.setStatus("ACTIVE");
        employee.setCreatedAt(LocalDateTime.now());
        employee.setUpdatedAt(LocalDateTime.now());
        employeeRepository.save(employee);
    }

    @Test
    public void testSaveEmployee_ShouldPersistEmployee() {
        Employee found = employeeRepository.findById(employee.getId()).orElse(null);
        assertNotNull(found);
        assertEquals("John Doe", found.getName());
    }

    @Test
    public void testFindById_ShouldReturnEmployee() {
        Optional<Employee> found = employeeRepository.findById(employee.getId());
        assertTrue(found.isPresent());
        assertEquals("BADGE123", found.get().getBadgeId());
    }

    @Test
    public void testFindByBadgeId_WithExistingBadgeId_ShouldReturnEmployee() {
        Optional<Employee> found = employeeRepository.findByBadgeId("BADGE123");
        assertTrue(found.isPresent());
        assertEquals("John Doe", found.get().getName());
    }

    @Test
    public void testFindByBadgeId_WithNonExistentBadgeId_ShouldReturnEmpty() {
        Optional<Employee> found = employeeRepository.findByBadgeId("NONEXISTENT");
        assertFalse(found.isPresent());
    }

    @Test
    public void testFindByStatus_ShouldReturnEmployees() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> page = employeeRepository.findByStatus("ACTIVE", pageable);
        assertEquals(1, page.getTotalElements());
    }

    @Test
    public void testFindByDepartment_ShouldReturnEmployees() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> page = employeeRepository.findByDepartment("Logistics", pageable);
        assertEquals(1, page.getTotalElements());
    }

    @Test
    public void testPagination_ShouldReturnCorrectPage() {
        Pageable pageable = PageRequest.of(0, 1);
        Page<Employee> page = employeeRepository.findAll(pageable);
        assertEquals(1, page.getContent().size());
    }

    @Test
    public void testUniqueConstraintOnBadgeId_ShouldThrowException() {
        Employee duplicate = new Employee();
        duplicate.setName("Jane Doe");
        duplicate.setBadgeId("BADGE123");
        duplicate.setRole("SUPERVISOR");
        duplicate.setDepartment("Logistics");
        duplicate.setShiftGroup("B");
        duplicate.setHireDate(LocalDate.of(2022, 2, 1));
        duplicate.setStatus("ACTIVE");
        duplicate.setCreatedAt(LocalDateTime.now());
        duplicate.setUpdatedAt(LocalDateTime.now());
        assertThrows(DataIntegrityViolationException.class, () -> {
            employeeRepository.saveAndFlush(duplicate);
        });
    }
}
