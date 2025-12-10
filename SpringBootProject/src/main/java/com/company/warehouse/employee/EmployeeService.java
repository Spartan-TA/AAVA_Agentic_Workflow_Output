package com.company.warehouse.employee;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service layer for Employee business logic.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final EmployeeMapper employeeMapper;

    public EmployeeDTO create(EmployeeDTO dto) {
        Employee entity = employeeMapper.toEntity(dto);
        entity.setId(null);
        entity.setDeleted(false);
        return employeeMapper.toDto(employeeRepository.save(entity));
    }

    public EmployeeDTO update(Long id, EmployeeDTO dto) {
        Employee entity = employeeRepository.findById(id)
                .filter(e -> !e.getDeleted())
                .orElseThrow(() -> new EntityNotFoundException("Employee not found"));
        entity.setName(dto.getName());
        entity.setBadgeId(dto.getBadgeId());
        entity.setRole(dto.getRole());
        entity.setDepartment(dto.getDepartment());
        entity.setShiftGroup(dto.getShiftGroup());
        entity.setHireDate(dto.getHireDate());
        entity.setStatus(dto.getStatus());
        return employeeMapper.toDto(employeeRepository.save(entity));
    }

    public EmployeeDTO patch(Long id, EmployeeDTO dto) {
        Employee entity = employeeRepository.findById(id)
                .filter(e -> !e.getDeleted())
                .orElseThrow(() -> new EntityNotFoundException("Employee not found"));
        if (dto.getName() != null) entity.setName(dto.getName());
        if (dto.getBadgeId() != null) entity.setBadgeId(dto.getBadgeId());
        if (dto.getRole() != null) entity.setRole(dto.getRole());
        if (dto.getDepartment() != null) entity.setDepartment(dto.getDepartment());
        if (dto.getShiftGroup() != null) entity.setShiftGroup(dto.getShiftGroup());
        if (dto.getHireDate() != null) entity.setHireDate(dto.getHireDate());
        if (dto.getStatus() != null) entity.setStatus(dto.getStatus());
        return employeeMapper.toDto(employeeRepository.save(entity));
    }

    public void delete(Long id) {
        Employee entity = employeeRepository.findById(id)
                .filter(e -> !e.getDeleted())
                .orElseThrow(() -> new EntityNotFoundException("Employee not found"));
        entity.setDeleted(true);
        employeeRepository.save(entity);
    }

    @Transactional(readOnly = true)
    public EmployeeDTO get(Long id) {
        Employee entity = employeeRepository.findById(id)
                .filter(e -> !e.getDeleted())
                .orElseThrow(() -> new EntityNotFoundException("Employee not found"));
        return employeeMapper.toDto(entity);
    }

    @Transactional(readOnly = true)
    public Page<EmployeeDTO> list(Pageable pageable) {
        return employeeRepository.findAllByDeletedFalse(pageable)
                .map(employeeMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<EmployeeDTO> filter(String name, String department, String role, Pageable pageable) {
        return employeeRepository.filter(name, department, role, pageable)
                .map(employeeMapper::toDto);
    }
}
