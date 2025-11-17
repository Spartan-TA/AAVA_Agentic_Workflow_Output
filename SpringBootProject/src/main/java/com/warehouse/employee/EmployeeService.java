package com.warehouse.employee;

import com.warehouse.employee.dto.EmployeeDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Service layer for Employee CRUD operations and business logic.
 */
@Service
public class EmployeeService {
    @Autowired
    private EmployeeRepository employeeRepository;

    /**
     * Create a new employee, enforcing unique badgeId.
     */
    @Transactional
    public Employee createEmployee(EmployeeDto dto) {
        if (employeeRepository.findByBadgeIdAndDeletedFalse(dto.getBadgeId()).isPresent()) {
            throw new IllegalArgumentException("Badge ID must be unique.");
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

    /**
     * Get paginated list of active (not deleted) employees.
     */
    public Page<Employee> getEmployees(Pageable pageable) {
        return employeeRepository.findAllByDeletedFalse(pageable);
    }

    /**
     * Get employee by ID if not deleted.
     */
    public Optional<Employee> getEmployee(Long id) {
        return employeeRepository.findById(id)
                .filter(e -> !e.isDeleted());
    }

    /**
     * Update employee details.
     */
    @Transactional
    public Optional<Employee> updateEmployee(Long id, EmployeeDto dto) {
        return getEmployee(id).map(employee -> {
            employee.setName(dto.getName());
            employee.setRole(dto.getRole());
            employee.setDepartment(dto.getDepartment());
            employee.setShiftGroup(dto.getShiftGroup());
            employee.setHireDate(dto.getHireDate());
            employee.setStatus(dto.getStatus());
            return employeeRepository.save(employee);
        });
    }

    /**
     * Soft-delete employee.
     */
    @Transactional
    public boolean deleteEmployee(Long id) {
        return getEmployee(id).map(employee -> {
            employee.setDeleted(true);
            employeeRepository.save(employee);
            return true;
        }).orElse(false);
    }

    /**
     * Filter employees by name and department.
     */
    public Page<Employee> filterEmployees(String name, String department, Pageable pageable) {
        return employeeRepository.filterEmployees(name, department, pageable);
    }
}
