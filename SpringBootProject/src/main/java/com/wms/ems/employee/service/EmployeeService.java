package com.wms.ems.employee.service;

import com.wms.ems.employee.domain.Employee;
import com.wms.ems.employee.dto.EmployeeRequestDTO;
import com.wms.ems.employee.dto.EmployeeResponseDTO;
import com.wms.ems.employee.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EmployeeService {
    private final EmployeeRepository employeeRepository;

    public Page<EmployeeResponseDTO> getAllEmployees(int page, int size, String department) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Employee> employees = (department == null || department.isBlank())
                ? employeeRepository.findAllByDeletedFalse(pageable)
                : employeeRepository.findAllByDepartmentAndDeletedFalse(department, pageable);
        return employees.map(this::toResponseDTO);
    }

    public EmployeeResponseDTO getEmployeeById(Long id) {
        Employee employee = employeeRepository.findById(id)
                .filter(e -> !e.getDeleted())
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        return toResponseDTO(employee);
    }

    @Transactional
    public EmployeeResponseDTO createEmployee(EmployeeRequestDTO dto) {
        Employee employee = Employee.builder()
                .name(dto.getName())
                .badgeId(dto.getBadgeId())
                .role(dto.getRole())
                .department(dto.getDepartment())
                .shiftGroup(dto.getShiftGroup())
                .hireDate(dto.getHireDate())
                .status(dto.getStatus())
                .deleted(false)
                .build();
        employee = employeeRepository.save(employee);
        return toResponseDTO(employee);
    }

    @Transactional
    public EmployeeResponseDTO updateEmployee(Long id, EmployeeRequestDTO dto) {
        Employee employee = employeeRepository.findById(id)
                .filter(e -> !e.getDeleted())
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        BeanUtils.copyProperties(dto, employee, "id", "badgeId", "createdAt", "updatedAt", "deleted");
        employee = employeeRepository.save(employee);
        return toResponseDTO(employee);
    }

    @Transactional
    public EmployeeResponseDTO patchEmployee(Long id, EmployeeRequestDTO dto) {
        Employee employee = employeeRepository.findById(id)
                .filter(e -> !e.getDeleted())
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        if (dto.getName() != null) employee.setName(dto.getName());
        if (dto.getRole() != null) employee.setRole(dto.getRole());
        if (dto.getDepartment() != null) employee.setDepartment(dto.getDepartment());
        if (dto.getShiftGroup() != null) employee.setShiftGroup(dto.getShiftGroup());
        if (dto.getHireDate() != null) employee.setHireDate(dto.getHireDate());
        if (dto.getStatus() != null) employee.setStatus(dto.getStatus());
        employee = employeeRepository.save(employee);
        return toResponseDTO(employee);
    }

    @Transactional
    public void deleteEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .filter(e -> !e.getDeleted())
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        employee.setDeleted(true);
        employeeRepository.save(employee);
    }

    private EmployeeResponseDTO toResponseDTO(Employee employee) {
        EmployeeResponseDTO dto = new EmployeeResponseDTO();
        BeanUtils.copyProperties(employee, dto);
        return dto;
    }
}
