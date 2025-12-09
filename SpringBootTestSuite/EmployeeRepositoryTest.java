// EmployeeRepositoryTest.java
package com.warehouse.ems.employee.repository;

import com.warehouse.ems.employee.entity.Employee;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import java.time.LocalDate;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class EmployeeRepositoryTest {
    @Autowired
    private EmployeeRepository employeeRepository;

    private Employee employee;

    @BeforeEach
    void setUp() {
        employee = Employee.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phone("1234567890")
                .hireDate(LocalDate.parse("2023-01-01"))
                .department("IT")
                .position("Developer")
                .salary(50000.0)
                .active(true)
                .build();
        employeeRepository.save(employee);
    }

    @AfterEach
    void tearDown() {
        employeeRepository.deleteAll();
    }

    @Test
    void testFindByEmail_ExistingEmail() {
        Optional<Employee> found = employeeRepository.findByEmail("john.doe@example.com");
        assertTrue(found.isPresent());
        assertEquals("John", found.get().getFirstName());
    }

    @Test
    void testFindByEmail_NonExistingEmail() {
        Optional<Employee> found = employeeRepository.findByEmail("nonexistent@example.com");
        assertFalse(found.isPresent());
    }

    @Test
    void testExistsByEmail_ExistingEmail() {
        boolean exists = employeeRepository.existsByEmail("john.doe@example.com");
        assertTrue(exists);
    }

    @Test
    void testExistsByEmail_NonExistingEmail() {
        boolean exists = employeeRepository.existsByEmail("nonexistent@example.com");
        assertFalse(exists);
    }

    @Test
    void testSaveEmployee() {
        Employee newEmployee = Employee.builder()
                .firstName("Jane")
                .lastName("Smith")
                .email("jane.smith@example.com")
                .phone("0987654321")
                .hireDate(LocalDate.parse("2023-02-01"))
                .department("HR")
                .position("Manager")
                .salary(60000.0)
                .active(true)
                .build();
        Employee saved = employeeRepository.save(newEmployee);
        assertNotNull(saved.getId());
        assertEquals("Jane", saved.getFirstName());
    }

    @Test
    void testDeleteEmployee() {
        employeeRepository.delete(employee);
        Optional<Employee> found = employeeRepository.findByEmail("john.doe@example.com");
        assertFalse(found.isPresent());
    }
}