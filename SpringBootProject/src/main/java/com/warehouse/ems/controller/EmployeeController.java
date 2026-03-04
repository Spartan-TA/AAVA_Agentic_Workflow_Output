package com.warehouse.ems.controller;

import com.warehouse.ems.dto.EmployeeDTO;
import com.warehouse.ems.model.Employee;
import com.warehouse.ems.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Optional;

/**
 * REST controller for Employee CRUD operations.
 */
@RestController
@RequestMapping("/employees")
@Validated
public class EmployeeController {

    private final EmployeeService employeeService;

    @Autowired
    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @Operation(summary = "Create employee", description = "Creates a new employee record.")
    @ApiResponse(responseCode = "201", description = "Employee created")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    @PostMapping
    public ResponseEntity<Employee> create(@Valid @RequestBody EmployeeDTO dto) {
        Employee employee = employeeService.createEmployee(dto);
        return ResponseEntity.status(201).body(employee);
    }

    @Operation(summary = "Get employee by badgeId", description = "Fetch employee by badgeId.")
    @ApiResponse(responseCode = "200", description = "Employee found")
    @PreAuthorize("hasAnyRole('ADMIN','HR','SUPERVISOR','WORKER')")
    @GetMapping("/{badgeId}")
    public ResponseEntity<Employee> getByBadgeId(@PathVariable String badgeId) {
        Optional<Employee> employee = employeeService.getByBadgeId(badgeId);
        return employee.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "List employees", description = "Paginated list of employees.")
    @ApiResponse(responseCode = "200", description = "Employees listed")
    @PreAuthorize("hasAnyRole('ADMIN','HR','SUPERVISOR')")
    @GetMapping
    public ResponseEntity<Page<Employee>> list(Pageable pageable) {
        return ResponseEntity.ok(employeeService.getAll(pageable));
    }

    @Operation(summary = "Update employee", description = "Updates employee record.")
    @ApiResponse(responseCode = "200", description = "Employee updated")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    @PutMapping("/{id}")
    public ResponseEntity<Employee> update(@PathVariable Long id, @Valid @RequestBody EmployeeDTO dto) {
        Optional<Employee> updated = employeeService.updateEmployee(id, dto);
        return updated.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Delete employee", description = "Soft-deletes employee record.")
    @ApiResponse(responseCode = "204", description = "Employee deleted")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        boolean deleted = employeeService.deleteEmployee(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
