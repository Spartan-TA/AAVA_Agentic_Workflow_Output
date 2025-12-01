package com.warehouse.ems.employee;

import io.swagger.v3.oas.annotations.Operation;
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
 */
@RestController
@RequestMapping("/employees")
@Tag(name = "Employee", description = "Employee Master Data APIs")
public class EmployeeController {
    private final EmployeeService employeeService;

    @Autowired
    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @Operation(summary = "Get all employees", description = "Returns paginated list of employees.")
    @GetMapping
    public ResponseEntity<Page<Employee>> getAllEmployees(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(employeeService.getAllEmployees(pageable));
    }

    @Operation(summary = "Get employee by badgeId")
    @GetMapping("/badge/{badgeId}")
    public ResponseEntity<Employee> getEmployeeByBadgeId(@PathVariable String badgeId) {
        Optional<Employee> employee = employeeService.getEmployeeByBadgeId(badgeId);
        return employee.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Create new employee")
    @PostMapping
    public ResponseEntity<Employee> createEmployee(@Valid @RequestBody Employee employee) {
        Employee created = employeeService.createEmployee(employee);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @Operation(summary = "Update employee")
    @PutMapping("/{id}")
    public ResponseEntity<Employee> updateEmployee(@PathVariable Long id, @Valid @RequestBody Employee employee) {
        Employee updated = employeeService.updateEmployee(id, employee);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Soft-delete employee")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> softDeleteEmployee(@PathVariable Long id) {
        employeeService.softDeleteEmployee(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Filter employees by role and department")
    @GetMapping("/filter")
    public ResponseEntity<Page<Employee>> filterEmployees(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String department,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(employeeService.filterEmployees(role, department, pageable));
    }
}
