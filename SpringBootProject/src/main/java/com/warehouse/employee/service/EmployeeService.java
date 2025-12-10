package com.warehouse.employee.service;

import com.warehouse.employee.dto.EmployeeDTO;
import com.warehouse.employee.model.Employee;
import com.warehouse.employee.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Service for Employee business logic.
 */
@Service
public class EmployeeService {
    @Autowired
    private EmployeeRepository employeeRepository;

    public Page<EmployeeDTO> getAllEmployees(Pageable pageable, String filter) {
        if (filter != null && !filter.isEmpty()) {
            return employeeRepository.filterByNameOrDepartment(filter, pageable)
                    .map(this::toDTO);
        }
        return employeeRepository.findAllByDeletedFalse(pageable)
                .map(this::toDTO);
    }

    public Optional<EmployeeDTO> getEmployeeByBadgeId(String badgeId) {
        return employeeRepository.findByBadgeIdAndDeletedFalse(badgeId)
                .map(this::toDTO);
    }

    @Transactional
    public EmployeeDTO createEmployee(EmployeeDTO dto) {
        Employee employee = toEntity(dto);
        employee.setDeleted(false);
        Employee saved = employeeRepository.save(employee);
        return toDTO(saved);
    }

    @Transactional
    public Optional<EmployeeDTO> updateEmployee(Long id, EmployeeDTO dto) {
        return employeeRepository.findById(id).filter(e -> !e.isDeleted()).map(existing -> {
            existing.setName(dto.getName());
            existing.setRole(dto.getRole());
            existing.setDepartment(dto.getDepartment());
            existing.setShiftGroup(dto.getShiftGroup());
            existing.setHireDate(dto.getHireDate());
            existing.setStatus(dto.getStatus());
            return toDTO(employeeRepository.save(existing));
        });
    }

    @Transactional
    public boolean softDeleteEmployee(Long id) {
        return employeeRepository.findById(id).filter(e -> !e.isDeleted()).map(e -> {
            e.setDeleted(true);
            employeeRepository.save(e);
            return true;
        }).orElse(false);
    }

    private EmployeeDTO toDTO(Employee employee) {
        EmployeeDTO dto = new EmployeeDTO();
        dto.setId(employee.getId());
        dto.setName(employee.getName());
        dto.setBadgeId(employee.getBadgeId());
        dto.setRole(employee.getRole());
        dto.setDepartment(employee.getDepartment());
        dto.setShiftGroup(employee.getShiftGroup());
        dto.setHireDate(employee.getHireDate());
        dto.setStatus(employee.getStatus());
        return dto;
    }

    private Employee toEntity(EmployeeDTO dto) {
        Employee employee = new Employee();
        employee.setName(dto.getName());
        employee.setBadgeId(dto.getBadgeId());
        employee.setRole(dto.getRole());
        employee.setDepartment(dto.getDepartment());
        employee.setShiftGroup(dto.getShiftGroup());
        employee.setHireDate(dto.getHireDate());
        employee.setStatus(dto.getStatus());
        return employee;
    }
}
