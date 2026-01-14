package com.example.warehouse.service;

import com.example.warehouse.entity.Employee;
import com.example.warehouse.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Service for Employee operations.
 */
@Service
public class EmployeeService {
    @Autowired
    private EmployeeRepository employeeRepository;

    public Page<Employee> getAllActiveEmployees(Pageable pageable) {
        return employeeRepository.findAllActive(pageable);
    }

    public Optional<Employee> getEmployeeById(Long id) {
        return employeeRepository.findById(id);
    }

    public Optional<Employee> getEmployeeByBadgeId(String badgeId) {
        return employeeRepository.findByBadgeId(badgeId);
    }

    @Transactional
    public Employee createEmployee(Employee employee) {
        return employeeRepository.save(employee);
    }

    @Transactional
    public Employee updateEmployee(Long id, Employee updated) {
        Employee employee = employeeRepository.findById(id).orElseThrow();
        employee.setName(updated.getName());
        employee.setRole(updated.getRole());
        employee.setDepartment(updated.getDepartment());
        employee.setShiftGroup(updated.getShiftGroup());
        employee.setHireDate(updated.getHireDate());
        employee.setStatus(updated.getStatus());
        return employeeRepository.save(employee);
    }

    @Transactional
    public void softDeleteEmployee(Long id) {
        Employee employee = employeeRepository.findById(id).orElseThrow();
        employee.setStatus("DELETED");
        employeeRepository.save(employee);
    }

    public Page<Employee> filterByDepartment(String department, Pageable pageable) {
        return employeeRepository.findByDepartment(department, pageable);
    }
}
