package com.warehouseems.employee.service;

import com.warehouseems.employee.model.Employee;
import com.warehouseems.employee.dto.EmployeeDto;
import com.warehouseems.employee.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

/**
 * Service layer for Employee business logic, including CRUD and soft-delete.
 */
@Service
public class EmployeeService {
    @Autowired
    private EmployeeRepository employeeRepository;

    /**
     * Create a new employee record.
     */
    @Transactional
    public EmployeeDto createEmployee(EmployeeDto dto) {
        Employee employee = toEntity(dto);
        employee.setDeleted(false);
        Employee saved = employeeRepository.save(employee);
        return toDto(saved);
    }

    /**
     * Get paginated list of employees (non-deleted).
     */
    public Page<EmployeeDto> getAllEmployees(Pageable pageable) {
        return employeeRepository.findAllByDeletedFalse(pageable)
                .map(this::toDto);
    }

    /**
     * Get employee by badgeId (non-deleted).
     */
    public Optional<EmployeeDto> getByBadgeId(String badgeId) {
        return employeeRepository.findByBadgeIdAndDeletedFalse(badgeId)
                .map(this::toDto);
    }

    /**
     * Update employee record by ID.
     */
    @Transactional
    public Optional<EmployeeDto> updateEmployee(Long id, EmployeeDto dto) {
        return employeeRepository.findById(id)
                .filter(e -> !e.isDeleted())
                .map(existing -> {
                    existing.setName(dto.getName());
                    existing.setBadgeId(dto.getBadgeId());
                    existing.setRole(dto.getRole());
                    existing.setDepartment(dto.getDepartment());
                    existing.setShiftGroup(dto.getShiftGroup());
                    existing.setHireDate(dto.getHireDate());
                    existing.setStatus(dto.getStatus());
                    return toDto(employeeRepository.save(existing));
                });
    }

    /**
     * Soft-delete employee by ID.
     */
    @Transactional
    public boolean deleteEmployee(Long id) {
        return employeeRepository.findById(id)
                .filter(e -> !e.isDeleted())
                .map(e -> {
                    e.setDeleted(true);
                    employeeRepository.save(e);
                    return true;
                }).orElse(false);
    }

    /**
     * Filter employees by department, role, and status.
     */
    public Page<EmployeeDto> filterEmployees(String department, String role, String status, Pageable pageable) {
        return employeeRepository.filterEmployees(department, role, status, pageable)
                .map(this::toDto);
    }

    /**
     * Convert Employee entity to DTO.
     */
    private EmployeeDto toDto(Employee e) {
        EmployeeDto dto = new EmployeeDto();
        dto.setId(e.getId());
        dto.setName(e.getName());
        dto.setBadgeId(e.getBadgeId());
        dto.setRole(e.getRole());
        dto.setDepartment(e.getDepartment());
        dto.setShiftGroup(e.getShiftGroup());
        dto.setHireDate(e.getHireDate());
        dto.setStatus(e.getStatus());
        return dto;
    }

    /**
     * Convert DTO to Employee entity.
     */
    private Employee toEntity(EmployeeDto dto) {
        Employee e = new Employee();
        e.setName(dto.getName());
        e.setBadgeId(dto.getBadgeId());
        e.setRole(dto.getRole());
        e.setDepartment(dto.getDepartment());
        e.setShiftGroup(dto.getShiftGroup());
        e.setHireDate(dto.getHireDate());
        e.setStatus(dto.getStatus());
        return e;
    }
}
