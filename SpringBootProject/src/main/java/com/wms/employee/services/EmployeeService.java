package com.wms.employee.services;

import com.wms.common.enums.Status;
import com.wms.common.enums.Role;
import com.wms.common.exceptions.ResourceNotFoundException;
import com.wms.employee.dtos.EmployeeDto;
import com.wms.employee.model.Department;
import com.wms.employee.model.Employee;
import com.wms.employee.repositories.DepartmentRepository;
import com.wms.employee.repositories.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service class for Employee CRUD operations.
 */
@Service
@RequiredArgsConstructor
public class EmployeeService {

    @Autowired
    private final EmployeeRepository employeeRepository;
    @Autowired
    private final DepartmentRepository departmentRepository;

    public List<EmployeeDto> getAllEmployees() {
        return employeeRepository.findByDeletedFalse().stream().map(this::toDto).collect(Collectors.toList());
    }

    public EmployeeDto getEmployeeById(Long id) {
        Employee employee = employeeRepository.findById(id)
                .filter(e -> !e.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));
        return toDto(employee);
    }

    @Transactional
    public EmployeeDto createEmployee(EmployeeDto dto) {
        Department department = departmentRepository.findById(dto.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + dto.getDepartmentId()));
        Employee employee = new Employee();
        employee.setName(dto.getName());
        employee.setBadgeId(dto.getBadgeId());
        employee.setRole(dto.getRole());
        employee.setDepartment(department);
        employee.setShiftGroup(dto.getShiftGroup());
        employee.setHireDate(dto.getHireDate());
        employee.setStatus(dto.getStatus() != null ? dto.getStatus() : Status.ACTIVE);
        employee.setDeleted(false);
        Employee saved = employeeRepository.save(employee);
        return toDto(saved);
    }

    @Transactional
    public EmployeeDto updateEmployee(Long id, EmployeeDto dto) {
        Employee employee = employeeRepository.findById(id)
                .filter(e -> !e.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));
        if (dto.getName() != null) employee.setName(dto.getName());
        if (dto.getBadgeId() != null) employee.setBadgeId(dto.getBadgeId());
        if (dto.getRole() != null) employee.setRole(dto.getRole());
        if (dto.getDepartmentId() != null) {
            Department department = departmentRepository.findById(dto.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + dto.getDepartmentId()));
            employee.setDepartment(department);
        }
        if (dto.getShiftGroup() != null) employee.setShiftGroup(dto.getShiftGroup());
        if (dto.getHireDate() != null) employee.setHireDate(dto.getHireDate());
        if (dto.getStatus() != null) employee.setStatus(dto.getStatus());
        Employee updated = employeeRepository.save(employee);
        return toDto(updated);
    }

    @Transactional
    public void deleteEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .filter(e -> !e.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));
        employee.setDeleted(true);
        employeeRepository.save(employee);
    }

    public List<EmployeeDto> getEmployeesByDepartment(Long departmentId) {
        return employeeRepository.findByDepartmentIdAndDeletedFalse(departmentId).stream().map(this::toDto).collect(Collectors.toList());
    }

    public List<EmployeeDto> getEmployeesByRole(Role role) {
        return employeeRepository.findByRoleAndDeletedFalse(role).stream().map(this::toDto).collect(Collectors.toList());
    }

    public List<EmployeeDto> getEmployeesByStatus(Status status) {
        return employeeRepository.findByStatusAndDeletedFalse(status).stream().map(this::toDto).collect(Collectors.toList());
    }

    private EmployeeDto toDto(Employee employee) {
        EmployeeDto dto = new EmployeeDto();
        dto.setId(employee.getId());
        dto.setName(employee.getName());
        dto.setBadgeId(employee.getBadgeId());
        dto.setRole(employee.getRole());
        dto.setDepartmentId(employee.getDepartment().getId());
        dto.setDepartmentName(employee.getDepartment().getName());
        dto.setShiftGroup(employee.getShiftGroup());
        dto.setHireDate(employee.getHireDate());
        dto.setStatus(employee.getStatus());
        dto.setDeleted(employee.isDeleted());
        return dto;
    }
}
