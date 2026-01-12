package com.warehouse.employee;

import com.warehouse.employee.model.ShiftAssignment;
import com.warehouse.employee.model.Employee;
import com.warehouse.employee.model.ShiftTemplate;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class ShiftAssignmentTest {
    @Test
    void testConstructorAndGetters() {
        Employee emp = new Employee();
        ShiftTemplate template = new ShiftTemplate();
        LocalDate date = LocalDate.of(2024, 6, 1);
        LocalDateTime now = LocalDateTime.now();
        ShiftAssignment assignment = new ShiftAssignment(1L, emp, template, date, true, "COMPLETED", now, now);
        assertEquals(1L, assignment.getId());
        assertEquals(emp, assignment.getEmployee());
        assertEquals(template, assignment.getShiftTemplate());
        assertEquals(date, assignment.getShiftDate());
        assertTrue(assignment.getIsOvertime());
        assertEquals("COMPLETED", assignment.getStatus());
        assertEquals(now, assignment.getCreatedAt());
        assertEquals(now, assignment.getUpdatedAt());
    }

    @Test
    void testDefaultValues() {
        ShiftAssignment assignment = new ShiftAssignment();
        assertNull(assignment.getId());
        assertNull(assignment.getEmployee());
        assertNull(assignment.getShiftTemplate());
        assertNull(assignment.getShiftDate());
        assertFalse(assignment.getIsOvertime());
        assertEquals("ASSIGNED", assignment.getStatus());
        assertNotNull(assignment.getCreatedAt());
        assertNotNull(assignment.getUpdatedAt());
    }

    @Test
    void testSetters() {
        ShiftAssignment assignment = new ShiftAssignment();
        Employee emp = new Employee();
        ShiftTemplate template = new ShiftTemplate();
        LocalDate date = LocalDate.of(2024, 7, 1);
        LocalDateTime created = LocalDateTime.of(2024, 7, 1, 8, 0);
        LocalDateTime updated = LocalDateTime.of(2024, 7, 2, 8, 0);
        assignment.setId(2L);
        assignment.setEmployee(emp);
        assignment.setShiftTemplate(template);
        assignment.setShiftDate(date);
        assignment.setIsOvertime(true);
        assignment.setStatus("CANCELLED");
        assignment.setCreatedAt(created);
        assignment.setUpdatedAt(updated);
        assertEquals(2L, assignment.getId());
        assertEquals(emp, assignment.getEmployee());
        assertEquals(template, assignment.getShiftTemplate());
        assertEquals(date, assignment.getShiftDate());
        assertTrue(assignment.getIsOvertime());
        assertEquals("CANCELLED", assignment.getStatus());
        assertEquals(created, assignment.getCreatedAt());
        assertEquals(updated, assignment.getUpdatedAt());
    }

    @Test
    void testOnUpdateUpdatesUpdatedAt() {
        ShiftAssignment assignment = new ShiftAssignment();
        LocalDateTime before = assignment.getUpdatedAt();
        try { Thread.sleep(10); } catch (InterruptedException ignored) {}
        assignment.onUpdate();
        LocalDateTime after = assignment.getUpdatedAt();
        assertTrue(after.isAfter(before));
    }

    @Test
    void testEdgeCases() {
        ShiftAssignment assignment = new ShiftAssignment();
        assignment.setStatus("");
        assignment.setIsOvertime(null);
        assertEquals("", assignment.getStatus());
        assertNull(assignment.getIsOvertime());
    }
}
