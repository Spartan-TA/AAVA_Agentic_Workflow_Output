package com.warehouse.ems.employee;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Comprehensive unit tests for Employee entity
 * Tests cover: entity fields, builder pattern, equals/hashCode, validation
 */
class EmployeeTest {

    @Test
    void testEmployeeEntityFieldsAndBuilder() {
        // Arrange & Act
        Employee employee = Employee.builder()
                .id(1L)
                .name("John Doe")
                .badgeId("B12345")
                .role("Operator")
                .department("Logistics")
                .shiftGroup("A")
                .hireDate(LocalDate.of(2020, 1, 15))
                .active(true)
                .deleted(false)
                .build();

        // Assert
        assertThat(employee.getId()).isEqualTo(1L);
        assertThat(employee.getName()).isEqualTo("John Doe");
        assertThat(employee.getBadgeId()).isEqualTo("B12345");
        assertThat(employee.getRole()).isEqualTo("Operator");
        assertThat(employee.getDepartment()).isEqualTo("Logistics");
        assertThat(employee.getShiftGroup()).isEqualTo("A");
        assertThat(employee.getHireDate()).isEqualTo(LocalDate.of(2020, 1, 15));
        assertThat(employee.isActive()).isTrue();
        assertThat(employee.isDeleted()).isFalse();
    }

    @Test
    void testEmployeeEqualsAndHashCode() {
        // Arrange
        Employee e1 = Employee.builder().id(1L).badgeId("B1").build();
        Employee e2 = Employee.builder().id(1L).badgeId("B1").build();
        Employee e3 = Employee.builder().id(2L).badgeId("B2").build();

        // Assert
        assertThat(e1).isEqualTo(e2);
        assertThat(e1).hasSameHashCodeAs(e2);
        assertThat(e1).isNotEqualTo(e3);
    }

    @Test
    void testEmployeeWithNullValues() {
        // Arrange & Act
        Employee employee = Employee.builder()
                .id(1L)
                .name(null)
                .badgeId("B123")
                .build();

        // Assert
        assertThat(employee.getName()).isNull();
        assertThat(employee.getBadgeId()).isEqualTo("B123");
    }

    @Test
    void testEmployeeWithEmptyStrings() {
        // Arrange & Act
        Employee employee = Employee.builder()
                .id(1L)
                .name("")
                .badgeId("")
                .build();

        // Assert
        assertThat(employee.getName()).isEmpty();
        assertThat(employee.getBadgeId()).isEmpty();
    }

    @Test
    void testEmployeeSoftDelete() {
        // Arrange
        Employee employee = Employee.builder()
                .id(1L)
                .name("John Doe")
                .active(true)
                .deleted(false)
                .build();

        // Act
        employee.setDeleted(true);
        employee.setActive(false);

        // Assert
        assertThat(employee.isDeleted()).isTrue();
        assertThat(employee.isActive()).isFalse();
    }
}