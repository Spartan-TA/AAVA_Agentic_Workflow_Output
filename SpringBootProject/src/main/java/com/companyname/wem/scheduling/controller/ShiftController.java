package com.companyname.wem.scheduling.controller;

import com.companyname.wem.scheduling.domain.EmployeeShiftAssignment;
import com.companyname.wem.scheduling.domain.ShiftTemplate;
import com.companyname.wem.scheduling.dto.ShiftAssignmentDTO;
import com.companyname.wem.scheduling.dto.ShiftTemplateDTO;
import com.companyname.wem.scheduling.repository.ShiftTemplateRepository;
import com.companyname.wem.scheduling.service.ScheduleService;
import jakarta.validation.Valid;
lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/shifts")
@RequiredArgsConstructor
public class ShiftController {
    private final ScheduleService scheduleService;
    private final ShiftTemplateRepository templateRepository;

    @PostMapping("/templates")
    public ResponseEntity<ShiftTemplate> createTemplate(@Valid @RequestBody ShiftTemplateDTO dto) {
        ShiftTemplate template = ShiftTemplate.builder()
            .name(dto.getName())
            .startTime(dto.getStartTime())
            .endTime(dto.getEndTime())
            .recurring(dto.isRecurring())
            .recurrencePattern(dto.getRecurrencePattern())
            .build();
        ShiftTemplate saved = templateRepository.save(template);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping("/templates")
    public ResponseEntity<List<ShiftTemplate>> listTemplates() {
        return ResponseEntity.ok(templateRepository.findAll());
    }

    @PostMapping("/assignments")
    public ResponseEntity<EmployeeShiftAssignment> assignShift(@Valid @RequestBody ShiftAssignmentDTO dto) {
        EmployeeShiftAssignment assignment = scheduleService.assignShift(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(assignment);
    }

    @GetMapping("/employee/{id}")
    public ResponseEntity<List<EmployeeShiftAssignment>> getEmployeeSchedule(@PathVariable Long id) {
        return ResponseEntity.ok(scheduleService.getEmployeeSchedule(id));
    }
}
