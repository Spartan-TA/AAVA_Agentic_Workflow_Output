package com.warehousemgmt.employee;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import java.util.Optional;
import java.util.List;
import java.util.stream.Collectors;

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
    public EmployeeDTO createEmployee(EmployeeDTO dto) {
        if (employeeRepository.existsByBadgeIdAndDeletedFalse(dto.getBadgeId())) {
            throw new IllegalArgumentException("Badge ID already exists.");
        }
        Employee employee = Employee.builder()
                .badgeId(dto.getBadgeId())
                .name(dto.getName())
                .role(dto.getRole())
                .department(dto.getDepartment())
                .shiftGroup(dto.getShiftGroup())
                .hireDate(dto.getHireDate())
                .status(dto.getStatus())
                .deleted(false)
                .build();
        employee = employeeRepository.save(employee);
        return toDTO(employee);
    }

    /**
     * Get all employees (not deleted).
     */
    @Transactional(readOnly = true)
    public List<EmployeeDTO> getAllEmployees() {
        return employeeRepository.findAllByDeletedFalse().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get employee by badge ID.
     */
    @Transactional(readOnly = true)
    public Optional<EmployeeDTO> getEmployeeByBadgeId(String badgeId) {
        return employeeRepository.findByBadgeIdAndDeletedFalse(badgeId)
                .map(this::toDTO);
    }

    /**
     * Update employee details.
     */
    @Transactional
    public EmployeeDTO updateEmployee(Long id, EmployeeDTO dto) {
        Employee employee = employeeRepository.findById(id)
                .filter(e -> !e.isDeleted())
                .orElseThrow(() -> new IllegalArgumentException("Employee not found."));
        employee.setName(dto.getName());
        employee.setRole(dto.getRole());
        employee.setDepartment(dto.getDepartment());
        employee.setShiftGroup(dto.getShiftGroup());
        employee.setHireDate(dto.getHireDate());
        employee.setStatus(dto.getStatus());
        employee = employeeRepository.save(employee);
        return toDTO(employee);
    }

    /**
     * Soft-delete employee.
     */
    @Transactional
    public void deleteEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .filter(e -> !e.isDeleted())
                .orElseThrow(() -> new IllegalArgumentException("Employee not found."));
        employee.setDeleted(true);
        employeeRepository.save(employee);
    }

    /**
     * Convert Employee entity to DTO.
     */
    private EmployeeDTO toDTO(Employee employee) {
        return EmployeeDTO.builder()
                .id(employee.getId())
                .badgeId(employee.getBadgeId())
                .name(employee.getName())
                .role(employee.getRole())
                .department(employee.getDepartment())
                .shiftGroup(employee.getShiftGroup())
                .hireDate(employee.getHireDate())
                .status(employee.getStatus())
                .build();
    }
}
