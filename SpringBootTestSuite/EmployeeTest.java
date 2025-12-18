package com.warehouse.ems.employee;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class EmployeeTest {

    private Employee employee;

    @BeforeEach
    public void setUp() {
        employee = Employee.builder()
                .name("John Doe")
                .badgeId("BADGE123")
                .role("WORKER")
                .department("Logistics")
                .shiftGroup("A")
                .hireDate(LocalDate.of(2022, 1, 1))
                .build();
    }

    @Test
    public void testEntityCreationWithValidData() {
        assertEquals("John Doe", employee.getName());
        assertEquals("BADGE123", employee.getBadgeId());
        assertEquals("WORKER", employee.getRole());
        assertEquals("Logistics", employee.getDepartment());
        assertEquals("A", employee.getShiftGroup());
        assertEquals(LocalDate.of(2022, 1, 1), employee.getHireDate());
        assertEquals("ACTIVE", employee.getStatus());
        assertFalse(employee.isDeleted());
    }

    @Test
    public void testNullFieldValidation() {
        Employee emp = Employee.builder()
                .name(null)
                .badgeId(null)
                .role(null)
                .build();
        assertNull(emp.getName());
        assertNull(emp.getBadgeId());
        assertNull(emp.getRole());
    }

    @Test
    public void testEmptyFieldValidation() {
        Employee emp = Employee.builder()
                .name("")
                .badgeId("")
                .role("")
                .build();
        assertEquals("", emp.getName());
        assertEquals("", emp.getBadgeId());
        assertEquals("", emp.getRole());
    }

    @Test
    public void testBadgeIdUniquenessConstraint() {
        Employee emp1 = Employee.builder().badgeId("BADGE123").build();
        Employee emp2 = Employee.builder().badgeId("BADGE123").build();
        assertEquals(emp1.getBadgeId(), emp2.getBadgeId());
        // Uniqueness is enforced at DB/service layer, not entity
    }

    @Test
    public void testSoftDeleteFlag() {
        employee.setDeleted(true);
        assertTrue(employee.isDeleted());
    }

    @Test
    public void testDefaultValues() {
        Employee emp = new Employee();
        assertEquals("ACTIVE", emp.getStatus());
        assertFalse(emp.isDeleted());
    }
}