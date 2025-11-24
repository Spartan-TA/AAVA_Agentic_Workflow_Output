package com.warehouse.employee.management.service;

import com.warehouse.employee.management.model.Employee;
import com.warehouse.employee.management.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

/**
 * Service layer for Employee business logic.
 */
@Service
public class EmployeeService {
    @Autowired
    private EmployeeRepository employeeRepository;

    public Page<Employee> getAllEmployees(Pageable pageable) {
        return employeeRepository.findAll(pageable);
    }

    public Optional<Employee> getEmployeeById(Long id) {
        return employeeRepository.findById(id);
    }

    public Optional<Employee> getEmployeeByBadgeId(String badgeId) {
        return employeeRepository.findByBadgeId(badgeId);
    }

    public Employee createEmployee(Employee employee) {
        return employeeRepository.save(employee);
    }

    public Employee updateEmployee(Long id, Employee updated) {
        return employeeRepository.findById(id)
            .map(emp -> {
                emp.setName(updated.getName());
                emp.setRole(updated.getRole());
                emp.setDepartment(updated.getDepartment());
                emp.setShiftGroup(updated.getShiftGroup());
                emp.setHireDate(updated.getHireDate());
                emp.setStatus(updated.getStatus());
                return employeeRepository.save(emp);
            }).orElseThrow(() -> new RuntimeException("Employee not found"));
    }

    @Transactional
    public void softDeleteEmployee(Long id) {
        employeeRepository.findById(id).ifPresent(emp -> {
            emp.setDeleted(true);
            employeeRepository.save(emp);
        });
    }
}
