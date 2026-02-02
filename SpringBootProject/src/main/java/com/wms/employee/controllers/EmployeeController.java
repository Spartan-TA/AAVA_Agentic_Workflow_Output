package com.wms.employee.controllers;

import com.wms.common.dto.ApiResponse;
import com.wms.employee.dtos.EmployeeDto;
import com.wms.employee.services.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for Employee endpoints.
 */
@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<EmployeeDto>>> getAllEmployees() {
        return ResponseEntity.ok(new ApiResponse<>(true, "Employees fetched successfully", employeeService.getAllEmployees()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EmployeeDto>> getEmployeeById(@PathVariable Long id) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Employee fetched successfully", employeeService.getEmployeeById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<EmployeeDto>> createEmployee(@RequestBody EmployeeDto dto) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Employee created successfully", employeeService.createEmployee(dto)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EmployeeDto>> updateEmployee(@PathVariable Long id, @RequestBody EmployeeDto dto) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Employee updated successfully", employeeService.updateEmployee(id, dto)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Employee deleted successfully", null));
    }

    @GetMapping("/department/{departmentId}")
    public ResponseEntity<ApiResponse<List<EmployeeDto>>> getEmployeesByDepartment(@PathVariable Long departmentId) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Employees by department fetched successfully", employeeService.getEmployeesByDepartment(departmentId)));
    }

    @GetMapping("/role/{role}")
    public ResponseEntity<ApiResponse<List<EmployeeDto>>> getEmployeesByRole(@PathVariable String role) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Employees by role fetched successfully", employeeService.getEmployeesByRole(Enum.valueOf(com.wms.common.enums.Role.class, role.toUpperCase()))));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<EmployeeDto>>> getEmployeesByStatus(@PathVariable String status) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Employees by status fetched successfully", employeeService.getEmployeesByStatus(Enum.valueOf(com.wms.common.enums.Status.class, status.toUpperCase()))));
    }
}
