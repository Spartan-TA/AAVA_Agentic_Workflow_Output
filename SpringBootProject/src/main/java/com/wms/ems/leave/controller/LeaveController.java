package com.wms.ems.leave.controller;

import com.wms.ems.leave.dto.LeaveRequestDto;
import com.wms.ems.leave.service.LeaveService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/leave")
@RequiredArgsConstructor
@Tag(name = "Leave", description = "Endpoints for leave management")
public class LeaveController {
    private final LeaveService leaveService;

    @Operation(summary = "Submit leave request")
    @PostMapping("/requests")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<?> submitRequest(@Valid @RequestBody LeaveRequestDto dto, BindingResult bindingResult, Principal principal) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(bindingResult.getAllErrors());
        }
        return ResponseEntity.ok(leaveService.submitRequest(dto, principal.getName()));
    }

    @Operation(summary = "Get leave requests")
    @GetMapping("/requests")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public ResponseEntity<List<LeaveRequestDto>> getRequests(@RequestParam(required = false) String employeeId) {
        return ResponseEntity.ok(leaveService.getRequests(employeeId));
    }

    @Operation(summary = "Approve leave request")
    @PostMapping("/requests/{id}/approve")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public ResponseEntity<?> approveRequest(@PathVariable Long id, Principal principal) {
        leaveService.approveRequest(id, principal.getName());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Deny leave request")
    @PostMapping("/requests/{id}/deny")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public ResponseEntity<?> denyRequest(@PathVariable Long id, Principal principal) {
        leaveService.denyRequest(id, principal.getName());
        return ResponseEntity.ok().build();
    }
}
