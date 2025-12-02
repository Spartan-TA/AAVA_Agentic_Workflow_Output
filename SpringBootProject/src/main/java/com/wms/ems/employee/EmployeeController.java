package com.wms.ems.employee;

import com.wms.ems.employee.dto.EmployeeRequestDTO;
import com.wms.ems.employee.dto.EmployeeResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;

/**
 * REST Controller for Employee Master Data CRUD operations.
 */
@RestController
@RequestMapping("/employees")
@Tag(name = "Employee", description = "Employee Master Data APIs")
public class EmployeeController {
    @Autowired
    private EmployeeService employeeService;

    @Operation(summary = "Get paginated list of employees", description = "Supports filtering by name and department.")
    @GetMapping
    public Page<EmployeeResponseDTO> getEmployees(@RequestParam(required = false) String name,
                                                  @RequestParam(required = false) Long departmentId,
                                                  Pageable pageable) {
        return employeeService.getAllEmployees(name, departmentId, pageable);
    }

    @Operation(summary = "Get employee by ID")
    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponseDTO> getEmployee(@PathVariable Long id) {
        return employeeService.getEmployee(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Create a new employee")
    @PostMapping
    public ResponseEntity<EmployeeResponseDTO> createEmployee(@RequestBody EmployeeRequestDTO dto) {
        return ResponseEntity.ok(employeeService.createEmployee(dto));
    }

    @Operation(summary = "Update an employee")
    @PutMapping("/{id}")
    public ResponseEntity<EmployeeResponseDTO> updateEmployee(@PathVariable Long id, @RequestBody EmployeeRequestDTO dto) {
        return employeeService.updateEmployee(id, dto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Soft-delete an employee")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        return employeeService.softDeleteEmployee(id) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
