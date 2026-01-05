package com.example.warehouse.test.repository;

import com.example.warehouse.entity.Employee;
import com.example.warehouse.entity.Department;
import com.example.warehouse.entity.Role;
import com.example.warehouse.repository.EmployeeRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@DataJpaTest
class EmployeeRepositoryTest {

    @Autowired
    private EmployeeRepository employeeRepository;

    private Employee employee;

    @BeforeEach
    void setUp() {
        employee = new Employee("Jane Smith", "B456", Role.HR, new Department("HR"), "B", LocalDate.now().minusYears(1), "ACTIVE");
        employeeRepository.save(employee);
    }

    @AfterEach
    void tearDown() {
        employeeRepository.deleteAll();
    }

    @Test
    void testFindByBadgeId_ExistingBadgeId_ShouldReturnEmployee() {
        Optional<Employee> found = employeeRepository.findByBadgeId("B456");
        assertTrue(found.isPresent());
        assertEquals("Jane Smith", found.get().getName());
    }

    @Test
    void testFindByBadgeId_NonExistingBadgeId_ShouldReturnEmpty() {
        Optional<Employee> found = employeeRepository.findByBadgeId("X999");
        assertFalse(found.isPresent());
    }

    @Test
    void testFindByDepartment_Pagination_ShouldReturnCorrectPage() {
        List<Employee> employees = employeeRepository.findByDepartmentName("HR", org.springframework.data.domain.PageRequest.of(0, 10));
        assertFalse(employees.isEmpty());
        assertEquals("Jane Smith", employees.get(0).getName());
    }

    @Test
    void testDeleteByBadgeId_ShouldRemoveEmployee() {
        employeeRepository.deleteByBadgeId("B456");
        assertFalse(employeeRepository.findByBadgeId("B456").isPresent());
    }
}