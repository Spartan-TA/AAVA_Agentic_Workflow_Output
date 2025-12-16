package com.warehouse.management.employee.controller;

import com.warehouse.management.common.dto.ApiResponse;
import com.warehouse.management.common.dto.PageResponse;
import com.warehouse.management.employee.dto.EmployeeCreateRequest;
import com.warehouse.management.employee.dto.EmployeeDTO;
import com.warehouse.management.employee.dto.EmployeeUpdateRequest;
import com.warehouse.management.employee.entity.Employee;
import com.warehouse.management.employee.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse as OpenApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for Employee CRUD operations.
 */
@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
@Slf4j
public class EmployeeController {
    private final EmployeeService employeeService;

    @Operation(summary = "Create Employee", description = "Create a new employee.",
            responses = {
                    @OpenApiResponse(responseCode = "201", description = "Employee created successfully", content = @Content(schema = @Schema(implementation = EmployeeDTO.class))),
                    @OpenApiResponse(responseCode = "409", description = "Duplicate badgeId")
            })
    @PostMapping
    public ResponseEntity<ApiResponse<EmployeeDTO>> createEmployee(
            @Valid @RequestBody EmployeeCreateRequest request,
            Authentication authentication) {
        String createdBy = authentication != null ? authentication.getName() : "system";
        EmployeeDTO dto = employeeService.createEmployee(request, createdBy);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<EmployeeDTO>builder().success(true).message("Employee created").data(dto).build());
    }

    @Operation(summary = "Get Employees", description = "Get paginated and filtered list of employees.",
            parameters = {
                    @Parameter(name = "status", in = ParameterIn.QUERY, schema = @Schema(type = "string", allowableValues = {"ACTIVE", "INACTIVE"})),
                    @Parameter(name = "department", in = ParameterIn.QUERY, schema = @Schema(type = "string")),
                    @Parameter(name = "role", in = ParameterIn.QUERY, schema = @Schema(type = "string", allowableValues = {"ADMIN", "HR", "SUPERVISOR", "WORKER"})),
                    @Parameter(name = "page", in = ParameterIn.QUERY, schema = @Schema(type = "integer", defaultValue = "0")),
                    @Parameter(name = "size", in = ParameterIn.QUERY, schema = @Schema(type = "integer", defaultValue = "10"))
            },
            responses = {
                    @OpenApiResponse(responseCode = "200", description = "Employees fetched", content = @Content(schema = @Schema(implementation = PageResponse.class)))
            })
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<EmployeeDTO>>> getEmployees(
            @RequestParam(value = "status", required = false) Employee.Status status,
            @RequestParam(value = "department", required = false) String department,
            @RequestParam(value = "role", required = false) Employee.Role role,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        PageResponse<EmployeeDTO> employees = employeeService.getAllEmployees(status, department, role, page, size);
        return ResponseEntity.ok(ApiResponse.<PageResponse<EmployeeDTO>>builder().success(true).message("Employees fetched").data(employees).build());
    }

    @Operation(summary = "Get Employee by ID", description = "Get employee details by ID.",
            responses = {
                    @OpenApiResponse(responseCode = "200", description = "Employee found", content = @Content(schema = @Schema(implementation = EmployeeDTO.class))),
                    @OpenApiResponse(responseCode = "404", description = "Employee not found")
            })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EmployeeDTO>> getEmployeeById(@PathVariable Long id) {
        EmployeeDTO dto = employeeService.getEmployeeById(id);
        return ResponseEntity.ok(ApiResponse.<EmployeeDTO>builder().success(true).message("Employee found").data(dto).build());
    }

    @Operation(summary = "Update Employee", description = "Update an employee by ID.",
            responses = {
                    @OpenApiResponse(responseCode = "200", description = "Employee updated", content = @Content(schema = @Schema(implementation = EmployeeDTO.class))),
                    @OpenApiResponse(responseCode = "404", description = "Employee not found"),
                    @OpenApiResponse(responseCode = "409", description = "Duplicate badgeId")
            })
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EmployeeDTO>> updateEmployee(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeUpdateRequest request,
            Authentication authentication) {
        String updatedBy = authentication != null ? authentication.getName() : "system";
        EmployeeDTO dto = employeeService.updateEmployee(id, request, updatedBy);
        return ResponseEntity.ok(ApiResponse.<EmployeeDTO>builder().success(true).message("Employee updated").data(dto).build());
    }

    @Operation(summary = "Partial Update Employee", description = "Partially update an employee by ID.",
            responses = {
                    @OpenApiResponse(responseCode = "200", description = "Employee updated", content = @Content(schema = @Schema(implementation = EmployeeDTO.class))),
                    @OpenApiResponse(responseCode = "404", description = "Employee not found"),
                    @OpenApiResponse(responseCode = "409", description = "Duplicate badgeId")
            })
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<EmployeeDTO>> partialUpdateEmployee(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeUpdateRequest request,
            Authentication authentication) {
        String updatedBy = authentication != null ? authentication.getName() : "system";
        EmployeeDTO dto = employeeService.updateEmployee(id, request, updatedBy);
        return ResponseEntity.ok(ApiResponse.<EmployeeDTO>builder().success(true).message("Employee updated").data(dto).build());
    }

    @Operation(summary = "Soft Delete Employee", description = "Soft delete an employee by ID.",
            responses = {
                    @OpenApiResponse(responseCode = "204", description = "Employee soft deleted"),
                    @OpenApiResponse(responseCode = "404", description = "Employee not found")
            })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> softDeleteEmployee(@PathVariable Long id, Authentication authentication) {
        String updatedBy = authentication != null ? authentication.getName() : "system";
        employeeService.softDeleteEmployee(id, updatedBy);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Restore Employee", description = "Restore a soft-deleted employee by ID.",
            responses = {
                    @OpenApiResponse(responseCode = "204", description = "Employee restored"),
                    @OpenApiResponse(responseCode = "404", description = "Employee not found")
            })
    @PatchMapping("/{id}/restore")
    public ResponseEntity<Void> restoreEmployee(@PathVariable Long id, Authentication authentication) {
        String updatedBy = authentication != null ? authentication.getName() : "system";
        employeeService.restoreEmployee(id, updatedBy);
        return ResponseEntity.noContent().build();
    }
}
