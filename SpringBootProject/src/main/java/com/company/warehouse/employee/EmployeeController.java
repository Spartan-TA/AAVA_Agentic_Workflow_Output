package com.company.warehouse.employee;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for Employee CRUD and search APIs.
 */
@RestController
@RequestMapping("/employees")
@RequiredArgsConstructor
@Tag(name = "Employee", description = "Employee master data management")
public class EmployeeController {
    private final EmployeeService employeeService;

    @Operation(summary = "Create a new employee")
    @PostMapping
    public ResponseEntity<EmployeeDTO> create(@Valid @RequestBody EmployeeDTO dto) {
        return new ResponseEntity<>(employeeService.create(dto), HttpStatus.CREATED);
    }

    @Operation(summary = "Get employee by ID")
    @GetMapping("/{id}")
    public ResponseEntity<EmployeeDTO> get(@PathVariable Long id) {
        return ResponseEntity.ok(employeeService.get(id));
    }

    @Operation(summary = "Update employee by ID")
    @PutMapping("/{id}")
    public ResponseEntity<EmployeeDTO> update(@PathVariable Long id, @Valid @RequestBody EmployeeDTO dto) {
        return ResponseEntity.ok(employeeService.update(id, dto));
    }

    @Operation(summary = "Patch employee by ID")
    @PatchMapping("/{id}")
    public ResponseEntity<EmployeeDTO> patch(@PathVariable Long id, @RequestBody EmployeeDTO dto) {
        return ResponseEntity.ok(employeeService.patch(id, dto));
    }

    @Operation(summary = "Soft-delete employee by ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        employeeService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "List employees with pagination")
    @GetMapping
    public ResponseEntity<Page<EmployeeDTO>> list(@ParameterObject Pageable pageable) {
        return ResponseEntity.ok(employeeService.list(pageable));
    }

    @Operation(summary = "Filter employees by name, department, role")
    @GetMapping("/search")
    public ResponseEntity<Page<EmployeeDTO>> filter(
            @Parameter(description = "Name contains") @RequestParam(required = false) String name,
            @Parameter(description = "Department") @RequestParam(required = false) String department,
            @Parameter(description = "Role") @RequestParam(required = false) String role,
            @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(employeeService.filter(name, department, role, pageable));
    }
}
