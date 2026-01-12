package com.warehouse.employee;

import com.warehouse.employee.model.AssetAssignment;
import com.warehouse.employee.model.Asset;
import com.warehouse.employee.model.Employee;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class AssetAssignmentTest {
    @Test
    void testConstructorAndGetters() {
        Asset asset = new Asset();
        Employee emp = new Employee();
        LocalDateTime assignedAt = LocalDateTime.of(2024, 6, 1, 10, 0);
        LocalDateTime returnedAt = LocalDateTime.of(2024, 6, 2, 12, 0);
        AssetAssignment assignment = new AssetAssignment(1L, asset, emp, assignedAt, returnedAt, "GOOD");
        assertEquals(1L, assignment.getId());
        assertEquals(asset, assignment.getAsset());
        assertEquals(emp, assignment.getEmployee());
        assertEquals(assignedAt, assignment.getAssignedAt());
        assertEquals(returnedAt, assignment.getReturnedAt());
        assertEquals("GOOD", assignment.getConditionOnReturn());
    }

    @Test
    void testDefaultValues() {
        AssetAssignment assignment = new AssetAssignment();
        assertNull(assignment.getId());
        assertNull(assignment.getAsset());
        assertNull(assignment.getEmployee());
        assertNotNull(assignment.getAssignedAt());
        assertNull(assignment.getReturnedAt());
        assertNull(assignment.getConditionOnReturn());
        assertTrue(assignment.isCheckedOut());
    }

    @Test
    void testSetters() {
        AssetAssignment assignment = new AssetAssignment();
        Asset asset = new Asset();
        Employee emp = new Employee();
        LocalDateTime assignedAt = LocalDateTime.of(2024, 7, 1, 10, 0);
        LocalDateTime returnedAt = LocalDateTime.of(2024, 7, 2, 12, 0);
        assignment.setId(2L);
        assignment.setAsset(asset);
        assignment.setEmployee(emp);
        assignment.setAssignedAt(assignedAt);
        assignment.setReturnedAt(returnedAt);
        assignment.setConditionOnReturn("DAMAGED");
        assertEquals(2L, assignment.getId());
        assertEquals(asset, assignment.getAsset());
        assertEquals(emp, assignment.getEmployee());
        assertEquals(assignedAt, assignment.getAssignedAt());
        assertEquals(returnedAt, assignment.getReturnedAt());
        assertEquals("DAMAGED", assignment.getConditionOnReturn());
        assertFalse(assignment.isCheckedOut());
    }

    @Test
    void testIsCheckedOutTrueWhenReturnedAtNull() {
        AssetAssignment assignment = new AssetAssignment();
        assignment.setReturnedAt(null);
        assertTrue(assignment.isCheckedOut());
    }

    @Test
    void testEdgeCases() {
        AssetAssignment assignment = new AssetAssignment();
        assignment.setConditionOnReturn("");
        assignment.setReturnedAt(null);
        assertEquals("", assignment.getConditionOnReturn());
        assertTrue(assignment.isCheckedOut());
    }
}
