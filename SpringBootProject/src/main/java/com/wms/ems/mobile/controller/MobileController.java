package com.wms.ems.mobile.controller;

import com.wms.ems.attendance.dto.AttendanceDto;
import com.wms.ems.leave.dto.LeaveRequestDto;
import com.wms.ems.mobile.service.MobileService;
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
@RequestMapping("/mobile")
@RequiredArgsConstructor
@Tag(name = "Mobile", description = "Endpoints for mobile app integration")
public class MobileController {
    private final MobileService mobileService;

    @Operation(summary = "Mobile clock-in")
    @PostMapping("/clock-in")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<?> mobileClockIn(@Valid @RequestBody AttendanceDto dto, BindingResult bindingResult, Principal principal) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(bindingResult.getAllErrors());
        }
        return ResponseEntity.ok(mobileService.mobileClockIn(dto, principal.getName()));
    }

    @Operation(summary = "Get mobile schedule")
    @GetMapping("/schedule")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<?> getSchedule(Principal principal) {
        return ResponseEntity.ok(mobileService.getSchedule(principal.getName()));
    }

    @Operation(summary = "Submit mobile leave request")
    @PostMapping("/leave")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<?> mobileLeave(@Valid @RequestBody LeaveRequestDto dto, BindingResult bindingResult, Principal principal) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(bindingResult.getAllErrors());
        }
        return ResponseEntity.ok(mobileService.mobileLeave(dto, principal.getName()));
    }

    @Operation(summary = "Get mobile announcements")
    @GetMapping("/announcements")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<List<String>> getAnnouncements(Principal principal) {
        return ResponseEntity.ok(mobileService.getAnnouncements(principal.getName()));
    }
}
