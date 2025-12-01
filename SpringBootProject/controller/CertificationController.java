package com.example.ems.controller;

import com.example.ems.dto.CertificationDto;
import com.example.ems.service.CertificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/certifications")
@Validated
public class CertificationController {
    private final CertificationService certificationService;

    @Autowired
    public CertificationController(CertificationService certificationService) {
        this.certificationService = certificationService;
    }

    @GetMapping
    public ResponseEntity<List<CertificationDto>> getAllCertifications() {
        return ResponseEntity.ok(certificationService.getAllCertifications());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CertificationDto> getCertificationById(@PathVariable Long id) {
        return ResponseEntity.ok(certificationService.getCertificationById(id));
    }

    @PostMapping
    public ResponseEntity<CertificationDto> createCertification(@Valid @RequestBody CertificationDto certificationDto) {
        return ResponseEntity.ok(certificationService.createCertification(certificationDto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CertificationDto> updateCertification(@PathVariable Long id, @Valid @RequestBody CertificationDto certificationDto) {
        return ResponseEntity.ok(certificationService.updateCertification(id, certificationDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCertification(@PathVariable Long id) {
        certificationService.deleteCertification(id);
        return ResponseEntity.noContent().build();
    }
}