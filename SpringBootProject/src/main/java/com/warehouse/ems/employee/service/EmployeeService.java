package com.warehouse.ems.employee.service;

import com.warehouse.ems.employee.dto.EmployeeDto;
import com.warehouse.ems.employee.entity.Employee;
import com.warehouse.ems.employee.repository.EmployeeRepository;
import com.warehouse.ems.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    @Transactional(readOnly = true)
    public Page<Employee> getAllEmployees(Pageable pageable) {
        return employeeRepository.findAllActive(pageable);
    }

    @Transactional(readOnly = true)
    public Employee getEmployeeByBadgeId(String badgeId) {
        return employeeRepository.findByBadgeIdAndDeletedFalse(badgeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
    }

    @Transactional
    public Employee createEmployee(EmployeeDto dto) {
        if (employeeRepository.existsByBadgeIdAndDeletedFalse(dto.getBadgeId())) {
            throw new IllegalArgumentException("Badge ID already exists");
        }
        Employee employee = Employee.builder()
                .badgeId(dto.getBadgeId())
                .name(dto.getName())
                .role(dto.getRole())
                .department(dto.getDepartment())
                .shiftGroup(dto.getShiftGroup())
                .hireDate(dto.getHireDate())
                .status(dto.getStatus())
                .warehouseId(dto.getWarehouseId())
                .deleted(false)
                .build();
        return employeeRepository.save(employee);
    }

    @Transactional
    public Employee updateEmployee(Long id, EmployeeDto dto) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
        employee.setName(dto.getName());
        employee.setRole(dto.getRole());
        employee.setDepartment(dto.getDepartment());
        employee.setShiftGroup(dto.getShiftGroup());
        employee.setHireDate(dto.getHireDate());
        employee.setStatus(dto.getStatus());
        employee.setWarehouseId(dto.getWarehouseId());
        return employeeRepository.save(employee);
    }

    @Transactional
    public void softDeleteEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
        employee.setDeleted(true);
        employeeRepository.save(employee);
    }
}