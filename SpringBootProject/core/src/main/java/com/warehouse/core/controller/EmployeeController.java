package com.warehouse.core.controller;

import com.warehouse.core.domain.Employee;
import com.warehouse.core.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * REST Controller for Employee CRUD operations.
 */
@RestController
@RequestMapping("/employees")
@Tag(name = "Employee", description = "Employee Master Data APIs")
public class EmployeeController {
    @Autowired
    private EmployeeService employeeService;

    @Operation(summary = "Get all employees with pagination")
    @GetMapping
    public Page<Employee> getAllEmployees(Pageable pageable) {
        return employeeService.getAllEmployees(pageable);
    }

    @Operation(summary = "Get employee by ID")
    @GetMapping("/{id}")
    public ResponseEntity<Employee> getEmployeeById(@PathVariable Long id) {
        Optional<Employee> employee = employeeService.getEmployeeById(id);
        return employee.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Create a new employee")
    @PostMapping
    public ResponseEntity<Employee> createEmployee(@RequestBody Employee employee) {
        Employee created = employeeService.createEmployee(employee);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @Operation(summary = "Update an existing employee")
    @PutMapping("/{id}")
    public ResponseEntity<Employee> updateEmployee(@PathVariable Long id, @RequestBody Employee employee) {
        Employee updated = employeeService.updateEmployee(id, employee);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Soft delete an employee")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> softDeleteEmployee(@PathVariable Long id) {
        employeeService.softDeleteEmployee(id);
        return ResponseEntity.noContent().build();
    }
}
