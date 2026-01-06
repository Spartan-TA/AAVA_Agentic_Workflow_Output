package com.companyname.warehouse.employee.service;

import com.companyname.warehouse.employee.dto.EmployeeRequestDTO;
import com.companyname.warehouse.employee.dto.EmployeeResponseDTO;
import com.companyname.warehouse.employee.entity.Employee;
import com.companyname.warehouse.employee.mapper.EmployeeMapper;
import com.companyname.warehouse.employee.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.persistence.EntityNotFoundException;
import java.util.Optional;

/**
 * Service for Employee business logic.
 */
@Service
@RequiredArgsConstructor
public class EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final EmployeeMapper employeeMapper;

    @Transactional
    public EmployeeResponseDTO createEmployee(EmployeeRequestDTO dto) {
        Employee employee = employeeMapper.toEntity(dto);
        employee = employeeRepository.save(employee);
        return employeeMapper.toDto(employee);
    }

    @Transactional
    public EmployeeResponseDTO updateEmployee(Long id, EmployeeRequestDTO dto) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found"));
        employeeMapper.updateEntityFromDto(dto, employee);
        employee = employeeRepository.save(employee);
        return employeeMapper.toDto(employee);
    }

    @Transactional(readOnly = true)
    @Cacheable("employees")
    public EmployeeResponseDTO getEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found"));
        return employeeMapper.toDto(employee);
    }

    @Transactional(readOnly = true)
    public Page<EmployeeResponseDTO> getAllEmployees(Pageable pageable) {
        return employeeRepository.findAll(pageable)
                .map(employeeMapper::toDto);
    }

    @Transactional
    public void deleteEmployee(Long id) {
        try {
            employeeRepository.deleteById(id);
        } catch (EmptyResultDataAccessException e) {
            throw new EntityNotFoundException("Employee not found");
        }
    }
}
