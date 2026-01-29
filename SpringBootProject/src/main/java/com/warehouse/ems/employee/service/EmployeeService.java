package com.warehouse.ems.employee.service;

import com.warehouse.ems.employee.dto.*;
import com.warehouse.ems.employee.entity.EmployeeEntity;
import com.warehouse.ems.employee.repository.EmployeeRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EmployeeService {
    private final EmployeeRepository employeeRepository;

    public Page<EmployeeDto> getAllEmployees(Pageable pageable) {
        return employeeRepository.findAll(pageable)
                .map(this::toDto);
    }

    public EmployeeDto getEmployeeById(Long id) {
        EmployeeEntity entity = employeeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found"));
        return toDto(entity);
    }

    public EmployeeDto getEmployeeByBadgeId(String badgeId) {
        EmployeeEntity entity = employeeRepository.findByBadgeId(badgeId)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found"));
        return toDto(entity);
    }

    @Transactional
    public EmployeeDto createEmployee(EmployeeCreateDto dto) {
        EmployeeEntity entity = EmployeeEntity.builder()
                .badgeId(dto.getBadgeId())
                .name(dto.getName())
                .role(dto.getRole())
                .department(dto.getDepartment())
                .shiftGroup(dto.getShiftGroup())
                .hireDate(dto.getHireDate())
                .status(dto.getStatus())
                .deleted(false)
                .build();
        return toDto(employeeRepository.save(entity));
    }

    @Transactional
    public EmployeeDto updateEmployee(Long id, EmployeeUpdateDto dto) {
        EmployeeEntity entity = employeeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found"));
        entity.setName(dto.getName());
        entity.setRole(dto.getRole());
        entity.setDepartment(dto.getDepartment());
        entity.setShiftGroup(dto.getShiftGroup());
        entity.setHireDate(dto.getHireDate());
        entity.setStatus(dto.getStatus());
        return toDto(employeeRepository.save(entity));
    }

    @Transactional
    public void deleteEmployee(Long id) {
        EmployeeEntity entity = employeeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found"));
        employeeRepository.delete(entity); // triggers soft-delete
    }

    private EmployeeDto toDto(EmployeeEntity entity) {
        return EmployeeDto.builder()
                .id(entity.getId())
                .badgeId(entity.getBadgeId())
                .name(entity.getName())
                .role(entity.getRole())
                .department(entity.getDepartment())
                .shiftGroup(entity.getShiftGroup())
                .hireDate(entity.getHireDate())
                .status(entity.getStatus())
                .build();
    }
}
