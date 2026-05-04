package com.warehouse.management.employee.service;

import com.warehouse.management.employee.dto.EmployeeDTO;
import com.warehouse.management.employee.entity.Employee;
import com.warehouse.management.employee.exception.DuplicateResourceException;
import com.warehouse.management.employee.exception.ResourceNotFoundException;
import com.warehouse.management.employee.mapper.EmployeeMapper;
import com.warehouse.management.employee.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Service class for Employee business logic.
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
     * Creates a new employee.
     * @param employeeDTO Employee data
     * @return Created EmployeeDTO
     */
    @Transactional
    public EmployeeDTO createEmployee(EmployeeDTO employeeDTO) {
        if (employeeRepository.existsByEmail(employeeDTO.getEmail())) {
            throw new DuplicateResourceException("Employee with email already exists");
        }
        Employee employee = employeeMapper.toEntity(employeeDTO);
        Employee saved = employeeRepository.save(employee);
        return employeeMapper.toDTO(saved);
    }

    /**
     * Updates an existing employee.
     * @param id Employee ID
     * @param employeeDTO Employee data
     * @return Updated EmployeeDTO
     */
    @Transactional
    public EmployeeDTO updateEmployee(Long id, EmployeeDTO employeeDTO) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
        if (!employee.getEmail().equals(employeeDTO.getEmail()) && employeeRepository.existsByEmail(employeeDTO.getEmail())) {
            throw new DuplicateResourceException("Employee with email already exists");
        }
        employeeMapper.updateEntity(employee, employeeDTO);
        Employee updated = employeeRepository.save(employee);
        return employeeMapper.toDTO(updated);
    }

    /**
     * Gets an employee by ID.
     * @param id Employee ID
     * @return EmployeeDTO
     */
    @Transactional(readOnly = true)
    public EmployeeDTO getEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
        return employeeMapper.toDTO(employee);
    }

    /**
     * Deletes an employee by ID.
     * @param id Employee ID
     */
    @Transactional
    public void deleteEmployee(Long id) {
        if (!employeeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Employee not found");
        }
        employeeRepository.deleteById(id);
    }

    /**
     * Gets a paginated list of employees by department.
     * @param department Department name
     * @param pageable Pageable
     * @return Page of EmployeeDTO
     */
    @Transactional(readOnly = true)
    public Page<EmployeeDTO> getEmployeesByDepartment(String department, Pageable pageable) {
        return employeeRepository.findByDepartment(department, pageable)
                .map(employeeMapper::toDTO);
    }

    /**
     * Gets a paginated list of all employees.
     * @param pageable Pageable
     * @return Page of EmployeeDTO
     */
    @Transactional(readOnly = true)
    public Page<EmployeeDTO> getAllEmployees(Pageable pageable) {
        return employeeRepository.findAll(pageable).map(employeeMapper::toDTO);
    }
}
