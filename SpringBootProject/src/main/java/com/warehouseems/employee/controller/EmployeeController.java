package com.warehouseems.employee.controller;

import com.warehouseems.employee.dto.EmployeeDto;
import com.warehouseems.employee.service.EmployeeService;
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
 * REST Controller for Employee CRUD operations and filtering.
 * Includes OpenAPI annotations and error handling.
 */
@RestController
@RequestMapping("/employees")
public class EmployeeController {
    @Autowired
    private EmployeeService employeeService;

    /**
     * Create a new employee.
     */
    @PostMapping
    public ResponseEntity<EmployeeDto> createEmployee(@Valid @RequestBody EmployeeDto dto) {
        EmployeeDto created = employeeService.createEmployee(dto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    /**
     * Get paginated list of employees.
     */
    @GetMapping
    public Page<EmployeeDto> getAllEmployees(@RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return employeeService.getAllEmployees(pageable);
    }

    /**
     * Get employee by badgeId.
     */
    @GetMapping("/badge/{badgeId}")
    public ResponseEntity<EmployeeDto> getByBadgeId(@PathVariable String badgeId) {
        Optional<EmployeeDto> dto = employeeService.getByBadgeId(badgeId);
        return dto.map(ResponseEntity::ok)
                  .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Update employee by ID.
     */
    @PutMapping("/{id}")
    public ResponseEntity<EmployeeDto> updateEmployee(@PathVariable Long id, @Valid @RequestBody EmployeeDto dto) {
        Optional<EmployeeDto> updated = employeeService.updateEmployee(id, dto);
        return updated.map(ResponseEntity::ok)
                      .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Soft-delete employee by ID.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        boolean deleted = employeeService.deleteEmployee(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    /**
     * Filter employees by department, role, and status.
     */
    @GetMapping("/filter")
    public Page<EmployeeDto> filterEmployees(@RequestParam(required = false) String department,
                                             @RequestParam(required = false) String role,
                                             @RequestParam(required = false) String status,
                                             @RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return employeeService.filterEmployees(department, role, status, pageable);
    }
}
