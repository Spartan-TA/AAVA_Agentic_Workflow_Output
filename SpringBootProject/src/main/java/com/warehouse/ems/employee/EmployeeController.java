package com.warehouse.ems.employee;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;

/**
 * REST controller for Employee CRUD operations.
 */
@RestController
@RequestMapping("/employees")
@Tag(name = "Employee", description = "Employee management APIs")
public class EmployeeController {
    private final EmployeeService employeeService;

    @Autowired
    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @Operation(summary = "Create new employee")
    @PostMapping
    public ResponseEntity<Employee> createEmployee(@Validated @RequestBody Employee employee) {
        Employee created = employeeService.createEmployee(employee);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @Operation(summary = "Get employee by ID")
    @GetMapping("/{id}")
    public ResponseEntity<Employee> getEmployee(@PathVariable Long id) {
        Optional<Employee> employee = employeeService.getEmployee(id);
        return employee.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Update employee")
    @PutMapping("/{id}")
    public ResponseEntity<Employee> updateEmployee(@PathVariable Long id, @Validated @RequestBody Employee updated) {
        Employee employee = employeeService.updateEmployee(id, updated);
        return ResponseEntity.ok(employee);
    }

    @Operation(summary = "Patch employee status")
    @PatchMapping("/{id}/status")
    public ResponseEntity<Employee> patchStatus(@PathVariable Long id, @RequestParam String status) {
        Employee employee = employeeService.patchStatus(id, status);
        return ResponseEntity.ok(employee);
    }

    @Operation(summary = "Delete employee (soft-delete)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "List employees with pagination and filtering")
    @GetMapping
    public ResponseEntity<Page<Employee>> listEmployees(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Employee> employees = employeeService.listEmployees(name, department, role, pageable);
        return ResponseEntity.ok(employees);
    }
}
