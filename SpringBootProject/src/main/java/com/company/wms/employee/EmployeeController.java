package com.company.wms.employee;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.List;

/**
 * REST controller for Employee management.
 */
@RestController
@RequestMapping("/api/employees")
@Validated
public class EmployeeController {
    private static final Logger logger = LoggerFactory.getLogger(EmployeeController.class);

    @Autowired
    private EmployeeService employeeService;

    /**
     * Get all employees.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<List<EmployeeDTO>> getAllEmployees() {
        logger.info("API: Get all employees");
        return ResponseEntity.ok(employeeService.getAllEmployees());
    }

    /**
     * Get employee by ID.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<EmployeeDTO> getEmployeeById(@PathVariable Long id) {
        logger.info("API: Get employee by id: {}", id);
        return ResponseEntity.ok(employeeService.getEmployeeById(id));
    }

    /**
     * Create a new employee.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EmployeeDTO> createEmployee(@Valid @RequestBody EmployeeDTO dto) {
        logger.info("API: Create employee");
        return ResponseEntity.ok(employeeService.createEmployee(dto));
    }

    /**
     * Update an employee.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EmployeeDTO> updateEmployee(@PathVariable Long id, @Valid @RequestBody EmployeeDTO dto) {
        logger.info("API: Update employee id: {}", id);
        return ResponseEntity.ok(employeeService.updateEmployee(id, dto));
    }

    /**
     * Soft delete an employee.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        logger.info("API: Delete employee id: {}", id);
        employeeService.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }
}
