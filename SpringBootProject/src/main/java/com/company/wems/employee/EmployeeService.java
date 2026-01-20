package com.company.wems.employee;

import com.company.wems.common.exception.DuplicateResourceException;
import com.company.wems.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final EmployeeMapper employeeMapper;

    @Transactional(readOnly = true)
    public Page<EmployeeDto> getAllEmployees(String department, Pageable pageable) {
        Page<Employee> employees = (department != null)
                ? employeeRepository.findAllByDepartment(department, pageable)
                : employeeRepository.findAll(pageable);
        return employees.map(employeeMapper::toDto);
    }

    @Transactional(readOnly = true)
    public EmployeeDto getEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
        return employeeMapper.toDto(employee);
    }

    @Transactional
    public EmployeeDto createEmployee(CreateEmployeeRequest request) {
        if (employeeRepository.existsByBadgeId(request.getBadgeId())) {
            throw new DuplicateResourceException("Badge ID already exists");
        }
        Employee employee = employeeMapper.toEntity(request);
        employee = employeeRepository.save(employee);
        return employeeMapper.toDto(employee);
    }

    @Transactional
    public EmployeeDto updateEmployee(Long id, UpdateEmployeeRequest request) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
        employeeMapper.updateEntityFromDto(request, employee);
        employee = employeeRepository.save(employee);
        return employeeMapper.toDto(employee);
    }

    @Transactional
    public void deleteEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
        employeeRepository.delete(employee);
    }
}