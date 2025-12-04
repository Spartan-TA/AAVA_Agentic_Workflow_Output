package com.warehouse.ems.employee.controller;

import com.warehouse.ems.employee.entity.Employee;
import com.warehouse.ems.employee.dto.EmployeeDto;
import com.warehouse.ems.employee.dto.EmployeeCreateDto;
import com.warehouse.ems.employee.dto.EmployeeUpdateDto;
import com.warehouse.ems.employee.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * REST controller for Employee CRUD, filtering, and soft-delete.
 */
@RestController
@RequestMapping("/employees")
public class EmployeeController {
    @Autowired
    private EmployeeService employeeService;

    @GetMapping
    public ResponseEntity<Page<Employee>> getAllEmployees(Pageable pageable) {
        return ResponseEntity.ok(employeeService.getAllEmployees(pageable));
    }

    @GetMapping("/{badgeId}")
    public ResponseEntity<Employee> getEmployee(@PathVariable String badgeId) {
        Optional<Employee> employee = employeeService.getEmployeeByBadgeId(badgeId);
        return employee.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Employee> createEmployee(@Valid @RequestBody EmployeeCreateDto dto) {
        Employee employee = employeeService.createEmployee(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(employee);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Employee> updateEmployee(@PathVariable Long id, @Valid @RequestBody EmployeeUpdateDto dto) {
        Employee employee = employeeService.updateEmployee(id, dto);
        return ResponseEntity.ok(employee);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Employee> patchEmployee(@PathVariable Long id, @RequestBody EmployeeUpdateDto dto) {
        Employee employee = employeeService.updateEmployee(id, dto);
        return ResponseEntity.ok(employee);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        employeeService.softDeleteEmployee(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/filter")
    public ResponseEntity<Page<Employee>> filterEmployees(@RequestParam(required = false) String department,
                                                          @RequestParam(required = false) String role,
                                                          Pageable pageable) {
        return ResponseEntity.ok(employeeService.filterEmployees(department, role, pageable));
    }
}
