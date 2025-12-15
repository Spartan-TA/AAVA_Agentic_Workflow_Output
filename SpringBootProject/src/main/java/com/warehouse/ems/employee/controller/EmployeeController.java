package com.warehouse.ems.employee.controller;

import com.warehouse.ems.employee.dto.EmployeeRequestDTO;
import com.warehouse.ems.employee.dto.EmployeeResponseDTO;
import com.warehouse.ems.employee.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for Employee Master Data CRUD operations.
 */
@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {
    private final EmployeeService employeeService;

    /**
     * Create a new employee.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public ResponseEntity<EmployeeResponseDTO> createEmployee(@Valid @RequestBody EmployeeRequestDTO dto) {
        EmployeeResponseDTO response = employeeService.createEmployee(dto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Get employee by ID.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','HR','SUPERVISOR')")
    public ResponseEntity<EmployeeResponseDTO> getEmployee(@PathVariable Long id) {
        EmployeeResponseDTO response = employeeService.getEmployee(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Update employee.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public ResponseEntity<EmployeeResponseDTO> updateEmployee(@PathVariable Long id, @Valid @RequestBody EmployeeRequestDTO dto) {
        EmployeeResponseDTO response = employeeService.updateEmployee(id, dto);
        return ResponseEntity.ok(response);
    }

    /**
     * Soft-delete employee.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * List employees with pagination and filtering.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','HR','SUPERVISOR')")
    public ResponseEntity<Page<EmployeeResponseDTO>> listEmployees(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<EmployeeResponseDTO> employees = employeeService.listEmployees(name, department, role, pageable);
        return ResponseEntity.ok(employees);
    }
}
