package com.warehouse.employee.management.service;

import com.warehouse.employee.management.dto.*;
import com.warehouse.employee.management.entity.Employee;
import com.warehouse.employee.management.repository.EmployeeRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Validated
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    @Transactional
    public EmployeeResponse createEmployee(EmployeeCreateRequest request) {
        if (employeeRepository.existsByBadgeIdAndDeletedFalse(request.getBadgeId())) {
            throw new IllegalArgumentException("Badge ID must be unique");
        }
        Employee employee = Employee.builder()
                .name(request.getName())
                .badgeId(request.getBadgeId())
                .role(request.getRole())
                .department(request.getDepartment())
                .shiftGroup(request.getShiftGroup())
                .hireDate(request.getHireDate())
                .status(request.getStatus())
                .deleted(false)
                .build();
        employee = employeeRepository.save(employee);
        return toResponse(employee);
    }

    @Transactional
    public EmployeeResponse updateEmployee(Long id, EmployeeUpdateRequest request) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found"));
        employee.setName(request.getName());
        employee.setRole(request.getRole());
        employee.setDepartment(request.getDepartment());
        employee.setShiftGroup(request.getShiftGroup());
        employee.setHireDate(request.getHireDate());
        employee.setStatus(request.getStatus());
        employee = employeeRepository.save(employee);
        return toResponse(employee);
    }

    @Transactional
    public void deleteEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found"));
        employee.setDeleted(true);
        employeeRepository.save(employee);
    }

    public EmployeeResponse getEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .filter(e -> !e.isDeleted())
                .orElseThrow(() -> new EntityNotFoundException("Employee not found"));
        return toResponse(employee);
    }

    public Page<EmployeeResponse> listEmployees(String search, Pageable pageable) {
        Page<Employee> employees;
        if (search != null && !search.isBlank()) {
            employees = employeeRepository.search(search, pageable);
        } else {
            employees = employeeRepository.findAllByDeletedFalse(pageable);
        }
        return employees.map(this::toResponse);
    }

    private EmployeeResponse toResponse(Employee employee) {
        return EmployeeResponse.builder()
                .id(employee.getId())
                .name(employee.getName())
                .badgeId(employee.getBadgeId())
                .role(employee.getRole())
                .department(employee.getDepartment())
                .shiftGroup(employee.getShiftGroup())
                .hireDate(employee.getHireDate())
                .status(employee.getStatus())
                .build();
    }
}