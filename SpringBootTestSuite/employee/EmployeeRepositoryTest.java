package com.warehouse.ems.employee;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.*;
import java.time.*;
import java.util.*;

@DataJpaTest
class EmployeeRepositoryTest {

    @Autowired
    private EmployeeRepository employeeRepository;

    @BeforeEach
    void setup() {
        employeeRepository.deleteAll();
        employeeRepository.save(new EmployeeEntity(null, "B123", "Alice", "alice@wh.com", "Logistics", "WORKER", LocalDate.now(), "ACTIVE", false, LocalDateTime.now(), LocalDateTime.now()));
        employeeRepository.save(new EmployeeEntity(null, "B124", "Bob", "bob@wh.com", "HR", "HR", LocalDate.now(), "ACTIVE", false, LocalDateTime.now(), LocalDateTime.now()));
    }

    @Test
    void testFindByBadgeId_Found_ReturnsEmployee() {
        Optional<EmployeeEntity> result = employeeRepository.findByBadgeId("B123");
        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals("Alice", result.get().getName());
    }

    @Test
    void testFindByBadgeId_NotFound_ReturnsEmpty() {
        Optional<EmployeeEntity> result = employeeRepository.findByBadgeId("B999");
        Assertions.assertFalse(result.isPresent());
    }

    @Test
    void testFindAllByDepartment_WithPagination_ReturnsCorrectEmployees() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<EmployeeEntity> page = employeeRepository.findAllByDepartment("HR", pageable);
        Assertions.assertEquals(1, page.getTotalElements());
        Assertions.assertEquals("Bob", page.getContent().get(0).getName());
    }

    @Test
    void testFindAllByDeletedFalse_ExcludesDeleted() {
        EmployeeEntity deleted = new EmployeeEntity(null, "B125", "Eve", "eve@wh.com", "IT", "WORKER", LocalDate.now(), "ACTIVE", true, LocalDateTime.now(), LocalDateTime.now());
        employeeRepository.save(deleted);
        List<EmployeeEntity> result = employeeRepository.findAllByDeletedFalse();
        Assertions.assertTrue(result.stream().noneMatch(e -> e.getBadgeId().equals("B125")));
    }

    @Test
    void testFindByBadgeId_WithNullInput_ThrowsException() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> employeeRepository.findByBadgeId(null));
    }

    @Test
    void testFindAllByDepartment_WithEmptyDepartment_ReturnsEmpty() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<EmployeeEntity> page = employeeRepository.findAllByDepartment("", pageable);
        Assertions.assertTrue(page.isEmpty());
    }

    @Test
    void testFindAllByDepartment_WithNonExistentDepartment_ReturnsEmpty() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<EmployeeEntity> page = employeeRepository.findAllByDepartment("NonExistent", pageable);
        Assertions.assertTrue(page.isEmpty());
    }

    @Test
    void testSave_WithValidEmployee_Success() {
        EmployeeEntity newEmployee = new EmployeeEntity(null, "B126", "Charlie", "charlie@wh.com", "IT", "ADMIN", LocalDate.now(), "ACTIVE", false, LocalDateTime.now(), LocalDateTime.now());
        EmployeeEntity saved = employeeRepository.save(newEmployee);
        Assertions.assertNotNull(saved.getId());
        Assertions.assertEquals("Charlie", saved.getName());
    }

    @Test
    void testSave_WithDuplicateBadgeId_ThrowsException() {
        EmployeeEntity duplicate = new EmployeeEntity(null, "B123", "Duplicate", "dup@wh.com", "IT", "WORKER", LocalDate.now(), "ACTIVE", false, LocalDateTime.now(), LocalDateTime.now());
        Assertions.assertThrows(Exception.class, () -> {
            employeeRepository.save(duplicate);
            employeeRepository.flush();
        });
    }

    @Test
    void testDelete_SoftDelete_SetsDeletedFlag() {
        EmployeeEntity employee = employeeRepository.findByBadgeId("B123").orElseThrow();
        employee.setDeleted(true);
        employeeRepository.save(employee);
        List<EmployeeEntity> activeEmployees = employeeRepository.findAllByDeletedFalse();
        Assertions.assertTrue(activeEmployees.stream().noneMatch(e -> e.getBadgeId().equals("B123")));
    }

    @Test
    void testFindAll_WithPagination_ReturnsCorrectPage() {
        Pageable pageable = PageRequest.of(0, 1);
        Page<EmployeeEntity> page = employeeRepository.findAll(pageable);
        Assertions.assertEquals(1, page.getContent().size());
        Assertions.assertEquals(2, page.getTotalElements());
    }

    @Test
    void testFindById_ExistingEmployee_ReturnsEmployee() {
        EmployeeEntity employee = employeeRepository.findByBadgeId("B123").orElseThrow();
        Optional<EmployeeEntity> found = employeeRepository.findById(employee.getId());
        Assertions.assertTrue(found.isPresent());
        Assertions.assertEquals("Alice", found.get().getName());
    }

    @Test
    void testFindById_NonExistentEmployee_ReturnsEmpty() {
        Optional<EmployeeEntity> found = employeeRepository.findById(999L);
        Assertions.assertFalse(found.isPresent());
    }

    @Test
    void testCount_ReturnsCorrectCount() {
        long count = employeeRepository.count();
        Assertions.assertEquals(2, count);
    }

    @Test
    void testDeleteAll_RemovesAllEmployees() {
        employeeRepository.deleteAll();
        long count = employeeRepository.count();
        Assertions.assertEquals(0, count);
    }