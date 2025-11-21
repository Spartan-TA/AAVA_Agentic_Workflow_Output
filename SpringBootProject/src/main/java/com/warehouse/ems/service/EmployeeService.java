package com.warehouse.ems.service;

import com.warehouse.ems.entity.Employee;
import com.warehouse.ems.exception.DuplicateBadgeIdException;
import com.warehouse.ems.exception.ResourceNotFoundException;
import com.warehouse.ems.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmployeeService {
    @Autowired
    private EmployeeRepository employeeRepository;

    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    public Employee getEmployeeById(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));
    }

    public Employee createEmployee(Employee employee) {
        if (employeeRepository.findByBadgeId(employee.getBadgeId()) != null) {
            throw new DuplicateBadgeIdException("Badge ID already exists: " + employee.getBadgeId());
        }
        return employeeRepository.save(employee);
    }

    public Employee updateEmployee(Long id, Employee employeeDetails) {
        Employee employee = getEmployeeById(id);
        employee.setName(employeeDetails.getName());
        employee.setBadgeId(employeeDetails.getBadgeId());
        employee.setEmail(employeeDetails.getEmail());
        employee.setPhone(employeeDetails.getPhone());
        employee.setRole(employeeDetails.getRole());
        return employeeRepository.save(employee);
    }

    public void deleteEmployee(Long id) {
        Employee employee = getEmployeeById(id);
        employeeRepository.delete(employee);
    }
}