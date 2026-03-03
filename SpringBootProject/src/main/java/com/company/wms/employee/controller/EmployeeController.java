package com.company.wms.employee.controller;

import com.company.wms.employee.dto.EmployeeDTO;
import com.company.wms.employee.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Employee management operations.
 * Provides CRUD endpoints with role-based access control.
 * 
 * @author WMS Development Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
@Tag(name = "Employee Management", description = "APIs for managing warehouse employees")
public class EmployeeController {
    
    private final EmployeeService employeeService;

    /**
     * Get paginated list of employees with optional filters
     */
    @Operation(summary = "Get all employees", description = "Retrieve paginated list of active employees with optional filtering")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved employees"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','HR','SUPERVISOR')")
    public ResponseEntity<Page<EmployeeDTO>> getAll(
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Filter by department") @RequestParam(required = false) String department,
            @Parameter(description = "Filter by role") @RequestParam(required = false) String role,
            @Parameter(description = "Search by name") @RequestParam(required = false) String search
    ) {
        Page<EmployeeDTO> employees = employeeService.getAll(page, size, department, role, search);
        return ResponseEntity.ok(employees);
    }

    /**
     * Get employee by ID
     */
    @Operation(summary = "Get employee by ID", description = "Retrieve a specific employee by their ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Employee found"),
        @ApiResponse(responseCode = "404", description = "Employee not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','HR','SUPERVISOR','WORKER')")
    public ResponseEntity<EmployeeDTO> getById(
            @Parameter(description = "Employee ID") @PathVariable Long id
    ) {
        EmployeeDTO employee = employeeService.getById(id);
        return ResponseEntity.ok(employee);
    }

    /**
     * Get employee by badge ID
     */
    @Operation(summary = "Get employee by badge ID", description = "Retrieve employee using their unique badge identifier")
    @GetMapping("/badge/{badgeId}")
    @PreAuthorize("hasAnyRole('ADMIN','HR','SUPERVISOR')")
    public ResponseEntity<EmployeeDTO> getByBadgeId(
            @Parameter(description = "Badge ID") @PathVariable String badgeId
    ) {
        EmployeeDTO employee = employeeService.getByBadgeId(badgeId);
        return ResponseEntity.ok(employee);
    }

    /**
     * Create new employee
     */
    @Operation(summary = "Create new employee", description = "Add a new employee to the system")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Employee created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "409", description = "Employee with badge ID already exists")
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EmployeeDTO> create(
            @Parameter(description = "Employee data") @Valid @RequestBody EmployeeDTO dto
    ) {
        EmployeeDTO created = employeeService.create(dto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    /**
     * Update existing employee
     */
    @Operation(summary = "Update employee", description = "Update an existing employee's information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Employee updated successfully"),
        @ApiResponse(responseCode = "404", description = "Employee not found"),
        @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public ResponseEntity<EmployeeDTO> update(
            @Parameter(description = "Employee ID") @PathVariable Long id,
            @Parameter(description = "Updated employee data") @Valid @RequestBody EmployeeDTO dto
    ) {
        EmployeeDTO updated = employeeService.update(id, dto);
        return ResponseEntity.ok(updated);
    }

    /**
     * Soft delete employee
     */
    @Operation(summary = "Delete employee", description = "Soft delete an employee (marks as deleted, preserves data)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Employee deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Employee not found")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Employee ID") @PathVariable Long id
    ) {
        employeeService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get employees by department
     */
    @Operation(summary = "Get employees by department", description = "Retrieve all employees in a specific department")
    @GetMapping("/department/{department}")
    @PreAuthorize("hasAnyRole('ADMIN','HR','SUPERVISOR')")
    public ResponseEntity<List<EmployeeDTO>> getByDepartment(
            @Parameter(description = "Department name") @PathVariable String department
    ) {
        List<EmployeeDTO> employees = employeeService.getByDepartment(department);
        return ResponseEntity.ok(employees);
    }

    /**
     * Get employees by shift group
     */
    @Operation(summary = "Get employees by shift group", description = "Retrieve all employees in a specific shift group")
    @GetMapping("/shift-group/{shiftGroup}")
    @PreAuthorize("hasAnyRole('ADMIN','HR','SUPERVISOR')")
    public ResponseEntity<List<EmployeeDTO>> getByShiftGroup(
            @Parameter(description = "Shift group identifier") @PathVariable String shiftGroup
    ) {
        List<EmployeeDTO> employees = employeeService.getByShiftGroup(shiftGroup);
        return ResponseEntity.ok(employees);
    }

    /**
     * Count employees by department
     */
    @Operation(summary = "Count employees by department", description = "Get the count of active employees in a department")
    @GetMapping("/count/department/{department}")
    @PreAuthorize("hasAnyRole('ADMIN','HR','SUPERVISOR')")
    public ResponseEntity<Long> countByDepartment(
            @Parameter(description = "Department name") @PathVariable String department
    ) {
        long count = employeeService.countByDepartment(department);
        return ResponseEntity.ok(count);
    }
}