import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class EmployeeRepositoryTest {
    @Autowired
    private EmployeeRepository employeeRepository;

    private Employee employee;

    @BeforeEach
    void setUp() {
        employee = new Employee();
        employee.setBadgeId("EMP001");
        employee.setFirstName("John");
        employee.setLastName("Doe");
        employee.setEmail("john.doe@example.com");
        employee.setPhone("1234567890");
        employee.setRole("WORKER");
        employee.setDepartment("Logistics");
        employee.setShiftGroup("A");
        employee.setStatus("ACTIVE");
        employee.setIsDeleted(false);
        employeeRepository.save(employee);
    }

    @Test
    @DisplayName("Find by badgeId returns employee if exists")
    void testFindByBadgeId_Exists() {
        Optional<Employee> found = employeeRepository.findByBadgeId("EMP001");
        assertTrue(found.isPresent());
        assertEquals("John", found.get().getFirstName());
    }

    @Test
    @DisplayName("Find by badgeId returns empty if not exists")
    void testFindByBadgeId_NotExists() {
        Optional<Employee> found = employeeRepository.findByBadgeId("EMP999");
        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("Find by department returns correct employees")
    void testFindByDepartment() {
        List<Employee> employees = employeeRepository.findByDepartment("Logistics");
        assertFalse(employees.isEmpty());
        assertEquals("EMP001", employees.get(0).getBadgeId());
    }

    @Test
    @DisplayName("Find by status returns correct employees")
    void testFindByStatus() {
        List<Employee> employees = employeeRepository.findByStatus("ACTIVE");
        assertFalse(employees.isEmpty());
        assertEquals("EMP001", employees.get(0).getBadgeId());
    }

    @Test
    @DisplayName("Find active employees does not return deleted")
    void testFindActiveEmployees() {
        employee.setIsDeleted(true);
        employeeRepository.save(employee);
        List<Employee> active = employeeRepository.findActiveEmployees();
        assertTrue(active.isEmpty());
    }

    @Test
    @DisplayName("Save duplicate badgeId throws exception")
    void testSaveDuplicateBadgeId() {
        Employee duplicate = new Employee();
        duplicate.setBadgeId("EMP001");
        duplicate.setFirstName("Jane");
        duplicate.setLastName("Smith");
        duplicate.setEmail("jane.smith@example.com");
        duplicate.setPhone("0987654321");
        duplicate.setRole("HR");
        duplicate.setDepartment("HR");
        duplicate.setShiftGroup("B");
        duplicate.setStatus("ACTIVE");
        duplicate.setIsDeleted(false);
        assertThrows(DataIntegrityViolationException.class, () -> {
            employeeRepository.saveAndFlush(duplicate);
        });
    }

    @Test
    @DisplayName("Delete employee marks as deleted (soft delete)")
    void testSoftDelete() {
        employee.setIsDeleted(true);
        employeeRepository.save(employee);
        Optional<Employee> found = employeeRepository.findByBadgeId("EMP001");
        assertTrue(found.isPresent());
        assertTrue(found.get().getIsDeleted());
    }
}
