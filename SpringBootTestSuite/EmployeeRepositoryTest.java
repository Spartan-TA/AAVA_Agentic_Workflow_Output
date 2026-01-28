package com.warehouse.employee.repository;

import com.warehouse.employee.domain.Employee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class EmployeeRepositoryTest {
    @Autowired
    private EmployeeRepository employeeRepository;

    private Employee employee;
    private Employee deletedEmployee;
    private String badgeId = "BADGE123";
    private String deletedBadgeId = "BADGE999";

    @BeforeEach
    void setUp() {
        employee = Employee.builder()
                .name("John Doe")
                .badgeId(badgeId)
                .role("Worker")
                .department("Packing")
                .shiftGroup("A")
                .hireDate(LocalDate.now())
                .status("Active")
                .deleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        deletedEmployee = Employee.builder()
                .name("Jane Deleted")
                .badgeId(deletedBadgeId)
                .role("Worker")
                .department("Packing")
                .shiftGroup("A")
                .hireDate(LocalDate.now())
                .status("Inactive")
                .deleted(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        employeeRepository.save(employee);
        employeeRepository.save(deletedEmployee);
    }

    @Test
    void testFindByBadgeIdAndDeletedFalse_WithExistingBadgeId_ShouldReturnEmployee() {
        Optional<Employee> found = employeeRepository.findByBadgeIdAndDeletedFalse(badgeId);
        assertTrue(found.isPresent());
        assertEquals(badgeId, found.get().getBadgeId());
        assertFalse(found.get().getDeleted());
    }

    @Test
    void testFindByBadgeIdAndDeletedFalse_WithNonExistentBadgeId_ShouldReturnEmpty() {
        Optional<Employee> found = employeeRepository.findByBadgeIdAndDeletedFalse("NON_EXISTENT");
        assertTrue(found.isEmpty());
    }

    @Test
    void testFindByBadgeIdAndDeletedFalse_WithDeletedEmployee_ShouldReturnEmpty() {
        Optional<Employee> found = employeeRepository.findByBadgeIdAndDeletedFalse(deletedBadgeId);
        assertTrue(found.isEmpty());
    }

    @Test
    void testFindAllByDeletedFalse_ShouldReturnOnlyActiveEmployees() {
        Page<Employee> page = employeeRepository.findAllByDeletedFalse(PageRequest.of(0, 10));
        assertEquals(1, page.getTotalElements());
        assertFalse(page.getContent().get(0).getDeleted());
    }

    @Test
    void testExistsByBadgeIdAndDeletedFalse_WithExistingBadgeId_ShouldReturnTrue() {
        assertTrue(employeeRepository.existsByBadgeIdAndDeletedFalse(badgeId));
    }

    @Test
    void testExistsByBadgeIdAndDeletedFalse_WithNonExistentBadgeId_ShouldReturnFalse() {
        assertFalse(employeeRepository.existsByBadgeIdAndDeletedFalse("NON_EXISTENT"));
    }

    @Test
    void testSave_ShouldSetCreatedAtAndUpdatedAt() {
        Employee newEmp = Employee.builder()
                .name("New Emp")
                .badgeId("BADGE_NEW")
                .role("Worker")
                .department("Packing")
                .shiftGroup("A")
                .hireDate(LocalDate.now())
                .status("Active")
                .deleted(false)
                .build();
        Employee saved = employeeRepository.save(newEmp);
        assertNotNull(saved.getCreatedAt());
        assertNotNull(saved.getUpdatedAt());
    }

    @Test
    void testUpdate_ShouldUpdateUpdatedAt() throws InterruptedException {
        Employee saved = employeeRepository.save(employee);
        LocalDateTime originalUpdatedAt = saved.getUpdatedAt();
        // Simulate update
        Thread.sleep(10); // ensure updatedAt will be different
        saved.setName("Updated Name");
        Employee updated = employeeRepository.save(saved);
        assertTrue(updated.getUpdatedAt().isAfter(originalUpdatedAt));
    }
}
