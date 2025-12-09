package com.company.wems.employee.service;

import com.company.wems.employee.entity.Employee;
import com.company.wems.employee.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

/**
 * Service for Employee business logic.
 */
@Service
public class EmployeeService {
    @Autowired
    private EmployeeRepository employeeRepository;

    public List<Employee> getAllActiveEmployees() {
        return employeeRepository.findAllActive();
    }

    public Page<Employee> getEmployees(Pageable pageable) {
        return employeeRepository.findAll(pageable);
    }

    public Optional<Employee> getEmployeeById(Long id) {
        return employeeRepository.findById(id).filter(e -> !e.isDeleted());
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
        return employeeRepository.findById(id).map(e -> {
            e.setName(updated.getName());
            e.setBadgeId(updated.getBadgeId());
            e.setRole(updated.getRole());
            e.setDepartment(updated.getDepartment());
            e.setShiftGroup(updated.getShiftGroup());
            e.setHireDate(updated.getHireDate());
            e.setStatus(updated.getStatus());
            return employeeRepository.save(e);
        }).orElseThrow(() -> new RuntimeException("Employee not found"));
    }

    @Transactional
    public void softDeleteEmployee(Long id) {
        employeeRepository.findById(id).ifPresent(e -> {
            e.setDeleted(true);
            employeeRepository.save(e);
        });
    }
}
