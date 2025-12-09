package com.warehouse.ems.controller;

import com.warehouse.ems.dto.EmployeeRequestDto;
import com.warehouse.ems.dto.EmployeeResponseDto;
import com.warehouse.ems.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * REST controller for Employee CRUD operations.
 * Epics: E02 (Employee Master Data CRUD)
 */
@RestController
@RequestMapping("/employees")
@Tag(name = "Employee", description = "Employee Master Data APIs")
public class EmployeeController {
    @Autowired
    private EmployeeService employeeService;

    @Operation(summary = "Create new employee", description = "Creates a new employee record. Badge ID must be unique.")
    @PostMapping
    public ResponseEntity<EmployeeResponseDto> createEmployee(@Valid @RequestBody EmployeeRequestDto dto) {
        EmployeeResponseDto created = employeeService.createEmployee(dto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @Operation(summary = "Get paginated list of employees", description = "Returns paginated, filtered list of employees.")
    @GetMapping
    public ResponseEntity<Page<EmployeeResponseDto>> getEmployees(
            @Parameter(description = "Filter by name") @RequestParam(required = false) String name,
            @Parameter(description = "Filter by department") @RequestParam(required = false) String department,
            @Parameter(description = "Filter by role") @RequestParam(required = false) String role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<EmployeeResponseDto> result = employeeService.getEmployees(name, department, role, pageable);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Get employee by ID", description = "Returns employee details by ID.")
    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponseDto> getEmployee(@PathVariable Long id) {
        Optional<EmployeeResponseDto> employee = employeeService.getEmployee(id);
        return employee.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Update employee", description = "Updates employee details by ID.")
    @PutMapping("/{id}")
    public ResponseEntity<EmployeeResponseDto> updateEmployee(@PathVariable Long id, @Valid @RequestBody EmployeeRequestDto dto) {
        Optional<EmployeeResponseDto> updated = employeeService.updateEmployee(id, dto);
        return updated.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Soft-delete employee", description = "Soft-deletes employee by ID.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        boolean deleted = employeeService.deleteEmployee(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
