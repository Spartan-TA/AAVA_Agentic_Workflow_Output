package com.warehouse.employee.service;

import com.warehouse.employee.domain.Employee;
import com.warehouse.employee.dto.EmployeeRequest;
import com.warehouse.employee.dto.EmployeeResponse;
import com.warehouse.employee.exception.DuplicateBadgeIdException;
import com.warehouse.employee.exception.EmployeeNotFoundException;
import com.warehouse.employee.mapper.EmployeeMapper;
import com.warehouse.employee.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service for Employee CRUD operations, validation, soft-delete, and pagination.
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
     * Create a new employee with validation.
     * @param request EmployeeRequest
     * @return EmployeeResponse
     */
    @Transactional
    public EmployeeResponse createEmployee(EmployeeRequest request) {
        if (employeeRepository.existsByBadgeIdAndDeletedFalse(request.getBadgeId())) {
            throw new DuplicateBadgeIdException("Badge ID already exists: " + request.getBadgeId());
        }
        Employee employee = employeeMapper.toEntity(request);
        employee.setDeleted(false);
        Employee saved = employeeRepository.save(employee);
        return employeeMapper.toResponse(saved);
    }

    /**
     * Get all employees (not deleted) with pagination.
     * @param page page number
     * @param size page size
     * @return List of EmployeeResponse
     */
    @Transactional(readOnly = true)
    public List<EmployeeResponse> getAllEmployees(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Employee> employees = employeeRepository.findAllByDeletedFalse(pageable);
        return employeeMapper.toResponseList(employees.getContent());
    }

    /**
     * Get employee by ID.
     * @param id Employee ID
     * @return EmployeeResponse
     */
    @Transactional(readOnly = true)
    public EmployeeResponse getEmployeeById(Long id) {
        Employee employee = employeeRepository.findById(id)
                .filter(e -> !e.isDeleted())
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found: " + id));
        return employeeMapper.toResponse(employee);
    }

    /**
     * Update employee by ID.
     * @param id Employee ID
     * @param request EmployeeRequest
     * @return EmployeeResponse
     */
    @Transactional
    public EmployeeResponse updateEmployee(Long id, EmployeeRequest request) {
        Employee employee = employeeRepository.findById(id)
                .filter(e -> !e.isDeleted())
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found: " + id));
        if (!employee.getBadgeId().equals(request.getBadgeId()) &&
                employeeRepository.existsByBadgeIdAndDeletedFalse(request.getBadgeId())) {
            throw new DuplicateBadgeIdException("Badge ID already exists: " + request.getBadgeId());
        }
        employee.setBadgeId(request.getBadgeId());
        employee.setName(request.getName());
        employee.setRole(request.getRole());
        employee.setStatus(request.getStatus());
        Employee updated = employeeRepository.save(employee);
        return employeeMapper.toResponse(updated);
    }

    /**
     * Soft-delete employee by ID.
     * @param id Employee ID
     */
    @Transactional
    public void deleteEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .filter(e -> !e.isDeleted())
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found: " + id));
        employee.setDeleted(true);
        employeeRepository.save(employee);
    }
}
