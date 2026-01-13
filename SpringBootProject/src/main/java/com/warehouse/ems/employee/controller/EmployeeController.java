package com.warehouse.ems.employee.controller;

import com.warehouse.ems.employee.dto.EmployeeDto;
import com.warehouse.ems.employee.entity.Employee;
import com.warehouse.ems.employee.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;
    private final ModelMapper modelMapper = new ModelMapper();

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'SUPERVISOR')")
    public ResponseEntity<Page<EmployeeDto>> getAllEmployees(Pageable pageable) {
        Page<Employee> employees = employeeService.getAllEmployees(pageable);
        Page<EmployeeDto> dtos = employees.map(emp -> modelMapper.map(emp, EmployeeDto.class));
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{badgeId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'SUPERVISOR')")
    public ResponseEntity<EmployeeDto> getEmployee(@PathVariable String badgeId) {
        Employee employee = employeeService.getEmployeeByBadgeId(badgeId);
        return ResponseEntity.ok(modelMapper.map(employee, EmployeeDto.class));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EmployeeDto> createEmployee(@RequestBody EmployeeDto dto) {
        Employee employee = employeeService.createEmployee(dto);
        return ResponseEntity.ok(modelMapper.map(employee, EmployeeDto.class));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EmployeeDto> updateEmployee(@PathVariable Long id, @RequestBody EmployeeDto dto) {
        Employee employee = employeeService.updateEmployee(id, dto);
        return ResponseEntity.ok(modelMapper.map(employee, EmployeeDto.class));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        employeeService.softDeleteEmployee(id);
        return ResponseEntity.noContent().build();
    }
}