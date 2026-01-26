package com.example.warehouse.employee.service;

import com.example.warehouse.employee.dto.EmployeeDto;
import com.example.warehouse.employee.entity.Employee;
import com.example.warehouse.employee.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EmployeeService {
    @Autowired
    private EmployeeRepository employeeRepository;

    // Convert Employee entity to DTO
    private EmployeeDto toDto(Employee employee) {
        return new EmployeeDto(
                employee.getId(),
                employee.getFirstName(),
                employee.getLastName(),
                employee.getEmail(),
                employee.getPosition(),
                employee.getHireDate(),
                employee.isActive()
        );
    }

    // Convert DTO to Employee entity
    private Employee toEntity(EmployeeDto dto) {
        Employee employee = new Employee();
        employee.setId(dto.getId());
        employee.setFirstName(dto.getFirstName());
        employee.setLastName(dto.getLastName());
        employee.setEmail(dto.getEmail());
        employee.setPosition(dto.getPosition());
        employee.setHireDate(dto.getHireDate());
        employee.setActive(dto.isActive());
        return employee;
    }

    // Get all employees
    public List<EmployeeDto> getAllEmployees() {
        return employeeRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    // Get employee by ID
    public Optional<EmployeeDto> getEmployeeById(Long id) {
        return employeeRepository.findById(id).map(this::toDto);
    }

    // Create new employee
    @Transactional
    public EmployeeDto createEmployee(EmployeeDto dto) {
        Employee employee = toEntity(dto);
        employee.setId(null); // Ensure ID is null for new entity
        Employee saved = employeeRepository.save(employee);
        return toDto(saved);
    }

    // Update existing employee
    @Transactional
    public Optional<EmployeeDto> updateEmployee(Long id, EmployeeDto dto) {
        return employeeRepository.findById(id).map(existing -> {
            existing.setFirstName(dto.getFirstName());
            existing.setLastName(dto.getLastName());
            existing.setEmail(dto.getEmail());
            existing.setPosition(dto.getPosition());
            existing.setHireDate(dto.getHireDate());
            existing.setActive(dto.isActive());
            Employee saved = employeeRepository.save(existing);
            return toDto(saved);
        });
    }

    // Delete employee
    @Transactional
    public boolean deleteEmployee(Long id) {
        if (employeeRepository.existsById(id)) {
            employeeRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
