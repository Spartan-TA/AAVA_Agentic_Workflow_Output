package com.wms.ems.employee.controller;

import com.wms.ems.employee.dto.EmployeeDto;
import com.wms.ems.employee.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for Employee CRUD operations.
 */
@RestController
@RequestMapping("/employees")
@Tag(name = "Employee", description = "Employee CRUD APIs")
public class EmployeeController {
    private final EmployeeService employeeService;

    @Autowired
    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @Operation(summary = "Create new employee", description = "Creates a new employee record.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Employee created"),
        @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PostMapping
    public ResponseEntity<EmployeeDto> createEmployee(@RequestBody EmployeeDto dto) {
        EmployeeDto created = employeeService.createEmployee(dto);
        return ResponseEntity.status(201).body(created);
    }

    @Operation(summary = "Get employee by badgeId", description = "Fetches employee by badgeId.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Employee found"),
        @ApiResponse(responseCode = "404", description = "Employee not found")
    })
    @GetMapping("/{badgeId}")
    public ResponseEntity<EmployeeDto> getEmployee(@PathVariable String badgeId) {
        EmployeeDto dto = employeeService.getEmployeeByBadgeId(badgeId);
        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "Get all employees", description = "Fetches all employees.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "List of employees")
    })
    @GetMapping
    public ResponseEntity<List<EmployeeDto>> getAllEmployees() {
        List<EmployeeDto> employees = employeeService.getAllEmployees();
        return ResponseEntity.ok(employees);
    }

    @Operation(summary = "Update employee", description = "Updates employee by badgeId.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Employee updated"),
        @ApiResponse(responseCode = "404", description = "Employee not found")
    })
    @PutMapping("/{badgeId}")
    public ResponseEntity<EmployeeDto> updateEmployee(@PathVariable String badgeId, @RequestBody EmployeeDto dto) {
        EmployeeDto updated = employeeService.updateEmployee(badgeId, dto);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Soft delete employee", description = "Soft deletes employee by badgeId.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Employee deleted"),
        @ApiResponse(responseCode = "404", description = "Employee not found")
    })
    @DeleteMapping("/{badgeId}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable String badgeId) {
        employeeService.softDeleteEmployee(badgeId);
        return ResponseEntity.noContent().build();
    }
}
