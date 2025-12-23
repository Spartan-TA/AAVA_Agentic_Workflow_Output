package com.company.wems;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
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
    private Employee employee3;

    @BeforeEach
    public void setUp() {
        employee1 = Employee.builder()
                .name("Alice")
                .badgeId("BADGE001")
                .role("Worker")
                .department("Logistics")
                .shiftGroup("A")
                .hireDate(LocalDate.of(2020, 1, 1))
                .status("Active")
                .deleted(false)
                .build();
        employee2 = Employee.builder()
                .name("Bob")
                .badgeId("BADGE002")
                .role("Manager")
                .department("HR")
                .shiftGroup("B")
                .hireDate(LocalDate.of(2021, 5, 10))
                .status("Inactive")
                .deleted(false)
                .build();
        employee3 = Employee.builder()
                .name("Charlie")
                .badgeId("BADGE003")
                .role("Worker")
                .department("Logistics")
                .shiftGroup("A")
                .hireDate(LocalDate.of(2022, 3, 15))
                .status("Active")
                .deleted(true)
                .build();
        employeeRepository.save(employee1);
        employeeRepository.save(employee2);
        employeeRepository.save(employee3);
    }

    @AfterEach
    public void tearDown() {
        employeeRepository.deleteAll();
    }

    @Test
    public void testFindByRole() {
        List<Employee> workers = employeeRepository.findByRole("Worker");
        assertEquals(2, workers.size());
        assertTrue(workers.stream().anyMatch(e -> e.getName().equals("Alice")));
        assertTrue(workers.stream().anyMatch(e -> e.getName().equals("Charlie")));
    }

    @Test
    public void testFindByDepartment() {
        List<Employee> logistics = employeeRepository.findByDepartment("Logistics");
        assertEquals(2, logistics.size());
    }

    @Test
    public void testFindByDeletedFalse() {
        List<Employee> notDeleted = employeeRepository.findByDeletedFalse();
        assertEquals(2, notDeleted.size());
        assertTrue(notDeleted.stream().allMatch(e -> !e.isDeleted()));
    }

    @Test
    public void testFindByBadgeId() {
        Optional<Employee> found = employeeRepository.findByBadgeId("BADGE001");
        assertTrue(found.isPresent());
        assertEquals("Alice", found.get().getName());
    }

    @Test
    public void testFindByBadgeId_NotFound() {
        Optional<Employee> found = employeeRepository.findByBadgeId("BADGE999");
        assertFalse(found.isPresent());
    }

    @Test
    public void testUniqueBadgeIdConstraint() {
        Employee duplicate = Employee.builder()
                .name("Duplicate")
                .badgeId("BADGE001")
                .role("Worker")
                .department("Logistics")
                .shiftGroup("A")
                .hireDate(LocalDate.of(2023, 1, 1))
                .status("Active")
                .deleted(false)
                .build();
        assertThrows(DataIntegrityViolationException.class, () -> {
            employeeRepository.saveAndFlush(duplicate);
        });
    }

    @Test
    public void testPagination() {
        Pageable pageable = PageRequest.of(0, 2);
        Page<Employee> page = employeeRepository.findAll(pageable);
        assertEquals(2, page.getContent().size());
        assertEquals(3, page.getTotalElements());
    }

    @Test
    public void testSaveNullFields() {
        Employee emp = Employee.builder()
                .name(null)
                .badgeId("BADGE004")
                .role("")
                .department(null)
                .shiftGroup("")
                .hireDate(null)
                .status("")
                .deleted(false)
                .build();
        Employee saved = employeeRepository.save(emp);
        assertNotNull(saved.getId());
        assertNull(saved.getName());
        assertEquals("", saved.getRole());
    }

    @Test
    public void testBoundaryBadgeIdLength() {
        String longBadgeId = "B".repeat(255);
        Employee emp = Employee.builder()
                .name("LongBadge")
                .badgeId(longBadgeId)
                .role("Worker")
                .department("Logistics")
                .shiftGroup("A")
                .hireDate(LocalDate.now())
                .status("Active")
                .deleted(false)
                .build();
        Employee saved = employeeRepository.save(emp);
        assertEquals(longBadgeId, saved.getBadgeId());
    }
}
