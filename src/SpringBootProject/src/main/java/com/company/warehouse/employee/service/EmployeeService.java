package com.company.warehouse.employee.service;

import com.company.warehouse.employee.entity.Employee;
import com.company.warehouse.employee.repository.EmployeeRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EmployeeService {
    private final EmployeeRepository employeeRepository;

    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    public Optional<Employee> getEmployeeById(Long id) {
        return employeeRepository.findById(id);
    }

    public Optional<Employee> getEmployeeByBadgeId(String badgeId) {
        return employeeRepository.findByBadgeId(badgeId);
    }

    @Transactional
    public Employee createEmployee(Employee employee) {
        if (employeeRepository.existsByBadgeId(employee.getBadgeId())) {
            throw new IllegalArgumentException("Badge ID already exists");
        }
        return employeeRepository.save(employee);
    }

    @Transactional
    public Employee updateEmployee(Long id, Employee updatedEmployee) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found"));
        employee.setName(updatedEmployee.getName());
        employee.setRole(updatedEmployee.getRole());
        employee.setDepartment(updatedEmployee.getDepartment());
        employee.setShiftGroup(updatedEmployee.getShiftGroup());
        employee.setHireDate(updatedEmployee.getHireDate());
        employee.setStatus(updatedEmployee.getStatus());
        return employeeRepository.save(employee);
    }

    @Transactional
    public void deleteEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found"));
        employee.setDeleted(true);
        employeeRepository.save(employee);
    }
}
