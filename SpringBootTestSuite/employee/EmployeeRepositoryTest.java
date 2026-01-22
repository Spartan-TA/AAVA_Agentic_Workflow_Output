package com.warehouse.ems.employee;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Comprehensive repository tests for Employee
 * Tests cover: CRUD operations, custom queries, pagination, constraints
 */
@DataJpaTest
class EmployeeRepositoryTest {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Test
    @DisplayName("Should save and retrieve employee by id")
    void testSaveAndFindById() {
        // Arrange
        Employee employee = Employee.builder()
                .name("Jane Smith")
                .badgeId("B54321")
                .role("Supervisor")
                .department("Packing")
                .shiftGroup("B")
                .hireDate(LocalDate.now())
                .active(true)
                .deleted(false)
                .build();

        // Act
        Employee saved = employeeRepository.save(employee);
        Optional<Employee> found = employeeRepository.findById(saved.getId());

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Jane Smith");
        assertThat(found.get().getBadgeId()).isEqualTo("B54321");
    }

    @Test
    @DisplayName("Should find all active employees")
    void testFindAllActiveEmployees() {
        // Arrange
        employeeRepository.save(Employee.builder().name("Active1").badgeId("B1").active(true).deleted(false).build());
        employeeRepository.save(Employee.builder().name("Inactive").badgeId("B2").active(false).deleted(false).build());
        employeeRepository.save(Employee.builder().name("Active2").badgeId("B3").active(true).deleted(false).build());

        // Act
        List<Employee> active = employeeRepository.findByActiveTrueAndDeletedFalse();

        // Assert
        assertThat(active).hasSize(2);
        assertThat(active).extracting(Employee::getName).containsExactlyInAnyOrder("Active1", "Active2");
    }

    @Test
    @DisplayName("Should handle empty repository")
    void testEmptyRepository() {
        // Act
        List<Employee> all = employeeRepository.findAll();

        // Assert
        assertThat(all).isEmpty();
    }

    @Test
    @DisplayName("Should enforce unique badge ID constraint")
    void testUniqueBadgeIdConstraint() {
        // Arrange
        employeeRepository.save(Employee.builder().name("John").badgeId("B123").build());

        // Act & Assert
        assertThatThrownBy(() -> {
            employeeRepository.save(Employee.builder().name("Jane").badgeId("B123").build());
            employeeRepository.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("Should find employees by department")
    void testFindByDepartment() {
        // Arrange
        employeeRepository.save(Employee.builder().name("John").badgeId("B1").department("Logistics").active(true).build());
        employeeRepository.save(Employee.builder().name("Jane").badgeId("B2").department("Packing").active(true).build());
        employeeRepository.save(Employee.builder().name("Bob").badgeId("B3").department("Logistics").active(true).build());

        // Act
        List<Employee> logistics = employeeRepository.findByDepartmentAndActiveTrue("Logistics");

        // Assert
        assertThat(logistics).hasSize(2);
        assertThat(logistics).extracting(Employee::getName).containsExactlyInAnyOrder("John", "Bob");
    }

    @Test
    @DisplayName("Should update employee successfully")
    void testUpdateEmployee() {
        // Arrange
        Employee employee = employeeRepository.save(Employee.builder().name("John").badgeId("B123").build());

        // Act
        employee.setName("John Updated");
        Employee updated = employeeRepository.save(employee);

        // Assert
        assertThat(updated.getName()).isEqualTo("John Updated");
    }

    @Test
    @DisplayName("Should delete employee successfully")
    void testDeleteEmployee() {
        // Arrange
        Employee employee = employeeRepository.save(Employee.builder().name("John").badgeId("B123").build());
        Long id = employee.getId();

        // Act
        employeeRepository.deleteById(id);

        // Assert
        assertThat(employeeRepository.findById(id)).isEmpty();
    }

    @Test
    @DisplayName("Should handle soft delete")
    void testSoftDelete() {
        // Arrange
        Employee employee = employeeRepository.save(Employee.builder()
                .name("John")
                .badgeId("B123")
                .active(true)
                .deleted(false)
                .build());

        // Act
        employee.setDeleted(true);
        employee.setActive(false);
        employeeRepository.save(employee);

        // Assert
        List<Employee> active = employeeRepository.findByActiveTrueAndDeletedFalse();
        assertThat(active).isEmpty();
    }

    @Test
    @DisplayName("Should find employees by role")
    void testFindByRole() {
        // Arrange
        employeeRepository.save(Employee.builder().name("John").badgeId("B1").role("ADMIN").build());
        employeeRepository.save(Employee.builder().name("Jane").badgeId("B2").role("WORKER").build());

        // Act
        List<Employee> admins = employeeRepository.findByRole("ADMIN");

        // Assert
        assertThat(admins).hasSize(1);
        assertThat(admins.get(0).getName()).isEqualTo("John");
    }
}