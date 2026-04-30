package com.warehouse.ems.service;

import com.warehouse.ems.dto.EmployeeDTO;
import com.warehouse.ems.entity.Employee;
import com.warehouse.ems.mapper.EmployeeMapper;
import com.warehouse.ems.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private EmployeeMapper employeeMapper;

    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'SUPERVISOR')")
    public Page<EmployeeDTO> getAllEmployees(String department, String status, Pageable pageable) {
        Page<Employee> employees;
        if (department != null && status != null) {
            employees = employeeRepository.findAll(
                Example.of(Employee.builder().department(department).status(status).deleted(false).build()),
                pageable
            );
        } else {
            employees = employeeRepository.findAll(pageable);
        }
        return employees.map(employeeMapper::toDto);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'SUPERVISOR')")
    public Optional<EmployeeDTO> getEmployeeById(Long id) {
        return employeeRepository.findById(id)
                .filter(e -> !Boolean.TRUE.equals(e.getDeleted()))
                .map(employeeMapper::toDto);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @Transactional
    public EmployeeDTO createEmployee(EmployeeDTO dto) {
        Employee employee = employeeMapper.toEntity(dto);
        employee.setDeleted(false);
        Employee saved = employeeRepository.save(employee);
        return employeeMapper.toDto(saved);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @Transactional
    public EmployeeDTO updateEmployee(Long id, EmployeeDTO dto) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        employee.setName(dto.getName());
        employee.setRole(dto.getRole());
        employee.setDepartment(dto.getDepartment());
        employee.setShiftGroup(dto.getShiftGroup());
        employee.setHireDate(dto.getHireDate());
        employee.setStatus(dto.getStatus());
        employee.setWarehouseId(dto.getWarehouseId());
        Employee saved = employeeRepository.save(employee);
        return employeeMapper.toDto(saved);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @Transactional
    public void softDeleteEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        employee.setDeleted(true);
        employeeRepository.save(employee);
    }
}