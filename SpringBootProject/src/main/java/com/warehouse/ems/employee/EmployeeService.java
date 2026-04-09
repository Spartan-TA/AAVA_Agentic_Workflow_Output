package com.warehouse.ems.employee;

import com.warehouse.ems.employee.dto.EmployeeCreateDto;
import com.warehouse.ems.employee.dto.EmployeeDto;
import com.warehouse.ems.employee.dto.EmployeeUpdateDto;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Service for Employee CRUD and business logic.
 */
@Service
@RequiredArgsConstructor
public class EmployeeService {
    private final EmployeeRepository employeeRepository;

    @Transactional(readOnly = true)
    public Page<EmployeeDto> getAllEmployees(String department, String role, Pageable pageable) {
        if (department != null) {
            return employeeRepository.findAllByDepartment(department, pageable).map(EmployeeDto::fromEntity);
        } else if (role != null) {
            return employeeRepository.findAllByRole(role, pageable).map(EmployeeDto::fromEntity);
        } else {
            return employeeRepository.findAll(pageable).map(EmployeeDto::fromEntity);
        }
    }

    @Transactional(readOnly = true)
    public EmployeeDto getEmployee(Long id) {
        return employeeRepository.findById(id)
                .map(EmployeeDto::fromEntity)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found"));
    }

    @Transactional
    public EmployeeDto createEmployee(EmployeeCreateDto dto) {
        Employee employee = Employee.builder()
                .name(dto.getName())
                .badgeId(dto.getBadgeId())
                .role(dto.getRole())
                .department(dto.getDepartment())
                .shiftGroup(dto.getShiftGroup())
                .hireDate(dto.getHireDate())
                .status(dto.getStatus())
                .build();
        return EmployeeDto.fromEntity(employeeRepository.save(employee));
    }

    @Transactional
    public EmployeeDto updateEmployee(Long id, EmployeeUpdateDto dto) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found"));
        if (dto.getName() != null) employee.setName(dto.getName());
        if (dto.getRole() != null) employee.setRole(dto.getRole());
        if (dto.getDepartment() != null) employee.setDepartment(dto.getDepartment());
        if (dto.getShiftGroup() != null) employee.setShiftGroup(dto.getShiftGroup());
        if (dto.getStatus() != null) employee.setStatus(dto.getStatus());
        return EmployeeDto.fromEntity(employeeRepository.save(employee));
    }

    @Transactional
    public void deleteEmployee(Long id) {
        employeeRepository.deleteById(id);
    }
}
