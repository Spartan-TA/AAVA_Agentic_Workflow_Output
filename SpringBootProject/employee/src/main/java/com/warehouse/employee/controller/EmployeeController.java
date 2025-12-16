package com.warehouse.employee.controller;

import com.warehouse.employee.domain.Employee;
import com.warehouse.employee.dto.EmployeeRequestDTO;
import com.warehouse.employee.dto.EmployeeResponseDTO;
import com.warehouse.employee.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;

/**
 * REST controller for Employee CRUD operations.
 */
@RestController
@RequestMapping("/employees")
public class EmployeeController {
    @Autowired
    private EmployeeService employeeService;

    @GetMapping
    public Page<EmployeeResponseDTO> getAllEmployees(@RequestParam(defaultValue = "0") int page,
                                                    @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return employeeService.getAllEmployees(pageable)
                .map(this::toResponseDTO);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponseDTO> getEmployeeById(@PathVariable Long id) {
        Optional<Employee> employee = employeeService.getEmployeeById(id);
        return employee.map(e -> ResponseEntity.ok(toResponseDTO(e)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<EmployeeResponseDTO> createEmployee(@Valid @RequestBody EmployeeRequestDTO dto) {
        Employee employee = toEntity(dto);
        Employee created = employeeService.createEmployee(employee);
        return new ResponseEntity<>(toResponseDTO(created), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmployeeResponseDTO> updateEmployee(@PathVariable Long id,
                                                             @Valid @RequestBody EmployeeRequestDTO dto) {
        Employee updated = employeeService.updateEmployee(id, toEntity(dto));
        return ResponseEntity.ok(toResponseDTO(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> softDeleteEmployee(@PathVariable Long id) {
        employeeService.softDeleteEmployee(id);
        return ResponseEntity.noContent().build();
    }

    // Mapping methods
    private EmployeeResponseDTO toResponseDTO(Employee e) {
        EmployeeResponseDTO dto = new EmployeeResponseDTO();
        dto.setId(e.getId());
        dto.setName(e.getName());
        dto.setBadgeId(e.getBadgeId());
        dto.setRole(e.getRole());
        dto.setDepartment(e.getDepartment());
        dto.setShiftGroup(e.getShiftGroup());
        dto.setHireDate(e.getHireDate());
        dto.setStatus(e.getStatus());
        return dto;
    }

    private Employee toEntity(EmployeeRequestDTO dto) {
        Employee e = new Employee();
        e.setName(dto.getName());
        e.setBadgeId(dto.getBadgeId());
        e.setRole(dto.getRole());
        e.setDepartment(dto.getDepartment());
        e.setShiftGroup(dto.getShiftGroup());
        e.setHireDate(dto.getHireDate());
        e.setStatus(dto.getStatus());
        return e;
    }
}
