package com.warehousemgmt.service;

import com.warehousemgmt.domain.Employee;
import com.warehousemgmt.repository.EmployeeRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

/**
 * Service for Employee business logic: CRUD, soft delete, filtering.
 */
@Service
@RequiredArgsConstructor
public class EmployeeService {
    private final EmployeeRepository employeeRepository;

    /**
     * Create a new employee, enforcing unique badgeId.
     */
    @Transactional
    public Employee createEmployee(@Valid Employee employee) {
        employeeRepository.findByBadgeIdAndDeletedFalse(employee.getBadgeId())
            .ifPresent(e -> { throw new IllegalArgumentException("Badge ID already exists"); });
        employee.setDeleted(false);
        return employeeRepository.save(employee);
    }

    /**
     * Get all active employees with pagination.
     */
    public Page<Employee> getAllEmployees(Pageable pageable) {
        return employeeRepository.findAll(pageable);
    }

    /**
     * Get employee by ID if not deleted.
     */
    public Optional<Employee> getEmployee(Long id) {
        return employeeRepository.findById(id)
            .filter(e -> !e.getDeleted());
    }

    /**
     * Update employee details.
     */
    @Transactional
    public Employee updateEmployee(Long id, @Valid Employee updated) {
        Employee employee = employeeRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Employee not found"));
        if (updated.getBadgeId() != null && !updated.getBadgeId().equals(employee.getBadgeId())) {
            employeeRepository.findByBadgeIdAndDeletedFalse(updated.getBadgeId())
                .ifPresent(e -> { throw new IllegalArgumentException("Badge ID already exists"); });
            employee.setBadgeId(updated.getBadgeId());
        }
        employee.setName(updated.getName());
        employee.setRole(updated.getRole());
        employee.setDepartment(updated.getDepartment());
        employee.setShiftGroup(updated.getShiftGroup());
        employee.setHireDate(updated.getHireDate());
        employee.setStatus(updated.getStatus());
        employee.setTenantId(updated.getTenantId());
        return employeeRepository.save(employee);
    }

    /**
     * Soft delete employee.
     */
    @Transactional
    public void deleteEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Employee not found"));
        employee.setDeleted(true);
        employeeRepository.save(employee);
    }

    /**
     * Filter employees by department and role.
     */
    public List<Employee> filterEmployees(String department, String role) {
        return employeeRepository.filterByDepartmentAndRole(department, role);
    }
}
