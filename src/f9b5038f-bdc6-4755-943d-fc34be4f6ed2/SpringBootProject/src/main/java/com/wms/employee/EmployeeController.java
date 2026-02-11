package com.wms.employee;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * REST Controller for Employee CRUD operations.
 */
@RestController
@RequestMapping("/employees")
public class EmployeeController {
    private final EmployeeService employeeService;

    @Autowired
    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping
    public Page<Employee> getAllEmployees(Pageable pageable) {
        return employeeService.getAllEmployees(pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Employee> getEmployeeById(@PathVariable Long id) {
        Optional<Employee> employee = employeeService.getEmployeeById(id);
        return employee.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Employee> createEmployee(@Valid @RequestBody EmployeeDto dto) {
        Employee employee = Employee.builder()
                .badgeId(dto.getBadgeId())
                .name(dto.getName())
                .role(dto.getRole())
                .department(dto.getDepartment())
                .shiftGroup(dto.getShiftGroup())
                .hireDate(dto.getHireDate())
                .status(dto.getStatus())
                .build();
        Employee saved = employeeService.createEmployee(employee);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Employee> updateEmployee(@PathVariable Long id, @Valid @RequestBody EmployeeDto dto) {
        Employee updated = Employee.builder()
                .badgeId(dto.getBadgeId())
                .name(dto.getName())
                .role(dto.getRole())
                .department(dto.getDepartment())
                .shiftGroup(dto.getShiftGroup())
                .hireDate(dto.getHireDate())
                .status(dto.getStatus())
                .build();
        Employee saved = employeeService.updateEmployee(id, updated);
        return ResponseEntity.ok(saved);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Employee> patchEmployee(@PathVariable Long id, @RequestBody EmployeeDto dto) {
        Optional<Employee> existingOpt = employeeService.getEmployeeById(id);
        if (existingOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Employee existing = existingOpt.get();
        if (dto.getBadgeId() != null) existing.setBadgeId(dto.getBadgeId());
        if (dto.getName() != null) existing.setName(dto.getName());
        if (dto.getRole() != null) existing.setRole(dto.getRole());
        if (dto.getDepartment() != null) existing.setDepartment(dto.getDepartment());
        if (dto.getShiftGroup() != null) existing.setShiftGroup(dto.getShiftGroup());
        if (dto.getHireDate() != null) existing.setHireDate(dto.getHireDate());
        if (dto.getStatus() != null) existing.setStatus(dto.getStatus());
        Employee saved = employeeService.updateEmployee(id, existing);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        employeeService.softDeleteEmployee(id);
        return ResponseEntity.noContent().build();
    }
}
