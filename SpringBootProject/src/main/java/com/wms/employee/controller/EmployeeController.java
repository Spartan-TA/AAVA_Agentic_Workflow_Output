package com.wms.employee.controller;

import com.wms.employee.dto.EmployeeDto;
import com.wms.employee.model.Employee;
import com.wms.employee.service.EmployeeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Api(tags = "Employee Management")
@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
@Validated
public class EmployeeController {
    private final EmployeeService employeeService;

    @ApiOperation("Get paginated and filtered list of employees")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','HR','SUPERVISOR','WORKER')")
    public ResponseEntity<Page<Employee>> getEmployees(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(employeeService.getEmployees(name, department, role, page, size));
    }

    @ApiOperation("Get employee by ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','HR','SUPERVISOR','WORKER')")
    public ResponseEntity<Employee> getEmployeeById(@PathVariable Long id) {
        return employeeService.getEmployeeById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @ApiOperation("Create new employee")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public ResponseEntity<Employee> createEmployee(@Valid @RequestBody EmployeeDto dto) {
        return ResponseEntity.ok(employeeService.createEmployee(dto));
    }

    @ApiOperation("Update employee")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public ResponseEntity<Employee> updateEmployee(@PathVariable Long id, @Valid @RequestBody EmployeeDto dto) {
        return ResponseEntity.ok(employeeService.updateEmployee(id, dto));
    }

    @ApiOperation("Soft delete employee")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        employeeService.softDeleteEmployee(id);
        return ResponseEntity.noContent().build();
    }
}
