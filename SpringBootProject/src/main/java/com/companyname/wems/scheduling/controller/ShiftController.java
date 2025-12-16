package com.companyname.wems.scheduling.controller;

import com.companyname.wems.scheduling.model.ShiftTemplate;
import com.companyname.wems.scheduling.model.ShiftAssignment;
import com.companyname.wems.scheduling.service.ShiftService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/shifts")
@RequiredArgsConstructor
public class ShiftController {
    private final ShiftService shiftService;

    // Create shift template
    @PostMapping("/templates")
    public ResponseEntity<ShiftTemplate> createTemplate(@RequestBody ShiftTemplate template) {
        return ResponseEntity.ok(shiftService.createShiftTemplate(template));
    }

    // List shift templates
    @GetMapping("/templates")
    public ResponseEntity<List<ShiftTemplate>> getTemplates() {
        return ResponseEntity.ok(shiftService.getAllShiftTemplates());
    }

    // Assign shifts to employees (bulk)
    @PostMapping("/assign")
    public ResponseEntity<List<ShiftAssignment>> assignShifts(@RequestBody List<ShiftAssignment> assignments) {
        return ResponseEntity.ok(shiftService.assignShiftsBulk(assignments));
    }

    // Get employee shifts
    @GetMapping("/employee/{id}")
    public ResponseEntity<List<ShiftAssignment>> getEmployeeShifts(@PathVariable Long id) {
        return ResponseEntity.ok(shiftService.getEmployeeShifts(id));
    }
}