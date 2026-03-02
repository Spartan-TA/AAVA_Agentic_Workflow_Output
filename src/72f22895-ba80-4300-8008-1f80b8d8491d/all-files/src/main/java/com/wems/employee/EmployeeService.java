package com.wems.employee;

import com.wems.common.ResourceNotFoundException;
import com.wems.common.BusinessValidationException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Service for Employee business logic.
 */
@Service
@RequiredArgsConstructor
public class EmployeeService {
    private final EmployeeRepository employeeRepository;

    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public EmployeeDto createEmployee(EmployeeDto dto) {
        if (employeeRepository.findByBadgeId(dto.getBadgeId()).isPresent()) {
            throw new BusinessValidationException("Badge ID already exists");
        }
        Employee employee = toEntity(dto);
        employee.setDeleted(false);
        Employee saved = employeeRepository.save(employee);
        return toDto(saved);
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN','HR','SUPERVISOR')")
    public EmployeeDto updateEmployee(Long id, EmployeeDto dto) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
        employee.setName(dto.getName());
        employee.setRole(dto.getRole());
        employee.setDepartment(dto.getDepartment());
        employee.setShiftGroup(dto.getShiftGroup());
        employee.setHireDate(dto.getHireDate());
        employee.setStatus(dto.getStatus());
        Employee saved = employeeRepository.save(employee);
        return toDto(saved);
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public void softDeleteEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
        employee.setDeleted(true);
        employeeRepository.save(employee);
    }

    @PreAuthorize("hasAnyRole('ADMIN','HR','SUPERVISOR','WORKER')")
    public EmployeeDto getEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
        return toDto(employee);
    }

    @PreAuthorize("hasAnyRole('ADMIN','HR','SUPERVISOR','WORKER')")
    public Page<EmployeeDto> listEmployees(Pageable pageable) {
        return employeeRepository.findAll(pageable).map(this::toDto);
    }

    // Mapping methods
    public EmployeeDto toDto(Employee e) {
        return EmployeeDto.builder()
                .id(e.getId())
                .badgeId(e.getBadgeId())
                .name(e.getName())
                .role(e.getRole())
                .department(e.getDepartment())
                .shiftGroup(e.getShiftGroup())
                .hireDate(e.getHireDate())
                .status(e.getStatus())
                .build();
    }

    public Employee toEntity(EmployeeDto dto) {
        return Employee.builder()
                .id(dto.getId())
                .badgeId(dto.getBadgeId())
                .name(dto.getName())
                .role(dto.getRole())
                .department(dto.getDepartment())
                .shiftGroup(dto.getShiftGroup())
                .hireDate(dto.getHireDate())
                .status(dto.getStatus())
                .build();
    }
}
