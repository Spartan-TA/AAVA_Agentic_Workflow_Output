package com.warehouse.ems.service;

import com.warehouse.ems.domain.Employee;
import com.warehouse.ems.dto.EmployeeRequestDto;
import com.warehouse.ems.dto.EmployeeResponseDto;
import com.warehouse.ems.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Service for Employee business logic.
 * Epics: E02 (Employee Master Data CRUD)
 */
@Service
public class EmployeeService {
    @Autowired
    private EmployeeRepository employeeRepository;

    /**
     * Create a new employee, enforcing unique badgeId.
     */
    @Transactional
    public EmployeeResponseDto createEmployee(EmployeeRequestDto dto) {
        if (employeeRepository.findByBadgeIdAndDeletedFalse(dto.getBadgeId()).isPresent()) {
            throw new IllegalArgumentException("Badge ID already exists.");
        }
        Employee employee = mapToEntity(dto);
        employee.setDeleted(false);
        Employee saved = employeeRepository.save(employee);
        return mapToResponseDto(saved);
    }

    /**
     * Get paginated, filtered list of employees.
     */
    public Page<EmployeeResponseDto> getEmployees(String name, String department, String role, Pageable pageable) {
        Page<Employee> page = employeeRepository.filterEmployees(name, department, role, pageable);
        return page.map(this::mapToResponseDto);
    }

    /**
     * Get employee by ID.
     */
    public Optional<EmployeeResponseDto> getEmployee(Long id) {
        return employeeRepository.findById(id)
                .filter(e -> !e.isDeleted())
                .map(this::mapToResponseDto);
    }

    /**
     * Update employee details.
     */
    @Transactional
    public Optional<EmployeeResponseDto> updateEmployee(Long id, EmployeeRequestDto dto) {
        return employeeRepository.findById(id).filter(e -> !e.isDeleted()).map(employee -> {
            employee.setName(dto.getName());
            employee.setRole(dto.getRole());
            employee.setDepartment(dto.getDepartment());
            employee.setShiftGroup(dto.getShiftGroup());
            employee.setHireDate(dto.getHireDate());
            employee.setStatus(dto.getStatus());
            // badgeId cannot be changed
            Employee updated = employeeRepository.save(employee);
            return mapToResponseDto(updated);
        });
    }

    /**
     * Soft-delete employee.
     */
    @Transactional
    public boolean deleteEmployee(Long id) {
        return employeeRepository.findById(id).filter(e -> !e.isDeleted()).map(employee -> {
            employee.setDeleted(true);
            employeeRepository.save(employee);
            return true;
        }).orElse(false);
    }

    /**
     * Map request DTO to entity.
     */
    private Employee mapToEntity(EmployeeRequestDto dto) {
        Employee e = new Employee();
        e.setName(dto.getName());
        e.setBadgeId(dto.getBadgeId());
        e.setRole(dto.getRole());
        e.setDepartment(dto.getDepartment());
        e.setShiftGroup(dto.getShiftGroup());
        e.setHireDate(dto.getHireDate());
        e.setStatus(dto.getStatus());
        return e;
    }

    /**
     * Map entity to response DTO.
     */
    private EmployeeResponseDto mapToResponseDto(Employee e) {
        EmployeeResponseDto dto = new EmployeeResponseDto();
        dto.setId(e.getId());
        dto.setName(e.getName());
        dto.setBadgeId(e.getBadgeId());
        dto.setRole(e.getRole());
        dto.setDepartment(e.getDepartment());
        dto.setShiftGroup(e.getShiftGroup());
        dto.setHireDate(e.getHireDate());
        dto.setStatus(e.getStatus());
        return dto;
    }
}
