package com.company.warehouse.employee.repository;

import com.company.warehouse.employee.entity.Employee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class EmployeeRepositoryTest {

    @Autowired
    private EmployeeRepository employeeRepository;

    private Employee employee1;
    private Employee employee2;
    private Employee deletedEmployee;

    @BeforeEach
    void setUp() {
        employeeRepository.deleteAll();
        employee1 = Employee.builder()
                .badgeId("B123456")
                .name("John Doe")
                .role("Worker")
                .department("Logistics")
                .shiftGroup("A")
                .hireDate(LocalDate.of(2020, 1, 1))
                .status("ACTIVE")
                .deleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        employee2 = Employee.builder()
                .badgeId("B654321")
                .name("Jane Smith")
                .role("Manager")
                .department("HR")
                .shiftGroup("B")
                .hireDate(LocalDate.of(2019, 5, 10))
                .status("INACTIVE")
                .deleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        deletedEmployee = Employee.builder()
                .badgeId("B999999")
                .name("Deleted Emp")
                .role("Worker")
                .department("Logistics")
                .shiftGroup("C")
                .hireDate(LocalDate.of(2018, 3, 15))
                .status("INACTIVE")
                .deleted(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        employeeRepository.save(employee1);
        employeeRepository.save(employee2);
        employeeRepository.save(deletedEmployee);
    }

    @Test
    @DisplayName("findByBadgeIdAndDeletedFalse returns employee when not deleted")
    void findByBadgeIdAndDeletedFalse_employeeExistsAndNotDeleted_returnsEmployee() {
        Optional<Employee> found = employeeRepository.findByBadgeIdAndDeletedFalse("B123456");
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("John Doe");
    }

    @Test
    @DisplayName("findByBadgeIdAndDeletedFalse returns empty when employee is deleted")
    void findByBadgeIdAndDeletedFalse_employeeDeleted_returnsEmpty() {
        Optional<Employee> found = employeeRepository.findByBadgeIdAndDeletedFalse("B999999");
        assertThat(found).isNotPresent();
    }

    @Test
    @DisplayName("findByBadgeIdAndDeletedFalse returns empty when badgeId does not exist")
    void findByBadgeIdAndDeletedFalse_badgeIdNotExist_returnsEmpty() {
        Optional<Employee> found = employeeRepository.findByBadgeIdAndDeletedFalse("B000000");
        assertThat(found).isNotPresent();
    }

    @Test
    @DisplayName("findAllByDeletedFalse returns only not deleted employees, paginated")
    void findAllByDeletedFalse_returnsOnlyNotDeletedEmployees() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> page = employeeRepository.findAllByDeletedFalse(pageable);
        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent()).extracting(Employee::getBadgeId)
                .containsExactlyInAnyOrder("B123456", "B654321");
    }

    @Test
    @DisplayName("existsByBadgeIdAndDeletedFalse returns true if employee exists and not deleted")
    void existsByBadgeIdAndDeletedFalse_employeeExistsAndNotDeleted_returnsTrue() {
        boolean exists = employeeRepository.existsByBadgeIdAndDeletedFalse("B654321");
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("existsByBadgeIdAndDeletedFalse returns false if employee is deleted")
    void existsByBadgeIdAndDeletedFalse_employeeDeleted_returnsFalse() {
        boolean exists = employeeRepository.existsByBadgeIdAndDeletedFalse("B999999");
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("existsByBadgeIdAndDeletedFalse returns false if badgeId does not exist")
    void existsByBadgeIdAndDeletedFalse_badgeIdNotExist_returnsFalse() {
        boolean exists = employeeRepository.existsByBadgeIdAndDeletedFalse("B000000");
        assertThat(exists).isFalse();
    }
}
