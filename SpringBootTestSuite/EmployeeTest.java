package com.warehouse.ems.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

public class EmployeeTest {
    private Employee employee;

    @BeforeEach
    void setUp() {
        employee = new Employee("B123", "John Doe", "WORKER");
    }

    @Test
    void testConstructorAndGetters() {
        assertEquals("B123", employee.getBadgeId());
        assertEquals("John Doe", employee.getName());
        assertEquals("WORKER", employee.getRole());
        assertEquals("ACTIVE", employee.getStatus());
        assertFalse(employee.isDeleted());
    }

    @Test
    void testSettersAndGetters() {
        employee.setId(10L);
        employee.setDepartment("Logistics");
        employee.setShiftGroup("A");
        LocalDate hireDate = LocalDate.of(2020, 1, 1);
        employee.setHireDate(hireDate);
        employee.setStatus("ON_LEAVE");
        employee.setDeleted(true);

        assertEquals(10L, employee.getId());
        assertEquals("Logistics", employee.getDepartment());
        assertEquals("A", employee.getShiftGroup());
        assertEquals(hireDate, employee.getHireDate());
        assertEquals("ON_LEAVE", employee.getStatus());
        assertTrue(employee.isDeleted());
    }

    @Test
    void testNullAndEmptyValues() {
        employee.setBadgeId("");
        employee.setName(null);
        employee.setRole("");
        employee.setDepartment(null);
        employee.setShiftGroup("");
        employee.setHireDate(null);
        employee.setStatus("");

        assertEquals("", employee.getBadgeId());
        assertNull(employee.getName());
        assertEquals("", employee.getRole());
        assertNull(employee.getDepartment());
        assertEquals("", employee.getShiftGroup());
        assertNull(employee.getHireDate());
        assertEquals("", employee.getStatus());
    }

    @Test
    void testDefaultValues() {
        Employee emp = new Employee();
        assertNull(emp.getId());
        assertNull(emp.getBadgeId());
        assertNull(emp.getName());
        assertNull(emp.getRole());
        assertNull(emp.getDepartment());
        assertNull(emp.getShiftGroup());
        assertNull(emp.getHireDate());
        assertNull(emp.getStatus());
        assertFalse(emp.isDeleted());
    }
}
