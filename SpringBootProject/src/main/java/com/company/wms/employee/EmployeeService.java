package com.company.wms.employee;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.prepost.PreAuthorize;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for Employee business logic.
 */
@Service
public class EmployeeService {
    private static final Logger logger = LoggerFactory.getLogger(EmployeeService.class);

    @Autowired
    private EmployeeRepository employeeRepository;

    @Transactional(readOnly = true)
    public List<EmployeeDTO> getAllEmployees() {
        logger.info("Fetching all active employees");
        return employeeRepository.findAllByDeletedFalse().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public EmployeeDTO getEmployeeById(Long id) {
        logger.info("Fetching employee by id: {}", id);
        Employee employee = employeeRepository.findById(id)
                .filter(e -> !e.getDeleted())
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found"));
        return toDTO(employee);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public EmployeeDTO createEmployee(EmployeeDTO dto) {
        logger.info("Creating new employee: {}", dto.getEmail());
        Employee employee = toEntity(dto);
        employee.setDeleted(false);
        Employee saved = employeeRepository.save(employee);
        return toDTO(saved);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public EmployeeDTO updateEmployee(Long id, EmployeeDTO dto) {
        logger.info("Updating employee id: {}", id);
        Employee employee = employeeRepository.findById(id)
                .filter(e -> !e.getDeleted())
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found"));
        employee.setFirstName(dto.getFirstName());
        employee.setLastName(dto.getLastName());
        employee.setEmail(dto.getEmail());
        employee.setPhone(dto.getPhone());
        employee.setRole(dto.getRole());
        employee.setStatus(dto.getStatus());
        // Set department and shiftGroup if needed
        employee.setHireDate(dto.getHireDate());
        employee.setTerminationDate(dto.getTerminationDate());
        Employee updated = employeeRepository.save(employee);
        return toDTO(updated);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteEmployee(Long id) {
        logger.info("Soft deleting employee id: {}", id);
        Employee employee = employeeRepository.findById(id)
                .filter(e -> !e.getDeleted())
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found"));
        employee.setDeleted(true);
        employeeRepository.save(employee);
    }

    private EmployeeDTO toDTO(Employee employee) {
        EmployeeDTO dto = new EmployeeDTO();
        dto.setId(employee.getId());
        dto.setFirstName(employee.getFirstName());
        dto.setLastName(employee.getLastName());
        dto.setEmail(employee.getEmail());
        dto.setPhone(employee.getPhone());
        dto.setRole(employee.getRole());
        dto.setStatus(employee.getStatus());
        if (employee.getDepartment() != null) dto.setDepartmentId(employee.getDepartment().getId());
        if (employee.getShiftGroup() != null) dto.setShiftGroupId(employee.getShiftGroup().getId());
        dto.setHireDate(employee.getHireDate());
        dto.setTerminationDate(employee.getTerminationDate());
        return dto;
    }

    private Employee toEntity(EmployeeDTO dto) {
        Employee employee = new Employee();
        employee.setFirstName(dto.getFirstName());
        employee.setLastName(dto.getLastName());
        employee.setEmail(dto.getEmail());
        employee.setPhone(dto.getPhone());
        employee.setRole(dto.getRole());
        employee.setStatus(dto.getStatus());
        // Set department and shiftGroup if needed
        employee.setHireDate(dto.getHireDate());
        employee.setTerminationDate(dto.getTerminationDate());
        return employee;
    }
}
