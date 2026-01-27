package com.warehouse.ems.employee;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for employee management.
 */
@RestController
@RequestMapping("/employees")
@RequiredArgsConstructor
public class EmployeeController {
    private final EmployeeService employeeService;

    @Operation(summary = "List employees", description = "Get paginated list of employees")
    @ApiResponse(responseCode = "200", description = "List of employees")
    @GetMapping
    public ResponseEntity<Page<EmployeeResponseDTO>> getAllEmployees(Pageable pageable) {
        return ResponseEntity.ok(employeeService.getAllEmployees(pageable));
    }

    @Operation(summary = "Get employee by ID", description = "Get employee details by ID")
    @ApiResponse(responseCode = "200", description = "Employee found")
    @ApiResponse(responseCode = "404", description = "Employee not found")
    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponseDTO> getEmployeeById(@PathVariable Long id) {
        return ResponseEntity.ok(employeeService.getEmployeeById(id));
    }

    @Operation(summary = "Create employee", description = "Create a new employee")
    @ApiResponse(responseCode = "201", description = "Employee created")
    @PostMapping
    public ResponseEntity<EmployeeResponseDTO> createEmployee(@Valid @RequestBody EmployeeRequestDTO dto) {
        return new ResponseEntity<>(employeeService.createEmployee(dto), HttpStatus.CREATED);
    }

    @Operation(summary = "Update employee", description = "Update an existing employee")
    @ApiResponse(responseCode = "200", description = "Employee updated")
    @PutMapping("/{id}")
    public ResponseEntity<EmployeeResponseDTO> updateEmployee(@PathVariable Long id, @Valid @RequestBody EmployeeRequestDTO dto) {
        return ResponseEntity.ok(employeeService.updateEmployee(id, dto));
    }

    @Operation(summary = "Patch employee", description = "Partially update an employee")
    @ApiResponse(responseCode = "200", description = "Employee patched")
    @PatchMapping("/{id}")
    public ResponseEntity<EmployeeResponseDTO> patchEmployee(@PathVariable Long id, @RequestBody EmployeeRequestDTO dto) {
        return ResponseEntity.ok(employeeService.patchEmployee(id, dto));
    }

    @Operation(summary = "Delete employee", description = "Soft delete an employee")
    @ApiResponse(responseCode = "204", description = "Employee deleted")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }
}
