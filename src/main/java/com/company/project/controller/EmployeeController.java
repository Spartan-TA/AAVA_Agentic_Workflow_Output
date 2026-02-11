package com.company.project.controller;

import com.company.project.dto.EmployeeRequest;
import com.company.project.dto.EmployeeResponse;
import com.company.project.service.EmployeeService;
import com.company.project.mapper.EmployeeMapper;
import com.company.project.exception.EmployeeNotFoundException;
import com.company.project.exception.DuplicateBadgeIdException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/employees")
@Tag(name = "Employee Management", description = "CRUD operations for employees")
public class EmployeeController {

    private final EmployeeService employeeService;
    private final EmployeeMapper employeeMapper;

    @Autowired
    public EmployeeController(EmployeeService employeeService, EmployeeMapper employeeMapper) {
        this.employeeService = employeeService;
        this.employeeMapper = employeeMapper;
    }

    @Operation(summary = "Create new employee", responses = {
            @ApiResponse(responseCode = "201", description = "Employee created successfully"),
            @ApiResponse(responseCode = "409", description = "Duplicate badgeId")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    @PostMapping
    public ResponseEntity<EmployeeResponse> createEmployee(@Valid @RequestBody EmployeeRequest request) {
        try {
            var employee = employeeService.createEmployee(request);
            return ResponseEntity.status(201).body(employeeMapper.toResponse(employee));
        } catch (DuplicateBadgeIdException e) {
            throw e;
        }
    }

    @Operation(summary = "Get all employees", responses = {
            @ApiResponse(responseCode = "200", description = "List of employees")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'SUPERVISOR')")
    @GetMapping
    public ResponseEntity<List<EmployeeResponse>> getAllEmployees() {
        var employees = employeeService.getAllEmployees();
        return ResponseEntity.ok(employeeMapper.toResponseList(employees));
    }

    @Operation(summary = "Get employee by ID", responses = {
            @ApiResponse(responseCode = "200", description = "Employee found"),
            @ApiResponse(responseCode = "404", description = "Employee not found")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'SUPERVISOR', 'WORKER')")
    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponse> getEmployeeById(@PathVariable Long id) {
        var employee = employeeService.getEmployeeById(id)
                .orElseThrow(() -> new EmployeeNotFoundException(id));
        return ResponseEntity.ok(employeeMapper.toResponse(employee));
    }

    @Operation(summary = "Update employee", responses = {
            @ApiResponse(responseCode = "200", description = "Employee updated successfully"),
            @ApiResponse(responseCode = "404", description = "Employee not found")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    @PutMapping("/{id}")
    public ResponseEntity<EmployeeResponse> updateEmployee(@PathVariable Long id, @Valid @RequestBody EmployeeRequest request) {
        var employee = employeeService.updateEmployee(id, request)
                .orElseThrow(() -> new EmployeeNotFoundException(id));
        return ResponseEntity.ok(employeeMapper.toResponse(employee));
    }

    @Operation(summary = "Delete employee (soft delete)", responses = {
            @ApiResponse(responseCode = "204", description = "Employee deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Employee not found")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }
}
