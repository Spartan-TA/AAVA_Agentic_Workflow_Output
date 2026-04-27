package com.warehouse.ems.employee.controller;

import com.warehouse.ems.employee.model.dto.EmployeeRequestDto;
import com.warehouse.ems.employee.model.dto.EmployeeResponseDto;
import com.warehouse.ems.employee.service.EmployeeService;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {
    private final EmployeeService employeeService;

    @Autowired
    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @PostMapping
    public ResponseEntity<EmployeeResponseDto> createEmployee(@Valid @RequestBody EmployeeRequestDto requestDto) {
        EmployeeResponseDto responseDto = employeeService.createEmployee(requestDto);
        return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponseDto> getEmployeeById(@PathVariable UUID id) {
        EmployeeResponseDto responseDto = employeeService.getEmployeeById(id);
        return ResponseEntity.ok(responseDto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmployeeResponseDto> updateEmployee(@PathVariable UUID id, @Valid @RequestBody EmployeeRequestDto requestDto) {
        EmployeeResponseDto responseDto = employeeService.updateEmployee(id, requestDto);
        return ResponseEntity.ok(responseDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> softDeleteEmployee(@PathVariable UUID id) {
        employeeService.softDeleteEmployee(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<Page<EmployeeResponseDto>> getAllEmployees(@RequestParam(required = false) String search, @ParameterObject Pageable pageable) {
        Page<EmployeeResponseDto> page = employeeService.getAllEmployees(search, pageable);
        return ResponseEntity.ok(page);
    }
}
