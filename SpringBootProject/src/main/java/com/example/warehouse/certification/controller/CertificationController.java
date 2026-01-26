package com.example.warehouse.certification.controller;

import com.example.warehouse.certification.entity.Certification;
import com.example.warehouse.certification.service.CertificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/certifications")
public class CertificationController {
    @Autowired
    private CertificationService certificationService;

    // Get all certifications
    @GetMapping
    public List<Certification> getAllCertifications() {
        return certificationService.getAllCertifications();
    }

    // Get certifications by employee
    @GetMapping("/employee/{employeeId}")
    public List<Certification> getCertificationsByEmployee(@PathVariable Long employeeId) {
        return certificationService.getCertificationsByEmployee(employeeId);
    }

    // Get certification by ID
    @GetMapping("/{id}")
    public ResponseEntity<Certification> getCertificationById(@PathVariable Long id) {
        Optional<Certification> cert = certificationService.getCertificationById(id);
        return cert.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Create new certification
    @PostMapping
    public ResponseEntity<Certification> createCertification(@RequestBody Certification certification) {
        Certification created = certificationService.createCertification(certification);
        return ResponseEntity.ok(created);
    }

    // Update certification
    @PutMapping("/{id}")
    public ResponseEntity<Certification> updateCertification(@PathVariable Long id, @RequestBody Certification certification) {
        Optional<Certification> updated = certificationService.updateCertification(id, certification);
        return updated.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Delete certification
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCertification(@PathVariable Long id) {
        boolean deleted = certificationService.deleteCertification(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
