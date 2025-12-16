package com.warehouse.management.employee.service;

import com.warehouse.management.common.dto.PageResponse;
import com.warehouse.management.employee.dto.EmployeeCreateRequest;
import com.warehouse.management.employee.dto.EmployeeDTO;
import com.warehouse.management.employee.dto.EmployeeUpdateRequest;
import com.warehouse.management.employee.entity.Employee;
import com.warehouse.management.employee.entity.Employee.Status;
import com.warehouse.management.employee.entity.Employee.Role;
import com.warehouse.management.employee.exception.DuplicateBadgeIdException;
import com.warehouse.management.employee.exception.EmployeeNotFoundException;
import com.warehouse.management.employee.mapper.EmployeeMapper;
import com.warehouse.management.employee.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for Employee business logic.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final EmployeeMapper employeeMapper;

    @Transactional
    public EmployeeDTO createEmployee(EmployeeCreateRequest request, String createdBy) {
        log.info("Creating employee with badgeId: {}", request.getBadgeId());
        if (employeeRepository.findByBadgeId(request.getBadgeId()).isPresent()) {
            throw new DuplicateBadgeIdException(request.getBadgeId());
        }
        Employee employee = employeeMapper.toEntity(request);
        employee.setCreatedBy(createdBy);
        employee.setUpdatedBy(createdBy);
        Employee saved = employeeRepository.save(employee);
        return employeeMapper.toDTO(saved);
    }

    @Transactional
    public EmployeeDTO updateEmployee(Long id, EmployeeUpdateRequest request, String updatedBy) {
        log.info("Updating employee id: {}", id);
        Employee employee = employeeRepository.findById(id).orElseThrow(() -> new EmployeeNotFoundException(id));
        if (request.getBadgeId() != null && !request.getBadgeId().equals(employee.getBadgeId())) {
            if (employeeRepository.findByBadgeId(request.getBadgeId()).isPresent()) {
                throw new DuplicateBadgeIdException(request.getBadgeId());
            }
        }
        employeeMapper.updateEntity(employee, request);
        employee.setUpdatedBy(updatedBy);
        Employee saved = employeeRepository.save(employee);
        return employeeMapper.toDTO(saved);
    }

    @Transactional(readOnly = true)
    public EmployeeDTO getEmployeeById(Long id) {
        log.info("Fetching employee by id: {}", id);
        Employee employee = employeeRepository.findById(id).orElseThrow(() -> new EmployeeNotFoundException(id));
        return employeeMapper.toDTO(employee);
    }

    @Transactional(readOnly = true)
    public PageResponse<EmployeeDTO> getAllEmployees(Status status, String department, Role role, int page, int size) {
        log.info("Fetching employees with filters - status: {}, department: {}, role: {}", status, department, role);
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Employee> employees;
        if (status != null && department != null && role != null) {
            employees = employeeRepository.findByStatusAndDepartmentAndRole(status, department, role, pageable);
        } else if (status != null && department != null) {
            employees = employeeRepository.findByStatusAndDepartment(status, department, pageable);
        } else if (status != null && role != null) {
            employees = employeeRepository.findByStatusAndRole(status, role, pageable);
        } else if (department != null && role != null) {
            employees = employeeRepository.findByDepartmentAndRole(department, role, pageable);
        } else if (status != null) {
            employees = employeeRepository.findByStatus(status, pageable);
        } else if (department != null) {
            employees = employeeRepository.findByDepartment(department, pageable);
        } else if (role != null) {
            employees = employeeRepository.findByRole(role, pageable);
        } else {
            employees = employeeRepository.findAll(pageable);
        }
        return PageResponse.<EmployeeDTO>builder()
                .pageNumber(employees.getNumber())
                .pageSize(employees.getSize())
                .totalPages(employees.getTotalPages())
                .totalElements(employees.getTotalElements())
                .content(employees.getContent().stream().map(employeeMapper::toDTO).collect(Collectors.toList()))
                .build();
    }

    @Transactional
    public void softDeleteEmployee(Long id, String updatedBy) {
        log.info("Soft deleting employee id: {}", id);
        Employee employee = employeeRepository.findById(id).orElseThrow(() -> new EmployeeNotFoundException(id));
        employee.setStatus(Status.INACTIVE);
        employee.setUpdatedBy(updatedBy);
        employee.setUpdatedAt(LocalDateTime.now());
        employeeRepository.save(employee);
    }

    @Transactional
    public void restoreEmployee(Long id, String updatedBy) {
        log.info("Restoring employee id: {}", id);
        Employee employee = employeeRepository.findById(id).orElseThrow(() -> new EmployeeNotFoundException(id));
        employee.setStatus(Status.ACTIVE);
        employee.setUpdatedBy(updatedBy);
        employee.setUpdatedAt(LocalDateTime.now());
        employeeRepository.save(employee);
    }
}
