package com.warehouse.ems.employee.controller;

import com.warehouse.ems.employee.dto.EmployeeRequestDTO;
import com.warehouse.ems.employee.dto.EmployeeResponseDTO;
import com.warehouse.ems.employee.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.List;

/**
 * REST controller for Employee CRUD operations.
 */
@RestController
@RequestMapping("/api/employees")
@Tag(name = "Employee", description = "Employee management endpoints")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    @Operation(summary = "Create a new employee")
    @PostMapping
    public ResponseEntity<EmployeeResponseDTO> createEmployee(@Valid @RequestBody EmployeeRequestDTO request) {
        return ResponseEntity.ok(employeeService.createEmployee(request));
    }

    @Operation(summary = "Update an existing employee")
    @PutMapping("/{id}")
    public ResponseEntity<EmployeeResponseDTO> updateEmployee(@PathVariable Long id,
                                                             @Valid @RequestBody EmployeeRequestDTO request) {
        return ResponseEntity.ok(employeeService.updateEmployee(id, request));
    }

    @Operation(summary = "Get employee by ID")
    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponseDTO> getEmployee(@PathVariable Long id) {
        return ResponseEntity.ok(employeeService.getEmployee(id));
    }

    @Operation(summary = "Get all employees")
    @GetMapping
    public ResponseEntity<List<EmployeeResponseDTO>> getAllEmployees() {
        return ResponseEntity.ok(employeeService.getAllEmployees());
    }

    @Operation(summary = "Delete employee by ID (soft delete)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }
}
