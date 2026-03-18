package com.warehouse.ems.employee.service;

import com.warehouse.ems.employee.domain.Employee;
import com.warehouse.ems.employee.dto.CreateEmployeeRequest;
import com.warehouse.ems.employee.dto.UpdateEmployeeRequest;
import com.warehouse.ems.employee.dto.EmployeeDto;
import com.warehouse.ems.employee.repository.EmployeeRepository;
import com.warehouse.ems.exception.ResourceNotFoundException;
import com.warehouse.ems.exception.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service class for Employee business logic.
 */
@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    @Autowired
    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    /**
     * Create a new employee.
     * @param request CreateEmployeeRequest DTO
     * @return EmployeeDto
     */
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public EmployeeDto createEmployee(CreateEmployeeRequest request) {
        if (employeeRepository.existsByEmail(request.getEmail())) {
            throw new ValidationException("Employee with email already exists.");
        }
        Employee employee = new Employee();
        employee.setFirstName(request.getFirstName());
        employee.setLastName(request.getLastName());
        employee.setEmail(request.getEmail());
        employee.setPhone(request.getPhone());
        employee.setDepartment(request.getDepartment());
        employee.setRole(request.getRole());
        employee.setActive(true);
        Employee saved = employeeRepository.save(employee);
        return toDto(saved);
    }

    /**
     * Update an existing employee.
     * @param id Employee ID
     * @param request UpdateEmployeeRequest DTO
     * @return EmployeeDto
     */
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public EmployeeDto updateEmployee(Long id, UpdateEmployeeRequest request) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
        employee.setFirstName(request.getFirstName());
        employee.setLastName(request.getLastName());
        employee.setPhone(request.getPhone());
        employee.setDepartment(request.getDepartment());
        employee.setRole(request.getRole());
        employee.setActive(request.isActive());
        Employee updated = employeeRepository.save(employee);
        return toDto(updated);
    }

    /**
     * Get employee by ID.
     * @param id Employee ID
     * @return EmployeeDto
     */
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','USER')")
    @Transactional(readOnly = true)
    public EmployeeDto getEmployeeById(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
        return toDto(employee);
    }

    /**
     * Get all employees.
     * @return List of EmployeeDto
     */
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Transactional(readOnly = true)
    public List<EmployeeDto> getAllEmployees() {
        return employeeRepository.findAll()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Delete employee by ID.
     * @param id Employee ID
     */
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void deleteEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
        employeeRepository.delete(employee);
    }

    /**
     * Convert Employee entity to EmployeeDto.
     * @param employee Employee entity
     * @return EmployeeDto
     */
    private EmployeeDto toDto(Employee employee) {
        EmployeeDto dto = new EmployeeDto();
        dto.setId(employee.getId());
        dto.setFirstName(employee.getFirstName());
        dto.setLastName(employee.getLastName());
        dto.setEmail(employee.getEmail());
        dto.setPhone(employee.getPhone());
        dto.setDepartment(employee.getDepartment());
        dto.setRole(employee.getRole());
        dto.setActive(employee.isActive());
        return dto;
    }
}
