package SpringBootTestSuite;

import com.wms.ems.employee.model.Employee;
import com.wms.ems.employee.model.Role;
import com.wms.ems.employee.model.Status;
import com.wms.ems.employee.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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

@DataJpaTest
class EmployeeRepositoryTest {
    @Autowired
    EmployeeRepository employeeRepository;

    Employee employee1, employee2, employee3, deletedEmployee;

    @BeforeEach
    void setup() {
        employee1 = Employee.builder()
                .name("Alice")
                .badgeId("BADGE1")
                .role(Role.WORKER)
                .department("Logistics")
                .shiftGroup("A")
                .hireDate(LocalDate.now())
                .status(Status.ACTIVE)
                .deleted(false)
                .build();
        employee2 = Employee.builder()
                .name("Bob")
                .badgeId("BADGE2")
                .role(Role.SUPERVISOR)
                .department("Packing")
                .shiftGroup("B")
                .hireDate(LocalDate.now())
                .status(Status.INACTIVE)
                .deleted(false)
                .build();
        employee3 = Employee.builder()
                .name("Charlie")
                .badgeId("BADGE3")
                .role(Role.WORKER)
                .department("Logistics")
                .shiftGroup("A")
                .hireDate(LocalDate.now())
                .status(Status.ACTIVE)
                .deleted(false)
                .build();
        deletedEmployee = Employee.builder()
                .name("Deleted")
                .badgeId("BADGE4")
                .role(Role.WORKER)
                .department("Logistics")
                .shiftGroup("A")
                .hireDate(LocalDate.now())
                .status(Status.TERMINATED)
                .deleted(true)
                .build();
        employeeRepository.save(employee1);
        employeeRepository.save(employee2);
        employeeRepository.save(employee3);
        employeeRepository.save(deletedEmployee);
    }

    @Test
    @DisplayName("Find by badgeId returns employee")
    void testFindByBadgeId() {
        Optional<Employee> found = employeeRepository.findByBadgeId("BADGE1");
        assertTrue(found.isPresent());
        assertEquals("Alice", found.get().getName());
    }

    @Test
    @DisplayName("Find by badgeId and deleted false returns employee")
    void testFindByBadgeIdAndDeletedFalse() {
        Optional<Employee> found = employeeRepository.findByBadgeIdAndDeletedFalse("BADGE1");
        assertTrue(found.isPresent());
        assertEquals("Alice", found.get().getName());
        assertFalse(found.get().isDeleted());
    }

    @Test
    @DisplayName("Find by badgeId and deleted false returns empty for deleted")
    void testFindByBadgeIdAndDeletedFalseDeleted() {
        Optional<Employee> found = employeeRepository.findByBadgeIdAndDeletedFalse("BADGE4");
        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("Find by deleted false returns only non-deleted employees")
    void testFindByDeletedFalse() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> page = employeeRepository.findByDeletedFalse(pageable);
        assertEquals(3, page.getTotalElements());
    }

    @Test
    @DisplayName("Find by role and deleted false")
    void testFindByRoleAndDeletedFalse() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> page = employeeRepository.findByRoleAndDeletedFalse(Role.WORKER, pageable);
        assertEquals(2, page.getTotalElements());
    }

    @Test
    @DisplayName("Find by department and deleted false")
    void testFindByDepartmentAndDeletedFalse() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> page = employeeRepository.findByDepartmentAndDeletedFalse("Logistics", pageable);
        assertEquals(2, page.getTotalElements());
    }

    @Test
    @DisplayName("Find by status and deleted false")
    void testFindByStatusAndDeletedFalse() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> page = employeeRepository.findByStatusAndDeletedFalse(Status.ACTIVE, pageable);
        assertEquals(2, page.getTotalElements());
    }

    @Test
    @DisplayName("Search by name (case-insensitive, partial)")
    void testSearchByName() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> page = employeeRepository.searchByName("ali", pageable);
        assertEquals(1, page.getTotalElements());
        assertEquals("Alice", page.getContent().get(0).getName());
    }

    @Test
    @DisplayName("Find by shift group and deleted false")
    void testFindByShiftGroupAndDeletedFalse() {
        List<Employee> list = employeeRepository.findByShiftGroupAndDeletedFalse("A");
        assertEquals(2, list.size());
    }

    @Test
    @DisplayName("Exists by badgeId and deleted false returns true")
    void testExistsByBadgeIdAndDeletedFalse() {
        assertTrue(employeeRepository.existsByBadgeIdAndDeletedFalse("BADGE1"));
    }

    @Test
    @DisplayName("Exists by badgeId and deleted false returns false for deleted")
    void testExistsByBadgeIdAndDeletedFalseDeleted() {
        assertFalse(employeeRepository.existsByBadgeIdAndDeletedFalse("BADGE4"));
    }

    @Test
    @DisplayName("Count active by department")
    void testCountActiveByDepartment() {
        long count = employeeRepository.countActiveByDepartment("Logistics");
        assertEquals(2, count);
    }

    @Test
    @DisplayName("Find all active employees")
    void testFindAllActive() {
        List<Employee> list = employeeRepository.findAllActive();
        assertEquals(2, list.size());
        assertTrue(list.stream().allMatch(e -> e.getStatus() == Status.ACTIVE));
    }

    @Test
    @DisplayName("Find by badgeId returns empty for non-existent badge")
    void testFindByBadgeIdNotFound() {
        Optional<Employee> found = employeeRepository.findByBadgeId("NONEXIST");
        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("Find by department and deleted false returns empty for unknown department")
    void testFindByDepartmentAndDeletedFalseUnknown() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> page = employeeRepository.findByDepartmentAndDeletedFalse("Unknown", pageable);
        assertEquals(0, page.getTotalElements());
    }

    @Test
    @DisplayName("Search by empty name returns all non-deleted employees")
    void testSearchByEmptyName() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> page = employeeRepository.searchByName("", pageable);
        assertEquals(3, page.getTotalElements());
    }

    @Test
    @DisplayName("Pagination returns correct page size")
    void testPagination() {
        Pageable pageable = PageRequest.of(0, 2);
        Page<Employee> page = employeeRepository.findByDeletedFalse(pageable);
        assertEquals(2, page.getContent().size());
        assertEquals(3, page.getTotalElements());
    }

    @Test
    @DisplayName("Boundary test: badgeId length 50")
    void testBoundaryBadgeIdLength() {
        String badgeId = "B".repeat(50);
        Employee emp = Employee.builder().name("Boundary").badgeId(badgeId).role(Role.WORKER).department("Logistics").shiftGroup("A").hireDate(LocalDate.now()).status(Status.ACTIVE).deleted(false).build();
        employeeRepository.save(emp);
        assertTrue(employeeRepository.existsByBadgeIdAndDeletedFalse(badgeId));
    }
}
