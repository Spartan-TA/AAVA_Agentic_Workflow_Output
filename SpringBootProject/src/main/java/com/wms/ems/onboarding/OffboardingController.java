package com.wms.ems.onboarding;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/offboarding")
public class OffboardingController {

    @Autowired
    private OnboardingService onboardingService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public ResponseEntity<String> offboardEmployee(@RequestBody OffboardDto dto) {
        onboardingService.offboardEmployee(dto);
        return ResponseEntity.ok("Employee offboarded");
    }
}
