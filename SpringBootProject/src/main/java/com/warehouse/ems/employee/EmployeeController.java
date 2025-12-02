package com.warehouse.ems.employee;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    @PostMapping
    public Employee createEmployee(@Valid @RequestBody Employee employee, @RequestHeader("X-User-Id") Long actorId) {
        return employeeService.createEmployee(employee, actorId);
    }

    @PreAuthorize("hasAnyRole('ADMIN','HR','SUPERVISOR')")
    @GetMapping
    public List<Employee> getAllEmployees() {
        return employeeService.getAllEmployees();
    }

    @PreAuthorize("hasAnyRole('ADMIN','HR','SUPERVISOR')")
    @GetMapping("/{id}")
    public Employee getEmployee(@PathVariable Long id) {
        return employeeService.getEmployee(id);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    @PutMapping("/{id}")
    public Employee updateEmployee(@PathVariable Long id, @Valid @RequestBody Employee employee, @RequestHeader("X-User-Id") Long actorId) {
        return employeeService.updateEmployee(id, employee, actorId);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public void deleteEmployee(@PathVariable Long id, @RequestHeader("X-User-Id") Long actorId) {
        employeeService.deleteEmployee(id, actorId);
    }
}