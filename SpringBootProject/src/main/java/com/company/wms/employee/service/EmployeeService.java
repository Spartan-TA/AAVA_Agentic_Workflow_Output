package com.company.wms.employee.service;

import com.company.wms.employee.dto.EmployeeDTO;
import com.company.wms.employee.entity.Employee;
import com.company.wms.employee.mapper.EmployeeMapper;
import com.company.wms.employee.repository.EmployeeRepository;
import com.company.wms.exception.NotFoundException;
import com.company.wms.exception.DuplicateResourceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service layer for Employee operations.
 * Handles business logic, validation, and transaction management.
 * 
 * @author WMS Development Team
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmployeeService {
    
    private final EmployeeRepository employeeRepository;
    private final EmployeeMapper employeeMapper;

    /**
     * Get all active employees with pagination and optional filtering
     * @param page page number (0-indexed)
     * @param size page size
     * @param department optional department filter
     * @param role optional role filter
     * @param searchTerm optional name search term
     * @return Page of EmployeeDTOs
     */
    @Transactional(readOnly = true)
    public Page<EmployeeDTO> getAll(int page, int size, String department, String role, String searchTerm) {
        log.info("Fetching employees - page: {}, size: {}, dept: {}, role: {}, search: {}", 
                 page, size, department, role, searchTerm);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Employee> employees;
        
        if (searchTerm != null && !searchTerm.isBlank()) {
            employees = employeeRepository.searchByName(searchTerm, pageable);
        } else if (department != null && !department.isBlank()) {
            employees = employeeRepository.findByDepartmentAndDeletedFalse(department, pageable);
        } else if (role != null && !role.isBlank()) {
            employees = employeeRepository.findByRoleAndDeletedFalse(role, pageable);
        } else {
            employees = employeeRepository.findAllActive(pageable);
        }
        
        return employees.map(employeeMapper::toDto);
    }

    /**
     * Get employee by ID
     * @param id employee ID
     * @return EmployeeDTO
     * @throws NotFoundException if employee not found or deleted
     */
    @Transactional(readOnly = true)
    public EmployeeDTO getById(Long id) {
        log.info("Fetching employee by ID: {}", id);
        Employee employee = employeeRepository.findActiveById(id)
                .orElseThrow(() -> new NotFoundException("Employee not found with ID: " + id));
        return employeeMapper.toDto(employee);
    }

    /**
     * Get employee by badge ID
     * @param badgeId unique badge identifier
     * @return EmployeeDTO
     * @throws NotFoundException if employee not found
     */
    @Transactional(readOnly = true)
    public EmployeeDTO getByBadgeId(String badgeId) {
        log.info("Fetching employee by badge ID: {}", badgeId);
        Employee employee = employeeRepository.findByBadgeIdAndDeletedFalse(badgeId)
                .orElseThrow(() -> new NotFoundException("Employee not found with badge ID: " + badgeId));
        return employeeMapper.toDto(employee);
    }

    /**
     * Create new employee
     * @param dto employee data
     * @return created EmployeeDTO
     * @throws DuplicateResourceException if badge ID already exists
     */
    @Transactional
    public EmployeeDTO create(EmployeeDTO dto) {
        log.info("Creating new employee with badge ID: {}", dto.getBadgeId());
        
        // Check for duplicate badge ID
        if (employeeRepository.findByBadgeIdAndDeletedFalse(dto.getBadgeId()).isPresent()) {
            throw new DuplicateResourceException("Employee with badge ID " + dto.getBadgeId() + " already exists");
        }
        
        Employee employee = employeeMapper.toEntity(dto);
        employee.setDeleted(false);
        Employee saved = employeeRepository.save(employee);
        
        log.info("Employee created successfully with ID: {}", saved.getId());
        return employeeMapper.toDto(saved);
    }

    /**
     * Update existing employee
     * @param id employee ID
     * @param dto updated employee data
     * @return updated EmployeeDTO
     * @throws NotFoundException if employee not found
     */
    @Transactional
    public EmployeeDTO update(Long id, EmployeeDTO dto) {
        log.info("Updating employee with ID: {}", id);
        
        Employee employee = employeeRepository.findActiveById(id)
                .orElseThrow(() -> new NotFoundException("Employee not found with ID: " + id));
        
        // Update fields using mapper
        employeeMapper.updateEntityFromDto(dto, employee);
        
        Employee updated = employeeRepository.save(employee);
        log.info("Employee updated successfully with ID: {}", updated.getId());
        
        return employeeMapper.toDto(updated);
    }

    /**
     * Soft delete employee
     * @param id employee ID
     * @throws NotFoundException if employee not found
     */
    @Transactional
    public void delete(Long id) {
        log.info("Soft deleting employee with ID: {}", id);
        
        Employee employee = employeeRepository.findActiveById(id)
                .orElseThrow(() -> new NotFoundException("Employee not found with ID: " + id));
        
        employee.setDeleted(true);
        employee.setStatus("TERMINATED");
        employeeRepository.save(employee);
        
        log.info("Employee soft deleted successfully with ID: {}", id);
    }

    /**
     * Get employees by department
     * @param department department name
     * @return list of EmployeeDTOs
     */
    @Transactional(readOnly = true)
    public List<EmployeeDTO> getByDepartment(String department) {
        log.info("Fetching employees for department: {}", department);
        Page<Employee> employees = employeeRepository.findByDepartmentAndDeletedFalse(
                department, Pageable.unpaged());
        return employeeMapper.toDtoList(employees.getContent());
    }

    /**
     * Get employees by shift group
     * @param shiftGroup shift group identifier
     * @return list of EmployeeDTOs
     */
    @Transactional(readOnly = true)
    public List<EmployeeDTO> getByShiftGroup(String shiftGroup) {
        log.info("Fetching employees for shift group: {}", shiftGroup);
        List<Employee> employees = employeeRepository.findByShiftGroupAndDeletedFalse(shiftGroup);
        return employeeMapper.toDtoList(employees);
    }

    /**
     * Count active employees by department
     * @param department department name
     * @return count of employees
     */
    @Transactional(readOnly = true)
    public long countByDepartment(String department) {
        return employeeRepository.countByDepartmentAndDeletedFalse(department);
    }
}