package com.warehouse.employee.management.employee.controller;

import com.warehouse.employee.management.employee.dto.EmployeeRequestDTO;
import com.warehouse.employee.management.employee.dto.EmployeeResponseDTO;
import com.warehouse.employee.management.employee.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for employee management.
 */
@RestController
@RequestMapping("/employees")
@RequiredArgsConstructor
@Tag(name = "Employee", description = "Employee CRUD and management APIs")
public class EmployeeController {
    private final EmployeeService employeeService;

    @Operation(summary = "Create employee")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    @PostMapping
    public ResponseEntity<EmployeeResponseDTO> createEmployee(@Valid @RequestBody EmployeeRequestDTO dto) {
        return new ResponseEntity<>(employeeService.createEmployee(dto), HttpStatus.CREATED);
    }

    @Operation(summary = "Update employee")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    @PutMapping("/{id}")
    public EmployeeResponseDTO updateEmployee(@PathVariable Long id, @Valid @RequestBody EmployeeRequestDTO dto) {
        return employeeService.updateEmployee(id, dto);
    }

    @Operation(summary = "Patch employee")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    @PatchMapping("/{id}")
    public EmployeeResponseDTO patchEmployee(@PathVariable Long id, @RequestBody EmployeeRequestDTO dto) {
        return employeeService.patchEmployee(id, dto);
    }

    @Operation(summary = "Delete employee (soft-delete)")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get employee by ID")
    @PreAuthorize("hasAnyRole('ADMIN','HR','SUPERVISOR','WORKER')")
    @GetMapping("/{id}")
    public EmployeeResponseDTO getEmployee(@PathVariable Long id) {
        return employeeService.getEmployee(id);
    }

    @Operation(summary = "List employees with pagination and filtering")
    @PreAuthorize("hasAnyRole('ADMIN','HR','SUPERVISOR')")
    @GetMapping
    public Page<EmployeeResponseDTO> getAllEmployees(
            @Parameter(description = "Department filter") @RequestParam(required = false) String department,
            @PageableDefault(size = 20) Pageable pageable) {
        return employeeService.getAllEmployees(department, pageable);
    }
}
