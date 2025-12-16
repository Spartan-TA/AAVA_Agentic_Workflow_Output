package com.company.warehouse.employee.controller;

import com.company.warehouse.employee.entity.Employee;
import com.company.warehouse.employee.service.EmployeeService;
import com.company.warehouse.employee.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * REST controller for employee CRUD operations with RBAC.
 */
@RestController
@RequestMapping("/employees")
@RequiredArgsConstructor
public class EmployeeController {
    private final EmployeeService employeeService;

    /**
     * Create a new employee (ADMIN, HR only).
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public ResponseEntity<Employee> createEmployee(@Valid @RequestBody CreateEmployeeDto dto,
                                                   @RequestHeader("X-Tenant-Id") String tenantId) {
        Employee employee = employeeService.createEmployee(dto, tenantId);
        return ResponseEntity.ok(employee);
    }

    /**
     * Get paginated list of employees (ADMIN, HR, SUPERVISOR).
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','HR','SUPERVISOR')")
    public ResponseEntity<Page<Employee>> getEmployees(@RequestHeader("X-Tenant-Id") String tenantId,
                                                       Pageable pageable) {
        Page<Employee> employees = employeeService.getEmployees(tenantId, pageable);
        return ResponseEntity.ok(employees);
    }

    /**
     * Get employee by ID (ADMIN, HR, SUPERVISOR).
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','HR','SUPERVISOR')")
    public ResponseEntity<Employee> getEmployee(@PathVariable Long id) {
        Optional<Employee> employee = employeeService.getEmployee(id);
        return employee.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    /**
     * Update employee (ADMIN, HR only).
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public ResponseEntity<Employee> updateEmployee(@PathVariable Long id,
                                                   @Valid @RequestBody UpdateEmployeeDto dto) {
        Employee updated = employeeService.updateEmployee(id, dto);
        return ResponseEntity.ok(updated);
    }

    /**
     * Soft-delete employee (ADMIN only).
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }
}
