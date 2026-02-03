package com.wms.core.service.impl;

import com.wms.core.domain.Employee;
import com.wms.core.repository.EmployeeRepository;
import com.wms.core.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeServiceImpl implements EmployeeService {
    private final EmployeeRepository employeeRepository;

    @Override
    public Employee create(Employee employee) {
        if (employeeRepository.existsByBadgeId(employee.getBadgeId())) {
            throw new IllegalArgumentException("Badge ID already exists");
        }
        return employeeRepository.save(employee);
    }

    @Override
    public Employee update(Long id, Employee employee) {
        Employee existing = employeeRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Employee not found"));
        existing.setName(employee.getName());
        existing.setRole(employee.getRole());
        existing.setDepartment(employee.getDepartment());
        existing.setShiftGroup(employee.getShiftGroup());
        existing.setHireDate(employee.getHireDate());
        existing.setStatus(employee.getStatus());
        return employeeRepository.save(existing);
    }

    @Override
    public void softDelete(Long id) {
        Employee employee = employeeRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Employee not found"));
        employee.setSoftDeleted(true);
        employee.setStatus("INACTIVE");
        employeeRepository.save(employee);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Employee> findById(Long id) {
        return employeeRepository.findById(id).filter(e -> !e.isSoftDeleted());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Employee> findAll(String filter, Pageable pageable) {
        // Implement filtering logic using JpaSpecificationExecutor if needed
        return employeeRepository.findAll(pageable);
    }
}