package com.wems.employee.service;

import com.wems.employee.domain.Employee;
import com.wems.employee.repository.EmployeeRepository;
import com.wems.employee.dto.EmployeeDTO;
import com.wems.common.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service layer for Employee operations.
 * Handles business logic, validation, and exception handling.
 */
@Service
public class EmployeeService {

    private static final Logger logger = LoggerFactory.getLogger(EmployeeService.class);

    @Autowired
    private EmployeeRepository employeeRepository;

    /**
     * Creates a new employee after validating badgeId uniqueness.
     * @param dto EmployeeDTO
     * @return Employee
     */
    public Employee createEmployee(EmployeeDTO dto) {
        if (employeeRepository.findByBadgeIdAndActiveTrue(dto.getBadgeId()) != null) {
            logger.warn("Attempted to create employee with duplicate badgeId: {}", dto.getBadgeId());
            throw new IllegalArgumentException("Badge ID must be unique.");
        }
        Employee employee = dto.toEntity();
        logger.info("Creating employee: {}", employee.getName());
        return employeeRepository.save(employee);
    }

    /**
     * Retrieves paginated list of active employees.
     * @param pageable Pageable
     * @return Page<Employee>
     */
    public Page<Employee> getActiveEmployees(Pageable pageable) {
        logger.debug("Fetching active employees with pagination.");
        return employeeRepository.findAllActive(pageable);
    }

    /**
     * Updates employee details.
     * @param id Employee ID
     * @param dto EmployeeDTO
     * @return Employee
     */
    @Transactional
    public Employee updateEmployee(Long id, EmployeeDTO dto) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found: " + id));
        employee.setName(dto.getName());
        employee.setEmail(dto.getEmail());
        employee.setBadgeId(dto.getBadgeId());
        employee.setRole(dto.getRole());
        logger.info("Updated employee: {}", employee.getId());
        return employeeRepository.save(employee);
    }

    /**
     * Soft deletes an employee.
     * @param id Employee ID
     */
    @Transactional
    public void softDeleteEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found: " + id));
        employee.softDelete();
        logger.info("Soft deleted employee: {}", employee.getId());
        employeeRepository.save(employee);
    }

    /**
     * Retrieves employee by badgeId.
     * @param badgeId Badge ID
     * @return Employee
     */
    public Employee getEmployeeByBadgeId(String badgeId) {
        Employee employee = employeeRepository.findByBadgeIdAndActiveTrue(badgeId);
        if (employee == null) {
            logger.warn("Employee not found with badgeId: {}", badgeId);
            throw new ResourceNotFoundException("Employee not found with badgeId: " + badgeId);
        }
        return employee;
    }
}
