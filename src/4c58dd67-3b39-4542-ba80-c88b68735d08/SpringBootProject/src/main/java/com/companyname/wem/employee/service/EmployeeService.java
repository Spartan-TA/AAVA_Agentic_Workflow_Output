package com.companyname.wem.employee.service;

import com.companyname.wem.employee.domain.Employee;
import com.companyname.wem.employee.domain.Status;
import com.companyname.wem.employee.dto.EmployeeDTO;
import com.companyname.wem.employee.mapper.EmployeeMapper;
import com.companyname.wem.employee.repository.EmployeeRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final EmployeeMapper employeeMapper;

    public List<EmployeeDTO> getAllEmployees() {
        return employeeRepository.findByDeletedFalse()
                .stream()
                .map(employeeMapper::toDto)
                .toList();
    }

    public Optional<EmployeeDTO> getEmployeeById(Long id) {
        return employeeRepository.findById(id)
                .filter(e -> !e.isDeleted())
                .map(employeeMapper::toDto);
    }

    @Transactional
    public EmployeeDTO createEmployee(@Valid EmployeeDTO employeeDTO) {
        Employee employee = employeeMapper.toEntity(employeeDTO);
        employee.setDeleted(false);
        Employee saved = employeeRepository.save(employee);
        return employeeMapper.toDto(saved);
    }

    @Transactional
    public Optional<EmployeeDTO> updateEmployee(Long id, @Valid EmployeeDTO employeeDTO) {
        return employeeRepository.findById(id)
                .filter(e -> !e.isDeleted())
                .map(existing -> {
                    Employee updated = employeeMapper.toEntity(employeeDTO);
                    updated.setId(existing.getId());
                    updated.setDeleted(existing.isDeleted());
                    Employee saved = employeeRepository.save(updated);
                    return employeeMapper.toDto(saved);
                });
    }

    @Transactional
    public boolean deleteEmployee(Long id) {
        return employeeRepository.findById(id)
                .filter(e -> !e.isDeleted())
                .map(e -> {
                    employeeRepository.delete(e);
                    return true;
                }).orElse(false);
    }

    public List<EmployeeDTO> getEmployeesByStatus(Status status) {
        return employeeRepository.findByStatus(status)
                .stream()
                .filter(e -> !e.isDeleted())
                .map(employeeMapper::toDto)
                .toList();
    }
}
