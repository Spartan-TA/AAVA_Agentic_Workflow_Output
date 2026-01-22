package com.warehouse.ems.repository;

import com.warehouse.ems.entity.Employee;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
class EmployeeRepositoryTest {
    @Autowired
    private EmployeeRepository employeeRepository;

    private Employee employee1, employee2, employee3;

    @BeforeEach
    void setUp() {
        employee1 = Employee.builder()
                .badgeId("B001")
                .name("Alice")
                .role("Worker")
                .department("Packing")
                .shiftGroup("A")
                .hireDate(LocalDate.of(2020, 1, 1))
                .status("Active")
                .deleted(false)
                .build();
        employee2 = Employee.builder()
                .badgeId("B002")
                .name("Bob")
                .role("Supervisor")
                .department("Shipping")
                .shiftGroup("B")
                .hireDate(LocalDate.of(2021, 2, 2))
                .status("Active")
                .deleted(false)
                .build();
        employee3 = Employee.builder()
                .badgeId("B003")
                .name("Charlie")
                .role("Worker")
                .department("Packing")
                .shiftGroup("A")
                .hireDate(LocalDate.of(2022, 3, 3))
                .status("Inactive")
                .deleted(true)
                .build();
        employeeRepository.save(employee1);
        employeeRepository.save(employee2);
        employeeRepository.save(employee3);
    }

    @Test
    void testFindByBadgeIdAndDeletedFalse_ExistingEmployee_ReturnsEmployee() {
        Optional<Employee> found = employeeRepository.findByBadgeIdAndDeletedFalse("B001");
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Alice");
    }

    @Test
    void testFindByBadgeIdAndDeletedFalse_SoftDeletedEmployee_ReturnsEmpty() {
        Optional<Employee> found = employeeRepository.findByBadgeIdAndDeletedFalse("B003");
        assertThat(found).isNotPresent();
    }

    @Test
    void testFindByDepartment_ValidDepartment_ReturnsEmployees() {
        List<Employee> packingEmployees = employeeRepository.findByDepartment("Packing");
        assertThat(packingEmployees).hasSize(1);
        assertThat(packingEmployees.get(0).getName()).isEqualTo("Alice");
    }

    @Test
    void testSave_NewEmployee_Success() {
        Employee emp = Employee.builder()
                .badgeId("B004")
                .name("David")
                .role("Worker")
                .department("Packing")
                .shiftGroup("C")
                .hireDate(LocalDate.of(2023, 4, 4))
                .status("Active")
                .deleted(false)
                .build();
        Employee saved = employeeRepository.save(emp);
        assertThat(saved.getId()).isNotNull();
        Optional<Employee> found = employeeRepository.findByBadgeIdAndDeletedFalse("B004");
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("David");
    }
}
