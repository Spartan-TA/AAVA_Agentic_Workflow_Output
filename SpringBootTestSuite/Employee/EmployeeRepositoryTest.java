package com.example.warehouse.employee;

import com.example.warehouse.employee.entity.Employee;
import com.example.warehouse.employee.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class EmployeeRepositoryTest {
    @Autowired
    private EmployeeRepository employeeRepository;

    private Employee employee1;
    private Employee employee2;

    @BeforeEach
    public void setUp() {
        employee1 = new Employee();
        employee1.setName("John Doe");
        employee1.setBadgeId("BADGE123");
        employee1.setRole("WORKER");
        employee1.setDepartment("Shipping");
        employee1.setShiftGroup("A");
        employee1.setHireDate(LocalDate.now());
        employee1.setStatus("ACTIVE");
        employeeRepository.save(employee1);

        employee2 = new Employee();
        employee2.setName("Jane Smith");
        employee2.setBadgeId("BADGE456");
        employee2.setRole("SUPERVISOR");
        employee2.setDepartment("Receiving");
        employee2.setShiftGroup("B");
        employee2.setHireDate(LocalDate.now());
        employee2.setStatus("INACTIVE");
        employeeRepository.save(employee2);
    }

    @Test
    public void testFindByBadgeId_ValidBadgeId_ReturnsEmployee() {
        Optional<Employee> result = employeeRepository.findByBadgeId("BADGE123");
        assertTrue(result.isPresent());
        assertEquals("John Doe", result.get().getName());
    }

    @Test
    public void testFindByBadgeId_InvalidBadgeId_ReturnsEmpty() {
        Optional<Employee> result = employeeRepository.findByBadgeId("BADGE999");
        assertFalse(result.isPresent());
    }

    @Test
    public void testExistsByBadgeId_ExistingBadgeId_ReturnsTrue() {
        boolean exists = employeeRepository.existsByBadgeId("BADGE123");
        assertTrue(exists);
    }

    @Test
    public void testExistsByBadgeId_NonExistingBadgeId_ReturnsFalse() {
        boolean exists = employeeRepository.existsByBadgeId("BADGE999");
        assertFalse(exists);
    }

    @Test
    public void testFindByDepartment_ValidDepartment_ReturnsEmployees() {
        List<Employee> result = employeeRepository.findByDepartment("Shipping");
        assertEquals(1, result.size());
        assertEquals("John Doe", result.get(0).getName());
    }

    @Test
    public void testFindByDepartment_InvalidDepartment_ReturnsEmptyList() {
        List<Employee> result = employeeRepository.findByDepartment("NonExistentDept");
        assertTrue(result.isEmpty());
    }

    @Test
    public void testFindByStatus_ValidStatus_ReturnsEmployees() {
        List<Employee> result = employeeRepository.findByStatus("ACTIVE");
        assertEquals(1, result.size());
        assertEquals("John Doe", result.get(0).getName());
    }

    @Test
    public void testFindByStatus_InvalidStatus_ReturnsEmptyList() {
        List<Employee> result = employeeRepository.findByStatus("ON_LEAVE");
        assertTrue(result.isEmpty());
    }

    @Test
    public void testFindByDepartmentAndStatus_ValidInputs_ReturnsPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> result = employeeRepository.findByDepartmentAndStatus("Shipping", "ACTIVE", pageable);
        assertEquals(1, result.getTotalElements());
        assertEquals("John Doe", result.getContent().get(0).getName());
    }

    @Test
    public void testFindByDepartmentAndStatus_InvalidInputs_ReturnsEmptyPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> result = employeeRepository.findByDepartmentAndStatus("NonExistentDept", "ACTIVE", pageable);
        assertEquals(0, result.getTotalElements());
    }
}