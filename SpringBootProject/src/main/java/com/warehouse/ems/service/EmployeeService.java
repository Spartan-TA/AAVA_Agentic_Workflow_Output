package com.warehouse.ems.service;

import com.warehouse.ems.domain.Employee;
import com.warehouse.ems.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * Service layer for Employee business logic.
 */
@Service
@RequiredArgsConstructor
public class EmployeeService {
    private final EmployeeRepository employeeRepository;

    public Page<Employee> getAllActiveEmployees(Pageable pageable) {
        return employeeRepository.findAllByStatus(Employee.Status.ACTIVE, pageable);
    }

    public Optional<Employee> getEmployeeById(UUID id) {
        return employeeRepository.findById(id)
                .filter(e -> e.getStatus() != Employee.Status.DELETED);
    }

    public boolean isBadgeIdUnique(String badgeId, UUID excludeId) {
        return !employeeRepository.existsByBadgeIdAndStatusNot(badgeId, Employee.Status.DELETED);
    }

    @Transactional
    public Employee createEmployee(Employee employee) {
        if (!isBadgeIdUnique(employee.getBadgeId(), null)) {
            throw new IllegalArgumentException("badgeId must be unique");
        }
        employee.setStatus(Employee.Status.ACTIVE);
        return employeeRepository.save(employee);
    }

    @Transactional
    public Employee updateEmployee(UUID id, Employee updated) {
        Employee existing = employeeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));
        if (!existing.getBadgeId().equals(updated.getBadgeId()) && !isBadgeIdUnique(updated.getBadgeId(), id)) {
            throw new IllegalArgumentException("badgeId must be unique");
        }
        existing.setName(updated.getName());
        existing.setBadgeId(updated.getBadgeId());
        existing.setRole(updated.getRole());
        existing.setDepartment(updated.getDepartment());
        existing.setShiftGroup(updated.getShiftGroup());
        existing.setHireDate(updated.getHireDate());
        // status is not updated here
        return employeeRepository.save(existing);
    }

    @Transactional
    public void softDeleteEmployee(UUID id) {
        Employee existing = employeeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));
        existing.setStatus(Employee.Status.DELETED);
        employeeRepository.save(existing);
    }
}
