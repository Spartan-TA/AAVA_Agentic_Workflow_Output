package com.example.warehouse.scheduling;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shifts")
public class ShiftController {
    @Autowired
    private ShiftService shiftService;

    @GetMapping
    public List<Shift> getAllShifts() {
        return shiftService.getAllShifts();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Shift> getShiftById(@PathVariable Long id) {
        return shiftService.getShiftById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Shift createShift(@RequestBody Shift shift) {
        return shiftService.createShift(shift);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteShift(@PathVariable Long id) {
        shiftService.deleteShift(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/templates")
    public List<ShiftTemplate> getAllShiftTemplates() {
        return shiftService.getAllShiftTemplates();
    }

    @PostMapping("/templates")
    public ShiftTemplate createShiftTemplate(@RequestBody ShiftTemplate template) {
        return shiftService.createShiftTemplate(template);
    }

    @DeleteMapping("/templates/{id}")
    public ResponseEntity<Void> deleteShiftTemplate(@PathVariable Long id) {
        shiftService.deleteShiftTemplate(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/bulk-assign")
    public ResponseEntity<Void> bulkAssignShifts(@RequestBody BulkAssignDto dto) {
        shiftService.bulkAssignShifts(dto);
        return ResponseEntity.ok().build();
    }
}
