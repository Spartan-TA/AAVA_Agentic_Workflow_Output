package com.warehouse.employee.management.controller;

import com.warehouse.employee.management.dto.EmployeeCreateRequest;
import com.warehouse.employee.management.dto.EmployeeUpdateRequest;
import com.warehouse.employee.management.dto.EmployeeResponse;
import com.warehouse.employee.management.service.EmployeeService;
import com.warehouse.employee.management.exception.ResourceNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.List;

/**
 * Controller for Employee management endpoints.
 */
@RestController
@RequestMapping("/employees")
@Tag(name = "Employee Management", description = "CRUD operations for employees")
public class EmployeeController {
    private final EmployeeService employeeService;

    @Autowired
    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @Operation(summary = "Get paginated list of employees", responses = {@ApiResponse(responseCode = "200", description = "List of employees")})
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @GetMapping
    public ResponseEntity<Page<EmployeeResponse>> getAllEmployees(Pageable pageable) {
        Page<EmployeeResponse> employees = employeeService.getAllEmployees(pageable);
        return ResponseEntity.ok(employees);
    }

    @Operation(summary = "Get employee by ID", responses = {@ApiResponse(responseCode = "200", description = "Employee found"), @ApiResponse(responseCode = "404", description = "Employee not found")})
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponse> getEmployeeById(@Parameter(description = "Employee ID") @PathVariable Long id) {
        EmployeeResponse employee = employeeService.getEmployeeById(id);
        return ResponseEntity.ok(employee);
    }

    @Operation(summary = "Create a new employee", responses = {@ApiResponse(responseCode = "201", description = "Employee created")})
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<EmployeeResponse> createEmployee(@Valid @RequestBody EmployeeCreateRequest request) {
        EmployeeResponse employee = employeeService.createEmployee(request);
        return ResponseEntity.status(201).body(employee);
    }

    @Operation(summary = "Update an employee", responses = {@ApiResponse(responseCode = "200", description = "Employee updated"), @ApiResponse(responseCode = "404", description = "Employee not found")})
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<EmployeeResponse> updateEmployee(@Parameter(description = "Employee ID") @PathVariable Long id, @Valid @RequestBody EmployeeUpdateRequest request) {
        EmployeeResponse employee = employeeService.updateEmployee(id, request);
        return ResponseEntity.ok(employee);
    }

    @Operation(summary = "Delete an employee", responses = {@ApiResponse(responseCode = "204", description = "Employee deleted"), @ApiResponse(responseCode = "404", description = "Employee not found")})
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(@Parameter(description = "Employee ID") @PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }
}
