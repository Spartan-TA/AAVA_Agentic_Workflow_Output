package com.example.warehouse.service;

import com.example.warehouse.entity.Employee;
import com.example.warehouse.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.Optional;

@Service
public class EmployeeService {
    private final EmployeeRepository employeeRepository;

    @Autowired
    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    public Page<Employee> getAllEmployees(Pageable pageable) {
        return employeeRepository.findAll(pageable);
    }

    public Page<Employee> filterByDepartment(String department, Pageable pageable) {
        return employeeRepository.findByDepartment(department, pageable);
    }

    public Page<Employee> filterByStatus(String status, Pageable pageable) {
        return employeeRepository.findByStatus(status, pageable);
    }

    public Employee getEmployeeById(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found"));
    }

    public Employee createEmployee(Employee employee) {
        if (employeeRepository.findByBadgeId(employee.getBadgeId()) != null) {
            throw new IllegalArgumentException("Badge ID must be unique");
        }
        return employeeRepository.save(employee);
    }

    public Employee updateEmployee(Long id, Employee updatedEmployee) {
        Employee existing = getEmployeeById(id);
        if (!existing.getBadgeId().equals(updatedEmployee.getBadgeId()) &&
                employeeRepository.findByBadgeId(updatedEmployee.getBadgeId()) != null) {
            throw new IllegalArgumentException("Badge ID must be unique");
        }
        updatedEmployee.setId(id);
        return employeeRepository.save(updatedEmployee);
    }

    @Transactional
    public void softDeleteEmployee(Long id) {
        Employee employee = getEmployeeById(id);
        employee.setStatus("INACTIVE");
        employeeRepository.save(employee);
    }

    public void deleteEmployee(Long id) {
        employeeRepository.deleteById(id);
    }
}
