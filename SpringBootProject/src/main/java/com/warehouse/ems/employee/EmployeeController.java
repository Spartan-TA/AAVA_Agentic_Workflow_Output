package com.warehouse.ems.employee;

import com.warehouse.ems.employee.dto.EmployeeCreateDto;
import com.warehouse.ems.employee.dto.EmployeeDto;
import com.warehouse.ems.employee.dto.EmployeeUpdateDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for Employee CRUD operations.
 */
@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
@Tag(name = "Employee", description = "Employee Master Data Management")
public class EmployeeController {
    private final EmployeeService employeeService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','HR','SUPERVISOR')")
    @Operation(summary = "List employees", description = "Paginated and filterable list of employees.")
    public Page<EmployeeDto> listEmployees(
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String role,
            @Parameter(hidden = true) @PageableDefault Pageable pageable) {
        return employeeService.getAllEmployees(department, role, pageable);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','HR','SUPERVISOR','WORKER')")
    @Operation(summary = "Get employee by ID")
    public EmployeeDto getEmployee(@PathVariable Long id) {
        return employeeService.getEmployee(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    @Operation(summary = "Create employee")
    public ResponseEntity<EmployeeDto> createEmployee(@Valid @RequestBody EmployeeCreateDto dto) {
        return new ResponseEntity<>(employeeService.createEmployee(dto), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    @Operation(summary = "Update employee")
    public EmployeeDto updateEmployee(@PathVariable Long id, @Valid @RequestBody EmployeeUpdateDto dto) {
        return employeeService.updateEmployee(id, dto);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    @Operation(summary = "Partially update employee")
    public EmployeeDto patchEmployee(@PathVariable Long id, @RequestBody EmployeeUpdateDto dto) {
        return employeeService.updateEmployee(id, dto);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete employee")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }
}
