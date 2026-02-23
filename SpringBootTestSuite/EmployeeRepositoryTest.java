package com.example.repository;

import com.example.entity.Employee;
import com.example.entity.EmployeeStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.*;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class EmployeeRepositoryTest {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Test
    void findByBadgeId_returnsEmployee() {
        Employee emp = new Employee();
        emp.setBadgeId("B123");
        employeeRepository.save(emp);

        Optional<Employee> found = employeeRepository.findByBadgeId("B123");
        assertTrue(found.isPresent());
        assertEquals("B123", found.get().getBadgeId());
    }

    @Test
    void findByDepartment_returnsList() {
        Employee emp1 = new Employee();
        emp1.setDepartment("IT");
        Employee emp2 = new Employee();
        emp2.setDepartment("IT");
        employeeRepository.save(emp1);
        employeeRepository.save(emp2);

        List<Employee> list = employeeRepository.findByDepartment("IT");
        assertEquals(2, list.size());
    }

    @Test
    void findByStatus_returnsList() {
        Employee emp = new Employee();
        emp.setStatus(EmployeeStatus.ACTIVE);
        employeeRepository.save(emp);

        List<Employee> list = employeeRepository.findByStatus(EmployeeStatus.ACTIVE);
        assertFalse(list.isEmpty());
    }

    @Test
    void findByRoleAndStatus_withPagination() {
        Employee emp = new Employee();
        emp.setRole("ENGINEER");
        emp.setStatus(EmployeeStatus.ACTIVE);
        employeeRepository.save(emp);

        Page<Employee> page = employeeRepository.findByRoleAndStatus("ENGINEER", EmployeeStatus.ACTIVE, PageRequest.of(0, 10));
        assertEquals(1, page.getTotalElements());
    }
}