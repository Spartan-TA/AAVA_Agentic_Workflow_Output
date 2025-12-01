package com.warehouse.ems.employee.service;

import com.warehouse.ems.employee.dto.EmployeeRequestDTO;
import com.warehouse.ems.employee.dto.EmployeeResponseDTO;
import com.warehouse.ems.employee.entity.Employee;
import com.warehouse.ems.employee.repository.EmployeeRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

/**
 * Service for Employee business logic.
 */
@Service
@RequiredArgsConstructor
public class EmployeeService {
    private final EmployeeRepository employeeRepository;

    @Transactional(readOnly = true)
    public Page<EmployeeResponseDTO> getAllEmployees(Pageable pageable) {
        return employeeRepository.findAllByDeletedFalse(pageable)
                .map(this::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public EmployeeResponseDTO getEmployeeById(Long id) {
        Employee employee = employeeRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found"));
        return toResponseDTO(employee);
    }

    @Transactional
    public EmployeeResponseDTO createEmployee(EmployeeRequestDTO dto) {
        if (employeeRepository.existsByBadgeIdAndDeletedFalse(dto.getBadgeId())) {
            throw new IllegalArgumentException("Badge ID already exists");
        }
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
        return toResponseDTO(employee);
    }

    @Transactional
    public EmployeeResponseDTO updateEmployee(Long id, EmployeeRequestDTO dto) {
        Employee employee = employeeRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found"));
        employee.setName(dto.getName());
        employee.setRole(dto.getRole());
        employee.setDepartment(dto.getDepartment());
        employee.setShiftGroup(dto.getShiftGroup());
        employee.setHireDate(dto.getHireDate());
        employee.setStatus(dto.getStatus());
        employee = employeeRepository.save(employee);
        return toResponseDTO(employee);
    }

    @Transactional
    public void deleteEmployee(Long id) {
        Employee employee = employeeRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found"));
        employee.setDeleted(true);
        employeeRepository.save(employee);
    }

    private EmployeeResponseDTO toResponseDTO(Employee employee) {
        return EmployeeResponseDTO.builder()
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
