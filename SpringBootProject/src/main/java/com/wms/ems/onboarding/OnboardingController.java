package com.wms.ems.onboarding;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/onboarding")
public class OnboardingController {

    @Autowired
    private OnboardingService onboardingService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public ResponseEntity<String> provisionNewHire(@RequestBody NewHireDto dto) {
        onboardingService.provisionNewHire(dto);
        return ResponseEntity.ok("New hire provisioned");
    }
}
