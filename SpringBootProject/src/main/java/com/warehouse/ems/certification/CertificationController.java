package com.warehouse.ems.certification;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * REST controller for Certification endpoints.
 */
@RestController
@RequestMapping("/certifications")
@Validated
public class CertificationController {
    private final CertificationService certificationService;

    @Autowired
    public CertificationController(CertificationService certificationService) {
        this.certificationService = certificationService;
    }

    /**
     * Create a new certification.
     */
    @PostMapping
    public ResponseEntity<Certification> createCertification(@Valid @RequestBody Certification certification) {
        try {
            Certification created = certificationService.createCertification(certification);
            return new ResponseEntity<>(created, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Get all certifications.
     */
    @GetMapping
    public ResponseEntity<List<Certification>> getAllCertifications() {
        return ResponseEntity.ok(certificationService.getAllCertifications());
    }

    /**
     * Update a certification.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Certification> updateCertification(@PathVariable Long id, @Valid @RequestBody Certification certification) {
        try {
            Certification updated = certificationService.updateCertification(id, certification);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Delete a certification.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCertification(@PathVariable Long id) {
        try {
            certificationService.deleteCertification(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Get certifications expiring soon (default 30 days, or pass ?days=7 for 7 days).
     */
    @GetMapping("/expiring")
    public ResponseEntity<List<Certification>> getExpiringCertifications(@RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(certificationService.getExpiringCertifications(days));
    }
}
