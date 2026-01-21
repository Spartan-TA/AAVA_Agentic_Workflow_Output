package com.companyname.warehouse.employee.service;

import com.companyname.warehouse.employee.dto.EmployeeRequestDTO;
import com.companyname.warehouse.employee.dto.EmployeeResponseDTO;
import com.companyname.warehouse.employee.model.Employee;
import com.companyname.warehouse.employee.repository.EmployeeRepository;
import com.companyname.warehouse.util.MapperUtil;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final MapperUtil mapperUtil;

    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public EmployeeResponseDTO createEmployee(EmployeeRequestDTO dto) {
        Employee employee = mapperUtil.toEmployee(dto);
        employee = employeeRepository.save(employee);
        return mapperUtil.toEmployeeResponseDTO(employee);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ADMIN','HR','SUPERVISOR')")
    public Page<EmployeeResponseDTO> listEmployees(Pageable pageable, String filter) {
        Page<Employee> page = employeeRepository.findAllByDeletedFalse(pageable);
        return page.map(mapperUtil::toEmployeeResponseDTO);
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public EmployeeResponseDTO updateEmployee(Long id, EmployeeRequestDTO dto) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found"));
        mapperUtil.updateEmployeeFromDTO(dto, employee);
        employee = employeeRepository.save(employee);
        return mapperUtil.toEmployeeResponseDTO(employee);
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public void softDeleteEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found"));
        employee.setDeleted(true);
        employeeRepository.save(employee);
    }
}
