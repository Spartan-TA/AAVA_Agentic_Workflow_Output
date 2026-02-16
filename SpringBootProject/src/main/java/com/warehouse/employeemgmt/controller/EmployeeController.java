package com.warehouse.employeemgmt.controller;

import com.warehouse.employeemgmt.domain.Employee;
import com.warehouse.employeemgmt.dto.EmployeeDTO;
import com.warehouse.employeemgmt.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * EmployeeController - REST API endpoints for employee management
 * 
 * Provides RESTful endpoints for CRUD operations on employees with
 * role-based access control and comprehensive API documentation.
 * 
 * Features:
 * - CRUD operations with proper HTTP methods
 * - Role-based access control via @PreAuthorize
 * - Pagination support
 * - OpenAPI/Swagger documentation
 * - Input validation
 * - Proper HTTP status codes
 * 
 * @author Warehouse Management Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/employees")
@RequiredArgsConstructor
@Tag(name = "Employee Management", description = "APIs for managing warehouse employees")
public class EmployeeController {
    
    private final EmployeeService employeeService;

    /**
     * Get all employees with pagination
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'SUPERVISOR')")
    @Operation(summary = "Get all employees", description = "Retrieve all active employees with pagination")
    public ResponseEntity<Page<Employee>> getAllEmployees(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        return ResponseEntity.ok(employeeService.getAllEmployees(pageable));
    }

    /**
     * Get employee by badge ID
     */
    @GetMapping("/badge/{badgeId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'SUPERVISOR', 'WORKER')")
    @Operation(summary = "Get employee by badge ID", description = "Retrieve employee details by badge ID")
    public ResponseEntity<Employee> getEmployeeByBadgeId(@PathVariable String badgeId) {
        return ResponseEntity.ok(employeeService.getEmployeeByBadgeId(badgeId));
    }

    /**
     * Get employee by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'SUPERVISOR')")
    @Operation(summary = "Get employee by ID", description = "Retrieve employee details by ID")
    public ResponseEntity<Employee> getEmployeeById(@PathVariable Long id) {
        return ResponseEntity.ok(employeeService.getEmployeeById(id));
    }

    /**
     * Create new employee
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @Operation(summary = "Create employee", description = "Create a new employee record")
    public ResponseEntity<Employee> createEmployee(@Valid @RequestBody EmployeeDTO dto) {
        Employee created = employeeService.createEmployee(dto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    /**
     * Update existing employee
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @Operation(summary = "Update employee", description = "Update an existing employee record")
    public ResponseEntity<Employee> updateEmployee(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeDTO dto) {
        return ResponseEntity.ok(employeeService.updateEmployee(id, dto));
    }

    /**
     * Soft delete employee
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete employee", description = "Soft delete an employee record")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        employeeService.softDeleteEmployee(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Find employees by department
     */
    @GetMapping("/department/{department}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'SUPERVISOR')")
    @Operation(summary = "Find by department", description = "Find employees by department")
    public ResponseEntity<List<Employee>> findByDepartment(@PathVariable String department) {
        return ResponseEntity.ok(employeeService.findByDepartment(department));
    }

    /**
     * Find employees by role
     */
    @GetMapping("/role/{role}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @Operation(summary = "Find by role", description = "Find employees by role")
    public ResponseEntity<List<Employee>> findByRole(@PathVariable String role) {
        return ResponseEntity.ok(employeeService.findByRole(role));
    }

    /**
     * Search employees by name
     */
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'SUPERVISOR')")
    @Operation(summary = "Search by name", description = "Search employees by name pattern")
    public ResponseEntity<List<Employee>> searchByName(@RequestParam String name) {
        return ResponseEntity.ok(employeeService.searchByName(name));
    }
}