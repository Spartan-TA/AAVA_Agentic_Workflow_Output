package com.companyname.wem.shift.controller;

import com.companyname.wem.shift.dto.ShiftAssignmentDTO;
import com.companyname.wem.shift.dto.ShiftTemplateDTO;
import com.companyname.wem.shift.service.ScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/shifts")
@RequiredArgsConstructor
public class ShiftController {
    private final ScheduleService scheduleService;

    @GetMapping("/templates")
    public ResponseEntity<List<ShiftTemplateDTO>> getAllShiftTemplates() {
        return ResponseEntity.ok(scheduleService.getAllShiftTemplates());
    }

    @GetMapping("/templates/{id}")
    public ResponseEntity<ShiftTemplateDTO> getShiftTemplateById(@PathVariable Long id) {
        return scheduleService.getShiftTemplateById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/templates")
    public ResponseEntity<ShiftTemplateDTO> createShiftTemplate(@Valid @RequestBody ShiftTemplateDTO dto) {
        return ResponseEntity.ok(scheduleService.createShiftTemplate(dto));
    }

    @PutMapping("/templates/{id}")
    public ResponseEntity<ShiftTemplateDTO> updateShiftTemplate(@PathVariable Long id, @Valid @RequestBody ShiftTemplateDTO dto) {
        return scheduleService.updateShiftTemplate(id, dto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/templates/{id}")
    public ResponseEntity<Void> deleteShiftTemplate(@PathVariable Long id) {
        boolean deleted = scheduleService.deleteShiftTemplate(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @GetMapping("/assignments/employee/{employeeId}")
    public ResponseEntity<List<ShiftAssignmentDTO>> getAssignmentsByEmployee(@PathVariable Long employeeId) {
        return ResponseEntity.ok(scheduleService.getAssignmentsByEmployee(employeeId));
    }

    @PostMapping("/assignments")
    public ResponseEntity<ShiftAssignmentDTO> assignShift(@Valid @RequestBody ShiftAssignmentDTO dto) {
        return ResponseEntity.ok(scheduleService.assignShift(dto));
    }
}
