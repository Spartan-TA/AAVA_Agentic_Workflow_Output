import com.warehouse.ems.employee.entity.Employee;
import com.warehouse.ems.employee.repository.EmployeeRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class EmployeeRepositoryTest {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Test
    void testFindByBadgeId() {
        Employee employee = new Employee();
        employee.setBadgeId("12345");
        employee.setName("John Doe");
        employee.setEmail("john.doe@example.com");
        employee.setPhone("123-456-7890");
        employee.setRole("Manager");
        employee.setDepartment("Operations");
        employee.setHireDate(LocalDate.of(2020, 1, 1));
        employee.setStatus("Active");
        employee.setDeleted(false);

        employeeRepository.save(employee);

        Optional<Employee> result = employeeRepository.findByBadgeId("12345");

        assertTrue(result.isPresent());
        assertEquals("12345", result.get().getBadgeId());
    }

    @Test
    void testFindByDeletedFalse() {
        Employee employee1 = new Employee();
        employee1.setBadgeId("12345");
        employee1.setName("John Doe");
        employee1.setEmail("john.doe@example.com");
        employee1.setPhone("123-456-7890");
        employee1.setRole("Manager");
        employee1.setDepartment("Operations");
        employee1.setHireDate(LocalDate.of(2020, 1, 1));
        employee1.setStatus("Active");
        employee1.setDeleted(false);

        Employee employee2 = new Employee();
        employee2.setBadgeId("67890");
        employee2.setName("Jane Doe");
        employee2.setEmail("jane.doe@example.com");
        employee2.setPhone("987-654-3210");
        employee2.setRole("Supervisor");
        employee2.setDepartment("Logistics");
        employee2.setHireDate(LocalDate.of(2019, 5, 15));
        employee2.setStatus("Active");
        employee2.setDeleted(true);

        employeeRepository.save(employee1);
        employeeRepository.save(employee2);

        List<Employee> result = employeeRepository.findByDeletedFalse();

        assertEquals(1, result.size());
        assertEquals("12345", result.get(0).getBadgeId());
    }

    @Test
    void testExistsByBadgeId() {
        Employee employee = new Employee();
        employee.setBadgeId("12345");
        employee.setName("John Doe");
        employee.setEmail("john.doe@example.com");
        employee.setPhone("123-456-7890");
        employee.setRole("Manager");
        employee.setDepartment("Operations");
        employee.setHireDate(LocalDate.of(2020, 1, 1));
        employee.setStatus("Active");
        employee.setDeleted(false);

        employeeRepository.save(employee);

        boolean exists = employeeRepository.existsByBadgeId("12345");

        assertTrue(exists);
    }

    @Test
    void testFindByDepartmentAndDeletedFalse() {
        Employee employee = new Employee();
        employee.setBadgeId("12345");
        employee.setName("John Doe");
        employee.setEmail("john.doe@example.com");
        employee.setPhone("123-456-7890");
        employee.setRole("Manager");
        employee.setDepartment("Operations");
        employee.setHireDate(LocalDate.of(2020, 1, 1));
        employee.setStatus("Active");
        employee.setDeleted(false);

        employeeRepository.save(employee);

        List<Employee> result = employeeRepository.findByDepartmentAndDeletedFalse("Operations");

        assertEquals(1, result.size());
        assertEquals("Operations", result.get(0).getDepartment());
    }
}