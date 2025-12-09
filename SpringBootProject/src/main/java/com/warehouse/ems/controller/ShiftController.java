package com.warehouse.ems.controller;

import com.warehouse.ems.domain.ShiftTemplate;
import com.warehouse.ems.domain.Employee;
import com.warehouse.ems.service.ShiftService;
import com.warehouse.ems.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * REST controller for Shift management.
 */
@RestController
@RequestMapping("/api/shifts")
public class ShiftController {
    private final ShiftService shiftService;
    private final EmployeeRepository employeeRepository;

    @Autowired
    public ShiftController(ShiftService shiftService, EmployeeRepository employeeRepository) {
        this.shiftService = shiftService;
        this.employeeRepository = employeeRepository;
    }

    /**
     * Create a new shift template.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('HR','ADMIN','SUPERVISOR')")
    public ResponseEntity<ShiftTemplate> createShift(@RequestBody ShiftTemplate shift) {
        ShiftTemplate saved = shiftService.createShift(shift);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    /**
     * Assign employees to a shift.
     */
    @PostMapping("/{shiftId}/assign")
    @PreAuthorize("hasAnyRole('HR','ADMIN','SUPERVISOR')")
    public ResponseEntity<ShiftTemplate> assignEmployees(@PathVariable Long shiftId, @RequestBody Set<Long> employeeIds) {
        Set<Employee> employees = new HashSet<>();
        for (Long empId : employeeIds) {
            Optional<Employee> empOpt = employeeRepository.findById(empId);
            empOpt.ifPresent(employees::add);
        }
        ShiftTemplate updated = shiftService.assignEmployees(shiftId, employees);
        return ResponseEntity.ok(updated);
    }
}
