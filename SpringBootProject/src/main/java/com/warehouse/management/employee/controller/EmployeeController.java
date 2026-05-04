package com.warehouse.management.employee.controller;

import com.warehouse.management.employee.dto.EmployeeDTO;
import com.warehouse.management.employee.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.api.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * REST controller for Employee operations.
 */
@RestController
@RequestMapping("/api/employees")
@Tag(name = "Employee", description = "Employee management APIs")
@Validated
public class EmployeeController {
    private final EmployeeService employeeService;

    @Autowired
    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    /**
     * Create a new employee.
     * @param employeeDTO Employee data
     * @return Created EmployeeDTO
     */
    @Operation(summary = "Create employee", description = "Creates a new employee.")
    @PostMapping
    public ResponseEntity<EmployeeDTO> createEmployee(@Valid @RequestBody EmployeeDTO employeeDTO) {
        EmployeeDTO created = employeeService.createEmployee(employeeDTO);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    /**
     * Update an existing employee.
     * @param id Employee ID
     * @param employeeDTO Employee data
     * @return Updated EmployeeDTO
     */
    @Operation(summary = "Update employee", description = "Updates an existing employee.")
    @PutMapping("/{id}")
    public ResponseEntity<EmployeeDTO> updateEmployee(
            @Parameter(description = "Employee ID") @PathVariable Long id,
            @Valid @RequestBody EmployeeDTO employeeDTO) {
        EmployeeDTO updated = employeeService.updateEmployee(id, employeeDTO);
        return ResponseEntity.ok(updated);
    }

    /**
     * Get an employee by ID.
     * @param id Employee ID
     * @return EmployeeDTO
     */
    @Operation(summary = "Get employee", description = "Gets an employee by ID.")
    @GetMapping("/{id}")
    public ResponseEntity<EmployeeDTO> getEmployee(
            @Parameter(description = "Employee ID") @PathVariable Long id) {
        EmployeeDTO employee = employeeService.getEmployee(id);
        return ResponseEntity.ok(employee);
    }

    /**
     * Delete an employee by ID.
     * @param id Employee ID
     * @return No content
     */
    @Operation(summary = "Delete employee", description = "Deletes an employee by ID.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(
            @Parameter(description = "Employee ID") @PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get paginated list of employees by department.
     * @param department Department name
     * @param pageable Pageable
     * @return Page of EmployeeDTO
     */
    @Operation(summary = "List employees by department", description = "Gets paginated employees by department.")
    @GetMapping("/department/{department}")
    public ResponseEntity<Page<EmployeeDTO>> getEmployeesByDepartment(
            @Parameter(description = "Department name") @PathVariable String department,
            @ParameterObject Pageable pageable) {
        Page<EmployeeDTO> page = employeeService.getEmployeesByDepartment(department, pageable);
        return ResponseEntity.ok(page);
    }

    /**
     * Get paginated list of all employees.
     * @param pageable Pageable
     * @return Page of EmployeeDTO
     */
    @Operation(summary = "List all employees", description = "Gets paginated list of all employees.")
    @GetMapping
    public ResponseEntity<Page<EmployeeDTO>> getAllEmployees(@ParameterObject Pageable pageable) {
        Page<EmployeeDTO> page = employeeService.getAllEmployees(pageable);
        return ResponseEntity.ok(page);
    }
}
