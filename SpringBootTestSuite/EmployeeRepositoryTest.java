package com.example.warehouse_employee_mgmt_epics;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class EmployeeRepositoryTest {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Employee activeEmployee;
    private Employee inactiveEmployee;

    @BeforeEach
    public void setUp() {
        activeEmployee = Employee.builder()
                .badgeId("A123")
                .email("active@example.com")
                .firstName("Active")
                .lastName("Employee")
                .department("Logistics")
                .role("WORKER")
                .status("ACTIVE")
                .isActive(true)
                .build();

        inactiveEmployee = Employee.builder()
                .badgeId("B456")
                .email("inactive@example.com")
                .firstName("Inactive")
                .lastName("Employee")
                .department("HR")
                .role("SUPERVISOR")
                .status("INACTIVE")
                .isActive(false)
                .build();

        entityManager.persist(activeEmployee);
        entityManager.persist(inactiveEmployee);
        entityManager.flush();
    }

    @Test
    public void testFindByBadgeId_Valid() {
        Employee found = employeeRepository.findByBadgeId("A123");
        assertNotNull(found);
        assertEquals("Active", found.getFirstName());
    }

    @Test
    public void testFindByBadgeId_Invalid() {
        Employee found = employeeRepository.findByBadgeId("ZZZZ");
        assertNull(found, "Should return null for non-existent badgeId");
    }

    @Test
    public void testFindByDepartment_Valid() {
        List<Employee> found = employeeRepository.findByDepartment("Logistics");
        assertFalse(found.isEmpty());
        assertEquals("Active", found.get(0).getFirstName());
    }

    @Test
    public void testFindByDepartment_Empty() {
        List<Employee> found = employeeRepository.findByDepartment("NonExistentDept");
        assertTrue(found.isEmpty());
    }

    @Test
    public void testFindByRole() {
        List<Employee> workers = employeeRepository.findByRole("WORKER");
        assertFalse(workers.isEmpty());
        assertEquals("Active", workers.get(0).getFirstName());

        List<Employee> supervisors = employeeRepository.findByRole("SUPERVISOR");
        assertFalse(supervisors.isEmpty());
        assertEquals("Inactive", supervisors.get(0).getFirstName());
    }

    @Test
    public void testFindByStatus() {
        List<Employee> active = employeeRepository.findByStatus("ACTIVE");
        assertFalse(active.isEmpty());
        assertEquals("Active", active.get(0).getFirstName());

        List<Employee> inactive = employeeRepository.findByStatus("INACTIVE");
        assertFalse(inactive.isEmpty());
        assertEquals("Inactive", inactive.get(0).getFirstName());
    }

    @Test
    public void testFindByIsActiveTrue() {
        List<Employee> active = employeeRepository.findByIsActiveTrue();
        assertEquals(1, active.size());
        assertEquals("Active", active.get(0).getFirstName());
    }

    @Test
    public void testPagination() {
        Page<Employee> page = employeeRepository.findAll(PageRequest.of(0, 1));
        assertEquals(1, page.getContent().size());
    }

    @Test
    public void testCustomQueryMethods() {
        // Assuming a custom query exists, e.g., findByEmailContaining
        List<Employee> found = employeeRepository.findByEmailContaining("example.com");
        assertEquals(2, found.size());
    }
}