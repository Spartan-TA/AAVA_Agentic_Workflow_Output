package com.warehouse.ems.scheduling;

import com.warehouse.ems.common.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.List;

/**
 * REST controller for managing shift assignments.
 */
@RestController
@RequestMapping("/api/scheduling/shifts")
@Validated
public class ShiftController {

    private final ShiftService shiftService;

    @Autowired
    public ShiftController(ShiftService shiftService) {
        this.shiftService = shiftService;
    }

    @GetMapping
    public ResponseEntity<List<ShiftAssignment>> getAllAssignments() {
        return ResponseEntity.ok(shiftService.getAllAssignments());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ShiftAssignment> getAssignmentById(@PathVariable Long id) {
        return ResponseEntity.ok(shiftService.getAssignmentById(id));
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<ShiftAssignment>> getAssignmentsByEmployee(@PathVariable Long employeeId) {
        return ResponseEntity.ok(shiftService.getAssignmentsByEmployee(employeeId));
    }

    @GetMapping("/date/{date}")
    public ResponseEntity<List<ShiftAssignment>> getAssignmentsByDate(@PathVariable String date) {
        LocalDate localDate = LocalDate.parse(date);
        return ResponseEntity.ok(shiftService.getAssignmentsByDate(localDate));
    }

    @PostMapping
    public ResponseEntity<ShiftAssignment> createAssignment(@Valid @RequestBody ShiftAssignment assignment) {
        ShiftAssignment created = shiftService.createAssignment(assignment);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ShiftAssignment> updateAssignment(@PathVariable Long id, @Valid @RequestBody ShiftAssignment assignment) {
        ShiftAssignment updated = shiftService.updateAssignment(id, assignment);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteAssignment(@PathVariable Long id) {
        shiftService.deleteAssignment(id);
        return ResponseEntity.ok(new ApiResponse(true, "Shift assignment deleted successfully"));
    }
}
