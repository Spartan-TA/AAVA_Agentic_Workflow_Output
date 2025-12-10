package com.warehouse.employee;

import com.warehouse.dto.EmployeeDTO;
import com.warehouse.exception.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

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
    @Transactional
    public Employee createEmployee(EmployeeDTO dto) {
        if (employeeRepository.findByBadgeId(dto.getBadgeId()).isPresent()) {
            throw new IllegalArgumentException("Badge ID must be unique");
        }
        Employee employee = Employee.builder()
                .name(dto.getName())
                .badgeId(dto.getBadgeId())
                .role(dto.getRole())
                .department(dto.getDepartment())
                .shiftGroup(dto.getShiftGroup())
                .hireDate(dto.getHireDate())
                .status(dto.getStatus())
                .build();
        return employeeRepository.save(employee);
    }

    /**
     * Get employee by ID.
     */
    public Employee getEmployee(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
    }

    /**
     * Update employee details.
     */
    @Transactional
    public Employee updateEmployee(Long id, EmployeeDTO dto) {
        Employee employee = getEmployee(id);
        if (!employee.getBadgeId().equals(dto.getBadgeId()) && employeeRepository.findByBadgeId(dto.getBadgeId()).isPresent()) {
            throw new IllegalArgumentException("Badge ID must be unique");
        }
        BeanUtils.copyProperties(dto, employee, "id", "createdAt", "deleted");
        return employeeRepository.save(employee);
    }

    /**
     * Soft-delete employee.
     */
    @Transactional
    public void deleteEmployee(Long id) {
        Employee employee = getEmployee(id);
        employee.setDeleted(true);
        employeeRepository.save(employee);
    }

    /**
     * List employees with filtering and pagination.
     */
    public Page<Employee> listEmployees(String name, String department, String role, Pageable pageable) {
        return employeeRepository.filterEmployees(
                StringUtils.hasText(name) ? name : null,
                StringUtils.hasText(department) ? department : null,
                StringUtils.hasText(role) ? role : null,
                pageable
        );
    }
}
