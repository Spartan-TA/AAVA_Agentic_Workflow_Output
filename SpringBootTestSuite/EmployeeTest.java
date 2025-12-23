package com.company.wems;

import org.junit.jupiter.api.*;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

public class EmployeeTest {
    private Employee employee;

    @BeforeEach
    public void setUp() {
        employee = Employee.builder()
                .id(1L)
                .name("John Doe")
                .badgeId("BADGE123")
                .role("Worker")
                .department("Logistics")
                .shiftGroup("A")
                .hireDate(LocalDate.of(2020, 1, 1))
                .status("Active")
                .deleted(false)
                .build();
    }

    @AfterEach
    public void tearDown() {
        employee = null;
    }

    @Test
    public void testEmployeeBuilderAndGetters() {
        assertEquals(1L, employee.getId());
        assertEquals("John Doe", employee.getName());
        assertEquals("BADGE123", employee.getBadgeId());
        assertEquals("Worker", employee.getRole());
        assertEquals("Logistics", employee.getDepartment());
        assertEquals("A", employee.getShiftGroup());
        assertEquals(LocalDate.of(2020, 1, 1), employee.getHireDate());
        assertEquals("Active", employee.getStatus());
        assertFalse(employee.isDeleted());
    }

    @Test
    public void testSetters() {
        employee.setName("Jane Smith");
        employee.setBadgeId("BADGE456");
        employee.setRole("Manager");
        employee.setDepartment("HR");
        employee.setShiftGroup("B");
        employee.setHireDate(LocalDate.of(2021, 5, 10));
        employee.setStatus("Inactive");
        employee.setDeleted(true);

        assertEquals("Jane Smith", employee.getName());
        assertEquals("BADGE456", employee.getBadgeId());
        assertEquals("Manager", employee.getRole());
        assertEquals("HR", employee.getDepartment());
        assertEquals("B", employee.getShiftGroup());
        assertEquals(LocalDate.of(2021, 5, 10), employee.getHireDate());
        assertEquals("Inactive", employee.getStatus());
        assertTrue(employee.isDeleted());
    }

    @Test
    public void testNullAndEmptyFields() {
        employee.setName(null);
        employee.setBadgeId("");
        employee.setRole(null);
        employee.setDepartment("");
        employee.setShiftGroup(null);
        employee.setStatus("");

        assertNull(employee.getName());
        assertEquals("", employee.getBadgeId());
        assertNull(employee.getRole());
        assertEquals("", employee.getDepartment());
        assertNull(employee.getShiftGroup());
        assertEquals("", employee.getStatus());
    }

    @Test
    public void testBoundaryConditions() {
        String longString = "x".repeat(255);
        employee.setName(longString);
        employee.setBadgeId(longString);
        employee.setRole(longString);
        employee.setDepartment(longString);
        employee.setShiftGroup(longString);
        employee.setStatus(longString);

        assertEquals(longString, employee.getName());
        assertEquals(longString, employee.getBadgeId());
        assertEquals(longString, employee.getRole());
        assertEquals(longString, employee.getDepartment());
        assertEquals(longString, employee.getShiftGroup());
        assertEquals(longString, employee.getStatus());
    }

    @Test
    public void testSpecialCharacters() {
        String special = "!@#$%^&*()_+-=~`'";:,.<>/?|";
        employee.setName(special);
        employee.setBadgeId(special);
        employee.setRole(special);
        employee.setDepartment(special);
        employee.setShiftGroup(special);
        employee.setStatus(special);

        assertEquals(special, employee.getName());
        assertEquals(special, employee.getBadgeId());
        assertEquals(special, employee.getRole());
        assertEquals(special, employee.getDepartment());
        assertEquals(special, employee.getShiftGroup());
        assertEquals(special, employee.getStatus());
    }

    @Test
    public void testEqualsAndHashCode() {
        Employee employee2 = Employee.builder()
                .id(1L)
                .name("John Doe")
                .badgeId("BADGE123")
                .role("Worker")
                .department("Logistics")
                .shiftGroup("A")
                .hireDate(LocalDate.of(2020, 1, 1))
                .status("Active")
                .deleted(false)
                .build();
        assertEquals(employee, employee2);
        assertEquals(employee.hashCode(), employee2.hashCode());
    }

    @Test
    public void testNotEquals() {
        Employee employee2 = Employee.builder()
                .id(2L)
                .name("Jane Smith")
                .badgeId("BADGE456")
                .role("Manager")
                .department("HR")
                .shiftGroup("B")
                .hireDate(LocalDate.of(2021, 5, 10))
                .status("Inactive")
                .deleted(true)
                .build();
        assertNotEquals(employee, employee2);
    }

    @Test
    public void testToString() {
        String str = employee.toString();
        assertTrue(str.contains("John Doe"));
        assertTrue(str.contains("BADGE123"));
    }
}
