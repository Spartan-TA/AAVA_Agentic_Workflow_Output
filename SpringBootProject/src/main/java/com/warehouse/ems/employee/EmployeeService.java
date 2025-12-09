package com.warehouse.ems.employee;

import com.warehouse.ems.exception.ResourceNotFoundException;
import com.warehouse.ems.exception.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.Optional;

@Service
public class EmployeeService {
    private static final Logger logger = LoggerFactory.getLogger(EmployeeService.class);

    @Autowired
    private EmployeeRepository employeeRepository;

    /**
     * Get all active employees.
     * @return List of Employee
     */
    public List<Employee> getAllEmployees() {
        logger.info("Fetching all active employees");
        return employeeRepository.findByDeletedFalse();
    }

    /**
     * Get employee by ID.
     * @param id Employee ID
     * @return Employee
     */
    public Employee getEmployeeById(Long id) {
        logger.info("Fetching employee with id: {}", id);
        return employeeRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));
    }

    /**
     * Create a new employee with validation.
     * @param employee Employee entity
     * @return Employee
     */
    public Employee createEmployee(Employee employee) {
        validateEmployee(employee);
        logger.info("Creating new employee: {}", employee.getEmail());
        return employeeRepository.save(employee);
    }

    /**
     * Update an existing employee.
     * @param id Employee ID
     * @param updated Employee entity
     * @return Employee
     */
    public Employee updateEmployee(Long id, Employee updated) {
        Employee existing = getEmployeeById(id);
        validateEmployee(updated);
        existing.setName(updated.getName());
        existing.setEmail(updated.getEmail());
        existing.setRole(updated.getRole());
        existing.setPhone(updated.getPhone());
        logger.info("Updating employee with id: {}", id);
        return employeeRepository.save(existing);
    }

    /**
     * Soft delete an employee.
     * @param id Employee ID
     */
    @Transactional
    public void deleteEmployee(Long id) {
        Employee employee = getEmployeeById(id);
        employee.setDeleted(true);
        logger.info("Soft deleting employee with id: {}", id);
        employeeRepository.save(employee);
    }

    /**
     * Validate employee fields.
     * @param employee Employee entity
     */
    private void validateEmployee(Employee employee) {
        if (employee.getName() == null || employee.getName().isEmpty()) {
            throw new ValidationException("Employee name is required");
        }
        if (employee.getEmail() == null || employee.getEmail().isEmpty()) {
            throw new ValidationException("Employee email is required");
        }
        // Additional validations can be added here
    }
}
