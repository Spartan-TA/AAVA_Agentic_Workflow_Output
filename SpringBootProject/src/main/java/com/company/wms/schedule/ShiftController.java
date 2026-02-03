package com.company.wms.schedule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

/**
 * REST Controller for managing shifts and assignments.
 */
@RestController
@RequestMapping("/api/schedule")
@Validated
public class ShiftController {
    private static final Logger logger = LoggerFactory.getLogger(ShiftController.class);

    private final ShiftService shiftService;

    @Autowired
    public ShiftController(ShiftService shiftService) {
        this.shiftService = shiftService;
    }

    @GetMapping("/shifts")
    public ResponseEntity<List<ShiftTemplate>> getAllShifts() {
        logger.info("API: Get all shifts");
        return ResponseEntity.ok(shiftService.getAllShifts());
    }

    @GetMapping("/shifts/{id}")
    public ResponseEntity<ShiftTemplate> getShiftById(@PathVariable Long id) {
        logger.info("API: Get shift by id {}", id);
        Optional<ShiftTemplate> shift = shiftService.getShiftById(id);
        return shift.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/shifts")
    public ResponseEntity<ShiftTemplate> createShift(@Valid @RequestBody ShiftTemplate shiftTemplate) {
        logger.info("API: Create shift");
        ShiftTemplate created = shiftService.createShift(shiftTemplate);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/shifts/{id}")
    public ResponseEntity<ShiftTemplate> updateShift(@PathVariable Long id, @Valid @RequestBody ShiftTemplate shiftTemplate) {
        logger.info("API: Update shift {}", id);
        ShiftTemplate updated = shiftService.updateShift(id, shiftTemplate);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/shifts/{id}")
    public ResponseEntity<Void> deleteShift(@PathVariable Long id) {
        logger.info("API: Delete shift {}", id);
        shiftService.deleteShift(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/assignments/employee/{employeeId}")
    public ResponseEntity<List<ShiftAssignment>> getAssignmentsByEmployee(@PathVariable Long employeeId) {
        logger.info("API: Get assignments for employee {}", employeeId);
        return ResponseEntity.ok(shiftService.getAssignmentsByEmployee(employeeId));
    }

    @PostMapping("/assignments")
    public ResponseEntity<ShiftAssignment> assignShift(@Valid @RequestBody ShiftAssignment assignment) {
        logger.info("API: Assign shift");
        ShiftAssignment created = shiftService.assignShift(assignment);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @DeleteMapping("/assignments/{assignmentId}")
    public ResponseEntity<Void> deleteAssignment(@PathVariable Long assignmentId) {
        logger.info("API: Delete assignment {}", assignmentId);
        shiftService.deleteAssignment(assignmentId);
        return ResponseEntity.noContent().build();
    }
}
