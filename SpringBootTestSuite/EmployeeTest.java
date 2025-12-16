import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDate;

public class EmployeeTest {
    private Employee employee;

    @BeforeEach
    void setUp() {
        employee = new Employee();
        employee.setId(1L);
        employee.setBadgeId("EMP001");
        employee.setFirstName("John");
        employee.setLastName("Doe");
        employee.setEmail("john.doe@example.com");
        employee.setPhone("1234567890");
        employee.setRole("WORKER");
        employee.setDepartment("Logistics");
        employee.setShiftGroup("A");
        employee.setHireDate(LocalDate.of(2022, 1, 1));
        employee.setStatus("ACTIVE");
        employee.setIsDeleted(false);
    }

    @Test
    void testEmployeeFields_ValidValues() {
        assertEquals(1L, employee.getId());
        assertEquals("EMP001", employee.getBadgeId());
        assertEquals("John", employee.getFirstName());
        assertEquals("Doe", employee.getLastName());
        assertEquals("john.doe@example.com", employee.getEmail());
        assertEquals("1234567890", employee.getPhone());
        assertEquals("WORKER", employee.getRole());
        assertEquals("Logistics", employee.getDepartment());
        assertEquals("A", employee.getShiftGroup());
        assertEquals(LocalDate.of(2022, 1, 1), employee.getHireDate());
        assertEquals("ACTIVE", employee.getStatus());
        assertFalse(employee.getIsDeleted());
    }

    @Test
    void testEmployeeFields_NullAndEmptyValues() {
        employee.setFirstName(null);
        employee.setLastName("");
        employee.setEmail(null);
        employee.setPhone("");
        assertNull(employee.getFirstName());
        assertEquals("", employee.getLastName());
        assertNull(employee.getEmail());
        assertEquals("", employee.getPhone());
    }

    @Test
    void testEqualsAndHashCode_SameObject() {
        assertEquals(employee, employee);
        assertEquals(employee.hashCode(), employee.hashCode());
    }

    @Test
    void testEqualsAndHashCode_EqualObjects() {
        Employee other = new Employee();
        other.setId(1L);
        other.setBadgeId("EMP001");
        other.setFirstName("John");
        other.setLastName("Doe");
        other.setEmail("john.doe@example.com");
        other.setPhone("1234567890");
        other.setRole("WORKER");
        other.setDepartment("Logistics");
        other.setShiftGroup("A");
        other.setHireDate(LocalDate.of(2022, 1, 1));
        other.setStatus("ACTIVE");
        other.setIsDeleted(false);
        assertEquals(employee, other);
        assertEquals(employee.hashCode(), other.hashCode());
    }

    @Test
    void testEqualsAndHashCode_DifferentObjects() {
        Employee other = new Employee();
        other.setId(2L);
        other.setBadgeId("EMP002");
        assertNotEquals(employee, other);
        assertNotEquals(employee.hashCode(), other.hashCode());
    }

    @Test
    void testToString_NotNull() {
        assertNotNull(employee.toString());
        assertTrue(employee.toString().contains("EMP001"));
    }
}
