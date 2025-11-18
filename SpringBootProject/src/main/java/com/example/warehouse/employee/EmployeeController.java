package com.example.warehouse.employee;

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

    @Operation(summary = "Get paginated list of employees")
    @GetMapping
    public Page<Employee> getAllEmployees(Pageable pageable) {
        return employeeService.getAllEmployees(pageable);
    }

    @Operation(summary = "Get employee by badgeId")
    @GetMapping("/{badgeId}")
    public ResponseEntity<Employee> getEmployee(@PathVariable String badgeId) {
        Optional<Employee> employee = employeeService.getEmployeeByBadgeId(badgeId);
        return employee.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Create new employee")
    @PostMapping
    public ResponseEntity<Employee> createEmployee(@RequestBody Employee employee) {
        Employee created = employeeService.createEmployee(employee);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @Operation(summary = "Update employee")
    @PutMapping("/{id}")
    public ResponseEntity<Employee> updateEmployee(@PathVariable Long id, @RequestBody Employee updated) {
        Employee employee = employeeService.updateEmployee(id, updated);
        return ResponseEntity.ok(employee);
    }

    @Operation(summary = "Soft delete employee")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        employeeService.softDeleteEmployee(id);
        return ResponseEntity.noContent().build();
    }
}
