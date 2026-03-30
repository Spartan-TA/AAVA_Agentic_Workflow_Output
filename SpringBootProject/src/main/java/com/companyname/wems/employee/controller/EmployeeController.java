package com.companyname.wems.employee.controller;

import com.companyname.wems.employee.dto.EmployeeRequest;
import com.companyname.wems.employee.dto.EmployeeResponse;
import com.companyname.wems.employee.entity.Employee;
import com.companyname.wems.employee.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Employee management
 * 
 * Provides RESTful endpoints for employee CRUD operations with:
 * - Role-based access control (RBAC)
 * - Request validation
 * - Pagination support
 * - OpenAPI documentation
 * - Comprehensive error handling
 * 
 * Security:
 * - POST/PUT/DELETE: ADMIN or HR roles required
 * - GET: ADMIN, HR, or SUPERVISOR roles required
 * 
 * @author WEMS Development Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/employees")
@RequiredArgsConstructor
@Tag(name = "Employee Management", description = "APIs for managing warehouse employees")
public class EmployeeController {
    
    private static final Logger logger = LoggerFactory.getLogger(EmployeeController.class);
    
    private final EmployeeService employeeService;

    /**
     * Create a new employee
     * 
     * @param request Employee creation request
     * @return Created employee response
     */
    @Operation(summary = "Create a new employee", 
               description = "Creates a new employee record with unique badge ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Employee created successfully",
                    content = @Content(schema = @Schema(implementation = EmployeeResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "409", description = "Badge ID already exists"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public ResponseEntity<EmployeeResponse> createEmployee(
            @Valid @RequestBody EmployeeRequest request) {
        
        logger.info("REST request to create employee: {}", request.getName());
        
        Employee employee = employeeService.createEmployee(request.toEntity());
        EmployeeResponse response = EmployeeResponse.fromEntity(employee);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get employee by ID
     * 
     * @param id Employee ID
     * @return Employee response
     */
    @Operation(summary = "Get employee by ID", 
               description = "Retrieves employee details by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Employee found",
                    content = @Content(schema = @Schema(implementation = EmployeeResponse.class))),
        @ApiResponse(responseCode = "404", description = "Employee not found"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'SUPERVISOR')")
    public ResponseEntity<EmployeeResponse> getEmployee(
            @Parameter(description = "Employee ID") @PathVariable Long id) {
        
        logger.debug("REST request to get employee: {}", id);
        
        Employee employee = employeeService.getEmployee(id);
        EmployeeResponse response = EmployeeResponse.fromEntity(employee);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get employee by badge ID
     * 
     * @param badgeId Badge ID
     * @return Employee response
     */
    @Operation(summary = "Get employee by badge ID", 
               description = "Retrieves employee details by badge ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Employee found"),
        @ApiResponse(responseCode = "404", description = "Employee not found")
    })
    @GetMapping("/badge/{badgeId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'SUPERVISOR')")
    public ResponseEntity<EmployeeResponse> getEmployeeByBadgeId(
            @Parameter(description = "Badge ID") @PathVariable String badgeId) {
        
        logger.debug("REST request to get employee by badge ID: {}", badgeId);
        
        Employee employee = employeeService.getEmployeeByBadgeId(badgeId);
        EmployeeResponse response = EmployeeResponse.fromEntity(employee);
        
        return ResponseEntity.ok(response);
    }

    /**
     * List all employees with pagination and filtering
     * 
     * @param department Optional department filter
     * @param status Optional status filter
     * @param pageable Pagination parameters
     * @return Page of employee responses
     */
    @Operation(summary = "List employees", 
               description = "Retrieves paginated list of employees with optional filters")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Employees retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'SUPERVISOR')")
    public ResponseEntity<Page<EmployeeResponse>> listEmployees(
            @Parameter(description = "Filter by department") 
            @RequestParam(required = false) String department,
            
            @Parameter(description = "Filter by status") 
            @RequestParam(required = false) Employee.Status status,
            
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) 
            Pageable pageable) {
        
        logger.debug("REST request to list employees - Department: {}, Status: {}", 
                    department, status);
        
        Page<Employee> employees = employeeService.listEmployees(department, status, pageable);
        Page<EmployeeResponse> response = employees.map(EmployeeResponse::fromEntity);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Search employees by name
     * 
     * @param namePattern Name pattern to search
     * @param pageable Pagination parameters
     * @return Page of matching employees
     */
    @Operation(summary = "Search employees by name", 
               description = "Searches employees by name pattern (case-insensitive)")
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'SUPERVISOR')")
    public ResponseEntity<Page<EmployeeResponse>> searchEmployees(
            @Parameter(description = "Name pattern to search") 
            @RequestParam String namePattern,
            
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) 
            Pageable pageable) {
        
        logger.debug("REST request to search employees by name: {}", namePattern);
        
        Page<Employee> employees = employeeService.searchEmployeesByName(namePattern, pageable);
        Page<EmployeeResponse> response = employees.map(EmployeeResponse::fromEntity);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Update an existing employee
     * 
     * @param id Employee ID
     * @param request Employee update request
     * @return Updated employee response
     */
    @Operation(summary = "Update employee", 
               description = "Updates an existing employee record")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Employee updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "404", description = "Employee not found"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public ResponseEntity<EmployeeResponse> updateEmployee(
            @Parameter(description = "Employee ID") @PathVariable Long id,
            @Valid @RequestBody EmployeeRequest request) {
        
        logger.info("REST request to update employee: {}", id);
        
        Employee employee = employeeService.updateEmployee(id, request.toEntity());
        EmployeeResponse response = EmployeeResponse.fromEntity(employee);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Delete (soft) an employee
     * 
     * @param id Employee ID
     * @return No content
     */
    @Operation(summary = "Delete employee", 
               description = "Soft-deletes an employee (sets status to TERMINATED)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Employee deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Employee not found"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteEmployee(
            @Parameter(description = "Employee ID") @PathVariable Long id) {
        
        logger.info("REST request to delete employee: {}", id);
        
        employeeService.deleteEmployee(id);
        
        return ResponseEntity.noContent().build();
    }

    /**
     * Get employee count by department
     * 
     * @param department Department name
     * @return Employee count
     */
    @Operation(summary = "Get employee count by department", 
               description = "Returns the number of employees in a department")
    @GetMapping("/count/department/{department}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'SUPERVISOR')")
    public ResponseEntity<Long> getEmployeeCountByDepartment(
            @Parameter(description = "Department name") @PathVariable String department) {
        
        logger.debug("REST request to count employees in department: {}", department);
        
        long count = employeeService.getEmployeeCountByDepartment(department);
        
        return ResponseEntity.ok(count);
    }

    /**
     * Get employee count by status
     * 
     * @param status Employee status
     * @return Employee count
     */
    @Operation(summary = "Get employee count by status", 
               description = "Returns the number of employees with a specific status")
    @GetMapping("/count/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<Long> getEmployeeCountByStatus(
            @Parameter(description = "Employee status") @PathVariable Employee.Status status) {
        
        logger.debug("REST request to count employees with status: {}", status);
        
        long count = employeeService.getEmployeeCountByStatus(status);
        
        return ResponseEntity.ok(count);
    }
}