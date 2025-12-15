package com.warehouseems.employee.controller;

import com.warehouseems.employee.dto.EmployeeDto;
import com.warehouseems.employee.entity.Employee;
import com.warehouseems.employee.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;

/**
 * REST Controller for Employee CRUD operations.
 * Supports filtering, pagination, and OpenAPI documentation.
 */
@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
@Tag(name = "Employee", description = "Employee Management APIs")
public class EmployeeController {
    private final EmployeeService employeeService;

    @Operation(summary = "Create a new employee")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<Employee> createEmployee(@Valid @RequestBody EmployeeDto dto) {
        Employee created = employeeService.createEmployee(dto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @Operation(summary = "Get all employees with optional filters and pagination")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'SUPERVISOR')")
    public Page<Employee> getAllEmployees(
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @Parameter(description = "Hire date start (yyyy-MM-dd)") String hireStart,
            @RequestParam(required = false) @Parameter(description = "Hire date end (yyyy-MM-dd)") String hireEnd,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        LocalDate start = hireStart != null ? LocalDate.parse(hireStart) : null;
        LocalDate end = hireEnd != null ? LocalDate.parse(hireEnd) : null;
        return employeeService.getAllEmployees(department, role, status, start, end, pageable);
    }

    @Operation(summary = "Get employee by ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'SUPERVISOR')")
    public Employee getEmployeeById(@PathVariable Long id) {
        return employeeService.getEmployeeById(id);
    }

    @Operation(summary = "Update employee by ID")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public Employee updateEmployee(@PathVariable Long id, @Valid @RequestBody EmployeeDto dto) {
        return employeeService.updateEmployee(id, dto);
    }

    @Operation(summary = "Soft delete employee by ID")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }
}
