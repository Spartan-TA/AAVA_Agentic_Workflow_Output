package com.warehouse.employee_mgmt.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive JUnit test suite for Employee.java entity
 * Tests all fields, getters/setters, validation, nulls, boundaries, and edge cases
 * 
 * @author Automation Test Engineer
 * @version 1.0
 */
@DisplayName("Employee Entity Comprehensive Tests")
class EmployeeEntityTest {
    private Employee employee;

    @BeforeEach
    void setUp() {
        employee = new Employee();
    }

    @Nested
    @DisplayName("Default Values Tests")
    class DefaultValuesTests {
        @Test
        @DisplayName("Default deleted is false")
        void testDefaultDeletedIsFalse() {
            assertFalse(employee.isDeleted());
        }
    }

    @Nested
    @DisplayName("Getter and Setter Tests")
    class GetterSetterTests {
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
        void testFirstNameGetterSetter() {
            employee.setFirstName("John");
            assertEquals("John", employee.getFirstName());
        }

        @Test
        void testLastNameGetterSetter() {
            employee.setLastName("Doe");
            assertEquals("Doe", employee.getLastName());
        }

        @Test
        void testEmailGetterSetter() {
            employee.setEmail("john.doe@example.com");
            assertEquals("john.doe@example.com", employee.getEmail());
        }

        @Test
        void testPhoneNumberGetterSetter() {
            employee.setPhoneNumber("1234567890");
            assertEquals("1234567890", employee.getPhoneNumber());
        }

        @Test
        void testRoleGetterSetter() {
            employee.setRole("ADMIN");
            assertEquals("ADMIN", employee.getRole());
        }

        @Test
        void testDepartmentGetterSetter() {
            employee.setDepartment("Logistics");
            assertEquals("Logistics", employee.getDepartment());
        }

        @Test
        void testShiftGroupGetterSetter() {
            employee.setShiftGroup("A");
            assertEquals("A", employee.getShiftGroup());
        }

        @Test
        void testHireDateGetterSetter() {
            LocalDate date = LocalDate.of(2020, 1, 1);
            employee.setHireDate(date);
            assertEquals(date, employee.getHireDate());
        }

        @Test
        void testStatusGetterSetter() {
            employee.setStatus("ACTIVE");
            assertEquals("ACTIVE", employee.getStatus());
        }

        @Test
        void testAddressGetterSetter() {
            employee.setAddress("123 Main St");
            assertEquals("123 Main St", employee.getAddress());
        }

        @Test
        void testEmergencyContactGetterSetter() {
            employee.setEmergencyContact("Jane Doe");
            assertEquals("Jane Doe", employee.getEmergencyContact());
        }

        @Test
        void testDeletedGetterSetter() {
            employee.setDeleted(true);
            assertTrue(employee.isDeleted());
            employee.setDeleted(false);
            assertFalse(employee.isDeleted());
        }

        @Test
        void testCreatedDateGetterSetter() {
            LocalDateTime now = LocalDateTime.now();
            employee.setCreatedDate(now);
            assertEquals(now, employee.getCreatedDate());
        }

        @Test
        void testLastModifiedDateGetterSetter() {
            LocalDateTime now = LocalDateTime.now();
            employee.setLastModifiedDate(now);
            assertEquals(now, employee.getLastModifiedDate());
        }

        @Test
        void testCreatedByGetterSetter() {
            employee.setCreatedBy("admin");
            assertEquals("admin", employee.getCreatedBy());
        }

        @Test
        void testLastModifiedByGetterSetter() {
            employee.setLastModifiedBy("hr");
            assertEquals("hr", employee.getLastModifiedBy());
        }
    }

    @Nested
    @DisplayName("Null Value Tests")
    class NullValueTests {
        @Test
        void testNullBadgeId() {
            employee.setBadgeId(null);
            assertNull(employee.getBadgeId());
        }

        @Test
        void testNullFirstName() {
            employee.setFirstName(null);
            assertNull(employee.getFirstName());
        }

        @Test
        void testNullLastName() {
            employee.setLastName(null);
            assertNull(employee.getLastName());
        }

        @Test
        void testNullEmail() {
            employee.setEmail(null);
            assertNull(employee.getEmail());
        }

        @Test
        void testNullPhoneNumber() {
            employee.setPhoneNumber(null);
            assertNull(employee.getPhoneNumber());
        }

        @Test
        void testNullRole() {
            employee.setRole(null);
            assertNull(employee.getRole());
        }

