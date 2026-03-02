package com.wms.employee;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * REST Controller for Employee management.
 * Provides CRUD endpoints with role-based access control.
 * 
 * Security:
 * - Create/Update/Delete: ADMIN, HR roles
 * - Read: ADMIN, HR, SUPERVISOR roles
 * 
 * API Documentation:
 * - OpenAPI/Swagger enabled
 * - All endpoints documented with @Operation
 */
@RestController
@RequestMapping("/api/v1/employees")
@Tag(name = "Employee Management", description = "Employee CRUD operations")
@Validated
@Slf4j
public class EmployeeController {
    
    private final EmployeeService employeeService;
    
    /**
     * Constructor-based dependency injection.
     * 
     * @param employeeService Service for employee business logic
     */
    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }
    
    /**
     * Create a new employee.
     * 
     * @param dto Employee data
     * @return Created employee with 201 status
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @Operation(summary = "Create new employee", 
               description = "Creates a new employee record. Requires ADMIN or HR role.")
    public ResponseEntity<EmployeeDto> createEmployee(@Valid @RequestBody EmployeeDto dto) {
        log.info("REST request to create employee: {}", dto.getBadgeId());
        Employee employee = employeeService.createEmployee(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapToDto(employee));
    }
    
    /**
     * Get all employees with optional filtering.
     * 
     * @param status Optional status filter
     * @param pageable Pagination parameters
     * @return Page of employees
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'SUPERVISOR')")
    @Operation(summary = "List employees",
               description = "Retrieves paginated list of employees with optional status filter.")
    public ResponseEntity<Page<EmployeeDto>> getEmployees(
            @RequestParam(required = false) EmployeeStatus status,
            Pageable pageable) {
        log.debug("REST request to get employees with status: {}", status);
        Page<EmployeeDto> employees = employeeService.getAllEmployees(status, pageable)
                .map(this::mapToDto);
        return ResponseEntity.ok(employees);
    }
    
    /**
     * Get employee by ID.
     * 
     * @param id Employee ID
     * @return Employee data or 404 if not found
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'SUPERVISOR')")
    @Operation(summary = "Get employee by ID",
               description = "Retrieves a single employee by their ID.")
    public ResponseEntity<EmployeeDto> getEmployee(@PathVariable Long id) {
        log.debug("REST request to get employee: {}", id);
        return employeeService.getEmployeeById(id)
                .map(this::mapToDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Get employee by badge ID.
     * 
     * @param badgeId Badge ID
     * @return Employee data or 404 if not found
     */
    @GetMapping("/badge/{badgeId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'SUPERVISOR')")
    @Operation(summary = "Get employee by badge ID",
               description = "Retrieves a single employee by their badge ID.")
    public ResponseEntity<EmployeeDto> getEmployeeByBadge(@PathVariable String badgeId) {
        log.debug("REST request to get employee by badge: {}", badgeId);
        return employeeService.getEmployeeByBadgeId(badgeId)
                .map(this::mapToDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Update an existing employee.
     * 
     * @param id Employee ID
     * @param dto Updated employee data
     * @return Updated employee
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @Operation(summary = "Update employee",
               description = "Updates an existing employee record. Requires ADMIN or HR role.")
    public ResponseEntity<EmployeeDto> updateEmployee(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeDto dto) {
        log.info("REST request to update employee: {}", id);
        Employee employee = employeeService.updateEmployee(id, dto);
        return ResponseEntity.ok(mapToDto(employee));
    }
    
    /**
     * Soft delete an employee.
     * 
     * @param id Employee ID
     * @return 204 No Content on success
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @Operation(summary = "Soft delete employee",
               description = "Marks an employee as deleted without removing the record. Requires ADMIN or HR role.")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        log.info("REST request to delete employee: {}", id);
        employeeService.softDeleteEmployee(id);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Search employees by multiple criteria.
     * 
     * @param department Optional department filter
     * @param role Optional role filter
     * @param status Optional status filter
     * @param pageable Pagination parameters
     * @return Page of matching employees
     */
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'SUPERVISOR')")
    @Operation(summary = "Search employees",
               description = "Search employees by department, role, and/or status.")
    public ResponseEntity<Page<EmployeeDto>> searchEmployees(
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) EmployeeStatus status,
            Pageable pageable) {
        log.debug("REST request to search employees");
        Page<EmployeeDto> employees = employeeService.searchEmployees(
                department, role, status, pageable)
                .map(this::mapToDto);
        return ResponseEntity.ok(employees);
    }
    
    /**
     * Map Employee entity to DTO.
     * 
     * @param employee Employee entity
     * @return Employee DTO
     */
    private EmployeeDto mapToDto(Employee employee) {
        return EmployeeDto.builder()
                .id(employee.getId())
                .badgeId(employee.getBadgeId())
                .name(employee.getName())
                .role(employee.getRole())
                .department(employee.getDepartment())
                .shiftGroup(employee.getShiftGroup())
                .hireDate(employee.getHireDate())
                .status(employee.getStatus())
                .build();
    }
}
