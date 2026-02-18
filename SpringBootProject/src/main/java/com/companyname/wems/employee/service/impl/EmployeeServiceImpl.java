package com.companyname.wems.employee.service.impl;

import com.companyname.wems.employee.dto.EmployeeDto;
import com.companyname.wems.employee.entity.Employee;
import com.companyname.wems.employee.repository.EmployeeRepository;
import com.companyname.wems.employee.service.EmployeeService;
import com.companyname.wems.common.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;

    @Override
    @Transactional
    public EmployeeDto createEmployee(EmployeeDto dto) {
        Employee employee = Employee.builder()
                .badgeId(dto.getBadgeId())
                .name(dto.getName())
                .role(dto.getRole())
                .department(dto.getDepartment())
                .shiftGroup(dto.getShiftGroup())
                .hireDate(dto.getHireDate())
                .status(dto.getStatus())
                .deleted(false)
                .build();
        employee = employeeRepository.save(employee);
        return toDto(employee);
    }

    @Override
    @Transactional
    public EmployeeDto updateEmployee(Long id, EmployeeDto dto) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Employee not found"));
        BeanUtils.copyProperties(dto, employee, "id", "badgeId");
        employee = employeeRepository.save(employee);
        return toDto(employee);
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeDto getEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Employee not found"));
        return toDto(employee);
    }

    @Override
    @Transactional
    public void deleteEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Employee not found"));
        employee.setDeleted(true);
        employeeRepository.save(employee);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EmployeeDto> listEmployees(String filter, Pageable pageable) {
        Page<Employee> page = employeeRepository.findAll((root, query, cb) -> {
            if (filter != null && !filter.isEmpty()) {
                return cb.and(
                        cb.equal(root.get("deleted"), false),
                        cb.or(
                                cb.like(cb.lower(root.get("name")), "%" + filter.toLowerCase() + "%"),
                                cb.like(cb.lower(root.get("badgeId")), "%" + filter.toLowerCase() + "%")
                        )
                );
            } else {
                return cb.equal(root.get("deleted"), false);
            }
        }, pageable);
        return page.map(this::toDto);
    }

    private EmployeeDto toDto(Employee employee) {
        return EmployeeDto.builder()
                .id(employee.getId())
                .badgeId(employee.getBadgeId())
                .name(employee.getName())
                .role(employee.getRole())
                .department(employee.getDepartment())
                .shiftGroup(employee.getShiftGroup())
                .hireDate(employee.getHireDate())
                .status(employee.getStatus())
                .build();
    }
}