package com.example.warehouse.repository;

import com.example.warehouse.entity.Employee;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
class EmployeeRepositoryTest {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Test
    void testFindByBadgeId_ExistingBadge_ReturnsEmployee() {
        Employee employee = new Employee();
        employee.setBadgeId("BADGE123");
        employee.setDepartment("Logistics");
        employee.setSoftDeleted(false);
        employeeRepository.save(employee);

        Optional<Employee> found = employeeRepository.findByBadgeId("BADGE123");

        assertThat(found).isPresent();
        assertThat(found.get().getBadgeId()).isEqualTo("BADGE123");
    }

    @Test
    void testFindByBadgeId_NonExistingBadge_ReturnsEmpty() {
        Optional<Employee> found = employeeRepository.findByBadgeId("NONEXISTENT");
        assertThat(found).isNotPresent();
    }

    @Test
    void testFindByDepartment_ExistingDepartment_ReturnsEmployees() {
        Employee employee1 = new Employee();
        employee1.setBadgeId("BADGE1");
        employee1.setDepartment("Logistics");
        employee1.setSoftDeleted(false);

        Employee employee2 = new Employee();
        employee2.setBadgeId("BADGE2");
        employee2.setDepartment("Logistics");
        employee2.setSoftDeleted(false);

        employeeRepository.save(employee1);
        employeeRepository.save(employee2);

        List<Employee> employees = employeeRepository.findByDepartment("Logistics");

        assertThat(employees).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    void testFindBySoftDeletedFalse_ReturnsOnlyActiveEmployees() {
        Employee active = new Employee();
        active.setBadgeId("ACTIVE");
        active.setSoftDeleted(false);

        Employee deleted = new Employee();
        deleted.setBadgeId("DELETED");
        deleted.setSoftDeleted(true);

        employeeRepository.save(active);
        employeeRepository.save(deleted);

        List<Employee> employees = employeeRepository.findBySoftDeletedFalse();

        assertThat(employees).extracting(Employee::getBadgeId).contains("ACTIVE");
        assertThat(employees).extracting(Employee::getBadgeId).doesNotContain("DELETED");
    }

    @Test
    void testSave_ValidEmployee_Success() {
        Employee employee = new Employee();
        employee.setBadgeId("VALID");
        employee.setDepartment("IT");
        employee.setSoftDeleted(false);

        Employee saved = employeeRepository.save(employee);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getBadgeId()).isEqualTo("VALID");
    }

    @Test
    void testSave_NullBadgeId_ThrowsException() {
        Employee employee = new Employee();
        employee.setDepartment("IT");
        employee.setSoftDeleted(false);

        assertThatThrownBy(() -> employeeRepository.save(employee))
                .isInstanceOf(Exception.class);
    }
}