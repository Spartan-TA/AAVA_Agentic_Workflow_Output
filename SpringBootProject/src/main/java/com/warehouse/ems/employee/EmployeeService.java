package com.warehouse.ems.employee;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for employee management.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final EmployeeMapper employeeMapper;

    @PreAuthorize("hasAnyRole('ADMIN','HR','SUPERVISOR')")
    public Page<EmployeeResponseDTO> getAllEmployees(Pageable pageable) {
        return employeeRepository.findAllActive(pageable)
                .map(employeeMapper::toResponseDTO);
    }

    @PreAuthorize("hasAnyRole('ADMIN','HR','SUPERVISOR','EMPLOYEE')")
    public EmployeeResponseDTO getEmployeeById(Long id) {
        Employee employee = employeeRepository.findActiveById(id)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found"));
        return employeeMapper.toResponseDTO(employee);
    }

    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public EmployeeResponseDTO createEmployee(EmployeeRequestDTO dto) {
        Employee employee = employeeMapper.toEntity(dto);
        employee.setDeleted(false);
        return employeeMapper.toResponseDTO(employeeRepository.save(employee));
    }

    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public EmployeeResponseDTO updateEmployee(Long id, EmployeeRequestDTO dto) {
        Employee employee = employeeRepository.findActiveById(id)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found"));
        employeeMapper.updateEmployeeFromDto(dto, employee);
        return employeeMapper.toResponseDTO(employeeRepository.save(employee));
    }

    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public EmployeeResponseDTO patchEmployee(Long id, EmployeeRequestDTO dto) {
        return updateEmployee(id, dto);
    }

    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public void deleteEmployee(Long id) {
        Employee employee = employeeRepository.findActiveById(id)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found"));
        employee.setDeleted(true);
        employeeRepository.save(employee);
    }
}
