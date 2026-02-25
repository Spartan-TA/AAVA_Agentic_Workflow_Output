package com.warehouse.employee.management.presentation.controller;

import com.warehouse.employee.management.application.dto.CreateEmployeeRequest;
import com.warehouse.employee.management.application.dto.EmployeeResponse;
import com.warehouse.employee.management.application.dto.UpdateEmployeeRequest;
import com.warehouse.employee.management.application.mapper.EmployeeMapper;
import com.warehouse.employee.management.application.service.EmployeeService;
import com.warehouse.employee.management.domain.employee.Employee;
import com.warehouse.employee.management.infrastructure.repository.EmployeeSpecifications;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/employees")
@RequiredArgsConstructor
@Tag(name = "Employee", description = "Employee management endpoints")
public class EmployeeController {
    private final EmployeeService employeeService;
    private final EmployeeMapper employeeMapper;

    @Operation(summary = "Create a new employee", description = "Creates a new employee record")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public ResponseEntity<EmployeeResponse> createEmployee(@Valid @RequestBody CreateEmployeeRequest request) {
        Employee employee = employeeMapper.toEntity(request);
        Employee saved = employeeService.createEmployee(employee);
        return ResponseEntity.ok(employeeMapper.toResponse(saved));
    }

    @Operation(summary = "Get employee by ID", description = "Fetches employee details by UUID")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','HR','SUPERVISOR')")
    public ResponseEntity<EmployeeResponse> getEmployee(@PathVariable UUID id) {
        Employee employee = employeeService.getEmployeeById(id)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));
        return ResponseEntity.ok(employeeMapper.toResponse(employee));
    }

    @Operation(summary = "Update employee", description = "Updates employee details")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public ResponseEntity<EmployeeResponse> updateEmployee(@PathVariable UUID id, @Valid @RequestBody UpdateEmployeeRequest request) {
        Employee updated = employeeMapper.toEntity(request);
        Employee saved = employeeService.updateEmployee(id, updated);
        return ResponseEntity.ok(employeeMapper.toResponse(saved));
    }

    @Operation(summary = "Patch employee", description = "Partially updates employee details")
    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public ResponseEntity<EmployeeResponse> patchEmployee(@PathVariable UUID id, @RequestBody UpdateEmployeeRequest request) {
        Employee patch = employeeMapper.toEntity(request);
        Employee saved = employeeService.patchEmployee(id, patch);
        return ResponseEntity.ok(employeeMapper.toResponse(saved));
    }

    @Operation(summary = "Soft delete employee", description = "Marks employee as deleted")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public ResponseEntity<Void> softDeleteEmployee(@PathVariable UUID id) {
        employeeService.softDeleteEmployee(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Restore employee", description = "Restores a soft-deleted employee")
    @PostMapping("/{id}/restore")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public ResponseEntity<Void> restoreEmployee(@PathVariable UUID id) {
        employeeService.restoreEmployee(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Search employees", description = "Search employees with filters")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','HR','SUPERVISOR')")
    public ResponseEntity<List<EmployeeResponse>> searchEmployees(
            @Parameter(description = "Status filter") @RequestParam(required = false) String status,
            @Parameter(description = "Department filter") @RequestParam(required = false) UUID departmentId,
            @Parameter(description = "Position filter") @RequestParam(required = false) UUID positionId,
            @Parameter(description = "Supervisor filter") @RequestParam(required = false) UUID supervisorId,
            @Parameter(description = "Tenant filter") @RequestParam(required = false) String tenantId
    ) {
        Specification<Employee> spec = EmployeeSpecifications.isNotDeleted();
        if (status != null) {
            spec = spec.and(EmployeeSpecifications.hasStatus(com.warehouse.employee.management.domain.employee.EmployeeStatus.valueOf(status)));
        }
        if (departmentId != null) {
            spec = spec.and(EmployeeSpecifications.hasDepartment(departmentId));
        }
        if (positionId != null) {
            spec = spec.and(EmployeeSpecifications.hasPosition(positionId));
        }
        if (supervisorId != null) {
            spec = spec.and(EmployeeSpecifications.hasSupervisor(supervisorId));
        }
        if (tenantId != null) {
            spec = spec.and(EmployeeSpecifications.hasTenant(tenantId));
        }
        List<Employee> employees = employeeService.searchEmployees(spec);
        List<EmployeeResponse> responses = employees.stream().map(employeeMapper::toResponse).toList();
        return ResponseEntity.ok(responses);
    }
}
