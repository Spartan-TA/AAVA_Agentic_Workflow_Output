package com.companyname.wem.employee.service;

import com.companyname.wem.employee.domain.Employee;
import com.companyname.wem.employee.dto.EmployeeDTO;
import com.companyname.wem.employee.mapper.EmployeeMapper;
import com.companyname.wem.employee.repository.EmployeeRepository;
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
    private final EmployeeMapper employeeMapper;

    @Transactional(readOnly = true)
    public Page<EmployeeDTO> getAll(Pageable pageable) {
        return employeeRepository.findAll(pageable)
                .map(employeeMapper::toDto);
    }

    @Transactional(readOnly = true)
    public EmployeeDTO getById(Long id) {
        Employee employee = employeeRepository.findById(id)
                .filter(e -> !e.isDeleted())
                .orElseThrow(() -> new EntityNotFoundException("Employee not found"));
        return employeeMapper.toDto(employee);
    }

    @Transactional
    public EmployeeDTO create(EmployeeDTO dto) {
        if (employeeRepository.existsByBadgeIdAndDeletedFalse(dto.getBadgeId())) {
            throw new IllegalArgumentException("Badge ID already exists");
        }
        Employee employee = employeeMapper.toEntity(dto);
        employee.setDeleted(false);
        return employeeMapper.toDto(employeeRepository.save(employee));
    }

    @Transactional
    public EmployeeDTO update(Long id, EmployeeDTO dto) {
        Employee employee = employeeRepository.findById(id)
                .filter(e -> !e.isDeleted())
                .orElseThrow(() -> new EntityNotFoundException("Employee not found"));
        employee.setName(dto.getName());
        employee.setRole(dto.getRole());
        employee.setDepartment(dto.getDepartment());
        employee.setShiftGroup(dto.getShiftGroup());
        employee.setHireDate(dto.getHireDate());
        employee.setStatus(dto.getStatus());
        return employeeMapper.toDto(employeeRepository.save(employee));
    }

    @Transactional
    public void softDelete(Long id) {
        Employee employee = employeeRepository.findById(id)
                .filter(e -> !e.isDeleted())
                .orElseThrow(() -> new EntityNotFoundException("Employee not found"));
        employee.setDeleted(true);
        employeeRepository.save(employee);
    }
}
