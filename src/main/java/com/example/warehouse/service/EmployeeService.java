package com.example.warehouse.service;

import com.example.warehouse.dto.EmployeeCreateDTO;
import com.example.warehouse.dto.EmployeeDTO;
import com.example.warehouse.dto.EmployeeUpdateDTO;
import com.example.warehouse.entity.Department;
import com.example.warehouse.entity.Employee;
import com.example.warehouse.entity.Role;
import com.example.warehouse.exception.ResourceNotFoundException;
import com.example.warehouse.exception.ValidationException;
import com.example.warehouse.repository.DepartmentRepository;
import com.example.warehouse.repository.EmployeeRepository;
import com.example.warehouse.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for managing employees.
 */
@Service
public class EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final RoleRepository roleRepository;
    private final DepartmentRepository departmentRepository;

    @Autowired
    public EmployeeService(EmployeeRepository employeeRepository, RoleRepository roleRepository, DepartmentRepository departmentRepository) {
        this.employeeRepository = employeeRepository;
        this.roleRepository = roleRepository;
        this.departmentRepository = departmentRepository;
    }

    /**
     * Get all employees.
     * @return List of EmployeeDTO
     */
    @Transactional(readOnly = true)
    public List<EmployeeDTO> getAllEmployees() {
        return employeeRepository.findAll().stream()
                .map(EmployeeDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get employee by ID.
     * @param id Employee ID
     * @return EmployeeDTO
     */
    @Transactional(readOnly = true)
    public EmployeeDTO getEmployeeById(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));
        return EmployeeDTO.fromEntity(employee);
    }

    /**
     * Create a new employee.
     * @param createDTO EmployeeCreateDTO
     * @return EmployeeDTO
     */
    @Transactional
    public EmployeeDTO createEmployee(EmployeeCreateDTO createDTO) {
        validateCreateDTO(createDTO);
        Role role = roleRepository.findById(createDTO.getRoleId())
                .orElseThrow(() -> new ValidationException("Invalid role ID"));
        Department department = departmentRepository.findById(createDTO.getDepartmentId())
                .orElseThrow(() -> new ValidationException("Invalid department ID"));
        Employee employee = new Employee();
        employee.setFirstName(createDTO.getFirstName());
        employee.setLastName(createDTO.getLastName());
        employee.setEmail(createDTO.getEmail());
        employee.setRole(role);
        employee.setDepartment(department);
        employee.setActive(true);
        employeeRepository.save(employee);
        return EmployeeDTO.fromEntity(employee);
    }

    /**
     * Update an existing employee.
     * @param id Employee ID
     * @param updateDTO EmployeeUpdateDTO
     * @return EmployeeDTO
     */
    @Transactional
    public EmployeeDTO updateEmployee(Long id, EmployeeUpdateDTO updateDTO) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));
        if (updateDTO.getFirstName() != null) {
            employee.setFirstName(updateDTO.getFirstName());
        }
        if (updateDTO.getLastName() != null) {
            employee.setLastName(updateDTO.getLastName());
        }
        if (updateDTO.getEmail() != null) {
            employee.setEmail(updateDTO.getEmail());
        }
        if (updateDTO.getRoleId() != null) {
            Role role = roleRepository.findById(updateDTO.getRoleId())
                    .orElseThrow(() -> new ValidationException("Invalid role ID"));
            employee.setRole(role);
        }
        if (updateDTO.getDepartmentId() != null) {
            Department department = departmentRepository.findById(updateDTO.getDepartmentId())
                    .orElseThrow(() -> new ValidationException("Invalid department ID"));
            employee.setDepartment(department);
        }
        employeeRepository.save(employee);
        return EmployeeDTO.fromEntity(employee);
    }

    /**
     * Deactivate an employee.
     * @param id Employee ID
     */
    @Transactional
    public void deactivateEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));
        employee.setActive(false);
        employeeRepository.save(employee);
    }

    private void validateCreateDTO(EmployeeCreateDTO dto) {
        if (dto.getFirstName() == null || dto.getFirstName().isEmpty()) {
            throw new ValidationException("First name is required");
        }
        if (dto.getLastName() == null || dto.getLastName().isEmpty()) {
            throw new ValidationException("Last name is required");
        }
        if (dto.getEmail() == null || dto.getEmail().isEmpty()) {
            throw new ValidationException("Email is required");
        }
        if (dto.getRoleId() == null) {
            throw new ValidationException("Role ID is required");
        }
        if (dto.getDepartmentId() == null) {
            throw new ValidationException("Department ID is required");
        }
    }
}
