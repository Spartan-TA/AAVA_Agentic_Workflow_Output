package com.company.wms.employee.controller;

import com.company.wms.employee.dto.CreateEmployeeRequest;
import com.company.wms.employee.dto.EmployeeDTO;
import com.company.wms.employee.dto.UpdateEmployeeRequest;
import com.company.wms.employee.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.util.List;

/**
 * REST Controller for Employee endpoints.
 */
@RestController
@RequestMapping("/api/employees")
@Validated
public class EmployeeController {
    private final EmployeeService employeeService;

    @Autowired
    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    /**
     * Create a new employee.
     */
    @PostMapping
    public ResponseEntity<EmployeeDTO> createEmployee(@Valid @RequestBody CreateEmployeeRequest request) {
        EmployeeDTO employee = employeeService.createEmployee(request);
        return new ResponseEntity<>(employee, HttpStatus.CREATED);
    }

    /**
     * Update an existing employee.
     */
    @PutMapping("/{id}")
    public ResponseEntity<EmployeeDTO> updateEmployee(
            @PathVariable("id") @Min(1) Long id,
            @Valid @RequestBody UpdateEmployeeRequest request) {
        EmployeeDTO employee = employeeService.updateEmployee(id, request);
        return ResponseEntity.ok(employee);
    }

    /**
     * Soft-delete an employee.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable("id") @Min(1) Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get employee by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<EmployeeDTO> getEmployee(@PathVariable("id") @Min(1) Long id) {
        EmployeeDTO employee = employeeService.getEmployee(id);
        return ResponseEntity.ok(employee);
    }

    /**
     * List employees with pagination and optional filtering.
     */
    @GetMapping
    public ResponseEntity<Page<EmployeeDTO>> listEmployees(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "department", required = false) String department,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestParam(value = "sortBy", defaultValue = "id") String sortBy) {
        Page<EmployeeDTO> employees = employeeService.listEmployees(name, department, page, size, sortBy);
        return ResponseEntity.ok(employees);
    }

    /**
     * List all active employees.
     */
    @GetMapping("/active")
    public ResponseEntity<List<EmployeeDTO>> listAllActiveEmployees() {
        List<EmployeeDTO> employees = employeeService.listAllActiveEmployees();
        return ResponseEntity.ok(employees);
    }
}