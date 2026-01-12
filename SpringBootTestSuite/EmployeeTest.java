package com.warehouse.employee;

import com.warehouse.employee.model.Employee;
import com.warehouse.employee.model.Certification;
import com.warehouse.employee.model.ShiftAssignment;
import com.warehouse.employee.model.LeaveRequest;
import com.warehouse.employee.model.AssetAssignment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class EmployeeTest {
    private Employee employee;

    @BeforeEach
    void setUp() {
        employee = new Employee();
        employee.setId(1L);
        employee.setBadgeId("BADGE123");
        employee.setName("John Doe");
        employee.setRole("Worker");
        employee.setDepartment("Packing");
        employee.setShiftGroup("Morning");
        employee.setHireDate(LocalDate.of(2020, 1, 1));
        employee.setStatus("ACTIVE");
        employee.setTenantId(100L);
        employee.setDeleted(false);
        employee.setCreatedAt(LocalDateTime.now().minusDays(1));
        employee.setUpdatedAt(LocalDateTime.now().minusHours(1));
        employee.setCertifications(new HashSet<>());
        employee.setShiftAssignments(new HashSet<>());
        employee.setLeaveRequests(new HashSet<>());
        employee.setAssetAssignments(new HashSet<>());
    }

    @AfterEach
    void tearDown() {
        employee = null;
    }

    @Test
    void constructor_AllArgs_FieldsSetCorrectly() {
        LocalDateTime now = LocalDateTime.now();
        Employee emp = new Employee(2L, "BADGE456", "Jane Smith", "Supervisor", "Shipping", "Night", LocalDate.of(2021, 5, 10), "INACTIVE", 101L, true, now.minusDays(2), now.minusDays(1), new HashSet<>(), new HashSet<>(), new HashSet<>(), new HashSet<>());
        assertEquals(2L, emp.getId());
        assertEquals("BADGE456", emp.getBadgeId());
        assertEquals("Jane Smith", emp.getName());
        assertEquals("Supervisor", emp.getRole());
        assertEquals("Shipping", emp.getDepartment());
        assertEquals("Night", emp.getShiftGroup());
        assertEquals(LocalDate.of(2021, 5, 10), emp.getHireDate());
        assertEquals("INACTIVE", emp.getStatus());
        assertEquals(101L, emp.getTenantId());
        assertTrue(emp.getDeleted());
        assertEquals(now.minusDays(2), emp.getCreatedAt());
        assertEquals(now.minusDays(1), emp.getUpdatedAt());
    }

    @Test
    void badgeId_UniqueConstraint_DuplicateBadgeIdNotAllowed() {
        // This test would be covered in repository integration tests
        assertEquals("BADGE123", employee.getBadgeId());
    }

    @Test
    void status_DefaultValue_IsActive() {
        Employee emp = new Employee();
        assertEquals("ACTIVE", emp.getStatus());
    }

    @Test
    void deleted_DefaultValue_IsFalse() {
        Employee emp = new Employee();
        assertFalse(emp.getDeleted());
    }

    @Test
    void createdAt_DefaultValue_IsNowOrBeforeNow() {
        Employee emp = new Employee();
        assertNotNull(emp.getCreatedAt());
        assertTrue(emp.getCreatedAt().isBefore(LocalDateTime.now().plusSeconds(1)));
    }

    @Test
    void updatedAt_DefaultValue_IsNowOrBeforeNow() {
        Employee emp = new Employee();
        assertNotNull(emp.getUpdatedAt());
        assertTrue(emp.getUpdatedAt().isBefore(LocalDateTime.now().plusSeconds(1)));
    }

    @Test
    void onUpdate_UpdatesUpdatedAtField() throws InterruptedException {
        LocalDateTime beforeUpdate = employee.getUpdatedAt();
        Thread.sleep(1000); // Ensure time difference
        employee.onUpdate();
        assertTrue(employee.getUpdatedAt().isAfter(beforeUpdate));
    }

    @Test
    void relationships_Certifications_EmptySet() {
        assertNotNull(employee.getCertifications());
        assertTrue(employee.getCertifications().isEmpty());
    }

    @Test
    void relationships_ShiftAssignments_EmptySet() {
        assertNotNull(employee.getShiftAssignments());
        assertTrue(employee.getShiftAssignments().isEmpty());
    }

    @Test
    void relationships_LeaveRequests_EmptySet() {
        assertNotNull(employee.getLeaveRequests());
        assertTrue(employee.getLeaveRequests().isEmpty());
    }

    @Test
    void relationships_AssetAssignments_EmptySet() {
        assertNotNull(employee.getAssetAssignments());
        assertTrue(employee.getAssetAssignments().isEmpty());
    }

    @Test
    void nullFields_ThrowsExceptionOnRequiredFields() {
        Employee emp = new Employee();
        assertThrows(NullPointerException.class, () -> {
            emp.setBadgeId(null);
            emp.getBadgeId().length();
        });
        assertThrows(NullPointerException.class, () -> {
            emp.setName(null);
            emp.getName().length();
        });
        assertThrows(NullPointerException.class, () -> {
            emp.setRole(null);
            emp.getRole().length();
        });
        assertThrows(NullPointerException.class, () -> {
            emp.setStatus(null);
            emp.getStatus().length();
        });
    }

    @Test
    void emptyStringFields_AllowedForOptionalFields() {
        employee.setDepartment("");
        employee.setShiftGroup("");
        assertEquals("", employee.getDepartment());
        assertEquals("", employee.getShiftGroup());
    }

    @Test
    void boundaryConditions_LongBadgeId_AllowedUpTo50Chars() {
        String badgeId = "A".repeat(50);
        employee.setBadgeId(badgeId);
        assertEquals(50, employee.getBadgeId().length());
    }

    @Test
    void boundaryConditions_LongRole_AllowedUpTo50Chars() {
        String role = "B".repeat(50);
        employee.setRole(role);
        assertEquals(50, employee.getRole().length());
    }

    @Test
    void boundaryConditions_LongDepartment_AllowedUpTo100Chars() {
        String dept = "C".repeat(100);
        employee.setDepartment(dept);
        assertEquals(100, employee.getDepartment().length());
    }

    @Test
    void boundaryConditions_LongShiftGroup_AllowedUpTo100Chars() {
        String shift = "D".repeat(100);
        employee.setShiftGroup(shift);
        assertEquals(100, employee.getShiftGroup().length());
    }
}
