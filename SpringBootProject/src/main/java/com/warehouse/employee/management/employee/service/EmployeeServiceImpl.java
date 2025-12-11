package com.warehouse.employee.management.employee.service;

import com.warehouse.employee.management.employee.dto.EmployeeRequestDTO;
import com.warehouse.employee.management.employee.dto.EmployeeResponseDTO;
import com.warehouse.employee.management.employee.entity.Employee;
import com.warehouse.employee.management.employee.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

/**
 * Implementation of EmployeeService with business logic.
 */
@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {
    private final EmployeeRepository employeeRepository;

    private EmployeeResponseDTO toDto(Employee employee) {
        return EmployeeResponseDTO.builder()
                .id(employee.getId())
                .name(employee.getName())
                .badgeId(employee.getBadgeId())
                .role(employee.getRole())
                .department(employee.getDepartment())
                .shiftGroup(employee.getShiftGroup())
                .hireDate(employee.getHireDate())
                .status(employee.getStatus())
                .build();
    }

    @Override
    @Transactional
    public EmployeeResponseDTO createEmployee(EmployeeRequestDTO dto) {
        if (employeeRepository.existsByBadgeIdAndDeletedFalse(dto.getBadgeId())) {
            throw new IllegalArgumentException("Badge ID already exists");
        }
        Employee employee = Employee.builder()
                .name(dto.getName())
                .badgeId(dto.getBadgeId())
                .role(dto.getRole())
                .department(dto.getDepartment())
                .shiftGroup(dto.getShiftGroup())
                .hireDate(dto.getHireDate())
                .status(dto.getStatus())
                .deleted(false)
                .build();
        return toDto(employeeRepository.save(employee));
    }

    @Override
    @Transactional
    public EmployeeResponseDTO updateEmployee(Long id, EmployeeRequestDTO dto) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));
        BeanUtils.copyProperties(dto, employee, "id", "badgeId"); // badgeId is immutable
        return toDto(employeeRepository.save(employee));
    }

    @Override
    @Transactional
    public EmployeeResponseDTO patchEmployee(Long id, EmployeeRequestDTO dto) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));
        if (dto.getName() != null) employee.setName(dto.getName());
        if (dto.getRole() != null) employee.setRole(dto.getRole());
        if (dto.getDepartment() != null) employee.setDepartment(dto.getDepartment());
        if (dto.getShiftGroup() != null) employee.setShiftGroup(dto.getShiftGroup());
        if (dto.getHireDate() != null) employee.setHireDate(dto.getHireDate());
        if (dto.getStatus() != null) employee.setStatus(dto.getStatus());
        return toDto(employeeRepository.save(employee));
    }

    @Override
    @Transactional
    public void deleteEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));
        employee.setDeleted(true); // Soft delete
        employeeRepository.save(employee);
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeResponseDTO getEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .filter(e -> !e.isDeleted())
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));
        return toDto(employee);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EmployeeResponseDTO> getAllEmployees(String department, Pageable pageable) {
        Page<Employee> page = (department == null || department.isBlank())
                ? employeeRepository.findAllByDeletedFalse(pageable)
                : employeeRepository.findAllByDepartmentAndDeletedFalse(department, pageable);
        return page.map(this::toDto);
    }
}
