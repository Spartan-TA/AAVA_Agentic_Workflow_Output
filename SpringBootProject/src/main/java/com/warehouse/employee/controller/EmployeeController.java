package com.warehouse.employee.controller;

import com.warehouse.common.dto.EmployeeCreateDto;
import com.warehouse.common.dto.EmployeeDto;
import com.warehouse.common.dto.EmployeeUpdateDto;
import com.warehouse.employee.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for employee CRUD operations.
 */
@RestController
@RequestMapping("/employees")
@RequiredArgsConstructor
public class EmployeeController {
    private final EmployeeService employeeService;

    /**
     * Get paginated list of employees.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','HR','SUPERVISOR')")
    public Page<EmployeeDto> getAll(@ParameterObject Pageable pageable) {
        return employeeService.getAll(pageable);
    }

    /**
     * Get employee by ID.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','HR','SUPERVISOR','WORKER')")
    public EmployeeDto getById(@PathVariable Long id) {
        return employeeService.getById(id);
    }

    /**
     * Create a new employee.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public ResponseEntity<EmployeeDto> create(@Valid @RequestBody EmployeeCreateDto dto) {
        return new ResponseEntity<>(employeeService.create(dto), HttpStatus.CREATED);
    }

    /**
     * Update employee details.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public EmployeeDto update(@PathVariable Long id, @Valid @RequestBody EmployeeUpdateDto dto) {
        return employeeService.update(id, dto);
    }

    /**
     * Soft-delete an employee.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        employeeService.softDelete(id);
        return ResponseEntity.noContent().build();
    }
}
