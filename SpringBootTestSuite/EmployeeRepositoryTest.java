package SpringBootTestSuite;

import com.warehouse.employee_mgmt.domain.Employee;
import com.warehouse.employee_mgmt.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class EmployeeRepositoryTest {
    @Autowired
    private EmployeeRepository employeeRepository;

    private Employee employee;
    private UUID employeeId;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        employeeId = UUID.randomUUID();
        pageable = PageRequest.of(0, 10);
        employee = Employee.builder()
                .id(employeeId)
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
        employeeRepository.save(employee);
    }

    @Test
    @DisplayName("testFindByBadgeIdAndDeletedFalse_NormalCase_ReturnsEmployee")
    void testFindByBadgeIdAndDeletedFalse_NormalCase_ReturnsEmployee() {
        Optional<Employee> found = employeeRepository.findByBadgeIdAndDeletedFalse("BADGE123");
        assertTrue(found.isPresent());
        assertEquals("John Doe", found.get().getName());
    }

    @Test
    @DisplayName("testFindByBadgeIdAndDeletedFalse_DeletedEmployee_ReturnsEmpty")
    void testFindByBadgeIdAndDeletedFalse_DeletedEmployee_ReturnsEmpty() {
        employee.setDeleted(true);
        employeeRepository.save(employee);
        Optional<Employee> found = employeeRepository.findByBadgeIdAndDeletedFalse("BADGE123");
        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("testSearch_NormalCase_ReturnsPage")
    void testSearch_NormalCase_ReturnsPage() {
        Page<Employee> page = employeeRepository.search("John", pageable);
        assertEquals(1, page.getTotalElements());
        assertEquals("John Doe", page.getContent().get(0).getName());
    }

    @Test
    @DisplayName("testSearch_Boundary_EmptySearch_ReturnsAll")
    void testSearch_Boundary_EmptySearch_ReturnsAll() {
        Page<Employee> page = employeeRepository.search("", pageable);
        assertEquals(1, page.getTotalElements());
    }

    @Test
    @DisplayName("testFindAllByDeletedFalse_NormalCase_ReturnsPage")
    void testFindAllByDeletedFalse_NormalCase_ReturnsPage() {
        Page<Employee> page = employeeRepository.findAllByDeletedFalse(pageable);
        assertEquals(1, page.getTotalElements());
    }

    @Test
    @DisplayName("testFindByDepartmentAndDeletedFalse_NormalCase_ReturnsPage")
    void testFindByDepartmentAndDeletedFalse_NormalCase_ReturnsPage() {
        Page<Employee> page = employeeRepository.findByDepartmentAndDeletedFalse("Logistics", pageable);
        assertEquals(1, page.getTotalElements());
    }

    @Test
    @DisplayName("testFindByStatusAndDeletedFalse_NormalCase_ReturnsPage")
    void testFindByStatusAndDeletedFalse_NormalCase_ReturnsPage() {
        Page<Employee> page = employeeRepository.findByStatusAndDeletedFalse("ACTIVE", pageable);
        assertEquals(1, page.getTotalElements());
    }

    @Test
    @DisplayName("testFindByRoleAndDeletedFalse_NormalCase_ReturnsPage")
    void testFindByRoleAndDeletedFalse_NormalCase_ReturnsPage() {
        Page<Employee> page = employeeRepository.findByRoleAndDeletedFalse("WORKER", pageable);
        assertEquals(1, page.getTotalElements());
    }

    @Test
    @DisplayName("testCountByDepartmentAndDeletedFalse_NormalCase_ReturnsCount")
    void testCountByDepartmentAndDeletedFalse_NormalCase_ReturnsCount() {
        long count = employeeRepository.countByDepartmentAndDeletedFalse("Logistics");
        assertEquals(1, count);
    }

    @Test
    @DisplayName("testFindByTenantIdAndDeletedFalse_NormalCase_ReturnsPage")
    void testFindByTenantIdAndDeletedFalse_NormalCase_ReturnsPage() {
        Page<Employee> page = employeeRepository.findByTenantIdAndDeletedFalse(employee.getTenantId(), pageable);
        assertEquals(1, page.getTotalElements());
    }

    @Test
    @DisplayName("testExistsByBadgeIdAndDeletedFalse_NormalCase_ReturnsTrue")
    void testExistsByBadgeIdAndDeletedFalse_NormalCase_ReturnsTrue() {
        boolean exists = employeeRepository.existsByBadgeIdAndDeletedFalse("BADGE123");
        assertTrue(exists);
    }

    @Test
    @DisplayName("testExistsByBadgeIdAndDeletedFalse_DeletedEmployee_ReturnsFalse")
    void testExistsByBadgeIdAndDeletedFalse_DeletedEmployee_ReturnsFalse() {
        employee.setDeleted(true);
        employeeRepository.save(employee);
        boolean exists = employeeRepository.existsByBadgeIdAndDeletedFalse("BADGE123");
        assertFalse(exists);
    }

    // Boundary and edge cases
    @Test
    @DisplayName("testFindByDepartmentAndDeletedFalse_Boundary_EmptyDepartment_ReturnsEmpty")
    void testFindByDepartmentAndDeletedFalse_Boundary_EmptyDepartment_ReturnsEmpty() {
        Page<Employee> page = employeeRepository.findByDepartmentAndDeletedFalse("", pageable);
        assertEquals(0, page.getTotalElements());
    }

    @Test
    @DisplayName("testFindByStatusAndDeletedFalse_Boundary_InvalidStatus_ReturnsEmpty")
    void testFindByStatusAndDeletedFalse_Boundary_InvalidStatus_ReturnsEmpty() {
        Page<Employee> page = employeeRepository.findByStatusAndDeletedFalse("INVALID", pageable);
        assertEquals(0, page.getTotalElements());
    }
}
