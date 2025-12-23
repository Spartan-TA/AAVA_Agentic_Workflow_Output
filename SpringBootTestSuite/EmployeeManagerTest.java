import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class EmployeeManagerTest {
    private EmployeeManager manager;

    @BeforeEach
    public void setUp() {
        manager = new EmployeeManager();
        manager.addEmployee(new Employee(101, "Ravi", "Sales"));
        manager.addEmployee(new Employee(102, "Mani", "HR"));
        manager.addEmployee(new Employee(103, "Sita", "IT"));
    }

    @Test
    public void testAddEmployee_NormalCase() {
        Employee emp = new Employee(104, "Arun", "Finance");
        manager.addEmployee(emp);
        assertEquals(emp, manager.searchEmployee(104));
    }

    @Test
    public void testAddEmployee_DuplicateId() {
        Employee emp = new Employee(101, "Ravi2", "Marketing");
        manager.addEmployee(emp);
        // Should find the first added employee with id 101
        Employee found = manager.searchEmployee(101);
        assertNotNull(found);
        assertEquals("Ravi", found.name); // The first one remains
    }

    @Test
    public void testSearchEmployee_ExistingId() {
        Employee emp = manager.searchEmployee(102);
        assertNotNull(emp);
        assertEquals("Mani", emp.name);
    }

    @Test
    public void testSearchEmployee_NonExistingId() {
        Employee emp = manager.searchEmployee(999);
        assertNull(emp);
    }

    @Test
    public void testSearchEmployee_NegativeId() {
        Employee emp = manager.searchEmployee(-1);
        assertNull(emp);
    }

    @Test
    public void testDeleteEmployee_ExistingId() {
        boolean removed = manager.deleteEmployee(102);
        assertTrue(removed);
        assertNull(manager.searchEmployee(102));
    }

    @Test
    public void testDeleteEmployee_NonExistingId() {
        boolean removed = manager.deleteEmployee(999);
        assertFalse(removed);
    }

    @Test
    public void testDeleteEmployee_NegativeId() {
        boolean removed = manager.deleteEmployee(-1);
        assertFalse(removed);
    }

    @Test
    public void testAddEmployee_NullEmployee() {
        assertThrows(NullPointerException.class, () -> manager.addEmployee(null));
    }

    @Test
    public void testAddEmployee_EmptyNameDept() {
        Employee emp = new Employee(105, "", "");
        manager.addEmployee(emp);
        Employee found = manager.searchEmployee(105);
        assertNotNull(found);
        assertEquals("", found.name);
        assertEquals("", found.dept);
    }

    @Test
    public void testDisplayAll_NoEmployees() {
        EmployeeManager emptyManager = new EmployeeManager();
        assertDoesNotThrow(() -> emptyManager.displayAll());
    }

    @AfterEach
    public void tearDown() {
        manager = null;
    }
}
