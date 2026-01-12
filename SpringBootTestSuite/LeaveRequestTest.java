package com.warehouse.employee;

import com.warehouse.employee.model.LeaveRequest;
import com.warehouse.employee.model.Employee;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class LeaveRequestTest {
    @Test
    void testConstructorAndGetters() {
        Employee emp = new Employee();
        LocalDate start = LocalDate.of(2024, 6, 1);
        LocalDate end = LocalDate.of(2024, 6, 5);
        LocalDateTime now = LocalDateTime.now();
        LeaveRequest request = new LeaveRequest(1L, emp, "PTO", start, end, "APPROVED", "Manager", "Approved for vacation", now, now);
        assertEquals(1L, request.getId());
        assertEquals(emp, request.getEmployee());
        assertEquals("PTO", request.getLeaveType());
        assertEquals(start, request.getStartDate());
        assertEquals(end, request.getEndDate());
        assertEquals("APPROVED", request.getStatus());
        assertEquals("Manager", request.getApprover());
        assertEquals("Approved for vacation", request.getComments());
        assertEquals(now, request.getRequestedAt());
        assertEquals(now, request.getApprovedAt());
    }

    @Test
    void testDefaultValues() {
        LeaveRequest request = new LeaveRequest();
        assertNull(request.getId());
        assertNull(request.getEmployee());
        assertNull(request.getLeaveType());
        assertNull(request.getStartDate());
        assertNull(request.getEndDate());
        assertEquals("REQUESTED", request.getStatus());
        assertNull(request.getApprover());
        assertNull(request.getComments());
        assertNotNull(request.getRequestedAt());
        assertNull(request.getApprovedAt());
    }

    @Test
    void testSetters() {
        LeaveRequest request = new LeaveRequest();
        Employee emp = new Employee();
        LocalDate start = LocalDate.of(2024, 7, 1);
        LocalDate end = LocalDate.of(2024, 7, 3);
        LocalDateTime requested = LocalDateTime.of(2024, 7, 1, 8, 0);
        LocalDateTime approved = LocalDateTime.of(2024, 7, 2, 8, 0);
        request.setId(2L);
        request.setEmployee(emp);
        request.setLeaveType("SICK");
        request.setStartDate(start);
        request.setEndDate(end);
        request.setStatus("REJECTED");
        request.setApprover("HR");
        request.setComments("Insufficient balance");
        request.setRequestedAt(requested);
        request.setApprovedAt(approved);
        assertEquals(2L, request.getId());
        assertEquals(emp, request.getEmployee());
        assertEquals("SICK", request.getLeaveType());
        assertEquals(start, request.getStartDate());
        assertEquals(end, request.getEndDate());
        assertEquals("REJECTED", request.getStatus());
        assertEquals("HR", request.getApprover());
        assertEquals("Insufficient balance", request.getComments());
        assertEquals(requested, request.getRequestedAt());
        assertEquals(approved, request.getApprovedAt());
    }

    @Test
    void testEdgeCases() {
        LeaveRequest request = new LeaveRequest();
        request.setStatus("");
        request.setLeaveType(null);
        request.setApprover("");
        request.setComments("");
        assertEquals("", request.getStatus());
        assertNull(request.getLeaveType());
        assertEquals("", request.getApprover());
        assertEquals("", request.getComments());
    }
}
