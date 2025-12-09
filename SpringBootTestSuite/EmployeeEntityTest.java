// EmployeeEntityTest.java
package com.warehouse.ems.employee.entity;

import org.junit.jupiter.api.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class EmployeeEntityTest {
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
    }

    @AfterEach
    void tearDown() {
        // No resources to clean up
    }

    @Test
    void testEmployeeCreation() {
        assertNotNull(employee);
        assertEquals("John", employee.getFirstName());
        assertEquals("Doe", employee.getLastName());
        assertEquals("john.doe@example.com", employee.getEmail());
    }

    @Test
    void testOnCreate() {
        employee.onCreate();
        assertNotNull(employee.getCreatedAt());
        assertNotNull(employee.getUpdatedAt());
    }

    @Test
    void testOnUpdate() {
        LocalDateTime before = employee.getUpdatedAt();
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        employee.onUpdate();
        assertNotEquals(before, employee.getUpdatedAt());
    }

    @Test
    void testEmployeeBuilder() {
        Employee built = Employee.builder()
                .firstName("Jane")
                .lastName("Smith")
                .email("jane.smith@example.com")
                .build();
        assertEquals("Jane", built.getFirstName());
        assertEquals("Smith", built.getLastName());
    }

    @Test
    void testEmployeeSettersAndGetters() {
        employee.setFirstName("Jane");
        assertEquals("Jane", employee.getFirstName());
        employee.setLastName("Smith");
        assertEquals("Smith", employee.getLastName());
        employee.setEmail("jane.smith@example.com");
        assertEquals("jane.smith@example.com", employee.getEmail());
    }
}