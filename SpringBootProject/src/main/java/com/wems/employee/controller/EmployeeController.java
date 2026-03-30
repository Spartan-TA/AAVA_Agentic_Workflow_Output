package com.wems.employee.controller;

import com.wems.employee.domain.Employee;
import com.wems.employee.dto.EmployeeDTO;
import com.wems.employee.service.EmployeeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * REST controller for Employee operations.
 * Secured with RBAC and exception handling.
 */
@RestController
@RequestMapping("/api/employee")
@Validated
public class EmployeeController {

    private static final Logger logger = LoggerFactory.getLogger(EmployeeController.class);

    @Autowired
    private EmployeeService employeeService;

    /**
     * Creates a new employee. Only ADMIN and HR can create.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public ResponseEntity<Employee> createEmployee(@Valid @RequestBody EmployeeDTO dto) {
        logger.info("API: Create employee");
        Employee employee = employeeService.createEmployee(dto);
        return ResponseEntity.ok(employee);
    }

    /**
     * Gets paginated list of active employees. Accessible to ADMIN, HR, SUPERVISOR.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','HR','SUPERVISOR')")
    public ResponseEntity<Page<Employee>> getActiveEmployees(Pageable pageable) {
        logger.info("API: Get active employees");
        Page<Employee> employees = employeeService.getActiveEmployees(pageable);
        return ResponseEntity.ok(employees);
    }

    /**
     * Updates employee details. Only ADMIN and HR can update.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public ResponseEntity<Employee> updateEmployee(@PathVariable Long id, @Valid @RequestBody EmployeeDTO dto) {
        logger.info("API: Update employee {}", id);
        Employee employee = employeeService.updateEmployee(id, dto);
        return ResponseEntity.ok(employee);
    }

    /**
     * Soft deletes an employee. Only ADMIN can delete.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> softDeleteEmployee(@PathVariable Long id) {
        logger.info("API: Soft delete employee {}"," + " id);
        employeeService.softDeleteEmployee(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Gets employee by badgeId. Accessible to ADMIN, HR, SUPERVISOR.
     */
    @GetMapping("/badge/{badgeId}")
    @PreAuthorize("hasAnyRole('ADMIN','HR','SUPERVISOR')")
    public ResponseEntity<Employee> getEmployeeByBadgeId(@PathVariable String badgeId) {
        logger.info("API: Get employee by badgeId {}"," + " badgeId);
        Employee employee = employeeService.getEmployeeByBadgeId(badgeId);
        return ResponseEntity.ok(employee);
    }
}