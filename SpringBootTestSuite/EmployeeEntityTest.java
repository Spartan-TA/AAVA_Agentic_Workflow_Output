package com.warehouse.ems.domain;

import org.junit.jupiter.api.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class EmployeeEntityTest {
    private Employee employee;

    @BeforeEach
    void setUp() {
        employee = Employee.builder()
                .id(1L)
                .badgeId("BADGE1234567890123456789012345678")
                .name("John Doe")
                .role("WORKER")
                .department("Logistics")
                .shiftGroup("A")
                .hireDate(LocalDate.of(2020, 1, 1))
                .status("ACTIVE")
                .deleted(false)
                .createdAt(LocalDateTime.now().minusDays(1))
                .updatedAt(LocalDateTime.now().minusDays(1))
                .build();
    }

    @Test
    void testGettersAndSetters() {
        employee.setId(2L);
        assertEquals(2L, employee.getId(), "ID should be updated");
        employee.setBadgeId("BADGE2");
        assertEquals("BADGE2", employee.getBadgeId(), "Badge ID should be updated");
        employee.setName("Jane Smith");
        assertEquals("Jane Smith", employee.getName(), "Name should be updated");
        employee.setRole("ADMIN");
        assertEquals("ADMIN", employee.getRole(), "Role should be updated");
        employee.setDepartment("HR");
        assertEquals("HR", employee.getDepartment(), "Department should be updated");
        employee.setShiftGroup("B");
        assertEquals("B", employee.getShiftGroup(), "Shift group should be updated");
        LocalDate hireDate = LocalDate.of(2021, 5, 10);
        employee.setHireDate(hireDate);
        assertEquals(hireDate, employee.getHireDate(), "Hire date should be updated");
        employee.setStatus("INACTIVE");
        assertEquals("INACTIVE", employee.getStatus(), "Status should be updated");
        employee.setDeleted(true);
        assertTrue(employee.getDeleted(), "Deleted should be true");
    }

    @Test
    void testBuilderCreatesCorrectObject() {
        Employee emp = Employee.builder()
                .id(3L)
                .badgeId("BADGE3")
                .name("Builder Test")
                .role("HR")
                .department("Admin")
                .shiftGroup("C")
                .hireDate(LocalDate.of(2019, 12, 31))
                .status("ACTIVE")
                .deleted(false)
                .build();
        assertEquals(3L, emp.getId());
        assertEquals("BADGE3", emp.getBadgeId());
        assertEquals("Builder Test", emp.getName());
        assertEquals("HR", emp.getRole());
        assertEquals("Admin", emp.getDepartment());
        assertEquals("C", emp.getShiftGroup());
        assertEquals(LocalDate.of(2019, 12, 31), emp.getHireDate());
        assertEquals("ACTIVE", emp.getStatus());
        assertFalse(emp.getDeleted());
    }

    @Test
    void testNoArgsConstructor() {
        Employee emp = new Employee();
        assertNull(emp.getId());
        assertNull(emp.getBadgeId());
        assertNull(emp.getName());
        assertNull(emp.getRole());
        assertNull(emp.getDepartment());
        assertNull(emp.getShiftGroup());
        assertNull(emp.getHireDate());
        assertNull(emp.getStatus());
        assertNull(emp.getDeleted());
    }

    @Test
    void testAllArgsConstructor() {
        LocalDateTime now = LocalDateTime.now();
        Employee emp = new Employee(4L, "BADGE4", "All Args", "SUPERVISOR", "Ops", "D", LocalDate.of(2018, 6, 15), "ACTIVE", false, now, now);
        assertEquals(4L, emp.getId());
        assertEquals("BADGE4", emp.getBadgeId());
        assertEquals("All Args", emp.getName());
        assertEquals("SUPERVISOR", emp.getRole());
        assertEquals("Ops", emp.getDepartment());
        assertEquals("D", emp.getShiftGroup());
        assertEquals(LocalDate.of(2018, 6, 15), emp.getHireDate());
        assertEquals("ACTIVE", emp.getStatus());
        assertFalse(emp.getDeleted());
        assertEquals(now, emp.getCreatedAt());
        assertEquals(now, emp.getUpdatedAt());
    }

    @Test
    void testOnUpdateSetsUpdatedAt() throws InterruptedException {
        LocalDateTime before = employee.getUpdatedAt();
        Thread.sleep(10); // ensure time difference
        employee.onUpdate();
        LocalDateTime after = employee.getUpdatedAt();
        assertTrue(after.isAfter(before), "updatedAt should be updated to a later time");
    }

    @Test
    void testDefaultValues() {
        Employee emp = new Employee();
        assertNull(emp.getDeleted(), "Default deleted should be null (Lombok default)");
        assertNull(emp.getCreatedAt(), "Default createdAt should be null (Lombok default)");
        assertNull(emp.getUpdatedAt(), "Default updatedAt should be null (Lombok default)");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "BADGE1234567890123456789012345678", "BADGE!@#$%^&*()_+"})
    void testBadgeIdBoundaries(String badgeId) {
        employee.setBadgeId(badgeId);
        assertEquals(badgeId, employee.getBadgeId(), "BadgeId should accept boundary values");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "A", "NameWith128Characters_aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"})
    void testNameBoundaries(String name) {
        employee.setName(name);
        assertEquals(name, employee.getName(), "Name should accept boundary values");
    }

    @Test
    void testNullFields() {
        employee.setDepartment(null);
        employee.setShiftGroup(null);
        employee.setHireDate(null);
        assertNull(employee.getDepartment());
        assertNull(employee.getShiftGroup());
        assertNull(employee.getHireDate());
    }

    @Test
    void testToStringAndHashCode() {
        String str = employee.toString();
        assertNotNull(str, "toString should not be null");
        int hash = employee.hashCode();
        assertNotEquals(0, hash, "hashCode should not be zero");
    }

    @Test
    void testEquals() {
        Employee emp2 = Employee.builder()
                .id(employee.getId())
                .badgeId(employee.getBadgeId())
                .name(employee.getName())
                .role(employee.getRole())
                .department(employee.getDepartment())
                .shiftGroup(employee.getShiftGroup())
                .hireDate(employee.getHireDate())
                .status(employee.getStatus())
                .deleted(employee.getDeleted())
                .createdAt(employee.getCreatedAt())
                .updatedAt(employee.getUpdatedAt())
                .build();
        assertEquals(employee, emp2, "Employees with same fields should be equal");
    }

    @Test
    void testXssAndSqlInjectionStrings() {
        String xss = "<script>alert('xss')</script>";
        String sql = "'; DROP TABLE employee; --";
        employee.setName(xss);
        assertEquals(xss, employee.getName());
        employee.setDepartment(sql);
        assertEquals(sql, employee.getDepartment());
    }
}
