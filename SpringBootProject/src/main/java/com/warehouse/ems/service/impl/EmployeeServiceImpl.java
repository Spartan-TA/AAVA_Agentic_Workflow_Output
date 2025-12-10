package com.warehouse.ems.service.impl;

import com.warehouse.ems.entity.Employee;
import com.warehouse.ems.repository.EmployeeRepository;
import com.warehouse.ems.dto.EmployeeDto;
import com.warehouse.ems.dto.EmployeeCreateDto;
import com.warehouse.ems.dto.EmployeeUpdateDto;
import com.warehouse.ems.exception.ResourceNotFoundException;
import com.warehouse.ems.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmployeeServiceImpl implements EmployeeService {
    @Autowired
    private EmployeeRepository employeeRepository;

    // Helper method to map Employee to EmployeeDto
    private EmployeeDto mapToDto(Employee employee) {
        EmployeeDto dto = new EmployeeDto();
        dto.setId(employee.getId());
        dto.setName(employee.getName());
        dto.setEmail(employee.getEmail());
        dto.setPosition(employee.getPosition());
        dto.setDepartment(employee.getDepartment());
        // Add other fields as needed
        return dto;
    }

    @Override
    public EmployeeDto getEmployeeById(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));
        return mapToDto(employee);
    }

    @Override
    public List<EmployeeDto> getAllEmployees() {
        return employeeRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public EmployeeDto createEmployee(EmployeeCreateDto employeeCreateDto) {
        Employee employee = new Employee();
        employee.setName(employeeCreateDto.getName());
        employee.setEmail(employeeCreateDto.getEmail());
        employee.setPosition(employeeCreateDto.getPosition());
        employee.setDepartment(employeeCreateDto.getDepartment());
        // Set other fields as needed
        Employee saved = employeeRepository.save(employee);
        return mapToDto(saved);
    }

    @Override
    public EmployeeDto updateEmployee(Long id, EmployeeUpdateDto employeeUpdateDto) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));
        employee.setName(employeeUpdateDto.getName());
        employee.setEmail(employeeUpdateDto.getEmail());
        employee.setPosition(employeeUpdateDto.getPosition());
        employee.setDepartment(employeeUpdateDto.getDepartment());
        // Update other fields as needed
        Employee updated = employeeRepository.save(employee);
        return mapToDto(updated);
    }

    @Override
    public void deleteEmployee(Long id) {
        if (!employeeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Employee not found with id: " + id);
        }
        employeeRepository.deleteById(id);
    }
}
