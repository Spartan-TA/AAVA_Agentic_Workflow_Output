package SpringBootTestSuite;

import com.warehouse.employee_mgmt.domain.Employee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class EmployeeEntityTest {
    private Employee employee;

    @BeforeEach
    void setUp() {
        employee = Employee.builder()
                .id(UUID.randomUUID())
                .name("John Doe")
                .badgeId("BADGE123")
                .role("WORKER")
                .department("Logistics")
                .shiftGroup("A")
                .hireDate(LocalDate.now().minusDays(1))
                .status("ACTIVE")
                .deleted(false)
                .tenantId(UUID.randomUUID())
                .build();
    }

    @Test
    @DisplayName("testPrePersist_SetsCreatedAtAndUpdatedAt")
    void testPrePersist_SetsCreatedAtAndUpdatedAt() {
        employee.onCreate();
        assertNotNull(employee.getCreatedAt());
        assertNotNull(employee.getUpdatedAt());
        assertTrue(employee.getCreatedAt().isBefore(LocalDateTime.now().plusSeconds(1)));
        assertTrue(employee.getUpdatedAt().isBefore(LocalDateTime.now().plusSeconds(1)));
    }

    @Test
    @DisplayName("testPreUpdate_UpdatesUpdatedAt")
    void testPreUpdate_UpdatesUpdatedAt() throws InterruptedException {
        employee.onCreate();
        LocalDateTime originalUpdatedAt = employee.getUpdatedAt();
        Thread.sleep(10); // Ensure time difference
        employee.onUpdate();
        assertTrue(employee.getUpdatedAt().isAfter(originalUpdatedAt));
    }

    @Test
    @DisplayName("testBuilder_Boundary_MaxLengthFields")
    void testBuilder_Boundary_MaxLengthFields() {
        Employee maxEmployee = Employee.builder()
                .name("A".repeat(100))
                .badgeId("B".repeat(50))
                .role("ADMIN")
                .department("D".repeat(50))
                .shiftGroup("S".repeat(50))
                .hireDate(LocalDate.now())
                .status("ACTIVE")
                .deleted(false)
                .tenantId(UUID.randomUUID())
                .build();
        assertEquals(100, maxEmployee.getName().length());
        assertEquals(50, maxEmployee.getBadgeId().length());
        assertEquals(50, maxEmployee.getDepartment().length());
        assertEquals(50, maxEmployee.getShiftGroup().length());
    }

    @Test
    @DisplayName("testBuilder_Boundary_EmptyStrings")
    void testBuilder_Boundary_EmptyStrings() {
        Employee emptyEmployee = Employee.builder()
                .name("")
                .badgeId("")
                .role("")
                .department("")
                .shiftGroup("")
                .hireDate(LocalDate.now())
                .status("")
                .deleted(false)
                .tenantId(UUID.randomUUID())
                .build();
        assertEquals("", emptyEmployee.getName());
        assertEquals("", emptyEmployee.getBadgeId());
        assertEquals("", emptyEmployee.getRole());
        assertEquals("", emptyEmployee.getDepartment());
        assertEquals("", emptyEmployee.getShiftGroup());
        assertEquals("", emptyEmployee.getStatus());
    }

    @Test
    @DisplayName("testBuilder_EdgeCase_FutureHireDate")
    void testBuilder_EdgeCase_FutureHireDate() {
        LocalDate futureDate = LocalDate.now().plusDays(1);
        Employee futureEmployee = Employee.builder()
                .name("Future")
                .badgeId("FUTURE1")
                .role("WORKER")
                .department("FutureDept")
                .shiftGroup("B")
                .hireDate(futureDate)
                .status("ACTIVE")
                .deleted(false)
                .tenantId(UUID.randomUUID())
                .build();
        assertEquals(futureDate, futureEmployee.getHireDate());
    }

    @Test
    @DisplayName("testSettersAndGetters_NormalCase")
    void testSettersAndGetters_NormalCase() {
        employee.setName("Jane Doe");
        employee.setBadgeId("BADGE999");
        employee.setRole("ADMIN");
        employee.setDepartment("HR");
        employee.setShiftGroup("C");
        employee.setHireDate(LocalDate.now());
        employee.setStatus("INACTIVE");
        employee.setDeleted(true);
        employee.setCreatedBy("admin");
        employee.setUpdatedBy("admin");
        UUID tenantId = UUID.randomUUID();
        employee.setTenantId(tenantId);
        assertEquals("Jane Doe", employee.getName());
        assertEquals("BADGE999", employee.getBadgeId());
        assertEquals("ADMIN", employee.getRole());
        assertEquals("HR", employee.getDepartment());
        assertEquals("C", employee.getShiftGroup());
        assertEquals("INACTIVE", employee.getStatus());
        assertTrue(employee.getDeleted());
        assertEquals("admin", employee.getCreatedBy());
        assertEquals("admin", employee.getUpdatedBy());
        assertEquals(tenantId, employee.getTenantId());
    }
}
