package com.company.warehousemgmt.service;

import com.company.warehousemgmt.domain.Employee;
import com.company.warehousemgmt.repository.EmployeeRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Service for Employee business logic.
 */
@Service
@RequiredArgsConstructor
public class EmployeeService {
    private final EmployeeRepository employeeRepository;

    @Transactional(readOnly = true)
    public Page<Employee> getAllEmployees(Pageable pageable) {
        return employeeRepository.findAllByDeletedFalse(pageable);
    }

    @Transactional(readOnly = true)
    public Optional<Employee> getEmployeeById(Long id) {
        return employeeRepository.findById(id).filter(e -> !Boolean.TRUE.equals(e.getDeleted()));
    }

    @Transactional
    public Employee createEmployee(Employee employee) {
        if (employeeRepository.findByBadgeId(employee.getBadgeId()).isPresent()) {
            throw new IllegalArgumentException("Badge ID must be unique");
        }
        return employeeRepository.save(employee);
    }

    @Transactional
    public Employee updateEmployee(Long id, Employee updated) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found"));
        if (updated.getFirstName() != null) employee.setFirstName(updated.getFirstName());
        if (updated.getLastName() != null) employee.setLastName(updated.getLastName());
        if (updated.getEmail() != null) employee.setEmail(updated.getEmail());
        if (updated.getDepartment() != null) employee.setDepartment(updated.getDepartment());
        if (updated.getShiftGroup() != null) employee.setShiftGroup(updated.getShiftGroup());
        if (updated.getHireDate() != null) employee.setHireDate(updated.getHireDate());
        if (updated.getStatus() != null) employee.setStatus(updated.getStatus());
        if (updated.getRole() != null) employee.setRole(updated.getRole());
        return employeeRepository.save(employee);
    }

    @Transactional
    public void softDeleteEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found"));
        employee.setDeleted(true);
        employeeRepository.save(employee);
    }
}
