package com.companyname.wem.attendance.controller;

import com.companyname.wem.attendance.domain.AttendanceEvent;
import com.companyname.wem.attendance.dto.ClockEventDTO;
import com.companyname.wem.attendance.service.AttendanceService;
import jakarta.validation.Valid;
lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/attendance")
@RequiredArgsConstructor
public class AttendanceController {
    private final AttendanceService service;

    @PostMapping("/clock-in")
    public ResponseEntity<AttendanceEvent> clockIn(@Valid @RequestBody ClockEventDTO dto) {
        AttendanceEvent event = service.clockIn(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(event);
    }

    @PostMapping("/clock-out")
    public ResponseEntity<AttendanceEvent> clockOut(@Valid @RequestBody ClockEventDTO dto) {
        AttendanceEvent event = service.clockOut(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(event);
    }
}
