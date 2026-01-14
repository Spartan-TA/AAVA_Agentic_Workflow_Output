package com.warehouse.domain;

import org.junit.jupiter.api.*;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class EmployeeTest {
    private Employee employee;

    @BeforeEach
    void setUp() {
        employee = new Employee();
    }

    @AfterEach
    void tearDown() {
        employee = null;
    }

    @Test
    void testIdGetterSetter() {
        employee.setId(123L);
        assertEquals(123L, employee.getId());
    }

    @Test
    void testBadgeIdGetterSetter() {
        employee.setBadgeId("B123");
        assertEquals("B123", employee.getBadgeId());
    }

    @Test
    void testNameGetterSetter() {
        employee.setName("John Doe");
        assertEquals("John Doe", employee.getName());
    }

    @Test
    void testRoleGetterSetter() {
        employee.setRole("Manager");
        assertEquals("Manager", employee.getRole());
    }

    @Test
    void testDepartmentGetterSetter() {
        employee.setDepartment("Logistics");
        assertEquals("Logistics", employee.getDepartment());
    }

    @Test
    void testShiftGroupGetterSetter() {
        ShiftGroup group = new ShiftGroup();
        group.setId(1L);
        employee.setShiftGroup(group);
        assertEquals(1L, employee.getShiftGroup().getId());
    }

    @Test
    void testHireDateGetterSetter() {
        LocalDate date = LocalDate.of(2020, 1, 1);
        employee.setHireDate(date);
        assertEquals(date, employee.getHireDate());
    }

    @Test
    void testStatusGetterSetter() {
        employee.setStatus("Active");
        assertEquals("Active", employee.getStatus());
    }

    @Test
    void testDeletedGetterSetter() {
        assertFalse(employee.isDeleted());
        employee.setDeleted(true);
        assertTrue(employee.isDeleted());
    }

    @Test
    void testNullValues() {
        employee.setBadgeId(null);
        employee.setName(null);
        employee.setRole(null);
        employee.setDepartment(null);
        employee.setHireDate(null);
        employee.setStatus(null);
        employee.setShiftGroup(null);
        assertNull(employee.getBadgeId());
        assertNull(employee.getName());
        assertNull(employee.getRole());
        assertNull(employee.getDepartment());
        assertNull(employee.getHireDate());
        assertNull(employee.getStatus());
        assertNull(employee.getShiftGroup());
    }

    @Test
    void testEdgeCaseEmptyStrings() {
        employee.setBadgeId("");
        employee.setName("");
        employee.setRole("");
        employee.setDepartment("");
        employee.setStatus("");
        assertEquals("", employee.getBadgeId());
        assertEquals("", employee.getName());
        assertEquals("", employee.getRole());
        assertEquals("", employee.getDepartment());
        assertEquals("", employee.getStatus());
    }
}
