package com.wms.scheduling.controller;

import com.wms.scheduling.entity.ShiftTemplate;
import com.wms.scheduling.entity.ShiftAssignment;
import com.wms.scheduling.dto.BulkAssignRequest;
import com.wms.scheduling.service.ShiftService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * REST controller for shift scheduling endpoints.
 */
@RestController
@RequestMapping("/api/shifts")
@RequiredArgsConstructor
public class ShiftController {
    private final ShiftService shiftService;

    @GetMapping("/templates")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'SUPERVISOR')")
    public ResponseEntity<List<ShiftTemplate>> getAllShiftTemplates() {
        return ResponseEntity.ok(shiftService.getAllShiftTemplates());
    }

    @PostMapping("/templates")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<ShiftTemplate> createShiftTemplate(@Valid @RequestBody ShiftTemplate template) {
        return new ResponseEntity<>(shiftService.createShiftTemplate(template), HttpStatus.CREATED);
    }

    @PostMapping("/assign")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'SUPERVISOR')")
    public ResponseEntity<ShiftAssignment> assignShift(@RequestParam Long employeeId, @RequestParam Long shiftTemplateId, @RequestParam LocalDate date) {
        return new ResponseEntity<>(shiftService.assignShift(employeeId, shiftTemplateId, date), HttpStatus.CREATED);
    }

    @PostMapping("/bulk-assign")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'SUPERVISOR')")
    public ResponseEntity<Void> bulkAssignShifts(@Valid @RequestBody BulkAssignRequest request) {
        shiftService.bulkAssignShifts(request);
        return ResponseEntity.noContent().build();
    }
}
