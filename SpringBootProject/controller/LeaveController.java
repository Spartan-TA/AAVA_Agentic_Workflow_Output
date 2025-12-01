package com.example.ems.controller;

import com.example.ems.dto.LeaveDto;
import com.example.ems.service.LeaveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/leaves")
@Validated
public class LeaveController {
    private final LeaveService leaveService;

    @Autowired
    public LeaveController(LeaveService leaveService) {
        this.leaveService = leaveService;
    }

    @GetMapping
    public ResponseEntity<List<LeaveDto>> getAllLeaves() {
        return ResponseEntity.ok(leaveService.getAllLeaves());
    }

    @GetMapping("/{id}")
    public ResponseEntity<LeaveDto> getLeaveById(@PathVariable Long id) {
        return ResponseEntity.ok(leaveService.getLeaveById(id));
    }

    @PostMapping
    public ResponseEntity<LeaveDto> createLeave(@Valid @RequestBody LeaveDto leaveDto) {
        return ResponseEntity.ok(leaveService.createLeave(leaveDto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<LeaveDto> updateLeave(@PathVariable Long id, @Valid @RequestBody LeaveDto leaveDto) {
        return ResponseEntity.ok(leaveService.updateLeave(id, leaveDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLeave(@PathVariable Long id) {
        leaveService.deleteLeave(id);
        return ResponseEntity.noContent().build();
    }
}