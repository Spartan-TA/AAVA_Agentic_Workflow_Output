package com.warehouse.ems.employee;

import com.warehouse.ems.common.exception.ResourceNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

/**
 * REST controller for Employee APIs.
 */
@RestController
@RequestMapping("/employees")
@RequiredArgsConstructor
public class EmployeeController {
    private final EmployeeService employeeService;

    /**
     * Create a new employee.
     */
    @PostMapping
    public ResponseEntity<EmployeeDto> create(@Valid @RequestBody EmployeeDto dto) {
        Employee employee = toEntity(dto);
        Employee saved = employeeService.create(employee);
        return new ResponseEntity<>(toDto(saved), HttpStatus.CREATED);
    }

    /**
     * Get employee by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<EmployeeDto> get(@PathVariable Long id) {
        Employee employee = employeeService.getById(id);
        return ResponseEntity.ok(toDto(employee));
    }

    /**
     * Update employee.
     */
    @PutMapping("/{id}")
    public ResponseEntity<EmployeeDto> update(@PathVariable Long id, @Valid @RequestBody EmployeeDto dto) {
        Employee updated = employeeService.update(id, toEntity(dto));
        return ResponseEntity.ok(toDto(updated));
    }

    /**
     * Partially update employee (PATCH).
     */
    @PatchMapping("/{id}")
    public ResponseEntity<EmployeeDto> patch(@PathVariable Long id, @RequestBody EmployeeDto dto) {
        Employee existing = employeeService.getById(id);
        if (dto.getName() != null) existing.setName(dto.getName());
        if (dto.getBadgeId() != null) existing.setBadgeId(dto.getBadgeId());
        if (dto.getRole() != null) existing.setRole(Employee.Role.valueOf(dto.getRole()));
        if (dto.getDepartment() != null) existing.setDepartment(dto.getDepartment());
        if (dto.getShiftGroup() != null) existing.setShiftGroup(dto.getShiftGroup());
        if (dto.getHireDate() != null) existing.setHireDate(dto.getHireDate());
        if (dto.getStatus() != null) existing.setStatus(dto.getStatus());
        if (dto.getActive() != null) existing.setActive(dto.getActive());
        if (dto.getDeleted() != null) existing.setDeleted(dto.getDeleted());
        Employee saved = employeeService.create(existing);
        return ResponseEntity.ok(toDto(saved));
    }

    /**
     * Soft-delete employee.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        employeeService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * List employees with pagination and filtering.
     */
    @GetMapping
    public ResponseEntity<Page<EmployeeDto>> list(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String department,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Employee> employees = employeeService.list(name, department, pageable);
        Page<EmployeeDto> dtos = employees.map(this::toDto);
        return ResponseEntity.ok(dtos);
    }

    // --- Mapping helpers ---
    private EmployeeDto toDto(Employee e) {
        return EmployeeDto.builder()
                .id(e.getId())
                .name(e.getName())
                .badgeId(e.getBadgeId())
                .role(e.getRole().name())
                .department(e.getDepartment())
                .shiftGroup(e.getShiftGroup())
                .hireDate(e.getHireDate())
                .status(e.getStatus())
                .active(e.isActive())
                .deleted(e.isDeleted())
                .build();
    }
    private Employee toEntity(EmployeeDto dto) {
        return Employee.builder()
                .id(dto.getId())
                .name(dto.getName())
                .badgeId(dto.getBadgeId())
                .role(dto.getRole() != null ? Employee.Role.valueOf(dto.getRole()) : null)
                .department(dto.getDepartment())
                .shiftGroup(dto.getShiftGroup())
                .hireDate(dto.getHireDate())
                .status(dto.getStatus())
                .active(dto.getActive() != null ? dto.getActive() : true)
                .deleted(dto.getDeleted() != null ? dto.getDeleted() : false)
                .build();
    }
}
