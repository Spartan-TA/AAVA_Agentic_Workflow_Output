package com.wms.ems.schedule;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/scheduling")
public class ShiftController {

    @Autowired
    private ShiftService shiftService;

    @PostMapping("/shifts")
    @PreAuthorize("hasAnyRole('ADMIN','HR','SUPERVISOR')")
    public ResponseEntity<ShiftTemplate> createShift(@RequestBody ShiftTemplate template) {
        return ResponseEntity.ok(shiftService.createShiftTemplate(template));
    }

    @GetMapping("/shifts")
    @PreAuthorize("hasAnyRole('ADMIN','HR','SUPERVISOR','WORKER')")
    public ResponseEntity<List<ShiftTemplate>> getAllShifts() {
        return ResponseEntity.ok(shiftService.getAllShiftTemplates());
    }

    @PutMapping("/shifts/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','HR','SUPERVISOR')")
    public ResponseEntity<ShiftTemplate> updateShift(@PathVariable Long id, @RequestBody ShiftTemplate template) {
        return ResponseEntity.ok(shiftService.updateShiftTemplate(id, template));
    }

    @DeleteMapping("/shifts/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public ResponseEntity<Void> deleteShift(@PathVariable Long id) {
        shiftService.deleteShiftTemplate(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/schedules/bulk-assign")
    @PreAuthorize("hasAnyRole('ADMIN','HR','SUPERVISOR')")
    public ResponseEntity<Void> bulkAssign(@RequestBody List<ShiftAssignment> assignments) {
        shiftService.bulkAssign(assignments);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/calendar/{date}")
    @PreAuthorize("hasAnyRole('ADMIN','HR','SUPERVISOR','WORKER')")
    public ResponseEntity<Boolean> isBlackoutDate(@PathVariable String date) {
        boolean isBlackout = shiftService.isBlackoutDate(LocalDate.parse(date));
        return ResponseEntity.ok(isBlackout);
    }
}
