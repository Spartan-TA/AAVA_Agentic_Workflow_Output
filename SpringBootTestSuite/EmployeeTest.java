package com.example.ems.employee;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDate;

public class EmployeeTest {

    @Test
    public void testDefaultConstructor() {
        Employee employee = new Employee();
        assertNull(employee.getId());
        assertNull(employee.getFirstName());
        assertNull(employee.getLastName());
        assertNull(employee.getEmail());
        assertNull(employee.getRole());
        assertNull(employee.getHireDate());
    }

    @Test
    public void testParameterizedConstructorAndGetters() {
        String firstName = "John";
        String lastName = "Doe";
        String email = "john.doe@example.com";
        String role = "Developer";
        LocalDate hireDate = LocalDate.of(2020, 1, 1);

        Employee employee = new Employee(firstName, lastName, email, role, hireDate);

        assertNull(employee.getId());
        assertEquals(firstName, employee.getFirstName());
        assertEquals(lastName, employee.getLastName());
        assertEquals(email, employee.getEmail());
        assertEquals(role, employee.getRole());
        assertEquals(hireDate, employee.getHireDate());
    }

    @Test
    public void testSetters() {
        Employee employee = new Employee();
        employee.setId(2L);
        employee.setFirstName("Jane");
        employee.setLastName("Smith");
        employee.setEmail("jane.smith@example.com");
        employee.setRole("Manager");
        employee.setHireDate(LocalDate.of(2021, 5, 15));

        assertEquals(2L, employee.getId());
        assertEquals("Jane", employee.getFirstName());
        assertEquals("Smith", employee.getLastName());
        assertEquals("jane.smith@example.com", employee.getEmail());
        assertEquals("Manager", employee.getRole());
        assertEquals(LocalDate.of(2021, 5, 15), employee.getHireDate());
    }

    @Test
    public void testNullAndEmptyFields() {
        Employee employee = new Employee();
        employee.setFirstName("");
        employee.setLastName(null);
        employee.setEmail("");
        employee.setRole(null);

        assertEquals("", employee.getFirstName());
        assertNull(employee.getLastName());
        assertEquals("", employee.getEmail());
        assertNull(employee.getRole());
    }

    @Test
    public void testBoundaryDateValues() {
        Employee employee = new Employee();
        LocalDate minDate = LocalDate.MIN;
        LocalDate maxDate = LocalDate.MAX;
        
        employee.setHireDate(minDate);
        assertEquals(minDate, employee.getHireDate());
        
        employee.setHireDate(maxDate);
        assertEquals(maxDate, employee.getHireDate());
    }

    @Test
    public void testNullHireDate() {
        Employee employee = new Employee();
        employee.setHireDate(null);
        assertNull(employee.getHireDate());
    }
}