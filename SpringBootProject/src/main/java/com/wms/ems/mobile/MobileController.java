package com.wms.ems.mobile;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/mobile")
public class MobileController {

    @GetMapping("/schedules")
    @PreAuthorize("hasAnyRole('ADMIN','HR','SUPERVISOR','WORKER')")
    public ResponseEntity<String> getSchedules() {
        // Return schedules optimized for mobile
        return ResponseEntity.ok("Mobile schedules endpoint");
    }

    @PostMapping("/clock-in")
    @PreAuthorize("hasAnyRole('ADMIN','HR','SUPERVISOR','WORKER')")
    public ResponseEntity<String> clockIn() {
        // Offline queue logic can be added here
        return ResponseEntity.ok("Mobile clock-in endpoint");
    }

    @PostMapping("/leave")
    @PreAuthorize("hasAnyRole('ADMIN','HR','SUPERVISOR','WORKER')")
    public ResponseEntity<String> requestLeave() {
        // Mobile leave request logic
        return ResponseEntity.ok("Mobile leave request endpoint");
    }
}
