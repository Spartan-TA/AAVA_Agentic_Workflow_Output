package com.wms.ems.certification;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/certifications")
public class CertificationController {

    @Autowired
    private CertificationService certificationService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','HR','SUPERVISOR')")
    public ResponseEntity<Certification> createCertification(@RequestBody Certification cert) {
        return ResponseEntity.ok(certificationService.createCertification(cert));
    }

    @GetMapping("/{employeeId}")
    @PreAuthorize("hasAnyRole('ADMIN','HR','SUPERVISOR','WORKER')")
    public ResponseEntity<List<Certification>> getCertifications(@PathVariable Long employeeId) {
        return ResponseEntity.ok(certificationService.getCertifications(employeeId));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','HR','SUPERVISOR')")
    public ResponseEntity<Certification> updateCertification(@PathVariable Long id, @RequestBody Certification cert) {
        return ResponseEntity.ok(certificationService.updateCertification(id, cert));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public ResponseEntity<Void> deleteCertification(@PathVariable Long id) {
        certificationService.deleteCertification(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/alerts/{days}")
    @PreAuthorize("hasAnyRole('ADMIN','HR','SUPERVISOR')")
    public ResponseEntity<List<Certification>> getExpiringCertifications(@PathVariable int days) {
        return ResponseEntity.ok(certificationService.getExpiringCertifications(days));
    }

    @GetMapping("/validate/{employeeId}/{type}")
    @PreAuthorize("hasAnyRole('ADMIN','HR','SUPERVISOR')")
    public ResponseEntity<Boolean> isCertificationValid(@PathVariable Long employeeId, @PathVariable String type) {
        return ResponseEntity.ok(certificationService.isCertificationValid(employeeId, type));
    }
}
