package SpringBootTestSuite;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

import com.example.warehouse.entity.Employee;
import com.example.warehouse.repository.EmployeeRepository;

@DataJpaTest
class EmployeeRepositoryTest {
    @Autowired
    private EmployeeRepository employeeRepository;

    private Employee employee1;
    private Employee employee2;
    private Employee employee3;

    @BeforeEach
    void setUp() {
        employeeRepository.deleteAll();
        employee1 = new Employee();
        employee1.setName("Alice Smith");
        employee1.setBadgeId("ABCD1234");
        employee1.setRole("Worker");
        employee1.setDepartment("Logistics");
        employee1.setShiftGroup("A");
        employee1.setHireDate(LocalDate.now().minusDays(10));
        employee1.setStatus("ACTIVE");
        employee1.setDeleted(false);
        employee1.setEmail("alice.smith@example.com");
        employee1.setPhone("+12345678901");

        employee2 = new Employee();
        employee2.setName("Bob Johnson");
        employee2.setBadgeId("EFGH5678");
        employee2.setRole("Manager");
        employee2.setDepartment("Admin");
        employee2.setShiftGroup("B");
        employee2.setHireDate(LocalDate.now().minusDays(20));
        employee2.setStatus("ON_LEAVE");
        employee2.setDeleted(false);
        employee2.setEmail("bob.johnson@example.com");
        employee2.setPhone("+19876543210");

        employee3 = new Employee();
        employee3.setName("Charlie Brown");
        employee3.setBadgeId("IJKL9012");
        employee3.setRole("Worker");
        employee3.setDepartment("Logistics");
        employee3.setShiftGroup("A");
        employee3.setHireDate(LocalDate.now().minusDays(5));
        employee3.setStatus("ACTIVE");
        employee3.setDeleted(true); // Soft deleted
        employee3.setEmail("charlie.brown@example.com");
        employee3.setPhone("+11223344556");

        employeeRepository.save(employee1);
        employeeRepository.save(employee2);
        employeeRepository.save(employee3);
    }

    @Test
    void testFindByBadgeIdAndDeletedFalse_Exists_ReturnsEmployee() {
        Optional<Employee> found = employeeRepository.findByBadgeIdAndDeletedFalse("ABCD1234");
        assertTrue(found.isPresent());
        assertEquals("Alice Smith", found.get().getName());
    }

    @Test
    void testFindByBadgeIdAndDeletedFalse_NotExists_ReturnsEmpty() {
        Optional<Employee> found = employeeRepository.findByBadgeIdAndDeletedFalse("ZZZZ9999");
        assertFalse(found.isPresent());
    }

    @Test
    void testFindByBadgeIdAndDeletedFalse_Deleted_ReturnsEmpty() {
        Optional<Employee> found = employeeRepository.findByBadgeIdAndDeletedFalse("IJKL9012");
        assertFalse(found.isPresent());
    }

