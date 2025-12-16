package com.warehouse.employee;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Employee management operations.
 * Provides CRUD endpoints with role-based access control.
 * 
 * @author Warehouse Development Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/employees")
@Tag(name = "Employee Management", description = "APIs for managing warehouse employees")
public class EmployeeController {
    
    @Autowired
    private EmployeeService employeeService;

    /**
     * Get all employees with pagination and filtering.
     * 
     * @param department Optional department filter
     * @param role Optional role filter
     * @param status Optional status filter
     * @param pageable Pagination parameters
     * @param authentication Current user authentication
     * @return Page of employees
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'SUPERVISOR')")
    @Operation(summary = "Get all employees", description = "Retrieve a paginated list of employees with optional filters")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved employees",
                     content = @Content(schema = @Schema(implementation = Page.class))),
        @ApiResponse(responseCode = "403", description = "Access denied", content = @Content)
    })
    public ResponseEntity<Page<EmployeeDTO>> getAllEmployees(
            @Parameter(description = "Filter by department") @RequestParam(required = false) String department,
            @Parameter(description = "Filter by role") @RequestParam(required = false) String role,
            @Parameter(description = "Filter by status") @RequestParam(required = false) String status,
            @PageableDefault(size = 20) Pageable pageable,
            Authentication authentication) {
        
        Page<EmployeeDTO> employees;
        if (department != null || role != null || status != null) {
            employees = employeeService.searchEmployees(department, role, status, pageable);
        } else {
            employees = employeeService.getAllEmployees(pageable);
        }
        
        return ResponseEntity.ok(employees);
    }

    /**
     * Get employee by ID.
     * 
     * @param id Employee ID
     * @param authentication Current user authentication
     * @return Employee details
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'SUPERVISOR')")
    @Operation(summary = "Get employee by ID", description = "Retrieve a specific employee by their ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Employee found",
                     content = @Content(schema = @Schema(implementation = EmployeeDTO.class))),
        @ApiResponse(responseCode = "404", description = "Employee not found", content = @Content),
        @ApiResponse(responseCode = "403", description = "Access denied", content = @Content)
    })
    public ResponseEntity<EmployeeDTO> getEmployeeById(
            @Parameter(description = "Employee ID") @PathVariable Long id,
            Authentication authentication) {
        
        return employeeService.getEmployeeById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get employee by badge ID.
     * 
     * @param badgeId Employee badge ID
     * @param authentication Current user authentication
     * @return Employee details
     */
    @GetMapping("/badge/{badgeId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'SUPERVISOR')")
    @Operation(summary = "Get employee by badge ID", description = "Retrieve a specific employee by their badge ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Employee found",
                     content = @Content(schema = @Schema(implementation = EmployeeDTO.class))),
        @ApiResponse(responseCode = "404", description = "Employee not found", content = @Content),
        @ApiResponse(responseCode = "403", description = "Access denied", content = @Content)
    })
    public ResponseEntity<EmployeeDTO> getEmployeeByBadgeId(
            @Parameter(description = "Employee badge ID") @PathVariable String badgeId,
            Authentication authentication) {
        
        return employeeService.getEmployeeByBadgeId(badgeId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Create a new employee.
     * 
     * @param employeeDTO Employee data
     * @param authentication Current user authentication
     * @return Created employee
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @Operation(summary = "Create employee", description = "Create a new employee record")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Employee created successfully",
                     content = @Content(schema = @Schema(implementation = EmployeeDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input or badge ID already exists", content = @Content),
        @ApiResponse(responseCode = "403", description = "Access denied", content = @Content)
    })
    public ResponseEntity<EmployeeDTO> createEmployee(
            @Valid @RequestBody EmployeeDTO employeeDTO,
            Authentication authentication) {
        
        String createdBy = authentication.getName();
        EmployeeDTO created = employeeService.createEmployee(employeeDTO, createdBy);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Update an existing employee.
     * 
     * @param id Employee ID
     * @param employeeDTO Updated employee data
     * @param authentication Current user authentication
     * @return Updated employee
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @Operation(summary = "Update employee", description = "Update an existing employee record")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Employee updated successfully",
                     content = @Content(schema = @Schema(implementation = EmployeeDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content),
        @ApiResponse(responseCode = "404", description = "Employee not found", content = @Content),
        @ApiResponse(responseCode = "403", description = "Access denied", content = @Content)
    })
    public ResponseEntity<EmployeeDTO> updateEmployee(
            @Parameter(description = "Employee ID") @PathVariable Long id,
            @Valid @RequestBody EmployeeDTO employeeDTO,
            Authentication authentication) {
        
        String updatedBy = authentication.getName();
        EmployeeDTO updated = employeeService.updateEmployee(id, employeeDTO, updatedBy);
        return ResponseEntity.ok(updated);
    }

    /**
     * Soft-delete an employee.
     * 
     * @param id Employee ID
     * @param authentication Current user authentication
     * @return No content
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete employee", description = "Soft-delete an employee record")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Employee deleted successfully", content = @Content),
        @ApiResponse(responseCode = "404", description = "Employee not found", content = @Content),
        @ApiResponse(responseCode = "403", description = "Access denied", content = @Content)
    })
    public ResponseEntity<Void> deleteEmployee(
            @Parameter(description = "Employee ID") @PathVariable Long id,
            Authentication authentication) {
        
        String deletedBy = authentication.getName();
        employeeService.deleteEmployee(id, deletedBy);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get employees by department.
     * 
     * @param department Department name
     * @param authentication Current user authentication
     * @return List of employees in the department
     */
    @GetMapping("/department/{department}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'SUPERVISOR')")
    @Operation(summary = "Get employees by department", description = "Retrieve all employees in a specific department")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved employees",
                     content = @Content(schema = @Schema(implementation = List.class))),
        @ApiResponse(responseCode = "403", description = "Access denied", content = @Content)
    })
    public ResponseEntity<List<EmployeeDTO>> getEmployeesByDepartment(
            @Parameter(description = "Department name") @PathVariable String department,
            Authentication authentication) {
        
        List<EmployeeDTO> employees = employeeService.getEmployeesByDepartment(department);
        return ResponseEntity.ok(employees);
    }

    /**
     * Get employee count by department.
     * 
     * @param department Department name
     * @param authentication Current user authentication
     * @return Employee count
     */
    @GetMapping("/department/{department}/count")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'SUPERVISOR')")
    @Operation(summary = "Count employees by department", description = "Get the number of employees in a specific department")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Count retrieved successfully", content = @Content),
        @ApiResponse(responseCode = "403", description = "Access denied", content = @Content)
    })
    public ResponseEntity<Long> countEmployeesByDepartment(
            @Parameter(description = "Department name") @PathVariable String department,
            Authentication authentication) {
        
        long count = employeeService.countEmployeesByDepartment(department);
        return ResponseEntity.ok(count);
    }
}