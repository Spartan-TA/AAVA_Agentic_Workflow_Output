package com.warehouseems.employee;

import com.warehouseems.employee.dto.EmployeeRequestDto;
import com.warehouseems.employee.dto.EmployeeResponseDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class EmployeeService {
    private final EmployeeRepository employeeRepository;

    @Autowired
    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    public Page<EmployeeResponseDto> getAllEmployees(Pageable pageable) {
        return employeeRepository.findAllByDeletedFalse(pageable)
                .map(this::toResponseDto);
    }

    public Optional<EmployeeResponseDto> getEmployeeById(Long id) {
        return employeeRepository.findByIdAndDeletedFalse(id)
                .map(this::toResponseDto);
    }

    public Optional<EmployeeResponseDto> getEmployeeByBadgeId(String badgeId) {
        return employeeRepository.findByBadgeIdAndDeletedFalse(badgeId)
                .map(this::toResponseDto);
    }

    @Transactional
    public EmployeeResponseDto createEmployee(EmployeeRequestDto dto) {
        Employee employee = new Employee();
        employee.setName(dto.getName());
        employee.setBadgeId(dto.getBadgeId());
        employee.setRole(dto.getRole());
        employee.setDepartment(dto.getDepartment());
        employee.setShiftGroup(dto.getShiftGroup());
        employee.setHireDate(dto.getHireDate());
        employee.setStatus(dto.getStatus());
        employee.setDeleted(false);
        Employee saved = employeeRepository.save(employee);
        return toResponseDto(saved);
    }

    @Transactional
    public Optional<EmployeeResponseDto> updateEmployee(Long id, EmployeeRequestDto dto) {
        return employeeRepository.findByIdAndDeletedFalse(id).map(employee -> {
            employee.setName(dto.getName());
            employee.setRole(dto.getRole());
            employee.setDepartment(dto.getDepartment());
            employee.setShiftGroup(dto.getShiftGroup());
            employee.setHireDate(dto.getHireDate());
            employee.setStatus(dto.getStatus());
            Employee updated = employeeRepository.save(employee);
            return toResponseDto(updated);
        });
    }

    @Transactional
    public boolean softDeleteEmployee(Long id) {
        return employeeRepository.findByIdAndDeletedFalse(id).map(employee -> {
            employee.setDeleted(true);
            employeeRepository.save(employee);
            return true;
        }).orElse(false);
    }

    private EmployeeResponseDto toResponseDto(Employee employee) {
        EmployeeResponseDto dto = new EmployeeResponseDto();
        dto.setId(employee.getId());
        dto.setName(employee.getName());
        dto.setBadgeId(employee.getBadgeId());
        dto.setRole(employee.getRole());
        dto.setDepartment(employee.getDepartment());
        dto.setShiftGroup(employee.getShiftGroup());
        dto.setHireDate(employee.getHireDate());
        dto.setStatus(employee.getStatus());
        return dto;
    }
}
