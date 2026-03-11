package SpringBootTestSuite;

import com.warehouse.modules.employee.entity.Employee;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class EmployeeRepositoryTest {
    @Autowired
    EmployeeRepository employeeRepository;

    // Minimal stub for EmployeeRole and EmployeeStatus
    enum EmployeeRole { ADMIN, HR, SUPERVISOR, WORKER }
    enum EmployeeStatus { ACTIVE, TERMINATED }

    Employee buildEmployee(String badgeId, boolean deleted) {
        return Employee.builder()
                .badgeId(badgeId)
                .name("Name")
                .role(EmployeeRole.ADMIN)
                .department("Dept")
                .shiftGroup("A")
                .hireDate(LocalDate.now())
                .status(EmployeeStatus.ACTIVE)
                .password("pw")
                .deleted(deleted)
                .build();
    }

    @Test
    @DisplayName("findByBadgeId returns employee if exists")
    void findByBadgeId_found() {
        Employee emp = buildEmployee("B1", false);
        employeeRepository.save(emp);
        assertTrue(employeeRepository.findByBadgeId("B1").isPresent());
    }

    @Test
    @DisplayName("findByBadgeId returns empty if not exists")
    void findByBadgeId_notFound() {
        assertTrue(employeeRepository.findByBadgeId("NOPE").isEmpty());
    }

    @Test
    @DisplayName("findAllByDeletedFalse returns only non-deleted employees")
    void findAllByDeletedFalse() {
        employeeRepository.save(buildEmployee("B2", false));
        employeeRepository.save(buildEmployee("B3", true));
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> page = employeeRepository.findAllByDeletedFalse(pageable);
        assertEquals(1, page.getTotalElements());
        assertEquals("B2", page.getContent().get(0).getBadgeId());
    }

    @Test
    @DisplayName("existsByBadgeId returns true if exists")
    void existsByBadgeId_true() {
        employeeRepository.save(buildEmployee("B4", false));
        assertTrue(employeeRepository.existsByBadgeId("B4"));
    }

    @Test
    @DisplayName("existsByBadgeId returns false if not exists")
    void existsByBadgeId_false() {
        assertFalse(employeeRepository.existsByBadgeId("NOPE2"));
    }
}

// Minimal stub for EmployeeRepository
interface EmployeeRepository extends org.springframework.data.jpa.repository.JpaRepository<Employee, Long> {
    java.util.Optional<Employee> findByBadgeId(String badgeId);
    org.springframework.data.domain.Page<Employee> findAllByDeletedFalse(org.springframework.data.domain.Pageable pageable);
    boolean existsByBadgeId(String badgeId);
}
