package com.warehouse.ems.employee.service.impl;

import com.warehouse.ems.employee.model.dto.EmployeeRequestDto;
import com.warehouse.ems.employee.model.dto.EmployeeResponseDto;
import com.warehouse.ems.employee.model.entity.Employee;
import com.warehouse.ems.employee.repository.EmployeeRepository;
import com.warehouse.ems.employee.service.EmployeeService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class EmployeeServiceImpl implements EmployeeService {
    private final EmployeeRepository employeeRepository;

    @Autowired
    public EmployeeServiceImpl(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Override
    @Transactional
    public EmployeeResponseDto createEmployee(EmployeeRequestDto requestDto) {
        if (employeeRepository.findByBadgeIdAndDeletedFalse(requestDto.getBadgeId()).isPresent()) {
            throw new IllegalArgumentException("Badge ID already exists");
        }
        Employee employee = new Employee();
        employee.setBadgeId(requestDto.getBadgeId());
        employee.setName(requestDto.getName());
        employee.setRole(requestDto.getRole());
        employee.setDepartment(requestDto.getDepartment());
        employee.setShiftGroup(requestDto.getShiftGroup());
        employee.setHireDate(requestDto.getHireDate());
        employee.setStatus(requestDto.getStatus());
        employee.setDeleted(false);
        Employee saved = employeeRepository.save(employee);
        return toResponseDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeResponseDto getEmployeeById(UUID id) {
        Employee employee = employeeRepository.findById(id)
                .filter(e -> !e.isDeleted())
                .orElseThrow(() -> new EntityNotFoundException("Employee not found"));
        return toResponseDto(employee);
    }

    @Override
    @Transactional
    public EmployeeResponseDto updateEmployee(UUID id, EmployeeRequestDto requestDto) {
        Employee employee = employeeRepository.findById(id)
                .filter(e -> !e.isDeleted())
                .orElseThrow(() -> new EntityNotFoundException("Employee not found"));
        employee.setName(requestDto.getName());
        employee.setRole(requestDto.getRole());
        employee.setDepartment(requestDto.getDepartment());
        employee.setShiftGroup(requestDto.getShiftGroup());
        employee.setHireDate(requestDto.getHireDate());
        employee.setStatus(requestDto.getStatus());
        Employee saved = employeeRepository.save(employee);
        return toResponseDto(saved);
    }

    @Override
    @Transactional
    public void softDeleteEmployee(UUID id) {
        Employee employee = employeeRepository.findById(id)
                .filter(e -> !e.isDeleted())
                .orElseThrow(() -> new EntityNotFoundException("Employee not found"));
        employee.setDeleted(true);
        employeeRepository.save(employee);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EmployeeResponseDto> getAllEmployees(String search, Pageable pageable) {
        Page<Employee> page;
        if (search != null && !search.isBlank()) {
            page = employeeRepository.searchActiveEmployees(search, pageable);
        } else {
            page = employeeRepository.findAllByDeletedFalse(pageable);
        }
        return page.map(this::toResponseDto);
    }

    private EmployeeResponseDto toResponseDto(Employee employee) {
        EmployeeResponseDto dto = new EmployeeResponseDto();
        dto.setId(employee.getId());
        dto.setBadgeId(employee.getBadgeId());
        dto.setName(employee.getName());
        dto.setRole(employee.getRole());
        dto.setDepartment(employee.getDepartment());
        dto.setShiftGroup(employee.getShiftGroup());
        dto.setHireDate(employee.getHireDate());
        dto.setStatus(employee.getStatus());
        return dto;
    }
}
