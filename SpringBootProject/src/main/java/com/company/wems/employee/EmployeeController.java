package com.company.wems.employee;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * REST controller for Employee operations.
 */
@RestController
@RequestMapping("/employees")
@Tag(name = "Employee", description = "Employee management APIs")
public class EmployeeController {
    @Autowired
    private EmployeeService employeeService;

    @Operation(summary = "Get all employees (paginated)")
    @GetMapping
    public Page<Employee> getAllEmployees(Pageable pageable) {
        return employeeService.getAllEmployees(pageable);
    }

    @Operation(summary = "Filter employees by role and department")
    @GetMapping("/filter")
    public List<Employee> filterEmployees(@RequestParam(required = false) String role,
                                          @RequestParam(required = false) String department) {
        return employeeService.filterEmployees(role, department);
    }

    @Operation(summary = "Get employee by badgeId")
    @GetMapping("/badge/{badgeId}")
    public ResponseEntity<Employee> getByBadgeId(@PathVariable String badgeId) {
        return employeeService.getByBadgeId(badgeId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Create new employee")
    @PostMapping
    public Employee createEmployee(@RequestBody Employee employee) {
        return employeeService.createEmployee(employee);
    }

    @Operation(summary = "Update employee details")
    @PutMapping("/{id}")
    public Employee updateEmployee(@PathVariable Long id, @RequestBody Employee employee) {
        return employeeService.updateEmployee(id, employee);
    }

    @Operation(summary = "Soft-delete employee")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> softDeleteEmployee(@PathVariable Long id) {
        employeeService.softDeleteEmployee(id);
        return ResponseEntity.noContent().build();
    }
}
