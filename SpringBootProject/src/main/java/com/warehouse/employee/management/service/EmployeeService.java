package com.warehouse.employee.management.service;

import com.warehouse.employee.management.domain.Employee;
import com.warehouse.employee.management.dto.EmployeeDTO;
import com.warehouse.employee.management.repository.EmployeeRepository;
import com.warehouse.employee.management.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    @PreAuthorize("hasAnyRole('ADMIN','HR','SUPERVISOR','WORKER')")
    public Page<Employee> getAllEmployees(Pageable pageable) {
        return employeeRepository.findAllByDeletedFalse(pageable);
    }

    @PreAuthorize("hasAnyRole('ADMIN','HR','SUPERVISOR','WORKER')")
    public Employee getEmployee(Long id) {
        return employeeRepository.findById(id)
                .filter(e -> !e.getDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
    }

    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    @Transactional
    public Employee createEmployee(EmployeeDTO dto) {
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

    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    @Transactional
    public Employee updateEmployee(Long id, EmployeeDTO dto) {
        Employee employee = getEmployee(id);
        employee.setName(dto.getName());
        employee.setRole(dto.getRole());
        employee.setDepartment(dto.getDepartment());
        employee.setShiftGroup(dto.getShiftGroup());
        employee.setHireDate(dto.getHireDate());
        employee.setStatus(dto.getStatus());
        return employeeRepository.save(employee);
    }

    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    @Transactional
    public void softDeleteEmployee(Long id) {
        Employee employee = getEmployee(id);
        employee.setDeleted(true);
        employeeRepository.save(employee);
    }
}