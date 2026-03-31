package com.wms.ems.employee.controller;

import com.wms.ems.employee.dto.EmployeeRequestDTO;
import com.wms.ems.employee.dto.EmployeeResponseDTO;
import com.wms.ems.employee.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/employees")
@RequiredArgsConstructor
@Tag(name = "Employee", description = "Employee management APIs")
public class EmployeeController {
    private final EmployeeService employeeService;

    @Operation(summary = "Get all employees with pagination and filtering")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'SUPERVISOR')")
    public Page<EmployeeResponseDTO> getAllEmployees(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String department) {
        return employeeService.getAllEmployees(page, size, department);
    }

    @Operation(summary = "Get employee by ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'SUPERVISOR', 'WORKER')")
    public EmployeeResponseDTO getEmployeeById(@PathVariable Long id) {
        return employeeService.getEmployeeById(id);
    }

    @Operation(summary = "Create a new employee")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<EmployeeResponseDTO> createEmployee(@Valid @RequestBody EmployeeRequestDTO dto) {
        return new ResponseEntity<>(employeeService.createEmployee(dto), HttpStatus.CREATED);
    }

    @Operation(summary = "Update an employee (full)")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public EmployeeResponseDTO updateEmployee(@PathVariable Long id, @Valid @RequestBody EmployeeRequestDTO dto) {
        return employeeService.updateEmployee(id, dto);
    }

    @Operation(summary = "Patch an employee (partial)")
    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public EmployeeResponseDTO patchEmployee(@PathVariable Long id, @RequestBody EmployeeRequestDTO dto) {
        return employeeService.patchEmployee(id, dto);
    }

    @Operation(summary = "Soft-delete an employee")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }
}
