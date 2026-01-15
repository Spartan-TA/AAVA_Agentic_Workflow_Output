package com.warehouse.ems.employee.service;

import com.warehouse.ems.employee.entity.Employee;
import com.warehouse.ems.employee.dto.EmployeeDTO;
import com.warehouse.ems.employee.repository.EmployeeRepository;
import com.warehouse.ems.employee.mapper.EmployeeMapper;
import com.warehouse.ems.exception.NotFoundException;
import com.warehouse.ems.audit.Auditable;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final EmployeeMapper employeeMapper;

    @Transactional(readOnly = true)
    public Page<EmployeeDTO> getAllEmployees(Pageable pageable, String department, String role) {
        Page<Employee> employees = employeeRepository.findAll(
            (root, query, cb) -> {
                var predicates = cb.conjunction();
                predicates = cb.and(predicates, cb.isFalse(root.get("softDeleted")));
                if (department != null) {
                    predicates = cb.and(predicates, cb.equal(root.get("department"), department));
                }
                if (role != null) {
                    predicates = cb.and(predicates, cb.equal(root.get("role"), role));
                }
                return predicates;
            }, pageable
        );
        return employees.map(employeeMapper::toDto);
    }

    @Transactional(readOnly = true)
    public EmployeeDTO getEmployeeById(Long id) {
        Employee employee = employeeRepository.findById(id)
            .filter(e -> !e.isSoftDeleted())
            .orElseThrow(() -> new NotFoundException("Employee not found"));
        return employeeMapper.toDto(employee);
    }

    @Transactional
    @Auditable(action = "CREATE_EMPLOYEE")
    public EmployeeDTO createEmployee(EmployeeDTO dto) {
        if (employeeRepository.existsByBadgeIdAndSoftDeletedFalse(dto.getBadgeId())) {
            throw new IllegalArgumentException("Badge ID already exists");
        }
        Employee employee = employeeMapper.toEntity(dto);
        employee.setSoftDeleted(false);
        employee = employeeRepository.save(employee);
        return employeeMapper.toDto(employee);
    }

    @Transactional
    @Auditable(action = "UPDATE_EMPLOYEE")
    public EmployeeDTO updateEmployee(Long id, EmployeeDTO dto) {
        Employee employee = employeeRepository.findById(id)
            .filter(e -> !e.isSoftDeleted())
            .orElseThrow(() -> new NotFoundException("Employee not found"));
        employee.setName(dto.getName());
        employee.setRole(dto.getRole());
        employee.setDepartment(dto.getDepartment());
        employee.setShiftGroup(dto.getShiftGroup());
        employee.setHireDate(dto.getHireDate());
        employee.setStatus(dto.getStatus());
        employee = employeeRepository.save(employee);
        return employeeMapper.toDto(employee);
    }

    @Transactional
    @Auditable(action = "SOFT_DELETE_EMPLOYEE")
    public void softDeleteEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
            .filter(e -> !e.isSoftDeleted())
            .orElseThrow(() -> new NotFoundException("Employee not found"));
        employee.setSoftDeleted(true);
        employeeRepository.save(employee);
    }
}