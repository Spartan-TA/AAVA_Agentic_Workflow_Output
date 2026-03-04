package com.wms.employee.service;

import com.wms.employee.dto.EmployeeDTO;
import com.wms.employee.model.Employee;
import com.wms.employee.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service layer for Employee business logic.
 */
@Service
public class EmployeeService {
    @Autowired
    private EmployeeRepository employeeRepository;

    /**
     * Create a new employee.
     */
    @Transactional
    public EmployeeDTO createEmployee(EmployeeDTO dto) {
        Employee employee = new Employee();
        employee.setBadgeId(dto.getBadgeId());
        employee.setName(dto.getName());
        employee.setRole(dto.getRole());
        employee.setDepartment(dto.getDepartment());
        employee.setShiftGroup(dto.getShiftGroup());
        employee.setHireDate(dto.getHireDate());
        employee.setStatus(dto.getStatus());
        employee.setDeleted(false);
        Employee saved = employeeRepository.save(employee);
        return toDTO(saved);
    }

    /**
     * Get all non-deleted employees.
     */
    public List<EmployeeDTO> getAllEmployees() {
        return employeeRepository.findAllByDeletedFalse().stream().map(this::toDTO).collect(Collectors.toList());
    }

    /**
     * Get employee by badge ID.
     */
    public Optional<EmployeeDTO> getByBadgeId(String badgeId) {
        return employeeRepository.findByBadgeId(badgeId).filter(e -> !e.isDeleted()).map(this::toDTO);
    }

    /**
     * Update employee by ID.
     */
    @Transactional
    public Optional<EmployeeDTO> updateEmployee(Long id, EmployeeDTO dto) {
        return employeeRepository.findById(id).filter(e -> !e.isDeleted()).map(employee -> {
            employee.setName(dto.getName());
            employee.setRole(dto.getRole());
            employee.setDepartment(dto.getDepartment());
            employee.setShiftGroup(dto.getShiftGroup());
            employee.setHireDate(dto.getHireDate());
            employee.setStatus(dto.getStatus());
            return toDTO(employeeRepository.save(employee));
        });
    }

    /**
     * Soft delete employee by ID.
     */
    @Transactional
    public boolean deleteEmployee(Long id) {
        return employeeRepository.findById(id).filter(e -> !e.isDeleted()).map(employee -> {
            employee.setDeleted(true);
            employeeRepository.save(employee);
            return true;
        }).orElse(false);
    }

    private EmployeeDTO toDTO(Employee e) {
        EmployeeDTO dto = new EmployeeDTO();
        dto.setId(e.getId());
        dto.setBadgeId(e.getBadgeId());
        dto.setName(e.getName());
        dto.setRole(e.getRole());
        dto.setDepartment(e.getDepartment());
        dto.setShiftGroup(e.getShiftGroup());
        dto.setHireDate(e.getHireDate());
        dto.setStatus(e.getStatus());
        return dto;
    }
}
