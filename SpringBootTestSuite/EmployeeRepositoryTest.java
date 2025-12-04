package SpringBootTestSuite;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.*;

@DataJpaTest
public class EmployeeRepositoryTest {

    @Autowired
    private EmployeeRepository employeeRepository;

    private Employee validEmployee;

    @BeforeEach
    public void setUp() {
        validEmployee = new Employee();
        validEmployee.setName("John Doe");
        validEmployee.setBadgeId("EMP001");
        validEmployee.setRole(Role.WORKER);
        validEmployee.setDepartment("Logistics");
        validEmployee.setHireDate(LocalDate.of(2023, 1, 1));
        validEmployee.setStatus(Status.ACTIVE);
        validEmployee.setDeleted(false);
        employeeRepository.save(validEmployee);
    }

    @Test
    public void testFindByBadgeIdAndDeletedFalse_ExistingBadge_ReturnsEmployee() {
        Optional<Employee> result = employeeRepository.findByBadgeIdAndDeletedFalse("EMP001");

        assertTrue(result.isPresent());
        assertEquals("John Doe", result.get().getName());
    }

    @Test
    public void testFindByBadgeIdAndDeletedFalse_DeletedEmployee_ReturnsEmpty() {
        validEmployee.setDeleted(true);
        employeeRepository.save(validEmployee);

        Optional<Employee> result = employeeRepository.findByBadgeIdAndDeletedFalse("EMP001");

        assertFalse(result.isPresent());
    }

    @Test
    public void testFindAllByDeletedFalse_ReturnsOnlyActiveEmployees() {
        Employee deletedEmployee = new Employee();
        deletedEmployee.setName("Jane Doe");
        deletedEmployee.setBadgeId("EMP002");
        deletedEmployee.setRole(Role.SUPERVISOR);
        deletedEmployee.setDeleted(true);
        employeeRepository.save(deletedEmployee);

        Page<Employee> result = employeeRepository.findAllByDeletedFalse(PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
        assertEquals("John Doe", result.getContent().get(0).getName());
    }
}