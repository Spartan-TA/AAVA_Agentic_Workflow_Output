package com.warehouse.ems.employee;

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
    private final EmployeeRepository employeeRepository;

    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    public Page<EmployeeDTO> getEmployees(String department, String role, Pageable pageable) {
        return employeeRepository.filterByDepartmentAndRole(department, role, pageable)
                .map(this::toDto);
    }

    public Optional<EmployeeDTO> getEmployeeById(Long id) {
        return employeeRepository.findById(id).map(this::toDto);
    }

    public Optional<EmployeeDTO> getEmployeeByBadgeId(String badgeId) {
        return employeeRepository.findByBadgeId(badgeId).map(this::toDto);
    }

    @Transactional
    public EmployeeDTO createEmployee(EmployeeDTO dto, String createdBy) {
        Employee employee = toEntity(dto);
        employee.setCreatedBy(createdBy);
        employee.setCreatedAt(java.time.OffsetDateTime.now());
        employee.setDeleted(false);
        return toDto(employeeRepository.save(employee));
    }

    @Transactional
    public Optional<EmployeeDTO> updateEmployee(Long id, EmployeeDTO dto, String updatedBy) {
        return employeeRepository.findById(id).map(employee -> {
            employee.setName(dto.getName());
            employee.setBadgeId(dto.getBadgeId());
            employee.setRole(dto.getRole());
            employee.setDepartment(dto.getDepartment());
            employee.setShiftGroup(dto.getShiftGroup());
            employee.setHireDate(dto.getHireDate());
            employee.setStatus(dto.getStatus());
            employee.setUpdatedBy(updatedBy);
            employee.setUpdatedAt(java.time.OffsetDateTime.now());
            return toDto(employeeRepository.save(employee));
        });
    }

    @Transactional
    public boolean softDeleteEmployee(Long id, String updatedBy) {
        return employeeRepository.findById(id).map(employee -> {
            employee.setDeleted(true);
            employee.setUpdatedBy(updatedBy);
            employee.setUpdatedAt(java.time.OffsetDateTime.now());
            employeeRepository.save(employee);
            return true;
        }).orElse(false);
    }

    private EmployeeDTO toDto(Employee employee) {
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
