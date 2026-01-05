package com.example.warehouse.test.entity;

import com.example.warehouse.entity.Employee;
import com.example.warehouse.entity.Department;
import com.example.warehouse.entity.Role;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class EmployeeEntityTest {

    @Test
    void testEmployee_ValidFields_ShouldCreateSuccessfully() {
        Employee employee = new Employee("John Doe", "B123", Role.WORKER, new Department("Logistics"), "A", LocalDate.now(), "ACTIVE");
        assertEquals("John Doe", employee.getName());
        assertEquals("B123", employee.getBadgeId());
        assertEquals(Role.WORKER, employee.getRole());
        assertEquals("Logistics", employee.getDepartment().getName());
        assertEquals("A", employee.getShiftGroup());
        assertEquals("ACTIVE", employee.getStatus());
    }

    @Test
    void testEmployee_NullName_ShouldThrowException() {
        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            new Employee(null, "B123", Role.WORKER, new Department("Logistics"), "A", LocalDate.now(), "ACTIVE");
        });
        assertTrue(ex.getMessage().contains("name"));
    }

    @Test
    void testEmployee_EmptyBadgeId_ShouldThrowException() {
        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            new Employee("John Doe", "", Role.WORKER, new Department("Logistics"), "A", LocalDate.now(), "ACTIVE");
        });
        assertTrue(ex.getMessage().contains("badgeId"));
    }

    @Test
    void testEmployee_InvalidRole_ShouldThrowException() {
        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            new Employee("John Doe", "B123", null, new Department("Logistics"), "A", LocalDate.now(), "ACTIVE");
        });
        assertTrue(ex.getMessage().contains("role"));
    }

    @Test
    void testEmployee_HireDateInFuture_ShouldThrowException() {
        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            new Employee("John Doe", "B123", Role.WORKER, new Department("Logistics"), "A", LocalDate.now().plusDays(1), "ACTIVE");
        });
        assertTrue(ex.getMessage().contains("hireDate"));
    }
}