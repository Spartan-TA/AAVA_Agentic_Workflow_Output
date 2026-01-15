package com.company.wms.employee.controller;

import com.company.wms.employee.domain.Employee;
import com.company.wms.employee.dto.EmployeeDTO;
import com.company.wms.employee.dto.CreateEmployeeRequest;
import com.company.wms.employee.dto.UpdateEmployeeRequest;
import com.company.wms.employee.service.EmployeeService;
import com.company.wms.common.dto.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

/**
 * REST controller for Employee operations.
 */
@RestController
@RequestMapping("/api/employees")
public class EmployeeController {
    private final EmployeeService employeeService;

    @Autowired
    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    /**
     * Create a new employee.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<EmployeeDTO>> create(@Valid @RequestBody CreateEmployeeRequest request) {
        Employee employee = new Employee();
        employee.setName(request.getName());
        employee.setBadgeId(request.getBadgeId());
        employee.setRole(request.getRole());
        employee.setDepartment(request.getDepartment());
        employee.setShiftGroup(request.getShiftGroup());
        employee.setHireDate(request.getHireDate());
        employee.setStatus(request.getStatus());
        Employee saved = employeeService.create(employee);
        return ResponseEntity.ok(ApiResponse.success(EmployeeDTO.from(saved)));
    }

    /**
     * Get employee by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EmployeeDTO>> getById(@PathVariable Long id) {
        Employee employee = employeeService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(EmployeeDTO.from(employee)));
    }

    /**
     * Update employee.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EmployeeDTO>> update(@PathVariable Long id, @Valid @RequestBody UpdateEmployeeRequest request) {
        Employee updated = new Employee();
        updated.setName(request.getName());
        updated.setBadgeId(request.getBadgeId());
        updated.setRole(request.getRole());
        updated.setDepartment(request.getDepartment());
        updated.setShiftGroup(request.getShiftGroup());
        updated.setHireDate(request.getHireDate());
        updated.setStatus(request.getStatus());
        Employee saved = employeeService.update(id, updated);
        return ResponseEntity.ok(ApiResponse.success(EmployeeDTO.from(saved)));
    }

    /**
     * Soft-delete employee.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        employeeService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    /**
     * List employees with pagination and search.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<EmployeeDTO>>> list(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Employee> employees = employeeService.search(name, department, role, pageable);
        Page<EmployeeDTO> dtoPage = employees.map(EmployeeDTO::from);
        return ResponseEntity.ok(ApiResponse.success(dtoPage));
    }
}
