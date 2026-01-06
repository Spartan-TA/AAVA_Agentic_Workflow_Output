package com.warehouse.management.controller;

import com.warehouse.management.dto.*;
import com.warehouse.management.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
@Tag(name = "Employee Management", description = "APIs for managing employees")
public class EmployeeController {
    private final EmployeeService employeeService;

    @Operation(summary = "Get all employees with pagination and filtering", responses = {
            @ApiResponse(responseCode = "200", description = "List of employees", content = @Content(mediaType = "application/json", schema = @Schema(implementation = EmployeeDTO.class)))
    })
    @GetMapping
    public ResponseEntity<Page<EmployeeDTO>> getAllEmployees(
            @Parameter(description = "Filter by department") @RequestParam(required = false) String department,
            @Parameter(description = "Filter by job title") @RequestParam(required = false) String jobTitle,
            @Parameter(description = "Filter by active status") @RequestParam(required = false) Boolean active,
            @PageableDefault Pageable pageable) {
        return ResponseEntity.ok(employeeService.getAllEmployees(department, jobTitle, active, pageable));
    }

    @Operation(summary = "Get employee by ID")
    @GetMapping("/{id}")
    public ResponseEntity<EmployeeDTO> getEmployeeById(@PathVariable Long id) {
        return ResponseEntity.ok(employeeService.getEmployeeById(id));
    }

    @Operation(summary = "Create a new employee")
    @PostMapping
    public ResponseEntity<EmployeeDTO> createEmployee(@Valid @RequestBody EmployeeCreateDTO dto) {
        return ResponseEntity.ok(employeeService.createEmployee(dto));
    }

    @Operation(summary = "Update an employee")
    @PutMapping("/{id}")
    public ResponseEntity<EmployeeDTO> updateEmployee(@PathVariable Long id, @Valid @RequestBody EmployeeUpdateDTO dto) {
        return ResponseEntity.ok(employeeService.updateEmployee(id, dto));
    }

    @Operation(summary = "Partially update an employee")
    @PatchMapping("/{id}")
    public ResponseEntity<EmployeeDTO> patchEmployee(@PathVariable Long id, @RequestBody EmployeeUpdateDTO dto) {
        return ResponseEntity.ok(employeeService.patchEmployee(id, dto));
    }

    @Operation(summary = "Delete an employee")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }
}