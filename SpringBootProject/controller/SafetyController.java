package com.example.ems.controller;

import com.example.ems.dto.SafetyDto;
import com.example.ems.service.SafetyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/safety")
@Validated
public class SafetyController {
    private final SafetyService safetyService;

    @Autowired
    public SafetyController(SafetyService safetyService) {
        this.safetyService = safetyService;
    }

    @GetMapping
    public ResponseEntity<List<SafetyDto>> getAllSafetyRecords() {
        return ResponseEntity.ok(safetyService.getAllSafetyRecords());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SafetyDto> getSafetyRecordById(@PathVariable Long id) {
        return ResponseEntity.ok(safetyService.getSafetyRecordById(id));
    }

    @PostMapping
    public ResponseEntity<SafetyDto> createSafetyRecord(@Valid @RequestBody SafetyDto safetyDto) {
        return ResponseEntity.ok(safetyService.createSafetyRecord(safetyDto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SafetyDto> updateSafetyRecord(@PathVariable Long id, @Valid @RequestBody SafetyDto safetyDto) {
        return ResponseEntity.ok(safetyService.updateSafetyRecord(id, safetyDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSafetyRecord(@PathVariable Long id) {
        safetyService.deleteSafetyRecord(id);
        return ResponseEntity.noContent().build();
    }
}