package com.warehouse.ems.employee;

import com.warehouse.ems.common.exception.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

/**
 * Service layer for Employee business logic.
 */
@Service
@RequiredArgsConstructor
public class EmployeeService {
    private final EmployeeRepository employeeRepository;

    /**
     * Create a new employee.
     */
    public Employee create(Employee employee) {
        return employeeRepository.save(employee);
    }

    /**
     * Get employee by ID.
     */
    public Employee getById(Long id) {
        return employeeRepository.findById(id)
                .filter(e -> !e.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found: " + id));
    }

    /**
     * Update an existing employee.
     */
    public Employee update(Long id, Employee updated) {
        Employee existing = getById(id);
        existing.setName(updated.getName());
        existing.setBadgeId(updated.getBadgeId());
        existing.setRole(updated.getRole());
        existing.setDepartment(updated.getDepartment());
        existing.setShiftGroup(updated.getShiftGroup());
        existing.setHireDate(updated.getHireDate());
        existing.setStatus(updated.getStatus());
        existing.setActive(updated.isActive());
        return employeeRepository.save(existing);
    }

    /**
     * Soft-delete an employee.
     */
    @Transactional
    public void delete(Long id) {
        Employee employee = getById(id);
        employee.setDeleted(true);
        employee.setActive(false);
        employeeRepository.save(employee);
    }

    /**
     * List employees with pagination and optional filtering.
     */
    public Page<Employee> list(String name, String department, Pageable pageable) {
        // Simple filtering; for advanced, use Specifications or QueryDSL
        Page<Employee> page = employeeRepository.findAll(pageable)
                .map(e -> e.isDeleted() ? null : e)
                .filter(e -> e != null);
        if (StringUtils.hasText(name)) {
            page = new PageImpl<>(page.stream().filter(e -> e.getName().toLowerCase().contains(name.toLowerCase())).toList(), pageable, page.getTotalElements());
        }
        if (StringUtils.hasText(department)) {
            page = new PageImpl<>(page.stream().filter(e -> department.equalsIgnoreCase(e.getDepartment())).toList(), pageable, page.getTotalElements());
        }
        return page;
    }

    /**
     * Restore a soft-deleted employee.
     */
    @Transactional
    public Employee restore(Long id) {
        Employee employee = getById(id);
        employee.setDeleted(false);
        employee.setActive(true);
        return employeeRepository.save(employee);
    }
}
