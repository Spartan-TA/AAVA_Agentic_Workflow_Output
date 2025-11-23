package com.warehouse.ems.employee;

import com.warehouse.ems.common.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Service for Employee business logic.
 */
@Service
@RequiredArgsConstructor
public class EmployeeService {
    private final EmployeeRepository employeeRepository;
    private static final Logger logger = LoggerFactory.getLogger(EmployeeService.class);

    public Employee createEmployee(EmployeeDTO dto) {
        if (employeeRepository.existsByBadgeIdAndDeletedFalse(dto.getBadgeId())) {
            throw new IllegalArgumentException("Badge ID must be unique.");
        }
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
        logger.info("Creating employee: {}", employee);
        return employeeRepository.save(employee);
    }

    public Page<Employee> getEmployees(Pageable pageable, String filter) {
        // Filtering logic can be implemented using specifications
        return employeeRepository.findAll(pageable);
    }

    public Employee getEmployee(Long id) {
        return employeeRepository.findById(id)
                .filter(e -> !e.getDeleted())
                .orElseThrow(() -> new NotFoundException("Employee not found"));
    }

    public Employee updateEmployee(Long id, EmployeeDTO dto) {
        Employee employee = getEmployee(id);
        employee.setName(dto.getName());
        employee.setRole(dto.getRole());
        employee.setDepartment(dto.getDepartment());
        employee.setShiftGroup(dto.getShiftGroup());
        employee.setHireDate(dto.getHireDate());
        employee.setStatus(dto.getStatus());
        employee.setUpdatedAt(java.time.LocalDateTime.now());
        logger.info("Updating employee: {}", employee);
        return employeeRepository.save(employee);
    }

    public void softDeleteEmployee(Long id) {
        Employee employee = getEmployee(id);
        employee.setDeleted(true);
        employee.setUpdatedAt(java.time.LocalDateTime.now());
        logger.info("Soft-deleting employee: {}", employee);
        employeeRepository.save(employee);
    }
}