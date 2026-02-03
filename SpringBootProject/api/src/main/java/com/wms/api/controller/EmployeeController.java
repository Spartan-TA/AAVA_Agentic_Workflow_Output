package com.wms.api.controller;

import com.wms.api.dto.EmployeeDto;
import com.wms.api.mapper.EmployeeMapper;
import com.wms.core.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
@Tag(name = "Employee Management", description = "APIs for managing warehouse employees")
public class EmployeeController {
    private final EmployeeService employeeService;
    private final EmployeeMapper employeeMapper;

    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @PostMapping
    @Operation(summary = "Create new employee", description = "Creates a new employee record")
    public ResponseEntity<EmployeeDto> create(@Valid @RequestBody EmployeeDto dto) {
        var employee = employeeService.create(employeeMapper.toEntity(dto));
        return ResponseEntity.status(HttpStatus.CREATED).body(employeeMapper.toDto(employee));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @PutMapping("/{id}")
    @Operation(summary = "Update employee", description = "Updates an existing employee record")
    public ResponseEntity<EmployeeDto> update(@PathVariable Long id, @Valid @RequestBody EmployeeDto dto) {
        var employee = employeeService.update(id, employeeMapper.toEntity(dto));
        return ResponseEntity.ok(employeeMapper.toDto(employee));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete employee", description = "Soft deletes an employee record")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        employeeService.softDelete(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'SUPERVISOR')")
    @GetMapping("/{id}")
    @Operation(summary = "Get employee by ID", description = "Retrieves an employee by their ID")
    public ResponseEntity<EmployeeDto> get(@PathVariable Long id) {
        return employeeService.findById(id)
            .map(employeeMapper::toDto)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'SUPERVISOR')")
    @GetMapping
    @Operation(summary = "List employees", description = "Retrieves a paginated list of employees with optional filtering")
    public ResponseEntity<Page<EmployeeDto>> list(
            @RequestParam(required = false) String filter, 
            Pageable pageable) {
        var page = employeeService.findAll(filter, pageable).map(employeeMapper::toDto);
        return ResponseEntity.ok(page);
    }
}