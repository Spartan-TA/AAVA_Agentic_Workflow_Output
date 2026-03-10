package com.company.warehouse.employee.service;

import com.company.warehouse.employee.dto.EmployeeDto;
import com.company.warehouse.employee.entity.Employee;
import com.company.warehouse.employee.repository.EmployeeRepository;
import com.company.warehouse.common.exception.ResourceNotFoundException;
import com.company.warehouse.common.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for Employee business logic.
 */
@Service
@RequiredArgsConstructor
public class EmployeeService {
    private final EmployeeRepository employeeRepository;

    @Transactional(readOnly = true)
    public Page<Employee> getAllEmployees(Pageable pageable) {
        return employeeRepository.findAllByDeletedFalse(pageable);
    }

    @Transactional(readOnly = true)
    public Employee getEmployeeById(Long id) {
        return employeeRepository.findById(id)
                .filter(e -> !e.getDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
    }

    @Transactional
    public Employee createEmployee(EmployeeDto dto) {
        if (employeeRepository.existsByBadgeIdAndDeletedFalse(dto.getBadgeId())) {
            throw new BadRequestException("Badge ID already exists");
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
        return employeeRepository.save(employee);
    }

    @Transactional
    public Employee updateEmployee(Long id, EmployeeDto dto) {
        Employee employee = getEmployeeById(id);
        if (!employee.getBadgeId().equals(dto.getBadgeId()) &&
                employeeRepository.existsByBadgeIdAndDeletedFalse(dto.getBadgeId())) {
            throw new BadRequestException("Badge ID already exists");
        }
        employee.setBadgeId(dto.getBadgeId());
        employee.setName(dto.getName());
        employee.setRole(dto.getRole());
        employee.setDepartment(dto.getDepartment());
        employee.setShiftGroup(dto.getShiftGroup());
        employee.setHireDate(dto.getHireDate());
        employee.setStatus(dto.getStatus());
        return employeeRepository.save(employee);
    }

    @Transactional
    public void deleteEmployee(Long id) {
        Employee employee = getEmployeeById(id);
        employee.setDeleted(true);
        employeeRepository.save(employee);
    }
}