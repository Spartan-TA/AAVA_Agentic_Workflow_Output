package com.wms.employee.repository;

import com.wms.employee.domain.Employee;
import com.wms.employee.domain.EmployeeStatus;
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

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
class EmployeeRepositoryTest {
    @Autowired
    private EmployeeRepository employeeRepository;

    private Employee employee1;
    private Employee employee2;
    private Employee employee3;

    @BeforeEach
    void setUp() {
        employee1 = new Employee();
        employee1.setBadgeId("B100");
        employee1.setName("Alice");
        employee1.setRole("Worker");
        employee1.setDepartment("Packing");
        employee1.setShiftGroup("A");
        employee1.setHireDate(LocalDate.of(2020, 1, 1));
        employee1.setStatus(EmployeeStatus.ACTIVE);
        employee1.setDeleted(false);

        employee2 = new Employee();
        employee2.setBadgeId("B101");
        employee2.setName("Bob");
        employee2.setRole("Supervisor");
        employee2.setDepartment("Shipping");
        employee2.setShiftGroup("B");
        employee2.setHireDate(LocalDate.of(2021, 2, 2));
        employee2.setStatus(EmployeeStatus.ON_LEAVE);
        employee2.setDeleted(false);

        employee3 = new Employee();
        employee3.setBadgeId("B102");
        employee3.setName("Charlie");
        employee3.setRole("Worker");
        employee3.setDepartment("Packing");
        employee3.setShiftGroup("A");
        employee3.setHireDate(LocalDate.of(2022, 3, 3));
        employee3.setStatus(EmployeeStatus.TERMINATED);
        employee3.setDeleted(true);

        employeeRepository.save(employee1);
        employeeRepository.save(employee2);
        employeeRepository.save(employee3);
    }

    @Test
    @DisplayName("testFindByBadgeId_ExistingBadgeId_ReturnsEmployee")
    void testFindByBadgeId_ExistingBadgeId_ReturnsEmployee() {
        Optional<Employee> found = employeeRepository.findByBadgeId("B100");
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Alice");
    }

    @Test
    @DisplayName("testFindByBadgeId_NonExistingBadgeId_ReturnsEmpty")
    void testFindByBadgeId_NonExistingBadgeId_ReturnsEmpty() {
        Optional<Employee> found = employeeRepository.findByBadgeId("B999");
        assertThat(found).isNotPresent();
    }

    @Test
    @DisplayName("testFindAllActive_OnlyNonDeletedReturned")
    void testFindAllActive_OnlyNonDeletedReturned() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> page = employeeRepository.findAllActive(pageable);
        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getContent()).extracting(Employee::getBadgeId).containsExactlyInAnyOrder("B100", "B101");
    }

    @Test
    @DisplayName("testFindByFilters_AllNullFilters_ReturnsAllActive")
    void testFindByFilters_AllNullFilters_ReturnsAllActive() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> page = employeeRepository.findByFilters(null, null, null, pageable);
        assertThat(page.getContent()).hasSize(2);
    }

    @Test
    @DisplayName("testFindByFilters_ByDepartment")
    void testFindByFilters_ByDepartment() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> page = employeeRepository.findByFilters("Packing", null, null, pageable);
        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getName()).isEqualTo("Alice");
    }

    @Test
    @DisplayName("testFindByFilters_ByRole")
    void testFindByFilters_ByRole() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> page = employeeRepository.findByFilters(null, "Supervisor", null, pageable);
        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getName()).isEqualTo("Bob");
    }

    @Test
    @DisplayName("testFindByFilters_ByStatus")
    void testFindByFilters_ByStatus() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> page = employeeRepository.findByFilters(null, null, EmployeeStatus.ON_LEAVE, pageable);
        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getName()).isEqualTo("Bob");
    }

    @Test
    @DisplayName("testFindByFilters_ByDepartmentAndRoleAndStatus")
    void testFindByFilters_ByDepartmentAndRoleAndStatus() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> page = employeeRepository.findByFilters("Packing", "Worker", EmployeeStatus.ACTIVE, pageable);
        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getName()).isEqualTo("Alice");
    }

    @Test
    @DisplayName("testFindByFilters_NoMatch_ReturnsEmpty")
    void testFindByFilters_NoMatch_ReturnsEmpty() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> page = employeeRepository.findByFilters("NonExist", null, null, pageable);
        assertThat(page.getContent()).isEmpty();
    }

    @Test
    @DisplayName("testFindByFilters_DeletedNotReturned")
    void testFindByFilters_DeletedNotReturned() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> page = employeeRepository.findByFilters(null, null, EmployeeStatus.TERMINATED, pageable);
        assertThat(page.getContent()).isEmpty();
    }
}
