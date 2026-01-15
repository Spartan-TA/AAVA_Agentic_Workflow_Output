package com.warehouse.training.controller;

import com.warehouse.training.entity.Certification;
import com.warehouse.training.service.CertificationService;
import com.warehouse.training.dto.CertificationDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/certifications")
public class CertificationController {
    @Autowired
    private CertificationService certificationService;

    @GetMapping
    public ResponseEntity<List<Certification>> getAllCertifications() {
        return ResponseEntity.ok(certificationService.getAllCertifications());
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<Certification>> getCertificationsByEmployee(@PathVariable Long employeeId) {
        return ResponseEntity.ok(certificationService.getCertificationsByEmployee(employeeId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Certification> getCertificationById(@PathVariable Long id) {
        return certificationService.getCertificationById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Certification> createCertification(@Valid @RequestBody Certification certification) {
        Certification created = certificationService.createCertification(certification);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Certification> updateCertification(@PathVariable Long id, @Valid @RequestBody Certification certification) {
        Certification updated = certificationService.updateCertification(id, certification);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCertification(@PathVariable Long id) {
        certificationService.deleteCertification(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/expiring")
    public ResponseEntity<List<Certification>> getExpiringCertifications(@RequestParam("before") String beforeDate) {
        LocalDate date = LocalDate.parse(beforeDate);
        return ResponseEntity.ok(certificationService.getExpiringCertifications(date));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Certification>> getCertificationsByStatus(@PathVariable Certification.Status status) {
        return ResponseEntity.ok(certificationService.getCertificationsByStatus(status));
    }
}
