package com.warehouse.ems.domain;

import org.junit.jupiter.api.*;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class EmployeeTest {

    private Employee employee;

    @BeforeEach
    public void setUp() {
        employee = new Employee();
        employee.setId(1L);
        employee.setBadgeId("B12345");
        employee.setName("John Doe");
        employee.setRole("WORKER");
        employee.setDepartment("Shipping");
        employee.setShiftGroup("A");
        employee.setHireDate(LocalDateTime.now().minusYears(1));
        employee.setStatus("ACTIVE");
        employee.setCreatedAt(LocalDateTime.now().minusYears(1));
        employee.setUpdatedAt(LocalDateTime.now());
        employee.setDeletedAt(null);
    }

    @Test
    public void testSoftDeleteSetsDeletedAt() {
        // Act
        employee.softDelete();

        // Assert
        assertNotNull(employee.getDeletedAt());
        assertTrue(employee.isDeleted());
    }

    @Test
    public void testIsDeletedReturnsFalseWhenDeletedAtIsNull() {
        // Assert
        assertFalse(employee.isDeleted());
    }

    @Test
    public void testIsDeletedReturnsTrueWhenDeletedAtIsSet() {
        // Arrange
        employee.setDeletedAt(LocalDateTime.now());

        // Assert
        assertTrue(employee.isDeleted());
    }

    @Test
    public void testPrePersistSetsCreatedAtAndUpdatedAt() {
        // Arrange
        employee.setCreatedAt(null);
        employee.setUpdatedAt(null);

        // Act
        employee.prePersist();

        // Assert
        assertNotNull(employee.getCreatedAt());
        assertNotNull(employee.getUpdatedAt());
    }

    @Test
    public void testPreUpdateSetsUpdatedAt() {
        // Arrange
        employee.setUpdatedAt(null);

        // Act
        employee.preUpdate();

        // Assert
        assertNotNull(employee.getUpdatedAt());
    }

    @Test
    public void testBadgeIdUniqueness() {
        // This would be tested at the repository/service layer, but here we ensure badgeId is set
        assertEquals("B12345", employee.getBadgeId());
    }
}