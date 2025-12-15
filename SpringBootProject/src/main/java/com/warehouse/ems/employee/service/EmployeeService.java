package com.warehouse.ems.employee.service;

import com.warehouse.ems.employee.domain.Employee;
import com.warehouse.ems.employee.dto.EmployeeRequestDTO;
import com.warehouse.ems.employee.dto.EmployeeResponseDTO;
import com.warehouse.ems.employee.repository.EmployeeRepository;
import com.warehouse.ems.exception.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Service for Employee business logic.
 */
@Service
@RequiredArgsConstructor
public class EmployeeService {
    private final EmployeeRepository employeeRepository;

    /**
     * Create a new employee, enforcing unique badgeId.
     */
    @Transactional
    public EmployeeResponseDTO createEmployee(EmployeeRequestDTO dto) {
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
                .deleted(false)
                .build();
        Employee saved = employeeRepository.save(employee);
        return toResponseDTO(saved);
    }

    /**
     * Get employee by ID.
     */
    public EmployeeResponseDTO getEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
        return toResponseDTO(employee);
    }

    /**
     * Update employee.
     */
    @Transactional
    public EmployeeResponseDTO updateEmployee(Long id, EmployeeRequestDTO dto) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
        if (!employee.getBadgeId().equals(dto.getBadgeId()) && employeeRepository.findByBadgeId(dto.getBadgeId()).isPresent()) {
            throw new IllegalArgumentException("Badge ID must be unique");
        }
        employee.setName(dto.getName());
        employee.setBadgeId(dto.getBadgeId());
        employee.setRole(dto.getRole());
        employee.setDepartment(dto.getDepartment());
        employee.setShiftGroup(dto.getShiftGroup());
        employee.setHireDate(dto.getHireDate());
        employee.setStatus(dto.getStatus());
        Employee updated = employeeRepository.save(employee);
        return toResponseDTO(updated);
    }

    /**
     * Soft-delete employee.
     */
    @Transactional
    public void deleteEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
        employeeRepository.delete(employee);
    }

    /**
     * List employees with pagination and filtering.
     */
    public Page<EmployeeResponseDTO> listEmployees(String name, String department, String role, Pageable pageable) {
        return employeeRepository.filterEmployees(name, department, role, pageable)
                .map(this::toResponseDTO);
    }

    /**
     * Map Employee entity to EmployeeResponseDTO.
     */
    private EmployeeResponseDTO toResponseDTO(Employee employee) {
        EmployeeResponseDTO dto = new EmployeeResponseDTO();
        dto.setId(employee.getId());
        dto.setName(employee.getName());
        dto.setBadgeId(employee.getBadgeId());
        dto.setRole(employee.getRole());
        dto.setDepartment(employee.getDepartment());
        dto.setShiftGroup(employee.getShiftGroup());
        dto.setHireDate(employee.getHireDate());
        dto.setStatus(employee.getStatus());
        dto.setCreatedAt(employee.getCreatedAt());
        dto.setUpdatedAt(employee.getUpdatedAt());
        dto.setCreatedBy(employee.getCreatedBy());
        dto.setUpdatedBy(employee.getUpdatedBy());
        return dto;
    }
}
