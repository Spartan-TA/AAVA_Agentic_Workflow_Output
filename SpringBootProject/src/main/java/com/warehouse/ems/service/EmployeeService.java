package com.warehouse.ems.service;

import com.warehouse.ems.dto.EmployeeDTO;
import com.warehouse.ems.model.Employee;
import com.warehouse.ems.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Service layer for Employee operations.
 */
@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    @Autowired
    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    /**
     * Create a new employee.
     */
    @Transactional
    public Employee createEmployee(EmployeeDTO dto) {
        Employee employee = new Employee();
        employee.setBadgeId(dto.getBadgeId());
        employee.setName(dto.getName());
        employee.setRole(dto.getRole());
        employee.setDepartment(dto.getDepartment());
        employee.setHireDate(dto.getHireDate());
        employee.setStatus(dto.getStatus());
        return employeeRepository.save(employee);
    }

    /**
     * Get employee by badgeId.
     */
    public Optional<Employee> getByBadgeId(String badgeId) {
        return employeeRepository.findByBadgeId(badgeId);
    }

    /**
     * Get paginated employees (not deleted).
     */
    public Page<Employee> getAll(Pageable pageable) {
        return employeeRepository.findAllByDeletedFalse(pageable);
    }

    /**
     * Update employee.
     */
    @Transactional
    public Optional<Employee> updateEmployee(Long id, EmployeeDTO dto) {
        Optional<Employee> opt = employeeRepository.findById(id);
        if (opt.isPresent()) {
            Employee employee = opt.get();
            employee.setName(dto.getName());
            employee.setRole(dto.getRole());
            employee.setDepartment(dto.getDepartment());
            employee.setHireDate(dto.getHireDate());
            employee.setStatus(dto.getStatus());
            return Optional.of(employeeRepository.save(employee));
        }
        return Optional.empty();
    }

    /**
     * Soft-delete employee.
     */
    @Transactional
    public boolean deleteEmployee(Long id) {
        Optional<Employee> opt = employeeRepository.findById(id);
        if (opt.isPresent()) {
            Employee employee = opt.get();
            employee.setDeleted(true);
            employeeRepository.save(employee);
            return true;
        }
        return false;
    }
}
