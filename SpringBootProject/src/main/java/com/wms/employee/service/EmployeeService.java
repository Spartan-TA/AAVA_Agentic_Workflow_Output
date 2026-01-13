package com.wms.employee.service;

import com.wms.employee.entity.Employee;
import com.wms.employee.repository.EmployeeRepository;
import com.wms.employee.dto.CreateEmployeeDto;
import com.wms.employee.dto.UpdateEmployeeDto;
import com.wms.employee.dto.EmployeeDto;
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

    @Transactional(readOnly = true)
    public Page<Employee> getEmployees(String department, Pageable pageable) {
        if (department != null) {
            return employeeRepository.findAllByDepartmentAndDeletedFalse(department, pageable);
        }
        return employeeRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Optional<Employee> getEmployeeById(Long id) {
        return employeeRepository.findById(id);
    }

    @Transactional
    public Employee createEmployee(CreateEmployeeDto dto) {
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
        return employeeRepository.save(employee);
    }

    @Transactional
    public Employee updateEmployee(Long id, UpdateEmployeeDto dto) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        employee.setName(dto.getName());
        employee.setRole(dto.getRole());
        employee.setDepartment(dto.getDepartment());
        employee.setShiftGroup(dto.getShiftGroup());
        employee.setHireDate(dto.getHireDate());
        employee.setStatus(dto.getStatus());
        return employeeRepository.save(employee);
    }

    @Transactional
    public void softDeleteEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        employee.setDeleted(true);
        employeeRepository.save(employee);
    }
}
