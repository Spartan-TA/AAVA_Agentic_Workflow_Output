package com.warehouse.employee.management.controller;

import com.warehouse.employee.management.dto.CertificationDto;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/certifications")
@Validated
public class CertificationController {
    private final List<CertificationDto> certifications = new ArrayList<>();

    @PreAuthorize("hasAuthority('CERTIFICATION_CREATE')")
    @PostMapping
    public CertificationDto addCertification(@Valid @RequestBody CertificationDto cert) {
        certifications.add(cert);
        return cert;
    }

    @PreAuthorize("hasAuthority('CERTIFICATION_READ')")
    @GetMapping
    public List<CertificationDto> getCertifications() {
        return Collections.unmodifiableList(certifications);
    }

    @PreAuthorize("hasAuthority('CERTIFICATION_ALERT')")
    @GetMapping("/expiry-alerts")
    public List<CertificationDto> getExpiringCertifications() {
        List<CertificationDto> expiring = new ArrayList<>();
        LocalDate now = LocalDate.now();
        for (CertificationDto c : certifications) {
            if (c.getExpiryDate().isBefore(now.plusMonths(1))) {
                expiring.add(c);
            }
        }
        return expiring;
    }
}
