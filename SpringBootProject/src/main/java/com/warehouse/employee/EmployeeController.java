package com.warehouse.employee;

import com.warehouse.dto.EmployeeDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

/**
 * REST controller for Employee CRUD operations.
 */
@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
@Tag(name = "Employee", description = "Employee Master Data APIs")
public class EmployeeController {
    private final EmployeeService employeeService;

    @Operation(summary = "Create a new employee")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public ResponseEntity<EmployeeDTO> createEmployee(@Valid @RequestBody EmployeeDTO dto) {
        var employee = employeeService.createEmployee(dto);
        return new ResponseEntity<>(toDto(employee), HttpStatus.CREATED);
    }

    @Operation(summary = "Get employee by ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','HR','SUPERVISOR')")
    public ResponseEntity<EmployeeDTO> getEmployee(@PathVariable Long id) {
        var employee = employeeService.getEmployee(id);
        return ResponseEntity.ok(toDto(employee));
    }

    @Operation(summary = "Update employee by ID")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public ResponseEntity<EmployeeDTO> updateEmployee(@PathVariable Long id, @Valid @RequestBody EmployeeDTO dto) {
        var employee = employeeService.updateEmployee(id, dto);
        return ResponseEntity.ok(toDto(employee));
    }

    @Operation(summary = "Soft-delete employee by ID")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "List employees with filtering and pagination")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','HR','SUPERVISOR')")
    public ResponseEntity<Page<EmployeeDTO>> listEmployees(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        var employees = employeeService.listEmployees(name, department, role, pageable)
                .map(this::toDto);
        return ResponseEntity.ok(employees);
    }

    // Helper method to map entity to DTO
    private EmployeeDTO toDto(Employee employee) {
        EmployeeDTO dto = new EmployeeDTO();
        dto.setName(employee.getName());
        dto.setBadgeId(employee.getBadgeId());
        dto.setRole(employee.getRole());
        dto.setDepartment(employee.getDepartment());
        dto.setShiftGroup(employee.getShiftGroup());
        dto.setHireDate(employee.getHireDate());
        dto.setStatus(employee.getStatus());
        return dto;
    }
}
