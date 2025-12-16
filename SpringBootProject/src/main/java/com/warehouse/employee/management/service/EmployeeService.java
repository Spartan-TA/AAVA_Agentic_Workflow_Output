package com.warehouse.employee.management.service;

import com.warehouse.employee.management.dto.EmployeeRequestDto;
import com.warehouse.employee.management.dto.EmployeeResponseDto;
import com.warehouse.employee.management.exception.ResourceNotFoundException;
import com.warehouse.employee.management.model.Employee;
import com.warehouse.employee.management.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Service for Employee business logic.
 */
@Service
@RequiredArgsConstructor
public class EmployeeService {
    private final EmployeeRepository employeeRepository;

    public Page<EmployeeResponseDto> getAllEmployees(Pageable pageable) {
        return employeeRepository.findAllByDeletedFalse(pageable)
                .map(this::toResponseDto);
    }

    public Page<EmployeeResponseDto> filterEmployees(String name, String department, String role, Pageable pageable) {
        return employeeRepository.filterEmployees(name, department, role, pageable)
                .map(this::toResponseDto);
    }

    public EmployeeResponseDto getEmployeeById(Long id) {
        Employee employee = employeeRepository.findById(id)
                .filter(e -> !e.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
        return toResponseDto(employee);
    }

    @Transactional
    public EmployeeResponseDto createEmployee(EmployeeRequestDto dto) {
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
        employee = employeeRepository.save(employee);
        return toResponseDto(employee);
    }

    @Transactional
    public EmployeeResponseDto updateEmployee(Long id, EmployeeRequestDto dto) {
        Employee employee = employeeRepository.findById(id)
                .filter(e -> !e.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
        employee.setName(dto.getName());
        employee.setBadgeId(dto.getBadgeId());
        employee.setRole(dto.getRole());
        employee.setDepartment(dto.getDepartment());
        employee.setShiftGroup(dto.getShiftGroup());
        employee.setHireDate(dto.getHireDate());
        employee.setStatus(dto.getStatus());
        employee = employeeRepository.save(employee);
        return toResponseDto(employee);
    }

    @Transactional
    public EmployeeResponseDto patchEmployee(Long id, EmployeeRequestDto dto) {
        Employee employee = employeeRepository.findById(id)
                .filter(e -> !e.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
        if (dto.getName() != null) employee.setName(dto.getName());
        if (dto.getBadgeId() != null) employee.setBadgeId(dto.getBadgeId());
        if (dto.getRole() != null) employee.setRole(dto.getRole());
        if (dto.getDepartment() != null) employee.setDepartment(dto.getDepartment());
        if (dto.getShiftGroup() != null) employee.setShiftGroup(dto.getShiftGroup());
        if (dto.getHireDate() != null) employee.setHireDate(dto.getHireDate());
        if (dto.getStatus() != null) employee.setStatus(dto.getStatus());
        employee = employeeRepository.save(employee);
        return toResponseDto(employee);
    }

    @Transactional
    public void deleteEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .filter(e -> !e.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
        employee.setDeleted(true);
        employeeRepository.save(employee);
    }

    private EmployeeResponseDto toResponseDto(Employee employee) {
        EmployeeResponseDto dto = new EmployeeResponseDto();
        dto.setId(employee.getId());
        dto.setName(employee.getName());
        dto.setBadgeId(employee.getBadgeId());
        dto.setRole(employee.getRole());
        dto.setDepartment(employee.getDepartment());
        dto.setShiftGroup(employee.getShiftGroup());
        dto.setHireDate(employee.getHireDate());
        dto.setStatus(employee.getStatus());
        return dto;
    }
}
