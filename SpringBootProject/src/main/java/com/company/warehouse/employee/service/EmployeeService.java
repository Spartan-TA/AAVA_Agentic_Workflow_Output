package com.company.warehouse.employee.service;

import com.company.warehouse.employee.dto.EmployeeRequest;
import com.company.warehouse.employee.dto.EmployeeResponse;
import com.company.warehouse.employee.entity.Employee;
import com.company.warehouse.employee.repository.EmployeeRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for Employee business logic.
 */
@Service
@Validated
@RequiredArgsConstructor
public class EmployeeService {
    private final EmployeeRepository employeeRepository;

    @Transactional
    public EmployeeResponse createEmployee(EmployeeRequest request) {
        if (employeeRepository.existsByBadgeId(request.getBadgeId())) {
            throw new IllegalArgumentException("Badge ID already exists");
        }
        Employee employee = Employee.builder()
                .badgeId(request.getBadgeId())
                .name(request.getName())
                .role(request.getRole())
                .department(request.getDepartment())
                .shiftGroup(request.getShiftGroup())
                .hireDate(request.getHireDate())
                .status(request.getStatus())
                .build();
        Employee saved = employeeRepository.save(employee);
        return toResponse(saved);
    }

    public EmployeeResponse getEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found"));
        return toResponse(employee);
    }

    public List<EmployeeResponse> getAllEmployees() {
        return employeeRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public EmployeeResponse updateEmployee(Long id, EmployeeRequest request) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found"));
        BeanUtils.copyProperties(request, employee, "id");
        Employee saved = employeeRepository.save(employee);
        return toResponse(saved);
    }

    @Transactional
    public void deleteEmployee(Long id) {
        if (!employeeRepository.existsById(id)) {
            throw new EntityNotFoundException("Employee not found");
        }
        employeeRepository.deleteById(id);
    }

    private EmployeeResponse toResponse(Employee employee) {
        EmployeeResponse response = new EmployeeResponse();
        BeanUtils.copyProperties(employee, response);
        return response;
    }
}
