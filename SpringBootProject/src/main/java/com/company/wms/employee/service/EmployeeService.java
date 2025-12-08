package com.company.wms.employee.service;

import com.company.wms.employee.entity.Employee;
import com.company.wms.employee.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Service for Employee business logic.
 */
@Service
@RequiredArgsConstructor
public class EmployeeService {
    private static final Logger log = LoggerFactory.getLogger(EmployeeService.class);
    private final EmployeeRepository employeeRepository;

    public Page<Employee> getAllEmployees(Pageable pageable) {
        return employeeRepository.findAll(pageable);
    }

    public Optional<Employee> getEmployeeById(Long id) {
        return employeeRepository.findById(id);
    }

    public Optional<Employee> getEmployeeByBadgeId(String badgeId) {
        return employeeRepository.findByBadgeId(badgeId);
    }

    public Employee createEmployee(Employee employee) {
        log.info("Creating employee: {}", employee);
        return employeeRepository.save(employee);
    }

    public Employee updateEmployee(Long id, Employee updated) {
        return employeeRepository.findById(id)
                .map(emp -> {
                    emp.setName(updated.getName());
                    emp.setRole(updated.getRole());
                    emp.setDepartment(updated.getDepartment());
                    emp.setShiftGroup(updated.getShiftGroup());
                    emp.setHireDate(updated.getHireDate());
                    emp.setStatus(updated.getStatus());
                    emp.setUpdatedAt(java.time.LocalDateTime.now());
                    return employeeRepository.save(emp);
                })
                .orElseThrow(() -> new RuntimeException("Employee not found"));
    }

    @Transactional
    public void softDeleteEmployee(Long id) {
        employeeRepository.findById(id).ifPresent(emp -> {
            emp.setDeleted(true);
            emp.setUpdatedAt(java.time.LocalDateTime.now());
            employeeRepository.save(emp);
            log.info("Soft-deleted employee: {}", id);
        });
    }
}
