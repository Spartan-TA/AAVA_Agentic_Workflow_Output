package com.wms.ems.scheduling.controller;

import com.wms.ems.scheduling.dto.ShiftTemplateDto;
import com.wms.ems.scheduling.dto.ShiftAssignmentDto;
import com.wms.ems.scheduling.dto.BulkAssignDto;
import com.wms.ems.scheduling.service.ShiftService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/shifts")
@RequiredArgsConstructor
@Tag(name = "Shifts", description = "Endpoints for shift templates and assignments management")
public class ShiftController {
    private final ShiftService shiftService;

    // Shift Templates CRUD
    @Operation(summary = "Create shift template")
    @PostMapping("/templates")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createTemplate(@Valid @RequestBody ShiftTemplateDto dto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(bindingResult.getAllErrors());
        }
        return ResponseEntity.ok(shiftService.createTemplate(dto));
    }

    @Operation(summary = "Get all shift templates")
    @GetMapping("/templates")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<List<ShiftTemplateDto>> getTemplates() {
        return ResponseEntity.ok(shiftService.getTemplates());
    }

    @Operation(summary = "Update shift template")
    @PutMapping("/templates/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateTemplate(@PathVariable Long id, @Valid @RequestBody ShiftTemplateDto dto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(bindingResult.getAllErrors());
        }
        return ResponseEntity.ok(shiftService.updateTemplate(id, dto));
    }

    @Operation(summary = "Delete shift template")
    @DeleteMapping("/templates/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteTemplate(@PathVariable Long id) {
        shiftService.deleteTemplate(id);
        return ResponseEntity.noContent().build();
    }

    // Shift Assignments CRUD
    @Operation(summary = "Create shift assignment")
    @PostMapping("/assignments")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<?> createAssignment(@Valid @RequestBody ShiftAssignmentDto dto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(bindingResult.getAllErrors());
        }
        return ResponseEntity.ok(shiftService.createAssignment(dto));
    }

    @Operation(summary = "Get all shift assignments")
    @GetMapping("/assignments")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<List<ShiftAssignmentDto>> getAssignments() {
        return ResponseEntity.ok(shiftService.getAssignments());
    }

    @Operation(summary = "Update shift assignment")
    @PutMapping("/assignments/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<?> updateAssignment(@PathVariable Long id, @Valid @RequestBody ShiftAssignmentDto dto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(bindingResult.getAllErrors());
        }
        return ResponseEntity.ok(shiftService.updateAssignment(id, dto));
    }

    @Operation(summary = "Delete shift assignment")
    @DeleteMapping("/assignments/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<?> deleteAssignment(@PathVariable Long id) {
        shiftService.deleteAssignment(id);
        return ResponseEntity.noContent().build();
    }

    // Bulk Assignment
    @Operation(summary = "Bulk assign shifts")
    @PostMapping("/assignments/bulk")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> bulkAssign(@Valid @RequestBody BulkAssignDto dto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(bindingResult.getAllErrors());
        }
        return ResponseEntity.ok(shiftService.bulkAssign(dto));
    }
}
