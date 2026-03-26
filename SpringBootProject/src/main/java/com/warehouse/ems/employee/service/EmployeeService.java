package com.warehouse.ems.employee.service;

import com.warehouse.ems.employee.dto.EmployeeDTO;
import com.warehouse.ems.employee.entity.Department;
import com.warehouse.ems.employee.entity.Employee;
import com.warehouse.ems.employee.repository.EmployeeRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final EmployeeMapper employeeMapper = Mappers.getMapper(EmployeeMapper.class);

    @Transactional(readOnly = true)
    public Page<EmployeeDTO> getAllEmployees(String name, Pageable pageable) {
        if (name != null && !name.isEmpty()) {
            return employeeRepository.searchByName(name, pageable).map(employeeMapper::toDto);
        }
        return employeeRepository.findAll(pageable).map(employeeMapper::toDto);
    }

    @Transactional(readOnly = true)
    public EmployeeDTO getEmployeeById(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found"));
        return employeeMapper.toDto(employee);
    }

    @Transactional
    public EmployeeDTO createEmployee(EmployeeDTO dto) {
        Employee employee = employeeMapper.toEntity(dto);
        employee.setId(null);
        employee.setDeleted(false);
        return employeeMapper.toDto(employeeRepository.save(employee));
    }

    @Transactional
    public EmployeeDTO updateEmployee(Long id, EmployeeDTO dto) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found"));
        employee.setName(dto.getName());
        employee.setBadgeId(dto.getBadgeId());
        employee.setRole(dto.getRole());
        employee.setShiftGroup(dto.getShiftGroup());
        employee.setHireDate(dto.getHireDate());
        employee.setStatus(dto.getStatus());
        // Department update logic can be added here
        return employeeMapper.toDto(employeeRepository.save(employee));
    }

    @Transactional
    public void softDeleteEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found"));
        employee.setDeleted(true);
        employeeRepository.save(employee);
    }

    @Mapper
    public interface EmployeeMapper {
        @Mapping(source = "department.id", target = "departmentId")
        @Mapping(source = "department.name", target = "departmentName")
        EmployeeDTO toDto(Employee employee);
        Employee toEntity(EmployeeDTO dto);
    }
}
