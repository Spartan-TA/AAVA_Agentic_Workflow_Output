package com.warehouse.employee.management.controller;

import com.warehouse.employee.management.dto.EmployeeRequestDto;
import com.warehouse.employee.management.dto.EmployeeResponseDto;
import com.warehouse.employee.management.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * REST controller for Employee CRUD operations.
 */
@RestController
@RequestMapping("/employees")
@RequiredArgsConstructor
@Tag(name = "Employee", description = "Employee management APIs")
@Validated
public class EmployeeController {
    private final EmployeeService employeeService;

    @Operation(summary = "Get all employees (paginated)")
    @GetMapping
    public ResponseEntity<Page<EmployeeResponseDto>> getAllEmployees(Pageable pageable) {
        return ResponseEntity.ok(employeeService.getAllEmployees(pageable));
    }

    @Operation(summary = "Filter employees by name, department, role")
    @GetMapping("/filter")
    public ResponseEntity<Page<EmployeeResponseDto>> filterEmployees(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String role,
            Pageable pageable) {
        return ResponseEntity.ok(employeeService.filterEmployees(name, department, role, pageable));
    }

    @Operation(summary = "Get employee by ID")
    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponseDto> getEmployeeById(@PathVariable Long id) {
        return ResponseEntity.ok(employeeService.getEmployeeById(id));
    }

    @Operation(summary = "Create new employee")
    @PostMapping
    public ResponseEntity<EmployeeResponseDto> createEmployee(@Valid @RequestBody EmployeeRequestDto dto) {
        return new ResponseEntity<>(employeeService.createEmployee(dto), HttpStatus.CREATED);
    }

    @Operation(summary = "Update employee by ID")
    @PutMapping("/{id}")
    public ResponseEntity<EmployeeResponseDto> updateEmployee(@PathVariable Long id, @Valid @RequestBody EmployeeRequestDto dto) {
        return ResponseEntity.ok(employeeService.updateEmployee(id, dto));
    }

    @Operation(summary = "Patch employee by ID")
    @PatchMapping("/{id}")
    public ResponseEntity<EmployeeResponseDto> patchEmployee(@PathVariable Long id, @RequestBody EmployeeRequestDto dto) {
        return ResponseEntity.ok(employeeService.patchEmployee(id, dto));
    }

    @Operation(summary = "Soft-delete employee by ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }
}
