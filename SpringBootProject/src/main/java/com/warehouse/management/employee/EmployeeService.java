package com.warehouse.management.employee;

import com.warehouse.management.exception.ResourceNotFoundException;
import com.warehouse.management.employee.dto.EmployeeRequest;
import com.warehouse.management.employee.dto.EmployeeResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service layer for Employee operations.
 */
@Service
public class EmployeeService {
    @Autowired
    private EmployeeRepository employeeRepository;

    @Transactional(readOnly = true)
    public Page<EmployeeResponse> getAllEmployees(Pageable pageable) {
        return employeeRepository.findAllByDeletedFalse(pageable)
                .map(EmployeeResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public EmployeeResponse getEmployeeById(Long id) {
        Employee employee = employeeRepository.findById(id)
                .filter(e -> !Boolean.TRUE.equals(e.getDeleted()))
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
        return EmployeeResponse.fromEntity(employee);
    }

    @Transactional
    public EmployeeResponse createEmployee(EmployeeRequest request) {
        Employee employee = Employee.builder()
                .employeeCode(request.getEmployeeCode())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .dob(request.getDob())
                .hireDate(request.getHireDate())
                .status(request.getStatus())
                .department(request.getDepartment())
                .position(request.getPosition())
                .deleted(false)
                .build();
        employee = employeeRepository.save(employee);
        return EmployeeResponse.fromEntity(employee);
    }

    @Transactional
    public EmployeeResponse updateEmployee(Long id, EmployeeRequest request) {
        Employee employee = employeeRepository.findById(id)
                .filter(e -> !Boolean.TRUE.equals(e.getDeleted()))
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
        employee.setFirstName(request.getFirstName());
        employee.setLastName(request.getLastName());
        employee.setEmail(request.getEmail());
        employee.setPhone(request.getPhone());
        employee.setDob(request.getDob());
        employee.setHireDate(request.getHireDate());
        employee.setStatus(request.getStatus());
        employee.setDepartment(request.getDepartment());
        employee.setPosition(request.getPosition());
        employeeRepository.save(employee);
        return EmployeeResponse.fromEntity(employee);
    }

    @Transactional
    public void softDeleteEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .filter(e -> !Boolean.TRUE.equals(e.getDeleted()))
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
        employee.setDeleted(true);
        employeeRepository.save(employee);
    }
}
