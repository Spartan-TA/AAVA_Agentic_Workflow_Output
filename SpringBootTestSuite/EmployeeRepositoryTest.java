package com.warehouse.management.employee.repository;

import com.warehouse.management.employee.domain.Employee;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.*;

import javax.persistence.EntityManager;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class EmployeeRepositoryTest {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private EntityManager entityManager;

    private Employee persistEmployee(String badgeId, String department, String status, boolean deleted) {
        Employee employee = Employee.builder()
                .name("Test User")
                .badgeId(badgeId)
                .role("WORKER")
                .department(department)
                .shiftGroup("A")
                .hireDate(LocalDate.of(2020, 1, 1))
                .status(status)
                .deleted(deleted)
                .build();
        entityManager.persist(employee);
        entityManager.flush();
        return employee;
    }

    @Test
    @DisplayName("findByBadgeId returns employee if exists")
    void testFindByBadgeId() {
        Employee employee = persistEmployee("BADGE100", "Logistics", "ACTIVE", false);

        Optional<Employee> found = employeeRepository.findByBadgeId("BADGE100");

        assertTrue(found.isPresent());
        assertEquals("BADGE100", found.get().getBadgeId());
    }

    @Test
    @DisplayName("findByDeletedFalse returns only non-deleted employees")
    void testFindByDeletedFalse() {
        persistEmployee("BADGE101", "Logistics", "ACTIVE", false);
        persistEmployee("BADGE102", "Logistics", "ACTIVE", true);

        Page<Employee> page = employeeRepository.findByDeletedFalse(PageRequest.of(0, 10));
        assertEquals(1, page.getTotalElements());
        assertFalse(page.getContent().get(0).getDeleted());
    }

    @Test
    @DisplayName("findByDepartmentAndDeletedFalse returns employees by department")
    void testFindByDepartmentAndDeletedFalse() {
        persistEmployee("BADGE103", "Packing", "ACTIVE", false);
        persistEmployee("BADGE104", "Packing", "INACTIVE", false);
        persistEmployee("BADGE105", "Logistics", "ACTIVE", false);

        List<Employee> packingEmployees = employeeRepository.findByDepartmentAndDeletedFalse("Packing");
        assertEquals(2, packingEmployees.size());
        assertTrue(packingEmployees.stream().allMatch(e -> "Packing".equals(e.getDepartment())));
    }

    @Test
    @DisplayName("findByStatusAndDeletedFalse returns employees by status")
    void testFindByStatusAndDeletedFalse() {
        persistEmployee("BADGE106", "Logistics", "ACTIVE", false);
        persistEmployee("BADGE107", "Logistics", "INACTIVE", false);

        List<Employee> activeEmployees = employeeRepository.findByStatusAndDeletedFalse("ACTIVE");
        assertEquals(1, activeEmployees.size());
        assertEquals("ACTIVE", activeEmployees.get(0).getStatus());
    }

    @Test
    @DisplayName("Pagination works for findByDeletedFalse")
    void testPagination() {
        for (int i = 0; i < 15; i++) {
            persistEmployee("BADGE" + (200 + i), "Logistics", "ACTIVE", false);
        }
        Page<Employee> page = employeeRepository.findByDeletedFalse(PageRequest.of(0, 10));
        assertEquals(10, page.getContent().size());
        assertEquals(15, page.getTotalElements());
    }
}