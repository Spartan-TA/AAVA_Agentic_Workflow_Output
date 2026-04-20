package com.warehouse.management.employee.service;

import com.warehouse.management.employee.domain.Employee;
import com.warehouse.management.employee.dto.EmployeeRequestDTO;
import com.warehouse.management.employee.dto.EmployeeResponseDTO;
import com.warehouse.management.employee.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {
    private final EmployeeRepository employeeRepository;

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public EmployeeResponseDTO createEmployee(EmployeeRequestDTO dto) {
        if (employeeRepository.findByBadgeIdAndDeletedFalse(dto.getBadgeId()).isPresent()) {
            throw new IllegalArgumentException("Badge ID must be unique");
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
        employee = employeeRepository.save(employee);
        return toResponseDTO(employee);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public EmployeeResponseDTO updateEmployee(Long id, EmployeeRequestDTO dto) {
        Employee employee = employeeRepository.findById(id)
                .filter(e -> !e.isDeleted())
                .orElseThrow(() -> new NoSuchElementException("Employee not found"));
        if (!employee.getBadgeId().equals(dto.getBadgeId()) && employeeRepository.findByBadgeIdAndDeletedFalse(dto.getBadgeId()).isPresent()) {
            throw new IllegalArgumentException("Badge ID must be unique");
        }
        BeanUtils.copyProperties(dto, employee, "id", "deleted");
        employee = employeeRepository.save(employee);
        return toResponseDTO(employee);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ADMIN','HR','SUPERVISOR','WORKER')")
    public EmployeeResponseDTO getEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .filter(e -> !e.isDeleted())
                .orElseThrow(() -> new NoSuchElementException("Employee not found"));
        return toResponseDTO(employee);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ADMIN','HR','SUPERVISOR','WORKER')")
    public Page<EmployeeResponseDTO> listEmployees(String search, Pageable pageable) {
        Page<Employee> page = (search == null || search.isBlank()) ?
                employeeRepository.findAllByDeletedFalse(pageable) :
                employeeRepository.search(search, pageable);
        return page.map(this::toResponseDTO);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public void deleteEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .filter(e -> !e.isDeleted())
                .orElseThrow(() -> new NoSuchElementException("Employee not found"));
        employee.setDeleted(true);
        employeeRepository.save(employee);
    }

    private EmployeeResponseDTO toResponseDTO(Employee employee) {
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
}
