package com.warehouse.employee.controller;

import com.warehouse.employee.domain.Employee;
import com.warehouse.employee.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * REST controller for Employee CRUD operations.
 */
@RestController
@RequestMapping("/employees")
@Tag(name = "Employee", description = "Employee management APIs")
public class EmployeeController {
    @Autowired
    private EmployeeService employeeService;

    @Operation(summary = "Get all employees", description = "Returns paginated list of employees.")
    @GetMapping
    public Page<Employee> getAllEmployees(@RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "10") int size) {
        return employeeService.getAllEmployees(PageRequest.of(page, size));
    }

    @Operation(summary = "Filter employees", description = "Filter by name and department.")
    @GetMapping("/filter")
    public Page<Employee> filterEmployees(@RequestParam(required = false) String name,
                                          @RequestParam(required = false) String department,
                                          @RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "10") int size) {
        return employeeService.filterEmployees(name, department, PageRequest.of(page, size));
    }

    @Operation(summary = "Get employee by badgeId")
    @GetMapping("/{badgeId}")
    public ResponseEntity<Employee> getEmployee(@PathVariable String badgeId) {
        Optional<Employee> employee = employeeService.getEmployeeByBadgeId(badgeId);
        return employee.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Create employee")
    @PostMapping
    public ResponseEntity<Employee> createEmployee(@RequestBody Employee employee) {
        Employee created = employeeService.createEmployee(employee);
        return ResponseEntity.ok(created);
    }

    @Operation(summary = "Update employee")
    @PutMapping("/{id}")
    public ResponseEntity<Employee> updateEmployee(@PathVariable Long id, @RequestBody Employee employee) {
        Employee updated = employeeService.updateEmployee(id, employee);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Soft-delete employee")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        employeeService.softDeleteEmployee(id);
        return ResponseEntity.noContent().build();
    }
}
