package com.companyname.wem.employee.service;

import com.companyname.wem.employee.domain.Employee;
import com.companyname.wem.employee.dto.EmployeeDTO;
import com.companyname.wem.employee.mapper.EmployeeMapper;
import com.companyname.wem.employee.repository.EmployeeRepository;
lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EmployeeService {
    private final EmployeeRepository repository;
    private final EmployeeMapper mapper;

    @Transactional
    public EmployeeDTO create(EmployeeDTO dto) {
        if (repository.existsByBadgeId(dto.getBadgeId())) {
            throw new RuntimeException("Badge ID already exists");
        }
        Employee employee = mapper.toEntity(dto);
        Employee saved = repository.save(employee);
        return mapper.toDto(saved);
    }

    public EmployeeDTO getById(Long id) {
        Employee employee = repository.findById(id)
            .orElseThrow(() -> new RuntimeException("Employee not found"));
        return mapper.toDto(employee);
    }

    public Page<EmployeeDTO> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toDto);
    }

    @Transactional
    public EmployeeDTO update(Long id, EmployeeDTO dto) {
        Employee employee = repository.findById(id)
            .orElseThrow(() -> new RuntimeException("Employee not found"));
        mapper.updateEntityFromDto(dto, employee);
        Employee updated = repository.save(employee);
        return mapper.toDto(updated);
    }

    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Employee not found");
        }
        repository.deleteById(id);
    }
}
