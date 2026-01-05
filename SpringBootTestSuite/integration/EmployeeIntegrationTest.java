package com.example.warehouse.test.integration;

import com.example.warehouse.entity.Employee;
import com.example.warehouse.entity.Department;
import com.example.warehouse.entity.Role;
import com.example.warehouse.repository.EmployeeRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class EmployeeIntegrationTest {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Test
    void testEmployeeCrudFlow_EndToEnd_ShouldPersistAndRetrieve() {
        Employee employee = new Employee("Eve Green", "B654", Role.SUPERVISOR, new Department("Shipping"), "E", LocalDate.now().minusDays(10), "ACTIVE");
        employeeRepository.save(employee);

        Employee found = employeeRepository.findByBadgeId("B654").orElse(null);
        assertNotNull(found);
        assertEquals("Eve Green", found.getName());

        found.setStatus("INACTIVE");
        employeeRepository.save(found);

        Employee updated = employeeRepository.findByBadgeId("B654").orElse(null);
        assertEquals("INACTIVE", updated.getStatus());

        employeeRepository.deleteByBadgeId("B654");
        assertFalse(employeeRepository.findByBadgeId("B654").isPresent());
    }
}