package com.wms.ems.employee.service;

import com.wms.ems.employee.repository.EmployeeRepository;
import com.wms.ems.employee.dto.EmployeeDto;
import com.wms.ems.employee.entity.Employee;
import com.wms.ems.employee.entity.EmployeeStatus;
import com.wms.ems.common.exception.ResourceNotFoundException;
import com.wms.ems.common.exception.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service class for Employee business logic and operations.
 */
@Slf4j
@Service
@Transactional
public class EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;

    /**
     * Creates a new Employee after validating unique badgeId.
     * @param dto EmployeeDto
     * @return Employee
     */
    public Employee createEmployee(EmployeeDto dto) {
        if (employeeRepository.existsByBadgeId(dto.getBadgeId())) {
            log.warn("Attempt to create employee with duplicate badgeId: {}", dto.getBadgeId());
            throw new ValidationException("Badge ID must be unique");
        }
        Employee employee = new Employee(dto);
        employee.setDeleted(false);
        return employeeRepository.save(employee);
    }

    /**
     * Retrieves an Employee by ID.
     * @param id Employee ID
     * @return Employee
     */
    @Transactional(readOnly = true)
    public Employee getEmployeeById(Long id) {
        return employeeRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));
    }

    /**
     * Retrieves an Employee by badgeId.
     * @param badgeId Badge ID
     * @return Employee
     */
    @Transactional(readOnly = true)
    public Employee getEmployeeByBadgeId(String badgeId) {
        return employeeRepository.findByBadgeIdAndDeletedFalse(badgeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with badgeId: " + badgeId));
    }

    /**
     * Lists Employees with optional filtering by department and status, paginated.
     * @param pageable Pageable
     * @param department Department
     * @param status EmployeeStatus
     * @return Page<Employee>
     */
    @Transactional(readOnly = true)
    public Page<Employee> listEmployees(Pageable pageable, String department, EmployeeStatus status) {
        if (department != null && status != null) {
            return employeeRepository.findByDepartmentAndStatusAndDeletedFalse(department, status, pageable);
        } else if (department != null) {
            return employeeRepository.findByDepartmentAndDeletedFalse(department, pageable);
        } else if (status != null) {
            return employeeRepository.findByStatusAndDeletedFalse(status, pageable);
        } else {
            return employeeRepository.findByDeletedFalse(pageable);
        }
    }

    /**
     * Updates an Employee with partial update support.
     * @param id Employee ID
     * @param dto EmployeeDto
     * @return Employee
     */
    public Employee updateEmployee(Long id, EmployeeDto dto) {
        Employee employee = getEmployeeById(id);
        if (dto.getBadgeId() != null && !dto.getBadgeId().equals(employee.getBadgeId())) {
            if (employeeRepository.existsByBadgeId(dto.getBadgeId())) {
                throw new ValidationException("Badge ID must be unique");
            }
            employee.setBadgeId(dto.getBadgeId());
        }
        if (dto.getName() != null) employee.setName(dto.getName());
        if (dto.getDepartment() != null) employee.setDepartment(dto.getDepartment());
        if (dto.getStatus() != null) employee.setStatus(dto.getStatus());
        // Add other fields as needed
        return employeeRepository.save(employee);
    }

    /**
     * Soft deletes an Employee (sets deleted flag to true).
     * @param id Employee ID
     */
    public void softDeleteEmployee(Long id) {
        Employee employee = getEmployeeById(id);
        employee.setDeleted(true);
        employeeRepository.save(employee);
    }

    /**
     * Gets Employees by department.
     * @param department Department
     * @return List<Employee>
     */
    @Transactional(readOnly = true)
    public List<Employee> getEmployeesByDepartment(String department) {
        return employeeRepository.findByDepartmentAndDeletedFalse(department);
    }
}
