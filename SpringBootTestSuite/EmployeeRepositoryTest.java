package com.company.warehouse.employee;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

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
        employee.setBadgeId("ABC123");
        employee.setRole("Worker");
        employee.setDepartment("Logistics");
        employee.setShiftGroup("A");
        employee.setHireDate(LocalDate.now().minusDays(10));
        employee.setStatus("ACTIVE");
        employee.setCreatedAt(LocalDateTime.now().minusDays(10));
        employee.setUpdatedAt(LocalDateTime.now().minusDays(1));
        employeeRepository.save(employee);
    }

    @Test
    @DisplayName("testFindByBadgeId_WithExistingBadgeId_ShouldReturnEmployee")
    public void testFindByBadgeId_WithExistingBadgeId_ShouldReturnEmployee() {
        Optional<Employee> found = employeeRepository.findByBadgeId("ABC123");
        assertTrue(found.isPresent());
        assertEquals("John Doe", found.get().getName());
    }

    @Test
    @DisplayName("testFindByBadgeId_WithNonExistingBadgeId_ShouldReturnEmpty")
    public void testFindByBadgeId_WithNonExistingBadgeId_ShouldReturnEmpty() {
        Optional<Employee> found = employeeRepository.findByBadgeId("NOTFOUND");
        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("testExistsByBadgeId_WithExistingBadgeId_ShouldReturnTrue")
    public void testExistsByBadgeId_WithExistingBadgeId_ShouldReturnTrue() {
        boolean exists = employeeRepository.existsByBadgeId("ABC123");
        assertTrue(exists);
    }

    @Test
    @DisplayName("testExistsByBadgeId_WithNonExistingBadgeId_ShouldReturnFalse")
    public void testExistsByBadgeId_WithNonExistingBadgeId_ShouldReturnFalse() {
        boolean exists = employeeRepository.existsByBadgeId("NOTFOUND");
        assertFalse(exists);
    }
}
