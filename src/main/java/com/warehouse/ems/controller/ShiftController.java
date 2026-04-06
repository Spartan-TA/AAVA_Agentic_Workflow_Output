package com.warehouse.ems.controller;

import com.warehouse.ems.dto.ShiftDTO;
import com.warehouse.ems.dto.ShiftAssignmentDTO;
import com.warehouse.ems.service.ShiftService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shifts")
public class ShiftController {

    @Autowired
    private ShiftService shiftService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','HR','SUPERVISOR')")
    public ResponseEntity<List<ShiftDTO>> getAllShifts() {
        return ResponseEntity.ok(shiftService.getAllShifts());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public ResponseEntity<ShiftDTO> createShift(@RequestBody ShiftDTO shiftDTO) {
        return ResponseEntity.ok(shiftService.createShift(shiftDTO));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public ResponseEntity<ShiftDTO> updateShift(@PathVariable Long id, @RequestBody ShiftDTO shiftDTO) {
        return ResponseEntity.ok(shiftService.updateShift(id, shiftDTO));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public ResponseEntity<Void> deleteShift(@PathVariable Long id) {
        shiftService.deleteShift(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/assignments")
    @PreAuthorize("hasRole('SUPERVISOR')")
    public ResponseEntity<List<ShiftAssignmentDTO>> assignShifts(@RequestBody List<ShiftAssignmentDTO> assignments) {
        return ResponseEntity.ok(shiftService.assignShifts(assignments));
    }

    @GetMapping("/conflicts")
    @PreAuthorize("hasAnyRole('ADMIN','HR','SUPERVISOR')")
    public ResponseEntity<List<ShiftAssignmentDTO>> getShiftConflicts() {
        return ResponseEntity.ok(shiftService.getShiftConflicts());
    }
}
