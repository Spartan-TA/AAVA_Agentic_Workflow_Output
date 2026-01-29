package com.warehouse.employee.management.controller;

import com.warehouse.employee.management.dto.EmployeeDto;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import java.util.*;

@RestController
@RequestMapping("/employees")
@Validated
public class EmployeeController {
    private final List<EmployeeDto> employees = new ArrayList<>();

    @PreAuthorize("hasAuthority('EMPLOYEE_CREATE')")
    @PostMapping
    public EmployeeDto createEmployee(@Valid @RequestBody EmployeeDto employeeDto) {
        employees.add(employeeDto);
        return employeeDto;
    }

    @PreAuthorize("hasAuthority('EMPLOYEE_READ')")
    @GetMapping
    public List<EmployeeDto> getEmployees(@RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "10") int size,
                                          @RequestParam(required = false) String department,
                                          @RequestParam(required = false) String status) {
        // Simple filtering and pagination
        List<EmployeeDto> filtered = new ArrayList<>();
        for (EmployeeDto e : employees) {
            if ((department == null || department.equals(e.getDepartmentId().toString())) &&
                (status == null || status.equalsIgnoreCase(e.getStatus()))) {
                filtered.add(e);
            }
        }
        int from = Math.min(page * size, filtered.size());
        int to = Math.min(from + size, filtered.size());
        return filtered.subList(from, to);
    }

    @PreAuthorize("hasAuthority('EMPLOYEE_UPDATE')")
    @PutMapping("/{index}")
    public EmployeeDto updateEmployee(@PathVariable int index, @Valid @RequestBody EmployeeDto employeeDto) {
        if (index < 0 || index >= employees.size()) throw new IllegalArgumentException("Invalid index");
        employees.set(index, employeeDto);
        return employeeDto;
    }

    @PreAuthorize("hasAuthority('EMPLOYEE_DELETE')")
    @DeleteMapping("/{index}")
    public void deleteEmployee(@PathVariable int index) {
        if (index < 0 || index >= employees.size()) throw new IllegalArgumentException("Invalid index");
        employees.remove(index);
    }
}
