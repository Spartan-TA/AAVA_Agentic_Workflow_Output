package com.wms.employee.controller;

import com.wms.employee.dto.CreateEmployeeRequest;
import com.wms.employee.dto.EmployeeDto;
import com.wms.employee.dto.UpdateEmployeeRequest;
import com.wms.employee.entity.Employee;
import com.wms.employee.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Optional;

/**
 * REST controller for Employee CRUD APIs.
 */
@RestController
@RequestMapping("/api/employees")
public class EmployeeController {
    @Autowired
    private EmployeeService employeeService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'SUPERVISOR')")
    public Page<Employee> getEmployees(@RequestParam(required = false) String department,
                                       @RequestParam(defaultValue = "0") int page,
                                       @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return employeeService.getAllEmployees(department, pageable);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'SUPERVISOR', 'WORKER')")
    public ResponseEntity<Employee> getEmployee(@PathVariable Long id) {
        return employeeService.getEmployeeById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<Employee> createEmployee(@Valid @RequestBody CreateEmployeeRequest request, Principal principal) {
        Employee employee = employeeService.createEmployee(request, principal.getName());
        return new ResponseEntity<>(employee, HttpStatus.CREATED);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<Employee> updateEmployee(@PathVariable Long id,
                                                   @Valid @RequestBody UpdateEmployeeRequest request,
                                                   Principal principal) {
        Optional<Employee> updated = employeeService.updateEmployee(id, request, principal.getName());
        return updated.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id, Principal principal) {
        boolean deleted = employeeService.softDeleteEmployee(id, principal.getName());
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
