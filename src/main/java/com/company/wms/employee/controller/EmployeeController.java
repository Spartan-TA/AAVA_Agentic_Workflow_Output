package com.company.wms.employee.controller;

import com.company.wms.employee.dto.EmployeeRequestDTO;
import com.company.wms.employee.dto.EmployeeResponseDTO;
import com.company.wms.employee.entity.Employee;
import com.company.wms.employee.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.PageRequest;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/employees")
@Tag(name = "Employee", description = "Employee Master Data CRUD APIs")
public class EmployeeController {
    private final EmployeeService employeeService;

    @Autowired
    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @Operation(summary = "Create new employee")
    @PostMapping
    public ResponseEntity<EmployeeResponseDTO> createEmployee(@Valid @RequestBody EmployeeRequestDTO dto) {
        Employee employee = new Employee();
        employee.setBadgeId(dto.getBadgeId());
        employee.setName(dto.getName());
        employee.setRole(dto.getRole());
        employee.setDepartment(dto.getDepartment());
        employee.setShiftGroup(dto.getShiftGroup());
        employee.setHireDate(dto.getHireDate());
        employee.setStatus(dto.getStatus());
        Employee saved = employeeService.createEmployee(employee);
        return ResponseEntity.ok(toResponseDTO(saved));
    }

    @Operation(summary = "Get employee by ID")
    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponseDTO> getEmployeeById(@PathVariable Long id) {
        return employeeService.getEmployeeById(id)
                .map(e -> ResponseEntity.ok(toResponseDTO(e)))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Get employee by badgeId")
    @GetMapping("/badge/{badgeId}")
    public ResponseEntity<EmployeeResponseDTO> getEmployeeByBadgeId(@PathVariable String badgeId) {
        return employeeService.getEmployeeByBadgeId(badgeId)
                .map(e -> ResponseEntity.ok(toResponseDTO(e)))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Get all employees with pagination")
    @GetMapping
    public ResponseEntity<List<EmployeeResponseDTO>> getAllEmployees(@RequestParam(defaultValue = "0") int page,
                                                                    @RequestParam(defaultValue = "10") int size) {
        List<EmployeeResponseDTO> dtos = employeeService.getAllEmployees(page, size)
                .stream().map(this::toResponseDTO).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @Operation(summary = "Update employee")
    @PutMapping("/{id}")
    public ResponseEntity<EmployeeResponseDTO> updateEmployee(@PathVariable Long id,
                                                             @Valid @RequestBody EmployeeRequestDTO dto) {
        Employee updated = new Employee();
        updated.setBadgeId(dto.getBadgeId());
        updated.setName(dto.getName());
        updated.setRole(dto.getRole());
        updated.setDepartment(dto.getDepartment());
        updated.setShiftGroup(dto.getShiftGroup());
        updated.setHireDate(dto.getHireDate());
        updated.setStatus(dto.getStatus());
        Employee saved = employeeService.updateEmployee(id, updated);
        return ResponseEntity.ok(toResponseDTO(saved));
    }

    @Operation(summary = "Soft delete employee")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> softDeleteEmployee(@PathVariable Long id) {
        employeeService.softDeleteEmployee(id);
        return ResponseEntity.noContent().build();
    }

    private EmployeeResponseDTO toResponseDTO(Employee e) {
        EmployeeResponseDTO dto = new EmployeeResponseDTO();
        dto.setId(e.getId());
        dto.setBadgeId(e.getBadgeId());
        dto.setName(e.getName());
        dto.setRole(e.getRole());
        dto.setDepartment(e.getDepartment());
        dto.setShiftGroup(e.getShiftGroup());
        dto.setHireDate(e.getHireDate());
        dto.setStatus(e.getStatus());
        return dto;
    }
}
