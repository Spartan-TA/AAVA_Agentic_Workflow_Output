package com.companyname.wems.certification.controller;

import com.companyname.wems.certification.model.EmployeeCertification;
import com.companyname.wems.certification.service.CertificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/certifications")
@RequiredArgsConstructor
public class CertificationController {
    private final CertificationService certificationService;

    // Add or update certification
    @PostMapping
    public ResponseEntity<EmployeeCertification> addCertification(@RequestBody EmployeeCertification cert) {
        return ResponseEntity.ok(certificationService.addOrUpdateCertification(cert));
    }

    // Get certifications for employee
    @GetMapping("/employee/{id}")
    public ResponseEntity<List<EmployeeCertification>> getEmployeeCertifications(@PathVariable Long id) {
        return ResponseEntity.ok(certificationService.getEmployeeCertifications(id));
    }

    // Get expiring certifications
    @GetMapping("/expiring")
    public ResponseEntity<List<EmployeeCertification>> getExpiringCertifications() {
        return ResponseEntity.ok(certificationService.getExpiringCertifications());
    }
}