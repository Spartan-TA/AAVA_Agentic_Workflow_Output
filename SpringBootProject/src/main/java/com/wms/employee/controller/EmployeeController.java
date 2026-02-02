package com.wms.employee.controller;

import com.wms.employee.dto.EmployeeDto;
import com.wms.employee.service.EmployeeService;
import com.wms.common.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse as SwaggerApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for employee management.
 */
@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
@Tag(name = "Employee", description = "Employee management endpoints")
public class EmployeeController {
    private final EmployeeService employeeService;

    @GetMapping
    @Operation(summary = "Get all employees", description = "Returns a list of all employees")
    @SwaggerApiResponse(responseCode = "200", description = "List of employees returned successfully")
    public ResponseEntity<ApiResponse<List<EmployeeDto>>> getAllEmployees() {
        return ResponseEntity.ok(ApiResponse.<List<EmployeeDto>>builder()
                .success(true)
                .message("Employees fetched successfully")
                .data(employeeService.getAllEmployees())
                .build());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get employee by ID", description = "Returns employee details by ID")
    @SwaggerApiResponse(responseCode = "200", description = "Employee returned successfully")
    @SwaggerApiResponse(responseCode = "404", description = "Employee not found")
    public ResponseEntity<ApiResponse<EmployeeDto>> getEmployeeById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.<EmployeeDto>builder()
                .success(true)
                .message("Employee fetched successfully")
                .data(employeeService.getEmployeeById(id))
                .build());
    }

    @PostMapping
    @Operation(summary = "Create employee", description = "Creates a new employee")
    @SwaggerApiResponse(responseCode = "201", description = "Employee created successfully")
    public ResponseEntity<ApiResponse<EmployeeDto>> createEmployee(@Valid @RequestBody EmployeeDto dto) {
        EmployeeDto created = employeeService.createEmployee(dto);
        return ResponseEntity.status(201).body(ApiResponse.<EmployeeDto>builder()
                .success(true)
                .message("Employee created successfully")
                .data(created)
                .build());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update employee", description = "Updates an existing employee")
    @SwaggerApiResponse(responseCode = "200", description = "Employee updated successfully")
    @SwaggerApiResponse(responseCode = "404", description = "Employee not found")
    public ResponseEntity<ApiResponse<EmployeeDto>> updateEmployee(@PathVariable Long id, @Valid @RequestBody EmployeeDto dto) {
        EmployeeDto updated = employeeService.updateEmployee(id, dto);
        return ResponseEntity.ok(ApiResponse.<EmployeeDto>builder()
                .success(true)
                .message("Employee updated successfully")
                .data(updated)
                .build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete employee", description = "Deletes an employee (soft delete)")
    @SwaggerApiResponse(responseCode = "204", description = "Employee deleted successfully")
    @SwaggerApiResponse(responseCode = "404", description = "Employee not found")
    public ResponseEntity<ApiResponse<Void>> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.status(204).body(ApiResponse.<Void>builder()
                .success(true)
                .message("Employee deleted successfully")
                .data(null)
                .build());
    }
}
