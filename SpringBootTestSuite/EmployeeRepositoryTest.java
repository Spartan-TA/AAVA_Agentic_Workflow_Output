package SpringBootTestSuite;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

// Assume these imports exist
import com.example.ems.entity.Employee;
import com.example.ems.repository.EmployeeRepository;

@DataJpaTest
public class EmployeeRepositoryTest {
    @Autowired
    private EmployeeRepository employeeRepository;

    private Employee employee1;
    private Employee employee2;

    @BeforeEach
    public void setUp() {
        employee1 = new Employee();
        employee1.setName("John Doe");
        employee1.setBadgeId("EMP001");
        employee1.setRole("WORKER");
        employee1.setStatus("ACTIVE");
        employee1.setDeleted(false);

        employee2 = new Employee();
        employee2.setName("Jane Smith");
        employee2.setBadgeId("EMP002");
        employee2.setRole("SUPERVISOR");
        employee2.setStatus("INACTIVE");
        employee2.setDeleted(false);

        employeeRepository.save(employee1);
        employeeRepository.save(employee2);
    }

    @Test
    public void testFindByBadgeIdAndDeletedFalse_Found() {
        Optional<Employee> result = employeeRepository.findByBadgeIdAndDeletedFalse("EMP001");
        assertTrue(result.isPresent());
        assertEquals("John Doe", result.get().getName());
    }

    @Test
    public void testFindByBadgeIdAndDeletedFalse_NotFound() {
        Optional<Employee> result = employeeRepository.findByBadgeIdAndDeletedFalse("EMP999");
        assertFalse(result.isPresent());
    }

    @Test
    public void testFindAllByDeletedFalse() {
        List<Employee> result = employeeRepository.findAllByDeletedFalse();
        assertEquals(2, result.size());
    }

    @Test
    public void testFindAllByDeletedFalseAndStatus() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> result = employeeRepository.findAllByDeletedFalseAndStatus("ACTIVE", pageable);
        assertEquals(1, result.getTotalElements());
        assertEquals("John Doe", result.getContent().get(0).getName());
    }

    @Test
    public void testExistsByBadgeIdAndDeletedFalse_True() {
        boolean exists = employeeRepository.existsByBadgeIdAndDeletedFalse("EMP001");
        assertTrue(exists);
    }

    @Test
    public void testExistsByBadgeIdAndDeletedFalse_False() {
        boolean exists = employeeRepository.existsByBadgeIdAndDeletedFalse("EMP999");
        assertFalse(exists);
    }

    @Test
    public void testSoftDelete_EmployeeNotReturned() {
        employee1.setDeleted(true);
        employeeRepository.save(employee1);
        List<Employee> result = employeeRepository.findAllByDeletedFalse();
        assertEquals(1, result.size());
        assertEquals("Jane Smith", result.get(0).getName());
    }
}