    @Test
    void testFindAllByDeletedFalse_Pagination_FullPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> page = employeeRepository.findAllByDeletedFalse(pageable);
        assertEquals(2, page.getTotalElements());
        assertTrue(page.getContent().stream().allMatch(e -> !e.isDeleted()));
    }

    @Test
    void testFindAllByDeletedFalse_Pagination_EmptyPage() {
        employeeRepository.deleteAll();
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> page = employeeRepository.findAllByDeletedFalse(pageable);
        assertEquals(0, page.getTotalElements());
    }

    @Test
    void testFindByDepartmentAndDeletedFalse_Exists_ReturnsEmployees() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> page = employeeRepository.findByDepartmentAndDeletedFalse("Logistics", pageable);
        assertEquals(1, page.getTotalElements());
        assertEquals("Alice Smith", page.getContent().get(0).getName());
    }

    @Test
    void testFindByDepartmentAndDeletedFalse_NotExists_ReturnsEmpty() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> page = employeeRepository.findByDepartmentAndDeletedFalse("Finance", pageable);
        assertEquals(0, page.getTotalElements());
    }

    @Test
    void testFindByStatusAndDeletedFalse_Active_ReturnsEmployees() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> page = employeeRepository.findByStatusAndDeletedFalse("ACTIVE", pageable);
        assertEquals(1, page.getTotalElements());
        assertEquals("Alice Smith", page.getContent().get(0).getName());
    }

    @Test
    void testFindByStatusAndDeletedFalse_OnLeave_ReturnsEmployees() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> page = employeeRepository.findByStatusAndDeletedFalse("ON_LEAVE", pageable);
        assertEquals(1, page.getTotalElements());
        assertEquals("Bob Johnson", page.getContent().get(0).getName());
    }

    @Test
    void testFindByRoleAndDeletedFalse_Worker_ReturnsEmployees() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> page = employeeRepository.findByRoleAndDeletedFalse("Worker", pageable);
        assertEquals(1, page.getTotalElements());
        assertEquals("Alice Smith", page.getContent().get(0).getName());
    }

    @Test
    void testSearchByName_ExactMatch_ReturnsEmployee() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> page = employeeRepository.searchByName("Alice Smith", pageable);
        assertEquals(1, page.getTotalElements());
        assertEquals("Alice Smith", page.getContent().get(0).getName());
    }

    @Test
    void testSearchByName_PartialMatch_ReturnsEmployee() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> page = employeeRepository.searchByName("Alice", pageable);
        assertEquals(1, page.getTotalElements());
        assertEquals("Alice Smith", page.getContent().get(0).getName());
    }

    @Test
    void testSearchByName_NoMatch_ReturnsEmpty() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> page = employeeRepository.searchByName("Zachary", pageable);
        assertEquals(0, page.getTotalElements());
    }

    @Test
    void testFindByDepartmentAndShiftGroup_ReturnsEmployees() {
        List<Employee> list = employeeRepository.findByDepartmentAndShiftGroup("Logistics", "A");
        assertEquals(1, list.size());
        assertEquals("Alice Smith", list.get(0).getName());
    }

    @Test
    void testFindByDepartmentAndShiftGroup_NoMatch_ReturnsEmpty() {
        List<Employee> list = employeeRepository.findByDepartmentAndShiftGroup("Admin", "C");
        assertEquals(0, list.size());
    }

    @Test
    void testCountActiveByDepartment_Logistics_ReturnsCount() {
        long count = employeeRepository.countActiveByDepartment("Logistics");
        assertEquals(1, count);
    }

    @Test
    void testCountActiveByDepartment_Admin_ReturnsZero() {
        long count = employeeRepository.countActiveByDepartment("Admin");
        assertEquals(0, count);
    }

    @Test
    void testExistsByBadgeIdAndIdNot_Exists_ReturnsTrue() {
        boolean exists = employeeRepository.existsByBadgeIdAndIdNot("ABCD1234", employee2.getId());
        assertTrue(exists);
    }

    @Test
    void testExistsByBadgeIdAndIdNot_NotExists_ReturnsFalse() {
        boolean exists = employeeRepository.existsByBadgeIdAndIdNot("ZZZZ9999", employee1.getId());
        assertFalse(exists);
    }

    @Test
    void testFindAllByStatusAndDeletedFalse_Active_ReturnsEmployees() {
        List<Employee> list = employeeRepository.findAllByStatusAndDeletedFalse("ACTIVE");
        assertEquals(1, list.size());
        assertEquals("Alice Smith", list.get(0).getName());
    }

    @Test
    void testFindAllByStatusAndDeletedFalse_OnLeave_ReturnsEmployees() {
        List<Employee> list = employeeRepository.findAllByStatusAndDeletedFalse("ON_LEAVE");
        assertEquals(1, list.size());
        assertEquals("Bob Johnson", list.get(0).getName());
    }

    @Test
    void testFindAllByStatusAndDeletedFalse_NoMatch_ReturnsEmpty() {
        List<Employee> list = employeeRepository.findAllByStatusAndDeletedFalse("SUSPENDED");
        assertEquals(0, list.size());
    }

    @Test
    void testPagination_LastPage_ReturnsEmpty() {
        Pageable pageable = PageRequest.of(1, 2); // Only 2 non-deleted employees
        Page<Employee> page = employeeRepository.findAllByDeletedFalse(pageable);
        assertEquals(0, page.getContent().size());
    }
}
