package com.warehouse.employee.service;

import com.warehouse.common.dto.EmployeeCreateDto;
import com.warehouse.common.dto.EmployeeDto;
import com.warehouse.common.dto.EmployeeUpdateDto;
import com.warehouse.employee.domain.Employee;
import com.warehouse.employee.domain.Role;
import com.warehouse.employee.repository.EmployeeRepository;
import com.warehouse.employee.repository.RoleRepository;
import com.warehouse.common.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service for employee business logic.
 */
@Service
@RequiredArgsConstructor
public class EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final RoleRepository roleRepository;

    public EmployeeDto toDto(Employee employee) {
        return EmployeeDto.builder()
                .id(employee.getId())
                .badgeId(employee.getBadgeId())
                .name(employee.getName())
                .roles(employee.getRoles().stream().map(Role::getName).collect(Collectors.toSet()))
                .department(employee.getDepartment())
                .shiftGroup(employee.getShiftGroup())
                .hireDate(employee.getHireDate())
                .status(employee.getStatus())
                .build();
    }

    @Transactional(readOnly = true)
    public Page<EmployeeDto> getAll(Pageable pageable) {
        return employeeRepository.findAllByDeletedFalse(pageable).map(this::toDto);
    }

    @Transactional(readOnly = true)
    public EmployeeDto getById(Long id) {
        Employee employee = employeeRepository.findById(id)
                .filter(e -> !e.getDeleted())
                .orElseThrow(() -> new NotFoundException("Employee not found"));
        return toDto(employee);
    }

    @Transactional
    public EmployeeDto create(EmployeeCreateDto dto) {
        if (employeeRepository.existsByBadgeId(dto.getBadgeId())) {
            throw new IllegalArgumentException("Badge ID already exists");
        }
        Set<Role> roles = dto.getRoles().stream()
                .map(roleName -> roleRepository.findByName(roleName)
                        .orElseThrow(() -> new NotFoundException("Role not found: " + roleName)))
                .collect(Collectors.toSet());
        Employee employee = Employee.builder()
                .badgeId(dto.getBadgeId())
                .name(dto.getName())
                .roles(roles)
                .department(dto.getDepartment())
                .shiftGroup(dto.getShiftGroup())
                .hireDate(dto.getHireDate())
                .status(dto.getStatus())
                .deleted(false)
                .build();
        return toDto(employeeRepository.save(employee));
    }

    @Transactional
    public EmployeeDto update(Long id, EmployeeUpdateDto dto) {
        Employee employee = employeeRepository.findById(id)
                .filter(e -> !e.getDeleted())
                .orElseThrow(() -> new NotFoundException("Employee not found"));
        if (dto.getName() != null) employee.setName(dto.getName());
        if (dto.getRoles() != null) {
            Set<Role> roles = dto.getRoles().stream()
                    .map(roleName -> roleRepository.findByName(roleName)
                            .orElseThrow(() -> new NotFoundException("Role not found: " + roleName)))
                    .collect(Collectors.toSet());
            employee.setRoles(roles);
        }
        if (dto.getDepartment() != null) employee.setDepartment(dto.getDepartment());
        if (dto.getShiftGroup() != null) employee.setShiftGroup(dto.getShiftGroup());
        if (dto.getHireDate() != null) employee.setHireDate(dto.getHireDate());
        if (dto.getStatus() != null) employee.setStatus(dto.getStatus());
        return toDto(employeeRepository.save(employee));
    }

    @Transactional
    public void softDelete(Long id) {
        Employee employee = employeeRepository.findById(id)
                .filter(e -> !e.getDeleted())
                .orElseThrow(() -> new NotFoundException("Employee not found"));
        employee.setDeleted(true);
        employeeRepository.save(employee);
    }
}