        @Test
        void testNullDepartment() {
            employee.setDepartment(null);
            assertNull(employee.getDepartment());
        }

        @Test
        void testNullShiftGroup() {
            employee.setShiftGroup(null);
            assertNull(employee.getShiftGroup());
        }

        @Test
        void testNullHireDate() {
            employee.setHireDate(null);
            assertNull(employee.getHireDate());
        }

        @Test
        void testNullStatus() {
            employee.setStatus(null);
            assertNull(employee.getStatus());
        }

        @Test
        void testNullAddress() {
            employee.setAddress(null);
            assertNull(employee.getAddress());
        }

        @Test
        void testNullEmergencyContact() {
            employee.setEmergencyContact(null);
            assertNull(employee.getEmergencyContact());
        }

        @Test
        void testNullCreatedDate() {
            employee.setCreatedDate(null);
            assertNull(employee.getCreatedDate());
        }

        @Test
        void testNullLastModifiedDate() {
            employee.setLastModifiedDate(null);
            assertNull(employee.getLastModifiedDate());
        }

        @Test
        void testNullCreatedBy() {
            employee.setCreatedBy(null);
            assertNull(employee.getCreatedBy());
        }

        @Test
        void testNullLastModifiedBy() {
            employee.setLastModifiedBy(null);
            assertNull(employee.getLastModifiedBy());
        }
    }

    @Nested
    @DisplayName("Boundary Value Tests")
    class BoundaryValueTests {
        @Test
        void testBoundaryBadgeId() {
            String badge = "B" + "1".repeat(255);
            employee.setBadgeId(badge);
            assertEquals(badge, employee.getBadgeId());
        }

        @Test
        void testBoundaryFirstName() {
            String name = "A".repeat(255);
            employee.setFirstName(name);
            assertEquals(name, employee.getFirstName());
        }

        @Test
        void testBoundaryLastName() {
            String name = "B".repeat(255);
            employee.setLastName(name);
            assertEquals(name, employee.getLastName());
        }

        @Test
        void testBoundaryEmail() {
            String email = "a@b.co";
            employee.setEmail(email);
            assertEquals(email, employee.getEmail());
        }

        @Test
        void testBoundaryPhoneNumber() {
            String phone = "1".repeat(20);
            employee.setPhoneNumber(phone);
            assertEquals(phone, employee.getPhoneNumber());
        }

        @Test
        void testBoundaryRole() {
            String role = "WORKER";
            employee.setRole(role);
            assertEquals(role, employee.getRole());
        }

        @Test
        void testBoundaryDepartment() {
            String dept = "D".repeat(100);
            employee.setDepartment(dept);
            assertEquals(dept, employee.getDepartment());
        }

        @Test
        void testBoundaryShiftGroup() {
            String shift = "Z".repeat(10);
            employee.setShiftGroup(shift);
            assertEquals(shift, employee.getShiftGroup());
        }

        @Test
        void testBoundaryStatus() {
            String status = "ACTIVE";
            employee.setStatus(status);
            assertEquals(status, employee.getStatus());
        }

        @Test
        void testBoundaryAddress() {
            String addr = "A".repeat(500);
            employee.setAddress(addr);
            assertEquals(addr, employee.getAddress());
        }

        @Test
        void testBoundaryEmergencyContact() {
            String contact = "C".repeat(100);
            employee.setEmergencyContact(contact);
            assertEquals(contact, employee.getEmergencyContact());
        }
    }

    @Nested
    @DisplayName("Object Methods Tests")
    class ObjectMethodsTests {
        @Test
        void testToStringNotNull() {
            assertNotNull(employee.toString());
        }

        @Test
        void testEqualsAndHashCode() {
            Employee e1 = new Employee();
            Employee e2 = new Employee();
            e1.setId(1L);
            e2.setId(1L);
            assertEquals(e1, e2);
            assertEquals(e1.hashCode(), e2.hashCode());
        }

        @Test
        void testNotEquals() {
            Employee e1 = new Employee();
            Employee e2 = new Employee();
            e1.setId(1L);
            e2.setId(2L);
            assertNotEquals(e1, e2);
        }

        @Test
        void testEqualsNull() {
            Employee e1 = new Employee();
            assertNotEquals(e1, null);
        }

        @Test
        void testEqualsDifferentClass() {
            Employee e1 = new Employee();
            assertNotEquals(e1, "string");
        }
    }
}