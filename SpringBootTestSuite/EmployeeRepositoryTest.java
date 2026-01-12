package com.warehouse.employee;

import com.warehouse.employee.model.Employee;
import com.warehouse.employee.repository.EmployeeRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

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
    void setUp() {
        employee1 = new Employee();
        employee1.setBadgeId("BADGE001");
        employee1.setName("Alice");
        employee1.setRole("Worker");
        employee1.setDepartment("Packing");
        employee1.setShiftGroup("Morning");
        employee1.setHireDate(LocalDate.of(2020, 1, 1));
        employee1.setStatus("ACTIVE");
        employee1.setDeleted(false);
        employeeRepository.save(employee1);

        employee2 = new Employee();
        employee2.setBadgeId("BADGE002");
        employee2.setName("Bob");
        employee2.setRole("Supervisor");
        employee2.setDepartment("Shipping");
        employee2.setShiftGroup("Night");
        employee2.setHireDate(LocalDate.of(2021, 5, 10));
        employee2.setStatus("INACTIVE");
        employee2.setDeleted(false);
        employeeRepository.save(employee2);

        employee3 = new Employee();
        employee3.setBadgeId("BADGE003");
        employee3.setName("Charlie");
        employee3.setRole("Worker");
        employee3.setDepartment("Packing");
        employee3.setShiftGroup("Morning");
        employee3.setHireDate(LocalDate.of(2022, 3, 15));
        employee3.setStatus("ACTIVE");
        employee3.setDeleted(true);
        employeeRepository.save(employee3);
    }

    @AfterEach
    void tearDown() {
        employeeRepository.deleteAll();
    }

    @Test
    void findByBadgeId_ValidBadgeId_ReturnsEmployee() {
        Optional<Employee> found = employeeRepository.findByBadgeId("BADGE001");
        assertTrue(found.isPresent());
        assertEquals("Alice", found.get().getName());
    }

    @Test
    void findByBadgeId_NonExistentBadgeId_ReturnsEmptyOptional() {
        Optional<Employee> found = employeeRepository.findByBadgeId("BADGE999");
        assertFalse(found.isPresent());
    }

    @Test
    void findByBadgeId_NullBadgeId_ThrowsException() {
        assertThrows(Exception.class, () -> employeeRepository.findByBadgeId(null));
    }

    @Test
    void findAllByDeletedFalse_Pagination_ReturnsActiveEmployeesOnly() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> page = employeeRepository.findAllByDeletedFalse(pageable);
        assertEquals(2, page.getTotalElements());
        assertTrue(page.getContent().stream().allMatch(e -> !e.getDeleted()));
    }

    @Test
    void findByDepartment_ValidDepartment_ReturnsEmployees() {
        List<Employee> packingEmployees = employeeRepository.findByDepartment("Packing");
        assertEquals(2, packingEmployees.size());
        assertTrue(packingEmployees.stream().anyMatch(e -> e.getName().equals("Alice")));
    }

    @Test
    void findByDepartment_EmptyString_ReturnsEmptyList() {
        List<Employee> result = employeeRepository.findByDepartment("");
        assertTrue(result.isEmpty());
    }

    @Test
    void findByRole_ValidRole_ReturnsEmployees() {
        List<Employee> workers = employeeRepository.findByRole("Worker");
        assertEquals(2, workers.size());
    }

    @Test
    void findByStatus_ValidStatus_ReturnsEmployees() {
        List<Employee> activeEmployees = employeeRepository.findByStatus("ACTIVE");
        assertEquals(2, activeEmployees.size());
    }

    @Test
    void findByShiftGroup_ValidShiftGroup_ReturnsEmployees() {
        List<Employee> morningShift = employeeRepository.findByShiftGroup("Morning");
        assertEquals(2, morningShift.size());
    }

    @Test
    void searchByName_PartialName_ReturnsMatchingEmployees() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> page = employeeRepository.searchByName("li", pageable);
        assertEquals(2, page.getTotalElements());
        assertTrue(page.getContent().stream().anyMatch(e -> e.getName().equals("Alice")));
    }

    @Test
    void searchByName_NonExistentName_ReturnsEmptyPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> page = employeeRepository.searchByName("ZZZ", pageable);
        assertEquals(0, page.getTotalElements());
    }

    @Test
    void countActiveByDepartment_ValidDepartment_ReturnsCount() {
        Long count = employeeRepository.countActiveByDepartment("Packing");
        assertEquals(1L, count);
    }

    @Test
    void countActiveByDepartment_NonExistentDepartment_ReturnsZero() {
        Long count = employeeRepository.countActiveByDepartment("NonExistentDept");
        assertEquals(0L, count);
    }

    @Test
    void findAllByDeletedFalse_PaginationAndSorting_ReturnsSortedEmployees() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("name").descending());
        Page<Employee> page = employeeRepository.findAllByDeletedFalse(pageable);
        assertEquals(2, page.getTotalElements());
        assertEquals("Bob", page.getContent().get(0).getName());
    }

    @Test
    void save_DuplicateBadgeId_ThrowsException() {
        Employee duplicate = new Employee();
        duplicate.setBadgeId("BADGE001");
        duplicate.setName("Duplicate");
        duplicate.setRole("Worker");
        duplicate.setDepartment("Packing");
        duplicate.setShiftGroup("Morning");
        duplicate.setHireDate(LocalDate.of(2022, 1, 1));
        duplicate.setStatus("ACTIVE");
        duplicate.setDeleted(false);
        assertThrows(Exception.class, () -> employeeRepository.saveAndFlush(duplicate));
    }

    @Test
    void findByBadgeId_EmptyString_ReturnsEmptyOptional() {
        Optional<Employee> found = employeeRepository.findByBadgeId("");
        assertFalse(found.isPresent());
    }

    @Test
    void findById_NonExistentId_ReturnsEmptyOptional() {
        Optional<Employee> found = employeeRepository.findById(999L);
        assertFalse(found.isPresent());
    }
}
