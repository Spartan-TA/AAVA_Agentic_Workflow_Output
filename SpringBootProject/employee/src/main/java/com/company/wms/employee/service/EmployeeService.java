package com.company.wms.employee.service;

import com.company.wms.employee.dto.CreateEmployeeRequest;
import com.company.wms.employee.dto.EmployeeDTO;
import com.company.wms.employee.dto.UpdateEmployeeRequest;
import com.company.wms.employee.entity.Employee;
import com.company.wms.employee.mapper.EmployeeMapper;
import com.company.wms.employee.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service class for Employee business logic, CRUD, soft-delete, pagination, and filtering.
 */
@Service
public class EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final EmployeeMapper employeeMapper;

    @Autowired
    public EmployeeService(EmployeeRepository employeeRepository, EmployeeMapper employeeMapper) {
        this.employeeRepository = employeeRepository;
        this.employeeMapper = employeeMapper;
    }

    /**
     * Create a new employee.
     */
    @Transactional
    public EmployeeDTO createEmployee(CreateEmployeeRequest request) {
        Employee employee = employeeMapper.toEntity(request);
        employee.setActive(true);
        Employee saved = employeeRepository.save(employee);
        return employeeMapper.toDTO(saved);
    }

    /**
     * Update an existing employee.
     */
    @Transactional
    public EmployeeDTO updateEmployee(Long id, UpdateEmployeeRequest request) {
        Employee employee = employeeRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        employeeMapper.updateEntity(employee, request);
        Employee updated = employeeRepository.save(employee);
        return employeeMapper.toDTO(updated);
    }

    /**
     * Soft-delete an employee.
     */
    @Transactional
    public void deleteEmployee(Long id) {
        Employee employee = employeeRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        employee.setActive(false);
        employeeRepository.save(employee);
    }

    /**
     * Get employee by ID.
     */
    @Transactional(readOnly = true)
    public EmployeeDTO getEmployee(Long id) {
        Employee employee = employeeRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        return employeeMapper.toDTO(employee);
    }

    /**
     * List employees with pagination and optional filtering by name or department.
     */
    @Transactional(readOnly = true)
    public Page<EmployeeDTO> listEmployees(String name, String department, int page, int size, String sortBy) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).ascending());
        Page<Employee> employees;
        if (name != null && department != null) {
            employees = employeeRepository.findByNameContainingIgnoreCaseAndDepartmentContainingIgnoreCaseAndActiveTrue(name, department, pageable);
        } else if (name != null) {
            employees = employeeRepository.findByNameContainingIgnoreCaseAndActiveTrue(name, pageable);
        } else if (department != null) {
            employees = employeeRepository.findByDepartmentContainingIgnoreCaseAndActiveTrue(department, pageable);
        } else {
            employees = employeeRepository.findByActiveTrue(pageable);
        }
        return employees.map(employeeMapper::toDTO);
    }

    /**
     * List all active employees.
     */
    @Transactional(readOnly = true)
    public List<EmployeeDTO> listAllActiveEmployees() {
        return employeeRepository.findByActiveTrue()
                .stream()
                .map(employeeMapper::toDTO)
                .collect(Collectors.toList());
    }
}