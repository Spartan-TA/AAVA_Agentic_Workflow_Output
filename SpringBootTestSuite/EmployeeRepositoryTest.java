package com.example.warehouse.repository;

import com.example.warehouse.entity.Employee;
import com.example.warehouse.entity.EmployeeStatus;
import com.example.warehouse.entity.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.*;

import java.time.LocalDate;
import java.util.List;
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
        employee.setName("John Doe");
        employee.setBadgeId("B123");
        employee.setEmail("john.doe@example.com");
        employee.setRole(Role.WORKER);
        employee.setDepartment("Logistics");
        employee.setShiftGroup("A");
        employee.setHireDate(LocalDate.now());
        employee.setStatus(EmployeeStatus.ACTIVE);
        employee.setDeleted(false);
        employeeRepository.save(employee);
    }

    @Test
    void testSave_ValidEmployee_Success() {
        Employee saved = employeeRepository.save(employee);
        assertNotNull(saved.getId());
        assertEquals("John Doe", saved.getName());
    }

    @Test
    void testFindById_ExistingId_ReturnsEmployee() {
        Optional<Employee> found = employeeRepository.findById(employee.getId());
        assertTrue(found.isPresent());
        assertEquals(employee.getBadgeId(), found.get().getBadgeId());
    }

    @Test
    void testFindById_NonExistingId_ReturnsEmpty() {
        Optional<Employee> found = employeeRepository.findById(999L);
        assertFalse(found.isPresent());
    }

    @Test
    void testFindByBadgeId_ExistingBadgeId_ReturnsEmployee() {
        Optional<Employee> found = employeeRepository.findByBadgeId("B123");
        assertTrue(found.isPresent());
        assertEquals(employee.getEmail(), found.get().getEmail());
    }

    @Test
    void testFindByBadgeId_NonExistingBadgeId_ReturnsEmpty() {
        Optional<Employee> found = employeeRepository.findByBadgeId("B999");
        assertFalse(found.isPresent());
    }

    @Test
    void testFindByEmail_ExistingEmail_ReturnsEmployee() {
        Optional<Employee> found = employeeRepository.findByEmail("john.doe@example.com");
        assertTrue(found.isPresent());
        assertEquals(employee.getBadgeId(), found.get().getBadgeId());
    }

    @Test
    void testFindAllByDeletedFalse_ReturnsOnlyActiveEmployees() {
        Page<Employee> page = employeeRepository.findAllByDeletedFalse(PageRequest.of(0, 10));
        assertEquals(1, page.getTotalElements());
        assertFalse(page.getContent().get(0).isDeleted());
    }

    @Test
    void testFindByDepartmentAndDeletedFalse_ReturnsFilteredEmployees() {
        List<Employee> list = employeeRepository.findByDepartmentAndDeletedFalse("Logistics");
        assertEquals(1, list.size());
        assertEquals("Logistics", list.get(0).getDepartment());
    }

    @Test
    void testFindByRoleAndDeletedFalse_ReturnsFilteredEmployees() {
        List<Employee> list = employeeRepository.findByRoleAndDeletedFalse(Role.WORKER);
        assertEquals(1, list.size());
        assertEquals(Role.WORKER, list.get(0).getRole());
    }

    @Test
    void testExistsByBadgeId_ExistingBadgeId_ReturnsTrue() {
        assertTrue(employeeRepository.existsByBadgeId("B123"));
    }

    @Test
    void testExistsByBadgeId_NonExistingBadgeId_ReturnsFalse() {
        assertFalse(employeeRepository.existsByBadgeId("B999"));
    }

    @Test
    void testExistsByEmail_ExistingEmail_ReturnsTrue() {
        assertTrue(employeeRepository.existsByEmail("john.doe@example.com"));
    }
}