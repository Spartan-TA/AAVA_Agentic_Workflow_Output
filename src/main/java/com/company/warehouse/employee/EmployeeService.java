package com.company.warehouse.employee;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EmployeeService {
    private final EmployeeRepository employeeRepository;

    @Transactional(readOnly = true)
    public Page<Employee> getAllEmployees(Pageable pageable) {
        return employeeRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Optional<Employee> getEmployeeById(Long id) {
        return employeeRepository.findById(id);
    }

    @Transactional
    public Employee createEmployee(Employee employee) {
        // Validation: badgeId must be unique
        if (employeeRepository.findByBadgeId(employee.getBadgeId()).isPresent()) {
            throw new DuplicateResourceException("Badge ID already exists");
        }
        return employeeRepository.save(employee);
    }

    @Transactional
    public Employee updateEmployee(Long id, Employee updated) {
        Employee existing = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
        // Update fields
        existing.setName(updated.getName());
        existing.setRole(updated.getRole());
        existing.setDepartment(updated.getDepartment());
        existing.setShiftGroup(updated.getShiftGroup());
        existing.setHireDate(updated.getHireDate());
        existing.setStatus(updated.getStatus());
        return employeeRepository.save(existing);
    }

    @Transactional
    public void softDeleteEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
        employeeRepository.delete(employee); // Triggers @SQLDelete
    }
}
