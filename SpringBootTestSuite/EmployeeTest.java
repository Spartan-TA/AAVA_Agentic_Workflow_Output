package com.wems.employee.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class EmployeeTest {
    private Employee employee;

    @BeforeEach
    void setUp() {
        employee = new Employee();
        employee.setId(1L);
        employee.setName("John Doe");
        employee.setEmail("john.doe@example.com");
        employee.setBadgeId("BADGE123");
        employee.setRole("WORKER");
        employee.setActive(true);
        employee.setActive(true);
    }

    @Test
    void gettersAndSetters_WorkCorrectly() {
        assertEquals(1L, employee.getId());
        assertEquals("John Doe", employee.getName());
        assertEquals("john.doe@example.com", employee.getEmail());
        assertEquals("BADGE123", employee.getBadgeId());
        assertEquals("WORKER", employee.getRole());
        assertTrue(employee.isActive());
    }

    @Test
    void softDelete_SetsActiveFalse() {
        employee.softDelete();
        assertFalse(employee.isActive());
    }

    @Test
    void setActiveFalse() {
        employee.setActive(false);
        assertFalse(employee.isActive());
    }

    @Test
    void setActiveTrue() {
        employee.setActive(true);
        assertTrue(employee.isActive());
    }

    @Test
    void nullFields_GettersReturnNull() {
        Employee e = new Employee();
        assertNull(e.getName());
        assertNull(e.getEmail());
        assertNull(e.getBadgeId());
        assertNull(e.getRole());
        assertTrue(e.isActive()); // default true
    }

    @Test
    void boundaryValues_MaxLength() {
        employee.setName("a".repeat(100));
        employee.setBadgeId("b".repeat(20));
        employee.setRole("c".repeat(50));
        assertEquals(100, employee.getName().length());
        assertEquals(20, employee.getBadgeId().length());
        assertEquals(50, employee.getRole().length());
    }

    @Test
    void createdAtAndUpdatedAt_Getters() {
        LocalDateTime now = LocalDateTime.now();
        // These fields are set by JPA auditing, but we can set manually for test
        employee.setActive(true);
        // No setters for createdAt/updatedAt, so test default null
        assertNull(employee.getCreatedAt());
        assertNull(employee.getUpdatedAt());
    }

    @Test
    void createdByAndUpdatedBy_Getters() {
        // No setters for createdBy/updatedBy, so test default null
        assertNull(employee.getCreatedBy());
        assertNull(employee.getUpdatedBy());
    }
}
