package com.wms.ems.employee.service;

import com.wms.ems.employee.dto.EmployeeDto;
import com.wms.ems.employee.entity.Employee;
import com.wms.ems.employee.mapper.EmployeeMapper;
import com.wms.ems.employee.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Implementation of EmployeeService for CRUD operations.
 */
@Service
@Transactional
public class EmployeeServiceImpl implements EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final EmployeeMapper employeeMapper;

    @Autowired
    public EmployeeServiceImpl(EmployeeRepository employeeRepository, EmployeeMapper employeeMapper) {
        this.employeeRepository = employeeRepository;
        this.employeeMapper = employeeMapper;
    }

    @Override
    public EmployeeDto createEmployee(EmployeeDto dto) {
        Employee entity = employeeMapper.toEntity(dto);
        entity.setDeleted(false);
        Employee saved = employeeRepository.save(entity);
        return employeeMapper.toDto(saved);
    }

    @Override
    public EmployeeDto getEmployeeByBadgeId(String badgeId) {
        Employee employee = employeeRepository.findByBadgeIdAndDeletedFalse(badgeId)
                .orElseThrow(() -> new RuntimeException("Employee not found: " + badgeId));
        return employeeMapper.toDto(employee);
    }

    @Override
    public List<EmployeeDto> getAllEmployees() {
        List<Employee> employees = employeeRepository.findAllByDeletedFalse();
        return employeeMapper.toDtoList(employees);
    }

    @Override
    public EmployeeDto updateEmployee(String badgeId, EmployeeDto dto) {
        Employee employee = employeeRepository.findByBadgeIdAndDeletedFalse(badgeId)
                .orElseThrow(() -> new RuntimeException("Employee not found: " + badgeId));
        employee.setName(dto.getName());
        employee.setRole(dto.getRole());
        employee.setDepartment(dto.getDepartment());
        employee.setShiftGroup(dto.getShiftGroup());
        employee.setHireDate(dto.getHireDate());
        employee.setStatus(dto.getStatus());
        Employee updated = employeeRepository.save(employee);
        return employeeMapper.toDto(updated);
    }

    @Override
    public void softDeleteEmployee(String badgeId) {
        Employee employee = employeeRepository.findByBadgeIdAndDeletedFalse(badgeId)
                .orElseThrow(() -> new RuntimeException("Employee not found: " + badgeId));
        employee.setDeleted(true);
        employeeRepository.save(employee);
    }
}
