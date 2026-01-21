package com.wms.employee;

import com.wms.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for Employee CRUD operations.
 */
@Service
@RequiredArgsConstructor
public class EmployeeService {
    private final EmployeeRepository employeeRepository;

    public EmployeeDTO create(EmployeeDTO dto) {
        if (employeeRepository.findByBadgeId(dto.getBadgeId()).isPresent()) {
            throw new IllegalArgumentException("Badge ID must be unique");
        }
        Employee employee = EmployeeMapper.toEntity(dto);
        employee.setDeleted(false);
        return EmployeeMapper.toDto(employeeRepository.save(employee));
    }

    public EmployeeDTO get(Long id) {
        Employee employee = employeeRepository.findById(id)
                .filter(e -> !e.getDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
        return EmployeeMapper.toDto(employee);
    }

    public Page<EmployeeDTO> list(int page, int size, String filter) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Employee> employees = employeeRepository.findAll(pageable);
        return employees.map(EmployeeMapper::toDto);
    }

    @Transactional
    public EmployeeDTO update(Long id, EmployeeDTO dto) {
        Employee employee = employeeRepository.findById(id)
                .filter(e -> !e.getDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
        employee.setName(dto.getName());
        employee.setRole(dto.getRole());
        employee.setDepartment(dto.getDepartment());
        employee.setShiftGroup(dto.getShiftGroup());
        employee.setHireDate(dto.getHireDate());
        employee.setStatus(dto.getStatus());
        return EmployeeMapper.toDto(employeeRepository.save(employee));
    }

    @Transactional
    public void delete(Long id) {
        Employee employee = employeeRepository.findById(id)
                .filter(e -> !e.getDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
        employee.setDeleted(true);
        employeeRepository.save(employee);
    }
}