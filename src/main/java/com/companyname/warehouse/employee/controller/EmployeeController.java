package com.companyname.warehouse.employee.controller;

import com.companyname.warehouse.employee.dto.EmployeeRequestDTO;
import com.companyname.warehouse.employee.dto.EmployeeResponseDTO;
import com.companyname.warehouse.employee.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/employees")
@RequiredArgsConstructor
@Tag(name = "Employee", description = "Employee management APIs")
public class EmployeeController {
    private final EmployeeService employeeService;

    @PostMapping
    @Operation(summary = "Create employee")
    public EmployeeResponseDTO createEmployee(@Valid @RequestBody EmployeeRequestDTO dto) {
        return employeeService.createEmployee(dto);
    }

    @GetMapping
    @Operation(summary = "List employees (paginated)")
    public Page<EmployeeResponseDTO> listEmployees(Pageable pageable, @RequestParam(required = false) String filter) {
        return employeeService.listEmployees(pageable, filter);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get employee by ID")
    public EmployeeResponseDTO getEmployee(@PathVariable Long id) {
        return employeeService.listEmployees(Pageable.unpaged(), null)
                .stream()
                .filter(e -> e.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Employee not found"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update employee")
    public EmployeeResponseDTO updateEmployee(@PathVariable Long id, @Valid @RequestBody EmployeeRequestDTO dto) {
        return employeeService.updateEmployee(id, dto);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft delete employee")
    public void deleteEmployee(@PathVariable Long id) {
        employeeService.softDeleteEmployee(id);
    }
}
