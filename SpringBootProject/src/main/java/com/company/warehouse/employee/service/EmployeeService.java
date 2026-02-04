package com.company.warehouse.employee.service;

import com.company.warehouse.common.enums.Status;
import com.company.warehouse.common.exception.ResourceNotFoundException;
import com.company.warehouse.employee.dto.EmployeeDTO;
import com.company.warehouse.employee.entity.Employee;
import com.company.warehouse.employee.repository.EmployeeRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Service layer for Employee business logic.
 */
@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    @Transactional
    public EmployeeDTO createEmployee(EmployeeDTO dto) {
        Employee employee = Employee.builder()
                .badgeId(dto.getBadgeId())
                .name(dto.getName())
                .role(dto.getRole())
                .department(dto.getDepartment())
                .shiftGroup(dto.getShiftGroup())
                .hireDate(dto.getHireDate())
                .status(Status.ACTIVE)
                .build();
        employee = employeeRepository.save(employee);
        return toDTO(employee);
    }

    public Page<EmployeeDTO> getEmployees(Pageable pageable, String filter) {
        Page<Employee> page;
        if (filter != null && !filter.isEmpty()) {
            page = employeeRepository.findAll(
                Example.of(Employee.builder().name(filter).build(), ExampleMatcher.matchingAny()), pageable);
        } else {
            page = employeeRepository.findAll(pageable);
        }
        return page.map(this::toDTO);
    }

    public EmployeeDTO getEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
        return toDTO(employee);
    }

    @Transactional
    public EmployeeDTO updateEmployee(Long id, EmployeeDTO dto) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
        employee.setName(dto.getName());
        employee.setRole(dto.getRole());
        employee.setDepartment(dto.getDepartment());
        employee.setShiftGroup(dto.getShiftGroup());
        employee.setHireDate(dto.getHireDate());
        employee = employeeRepository.save(employee);
        return toDTO(employee);
    }

    @Transactional
    public void deleteEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
        employee.setStatus(Status.DELETED);
        employeeRepository.save(employee);
    }

    private EmployeeDTO toDTO(Employee employee) {
        EmployeeDTO dto = new EmployeeDTO();
        dto.setId(employee.getId());
        dto.setBadgeId(employee.getBadgeId());
        dto.setName(employee.getName());
        dto.setRole(employee.getRole());
        dto.setDepartment(employee.getDepartment());
        dto.setShiftGroup(employee.getShiftGroup());
        dto.setHireDate(employee.getHireDate());
        dto.setStatus(employee.getStatus());
        return dto;
    }
}